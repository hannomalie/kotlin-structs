package de.hanno.struct.benchmark

import de.hanno.struct.Struct

class JavaStruct(parent: Struct? = null) : Struct(parent) {
    val a by 0
    val b by 0.0f
    val c by 0L
}
class JavaMutableStruct(parent: Struct? = null): Struct(parent) {
    var a by 0
    var b by 0.0f
    var c by 0L
}