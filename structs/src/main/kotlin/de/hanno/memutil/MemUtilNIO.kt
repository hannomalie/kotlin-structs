package de.hanno.memutil

import java.nio.ByteBuffer

class MemUtilNIO : MemUtil {
    override fun putDouble(dst: ByteBuffer, offset: Long, value: Double) {
        dst.putDouble(offset.toInt(), value)
    }

    override fun getDouble(dst: ByteBuffer, offset: Long): Double = dst.getDouble(offset.toInt())

    override fun putBoolean(dst: ByteBuffer, offset: Long, value: Boolean) {
        dst.putInt(offset.toInt(), if(value) 1 else 0)
    }

    override fun getBoolean(dst: ByteBuffer, offset: Long): Boolean = dst.getInt(offset.toInt()) == 1

    override fun putFloat(dst: ByteBuffer, offset: Long, value: Float) {
        dst.putFloat(offset.toInt(), value)
    }

    override fun getFloat(dst: ByteBuffer, offset: Long): Float = dst.getFloat(offset.toInt())

    override fun putLong(dst: ByteBuffer, offset: Long, value: Long) {
        dst.putLong(offset.toInt(), value)
    }

    override fun getLong(dst: ByteBuffer, offset: Long): Long = dst.getLong(offset.toInt())

    override fun putInt(dst: ByteBuffer, offset: Long, value: Int) {
        dst.putInt(offset.toInt(), value)
    }

    override fun getInt(dst: ByteBuffer, offset: Long): Int = dst.getInt(offset.toInt())
}