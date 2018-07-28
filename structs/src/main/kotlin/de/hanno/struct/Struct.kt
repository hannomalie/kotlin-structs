package de.hanno.struct

import org.lwjgl.BufferUtils
import java.lang.IllegalStateException
import java.nio.ByteBuffer
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface Sizable {
    val sizeInBytes: Int
}

interface Bufferable: Sizable {
    val buffer: ByteBuffer
}

interface Struct: Bufferable {
    val parent: Struct?
    val baseByteOffset: Int
    val memberStructs: MutableList<IStructProperty>
    fun getCurrentLocalByteOffset() = memberStructs.sumBy { it.sizeInBytes }
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

interface SimpleStruct : Struct {

    operator fun Int.provideDelegate(thisRef: SimpleStruct, prop: KProperty<*>): IntProperty {
        return IntProperty(getCurrentLocalByteOffset()).apply { thisRef.register(this@apply) }
    }
    operator fun Float.provideDelegate(thisRef: SimpleStruct, prop: KProperty<*>): FloatProperty {
        return FloatProperty(getCurrentLocalByteOffset()).apply { thisRef.register(this@apply) }
    }
    operator fun Double.provideDelegate(thisRef: SimpleStruct, prop: KProperty<*>): DoubleProperty {
        return DoubleProperty(getCurrentLocalByteOffset()).apply { thisRef.register(this@apply) }
    }
    operator fun Long.provideDelegate(thisRef: SimpleStruct, prop: KProperty<*>): LongProperty {
        return LongProperty(getCurrentLocalByteOffset()).apply { thisRef.register(this@apply) }
    }

    operator fun <FIELD: SimpleStruct> FIELD.provideDelegate(thisRef: SimpleStruct, prop: KProperty<*>): StructProperty<FIELD> {
        return object : StructProperty<FIELD>(sizeInBytes) {
            var currentRef = this@provideDelegate

            override fun getValue(thisRef: SimpleStruct, property: KProperty<*>): FIELD {
                return currentRef
            }

            override fun setValue(thisRef: SimpleStruct, property: KProperty<*>, value: FIELD) {
                currentRef = value
            }

        }.apply { thisRef.register(this@apply) }
    }

    fun register(structProperty: IStructProperty) {
        memberStructs.add(structProperty)
    }
}

abstract class BaseStruct(override val parent: Struct? = null): SimpleStruct {
    override val memberStructs = mutableListOf<IStructProperty>()
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
        val tmpParent = parent
        tmpParent?.buffer ?: reserveBuffer
    }
}

private val emptyBuffer = BufferUtils.createByteBuffer(0)
abstract class SlidingWindow : BaseStruct() {
    override var parent: Struct? = null
        set(value) {
            field = value
            buffer = value?.buffer ?: throw IllegalStateException("Parent doesn't have a buffer!")
            baseByteOffset = value.buffer.position()
        }
    override val memberStructs = mutableListOf<IStructProperty>()
    override val sizeInBytes by lazy {
        memberStructs.sumBy { it.sizeInBytes }
    }
    override var baseByteOffset = 0
    override var buffer: ByteBuffer = parent?.buffer ?: emptyBuffer
}

//TODO: Rename
interface IStructProperty {
    val localByteOffset: Int
    val sizeInBytes: Int
}
interface GenericStructProperty<OWNER_TYPE: SimpleStruct, FIELD_TYPE> : ReadWriteProperty<OWNER_TYPE, FIELD_TYPE>, IStructProperty

abstract class StructProperty<FIELD_TYPE>(override val sizeInBytes: Int): GenericStructProperty<SimpleStruct, FIELD_TYPE> {
    override var localByteOffset = 0
        protected set
}
class IntProperty(override var localByteOffset: Int):IStructProperty {
    override val sizeInBytes = 4

    operator fun setValue(thisRef: SimpleStruct, property: KProperty<*>, value: Int) {
        thisRef.buffer.putInt(thisRef.baseByteOffset + localByteOffset, value)
    }
    operator fun getValue(thisRef: SimpleStruct, property: KProperty<*>) = thisRef.buffer.getInt(thisRef.baseByteOffset + localByteOffset)
}

class FloatProperty(override var localByteOffset: Int):IStructProperty {
    override val sizeInBytes = 4

    operator fun setValue(thisRef: SimpleStruct, property: KProperty<*>, value: Float) {
        thisRef.buffer.putFloat(thisRef.baseByteOffset + localByteOffset, value)
    }
    operator fun getValue(thisRef: SimpleStruct, property: KProperty<*>) = thisRef.buffer.getFloat(thisRef.baseByteOffset + localByteOffset)
}

class DoubleProperty(override var localByteOffset: Int):IStructProperty {
    override val sizeInBytes = 8

    operator fun setValue(thisRef: SimpleStruct, property: KProperty<*>, value: Double) {
        thisRef.buffer.putDouble(thisRef.baseByteOffset + localByteOffset, value)
    }
    operator fun getValue(thisRef: SimpleStruct, property: KProperty<*>) = thisRef.buffer.getDouble(thisRef.baseByteOffset + localByteOffset)
}
class LongProperty(override var localByteOffset: Int):IStructProperty {
    override val sizeInBytes = 8

    operator fun setValue(thisRef: SimpleStruct, property: KProperty<*>, value: Long) {
        thisRef.buffer.putLong(thisRef.baseByteOffset + localByteOffset, value)
    }
    operator fun getValue(thisRef: SimpleStruct, property: KProperty<*>) = thisRef.buffer.getLong(thisRef.baseByteOffset + localByteOffset)
}
