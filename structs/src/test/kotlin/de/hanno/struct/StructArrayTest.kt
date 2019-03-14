package de.hanno.struct

import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.lwjgl.BufferUtils
import kotlin.Array

class StructArrayTest {

    class MyStruct : Struct() {
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
    fun testForEach() {
        val array = prepareAnArray()

        var counter = 0
        array.forEach {
            Assert.assertEquals(counter, it.myInt)
            counter++
        }
    }

    @Test
    fun testGetAtIndexResizable() {
        val array = prepareAResizableArray().apply {
            resize(11)
        }

        for(i in 0..9) {
            val atIndex = array.getAtIndex(i)
            Assert.assertSame(array.buffer, atIndex.buffer)
            Assert.assertEquals(i, atIndex.myInt)
        }
    }

    @Test
    fun testArrayInStruct() {
        class ArrayHolderStruct : Struct() {
            var myInt by 0
            var nestedArray by StructArray(10) { MyStruct() }
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

    @Test
    fun testResize() {
        var source = StructArray(10){ MyStruct() }
        Assert.assertEquals(10, source.size)

        val bufferBefore = source.buffer

        source = source.resize(20)
        Assert.assertNotSame(source.buffer, bufferBefore)
        Assert.assertEquals(20, source.size)

        source = source.shrink(5)
        Assert.assertNotSame(source.buffer, bufferBefore)
        Assert.assertEquals(5, source.size)

        source = source.enlarge(7)
        Assert.assertNotSame(source.buffer, bufferBefore)
        Assert.assertEquals(7, source.size)

        source = source.resize(20)
        Assert.assertNotSame(source.buffer, bufferBefore)
        Assert.assertEquals(20, source.size)
    }

    private fun prepareAnArray(): StructArray<MyStruct> {

        val structArray = StructArray(10) { MyStruct() }

        structArray.forEachIndexed { index, current ->
            assertSame(current.buffer, structArray.buffer)
            assertEquals((index * current.sizeInBytes).toLong(), current.baseByteOffset)
            current.myInt = index
        }

        checkResultArray(structArray)

        return structArray
    }
    private fun prepareAResizableArray(): StructArray<MyStruct> {

        val structArray = StructArray(10) { MyStruct() }

        structArray.forEachIndexed { index, current ->
            assertSame(current.buffer, structArray.buffer)
            assertEquals((index * current.sizeInBytes).toLong(), current.baseByteOffset)
            current.myInt = index
        }

        checkResultArray(structArray)

        return structArray
    }

    private fun checkResultArray(structArray: SlidingWindowArray<MyStruct>) {
        structArray.forEachIndexed { index, current ->
            assertEquals((index * current.sizeInBytes).toLong(), current.baseByteOffset)
            assertEquals(index, current.myInt)
        }

        with(structArray.buffer) {
            rewind()
            Assert.assertEquals(0, int)
            Assert.assertEquals(1, int)
            Assert.assertEquals(2, int)
            Assert.assertEquals(3, int)
            Assert.assertEquals(4, int)
            Assert.assertEquals(5, int)
            Assert.assertEquals(6, int)
            Assert.assertEquals(7, int)
            Assert.assertEquals(8, int)
            Assert.assertEquals(9, int)
        }
    }

    @Test
    fun testStructObjectArray() {
        class StructObject : Struct() {
            var a by 0
            val aString = "aString"
        }
        val array = StructObjectArray(size = 10, factory = { StructObject() })
        for(i in 0 until array.size) {
            array[i].a = i
        }

        Assert.assertEquals(10, array.size)
        Assert.assertEquals(10, array.backingList.size)
        for(i in 0 until array.size) {
            Assert.assertEquals(i, array[i].a)
            Assert.assertEquals("aString", array[i].aString)
        }
    }


    companion object {

        class Vector3f : Struct() {
            var x by 0.0f
            var y by 0.0f
            var z by 0.0f
        }
        class MyStruct : Struct() {
            var myInt by 0
            val position by Vector3f()
        }

        @JvmStatic fun main(args: Array<String>) {
            val array = StructArray(20000) { MyStruct() }
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