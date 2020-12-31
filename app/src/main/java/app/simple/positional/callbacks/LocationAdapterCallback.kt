package app.simple.positional.callbacks

import app.simple.positional.model.Locations
import org.jetbrains.annotations.NotNull

interface LocationAdapterCallback {
    fun onLocationItemClicked(@NotNull locations: Locations)
}