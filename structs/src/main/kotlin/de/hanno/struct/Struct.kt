@file:Suppress("NOTHING_TO_INLINE")

package de.hanno.struct

import de.hanno.memutil.BufferAccess
import de.hanno.memutil.MemUtil
import org.lwjgl.BufferUtils
import java.nio.ByteBuffer
import kotlin.reflect.KProperty

fun <T : Struct> T.copyTo(target: T) {
    val oldTargetBufferPosition = target.buffer.position()
    val oldSourceBufferPosition = buffer.position()

    target.buffer.position(target.baseByteOffset.toInt())
    buffer.position(baseByteOffset.toInt())

    target.buffer.put(buffer)
    target.buffer.position(oldTargetBufferPosition)
    buffer.position(oldSourceBufferPosition)
}

fun <T : Struct> T.copyFrom(target: T) {
    target.copyTo(this)
}

abstract class Struct {
    var parent: Struct? = null
        internal set(value) {
            if (field != null) throw IllegalStateException("Cannot reassign struct to a parent!")
            field = value
            localByteOffset = value?.getCurrentLocalByteOffset() ?: 0
        }
    private val parentBaseByteOffset
        get() = parent?.baseByteOffset ?: 0
    open var sizeInBytes = 0
        internal set
    var localByteOffset: Long = 0

    val baseByteOffset: Long
        get() = localByteOffset + parentBaseByteOffset

    open val ownBuffer by lazy { BufferUtils.createByteBuffer(sizeInBytes) }
    open fun provideBuffer() = parent?.buffer ?: ownBuffer

    val buffer: ByteBuffer
        get() = provideBuffer()


    val <T : Struct> T.LongAsDouble: LongAsDoubleHelper<T>
        get() {
            return LongAsDoubleHelper(this)
        }

    fun usesOwnBuffer(): Boolean = parent == null
    fun getCurrentLocalByteOffset() = sizeInBytes.toLong()

    operator fun Int.provideDelegate(thisRef: Struct, prop: KProperty<*>): IntProperty {
        return IntProperty(getCurrentLocalByteOffset())
                .apply { thisRef.registerInlineProperty(this@apply.sizeInBytes) }
                .apply { if (this@provideDelegate != 0) this.setValue(thisRef, prop, this@provideDelegate) }
    }

    operator fun Float.provideDelegate(thisRef: Struct, prop: KProperty<*>): FloatProperty {
        return FloatProperty(getCurrentLocalByteOffset())
                .apply { thisRef.registerInlineProperty(this@apply.sizeInBytes) }
                .apply { if (this@provideDelegate != 0f) this.setValue(thisRef, prop, this@provideDelegate) }
    }

    operator fun Double.provideDelegate(thisRef: Struct, prop: KProperty<*>): DoubleProperty {
        return DoubleProperty(getCurrentLocalByteOffset())
                .apply { thisRef.registerInlineProperty(this@apply.sizeInBytes) }
                .apply { if (this@provideDelegate != .0) this.setValue(thisRef, prop, this@provideDelegate) }
    }

    fun <T : Struct> T.longAsDouble(): LongAsDoubleHelper<T> {
        return LongAsDoubleHelper(this)
    }

    operator fun Long.provideDelegate(thisRef: Struct, prop: KProperty<*>): LongProperty {
        return LongProperty(getCurrentLocalByteOffset())
                .apply { thisRef.registerInlineProperty(this@apply.sizeInBytes) }
                .apply { if (this@provideDelegate != 0L) this.setValue(thisRef, prop, this@provideDelegate) }
    }

    operator fun Boolean.provideDelegate(thisRef: Struct, prop: KProperty<*>): BooleanProperty {
        return BooleanProperty(getCurrentLocalByteOffset())
                .apply { thisRef.registerInlineProperty(this@apply.sizeInBytes) }
                .apply { if (this@provideDelegate) this.setValue(thisRef, prop, this@provideDelegate) }
    }

    operator fun <ENUM : Enum<*>> Class<ENUM>.provideDelegate(thisRef: Struct, prop: KProperty<*>): EnumProperty<ENUM> {
        return EnumProperty(getCurrentLocalByteOffset(), this)
                .apply { thisRef.register(this@apply) }
//        TODO: Make this possible
//                .apply { if(this@provideDelegate) this.setValue(thisRef, prop, this@provideDelegate) }
    }

    operator fun <FIELD : Struct> FIELD.provideDelegate(thisRef: Struct, prop: KProperty<*>): GenericStructProperty<Struct, FIELD> {
        return thisRef.register(this)
    }

    fun <T : Struct> register(struct: T): GenericStructProperty<Struct, T> {
        return object : GenericStructProperty<Struct, T>() {
            override val sizeInBytes = struct.sizeInBytes
            override val localByteOffset = this@Struct.getCurrentLocalByteOffset()
            override var currentRef = struct
        }.apply {
            struct.parent = this@Struct
            register(this)
        }
    }

    fun register(structProperty: StructProperty) {
        sizeInBytes += structProperty.sizeInBytes
    }

    @kotlin.PublishedApi
    internal fun registerInlineProperty(sizeInBytes: Int) {
        this.sizeInBytes += sizeInBytes
    }
}

@kotlin.Suppress("NON_PUBLIC_PRIMARY_CONSTRUCTOR_OF_INLINE_CLASS")
inline class LongAsDoubleHelper<T : Struct> private constructor(private val _parent: Struct) {
    //Inline classes aren't allowed to have their property based on a type parameter, and so the current solution is
    //to have the property be of the type param's supertype and then have a public secondary constructor that actually
    //uses the type parameter. The "unit" is to avoid overload resolution ambiguity.
    constructor(parent: T, unit: Unit = Unit) : this(parent)

    val parent: T
        get() {
            //This cast will always succeed since LongAsDoubleHelper only accepts parent values of type T.
            @Suppress("UNCHECKED_CAST")
            return _parent as T
        }

    operator fun provideDelegate(thisRef: T, prop: KProperty<*>): LongAsDoubleProperty {
        return LongAsDoubleProperty(parent.getCurrentLocalByteOffset())
                .apply { thisRef.registerInlineProperty(this@apply.sizeInBytes) }
//                    .apply { if(this@provideDelegate != 0L) this.setValue(thisRef, prop, this@provideDelegate) }
    }
}

@kotlin.Suppress("NON_PUBLIC_PRIMARY_CONSTRUCTOR_OF_INLINE_CLASS")
inline class SlidingWindow<T : Struct> private constructor(private val _underlying: Struct) {
    //Inline classes aren't allowed to have their property based on a type parameter, and so the current solution is
    //to have the property be of the type param's supertype and then have a public secondary constructor that actually
    //uses the type parameter. The "unit" is to avoid overload resolution ambiguity
    constructor(underlying: T, unit: Unit = Unit) : this(underlying)

    val underlying: T
        get() {
            //This cast will always succeed since SlidingWindow only accepts underlying values of type T.
            @Suppress("UNCHECKED_CAST")
            return _underlying as T
        }
    var localByteOffset: Long
        get() = underlying.localByteOffset
        set(value) {
            underlying.localByteOffset = value
        }
    var sizeInBytes
        get() = underlying.sizeInBytes
        set(value) {
            underlying.sizeInBytes = value
        }

    var parent: Struct?
        get() = underlying.parent
        set(value) {
            underlying.parent = value
        }
}

interface StructProperty {
    val localByteOffset: Long
    val sizeInBytes: Int

    companion object : MemUtil by MemUtil.Companion
}

abstract class GenericStructProperty<OWNER_TYPE : Struct, FIELD_TYPE> : StructProperty {
    abstract var currentRef: FIELD_TYPE

    operator fun getValue(thisRef: OWNER_TYPE, property: KProperty<*>): FIELD_TYPE {
        return currentRef
    }

    operator fun setValue(thisRef: OWNER_TYPE, property: KProperty<*>, value: FIELD_TYPE) {
        currentRef = value
    }
}

inline class IntProperty(override val localByteOffset: Long) : StructProperty {
    override val sizeInBytes
        get() = 4

    operator fun setValue(thisRef: Struct, property: KProperty<*>, value: Int) {
        BufferAccess.putInt(thisRef.buffer, thisRef.baseByteOffset + localByteOffset, value)
    }

    operator fun getValue(thisRef: Struct, property: KProperty<*>) = BufferAccess.getInt(thisRef.buffer, thisRef.baseByteOffset + localByteOffset)
}

class EnumProperty<ENUM : Enum<*>>(override val localByteOffset: Long, val enumClass: Class<ENUM>) : StructProperty {
    override val sizeInBytes
        get() = 4
    val enumValues
        get() = enumClass.enumConstants

    operator fun setValue(thisRef: Struct, property: KProperty<*>, value: ENUM) {
        BufferAccess.putInt(thisRef.buffer, thisRef.baseByteOffset + localByteOffset, value.ordinal)
    }

    operator fun getValue(thisRef: Struct, property: KProperty<*>) = enumValues[BufferAccess.getInt(thisRef.buffer, thisRef.baseByteOffset + localByteOffset)]
}

inline class FloatProperty(override val localByteOffset: Long) : StructProperty {
    override val sizeInBytes
        get() = 4

    operator fun setValue(thisRef: Struct, property: KProperty<*>, value: Float) {
        BufferAccess.putFloat(thisRef.buffer, thisRef.baseByteOffset + localByteOffset, value)
    }

    operator fun getValue(thisRef: Struct, property: KProperty<*>) = BufferAccess.getFloat(thisRef.buffer, thisRef.baseByteOffset + localByteOffset)
}

inline class DoubleProperty(override val localByteOffset: Long) : StructProperty {
    override val sizeInBytes
        get() = 8

    operator fun setValue(thisRef: Struct, property: KProperty<*>, value: Double) {
        BufferAccess.putDouble(thisRef.buffer, thisRef.baseByteOffset + localByteOffset, value)
    }

    operator fun getValue(thisRef: Struct, property: KProperty<*>) = BufferAccess.getDouble(thisRef.buffer, thisRef.baseByteOffset + localByteOffset)
}

inline class LongProperty(override val localByteOffset: Long) : StructProperty {
    override val sizeInBytes
        get() = 8

    operator fun setValue(thisRef: Struct, property: KProperty<*>, value: Long) {
        BufferAccess.putLong(thisRef.buffer, thisRef.baseByteOffset + localByteOffset, value)
    }

    operator fun getValue(thisRef: Struct, property: KProperty<*>) = BufferAccess.getLong(thisRef.buffer, thisRef.baseByteOffset + localByteOffset)
}

inline class BooleanProperty(override val localByteOffset: Long) : StructProperty {
    override val sizeInBytes
        get() = 4

    operator fun setValue(thisRef: Struct, property: KProperty<*>, value: Boolean) {
        BufferAccess.putInt(thisRef.buffer, thisRef.baseByteOffset + localByteOffset, if (value) 1 else 0)
    }

    operator fun getValue(thisRef: Struct, property: KProperty<*>) = BufferAccess.getInt(thisRef.buffer, thisRef.baseByteOffset + localByteOffset) == 1
}

inline class LongAsDoubleProperty(override val localByteOffset: Long) : StructProperty {
    override val sizeInBytes
        get() = 8

    operator fun setValue(thisRef: Struct, property: KProperty<*>, value: Long) {
        StructProperty.putDouble(thisRef.buffer, thisRef.baseByteOffset + localByteOffset, java.lang.Double.longBitsToDouble(value))
    }

    operator fun getValue(thisRef: Struct, property: KProperty<*>): Long {
        return java.lang.Double.doubleToLongBits(BufferAccess.getDouble(thisRef.buffer, thisRef.baseByteOffset + localByteOffset))
    }
}
