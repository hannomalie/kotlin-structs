package de.hanno.struct

import org.lwjgl.BufferUtils

class StructArray<T: SlidingWindow<*>>(val size: Int, val factory: () -> T) {
    val slidingWindow = factory()
    val buffer = BufferUtils.createByteBuffer(size* slidingWindow.sizeInBytes)

    fun forEach(function: (T) -> Unit) {
        while(buffer.position() < buffer.capacity() - slidingWindow.sizeInBytes) {
            slidingWindow.buffer = buffer
            function(slidingWindow)
            buffer.position(buffer.position() + slidingWindow.sizeInBytes)
        }
    }
}

fun <T: SlidingWindow<*>> StructArray<T>.copyTo(target: StructArray<T>, resetBuffers: Boolean = true) {
//    val tempArray = ByteArray(size*slidingWindow.sizeInBytes)
//    this.buffer[tempArray].get(tempArray, 0, tempArray.size)
    if(resetBuffers) {
        buffer.rewind()
        target.buffer.rewind()
    }
    target.buffer.put(this.buffer)
}