package de.hanno.struct

import de.hanno.memutil.MemUtil
import java.lang.foreign.Arena
import java.lang.foreign.MemoryLayout
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout.*
import java.lang.invoke.VarHandle
import kotlin.reflect.KProperty


fun <T: Struct> T.copyTo(target: T) {
    target.buffer.copyFrom(buffer)
//    val oldTargetBufferPosition = target.buffer.position()
//    val oldSourceBufferPosition = buffer.position()
//
//    target.buffer.position(target.baseByteOffset.toInt())
//    buffer.position(baseByteOffset.toInt())
//
//    target.buffer.put(buffer)
//    target.buffer.position(oldTargetBufferPosition)
//    buffer.position(oldSourceBufferPosition)
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
    var layout: MemoryLayout = MemoryLayout.structLayout(
        *(memberStructs.map {
            when(it) {
                is BooleanProperty -> JAVA_BOOLEAN.withName(it.name)
                is DoubleProperty -> JAVA_DOUBLE.withName(it.name)
                is EnumProperty<*> -> JAVA_INT.withName(it.name)
                is FloatProperty -> JAVA_FLOAT.withName(it.name)
                is GenericStructProperty<*, *> -> TODO()
                is IntProperty -> JAVA_INT.withName(it.name)
                is LongAsDoubleProperty -> JAVA_LONG.withName(it.name)
                is LongProperty -> JAVA_LONG.withName(it.name)
            }
        }).toTypedArray()
    )
    open val sizeInBytes by lazy {
        memberStructs.sumBy { it.sizeInBytes }
    }
    var localByteOffset: Long = 0
        internal set(value) {
            field = value
        }

    val baseByteOffset: Long
        get() = localByteOffset + parentBaseByteOffset

    open val ownBuffer by lazy {
        //Arena.global().allocate(sizeInBytes.toLong(), ValueLayout.JAVA_BYTE.byteAlignment())
        Arena.ofAuto().allocate(layout)
    }
    open var provideBuffer: () -> MemorySegment = {
        parent?.buffer ?: ownBuffer
    }

    val buffer: MemorySegment
        get() = provideBuffer()


    val <T: Struct> T.LongAsDouble: Struct.LongAsDoubleHelper<T>
        get() { return LongAsDoubleHelper(this) }

    fun usesOwnBuffer(): Boolean = parent == null
    fun getCurrentLocalByteOffset() = memberStructs.sumBy { it.sizeInBytes }.toLong()

    operator fun Int.provideDelegate(thisRef: Struct, prop: KProperty<*>): IntProperty {
        return IntProperty(prop.name, getCurrentLocalByteOffset())
                .apply { thisRef.register(this@apply) }
                .apply { if(this@provideDelegate != 0) this.setValue(thisRef, prop, this@provideDelegate) }
    }

    operator fun Float.provideDelegate(thisRef: Struct, prop: KProperty<*>): FloatProperty {
        return FloatProperty(prop.name, getCurrentLocalByteOffset())
                .apply { thisRef.register(this@apply) }
                .apply { if(this@provideDelegate != 0f) this.setValue(thisRef, prop, this@provideDelegate) }
    }

    operator fun Double.provideDelegate(thisRef: Struct, prop: KProperty<*>): DoubleProperty {
        return DoubleProperty(prop.name, getCurrentLocalByteOffset())
                .apply { thisRef.register(this@apply) }
                .apply { if(this@provideDelegate != .0) this.setValue(thisRef, prop, this@provideDelegate) }
    }

    fun <T: Struct> T.longAsDouble(): Struct.LongAsDoubleHelper<T> { return LongAsDoubleHelper(this) }
    operator fun Long.provideDelegate(thisRef: Struct, prop: KProperty<*>): LongProperty {
        return LongProperty(prop.name, getCurrentLocalByteOffset())
                .apply { thisRef.register(this@apply) }
                .apply { if(this@provideDelegate != 0L) this.setValue(thisRef, prop, this@provideDelegate) }
    }

    operator fun Boolean.provideDelegate(thisRef: Struct, prop: KProperty<*>): BooleanProperty {
        return BooleanProperty(prop.name, getCurrentLocalByteOffset())
                .apply { thisRef.register(this@apply) }
                .apply { if(this@provideDelegate) this.setValue(thisRef, prop, this@provideDelegate) }
    }

    operator fun <ENUM: Enum<*>> Class<ENUM>.provideDelegate(thisRef: Struct, prop: KProperty<*>): EnumProperty<ENUM> {
        return EnumProperty(prop.name, getCurrentLocalByteOffset(), this)
                .apply { thisRef.register(this@apply) }
//        TODO: Make this possible
//                .apply { if(this@provideDelegate) this.setValue(thisRef, prop, this@provideDelegate) }
    }

    operator fun <FIELD: Struct> FIELD.provideDelegate(thisRef: Struct, prop: KProperty<*>): GenericStructProperty<Struct, FIELD> {
        return thisRef.register(prop.name, this)
    }

    fun <T: Struct> register(name: String, struct: T): GenericStructProperty<Struct, T> {
        return object : GenericStructProperty<Struct, T>() {
            override val name = name
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

        var layout: MemoryLayout = MemoryLayout.structLayout(
            *(memberStructs.map {
                when(it) {
                    is BooleanProperty -> JAVA_INT.withName(it.name)
                    is DoubleProperty -> JAVA_DOUBLE.withName(it.name)
                    is EnumProperty<*> -> JAVA_INT.withName(it.name)
                    is FloatProperty -> JAVA_FLOAT.withName(it.name)
                    is GenericStructProperty<*, *> -> TODO()
                    is IntProperty -> JAVA_INT.withName(it.name)
                    is LongAsDoubleProperty -> JAVA_LONG.withName(it.name)
                    is LongProperty -> JAVA_LONG.withName(it.name)
                }
            }).toTypedArray()
        )
        this.layout = layout
    }

    class LongAsDoubleHelper<T: Struct>(val parent: T) {
        operator fun provideDelegate(thisRef: T, prop: KProperty<*>): LongAsDoubleProperty {
            return LongAsDoubleProperty(prop.name, parent.getCurrentLocalByteOffset())
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

sealed interface StructProperty {
    val name: String
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

class IntProperty(override val name: String, override val localByteOffset: Long):StructProperty {
    override val sizeInBytes= 4

    operator fun setValue(thisRef: Struct, property: KProperty<*>, value: Int) {
        MemUtil.putInt(thisRef.buffer, thisRef.baseByteOffset + localByteOffset, value)
    }
    operator fun getValue(thisRef: Struct, property: KProperty<*>): Int {
        val yHandle: VarHandle = thisRef.layout.varHandle(MemoryLayout.PathElement.groupElement(property.name))
        return yHandle.get(thisRef.buffer) as Int

        //return MemUtil.getInt(thisRef.buffer, thisRef.baseByteOffset + localByteOffset)
    }
}
class EnumProperty<ENUM: Enum<*>>(override val name: String, override val localByteOffset: Long, val enumClass: Class<ENUM>):StructProperty {
    override val sizeInBytes = 4
    val enumValues get() = enumClass.enumConstants

    operator fun setValue(thisRef: Struct, property: KProperty<*>, value: ENUM) {
        MemUtil.putInt(thisRef.buffer, thisRef.baseByteOffset + localByteOffset, value.ordinal)
    }
    operator fun getValue(thisRef: Struct, property: KProperty<*>): ENUM {
        val value = MemUtil.getInt(thisRef.buffer, thisRef.baseByteOffset + localByteOffset)
        return enumValues[value]
    }
}

class FloatProperty(override val name: String, override val localByteOffset: Long):StructProperty {
    override val sizeInBytes= 4

    operator fun setValue(thisRef: Struct, property: KProperty<*>, value: Float) {
        MemUtil.putFloat(thisRef.buffer, thisRef.baseByteOffset + localByteOffset, value)
    }
    operator fun getValue(thisRef: Struct, property: KProperty<*>): Float {
        return MemUtil.getFloat(thisRef.buffer, thisRef.baseByteOffset + localByteOffset)
    }
}

class DoubleProperty(override val name: String, override val localByteOffset: Long):StructProperty {
    override val sizeInBytes= 8

    operator fun setValue(thisRef: Struct, property: KProperty<*>, value: Double) {
        MemUtil.putDouble(thisRef.buffer, thisRef.baseByteOffset + localByteOffset, value)
    }
    operator fun getValue(thisRef: Struct, property: KProperty<*>): Double {
        return MemUtil.getDouble(thisRef.buffer, thisRef.baseByteOffset + localByteOffset)
    }
}
class LongProperty(override val name: String, override val localByteOffset: Long):StructProperty {
    override val sizeInBytes= 8

    operator fun setValue(thisRef: Struct, property: KProperty<*>, value: Long) {
        MemUtil.putLong(thisRef.buffer, thisRef.baseByteOffset + localByteOffset, value)
    }
    operator fun getValue(thisRef: Struct, property: KProperty<*>): Long {
        return MemUtil.getLong(thisRef.buffer, thisRef.baseByteOffset + localByteOffset)
    }
}
class BooleanProperty(override val name: String, override val localByteOffset: Long):StructProperty {
    override val sizeInBytes= 4

    operator fun setValue(thisRef: Struct, property: KProperty<*>, value: Boolean) {
        MemUtil.putInt(thisRef.buffer, thisRef.baseByteOffset + localByteOffset, if(value) 1 else 0)
    }
    operator fun getValue(thisRef: Struct, property: KProperty<*>): Boolean {
        return MemUtil.getInt(thisRef.buffer, thisRef.baseByteOffset + localByteOffset) == 1
    }
}

class LongAsDoubleProperty(override val name: String, override val localByteOffset: Long): StructProperty {
    override val sizeInBytes= 8

    operator fun setValue(thisRef: Struct, property: KProperty<*>, value: Long) {
        MemUtil.putDouble(thisRef.buffer, thisRef.baseByteOffset + localByteOffset, value.toDouble())
    }
    operator fun getValue(thisRef: Struct, property: KProperty<*>): Double {
        return MemUtil.getLong(thisRef.buffer, thisRef.baseByteOffset + localByteOffset).toDouble()
    }
}
