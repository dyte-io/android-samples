package com.cloudflare.activespeakerui.sample.utils

import android.graphics.Point
import android.os.Build
import android.view.WindowManager
import androidx.annotation.FloatRange
import androidx.appcompat.app.AppCompatDialog

internal object DialogUtils {
  fun AppCompatDialog.setWidthToScreenPercentage(
      @FloatRange(from = 0.00, to = 1.00) screenWidthPercentage: Float
  ) {
    window?.run {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val bounds = windowManager.currentWindowMetrics.bounds
        setLayout(
            (bounds.width() * screenWidthPercentage).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT)
      } else {
        val size = Point()
        windowManager.defaultDisplay.getSize(size)
        setLayout((size.x * screenWidthPercentage).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
      }
    }
  }
}
