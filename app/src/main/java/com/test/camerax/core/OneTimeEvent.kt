package com.test.camerax.core

data class OneTimeEvent<T>(
    val data: T
) {
    private var isProcessed: Boolean = false

    fun process(block: (T) -> Unit) {
        if (!isProcessed) {
            block.invoke(data);
            isProcessed = true;
        }
    }

}