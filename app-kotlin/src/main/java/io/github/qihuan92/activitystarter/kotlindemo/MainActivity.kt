package io.github.qihuan92.activitystarter.kotlindemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.qihuan92.activitystarter.annotation.Builder

@Builder
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}