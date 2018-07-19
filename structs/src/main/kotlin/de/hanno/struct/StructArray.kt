package de.hanno.struct

import org.lwjgl.BufferUtils

class StructArray<T: SlidingWindow>(val size: Int, val factory: () -> T) {
    val slidingWindow = factory()
    val buffer = BufferUtils.createByteBuffer(size* slidingWindow.sizeInBytes)

    @JvmOverloads fun forEach(rewindBuffer: Boolean = true, function: (T) -> Unit) {
        if (rewindBuffer) {
            buffer.rewind()
        }
        while(buffer.position() <= buffer.capacity() - slidingWindow.sizeInBytes) {
            slidingWindow.buffer = buffer
            slidingWindow.baseByteOffset = buffer.position()
            function(slidingWindow)
            buffer.position(buffer.position() + slidingWindow.sizeInBytes)
        }
    }

    @JvmOverloads fun forEachIndexed(rewindBuffer: Boolean = true, function: (Int, T) -> Unit) {
        if (rewindBuffer) {
            buffer.rewind()
        }
        var counter = 0
        while(buffer.position() <= buffer.capacity() - slidingWindow.sizeInBytes) {
            slidingWindow.buffer = buffer
            slidingWindow.baseByteOffset = buffer.position()
            function(counter, slidingWindow)
            buffer.position(buffer.position() + slidingWindow.sizeInBytes)
            counter++
        }
    }
}

@JvmOverloads fun <T: SlidingWindow> StructArray<T>.copyTo(target: StructArray<T>, rewindBuffers: Boolean = true) {
    if(rewindBuffers) {
        buffer.rewind()
        target.buffer.rewind()
    }
    target.buffer.put(this.buffer)
}

@JvmOverloads fun <T: SlidingWindow> StructArray<T>.clone(rewindBuffer: Boolean = true): StructArray<T> {
    return de.hanno.struct.StructArray(this.size, this.factory).apply {
        this@clone.copyTo(this@apply, true)
        if(rewindBuffer) {
            this.buffer.rewind()
        }
    }
}
