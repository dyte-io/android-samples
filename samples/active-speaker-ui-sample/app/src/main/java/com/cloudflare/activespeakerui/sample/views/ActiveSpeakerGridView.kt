package com.cloudflare.activespeakerui.sample.views

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.cloudflare.activespeakerui.sample.R

class ActiveSpeakerGridView : RelativeLayout {
  constructor(context: Context) : super(context) {
    init(context)
  }

  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
    init(context)
  }

  constructor(
      context: Context,
      attrs: AttributeSet?,
      defStyleAttr: Int
  ) : super(context, attrs, defStyleAttr) {
    init(context)
  }

  private fun init(context: Context) {
    inflate(context, R.layout.active_speaker_grid, this)
  }
}
