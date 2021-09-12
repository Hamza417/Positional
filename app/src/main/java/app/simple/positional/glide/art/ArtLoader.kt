package app.simple.positional.glide.art

import android.graphics.drawable.Drawable
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey

class ArtLoader : ModelLoader<Art, Drawable> {
    override fun buildLoadData(model: Art, width: Int, height: Int, options: Options): ModelLoader.LoadData<Drawable>? {
        return ModelLoader.LoadData(ObjectKey(model), ArtFetcher(art = model))
    }

    override fun handles(model: Art): Boolean {
        return true
    }

    internal class Factory : ModelLoaderFactory<Art, Drawable> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<Art, Drawable> {
            return ArtLoader()
        }

        override fun teardown() {
            /* no-op */
        }
    }
}