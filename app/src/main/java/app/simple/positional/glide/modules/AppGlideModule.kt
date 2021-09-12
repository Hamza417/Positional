package app.simple.positional.glide.modules

import android.content.Context
import android.graphics.drawable.Drawable
import app.simple.positional.R
import app.simple.positional.glide.art.Art
import app.simple.positional.glide.art.ArtLoader
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions

@GlideModule
class AppGlideModule : AppGlideModule() {
    override fun isManifestParsingEnabled(): Boolean {
        return false
    }

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setDefaultRequestOptions(RequestOptions().format(DecodeFormat.PREFER_ARGB_8888)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .fallback(R.drawable.ic_pin_01)
                .dontAnimate()
                .dontTransform()
                .error(R.drawable.ic_pin_01))
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.append(Art::class.java, Drawable::class.java, ArtLoader.Factory())
    }
}