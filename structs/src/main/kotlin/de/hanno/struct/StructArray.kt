package de.hanno.struct

import org.lwjgl.BufferUtils
import java.nio.ByteBuffer

class StructArray<T: SlidingWindow>(val size: Int, val factory: () -> T) {
    val slidingWindow = factory()
    var buffer = BufferUtils.createByteBuffer(size* slidingWindow.sizeInBytes)
        private set

    fun shrink(sizeInBytes: Int, copyContent: Boolean = true) = if(buffer.capacity() > sizeInBytes) {
        resize(sizeInBytes, copyContent)
        true
    } else false

    fun enlarge(sizeInBytes: Int, copyContent: Boolean = true) = if(buffer.capacity() < sizeInBytes) {
        resize(sizeInBytes, copyContent)
        true
    } else false

    fun resize(sizeInBytes: Int, copyContent: Boolean = true) {
        val newBuffer = BufferUtils.createByteBuffer(sizeInBytes)
        if(copyContent) {
            val oldBuffer = buffer
            oldBuffer.copyTo(newBuffer, true)
        }
        buffer = newBuffer
    }

    @JvmOverloads fun forEach(rewindBuffer: Boolean = true, function: (T) -> Unit) {
        buffer.forEach(rewindBuffer, slidingWindow, function)
    }

    @JvmOverloads fun forEachIndexed(rewindBuffer: Boolean = true, function: (Int, T) -> Unit) {
        buffer.forEachIndexed(rewindBuffer, slidingWindow, function)
    }

    fun getAtIndex(index: Int) : T {
        slidingWindow.buffer = this.buffer
        slidingWindow.baseByteOffset = index * slidingWindow.sizeInBytes
        buffer.position(index * slidingWindow.sizeInBytes)
        return slidingWindow
    }
}

@JvmOverloads fun <T: SlidingWindow> StructArray<T>.copyTo(target: StructArray<T>, rewindBuffers: Boolean = true) {
    copyTo(target.buffer, rewindBuffers)
}
@JvmOverloads fun <T: SlidingWindow> StructArray<T>.copyTo(target: ByteBuffer, rewindBuffers: Boolean = true) {
    buffer.copyTo(target, rewindBuffers)
}

@JvmOverloads fun <T: SlidingWindow> StructArray<T>.clone(rewindBuffer: Boolean = true): StructArray<T> {
    return de.hanno.struct.StructArray(this.size, this.factory).apply {
        this@clone.copyTo(this@apply, true)
        if(rewindBuffer) {
            this.buffer.rewind()
        }
    }
}
@JvmOverloads fun ByteBuffer.copyTo(target: ByteBuffer, rewindBuffers: Boolean = true) {
    val positionBefore = position()
    if(rewindBuffers) {
        rewind()
        target.rewind()
    }
    if(capacity() > target.capacity()) {
        val array = toArray(true, target.capacity())
        target.put(array, 0, array.size)
        target.rewind()
    } else {
        target.put(this)
    }
    if(rewindBuffers) {
        rewind()
    } else {
        position(positionBefore)
    }
}

@JvmOverloads fun ByteBuffer.toArray(rewindBuffer: Boolean = true, sizeInBytes: Int = capacity()): ByteArray {
    val positionBefore = position()
    return if(rewindBuffer) {
        rewind()
        ByteArray(sizeInBytes).apply {
            get(this, 0, this.size)
        }
    } else {
        ByteArray(Math.max(sizeInBytes, remaining())).apply {
            get(this, position(), this.size)
        }
    }.apply {
        if(rewindBuffer) {
            rewind()
        } else {
            position(positionBefore)
        }
    }
}

@JvmOverloads fun <T:SlidingWindow> ByteBuffer.forEach(rewindBuffer: Boolean = true, slidingWindow: T, function: (T) -> Unit) {
    if (rewindBuffer) {
        rewind()
    }
    while(position() <= capacity() - slidingWindow.sizeInBytes) {
        slidingWindow.buffer = this
        slidingWindow.baseByteOffset = position()
        function(slidingWindow)
        position(position() + slidingWindow.sizeInBytes)
    }
}
@JvmOverloads fun <T:SlidingWindow> ByteBuffer.forEachIndexed(rewindBuffer: Boolean = true, slidingWindow: T, function: (Int, T) -> Unit) {
    val positionBefore = position()
    if (rewindBuffer) {
        rewind()
    }

    var counter = 0
    while(position() <= capacity() - slidingWindow.sizeInBytes) {
        slidingWindow.buffer = this
        slidingWindow.baseByteOffset = position()
        function(counter, slidingWindow)
        position(position() + slidingWindow.sizeInBytes)
        counter++
    }

    if(rewindBuffer) {
        rewind()
    } else {
        position(positionBefore)
    }
}
