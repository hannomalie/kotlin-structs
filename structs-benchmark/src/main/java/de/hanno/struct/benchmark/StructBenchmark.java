package de.hanno.struct.benchmark;

import de.hanno.struct.StructArray;
import de.hanno.struct.benchmark.de.hanno.struct.benchmark.kotlin.IterateAndMutateStructArrayIndexedState;
import de.hanno.struct.benchmark.de.hanno.struct.benchmark.kotlin.IterateAndMutateStructArrayState;
import de.hanno.struct.benchmark.de.hanno.struct.benchmark.kotlin.IterateStructState;
import kotlin.Unit;
import org.lwjgl.BufferUtils;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.spf4j.stackmonitor.JmhFlightRecorderProfiler;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import static de.hanno.struct.ArrayKt.forEach;
import static de.hanno.struct.benchmark.de.hanno.struct.benchmark.kotlin.KotlinBenchmarksKt.*;

@Fork(1)
@Measurement(iterations = 1)
@Warmup(iterations = 1)
public class StructBenchmark {

    public static final int LIST_SIZE = 5000;

    @State(Scope.Thread)
    public static class VanillaArrayListState {
        public final ArrayList<JavaVanilla> list = new ArrayList<>(LIST_SIZE);
        @Setup
        public void setUp() {
            for (int i = 0; i < LIST_SIZE; i++) {
                list.add(new JavaVanilla(3, 0.5f, 234234L));
            }
        }
    }

    @State(Scope.Thread)
    public static class MutableVanillaArrayListState {
        public final ArrayList<JavaMutableVanilla> list = new ArrayList<>(LIST_SIZE);
        @Setup
        public void setUp() {
            for (int i = 0; i < LIST_SIZE; i++) {
                list.add(new JavaMutableVanilla(3, 0.5f, 234234L));
            }
        }
    }

    @State(Scope.Thread)
    public static class ResizableMutableStructArrayState {
        public final StructArray<SimpleMutableStruct> array = new StructArray<>(LIST_SIZE, SimpleMutableStruct::new);
    }

    @State(Scope.Thread)
    public static class DirectBufferState {
        public final ByteBuffer directBuffer = BufferUtils.createByteBuffer(12* LIST_SIZE);
    }

    @State(Scope.Thread)
    public static class SimpleSlidingWindowBufferState {
        public final ByteBuffer simpleSlidingWindowBuffer = BufferUtils.createByteBuffer(12* LIST_SIZE);
        public final SimpleSlidingWindow simpleSlidingWindow = new SimpleSlidingWindow(simpleSlidingWindowBuffer);
    }

    @State(Scope.Thread)
    public static class KotlinSimpleSlidingWindowBufferState {
        public final ByteBuffer buffer = BufferUtils.createByteBuffer(12* LIST_SIZE);
        public final KotlinSimpleSlidingWindow slidingWindow = new KotlinSimpleSlidingWindow(buffer);
    }

    @State(Scope.Thread)
    public static class KotlinDelegatedPropertySimpleSlidingWindowBufferState {
        public final ByteBuffer buffer = BufferUtils.createByteBuffer(12* LIST_SIZE);
        public final KotlinDelegatedPropertySimpleSlidingWindow slidingWindow = new KotlinDelegatedPropertySimpleSlidingWindow(buffer);
    }

    @State(Scope.Thread)
    public static class KotlinDelegatedPropertyUnsafeSimpleSlidingWindowBufferState {
        public final ByteBuffer buffer = BufferUtils.createByteBuffer(12* LIST_SIZE);
        public final KotlinDelegatedPropertyUnsafeSimpleSlidingWindow slidingWindow = new KotlinDelegatedPropertyUnsafeSimpleSlidingWindow(buffer);
    }

    @State(Scope.Thread)
    public static class KotlinDelegatedPropertyUnsafeSlidingWindowBufferState {
        public final ByteBuffer buffer = BufferUtils.createByteBuffer(12* LIST_SIZE);
        public final KotlinDelegatedPropertyUnsafeSlidingWindow windowStruct = new KotlinDelegatedPropertyUnsafeSlidingWindow(buffer);
    }


    @Benchmark
    public void iterateVanilla(Blackhole hole, VanillaArrayListState state) {
        state.list.forEach((it) -> {
            hole.consume(it.getA());
            hole.consume(it.getB());
            hole.consume(it.getC());
        });
    }

    @Benchmark
    public void iterateAndMutateBufferDirect(Blackhole hole, DirectBufferState state) {
        for(int i = 0; i <= state.directBuffer.capacity() - 12; i+=12) {
            state.directBuffer.putFloat(i, state.directBuffer.getFloat(i));
            state.directBuffer.putFloat(i +4, state.directBuffer.getFloat(i +4));
            state.directBuffer.putFloat(i +8, state.directBuffer.getFloat(i +8));
        }
    }
    @Benchmark
    public void iterateAndMutateSimpleSlidingWindowBuffer(Blackhole hole, SimpleSlidingWindowBufferState state) {
        for(int i = 0; i <= state.simpleSlidingWindowBuffer.capacity() - 12; i+=12) {
            state.simpleSlidingWindow.baseByteOffset = i;
            state.simpleSlidingWindow.setX(state.simpleSlidingWindow.getX() + 1);
            state.simpleSlidingWindow.setY(state.simpleSlidingWindow.getY() + 2);
            state.simpleSlidingWindow.setZ(state.simpleSlidingWindow.getZ() + 3);
            hole.consume(state.simpleSlidingWindow);
        }
    }

    @Benchmark
    public void iterateAndMutateKotlinSimpleSlidingWindowBuffer(Blackhole hole, KotlinSimpleSlidingWindowBufferState state) {
        for(int i = 0; i <= state.buffer.capacity() - 12; i+=12) {
            state.slidingWindow.setBaseByteOffset(i);
            state.slidingWindow.setX(state.slidingWindow.getX() + 1);
            state.slidingWindow.setY(state.slidingWindow.getY() + 2);
            state.slidingWindow.setZ(state.slidingWindow.getZ() + 3);
            hole.consume(state.slidingWindow);
        }
    }

    @Benchmark
    public void iterateAndMutateKotlinDelegatedPropertySlidingWindowBuffer(Blackhole hole, KotlinDelegatedPropertySimpleSlidingWindowBufferState state) {
        for(int i = 0; i <= state.buffer.capacity() - 12; i+=12) {
            state.slidingWindow.setBaseByteOffset(i);
            state.slidingWindow.setX(state.slidingWindow.getX() + 1);
            state.slidingWindow.setY(state.slidingWindow.getY() + 2);
            state.slidingWindow.setZ(state.slidingWindow.getZ() + 3);
            hole.consume(state.slidingWindow);
        }
    }
    @Benchmark
    public void iterateAndMutateKotlinDelegatedPropertyUnsafeSimpleSlidingWindowBuffer(Blackhole hole, KotlinDelegatedPropertyUnsafeSimpleSlidingWindowBufferState state) {
        for(int i = 0; i <= state.buffer.capacity() - 12; i+=12) {
            state.slidingWindow.setBaseByteOffset(i);
            state.slidingWindow.setX(state.slidingWindow.getX() + 1);
            state.slidingWindow.setY(state.slidingWindow.getY() + 2);
            state.slidingWindow.setZ(state.slidingWindow.getZ() + 3);
            hole.consume(state.slidingWindow);
        }
    }

    @Benchmark
    public void iterateAndMutateKotlinDelegatedPropertyUnsafeSlidingWindowBuffer(Blackhole hole, KotlinDelegatedPropertyUnsafeSlidingWindowBufferState state) {
        for(int i = 0; i <= state.buffer.capacity() - 12; i+=12) {
            state.windowStruct.setLocalByteOffset(i);
            state.windowStruct.setX(state.windowStruct.getX() + 1);
            state.windowStruct.setY(state.windowStruct.getY() + 2);
            state.windowStruct.setZ(state.windowStruct.getZ() + 3);
            hole.consume(state.windowStruct);
        }
    }

    @Benchmark
    public void iterateAndMutateResizableStruct(Blackhole hole, ResizableMutableStructArrayState state) {
        forEach(state.array, false, (SimpleMutableStruct struct) -> {
            struct.setA(struct.getA() + 1);
            struct.setB(struct.getB() + 2);
            struct.setC(struct.getC() + 3);
            hole.consume(struct);
            return Unit.INSTANCE;
        });
    }

    @Benchmark
    public void iterateAndMutateVanilla(Blackhole hole, MutableVanillaArrayListState state) {
        state.list.forEach((JavaMutableVanilla nonStruct) -> {
            nonStruct.setA(nonStruct.getA() + 1);
            nonStruct.setB(nonStruct.getB() + 2);
            nonStruct.setC(nonStruct.getC() + 3);
            hole.consume(nonStruct);
        });
    }


    // Kotlin

    @Benchmark
    public void kotlin_iterateStruct(Blackhole hole, IterateStructState state) {
        iterateStruct(hole, state);
    }
    @Benchmark
    public void kotlin_iterateAndMutateStructArrayIndexed(Blackhole hole, IterateAndMutateStructArrayIndexedState state) {
        iterateAndMutateStructArrayIndexed(hole, state);
    }
    @Benchmark
    public void kotlin_iterateAndMutateStructArray(Blackhole hole, IterateAndMutateStructArrayState state) {
        iterateAndMutateStructArray(hole, state);
    }


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(".*")
                .addProfiler(JmhFlightRecorderProfiler.class)
                .build();
        new Runner(opt).run();
    }
}
