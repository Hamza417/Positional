package app.simple.positional.util

object ArrayHelper {
    /**
     * Checks if all items in the [ArrayList] are same
     */
    fun ArrayList<Float>.checkIfAllElementsAreSame(): Boolean {
        return this.stream().distinct().allMatch(this[1]::equals)
    }

    /**
     * Checks if the last value of [ArrayList] is equal
     * to the given [value].
     *
     * @return true if value is same else false if value
     * is not same of list does not contain any items
     * @throws ArrayIndexOutOfBoundsException
     */
    fun ArrayList<Float>.isLastValueSame(value: Float): Boolean {
        return try {
            if (this.size == 0) {
                false
            } else {
                this.last() == value
            }
        } catch (e: ArrayIndexOutOfBoundsException) {
            false
        }
    }

    /**
     * Returns the second last element, or `null` if the list is empty.
     */
    fun <T> List<T>.secondLastOrNull(): T? {
        return if (size < 2) null else this[size - 2]
    }
}
