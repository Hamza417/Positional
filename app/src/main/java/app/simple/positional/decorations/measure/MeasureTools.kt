package app.simple.positional.decorations.measure

import android.animation.LayoutTransition
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import app.simple.positional.R
import app.simple.positional.decorations.corners.DynamicCornerLinearLayout

class MeasureTools(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
    DynamicCornerLinearLayout(context, attrs, defStyleAttr) {

    init {
        setProperties()
    }

    private fun setProperties() {
        initViews()
        layoutTransition = LayoutTransition()
    }

    private fun initViews() {
        val view = LayoutInflater.from(context).inflate(R.layout.tools_measure, this, true)
    }

}
