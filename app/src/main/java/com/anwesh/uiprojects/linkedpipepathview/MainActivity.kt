package com.anwesh.uiprojects.linkedpipepathview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.pipepathview.PipePathView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PipePathView.create(this)
    }
}
