package app.simple.positional.util

@Suppress("unused")
object ConditionUtils {
    /**
     * Quickly performs a null safety check
     * of a potential null object that has
     * no way to initialize but to throw an
     * exception. This approach is unsafe
     * and should not be used with conditional
     * statements
     *
     * Requires casting to the original object
     *
     * @throws UninitializedPropertyAccessException
     * @return [Any]
     */
    fun Any?.asNotNull(): Any {
        return this ?: throw UninitializedPropertyAccessException()
    }

    /**
     * Check if an object is null
     *
     * @return boolean
     */
    fun Any?.isNull(): Boolean {
        return this == null
    }

    fun Any?.isNotNull(): Boolean {
        return this != null
    }

    fun Number.isZero(): Boolean {
        return this == 0
    }
}
