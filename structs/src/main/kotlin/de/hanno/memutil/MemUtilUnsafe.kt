package de.hanno.memutil

import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import java.nio.ByteBuffer

class MemUtilUnsafe : MemUtil {
    override fun putDouble(dst: MemorySegment, offset: Long, value: Double) {
        dst.set(ValueLayout.JAVA_BYTE, offset, value.toInt().toByte())
    }

    override fun getDouble(dst: MemorySegment, offset: Long): Double {
        val byte0 = dst.get(ValueLayout.JAVA_BYTE, offset + 0)
        val byte1 = dst.get(ValueLayout.JAVA_BYTE, offset + 1)
        val byte2 = dst.get(ValueLayout.JAVA_BYTE, offset + 2)
        val byte3 = dst.get(ValueLayout.JAVA_BYTE, offset + 3)
        val byte4 = dst.get(ValueLayout.JAVA_BYTE, offset + 4)
        val byte5 = dst.get(ValueLayout.JAVA_BYTE, offset + 5)
        val byte6 = dst.get(ValueLayout.JAVA_BYTE, offset + 6)
        val byte7 = dst.get(ValueLayout.JAVA_BYTE, offset + 7)

        return bytesToBuffer(byteArrayOf(byte0, byte1, byte2, byte3, byte4, byte5, byte6, byte7)).double
    }
    override fun putBoolean(dst: MemorySegment, offset: Long, value: Boolean) {
        dst.set(ValueLayout.JAVA_BYTE, offset, (if(value) 1 else 0).toByte())
    }

    override fun getBoolean(dst: MemorySegment, offset: Long): Boolean {
        return dst.get(ValueLayout.JAVA_BYTE, offset).toInt() != 0
    }

    override fun putFloat(dst: MemorySegment, offset: Long, value: Float) {
        float2ByteArray(value).forEachIndexed { index , value ->
            dst.set(ValueLayout.JAVA_BYTE, offset + index, value)
        }
    }

    fun long2ByteArray(value: Long) = ByteBuffer.allocate(8).putLong(value).array()
    fun float2ByteArray(value: Float) = ByteBuffer.allocate(4).putFloat(value).array()
    fun int2ByteArray(value: Int) = ByteBuffer.allocate(4).putInt(value).array()

    override fun getFloat(dst: MemorySegment, offset: Long): Float {
        val byte0 = dst.get(ValueLayout.JAVA_BYTE, offset + 0)
        val byte1 = dst.get(ValueLayout.JAVA_BYTE, offset + 1)
        val byte2 = dst.get(ValueLayout.JAVA_BYTE, offset + 2)
        val byte3 = dst.get(ValueLayout.JAVA_BYTE, offset + 3)
        return bytesToBuffer(byteArrayOf(byte0, byte1, byte2, byte3)).float
    }

    override fun putLong(dst: MemorySegment, offset: Long, value: Long) {
        long2ByteArray(value).forEachIndexed { index , value ->
            dst.set(ValueLayout.JAVA_BYTE, offset + index, value)
        }
    }

    override fun getLong(dst: MemorySegment, offset: Long): Long {
        val byte0 = dst.get(ValueLayout.JAVA_BYTE, offset + 0)
        val byte1 = dst.get(ValueLayout.JAVA_BYTE, offset + 1)
        val byte2 = dst.get(ValueLayout.JAVA_BYTE, offset + 2)
        val byte3 = dst.get(ValueLayout.JAVA_BYTE, offset + 3)
        val byte4 = dst.get(ValueLayout.JAVA_BYTE, offset + 4)
        val byte5 = dst.get(ValueLayout.JAVA_BYTE, offset + 5)
        val byte6 = dst.get(ValueLayout.JAVA_BYTE, offset + 6)
        val byte7 = dst.get(ValueLayout.JAVA_BYTE, offset + 7)

        return bytesToBuffer(byteArrayOf(byte0, byte1, byte2, byte3, byte4, byte5, byte6, byte7)).long
    }

    override fun putInt(dst: MemorySegment, offset: Long, value: Int) {
        int2ByteArray(value).forEachIndexed { index , value ->
            dst.set(ValueLayout.JAVA_BYTE, offset + index, value)
        }
    }

    override fun getInt(dst: MemorySegment, offset: Long): Int {
        val byte0 = dst.get(ValueLayout.JAVA_BYTE, offset + 0)
        val byte1 = dst.get(ValueLayout.JAVA_BYTE, offset + 1)
        val byte2 = dst.get(ValueLayout.JAVA_BYTE, offset + 2)
        val byte3 = dst.get(ValueLayout.JAVA_BYTE, offset + 3)

        return bytesToBuffer(byteArrayOf(byte0, byte1, byte2, byte3)).int
    }
}

fun longToBytes(x: Long): ByteArray? {
    val buffer = ByteBuffer.allocate(java.lang.Long.BYTES)
    buffer.putLong(x)
    return buffer.array()
}

fun bytesToBuffer(bytes: ByteArray): ByteBuffer {
    val buffer = ByteBuffer.allocate(java.lang.Long.BYTES)
    buffer.put(bytes)
    buffer.flip() //need flip
    return buffer
}