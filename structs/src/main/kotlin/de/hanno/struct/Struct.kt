package de.hanno.struct

import de.hanno.memutil.BufferAccess
import de.hanno.memutil.MemUtil
import org.lwjgl.BufferUtils
import java.nio.ByteBuffer
import kotlin.reflect.KProperty

fun <T: Struct> T.copyTo(target: T) {
    val oldTargetBufferPosition = target.buffer.position()
    val oldSourceBufferPosition = buffer.position()

    target.buffer.position(target.baseByteOffset.toInt())
    buffer.position(baseByteOffset.toInt())

    target.buffer.put(buffer)
    target.buffer.position(oldTargetBufferPosition)
    buffer.position(oldSourceBufferPosition)
}
fun <T: Struct> T.copyFrom(target: T) {
    target.copyTo(this)
}

abstract class Struct {
    var parent: Struct? = null
        internal set(value) {
            if(field != null) throw IllegalStateException("Cannot reassign struct to a parent!")
            field = value
            localByteOffset = value?.getCurrentLocalByteOffset() ?: 0
        }
    private val parentBaseByteOffset
            get() = parent?.baseByteOffset ?: 0
    val memberStructs = mutableListOf<StructProperty>()
    open val sizeInBytes by lazy {
        memberStructs.sumBy { it.sizeInBytes }
    }
    var localByteOffset: Long = 0
        internal set(value) {
            field = value
        }

    val baseByteOffset: Long
        get() = localByteOffset + parentBaseByteOffset

    open val ownBuffer by lazy { BufferUtils.createByteBuffer(sizeInBytes) }
    open var provideBuffer: () -> ByteBuffer = {
        parent?.buffer ?: ownBuffer
    }

    val buffer: ByteBuffer
        get() = provideBuffer()


    val <T: Struct> T.LongAsDouble: Struct.LongAsDoubleHelper<T>
        get() { return LongAsDoubleHelper(this) }

    fun usesOwnBuffer(): Boolean = parent == null
    fun getCurrentLocalByteOffset() = memberStructs.sumBy { it.sizeInBytes }.toLong()

    operator fun Int.provideDelegate(thisRef: Struct, prop: KProperty<*>): IntProperty {
        return IntProperty(getCurrentLocalByteOffset())
                .apply { thisRef.register(this@apply) }
                .apply { if(this@provideDelegate != 0) this.setValue(thisRef, prop, this@provideDelegate) }
    }

    operator fun Float.provideDelegate(thisRef: Struct, prop: KProperty<*>): FloatProperty {
        return FloatProperty(getCurrentLocalByteOffset())
                .apply { thisRef.register(this@apply) }
                .apply { if(this@provideDelegate != 0f) this.setValue(thisRef, prop, this@provideDelegate) }
    }

    operator fun Double.provideDelegate(thisRef: Struct, prop: KProperty<*>): DoubleProperty {
        return DoubleProperty(getCurrentLocalByteOffset())
                .apply { thisRef.register(this@apply) }
                .apply { if(this@provideDelegate != .0) this.setValue(thisRef, prop, this@provideDelegate) }
    }

    fun <T: Struct> T.longAsDouble(): Struct.LongAsDoubleHelper<T> { return LongAsDoubleHelper(this) }
    operator fun Long.provideDelegate(thisRef: Struct, prop: KProperty<*>): LongProperty {
        return LongProperty(getCurrentLocalByteOffset())
                .apply { thisRef.register(this@apply) }
                .apply { if(this@provideDelegate != 0L) this.setValue(thisRef, prop, this@provideDelegate) }
    }

    operator fun Boolean.provideDelegate(thisRef: Struct, prop: KProperty<*>): BooleanProperty {
        return BooleanProperty(getCurrentLocalByteOffset())
                .apply { thisRef.register(this@apply) }
                .apply { if(this@provideDelegate) this.setValue(thisRef, prop, this@provideDelegate) }
    }

    operator fun <ENUM: Enum<*>> Class<ENUM>.provideDelegate(thisRef: Struct, prop: KProperty<*>): EnumProperty<ENUM> {
        return EnumProperty(getCurrentLocalByteOffset(), this)
                .apply { thisRef.register(this@apply) }
//        TODO: Make this possible
//                .apply { if(this@provideDelegate) this.setValue(thisRef, prop, this@provideDelegate) }
    }

    operator fun <FIELD: Struct> FIELD.provideDelegate(thisRef: Struct, prop: KProperty<*>): GenericStructProperty<Struct, FIELD> {
        return thisRef.register(this)
    }

    fun <T: Struct> register(struct: T): GenericStructProperty<Struct, T> {
        return object : GenericStructProperty<Struct, T>() {
            override val sizeInBytes by lazy {
                struct.sizeInBytes
            }
            override val localByteOffset = this@Struct.getCurrentLocalByteOffset()
            override var currentRef = struct
        }.apply {
            struct.parent = this@Struct
            register(this)
        }
    }

    fun register(structProperty: StructProperty) {
        memberStructs.add(structProperty)
    }

    class LongAsDoubleHelper<T: Struct>(val parent: T) {
        operator fun provideDelegate(thisRef: T, prop: KProperty<*>): LongAsDoubleProperty {
            return LongAsDoubleProperty(parent.getCurrentLocalByteOffset())
                    .apply { thisRef.register(this@apply) }
//                    .apply { if(this@provideDelegate != 0L) this.setValue(thisRef, prop, this@provideDelegate) }
        }
    }
}

class SlidingWindow<T: Struct>(val underlying: T) {
    var localByteOffset: Long
        get() = underlying.localByteOffset
        set(value) {
            underlying.localByteOffset = value
        }
    val sizeInBytes = underlying.sizeInBytes

    var parent: Struct?
        get() = underlying.parent
        set(value) {
            underlying.parent = value
        }
}

interface StructProperty {
    val localByteOffset: Long
    val sizeInBytes: Int

    companion object: MemUtil by MemUtil.Companion
}
abstract class GenericStructProperty<OWNER_TYPE: Struct, FIELD_TYPE> : StructProperty{
    abstract var currentRef: FIELD_TYPE

    operator fun getValue(thisRef: OWNER_TYPE, property: KProperty<*>): FIELD_TYPE {
        return currentRef
    }

    operator fun setValue(thisRef: OWNER_TYPE, property: KProperty<*>, value: FIELD_TYPE) {
        currentRef = value
    }
}

class IntProperty(override val localByteOffset: Long):StructProperty {
    override val sizeInBytes
        get() = 4

    operator fun setValue(thisRef: Struct, property: KProperty<*>, value: Int) {
        BufferAccess.putInt(thisRef.buffer, thisRef.baseByteOffset + localByteOffset, value)
    }
    operator fun getValue(thisRef: Struct, property: KProperty<*>) = BufferAccess.getInt(thisRef.buffer, thisRef.baseByteOffset + localByteOffset)
}
class EnumProperty<ENUM: Enum<*>>(override val localByteOffset: Long, val enumClass: Class<ENUM>):StructProperty {
    override val sizeInBytes
        get() = 4
    val enumValues
        get() = enumClass.enumConstants

    operator fun setValue(thisRef: Struct, property: KProperty<*>, value: ENUM) {
        BufferAccess.putInt(thisRef.buffer, thisRef.baseByteOffset + localByteOffset, value.ordinal)
    }
    operator fun getValue(thisRef: Struct, property: KProperty<*>) = enumValues[BufferAccess.getInt(thisRef.buffer, thisRef.baseByteOffset + localByteOffset)]
}

class FloatProperty(override val localByteOffset: Long):StructProperty {
    override val sizeInBytes
        get() = 4

    operator fun setValue(thisRef: Struct, property: KProperty<*>, value: Float) {
        BufferAccess.putFloat(thisRef.buffer, thisRef.baseByteOffset + localByteOffset, value)
    }
    operator fun getValue(thisRef: Struct, property: KProperty<*>) = BufferAccess.getFloat(thisRef.buffer, thisRef.baseByteOffset + localByteOffset)
}

class DoubleProperty(override val localByteOffset: Long):StructProperty {
    override val sizeInBytes
        get() = 8

    operator fun setValue(thisRef: Struct, property: KProperty<*>, value: Double) {
        BufferAccess.putDouble(thisRef.buffer, thisRef.baseByteOffset + localByteOffset, value)
    }
    operator fun getValue(thisRef: Struct, property: KProperty<*>) = BufferAccess.getDouble(thisRef.buffer, thisRef.baseByteOffset + localByteOffset)
}
class LongProperty(override val localByteOffset: Long):StructProperty {
    override val sizeInBytes
        get() = 8

    operator fun setValue(thisRef: Struct, property: KProperty<*>, value: Long) {
        BufferAccess.putLong(thisRef.buffer, thisRef.baseByteOffset + localByteOffset, value)
    }
    operator fun getValue(thisRef: Struct, property: KProperty<*>) = BufferAccess.getLong(thisRef.buffer, thisRef.baseByteOffset + localByteOffset)
}
class BooleanProperty(override val localByteOffset: Long):StructProperty {
    override val sizeInBytes
        get() = 4

    operator fun setValue(thisRef: Struct, property: KProperty<*>, value: Boolean) {
        BufferAccess.putInt(thisRef.buffer, thisRef.baseByteOffset + localByteOffset, if(value) 1 else 0)
    }
    operator fun getValue(thisRef: Struct, property: KProperty<*>) = BufferAccess.getInt(thisRef.buffer, thisRef.baseByteOffset + localByteOffset) == 1
}

class LongAsDoubleProperty(override val localByteOffset: Long): StructProperty {
    override val sizeInBytes
        get() = 8

    operator fun setValue(thisRef: Struct, property: KProperty<*>, value: Long) {
        StructProperty.putDouble(thisRef.buffer, thisRef.baseByteOffset + localByteOffset, java.lang.Double.longBitsToDouble(value))
    }
    operator fun getValue(thisRef: Struct, property: KProperty<*>): Long {
        return java.lang.Double.doubleToLongBits(BufferAccess.getDouble(thisRef.buffer, thisRef.baseByteOffset + localByteOffset))
    }
}
