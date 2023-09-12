package com.example.tictactoecustomviewapp

import android.content.Context
import android.view.WindowManager

class Ulities {
    companion object {
        fun getScreenWidth(context: Context): Int {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager.defaultDisplay
            val size = android.graphics.Point()
            display.getSize(size)
            return size.x
        }
    }
}