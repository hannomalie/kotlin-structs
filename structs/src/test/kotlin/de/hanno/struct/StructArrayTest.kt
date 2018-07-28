package de.hanno.struct

import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import org.lwjgl.BufferUtils
import java.nio.ByteBuffer

class StructArrayTest {

    class MyStruct : SlidingWindow() {
        var myInt by 0
    }

    @Test
    fun testStructArray() {
        prepareAnArray()
    }

    @Test
    fun testStructArrayCopy() {
        val source = prepareAnArray()
        val target = de.hanno.struct.StructArray(10) { MyStruct() }

        source.copyTo(target)

        checkResultArray(target)
    }

    @Test
    fun testStructArrayCopyToBuffer() {
        val source = prepareAnArray()
        val target = BufferUtils.createByteBuffer(MyStruct().sizeInBytes*10)

        source.copyTo(target)

        target.rewind()
        for(i in 0..9) {
            Assert.assertEquals(i, target.int)
        }
    }

    @Test
    fun testStructArrayClone() {
        val source = prepareAnArray()
        val sourceArray = IntArray(MyStruct().sizeInBytes*10/Integer.BYTES).apply {
            source.buffer.rewind()
            source.buffer.asIntBuffer().get(this)
        }
        val target = source.clone()
        val targetArray = IntArray(MyStruct().sizeInBytes*10/Integer.BYTES).apply {
            target.buffer.rewind()
            target.buffer.asIntBuffer().get(this)
        }

        Assert.assertArrayEquals(sourceArray, targetArray)

        checkResultArray(target)
    }

    private fun prepareAnArray(): StructArray<MyStruct> {

        val structArray = de.hanno.struct.StructArray(10) { MyStruct() }

        structArray.forEachIndexed { index, current ->
            assertEquals(current.buffer, structArray.buffer)
            assertEquals((index) * current.sizeInBytes, current.buffer?.position())
            current.myInt = index
        }

        checkResultArray(structArray)

        return structArray
    }

    private fun checkResultArray(structArray: StructArray<MyStruct>) {
        structArray.forEachIndexed { index, current ->
            assertEquals((index) * current.sizeInBytes, current.buffer?.position())
            assertEquals(index, current.myInt)
        }
    }

}
