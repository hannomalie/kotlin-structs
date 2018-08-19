package de.hanno.struct

import org.lwjgl.BufferUtils
import java.nio.ByteBuffer

interface StructArray<T>: Structable {
    val slidingWindow: T
    val size: Int
    val factory: (Struct) -> T
    fun getAtIndex(index: Int) : T
    val indices: IntRange
    operator fun get(index: Int): T
}

class StaticStructArray<T: Struct>(parent: Struct? = null, override val size: Int, override val factory: (Struct) -> T): StructArray<T>, Struct(parent) {
    override val indices: IntRange
        get() = IntRange(0, size)
    override val slidingWindow = factory(this)
    override val sizeInBytes = size * slidingWindow.sizeInBytes

    override fun getAtIndex(index: Int) : T {
        slidingWindow.localByteOffset = (index * slidingWindow.sizeInBytes).toLong()
        return slidingWindow
    }
    override operator fun get(index: Int) = getAtIndex(index)
}

class ResizableStructArray<T:Struct>(parent: Struct? = null, override var size: Int, override val factory: (Struct) -> T): StructArray<T>, Struct(parent) {
    override val indices: IntRange
        get() = IntRange(0, size)
    override var slidingWindow = factory(this)
    override val sizeInBytes = size * slidingWindow.sizeInBytes


    override var buffer: ByteBuffer = super.buffer
        set(value) {
            field = value
            slidingWindow = factory(this)
        }

    override fun getAtIndex(index: Int) : T {
        slidingWindow.localByteOffset = (index * slidingWindow.sizeInBytes).toLong()
        return slidingWindow
    }
    override operator fun get(index: Int) = getAtIndex(index)

    fun shrinkToBytes(sizeInBytes: Int, copyContent: Boolean = true) = shrink(sizeInBytes/slidingWindow.sizeInBytes, copyContent)

    fun shrink(size: Int, copyContent: Boolean = true) = if(buffer.capacity() > (size*slidingWindow.sizeInBytes)) {
        resize(size, copyContent)
        true
    } else false

    fun enlargeToBytes(size: Int, copyContent: Boolean = true) = enlarge(size/slidingWindow.sizeInBytes, copyContent)

    fun enlarge(size: Int, copyContent: Boolean = true) = if(buffer.capacity() < (size*slidingWindow.sizeInBytes)) {
        resize(size, copyContent)
        true
    } else false

    fun resizeToBytes(size: Int, copyContent: Boolean = true) = resize(size/slidingWindow.sizeInBytes, copyContent)

    fun resize(size: Int, copyContent: Boolean = true) {
        val targetSize = Math.max(1, size)
        val newBuffer = BufferUtils.createByteBuffer(targetSize * slidingWindow.sizeInBytes)
        if(copyContent) {
            val oldBuffer = buffer
            oldBuffer.copyTo(newBuffer, true, 0)
        }
        this.size = targetSize
        buffer = newBuffer
    }
}

@JvmOverloads inline fun <T:Struct> StructArray<T>.forEach(rewindBuffer: Boolean = true, function: (T) -> Unit) {
    buffer.forEach(rewindBuffer, slidingWindow, function)
}

@JvmOverloads inline fun <T:Struct> StructArray<T>.forEachIndexed(rewindBuffer: Boolean = true, function: (Int, T) -> Unit) {
    this.buffer.forEachIndexed(rewindBuffer, slidingWindow, function)
}

@JvmOverloads inline fun <T: Struct> StructArray<T>.copyTo(target: StructArray<T>, rewindBuffers: Boolean = true) {
    copyTo(target.buffer, rewindBuffers)
}

@JvmOverloads inline fun <T: Struct> StructArray<T>.copyTo(target: ByteBuffer, rewindBuffers: Boolean = true) {
    buffer.copyTo(target, rewindBuffers, 0)
}

@JvmOverloads fun <T: Struct> StructArray<T>.clone(rewindBuffer: Boolean = true): StaticStructArray<T> {
    return StaticStructArray(size = this.size, factory = this.factory).apply {
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

@JvmOverloads inline fun <T:Struct> ByteBuffer.forEach(rewindBuffer: Boolean = true, slidingWindow: T, function: (T) -> Unit) {
    val positionBefore = position()
    if (rewindBuffer) {
        rewind()
    }

    var counter = 0
    val capacity = capacity()
    val slidingWindowSize = slidingWindow.sizeInBytes
    while(counter* slidingWindowSize <= capacity - slidingWindowSize) {
        slidingWindow.localByteOffset = (counter * slidingWindowSize).toLong()
        function(slidingWindow)
        counter++
    }

    if(rewindBuffer) {
        rewind()
    } else {
        position(positionBefore)
    }
}
@JvmOverloads inline fun <T:Struct> ByteBuffer.forEachIndexed(rewindBuffer: Boolean = true, slidingWindow: T, function: (Int, T) -> Unit) {
    val positionBefore = position()
    if (rewindBuffer) {
        rewind()
    }

    var counter = 0
    val capacity = capacity()
    val slidingWindowSize = slidingWindow.sizeInBytes
    while(counter* slidingWindowSize <= capacity - slidingWindowSize) {
        slidingWindow.localByteOffset = (counter * slidingWindowSize).toLong()
        function(counter, slidingWindow)
        counter++
    }

    if(rewindBuffer) {
        rewind()
    } else {
        position(positionBefore)
    }
}
