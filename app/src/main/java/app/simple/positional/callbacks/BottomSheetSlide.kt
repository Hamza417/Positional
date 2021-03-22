package app.simple.positional.callbacks

interface BottomSheetSlide {
    fun onBottomSheetSliding(slideOffset: Float)
    fun onMapClicked(fullScreen: Boolean)
}