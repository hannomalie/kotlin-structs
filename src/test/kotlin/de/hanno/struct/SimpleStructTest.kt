package de.hanno.struct

import org.junit.Assert
import org.junit.Test

class SimpleStructTest {
    @Test
    fun testSimpleStructClass() {
        class MyStruct: SimpleStruct<MyStruct>() {
            val myInt by Int
            var myMutableInt by Int
            var myMutableFloat by Float
        }


        val myStruct = MyStruct().apply { myMutableInt = 4 }.apply { myMutableFloat = 2.0f }

        Assert.assertEquals(96, myStruct.sizeInBytes)
        Assert.assertEquals(IntStruct, myStruct.getMemberStructs()[0].bytable)
        Assert.assertEquals(IntStruct, myStruct.getMemberStructs()[1].bytable)
        Assert.assertEquals(FloatStruct, myStruct.getMemberStructs()[2].bytable)
        Assert.assertEquals(0, myStruct.myInt)
        Assert.assertEquals(4, myStruct.myMutableInt)
        Assert.assertEquals(2f, myStruct.myMutableFloat)
        myStruct.buffer.rewind()
        Assert.assertEquals(0, myStruct.buffer.int)
        Assert.assertEquals(4, myStruct.buffer.int)
        Assert.assertEquals(2f, myStruct.buffer.float)
    }

    @Test
    fun testSimpleStructClassFromAndToBuffer() {
        class MyStruct: SimpleStruct<MyStruct>() {
            val myInt by Int
            var myMutableInt by Int
        }


        val source = MyStruct().apply { myMutableInt = 4 }

        Assert.assertEquals(64, source.sizeInBytes)
        Assert.assertEquals(IntStruct, source.getMemberStructs()[0].bytable)
        Assert.assertEquals(IntStruct, source.getMemberStructs()[1].bytable)
        Assert.assertEquals(0, source.myInt)
        Assert.assertEquals(4, source.myMutableInt)
        source.buffer.rewind()
        Assert.assertEquals(0, source.buffer.int)
        Assert.assertEquals(4, source.buffer.int)


        val target = MyStruct()
        source.saveTo(target)
        Assert.assertEquals(0, target.myInt)
        Assert.assertEquals(4, target.myMutableInt)

        target.myMutableInt = 5
        Assert.assertEquals(5, target.myMutableInt)

        target.buffer.rewind()
        source.buffer.rewind()
        source.initFrom(target)
        Assert.assertEquals(5, source.myMutableInt)

    }

    class NestedStruct: SimpleStruct<NestedStruct>() {
        var myMutableInt by Int
    }


    @Test
    fun testNestedStruct() {

        class MyStruct : SimpleStruct<MyStruct>() {
            val myInt by Int
            var myMutableFloat by Float
            val nestedStruct: NestedStruct by NestedStruct()
        }


        val myStruct = MyStruct().apply { myMutableFloat = 2.0f }
        val nested = myStruct.nestedStruct
        nested.myMutableInt = 99
        Assert.assertEquals(IntStruct, nested.getMemberStructs()[0].bytable)
        Assert.assertEquals(99, nested.myMutableInt)

        Assert.assertEquals(96, myStruct.sizeInBytes)
        Assert.assertEquals(IntStruct, myStruct.getMemberStructs()[0].bytable)
        Assert.assertEquals(FloatStruct, myStruct.getMemberStructs()[1].bytable)
        Assert.assertEquals(nested, myStruct.getMemberStructs()[2].bytable)
    }
}


