package de.hanno.struct

import org.lwjgl.BufferUtils
import java.nio.ByteBuffer
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface Bytable {
    val sizeInBytes: Int
}

object IntStruct: Bytable {
    fun ByteBuffer.put(input: Int) { this.putInt(input) }
    fun ByteBuffer.get(target: Int): Int = this.int

    override val sizeInBytes: Int = 32
}

object FloatStruct: Bytable {
    fun ByteBuffer.put(input: Float) { this.putFloat(input) }
    fun ByteBuffer.get(target: Float): Float = this.float
    override val sizeInBytes: Int = 32
}

object DoubleStruct: Bytable {
    fun ByteBuffer.put(input: Double) { this.putDouble(input) }
    fun ByteBuffer.get(target: Double): Double = this.double
    override val sizeInBytes: Int = 64
}

object LongStruct: Bytable {
    fun ByteBuffer.put(input: Long) { this.putLong(input) }
    fun ByteBuffer.get(target: Long): Long = this.long
    override val sizeInBytes: Int = 64
}

interface Struct<T>: Bytable {
    fun getMemberStructs(): List<StructDescription> = emptyList()
    val buffer: ByteBuffer
    fun ByteBuffer.put(input: T)
    fun <TARGET_TYPE: Struct<*>> saveTo(target: TARGET_TYPE): TARGET_TYPE = buffer.saveTo(target)
    fun <TARGET_TYPE: Struct<*>> ByteBuffer.saveTo(target: TARGET_TYPE): TARGET_TYPE

    fun initFrom(source: Struct<*>) { buffer.initFrom(source) }
    fun ByteBuffer.initFrom(source: Struct<*>)
}

abstract class SimpleStruct<SELF_TYPE> : Struct<SELF_TYPE> {
    private val memberStructs = mutableListOf<StructDescription>()
    override fun getMemberStructs() = memberStructs.toList()

    var currentByteOffset: kotlin.Int = 0
    override val buffer: ByteBuffer by lazy { BufferUtils.createByteBuffer(sizeInBytes) }

    override val sizeInBytes by lazy {
        memberStructs.sumBy {
            it.bytable.sizeInBytes
        }
    }

    operator fun Int.Companion.provideDelegate(thisRef: Struct<SELF_TYPE>, prop: KProperty<*>): StructProperty<Struct<SELF_TYPE>, Int> {
        return IntProperty<Struct<SELF_TYPE>>(currentByteOffset, thisRef).register(IntStruct)
    }
    operator fun Float.Companion.provideDelegate(thisRef: Struct<SELF_TYPE>, prop: KProperty<*>): StructProperty<Struct<SELF_TYPE>, Float> {
        return FloatProperty<Struct<SELF_TYPE>>(currentByteOffset, thisRef).register(FloatStruct)
    }
    operator fun Double.Companion.provideDelegate(thisRef: Struct<SELF_TYPE>, prop: KProperty<*>): StructProperty<Struct<SELF_TYPE>, Double> {
        return DoubleProperty<Struct<SELF_TYPE>>(currentByteOffset, thisRef).register(DoubleStruct)
    }


    operator fun <FIELD_TYPE: Struct<*>> FIELD_TYPE.provideDelegate(thisRef: Struct<SELF_TYPE>, prop: KProperty<*>): StructProperty<Struct<SELF_TYPE>, FIELD_TYPE> {

        return object: StructProperty<Struct<SELF_TYPE>, FIELD_TYPE>() {
            override fun getValue(thisRef: Struct<SELF_TYPE>, property: KProperty<*>): FIELD_TYPE {
                return this@provideDelegate
            }

            override fun setValue(thisRef: Struct<SELF_TYPE>, property: KProperty<*>, value: FIELD_TYPE) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override val byteOffset = currentByteOffset
            override val sizeInBytes = this@provideDelegate.sizeInBytes
        }.register(this)

    }

    private fun <FIELD_TYPE> StructProperty<Struct<SELF_TYPE>, FIELD_TYPE>.register(struct: Bytable): StructProperty<Struct<SELF_TYPE>, FIELD_TYPE> {
        currentByteOffset += this.sizeInBytes
        memberStructs.add(StructDescription(struct, memberStructs.size))
        return this
    }

    override fun ByteBuffer.put(input: SELF_TYPE) {
        getMemberStructs().forEach {
            with(it.bytable) {
                this@put.put(input)
            }
        }
    }

    override fun <TARGET_TYPE: Struct<*>> ByteBuffer.saveTo(target: TARGET_TYPE): TARGET_TYPE {
        val src = this.duplicate()
        src.position(0)
        src.limit(this@SimpleStruct.sizeInBytes)
        target.buffer.put(src)
        return target //TODO: Remove this?
    }

    override fun ByteBuffer.initFrom(source: Struct<*>) {
        val src = source.buffer.duplicate()
        src.limit(source.sizeInBytes)
        buffer.put(src)
    }
}

abstract class StructProperty<OWNER, FIELD_TYPE>: ReadWriteProperty<OWNER, FIELD_TYPE>{
    protected abstract val byteOffset: Int
    abstract val sizeInBytes: Int
}

data class StructDescription(val bytable: Bytable, val index: kotlin.Int)

class IntProperty<OWNER>(currentByteOffset: Int, val bufferProvider: Struct<*>): StructProperty<OWNER, Int>() {
    override val sizeInBytes = Integer.BYTES
    override val byteOffset = currentByteOffset

    override fun setValue(thisRef: OWNER, property: KProperty<*>, value: Int) {
        bufferProvider.buffer.putInt(byteOffset, value)
    }

    override fun getValue(thisRef: OWNER, property: KProperty<*>) = bufferProvider.buffer.getInt(byteOffset)
}

class FloatProperty<OWNER>(currentByteOffset: Int, val bufferProvider: Struct<*>): StructProperty<OWNER, Float>() {
    override val sizeInBytes = java.lang.Float.BYTES
    override val byteOffset = currentByteOffset

    override fun setValue(thisRef: OWNER, property: KProperty<*>, value: Float) {
        bufferProvider.buffer.putFloat(byteOffset, value)
    }

    override fun getValue(thisRef: OWNER, property: KProperty<*>) = bufferProvider.buffer.getFloat(byteOffset)
}

class DoubleProperty<OWNER>(currentByteOffset: Int, val bufferProvider: Struct<*>): StructProperty<OWNER, Double>() {
    override val sizeInBytes = java.lang.Float.BYTES
    override val byteOffset = currentByteOffset

    override fun setValue(thisRef: OWNER, property: KProperty<*>, value: Double) {
        bufferProvider.buffer.putDouble(byteOffset, value)
    }

    override fun getValue(thisRef: OWNER, property: KProperty<*>) = bufferProvider.buffer.getDouble(byteOffset)
}
