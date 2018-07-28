package de.hanno.struct

import org.lwjgl.BufferUtils
import java.lang.IllegalStateException
import java.nio.ByteBuffer
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface Sized {
    val sizeInBytes: Int
}

interface Bufferable: Sized {
    val buffer: ByteBuffer
}

interface Structable: Bufferable {
    val baseByteOffset: Int
    val memberStructs: MutableList<StructProperty>
    fun getCurrentLocalByteOffset() = memberStructs.sumBy { it.sizeInBytes }

    operator fun Int.provideDelegate(thisRef: Structable, prop: KProperty<*>): IntProperty {
        return IntProperty(getCurrentLocalByteOffset()).apply { thisRef.register(this@apply) }
    }
    operator fun Float.provideDelegate(thisRef: Structable, prop: KProperty<*>): FloatProperty {
        return FloatProperty(getCurrentLocalByteOffset()).apply { thisRef.register(this@apply) }
    }
    operator fun Double.provideDelegate(thisRef: Structable, prop: KProperty<*>): DoubleProperty {
        return DoubleProperty(getCurrentLocalByteOffset()).apply { thisRef.register(this@apply) }
    }
    operator fun Long.provideDelegate(thisRef: Structable, prop: KProperty<*>): LongProperty {
        return LongProperty(getCurrentLocalByteOffset()).apply { thisRef.register(this@apply) }
    }

    operator fun <FIELD: Structable> FIELD.provideDelegate(thisRef: Structable, prop: KProperty<*>): GenericStructProperty<Structable, FIELD> {
        return object : GenericStructProperty<Structable, FIELD> {
            override val sizeInBytes = this@provideDelegate.sizeInBytes
            override val localByteOffset = thisRef.getCurrentLocalByteOffset()

            var currentRef = this@provideDelegate

            override fun getValue(thisRef: Structable, property: KProperty<*>): FIELD {
                return currentRef
            }

            override fun setValue(thisRef: Structable, property: KProperty<*>, value: FIELD) {
                currentRef = value
            }

        }.apply { thisRef.register(this) }
    }

    fun register(structProperty: StructProperty) {
        memberStructs.add(structProperty)
    }
}

fun <T: Bufferable> T.copyTo(target: T) {
    target.buffer.put(buffer)
}
fun <T: Bufferable, S: Bufferable> T.copyToOther(target: S) {
    if(target.sizeInBytes > sizeInBytes) {
        target.buffer.put(buffer)
    } else {
        val tempArray = ByteArray(sizeInBytes)
        this.buffer.rewind()
        this.buffer.get(tempArray, 0, sizeInBytes)
        target.buffer.put(tempArray, 0, sizeInBytes)
    }
}
fun <T: Bufferable> T.copyFrom(target: T) {
    target.copyTo(this)
}

abstract class Struct(open val parent: Structable? = null): Structable {
    override val memberStructs = mutableListOf<StructProperty>()
    override val sizeInBytes by lazy {
        memberStructs.sumBy { it.sizeInBytes }
    }
    override val baseByteOffset: Int = parent?.getCurrentLocalByteOffset() ?: 0
        get() {
            val tmpParent = parent
            return if(tmpParent != null) {
                tmpParent.baseByteOffset + field
            } else field
        }
    private val reserveBuffer by lazy { BufferUtils.createByteBuffer(sizeInBytes) }
    override val buffer by lazy {
        parent?.buffer ?: reserveBuffer
    }
}

private val emptyBuffer = BufferUtils.createByteBuffer(0)
abstract class SlidingWindow : Struct() {
    override var baseByteOffset: Int = 0
        get() = buffer.position()
    override var buffer: ByteBuffer = emptyBuffer
        get() = parent?.buffer ?: field
}

interface StructProperty {
    val localByteOffset: Int
    val sizeInBytes: Int
}
interface GenericStructProperty<OWNER_TYPE: Structable, FIELD_TYPE> : ReadWriteProperty<OWNER_TYPE, FIELD_TYPE>, StructProperty

class IntProperty(override var localByteOffset: Int):StructProperty {
    override val sizeInBytes = 4

    operator fun setValue(thisRef: Structable, property: KProperty<*>, value: Int) {
        thisRef.buffer.putInt(thisRef.baseByteOffset + localByteOffset, value)
    }
    operator fun getValue(thisRef: Structable, property: KProperty<*>) = thisRef.buffer.getInt(thisRef.baseByteOffset + localByteOffset)
}

class FloatProperty(override var localByteOffset: Int):StructProperty {
    override val sizeInBytes = 4

    operator fun setValue(thisRef: Structable, property: KProperty<*>, value: Float) {
        thisRef.buffer.putFloat(thisRef.baseByteOffset + localByteOffset, value)
    }
    operator fun getValue(thisRef: Structable, property: KProperty<*>) = thisRef.buffer.getFloat(thisRef.baseByteOffset + localByteOffset)
}

class DoubleProperty(override var localByteOffset: Int):StructProperty {
    override val sizeInBytes = 8

    operator fun setValue(thisRef: Structable, property: KProperty<*>, value: Double) {
        thisRef.buffer.putDouble(thisRef.baseByteOffset + localByteOffset, value)
    }
    operator fun getValue(thisRef: Structable, property: KProperty<*>) = thisRef.buffer.getDouble(thisRef.baseByteOffset + localByteOffset)
}
class LongProperty(override var localByteOffset: Int):StructProperty {
    override val sizeInBytes = 8

    operator fun setValue(thisRef: Structable, property: KProperty<*>, value: Long) {
        thisRef.buffer.putLong(thisRef.baseByteOffset + localByteOffset, value)
    }
    operator fun getValue(thisRef: Structable, property: KProperty<*>) = thisRef.buffer.getLong(thisRef.baseByteOffset + localByteOffset)
}
