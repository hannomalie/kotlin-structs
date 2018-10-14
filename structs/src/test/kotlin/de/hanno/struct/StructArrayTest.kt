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
        val array = prepareAResizableArray()
        array.resize(11)

        for(i in 0..9) {
            Assert.assertEquals(i, array.getAtIndex(i).myInt)
        }
    }

    @Test
    fun testArrayInStruct() {
        class ArrayHolderStruct : Struct() {
            var myInt by 0
            var nestedArray by StaticStructArray(this, 10) { MyStruct(it) }
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
        val target = de.hanno.struct.StaticStructArray(null, 10) { MyStruct(it) }

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
        val source = ResizableStructArray(null, 10){ MyStruct(it) }
        Assert.assertEquals(10, source.size)

        var bufferBefore = source.buffer
        source.resize(20)
        Assert.assertNotSame(source.buffer, bufferBefore)
        Assert.assertEquals(20, source.size)

        bufferBefore = source.buffer
        source.shrink(5)
        Assert.assertNotSame(source.buffer, bufferBefore)
        Assert.assertEquals(5, source.size)

        bufferBefore = source.buffer
        source.enlarge(7)
        Assert.assertNotSame(source.buffer, bufferBefore)
        Assert.assertEquals(7, source.size)
    }

    private fun prepareAnArray(): StaticStructArray<MyStruct> {

        val structArray = StaticStructArray(null, 10) { MyStruct(it) }

        structArray.forEachIndexed { index, current ->
            assertSame(current.buffer, structArray.buffer)
            assertEquals((index * current.sizeInBytes).toLong(), current.baseByteOffset)
            current.myInt = index
        }

        checkResultArray(structArray)

        return structArray
    }
    private fun prepareAResizableArray(): ResizableStructArray<MyStruct> {

        val structArray = ResizableStructArray(null, 10) { MyStruct(it) }

        structArray.forEachIndexed { index, current ->
            assertSame(current.buffer, structArray.buffer)
            assertEquals((index * current.sizeInBytes).toLong(), current.baseByteOffset)
            current.myInt = index
        }

        checkResultArray(structArray)

        return structArray
    }

    private fun checkResultArray(structArray: SlidingWindowStructArray<MyStruct>) {
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
        class StructObject(parent: Struct?): Struct(parent) {
            var a by 0
            val aString = "aString"
        }
        val array = StaticStructObjectArray(size = 10, factory = { struct -> StructObject(struct) })
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
            val array = StaticStructArray(null, 20000) { MyStruct(it) }
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