package de.hanno.struct

import de.hanno.memutil.MemUtil
import de.hanno.struct.StructProperty.Companion.putDouble
import de.hanno.struct.StructProperty.Companion.putFloat
import de.hanno.struct.StructProperty.Companion.putInt
import de.hanno.struct.StructProperty.Companion.putLong
import org.lwjgl.BufferUtils
import java.nio.ByteBuffer
import kotlin.reflect.KProperty

interface Sized {
    val sizeInBytes: Int
}

interface Bufferable: Sized {
    val buffer: ByteBuffer
}

interface Structable: Bufferable {
    val baseByteOffset: Long
    val memberStructs: MutableList<StructProperty>
    fun getCurrentLocalByteOffset() = memberStructs.sumBy { it.sizeInBytes }.toLong()

    operator fun Int.provideDelegate(thisRef: Structable, prop: KProperty<*>): IntProperty {
        return IntProperty(getCurrentLocalByteOffset())
                .apply { thisRef.register(this@apply) }
                .apply { if(this@provideDelegate != 0) this.setValue(thisRef, prop, this@provideDelegate) }
    }
    operator fun Float.provideDelegate(thisRef: Structable, prop: KProperty<*>): FloatProperty {
        return FloatProperty(getCurrentLocalByteOffset())
                .apply { thisRef.register(this@apply) }
                .apply { if(this@provideDelegate != 0f) this.setValue(thisRef, prop, this@provideDelegate) }
    }
    operator fun Double.provideDelegate(thisRef: Structable, prop: KProperty<*>): DoubleProperty {
        return DoubleProperty(getCurrentLocalByteOffset())
                .apply { thisRef.register(this@apply) }
                .apply { this.setValue(thisRef, prop, this@provideDelegate) }
                .apply { if(this@provideDelegate != .0) this.setValue(thisRef, prop, this@provideDelegate) }
    }
    operator fun Long.provideDelegate(thisRef: Structable, prop: KProperty<*>): LongProperty {
        return LongProperty(getCurrentLocalByteOffset())
                .apply { thisRef.register(this@apply) }
                .apply { if(this@provideDelegate != 0L) this.setValue(thisRef, prop, this@provideDelegate) }
    }
    operator fun Boolean.provideDelegate(thisRef: Structable, prop: KProperty<*>): BooleanProperty {
        return BooleanProperty(getCurrentLocalByteOffset())
                .apply { thisRef.register(this@apply) }
                .apply { if(this@provideDelegate) this.setValue(thisRef, prop, this@provideDelegate) }
    }

    operator fun <FIELD: Structable> FIELD.provideDelegate(thisRef: Structable, prop: KProperty<*>): GenericStructProperty<Structable, FIELD> {
        return object : GenericStructProperty<Structable, FIELD>() {
            override val sizeInBytes = this@provideDelegate.sizeInBytes
            override val localByteOffset = thisRef.getCurrentLocalByteOffset()
            override var currentRef = this@provideDelegate

        }.apply { thisRef.register(this) }
    }

//    TODO: Make more specific extension method for resizablestructarray and freeze struct afterwards -> struct members not supported dynamic

    fun register(structProperty: StructProperty) {
        memberStructs.add(structProperty)
    }
}

@JvmOverloads fun <T: Bufferable> T.copyTo(target: T, rewindBuffersBefore: Boolean = true) {
    if(rewindBuffersBefore) {
        buffer.rewind()
        target.buffer.rewind()
    }
    target.buffer.put(buffer)
}
fun <T: Bufferable, S: Bufferable> T.copyToOther(target: S) {
    if(target.sizeInBytes > sizeInBytes) {
        target.buffer.put(buffer)
    } else {
        val tempArray = ByteArray(sizeInBytes)
        this.buffer.rewind()
        this.buffer.get(tempArray, 0, sizeInBytes)
        target.buffer.put(tempArray, 0, sizeInBytes)
    }
}
fun <T: Bufferable> T.copyFrom(target: T) {
    target.copyTo(this)
}

abstract class Struct(val parent: Structable? = null): Structable {
    override val memberStructs = mutableListOf<StructProperty>()
    override val sizeInBytes by lazy {
        memberStructs.sumBy { it.sizeInBytes }
    }
    var localByteOffset: Long = parent?.getCurrentLocalByteOffset() ?: 0

    final override val baseByteOffset: Long
        get() = localByteOffset + (parent?.baseByteOffset ?: 0)

    protected val ownBuffer by lazy { BufferUtils.createByteBuffer(sizeInBytes) }
    override val buffer by lazy { parent?.buffer ?: ownBuffer }
    fun usesOwnBuffer(): Boolean = ownBuffer === buffer
}

interface StructProperty {
    val localByteOffset: Long
    val sizeInBytes: Int

    companion object: MemUtil by MemUtil.Companion
}
abstract class GenericStructProperty<OWNER_TYPE: Structable, FIELD_TYPE> : StructProperty{
    abstract var currentRef: FIELD_TYPE

    inline operator fun getValue(thisRef: OWNER_TYPE, property: KProperty<*>): FIELD_TYPE {
        return currentRef
    }

    inline operator fun setValue(thisRef: OWNER_TYPE, property: KProperty<*>, value: FIELD_TYPE) {
        currentRef = value
    }
}

class IntProperty(override var localByteOffset: Long):StructProperty {
    override val sizeInBytes = 4

    inline operator fun setValue(thisRef: Structable, property: KProperty<*>, value: Int) {
        putInt(thisRef.buffer, thisRef.baseByteOffset + localByteOffset, value)
    }
    inline operator fun getValue(thisRef: Structable, property: KProperty<*>) = StructProperty.Companion.getInt(thisRef.buffer, thisRef.baseByteOffset + localByteOffset)
}

class FloatProperty(override var localByteOffset: Long):StructProperty {
    override val sizeInBytes = 4

    inline operator fun setValue(thisRef: Structable, property: KProperty<*>, value: Float) {
        putFloat(thisRef.buffer, thisRef.baseByteOffset + localByteOffset, value)
    }
    inline operator fun getValue(thisRef: Structable, property: KProperty<*>) = StructProperty.Companion.getFloat(thisRef.buffer, thisRef.baseByteOffset + localByteOffset)
}

class DoubleProperty(override var localByteOffset: Long):StructProperty {
    override val sizeInBytes = 8

    inline operator fun setValue(thisRef: Structable, property: KProperty<*>, value: Double) {
        putDouble(thisRef.buffer, thisRef.baseByteOffset + localByteOffset, value)
    }
    inline operator fun getValue(thisRef: Structable, property: KProperty<*>) = StructProperty.Companion.getDouble(thisRef.buffer, thisRef.baseByteOffset + localByteOffset)
}
class LongProperty(override var localByteOffset: Long):StructProperty {
    override val sizeInBytes = 8

    inline operator fun setValue(thisRef: Structable, property: KProperty<*>, value: Long) {
        putLong(thisRef.buffer, thisRef.baseByteOffset + localByteOffset, value)
    }
    inline operator fun getValue(thisRef: Structable, property: KProperty<*>) = StructProperty.Companion.getLong(thisRef.buffer, thisRef.baseByteOffset + localByteOffset)
}
class BooleanProperty(override var localByteOffset: Long):StructProperty {
    override val sizeInBytes = 4

    inline operator fun setValue(thisRef: Structable, property: KProperty<*>, value: Boolean) {
        putInt(thisRef.buffer, thisRef.baseByteOffset + localByteOffset, if(value) 1 else 0)
    }
    inline operator fun getValue(thisRef: Structable, property: KProperty<*>) = StructProperty.Companion.getInt(thisRef.buffer, thisRef.baseByteOffset + localByteOffset) == 1
}
