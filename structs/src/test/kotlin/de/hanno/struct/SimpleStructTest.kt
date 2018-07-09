package de.hanno.struct

import org.junit.Assert
import org.junit.Test

class NestedStructTest {

    @Test
    fun testNestedStructClass() {
        class MyStruct: BaseStruct<MyStruct>() {
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
        class MyStruct: BaseStruct<MyStruct>() {
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

        class SimpleNestedStruct(parent: Struct): BaseStruct<SimpleNestedStruct>(parent) {
            var myMutableInt by 0
        }

        class MyStruct : BaseStruct<MyStruct>() {
            val myInt by 0
            var myMutableFloat by 0.0f
            val nestedStruct: SimpleNestedStruct by SimpleNestedStruct(this@MyStruct)
        }


        val myStruct = MyStruct()
        Assert.assertEquals(3, myStruct.memberStructs.size)
        val nested = myStruct.nestedStruct

        Assert.assertEquals(myStruct.buffer, nested.buffer)
        Assert.assertEquals(12, myStruct.sizeInBytes)

        myStruct.myMutableFloat = 2.0f
        nested.myMutableInt = 99
        Assert.assertTrue(nested.memberStructs[0] is IntProperty)
        Assert.assertEquals(8, nested.memberStructs[0].baseByteOffset)
        Assert.assertEquals(99, nested.myMutableInt)
        Assert.assertEquals(0, myStruct.myInt)
        Assert.assertTrue(myStruct.memberStructs[0] is IntProperty)
        Assert.assertTrue(myStruct.memberStructs[1] is FloatProperty)
        Assert.assertTrue(myStruct.memberStructs[2] is ReadOnlyStructProperty)


        myStruct.buffer.rewind()
        Assert.assertEquals(0, myStruct.buffer.int)
        Assert.assertEquals(2.0f, myStruct.buffer.float)
        Assert.assertEquals(99, myStruct.buffer.int)
    }

    @Test
    fun testArrayStruct() {
        class MyStructArray : BaseStruct<MyStructArray>() {
        }
    }
}


