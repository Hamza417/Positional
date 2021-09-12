package app.simple.positional.glide.utils

import android.widget.ImageView
import app.simple.positional.glide.art.Art
import app.simple.positional.glide.modules.GlideApp

object ArtLoader {
    fun ImageView.loadArtDrawable(resource: Int) {
        GlideApp.with(this)
                .load(Art(resource, this.context))
                .into(this)
    }
}