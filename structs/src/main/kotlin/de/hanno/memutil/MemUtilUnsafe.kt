package de.hanno.memutil

import java.nio.ByteBuffer

class MemUtilUnsafe : MemUtil {
    override fun putBoolean(dst: ByteBuffer, offset: Long, value: Boolean) {
        UNSAFE.putInt(dst.address + offset, if(value) 1 else 0)
    }

    override fun getBoolean(dst: ByteBuffer, offset: Long) = UNSAFE.getInt(dst.address + offset) == 1

    override fun putFloat(dst: ByteBuffer, offset: Long, value: Float) {
        UNSAFE.putFloat(dst.address + offset, value)
    }

    override fun getFloat(dst: ByteBuffer, offset: Long) = UNSAFE.getFloat(dst.address + offset)

    override fun putLong(dst: ByteBuffer, offset: Long, value: Long) {
        UNSAFE.putLong(dst.address + offset, value)
    }

    override fun getLong(dst: ByteBuffer, offset: Long) = UNSAFE.getLong(dst.address + offset)

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