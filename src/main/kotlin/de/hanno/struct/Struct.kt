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

interface Struct<T> {
    val sizeInBytes: kotlin.Int
    fun getMemberStructs(): List<StructDescription<out Any>> = emptyList()
    val buffer: ByteBuffer
    fun ByteBuffer.put(input: T)
    fun <TARGET_TYPE: Struct<*>> saveTo(target: TARGET_TYPE): TARGET_TYPE = buffer.saveTo(target)
    fun <TARGET_TYPE: Struct<*>> ByteBuffer.saveTo(target: TARGET_TYPE): TARGET_TYPE

    fun initFrom(source: Struct<*>) { buffer.initFrom(source) }
    fun ByteBuffer.initFrom(source: Struct<*>)
}

abstract class SimpleStruct<SELF_TYPE> : Struct<SELF_TYPE> {
    private val memberStructs = mutableListOf<StructDescription<out Any>>()
    override fun getMemberStructs() = memberStructs.toList()

    var currentByteOffset: kotlin.Int = 0
    override val buffer: ByteBuffer by lazy { BufferUtils.createByteBuffer(sizeInBytes) }

    override val sizeInBytes by lazy { memberStructs.sumBy { it.bytable.sizeInBytes } }

    operator fun <SELF_TYPE> kotlin.Int.Companion.provideDelegate(thisRef: Struct<SELF_TYPE>, prop: KProperty<*>): ReadWriteProperty<Struct<SELF_TYPE>, kotlin.Int> {
        return registerStructProperty(thisRef, prop)
    }

    private inline fun <OWNER, reified FIELD_TYPE> registerStructProperty(thisRef: OWNER, property: KProperty<*>): ReadWriteProperty<OWNER, FIELD_TYPE> {
        return object: StructProperty<OWNER, FIELD_TYPE>() {
            private val myByteOffset = currentByteOffset

            override fun setValue(thisRef: OWNER, property: KProperty<*>, value: FIELD_TYPE) {
                buffer.putInt(myByteOffset, value as kotlin.Int) // TODO: Check this cast
            }

            override fun getValue(thisRef: OWNER, property: KProperty<*>) = buffer.getInt(myByteOffset) as FIELD_TYPE // TODO: Check this cast
        }.apply {
            memberStructs.add(StructDescription(IntStruct, memberStructs.size))
            currentByteOffset += Integer.BYTES
            this
        }
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

abstract class StructProperty<OWNER, FIELD_TYPE>: ReadWriteProperty<OWNER, FIELD_TYPE>

data class StructDescription<T>(val bytable: Bytable, val index: kotlin.Int)

class Layout(unsortedStructDescriptions: List<StructDescription<Any>>,
             val structDescriptions: List<StructDescription<Any>> = unsortedStructDescriptions.sortedBy { it.index },
             val sizeInBytes: kotlin.Int = unsortedStructDescriptions.sumBy { it.bytable.sizeInBytes }) {
    val memberStructs by lazy {
        structDescriptions.map { it.bytable }
    }
}
