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
package de.hanno.struct

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

open class MemUtilNIO : MemUtil {
    override fun putFloat(dst: ByteBuffer, offset: Long, value: Float) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getFloat(dst: ByteBuffer, offset: Long): Float {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun putLong(dst: ByteBuffer, offset: Long, value: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLong(dst: ByteBuffer, offset: Long): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun putInt(dst: ByteBuffer, offset: Long, value: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getInt(dst: ByteBuffer, offset: Long): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class MemUtilUnsafe : MemUtil {
    override fun putFloat(dst: ByteBuffer, offset: Long, value: Float) {
        UNSAFE.putFloat(dst.address + offset, value)
    }

    override fun getFloat(dst: ByteBuffer, offset: Long) = UNSAFE.getFloat(dst.address + offset)

    override fun putLong(dst: ByteBuffer, offset: Long, value: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLong(dst: ByteBuffer, offset: Long): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun putInt(dst: ByteBuffer, offset: Long, value: Int) {
        UNSAFE.putInt(dst.address + offset, value)
    }

    override fun getInt(dst: ByteBuffer, offset: Long) = UNSAFE.getInt(dst.address + offset)


    val ByteBuffer.address: Long
        get() = UNSAFE.getLong(this, ADDRESS_FIELDOFFSET)

    companion object {
        val UNSAFE = getUnsafeInstance()

        val floatArrayOffset: Long

        /**
         * Used to create a direct ByteBuffer for a known address.
         */
        private external fun newTestBuffer(): ByteBuffer

        init {
            try {
                floatArrayOffset = UNSAFE.arrayBaseOffset(FloatArray::class.java).toLong()
                // Check if we can use object field offset/address put/get methods
                sun.misc.Unsafe::class.java.getDeclaredMethod("getLong", Any::class.java, Long::class.javaPrimitiveType)
                sun.misc.Unsafe::class.java.getDeclaredMethod("putLong", Any::class.java, Long::class.javaPrimitiveType, Long::class.javaPrimitiveType)
            } catch (e: NoSuchFieldException) {
                throw UnsupportedOperationException(e)
            } catch (e: NoSuchMethodException) {
                throw UnsupportedOperationException(e)
            }

        }

        @Throws(SecurityException::class)
        fun getUnsafeInstance(): sun.misc.Unsafe {
            val fields = sun.misc.Unsafe::class.java.declaredFields
            for (i in fields.indices) {
                val field = fields[i]
                if (field.type != sun.misc.Unsafe::class.java)
                    continue
                val modifiers = field.modifiers
                if (!(java.lang.reflect.Modifier.isStatic(modifiers) && java.lang.reflect.Modifier.isFinal(modifiers)))
                    continue
                field.isAccessible = true
                try {
                    return field.get(null) as sun.misc.Unsafe
                } catch (e: IllegalAccessException) {
                    /* Ignore */
                }

                break
            }
            throw UnsupportedOperationException()
        }


        private fun atLeastJava9(classVersion: String): Boolean {
            return try {
                val value = java.lang.Double.parseDouble(classVersion)
                value >= 53.0
            } catch (e: NumberFormatException) {
                false
            }
        }

        @Throws(NoSuchFieldException::class)
        private fun getDeclaredField(root: Class<*>, fieldName: String): java.lang.reflect.Field {
            var type: Class<*>? = root
            do {
                type = try {
                    return type!!.getDeclaredField(fieldName)
                } catch (e: NoSuchFieldException) {
                    type!!.superclass
                } catch (e: SecurityException) {
                    type!!.superclass
                }

            } while (type != null)
            throw NoSuchFieldException(fieldName + " does not exist in " + root.name + " or any of its superclasses.") //$NON-NLS-1$ //$NON-NLS-2$
        }

        val ADDRESS = getDeclaredField(ByteBuffer::class.java, "address").apply { this.isAccessible = true }
        val ADDRESS_FIELDOFFSET = UNSAFE.objectFieldOffset(ADDRESS)
        private fun throwNoDirectBufferException() {
            throw IllegalArgumentException("Must use a direct buffer")
        }
    }
}

class Options {
    companion object {
        val NO_UNSAFE = false
        val DEBUG = false
    }
}