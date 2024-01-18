package io.dyte.activespeakerui.sample.utils

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import dyte.io.uikit.token.DyteBorderRadiusToken
import dyte.io.uikit.token.DyteBorderRadiusToken.BorderRadiusSize
import kotlin.math.roundToInt

object ViewUtils {
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

  public fun Int.dpToPx(): Int = dpToPxFloat().roundToInt()

  public fun Int.dpToPxFloat(): Float = (this * Resources.getSystem().displayMetrics.density)
}
