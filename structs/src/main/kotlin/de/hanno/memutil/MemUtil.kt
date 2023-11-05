/*
 * (C) Copyright 2016-2018 Kai Burjack

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.

 */
package de.hanno.memutil

import de.hanno.memutil.Config.Companion.useUnsafe
import java.lang.foreign.MemorySegment
import java.nio.ByteBuffer

/**
 * Helper class to do efficient memory operations.
 * Heavy stuff, use with extreme caution!
 *
 * @author The LWJGL authors
 * @author Kai Burjack
 * @author Hannes Tenter
 */
interface MemUtil {

    fun putLong(dst: MemorySegment, offset: Long, value: Long)
    fun getLong(dst: MemorySegment, offset: Long): Long

    fun putInt(dst: MemorySegment, offset: Long, value: Int)
    fun getInt(dst: MemorySegment, offset: Long): Int

    fun putFloat(dst: MemorySegment, offset: Long, value: Float)
    fun getFloat(dst: MemorySegment, offset: Long): Float

    fun putDouble(dst: MemorySegment, offset: Long, value: Double)
    fun getDouble(dst: MemorySegment, offset: Long): Double

    fun putBoolean(dst: MemorySegment, offset: Long, value: Boolean)
    fun getBoolean(dst: MemorySegment, offset: Long): Boolean

    companion object: MemUtil by MemUtilUnsafe()
}


class Config {
    companion object {
        var useUnsafe: Boolean = false
    }
}
