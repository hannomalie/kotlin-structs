package de.hanno.struct

import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.lwjgl.BufferUtils

class StructTest {

    @Test
    fun testSimpleStructUnInitialized() {
        class MyStruct: Struct() {
            val myInt by 0
            var myMutableFloat by 0.0f
            var myMutableBoolean by false
        }
        val myStruct = MyStruct()
        assertEquals(0, myStruct.myInt)
        assertEquals(.0f, myStruct.myMutableFloat)
        assertEquals(false, myStruct.myMutableBoolean)
    }

    @Test
    fun testSimpleStructInitialized() {
        class MyStruct: Struct() {
            override val buffer = BufferUtils.createByteBuffer(8)
            val myInt by 2
            var myMutableFloat by 4.0f
        }
        val myStruct = MyStruct()
        assertEquals(2, myStruct.myInt)
        assertEquals(4.0f, myStruct.myMutableFloat)
    }

    @Test
    fun testNestedStructInitialized() {
        class NestedStruct : Struct() {
            var myMutableInt by 4
        }
        class MyStruct: Struct() {
            override val buffer = BufferUtils.createByteBuffer(8)
            val nestedStruct by NestedStruct()
            var myMutableFloat by 4.0f
        }
        val myStruct = MyStruct()
        assertEquals(myStruct, myStruct.nestedStruct.parent)
        assertEquals(4, myStruct.nestedStruct.myMutableInt)
        myStruct.nestedStruct.myMutableInt = 99
        assertEquals(99, myStruct.nestedStruct.myMutableInt)
        assertEquals(4.0f, myStruct.myMutableFloat)
    }

    @Test
    fun testNestedStructClass() {
        class MyStruct: Struct() {
            val myInt by 0
            var myMutableInt by 0
            var myMutableFloat by 0.0f
        }

        val myStruct = MyStruct().apply { myMutableInt = 4 }.apply { myMutableFloat = 2.0f }

        assertEquals(12, myStruct.sizeInBytes)
        Assert.assertTrue(myStruct.memberStructs[0] is IntProperty)
        Assert.assertTrue(myStruct.memberStructs[1] is IntProperty)
        Assert.assertTrue(myStruct.memberStructs[2] is FloatProperty)
        assertEquals(0, myStruct.myInt)
        assertEquals(4, myStruct.myMutableInt)
        assertEquals(2f, myStruct.myMutableFloat)
        myStruct.buffer.rewind()
        assertEquals(0, myStruct.buffer.int)
        assertEquals(4, myStruct.buffer.int)
        assertEquals(2f, myStruct.buffer.float)
    }

    @Test
    fun testNestedStructClassFromAndToBuffer() {
        class MyStruct: Struct() {
            val myInt by 0
            var myMutableInt by 0
        }


        val source = MyStruct().apply { myMutableInt = 4 }

        assertEquals(8, source.sizeInBytes)
        Assert.assertTrue(source.memberStructs[0] is IntProperty)
        Assert.assertTrue(source.memberStructs[1] is IntProperty)
        assertEquals(0, source.myInt)
        assertEquals(4, source.myMutableInt)
        source.buffer.rewind()
        assertEquals(0, source.buffer.int)
        assertEquals(4, source.buffer.int)


        val target = MyStruct()
        Assert.assertTrue(source.usesOwnBuffer())
        Assert.assertTrue(target.usesOwnBuffer())
        source.copyTo(target)
        Assert.assertNotSame(source.buffer, target.buffer)
        assertEquals(8, target.buffer.capacity())
        assertEquals(0, target.myInt)
        assertEquals(4, target.myMutableInt)

        target.myMutableInt = 5
        assertEquals(5, target.myMutableInt)

        target.buffer.rewind()
        source.buffer.rewind()
        source.copyFrom(target)
        assertEquals(5, source.myMutableInt)

    }


    @Test
    fun testNestedStruct() {

        class SimpleNestedStruct(): Struct() {
            var myMutableInt by 0
        }
        class ComplexNestedStruct(): Struct() {
            var myMutableInt by 0
            var nestedStruct by SimpleNestedStruct()
        }

        class MyStruct : Struct() {
            val myInt by 0
            var myMutableFloat by 0.0f
            val simpleNestedStruct: SimpleNestedStruct by SimpleNestedStruct()
            val complexNestedStruct: ComplexNestedStruct by ComplexNestedStruct()
        }

        val myStruct = MyStruct()
        assertEquals(4, myStruct.memberStructs.size)
        val simpleNestedStruct = myStruct.simpleNestedStruct
        val complexNestedStruct = myStruct.complexNestedStruct

        assertEquals(myStruct, simpleNestedStruct.parent)
        assertFalse(simpleNestedStruct.usesOwnBuffer())
        assertEquals(myStruct.buffer, simpleNestedStruct.buffer)
        assertEquals(myStruct.buffer, complexNestedStruct.buffer)
        assertEquals(20, myStruct.sizeInBytes)

        myStruct.myMutableFloat = 2.0f
        simpleNestedStruct.myMutableInt = 99
        Assert.assertTrue(simpleNestedStruct.memberStructs[0] is IntProperty)
        assertEquals(8, simpleNestedStruct.baseByteOffset)
        assertEquals(99, simpleNestedStruct.myMutableInt)
        assertEquals(0, myStruct.myInt)
        Assert.assertTrue(myStruct.memberStructs[0] is IntProperty)
        assertEquals(0, myStruct.memberStructs[0].localByteOffset)
        assertEquals(4, myStruct.memberStructs[0].sizeInBytes)
        Assert.assertTrue(myStruct.memberStructs[1] is FloatProperty)
        assertEquals(4, myStruct.memberStructs[1].localByteOffset)
        assertEquals(4, myStruct.memberStructs[1].sizeInBytes)
        Assert.assertNotNull(myStruct.memberStructs[2])
        assertEquals(8, myStruct.memberStructs[2].localByteOffset)
        assertEquals(4, myStruct.memberStructs[2].sizeInBytes)
        Assert.assertNotNull(myStruct.memberStructs[3])
        assertEquals(12, myStruct.memberStructs[3].localByteOffset)
        assertEquals(8, myStruct.memberStructs[3].sizeInBytes)

        assertEquals(12, complexNestedStruct.baseByteOffset)
        complexNestedStruct.myMutableInt = 18
        assertEquals(18, complexNestedStruct.myMutableInt)

        complexNestedStruct.nestedStruct.myMutableInt = 27
        assertEquals(27, complexNestedStruct.nestedStruct.myMutableInt)


        myStruct.buffer.rewind()
        assertEquals(0, myStruct.buffer.int)
        assertEquals(2.0f, myStruct.buffer.float)
        assertEquals(99, myStruct.buffer.int)
    }

}


