package com.cloudflare.activespeakerui.sample.utils

import android.content.Context
import android.content.res.Resources
import android.view.View
import kotlin.math.roundToInt

internal object ViewUtils {
  fun View.visible() {
    this.visibility = View.VISIBLE
  }

  fun View.gone() {
    this.visibility = View.GONE
  }

  fun View.invisible() {
    this.visibility = View.INVISIBLE
  }

  internal fun Context.getOrientation(): Int {
    return resources.configuration.orientation
  }

  fun Int.dpToPx(): Int = dpToPxFloat().roundToInt()

  fun Int.dpToPxFloat(): Float = (this * Resources.getSystem().displayMetrics.density)
}
