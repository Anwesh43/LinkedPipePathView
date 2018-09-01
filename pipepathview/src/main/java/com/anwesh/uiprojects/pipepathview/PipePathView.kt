package com.anwesh.uiprojects.pipepathview

/**
 * Created by anweshmishra on 02/09/18.
 */

import android.graphics.Paint
import android.graphics.Canvas
import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.graphics.Color

val nodes : Int = 5

fun Canvas.drawPipePathNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val hGap : Float = h / (nodes + 1)
    val index : Int = i % 2
    val sf : Float = 1f - 2  * index
    val sc1 : Float = Math.min(0.5f, scale) * 2
    val sc2 : Float = Math.min(0.5f, Math.max(scale - 0.5f, 0f)) * 2
    val tw : Float = 0.9f * w
    save()
    translate(w/2, hGap * i + hGap / 2)
    scale(sf, 1f)
    save()
    translate(-tw / 2, 0f)
    paint.strokeWidth = Math.min(w, h) / 60
    paint.strokeCap = Paint.Cap.ROUND
    paint.color = Color.parseColor("#0288D1")
    drawLine(0f, 0f, tw * sc1, 0f, paint)
    drawLine(tw, 0f, tw, hGap * sc2, paint)
    paint.color = Color.WHITE
    drawCircle(tw * sc1, hGap * sc2, hGap/20, paint)
    restore()
    restore()
}

class PipePathView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {
        fun update(cb : (Float) -> Unit) {
            scale += 0.05f * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1 - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {
        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class PPNode(var i : Int, val state : State = State()) {

        var prev : PPNode? = null
        var next : PPNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            next = PPNode(i + 1)
            next?.prev = this
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawPipePathNode(i, state.scale, paint)
            prev?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : PPNode {
            var curr : PPNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class PipePath(var i : Int) {
        private var curr : PPNode = PPNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : PipePathView) {

        private val pp : PipePath = PipePath(0)
        private val animator : Animator = Animator(view)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            pp.draw(canvas, paint)
            animator.animate {
                pp.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            pp.startUpdating {
                animator.start()
            }
        }
    }
}