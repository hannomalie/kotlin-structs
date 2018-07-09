package de.hanno.struct

import org.lwjgl.BufferUtils
import java.nio.ByteBuffer
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface Bytable {
    val sizeInBytes: Int
}

interface Bufferable: Bytable {
    val buffer: ByteBuffer
}

interface Struct: Bufferable {
    val memberStructs: MutableList<ReadOnlyStructProperty<*,*>>
    fun getCurrentLocalByteOffset() = memberStructs.sumBy { it.sizeInBytes }
}

fun <T: Struct> T.copyTo(target: T) {
    val tempArray = ByteArray(sizeInBytes)
    this.buffer.rewind()
    this.buffer.get(tempArray, 0, sizeInBytes)
    target.buffer.put(tempArray, 0, sizeInBytes)
}
fun <T: Struct> T.copyFrom(target: T) {
    target.copyTo(this)
}

interface SimpleStruct<SELF_TYPE>: Struct {
    val parent: Struct?
    val baseByteOffset: Int

    operator fun Int.provideDelegate(thisRef: SimpleStruct<SELF_TYPE>, prop: KProperty<*>): StructProperty<SimpleStruct<SELF_TYPE>, Int> {
        return IntProperty<SimpleStruct<SELF_TYPE>>(thisRef, baseByteOffset, getCurrentLocalByteOffset()).apply { this@apply.register(this@apply) }
    }
    operator fun Float.provideDelegate(thisRef: SimpleStruct<SELF_TYPE>, prop: KProperty<*>): StructProperty<SimpleStruct<SELF_TYPE>, Float> {
        return FloatProperty<SimpleStruct<SELF_TYPE>>(thisRef, baseByteOffset, getCurrentLocalByteOffset()).apply { this@apply.register(this@apply) }
    }
    operator fun Double.provideDelegate(thisRef: SimpleStruct<SELF_TYPE>, prop: KProperty<*>): StructProperty<SimpleStruct<SELF_TYPE>, Double> {
        return DoubleProperty<SimpleStruct<SELF_TYPE>>(thisRef, baseByteOffset, getCurrentLocalByteOffset()).apply { this@apply.register(this@apply) }
    }
    operator fun Long.provideDelegate(thisRef: SimpleStruct<SELF_TYPE>, prop: KProperty<*>): StructProperty<SimpleStruct<SELF_TYPE>, Long> {
        return LongProperty<SimpleStruct<SELF_TYPE>>(thisRef, baseByteOffset, getCurrentLocalByteOffset()).apply { this@apply.register(this@apply) }
    }

    operator fun <T: SimpleStruct<*>> T.provideDelegate(thisRef: SimpleStruct<SELF_TYPE>, prop: KProperty<*>): ReadOnlyStructProperty<SimpleStruct<SELF_TYPE>, T> {
        return object : ReadOnlyStructProperty<SimpleStruct<SELF_TYPE>, T>() {
            override val sizeInBytes = this@provideDelegate.sizeInBytes

            override fun getValue(thisRef: SimpleStruct<SELF_TYPE>, property: KProperty<*>): T {
                return this@provideDelegate
            }
        }.apply { this@apply.register(this@apply) }
    }

    fun <FIELD_TYPE> ReadOnlyStructProperty<SimpleStruct<SELF_TYPE>, FIELD_TYPE>.register(structProperty: ReadOnlyStructProperty<SimpleStruct<SELF_TYPE>, FIELD_TYPE>) {
        memberStructs.add(structProperty)
    }
}

abstract class BaseStruct<SELF_TYPE>(override val parent: Struct? = null): SimpleStruct<SELF_TYPE> {
    override val memberStructs = mutableListOf<ReadOnlyStructProperty<*,*>>()
    override val sizeInBytes by lazy {
        memberStructs.sumBy { it.sizeInBytes }
    }
    override val baseByteOffset = parent?.getCurrentLocalByteOffset() ?: 0
    override val buffer by lazy {
        parent?.buffer ?: BufferUtils.createByteBuffer(sizeInBytes)
    }
}

abstract class SlidingWindow<SELF_TYPE>: BaseStruct<SELF_TYPE>() {
    override var parent: Struct? = null
        set(value) {
            field = value
            buffer = value?.buffer
        }
    override val memberStructs = mutableListOf<ReadOnlyStructProperty<*,*>>()
    override val sizeInBytes by lazy {
        memberStructs.sumBy { it.sizeInBytes }
    }
    override val baseByteOffset = 0
    override var buffer: ByteBuffer? = parent?.buffer
}

abstract class ReadOnlyStructProperty<OWNER, FIELD_TYPE>: ReadOnlyProperty<OWNER, FIELD_TYPE> {
    abstract val sizeInBytes: Int

    open var baseByteOffset = 0
        protected set
    open var localByteOffset = 0
        protected set
}

abstract class StructProperty<OWNER, FIELD_TYPE>: ReadOnlyStructProperty<OWNER, FIELD_TYPE>(), ReadWriteProperty<OWNER, FIELD_TYPE>


abstract class AbstractProperty<OWNER, FIELD_TYPE>(override val sizeInBytes: Int) : StructProperty<OWNER, FIELD_TYPE>()

class IntProperty<OWNER>(private val parentStruct: SimpleStruct<*>, override var baseByteOffset: Int = 0, override var localByteOffset: Int): AbstractProperty<OWNER, Int>(Integer.BYTES) {

    override fun setValue(thisRef: OWNER, property: KProperty<*>, value: Int) {
        parentStruct.buffer.putInt(baseByteOffset + localByteOffset, value)
    }
    override fun getValue(thisRef: OWNER, property: KProperty<*>) = parentStruct.buffer.getInt(baseByteOffset + localByteOffset)
}

class FloatProperty<OWNER>(private val parentStruct: SimpleStruct<*>, override var baseByteOffset: Int = 0, override var localByteOffset: Int): AbstractProperty<OWNER, Float>(java.lang.Float.BYTES) {

    override fun setValue(thisRef: OWNER, property: KProperty<*>, value: Float) {
        parentStruct.buffer.putFloat(baseByteOffset + localByteOffset, value)
    }
    override fun getValue(thisRef: OWNER, property: KProperty<*>) = parentStruct.buffer.getFloat(baseByteOffset + localByteOffset)
}

class DoubleProperty<OWNER>(private val parentStruct: SimpleStruct<*>, override var baseByteOffset: Int = 0, override var localByteOffset: Int): AbstractProperty<OWNER, Double>(java.lang.Double.BYTES) {

    override fun setValue(thisRef: OWNER, property: KProperty<*>, value: Double) {
        parentStruct.buffer.putDouble(baseByteOffset + localByteOffset, value)
    }
    override fun getValue(thisRef: OWNER, property: KProperty<*>) = parentStruct.buffer.getDouble(baseByteOffset + localByteOffset)
}
class LongProperty<OWNER>(private val parentStruct: SimpleStruct<*>, override var baseByteOffset: Int = 0, override var localByteOffset: Int): AbstractProperty<OWNER, Long>(java.lang.Long.BYTES) {

    override fun setValue(thisRef: OWNER, property: KProperty<*>, value: Long) {
        parentStruct.buffer.putLong(baseByteOffset + localByteOffset, value)
    }
    override fun getValue(thisRef: OWNER, property: KProperty<*>) = parentStruct.buffer.getLong(baseByteOffset + localByteOffset)
}
