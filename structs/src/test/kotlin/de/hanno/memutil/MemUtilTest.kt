package de.hanno.memutil

import org.junit.Assert
import org.junit.Test
import org.lwjgl.BufferUtils

class MemUtilTest {

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
}