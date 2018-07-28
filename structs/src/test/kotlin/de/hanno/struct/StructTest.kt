package de.hanno.struct

import org.junit.Assert
import org.junit.Test

class StructTest {

    @Test
    fun testNestedStructClass() {
        class MyStruct: Struct() {
            val myInt by 0
            var myMutableInt by 0
            var myMutableFloat by 0.0f
        }


        val myStruct = MyStruct().apply { myMutableInt = 4 }.apply { myMutableFloat = 2.0f }

        Assert.assertEquals(12, myStruct.sizeInBytes)
        Assert.assertTrue(myStruct.memberStructs[0] is IntProperty)
        Assert.assertTrue(myStruct.memberStructs[1] is IntProperty)
        Assert.assertTrue(myStruct.memberStructs[2] is FloatProperty)
        Assert.assertEquals(0, myStruct.myInt)
        Assert.assertEquals(4, myStruct.myMutableInt)
        Assert.assertEquals(2f, myStruct.myMutableFloat)
        myStruct.buffer.rewind()
        Assert.assertEquals(0, myStruct.buffer.int)
        Assert.assertEquals(4, myStruct.buffer.int)
        Assert.assertEquals(2f, myStruct.buffer.float)
    }

    @Test
    fun testNestedStructClassFromAndToBuffer() {
        class MyStruct: Struct() {
            val myInt by 0
            var myMutableInt by 0
        }


        val source = MyStruct().apply { myMutableInt = 4 }

        Assert.assertEquals(8, source.sizeInBytes)
        Assert.assertTrue(source.memberStructs[0] is IntProperty)
        Assert.assertTrue(source.memberStructs[1] is IntProperty)
        Assert.assertEquals(0, source.myInt)
        Assert.assertEquals(4, source.myMutableInt)
        source.buffer.rewind()
        Assert.assertEquals(0, source.buffer.int)
        Assert.assertEquals(4, source.buffer.int)


        val target = MyStruct()
        source.copyTo(target)
        Assert.assertEquals(0, target.myInt)
        Assert.assertEquals(4, target.myMutableInt)

        target.myMutableInt = 5
        Assert.assertEquals(5, target.myMutableInt)

        target.buffer.rewind()
        source.buffer.rewind()
        source.copyFrom(target)
        Assert.assertEquals(5, source.myMutableInt)

    }


    @Test
    fun testNestedStruct() {

        class SimpleNestedStruct(parent: Structable): Struct(parent) {
            var myMutableInt by 0
        }
        class ComplexNestedStruct(parent: Structable): Struct(parent) {
            var myMutableInt by 0
            var nestedStruct by SimpleNestedStruct(this)
        }

        class MyStruct : Struct() {
            val myInt by 0
            var myMutableFloat by 0.0f
            val simpleNestedStruct: SimpleNestedStruct by SimpleNestedStruct(this)
            val complexNestedStruct: ComplexNestedStruct by ComplexNestedStruct(this)
        }

        val myStruct = MyStruct()
        Assert.assertEquals(4, myStruct.memberStructs.size)
        val simpleNestedStruct = myStruct.simpleNestedStruct
        val complexNestedStruct = myStruct.complexNestedStruct

        Assert.assertEquals(myStruct.buffer, simpleNestedStruct.buffer)
        Assert.assertEquals(myStruct.buffer, complexNestedStruct.buffer)
        Assert.assertEquals(20, myStruct.sizeInBytes)

        myStruct.myMutableFloat = 2.0f
        simpleNestedStruct.myMutableInt = 99
        Assert.assertTrue(simpleNestedStruct.memberStructs[0] is IntProperty)
        Assert.assertEquals(8, simpleNestedStruct.baseByteOffset)
        Assert.assertEquals(99, simpleNestedStruct.myMutableInt)
        Assert.assertEquals(0, myStruct.myInt)
        Assert.assertTrue(myStruct.memberStructs[0] is IntProperty)
        Assert.assertEquals(0, myStruct.memberStructs[0].localByteOffset)
        Assert.assertEquals(4, myStruct.memberStructs[0].sizeInBytes)
        Assert.assertTrue(myStruct.memberStructs[1] is FloatProperty)
        Assert.assertEquals(4, myStruct.memberStructs[1].localByteOffset)
        Assert.assertEquals(4, myStruct.memberStructs[1].sizeInBytes)
        Assert.assertNotNull(myStruct.memberStructs[2])
        Assert.assertEquals(8, myStruct.memberStructs[2].localByteOffset)
        Assert.assertEquals(4, myStruct.memberStructs[2].sizeInBytes)
        Assert.assertNotNull(myStruct.memberStructs[3])
        Assert.assertEquals(12, myStruct.memberStructs[3].localByteOffset)
        Assert.assertEquals(8, myStruct.memberStructs[3].sizeInBytes)

        Assert.assertEquals(12, complexNestedStruct.baseByteOffset)
        complexNestedStruct.myMutableInt = 18
        Assert.assertEquals(18, complexNestedStruct.myMutableInt)

        complexNestedStruct.nestedStruct.myMutableInt = 27
        Assert.assertEquals(27, complexNestedStruct.nestedStruct.myMutableInt)


        myStruct.buffer.rewind()
        Assert.assertEquals(0, myStruct.buffer.int)
        Assert.assertEquals(2.0f, myStruct.buffer.float)
        Assert.assertEquals(99, myStruct.buffer.int)
    }

}


