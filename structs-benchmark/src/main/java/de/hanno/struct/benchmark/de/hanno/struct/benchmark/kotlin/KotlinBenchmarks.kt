package de.hanno.struct.benchmark.de.hanno.struct.benchmark.kotlin

import de.hanno.struct.StructArray
import de.hanno.struct.benchmark.SimpleMutableStruct
import de.hanno.struct.benchmark.SimpleStruct
import de.hanno.struct.benchmark.StructBenchmark
import de.hanno.struct.forEach
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.infra.Blackhole

@State(Scope.Thread)
open class IterateAndMutateStructArrayState {
    @JvmField
    val structArray = StructArray(StructBenchmark.LIST_SIZE) { SimpleMutableStruct() }
}

@State(Scope.Thread)
open class IterateAndMutateStructArrayIndexedState {
    @JvmField
    val structArray = StructArray(StructBenchmark.LIST_SIZE) { SimpleMutableStruct() }
}

@State(Scope.Thread)
open class IterateStructState {
    @JvmField
    val structArray = StructArray(StructBenchmark.LIST_SIZE) { SimpleStruct() }
}

fun iterateAndMutateStructArray(hole: Blackhole, state: IterateAndMutateStructArrayState) {
    state.structArray.forEach(false) { struct: SimpleMutableStruct ->
        struct.a = struct.a + 1
        struct.b = struct.b + 2
        struct.c = struct.c + 3
        hole.consume(struct)
    }
}

fun iterateAndMutateStructArrayIndexed(hole: Blackhole, state: IterateAndMutateStructArrayIndexedState) {
    for (i in 0 until state.structArray.size) {
        val struct = state.structArray.getAtIndex(i)
        struct.a = struct.a + 1
        struct.b = struct.b + 2
        struct.c = struct.c + 3
        hole.consume(struct)
    }
}

fun iterateStruct(hole: Blackhole, state: IterateStructState) {
    state.structArray.forEach(false) { struct: SimpleStruct ->
        hole.consume(struct.a)
        hole.consume(struct.b)
        hole.consume(struct.c)
    }
}