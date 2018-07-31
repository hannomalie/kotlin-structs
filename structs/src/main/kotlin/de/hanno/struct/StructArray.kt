package de.hanno.struct

import org.lwjgl.BufferUtils
import java.nio.ByteBuffer

class StructArray<T: Struct>(parent: Struct? = null, val size: Int, val factory: (Struct) -> T): Struct(parent) {
    private var tempBuffer: ByteBuffer? = null
    override val buffer
        get() = parent?.buffer ?: tempBuffer ?: ownBuffer

    override val baseByteOffset = parent?.getCurrentLocalByteOffset() ?: 0

    val slidingWindow = factory(this)
    override val sizeInBytes: Int
        get() = size * slidingWindow.sizeInBytes

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
            oldBuffer.copyTo(newBuffer, true, 0)
        }
        tempBuffer = newBuffer
    }

    @JvmOverloads fun forEach(rewindBuffer: Boolean = true, function: (T) -> Unit) {
        buffer.forEach(rewindBuffer, slidingWindow, function)
    }

    @JvmOverloads fun forEachIndexed(rewindBuffer: Boolean = true, function: (Int, T) -> Unit) {
        buffer.forEachIndexed(rewindBuffer, slidingWindow, function)
    }

    fun getAtIndex(index: Int) : T {
        slidingWindow.slidingWindowOffset = index * slidingWindow.sizeInBytes
        return slidingWindow
    }
}

@JvmOverloads fun <T: Struct> StructArray<T>.copyTo(target: StructArray<T>, rewindBuffers: Boolean = true) {
    copyTo(target.buffer, rewindBuffers)
}
@JvmOverloads fun <T: Struct> StructArray<T>.copyTo(target: ByteBuffer, rewindBuffers: Boolean = true) {
    buffer.copyTo(target, rewindBuffers, 0)
}

@JvmOverloads fun <T: Struct> StructArray<T>.clone(rewindBuffer: Boolean = true): StructArray<T> {
    return de.hanno.struct.StructArray(size = this.size, factory = this.factory).apply {
        this@clone.copyTo(this@apply, true)
        if(rewindBuffer) {
            this.buffer.rewind()
        }
    }
}
@JvmOverloads fun ByteBuffer.copyTo(target: ByteBuffer, rewindBuffers: Boolean = true, targetOffset: Int = 0) {
    val positionBefore = position()
    if(rewindBuffers) {
        rewind()
        target.rewind()
    }
    if(capacity() > target.capacity() - targetOffset) {
        val array = toArray(true, target.capacity())
        target.put(array, targetOffset, array.size)
        target.rewind()
    } else {
        if(positionBefore != targetOffset) { target.position(targetOffset) }
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

@JvmOverloads fun <T:Struct> ByteBuffer.forEach(rewindBuffer: Boolean = true, slidingWindow: T, function: (T) -> Unit) {
    if (rewindBuffer) {
        rewind()
    }
    var counter = 0
    while(counter*slidingWindow.sizeInBytes <= capacity() - slidingWindow.sizeInBytes) {
        slidingWindow.slidingWindowOffset = counter * slidingWindow.sizeInBytes
        function(slidingWindow)
        counter++
    }
}
@JvmOverloads fun <T:Struct> ByteBuffer.forEachIndexed(rewindBuffer: Boolean = true, slidingWindow: T, function: (Int, T) -> Unit) {
    val positionBefore = position()
    if (rewindBuffer) {
        rewind()
    }

    var counter = 0
    while(counter*slidingWindow.sizeInBytes <= capacity() - slidingWindow.sizeInBytes) {
        slidingWindow.slidingWindowOffset = counter * slidingWindow.sizeInBytes
        function(counter, slidingWindow)
        counter++
    }

    if(rewindBuffer) {
        rewind()
    } else {
        position(positionBefore)
    }
}
