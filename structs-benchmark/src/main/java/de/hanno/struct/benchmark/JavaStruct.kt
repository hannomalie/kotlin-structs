package de.hanno.struct.benchmark

import de.hanno.struct.Struct
import de.hanno.struct.StructProperty
import de.hanno.struct.Structable
import java.nio.ByteBuffer
import kotlin.reflect.KProperty

class JavaStruct(parent: Struct? = null) : Struct(parent) {
    val a by 0
    val b by 0.0f
    val c by 0L
}
class JavaMutableStruct(parent: Struct? = null): Struct(parent) {
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

class KotlinDelegatedPropertySlidingWindow(val buffer: ByteBuffer) {
    var baseByteOffset = 0

    var x by FloatProperty(0)
    var y by FloatProperty(4)
    var z by FloatProperty(8)
}
class KotlinDelegatedInlinedPropertySlidingWindow(val buffer: ByteBuffer) {
    var baseByteOffset = 0

    var x by InlinedFloatProperty(0)
    var y by InlinedFloatProperty(4)
    var z by InlinedFloatProperty(8)
}

class FloatProperty(override var localByteOffset: Int): StructProperty {
    override val sizeInBytes = 4

    operator fun setValue(thisRef: KotlinDelegatedPropertySlidingWindow, property: KProperty<*>, value: Float) {
        thisRef.buffer.putFloat(thisRef.baseByteOffset + localByteOffset, value)
    }
    operator fun getValue(thisRef: KotlinDelegatedPropertySlidingWindow, property: KProperty<*>) = thisRef.buffer.getFloat(thisRef.baseByteOffset + localByteOffset)
}
class InlinedFloatProperty(override var localByteOffset: Int): StructProperty {
    override val sizeInBytes = 4

    inline operator fun setValue(thisRef: KotlinDelegatedInlinedPropertySlidingWindow, property: KProperty<*>, value: Float) {
        thisRef.buffer.putFloat(thisRef.baseByteOffset + localByteOffset, value)
    }
    inline operator fun getValue(thisRef: KotlinDelegatedInlinedPropertySlidingWindow, property: KProperty<*>) = thisRef.buffer.getFloat(thisRef.baseByteOffset + localByteOffset)
}