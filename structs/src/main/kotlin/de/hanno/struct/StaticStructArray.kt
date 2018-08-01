package de.hanno.struct

import org.lwjgl.BufferUtils
import java.nio.ByteBuffer

interface StructArray<T>: Structable {
    val slidingWindow: T
    val size: Int
    val factory: (Struct) -> T
    fun getAtIndex(index: Int) : T
}

class StaticStructArray<T: Struct>(parent: Struct? = null, override val size: Int, override val factory: (Struct) -> T): StructArray<T>, Struct(parent) {
    override val baseByteOffset = parent?.getCurrentLocalByteOffset() ?: 0
    override val slidingWindow = factory(this)
    override val sizeInBytes = size * slidingWindow.sizeInBytes

    override fun getAtIndex(index: Int) : T {
        slidingWindow.slidingWindowOffset = index * slidingWindow.sizeInBytes
        return slidingWindow
    }
}

class ResizableStructArray<T:Struct>(parent: Struct? = null, override var size: Int, override val factory: (Struct) -> T): StructArray<T>, Struct(parent) {
    override val baseByteOffset = parent?.getCurrentLocalByteOffset() ?: 0
    override var slidingWindow = factory(this)
    override val sizeInBytes = size * slidingWindow.sizeInBytes


    override var buffer: ByteBuffer = super.buffer
        set(value) {
            field = value
            slidingWindow = factory(this)
        }

    override fun getAtIndex(index: Int) : T {
        slidingWindow.slidingWindowOffset = index * slidingWindow.sizeInBytes
        return slidingWindow
    }

    fun shrink(size: Int, copyContent: Boolean = true) = if(buffer.capacity() > (size*slidingWindow.sizeInBytes)) {
        resize(size, copyContent)
        true
    } else false

    fun enlarge(size: Int, copyContent: Boolean = true) = if(buffer.capacity() < (size*slidingWindow.sizeInBytes)) {
        resize(size, copyContent)
        true
    } else false

    fun resize(size: Int, copyContent: Boolean = true) {
        val newBuffer = BufferUtils.createByteBuffer(size * slidingWindow.sizeInBytes)
        if(copyContent) {
            val oldBuffer = buffer
            oldBuffer.copyTo(newBuffer, true, 0)
        }
        this.size = size
        buffer = newBuffer
    }
}

@JvmOverloads fun <T:Struct> StructArray<T>.forEach(rewindBuffer: Boolean = true, function: (T) -> Unit) {
    buffer.forEach(rewindBuffer, slidingWindow, function)
}

@JvmOverloads fun <T:Struct> StructArray<T>.forEachIndexed(rewindBuffer: Boolean = true, function: (Int, T) -> Unit) {
    this.buffer.forEachIndexed(rewindBuffer, slidingWindow, function)
}

@JvmOverloads fun <T: Struct> StructArray<T>.copyTo(target: StructArray<T>, rewindBuffers: Boolean = true) {
    copyTo(target.buffer, rewindBuffers)
}

@JvmOverloads fun <T: Struct> StructArray<T>.copyTo(target: ByteBuffer, rewindBuffers: Boolean = true) {
    buffer.copyTo(target, rewindBuffers, 0)
}

@JvmOverloads fun <T: Struct> StaticStructArray<T>.clone(rewindBuffer: Boolean = true): StaticStructArray<T> {
    return StaticStructArray(size = this.size, factory = this.factory).apply {
        this@clone.copyTo(this@apply, true)
        if(rewindBuffer) {
            this.buffer.rewind()
        }
    }
}
@JvmOverloads fun <T: Struct> ResizableStructArray<T>.clone(rewindBuffer: Boolean = true): ResizableStructArray<T> {
    return ResizableStructArray(size = this.size, factory = this.factory).apply {
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
