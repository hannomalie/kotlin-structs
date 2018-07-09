package de.hanno.struct

import org.junit.Assert.assertEquals
import org.junit.Test

class StructArrayTest {

    class MyStruct : SlidingWindow<MyStruct>() {
        var myInt by 0
    }

    @Test
    fun testSimpleStructArray() {
        prepareAnArray()
    }

    @Test
    fun testStructArrayCopy() {
        val source = prepareAnArray()
        val target = StructArray(10) { MyStruct() }

        source.copyTo(target)

        checkResultArray(target)

    }

    private fun prepareAnArray(): StructArray<MyStruct> {

        val structArray = StructArray(10) { MyStruct() }

        var index = 0
        structArray.forEach { current ->
            assertEquals(current.buffer, structArray.buffer)
            assertEquals((index) * current.sizeInBytes, current.buffer?.position())
            index++
        }

        checkResultArray(structArray)

        return structArray
    }

    private fun checkResultArray(structArray: StructArray<MyStruct>) {
        var index2 = 0
        structArray.forEach { current ->
            assertEquals((index2 + 1) * current.sizeInBytes, current.buffer?.position())
            assertEquals(index2, current.myInt)
            index2++
        }
    }

}
