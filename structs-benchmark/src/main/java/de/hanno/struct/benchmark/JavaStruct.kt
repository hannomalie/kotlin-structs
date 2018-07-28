package de.hanno.struct.benchmark

import de.hanno.struct.SlidingWindow

class JavaStruct: SlidingWindow() {
    val a by 3
    val b by 0.5f
    val c by 234234L
}
class JavaMutableStruct: SlidingWindow() {
    var a by 3
    var b by 0.5f
    var c by 234234L
}