package app.simple.positional.singleton

object FloatingButtonStateCommunicator {

    private const val TAG = "FloatingButtonStateCommunicator"

    private var floatingButtonCurrentSize = 0
    private val floatingButtonStateCallbacks = mutableSetOf<FloatingButtonStateCallbacks>()

    fun setFloatingButtonSize(size: Int) {
        floatingButtonCurrentSize = size
    }

    fun getFloatingButtonSize(): Int {
        return floatingButtonCurrentSize
    }

    fun addFloatingButtonStateCallbacks(callback: FloatingButtonStateCallbacks) {
        floatingButtonStateCallbacks.add(callback)
    }

    fun removeFloatingButtonStateCallbacks(callback: FloatingButtonStateCallbacks) {
        floatingButtonStateCallbacks.remove(callback)
    }

    fun notifyFloatingButtonStateChange(size: Int) {
        floatingButtonStateCallbacks.forEach {
            it.onFloatingButtonStateChange(size)
        }
    }

    interface FloatingButtonStateCallbacks {
        fun onFloatingButtonStateChange(size: Int)
    }
}
