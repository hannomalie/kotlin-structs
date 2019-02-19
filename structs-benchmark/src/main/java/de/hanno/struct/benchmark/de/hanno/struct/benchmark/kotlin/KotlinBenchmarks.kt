package de.hanno.struct.benchmark.de.hanno.struct.benchmark.kotlin

import de.hanno.struct.StaticStructArray
import de.hanno.struct.Struct
import de.hanno.struct.benchmark.SimpleMutableStruct
import de.hanno.struct.benchmark.SimpleStruct
import de.hanno.struct.benchmark.StructBenchmark
import de.hanno.struct.benchmark.StructBenchmark.size
import de.hanno.struct.forEach
import org.openjdk.jmh.infra.Blackhole

class IterateAndMutateStructArray {
    companion object {
        @JvmStatic private val mutableStructArray = StaticStructArray(StructBenchmark.size) { parent: Struct -> SimpleMutableStruct(parent) }
        @JvmStatic fun run(hole: Blackhole) {
            mutableStructArray.forEach(false) { struct: SimpleMutableStruct ->
                struct.a = struct.a + 1
                struct.b = struct.b + 2
                struct.c = struct.c + 3
                hole.consume(struct)
            }
        }
    }
}
class IterateAndMutateStructArrayIndexed {
    companion object {
        @JvmStatic private val mutableStructArrayIndexIteration = StaticStructArray(size) { parent: Struct -> SimpleMutableStruct(parent) }
        @JvmStatic fun run(hole: Blackhole) {
            for (i in 0 until mutableStructArrayIndexIteration.size) {
                val struct = mutableStructArrayIndexIteration.getAtIndex(i)
                struct.a = struct.a + 1
                struct.b = struct.b + 2
                struct.c = struct.c + 3
                hole.consume(struct)
            }
        }
    }
}
class IterateStruct {
    companion object {
        @JvmStatic private val structArray = StaticStructArray(size) { parent: Struct -> SimpleStruct(parent) }
        @JvmStatic fun run(hole: Blackhole) {
            structArray.forEach(false) { struct: SimpleStruct ->
                hole.consume(struct.a)
                hole.consume(struct.b)
                hole.consume(struct.c)
            }
        }
    }
}