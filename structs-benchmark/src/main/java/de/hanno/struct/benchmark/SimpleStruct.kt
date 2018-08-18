package de.hanno.struct.benchmark

import de.hanno.memutil.MemUtilUnsafe
import de.hanno.struct.Struct
import de.hanno.struct.StructProperty
import java.nio.ByteBuffer
import kotlin.reflect.KProperty

class SimpleStruct(parent: Struct? = null) : Struct(parent) {
    val a by 0
    val b by 0.0f
    val c by 0L
}
class SimpleMutableStruct(parent: Struct? = null): Struct(parent) {
    var a by 0
    var b by 0.0f
    var c by 0L
}

class KotlinSimpleSlidingWindow(private val buffer: ByteBuffer) {
    var baseByteOffset = 0

    var x: Float
        get() = buffer.getFloat(baseByteOffset)
        set(x) {
            buffer.putFloat(baseByteOffset, x)
        }
    var y: Float
        get() = buffer.getFloat(baseByteOffset + 4)
        set(y) {
            buffer.putFloat(baseByteOffset + 4, y)
        }
    var z: Float
        get() = buffer.getFloat(baseByteOffset + 8)
        set(z) {
            buffer.putFloat(baseByteOffset + 8, z)
        }
}

class KotlinDelegatedPropertySimpleSlidingWindow(val buffer: ByteBuffer) {
    var baseByteOffset = 0

    var x by FloatProperty(0)
    var y by FloatProperty(4)
    var z by FloatProperty(8)
}
class KotlinDelegatedPropertyUnsafeSimpleSlidingWindow(val buffer: ByteBuffer) {
    var baseByteOffset = 0

    var x by UnsafeFloatProperty(0)
    var y by UnsafeFloatProperty(4)
    var z by UnsafeFloatProperty(8)
}
class KotlinDelegatedPropertyUnsafeSlidingWindow(override val buffer: ByteBuffer): Struct(null) {
    var x by 0f
    var y by 0f
    var z by 0f
}

class FloatProperty(override var localByteOffset: Long): StructProperty {
    override val sizeInBytes = 4

    inline operator fun setValue(thisRef: KotlinDelegatedPropertySimpleSlidingWindow, property: KProperty<*>, value: Float) {
        thisRef.buffer.putFloat((thisRef.baseByteOffset + localByteOffset).toInt(), value)
    }
    inline operator fun getValue(thisRef: KotlinDelegatedPropertySimpleSlidingWindow, property: KProperty<*>) = thisRef.buffer.getFloat((thisRef.baseByteOffset + localByteOffset).toInt())
}
class UnsafeFloatProperty(override var localByteOffset: Long): StructProperty {
    override val sizeInBytes = 4

    inline operator fun setValue(thisRef: KotlinDelegatedPropertyUnsafeSimpleSlidingWindow, property: KProperty<*>, value: Float) {
        unsafeMemUtil.putFloat(thisRef.buffer, thisRef.baseByteOffset + localByteOffset, value)
    }
    inline operator fun getValue(thisRef: KotlinDelegatedPropertyUnsafeSimpleSlidingWindow, property: KProperty<*>) = unsafeMemUtil.getFloat(thisRef.buffer, (thisRef.baseByteOffset + localByteOffset).toLong())

    companion object {
        val unsafeMemUtil = MemUtilUnsafe()
    }
}
