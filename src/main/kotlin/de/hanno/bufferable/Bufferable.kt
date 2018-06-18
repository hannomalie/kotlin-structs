package de.hanno.bufferable

import de.hanno.struct.Layout
import de.hanno.struct.Struct
import javafx.beans.property.ReadOnlyProperty
import org.lwjgl.system.MemoryUtil
import kotlin.reflect.KProperty

//interface Bufferable {}
//
//
//abstract class StructArray: Struct {
//    val elementCount: IntStruct
//}
//
//
//class StructContainer(val layout: Layout, override val elementCount: IntStruct): StructArray {
//    val buffer: Any = MemoryUtil.memAlloc(layout.getStructDescriptions().value * elementCount)
//    override val sizeInBytes = layout.getStructDescriptions().value * elementCount
//}
//
//
//class StructRegistry<T> {
//    private val registry: MutableList<T> = mutableListOf()
//
//    operator fun provideDelegate(thisRef: T, prop: KProperty<*>): ReadOnlyProperty {
//        registry.add(thisRef)
//        return prop
//    }
//}