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

import java.nio.ByteBuffer

/**
 * Helper class to do efficient memory operations.
 * Heavy stuff, sse with extreme caution!
 *
 * @author The LWJGL authors
 * @author Kai Burjack
 * @author Hannes Tenter
 */
interface MemUtil {

    fun putLong(dst: ByteBuffer, offset: Long, value: Long)
    fun getLong(dst: ByteBuffer, offset: Long): Long

    fun putInt(dst: ByteBuffer, offset: Long, value: Int)
    fun getInt(dst: ByteBuffer, offset: Long): Int

    fun putFloat(dst: ByteBuffer, offset: Long, value: Float)
    fun getFloat(dst: ByteBuffer, offset: Long): Float

    fun putBoolean(dst: ByteBuffer, offset: Long, value: Boolean)
    fun getBoolean(dst: ByteBuffer, offset: Long): Boolean

    companion object {
//        val INSTANCE = createInstance()
        private fun createInstance(): MemUtil {
            var accessor = try {
                if (Options.NO_UNSAFE)
                    MemUtilNIO()
                else
                    MemUtilUnsafe()
            } catch (e: Throwable) {
                MemUtilNIO()
            }

            return accessor
        }
    }
}

class Options {
    companion object {
        val NO_UNSAFE = false
        val DEBUG = false
    }
}