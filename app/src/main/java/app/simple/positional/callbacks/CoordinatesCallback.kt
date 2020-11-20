package app.simple.positional.callbacks

interface CoordinatesCallback {
    fun onCancel()
    fun onCoordinatesSet(boolean: Boolean)
}