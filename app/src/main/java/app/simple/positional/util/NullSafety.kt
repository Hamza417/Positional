package app.simple.positional.util

@Suppress("unused")
object NullSafety {
    /**
     * Quickly performs a null safety check
     * of a potential null object that has
     * no way to initialize but to throw an
     * exception
     *
     * Requires casting to the original object
     *
     * @throws UninitializedPropertyAccessException
     * @return [Any]
     */
    fun Any?.asNotNull(): Any {
        return this ?: throw UninitializedPropertyAccessException()
    }
}