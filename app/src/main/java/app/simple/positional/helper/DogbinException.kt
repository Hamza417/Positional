package app.simple.positional.helper

class DogbinException(message: String?) : Exception(message) {
    companion object {
        private const val serialVersionUID = 666L
    }
}