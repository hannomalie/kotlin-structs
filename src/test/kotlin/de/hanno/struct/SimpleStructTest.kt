package de.hanno.struct

import org.junit.Assert
import org.junit.Test
import java.nio.ByteBuffer

class SimpleStructTest {
    @Test
    fun testSimpleStructClass() {
        class MyStruct: SimpleStruct<MyStruct>() {
            val myInt by Int
            var myMutableInt by Int
        }


        val myStruct = MyStruct().apply { myMutableInt = 4 }

        Assert.assertEquals(64, myStruct.sizeInBytes)
        Assert.assertEquals(IntStruct, myStruct.getMemberStructs()[0].bytable)
        Assert.assertEquals(IntStruct, myStruct.getMemberStructs()[1].bytable)
        Assert.assertEquals(0, myStruct.myInt)
        Assert.assertEquals(4, myStruct.myMutableInt)
        myStruct.buffer.rewind()
        Assert.assertEquals(0, myStruct.buffer.int)
        Assert.assertEquals(4, myStruct.buffer.int)
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

//    @Test
//    fun testMultipleFieldsInSimpleStructClass() {
//        class MyStruct: SimpleStruct() {
//            val myInt = 8
//            val myFloat = .2f
//            val myDouble = .5
//            val myLong = 1L
//        }
//
//        Assert.assertEquals(192, MyStruct().sizeInBytes)
//        Assert.assertEquals(IntStruct, MyStruct().layout.memberStructs[0])
//        Assert.assertEquals(FloatStruct, MyStruct().layout.memberStructs[1])
//        Assert.assertEquals(DoubleStruct, MyStruct().layout.memberStructs[2])
//        Assert.assertEquals(LongStruct, MyStruct().layout.memberStructs[3])
//    }
//
//    @Test
//    fun testMultipleFieldsInNestedStructClass() {
//        class NestedStruct: SimpleStruct() {
//            val myInt = 12
//        }
//
//        class MyStruct: SimpleStruct() {
//            val myNestedStruct = NestedStruct()
//            val myInt = 8
//            val myFloat = .2f
//            val myDouble = .5
//            val myLong = 1L
//        }
//
//        Assert.assertEquals(32, NestedStruct().sizeInBytes)
//        Assert.assertEquals(224, MyStruct().sizeInBytes)
//        Assert.assertEquals(IntStruct, MyStruct().layout.memberStructs[1])
//        Assert.assertEquals(FloatStruct, MyStruct().layout.memberStructs[2])
//        Assert.assertEquals(DoubleStruct, MyStruct().layout.memberStructs[3])
//        Assert.assertEquals(LongStruct, MyStruct().layout.memberStructs[4])
//    }

}


