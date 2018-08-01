package de.hanno.struct

import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.lwjgl.BufferUtils

class StructArrayTest {

    class MyStruct(parent: Struct? = null) : Struct(parent) {
        var myInt by 0
    }

    @Test
    fun testStructArray() {
        prepareAnArray()
    }

    @Test
    fun testGetAtIndex() {
        val array = prepareAnArray()

        for(i in 0..9) {
            Assert.assertEquals(i, array.getAtIndex(i).myInt)
        }
    }

    @Test
    fun testArrayInStruct() {
        class ArrayHolderStruct : Struct() {
            var myInt by 0
            var nestedArray by StructArray(this, 10) { MyStruct(it) }
        }

        val holder = ArrayHolderStruct()

        Assert.assertFalse(holder.nestedArray.usesOwnBuffer())
        Assert.assertSame(holder.buffer, holder.nestedArray.buffer)

        holder.myInt = 5
        Assert.assertEquals(5, holder.myInt)
        holder.nestedArray.getAtIndex(0).myInt = 99
        Assert.assertEquals(99, holder.nestedArray.getAtIndex(0).myInt)
        holder.nestedArray.getAtIndex(5).myInt = 18
        Assert.assertEquals(18, holder.nestedArray.getAtIndex(5).myInt)

        holder.buffer.rewind()
        Assert.assertEquals(5, holder.buffer.int)
        Assert.assertEquals(99, holder.buffer.int)
        Assert.assertEquals(0, holder.buffer.int)
        Assert.assertEquals(0, holder.buffer.int)
        Assert.assertEquals(0, holder.buffer.int)
        Assert.assertEquals(0, holder.buffer.int)
        Assert.assertEquals(18, holder.buffer.int)
        Assert.assertEquals(0, holder.buffer.int)
        Assert.assertEquals(0, holder.buffer.int)
        Assert.assertEquals(0, holder.buffer.int)
        Assert.assertEquals(0, holder.buffer.int)
    }

    @Test
    fun testStructArrayCopy() {
        val source = prepareAnArray()
        val target = de.hanno.struct.StructArray(null, 10) { MyStruct(it) }

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

        val structArray = de.hanno.struct.StructArray(null, 10) { MyStruct(it) }

        structArray.forEachIndexed { index, current ->
            assertSame(current.buffer, structArray.buffer)
            assertEquals((index) * current.sizeInBytes, current.slidingWindowOffset)
            current.myInt = index
        }

        checkResultArray(structArray)

        return structArray
    }

    private fun checkResultArray(structArray: StructArray<MyStruct>) {
        structArray.forEachIndexed { index, current ->
            assertEquals((index) * current.sizeInBytes, current.slidingWindowOffset)
            assertEquals(index, current.myInt)
        }
    }


    companion object {

        class Vector3f(parent: Struct? = null) : Struct(parent) {
            var x by 0.0f
            var y by 0.0f
            var z by 0.0f
        }
        class MyStruct(parent: Struct? = null) : Struct(parent) {
            var myInt by 0
            val position by Vector3f(this)
        }

        @JvmStatic fun main(args: Array<String>) {
            val array = StructArray(null, 20000) { MyStruct(it) }
            while (true) {
                array.forEach {
                    it.myInt++
                    it.position.x++
                    it.position.y++
                    it.position.z++
                }
            }
        }
    }
}