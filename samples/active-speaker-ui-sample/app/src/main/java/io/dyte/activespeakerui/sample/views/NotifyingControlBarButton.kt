package io.dyte.activespeakerui.sample.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Rect
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import dyte.io.uikit.view.controlbarbuttons.ControlBarButtonVariant
import dyte.io.uikit.view.controlbarbuttons.DyteControlBarButton
import io.dyte.activespeakerui.sample.utils.ViewUtils.dpToPxFloat

open class NotifyingControlBarButton : DyteControlBarButton {
  private var _notificationDotRadius: Float = 8.dpToPxFloat()
  private var _notificationDotColor: Int = Color.parseColor("#2160FD")

  private var _notificationCountTextSize: Float = 8.dpToPxFloat()
  private var _notificationCountTextColor: Int = Color.WHITE

  private val notificationDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.FILL
    color = _notificationDotColor
  }

  private val notificationCountTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = _notificationCountTextColor
    textAlign = Align.CENTER
    if (_notificationCountTextSize == 0f) {
      _notificationCountTextSize = textSize
    } else {
      textSize = _notificationCountTextSize
    }
  }

  @get:Dimension
  @setparam: Dimension
  var notificationDotRadius: Float
    get() = _notificationDotRadius
    set(value) {
      _notificationDotRadius = value
      invalidate()
    }

  @get:Dimension
  @setparam: Dimension
  var notificationDotColor: Float
    get() = _notificationDotRadius
    set(value) {
      _notificationDotRadius = value
      invalidateNotificationDotPaint()
    }

  @get:Dimension(unit = Dimension.SP)
  @setparam: Dimension(unit = Dimension.SP)
  var notificationCountTextSize: Float
    get() = _notificationCountTextSize
    set(value) {
      _notificationCountTextSize = value
      invalidateNotificationDotTextPaint()
    }

  @get:ColorInt
  @setparam: ColorInt
  var notificationCountTextColor: Int
    get() = _notificationCountTextColor
    set(value) {
      _notificationCountTextColor = value
      invalidateNotificationDotTextPaint()
    }

  var notificationCount: Int = 0
    set(value) {
      if (value != field && value >= 0) {
        field = value
        invalidate()
      }
    }

  constructor(context: Context) : super(context)

  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
    context,
    attrs,
    defStyleAttr
  )

  override fun dispatchDraw(canvas: Canvas) {
    super.dispatchDraw(canvas)
    if (variant == ControlBarButtonVariant.Companion.BUTTON && notificationCount > 0) {
      drawNotificationDot(canvas)
    }
  }

  private fun drawNotificationDot(canvas: Canvas) {
    // Drawing Notification Dot
    val dotX = width - notificationDotRadius
    val dotY = notificationDotRadius
    canvas.drawCircle(dotX, dotY, notificationDotRadius, notificationDotPaint)

    // Drawing Notification Text
    val mText = formatNotificationCountText(notificationCount)
    val textBounds = Rect()
    notificationCountTextPaint.getTextBounds(mText, 0, mText.length, textBounds)
    // val textWidth = //textPaint.measureText(mText) //textBounds.width()
    val textHeight = textBounds.height()

    val textX = dotX
    val textY = dotY + (textHeight / 2f)
    canvas.drawText(mText, textX, textY, notificationCountTextPaint)
  }

  private fun invalidateNotificationDotPaint() {
    notificationDotPaint.color = _notificationDotColor
  }

  private fun invalidateNotificationDotTextPaint() {
    notificationCountTextPaint.let {
      it.textSize = _notificationCountTextSize
      it.color = _notificationCountTextColor
    }
  }

  private fun formatNotificationCountText(notificationCount: Int): String {
    if (notificationCount in 0..<1000) {
      return notificationCount.toString()
    }

    val kCount = notificationCount / 1000
    return if (kCount < 9) {
      "${kCount}k+"
    } else {
      "9k+"
    }
  }
}