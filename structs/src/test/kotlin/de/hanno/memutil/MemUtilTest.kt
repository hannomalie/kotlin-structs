package de.hanno.memutil

import org.junit.Assert
import org.junit.Test
import org.lwjgl.BufferUtils
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

class Foo {
    @Test
    fun foo() {
        val segment: MemorySegment = Arena.global().allocate(100, ValueLayout.JAVA_INT.byteAlignment())

        for (i in 0..24) {
            segment.setAtIndex(ValueLayout.JAVA_INT, i.toLong(), i)
        }
        println(segment.get(ValueLayout.JAVA_INT, 0))
        println(segment.get(ValueLayout.JAVA_INT, 4))
        println(segment.get(ValueLayout.JAVA_INT, 8))
    }
}

class MemUtilTest {

//    TODO: Test all implementations here with property based testing
    val memUtilUnsafe = MemUtilUnsafe()
    @Test
    fun testGetInt() {
        val buffer = Arena.global().allocate(4, ValueLayout.JAVA_BYTE.byteAlignment())
        memUtilUnsafe.putInt(buffer, 0L, 8)
        Assert.assertEquals(8, memUtilUnsafe.getInt(buffer, 0))
    }
    @Test
    fun testPutInt() {
        val buffer = Arena.global().allocate(4, ValueLayout.JAVA_BYTE.byteAlignment())
        memUtilUnsafe.putInt(buffer, 0, 9)

        val byte0 = buffer.get(ValueLayout.JAVA_BYTE, 0)
        val byte1 = buffer.get(ValueLayout.JAVA_BYTE, 1)
        val byte2 = buffer.get(ValueLayout.JAVA_BYTE, 2)
        val byte3 = buffer.get(ValueLayout.JAVA_BYTE, 3)

        Assert.assertEquals(9, bytesToBuffer(byteArrayOf(byte0, byte1, byte2, byte3)).int)
    }

    @Test
    fun testGetFloat() {
        val buffer = Arena.global().allocate(8L * Float.SIZE_BYTES, ValueLayout.JAVA_BYTE.byteAlignment())
        memUtilUnsafe.putFloat(buffer, 4, 8.0f)
        memUtilUnsafe.putFloat(buffer, 0, 2.0f)
        Assert.assertEquals(8.0f, memUtilUnsafe.getFloat(buffer, 4L))
        Assert.assertEquals(2.0f, memUtilUnsafe.getFloat(buffer, 0L))
    }
    @Test
    fun testPutFloat() {
        val buffer = Arena.global().allocate(2L * Float.SIZE_BYTES, ValueLayout.JAVA_BYTE.byteAlignment())
        memUtilUnsafe.putFloat(buffer, Float.SIZE_BYTES.toLong(), 9.0f)

        val byte0 = buffer.get(ValueLayout.JAVA_BYTE, 4L + 0)
        val byte1 = buffer.get(ValueLayout.JAVA_BYTE, 4L + 1)
        val byte2 = buffer.get(ValueLayout.JAVA_BYTE, 4L + 2)
        val byte3 = buffer.get(ValueLayout.JAVA_BYTE, 4L + 3)

        Assert.assertEquals(9.0f, bytesToBuffer(byteArrayOf(byte0, byte1, byte2, byte3)).float)
    }
}