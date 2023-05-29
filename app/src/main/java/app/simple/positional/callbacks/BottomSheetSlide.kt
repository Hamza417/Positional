package app.simple.positional.callbacks

interface BottomSheetSlide {
    fun onBottomSheetSliding(slideOffset: Float, animate: Boolean)
    fun onMapClicked(fullScreen: Boolean)
}