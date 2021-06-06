package app.simple.positional.decorations.views

import android.content.Context
import android.util.AttributeSet
import android.widget.EdgeEffect
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.simple.positional.R
import app.simple.positional.decorations.viewholders.VerticalListViewHolder
import app.simple.positional.util.StatusBarHeight

/**
 * Custom recycler view with nice layout animation and
 * smooth overscroll effect and various states retention
 */
class CustomRecyclerView(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs) {

    init {

        context.theme.obtainStyledAttributes(attrs, R.styleable.CustomRecyclerView, 0, 0).apply {
            try {
                if (getBoolean(R.styleable.CustomRecyclerView_statusBarPaddingRequired, true)) {
                    setPadding(paddingLeft, StatusBarHeight.getStatusBarHeight(resources) + paddingTop, paddingRight, paddingBottom)
                }
            } finally {
                recycle()
            }
        }

        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        setHasFixedSize(true)

        this.edgeEffectFactory = object : RecyclerView.EdgeEffectFactory() {
            override fun createEdgeEffect(recyclerView: RecyclerView, direction: Int): EdgeEffect {

                return object : EdgeEffect(recyclerView.context) {
                    override fun onPull(deltaDistance: Float) {
                        super.onPull(deltaDistance)
                        handlePull(deltaDistance)
                    }

                    override fun onPull(deltaDistance: Float, displacement: Float) {
                        super.onPull(deltaDistance, displacement)
                        handlePull(deltaDistance)
                    }

                    private fun handlePull(deltaDistance: Float) {
                        /**
                         * This is called on every touch event while the list is scrolled with a finger.
                         * simply update the view properties without animation.
                         */
                        val sign = if (direction == DIRECTION_BOTTOM) -1 else 1
                        val rotationDelta = sign * deltaDistance * VerticalListViewHolder.overScrollRotationMagnitude
                        val translationYDelta = sign * recyclerView.width * deltaDistance * VerticalListViewHolder.overScrollTranslationMagnitude

                        recyclerView.forEachVisibleHolder { holder: VerticalListViewHolder ->
                            holder.rotation.cancel()
                            holder.translationY.cancel()
                            holder.itemView.rotation += rotationDelta
                            holder.itemView.translationY += translationYDelta
                        }
                    }

                    override fun onRelease() {
                        super.onRelease()
                        /**
                         * The finger is lifted. This is when we should start the animations to bring
                         * the view property values back to their resting states.
                         */
                        recyclerView.forEachVisibleHolder { holder: VerticalListViewHolder ->
                            holder.rotation.start()
                            holder.translationY.start()
                        }
                    }

                    override fun onAbsorb(velocity: Int) {
                        super.onAbsorb(velocity)
                        val sign = if (direction == DIRECTION_BOTTOM) -1 else 1

                        /**
                         * The list has reached the edge on fling
                         */
                        val translationVelocity = sign * velocity * VerticalListViewHolder.flingTranslationMagnitude
                        recyclerView.forEachVisibleHolder { holder: VerticalListViewHolder ->
                            holder.translationY
                                    .setStartVelocity(translationVelocity)
                                    .start()
                        }
                    }
                }
            }
        }
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(adapter)
        adapter?.stateRestorationPolicy = Adapter.StateRestorationPolicy.ALLOW
        scheduleLayoutAnimation()
    }

    private inline fun <reified T : VerticalListViewHolder> RecyclerView.forEachVisibleHolder(action: (T) -> Unit) {
        for (i in 0 until childCount) {
            action(getChildViewHolder(getChildAt(i)) as T)
        }
    }
}
