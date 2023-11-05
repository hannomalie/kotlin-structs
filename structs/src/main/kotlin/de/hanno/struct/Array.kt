package de.hanno.struct

import java.lang.foreign.MemorySegment
import java.nio.ByteBuffer

interface Array<T> {
    val size: Int
    val indices: IntRange
    val buffer: MemorySegment
    val sizeInBytes: Int

    fun getAtIndex(index: Int) : T = get(index)
    operator fun get(index: Int): T
    companion object
}

operator fun <T: Struct> Array.Companion.invoke(size: Int, factory: () -> T): StructArray<T> {
    return StructArray(size, factory)
}

class StructArray<T: Struct>(override val size: Int, val factory: () -> T): Struct(), Array<T> {
    override val indices = IntRange(0, size)
    val slidingWindow: SlidingWindow<T> = SlidingWindow(factory().apply { parent = this@StructArray })
    override val sizeInBytes = size * slidingWindow.sizeInBytes

    override operator fun get(index: Int): T {
        val currentSlidingWindow = slidingWindow
        currentSlidingWindow.localByteOffset = (index * currentSlidingWindow.sizeInBytes).toLong()
        return currentSlidingWindow.underlying
    }
}

class StructObjectArray<T: Struct>(override val size: Int, val factory: (Struct) -> T): Struct(), Array<T> {
    val backingList = mutableListOf<T>()
    init {
        for(i in 0 until size) {
            val element = factory(this).apply {
                backingList.add(this)
            }
            this.register(i.toString(), element) // TODO: Likely not correct
        }
    }

    override val indices: IntRange = IntRange(0, size)

    override fun getAtIndex(index: Int) = backingList[index]
    override operator fun get(index: Int) = getAtIndex(index)
}

fun <T: Struct> StructArray<T>.shrink(size: Int, copyContent: Boolean = true) = shrinkToBytes(size * slidingWindow.sizeInBytes, copyContent)


fun <T: Struct> StructArray<T>.shrinkToBytes(sizeInBytes: Int, copyContent: Boolean = true) = if(buffer.byteSize() > sizeInBytes) {
    StructArray(sizeInBytes/slidingWindow.sizeInBytes, factory).apply {
        if(copyContent) {
            val self: Array<T> = this
            copyTo(self)
        }
    }
} else this

fun <T: Struct> StructArray<T>.resize(size: Int, copyContent: Boolean = true) = resizeToBytes(size * slidingWindow.sizeInBytes, copyContent)
fun <T: Struct> StructArray<T>.resizeToBytes(sizeInBytes: Int, copyContent: Boolean = true) = if(buffer.byteSize() != sizeInBytes.toLong()) {
    StructArray(sizeInBytes/slidingWindow.sizeInBytes, factory).apply {
        if(copyContent) {
            val self: Array<T> = this
            copyTo(self)
        }
    }
} else this


fun <T: Struct> StructArray<T>.enlarge(size: Int, copyContent: Boolean = true) = enlargeToBytes(size * slidingWindow.sizeInBytes, copyContent)

fun <T: Struct> StructArray<T>.enlargeToBytes(sizeInBytes: Int, copyContent: Boolean = true) = if(buffer.byteSize() < sizeInBytes.toLong()) {
    StructArray(sizeInBytes/slidingWindow.sizeInBytes, factory).apply {
        if(copyContent) {
            val self: Array<T> = this@apply
            this@enlargeToBytes.copyTo(self)
        }
    }
} else this


@JvmOverloads fun <T:Struct> StructArray<T>.forEach(rewindBuffer: Boolean = true, function: (T) -> Unit) {
    buffer.forEach(rewindBuffer, slidingWindow, function)
}

@JvmOverloads fun <T:Struct> StructArray<T>.forEachIndexed(rewindBuffer: Boolean = true, function: (Int, T) -> Unit) {
    this.buffer.forEachIndexed(rewindBuffer, slidingWindow, function)
}

@JvmOverloads fun <T: Struct> Array<T>.copyTo(target: Array<T>, rewindBuffers: Boolean = true) {
    copyTo(target.buffer, rewindBuffers)
}

@JvmOverloads fun <T: Struct> Array<T>.copyTo(target: MemorySegment, rewindBuffers: Boolean = true) {
    target.copyFrom(buffer)
}

@JvmOverloads fun <T: Struct> StructArray<T>.clone(rewindBuffer: Boolean = true): StructArray<T> {
    return StructArray(size = this.size, factory = this.factory).apply {
        val self: Array<T> = this@apply
        this@clone.copyTo(self, true)
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

@JvmOverloads fun <T:Struct> MemorySegment.forEach(rewindBuffer: Boolean = true, slidingWindow: T, function: (T) -> Unit) {
    forEach(rewindBuffer, SlidingWindow(slidingWindow), function)
}

@JvmOverloads fun <T:Struct> MemorySegment.forEach(rewindBuffer: Boolean = true, slidingWindow: SlidingWindow<T>, function: (T) -> Unit) {
    rewinded<T>(rewindBuffer) {
        performIteration(slidingWindow, function)
    }
}

fun <T : Struct> MemorySegment.performIteration(slidingWindow: SlidingWindow<T>, function: (T) -> Unit) {
    var counter = 0
    val capacity = byteSize()
    val slidingWindowSize = slidingWindow.sizeInBytes
    while (counter * slidingWindowSize <= capacity - slidingWindowSize) {
        slidingWindow.localByteOffset = (counter * slidingWindowSize).toLong()
        function(slidingWindow.underlying)
        counter++
    }
}
inline fun <T: Struct> MemorySegment.rewinded(rewind: Boolean, function: () -> Unit) {
    function()
}

@JvmOverloads inline fun <T:Struct> MemorySegment.forEachIndexed(rewindBuffer: Boolean = true, slidingWindow: T, function: (Int, T) -> Unit) {
    forEachIndexed(rewindBuffer, SlidingWindow(slidingWindow), function)
}
@JvmOverloads inline fun <T:Struct> MemorySegment.forEachIndexed(rewindBuffer: Boolean = true, slidingWindow: SlidingWindow<T>, function: (Int, T) -> Unit) {

    var counter = 0
    val capacity = byteSize()
    val slidingWindowSize = slidingWindow.sizeInBytes
    while(counter* slidingWindowSize <= capacity - slidingWindowSize) {
        slidingWindow.localByteOffset = (counter * slidingWindowSize).toLong()
        function(counter, slidingWindow.underlying)
        counter++
    }
}
