package de.hanno.memutil

import org.junit.Assert
import org.junit.Test
import org.lwjgl.BufferUtils

class MemUtilTest {

//    TODO: Test all implementations here with property based testing
    val memUtilUnsafe = MemUtilUnsafe()

    @Test
    fun testGetInt() {
        val buffer = BufferUtils.createByteBuffer(4)
        buffer.putInt(0, 8)
        Assert.assertEquals(8, memUtilUnsafe.getInt(buffer, 0))
    }
    @Test
    fun testPutInt() {
        val buffer = BufferUtils.createByteBuffer(4)
        memUtilUnsafe.putInt(buffer, 0, 9)
        Assert.assertEquals(9, buffer.getInt(0))
    }

    @Test
    fun testGetFloat() {
        val buffer = BufferUtils.createByteBuffer(8)
        buffer.putFloat(4, 8.0f)
        Assert.assertEquals(8.0f, memUtilUnsafe.getFloat(buffer, 4))
    }
    @Test
    fun testPutFloat() {
        val buffer = BufferUtils.createByteBuffer(8)
        memUtilUnsafe.putFloat(buffer, 4, 9.0f)
        Assert.assertEquals(9.0f, buffer.getFloat(4))
    }
}