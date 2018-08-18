package de.hanno.struct.benchmark;

import de.hanno.struct.StaticStructArray;
import de.hanno.struct.benchmark.de.hanno.struct.benchmark.kotlin.IterateAndMutateStructArray;
import de.hanno.struct.benchmark.de.hanno.struct.benchmark.kotlin.IterateAndMutateStructArrayIndexed;
import de.hanno.struct.benchmark.de.hanno.struct.benchmark.kotlin.IterateStruct;
import kotlin.Unit;
import org.lwjgl.BufferUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import static de.hanno.struct.StructArrayKt.forEach;

public class StructBenchmark {

    public static final int size = 5000;

    private static ArrayList<JavaVanilla> vanillaArrayList = new ArrayList<>(size);

    private static ArrayList<JavaMutableVanilla> mutableVanillaArrayList = new ArrayList<>(size);

    private static StaticStructArray<SimpleMutableStruct> resizableMutableStructArray = new StaticStructArray<>(null, size, SimpleMutableStruct::new);

    static {
        for (int i = 0; i < size; i++) {
            vanillaArrayList.add(new JavaVanilla(3, 0.5f, 234234L));
            mutableVanillaArrayList.add(new JavaMutableVanilla(3, 0.5f, 234234L));
        }

    }
    private static ByteBuffer directBuffer = BufferUtils.createByteBuffer(12*size);

    private static ByteBuffer simpleSlidingWindowBuffer = BufferUtils.createByteBuffer(12*size);
    private static SimpleSlidingWindow simpleSlidingWindow = new SimpleSlidingWindow(simpleSlidingWindowBuffer);

    private static ByteBuffer kotlinSimpleSlidingWindowBuffer = BufferUtils.createByteBuffer(12*size);
    private static KotlinSimpleSlidingWindow kotlinSimpleSlidingWindow = new KotlinSimpleSlidingWindow(kotlinSimpleSlidingWindowBuffer);

    private static ByteBuffer kotlinDelegatedPropertySimpleSlidingWindowBuffer = BufferUtils.createByteBuffer(12*size);
    private static KotlinDelegatedPropertySimpleSlidingWindow kotlinDelegatedPropertySimpleSlidingWindow = new KotlinDelegatedPropertySimpleSlidingWindow(kotlinDelegatedPropertySimpleSlidingWindowBuffer);

    private static ByteBuffer kotlinDelegatedPropertyUnsafeSimpleSlidingWindowBuffer = BufferUtils.createByteBuffer(12*size);
    private static KotlinDelegatedPropertyUnsafeSimpleSlidingWindow kotlinDelegatedPropertyUnsafeSimpleSlidingWindow = new KotlinDelegatedPropertyUnsafeSimpleSlidingWindow(kotlinDelegatedPropertyUnsafeSimpleSlidingWindowBuffer);

    private static ByteBuffer kotlinDelegatedPropertyUnsafeSlidingWindowBuffer = BufferUtils.createByteBuffer(12*size);
    private static KotlinDelegatedPropertyUnsafeSlidingWindow kotlinDelegatedPropertyUnsafeSlidingWindow = new KotlinDelegatedPropertyUnsafeSlidingWindow(kotlinDelegatedPropertyUnsafeSlidingWindowBuffer);

    static {
        simpleSlidingWindowBuffer.rewind();
        kotlinSimpleSlidingWindowBuffer.rewind();
        kotlinDelegatedPropertySimpleSlidingWindowBuffer.rewind();
        kotlinDelegatedPropertyUnsafeSimpleSlidingWindowBuffer.rewind();
        kotlinDelegatedPropertyUnsafeSlidingWindowBuffer.rewind();
    }

    @Benchmark
    @Measurement(iterations = 2)
    @Warmup(iterations = 2)
    public void iterateStruct(Blackhole hole) {
        IterateStruct.Companion.run(hole);
    }

    @Benchmark
    @Measurement(iterations = 2)
    @Warmup(iterations = 2)
    public void iterateVanilla(Blackhole hole) {
        vanillaArrayList.forEach((it) -> {
            hole.consume(it.getA());
            hole.consume(it.getB());
            hole.consume(it.getC());
        });
    }

    @Benchmark
    @Measurement(iterations = 2)
    @Warmup(iterations = 2)
    public void iterateAndMutateStructArray(Blackhole hole) {
        IterateAndMutateStructArray.Companion.run(hole);
    }

    @Benchmark
    @Measurement(iterations = 2)
    @Warmup(iterations = 2)
    public void iterateAndMutateStructArrayIndexed(Blackhole hole) {
        IterateAndMutateStructArrayIndexed.Companion.run(hole);
    }

    @Benchmark
    @Measurement(iterations = 2)
    @Warmup(iterations = 2)
    public void iterateAndMutateBufferDirect(Blackhole hole) {
        for(int i = 0; i <= directBuffer.capacity() - 12; i+=12) {
            directBuffer.putFloat(i, directBuffer.getFloat(i));
            directBuffer.putFloat(i +4, directBuffer.getFloat(i +4));
            directBuffer.putFloat(i +8, directBuffer.getFloat(i +8));
        }
    }
    @Benchmark
    @Measurement(iterations = 2)
    @Warmup(iterations = 2)
    public void iterateAndMutateSimpleSlidingWindowBuffer(Blackhole hole) {
        for(int i = 0; i <= simpleSlidingWindowBuffer.capacity() - 12; i+=12) {
            simpleSlidingWindow.baseByteOffset = i;
            simpleSlidingWindow.setX(simpleSlidingWindow.getX() + 1);
            simpleSlidingWindow.setY(simpleSlidingWindow.getY() + 2);
            simpleSlidingWindow.setZ(simpleSlidingWindow.getZ() + 3);
            hole.consume(simpleSlidingWindow);
        }
    }

    @Benchmark
    @Measurement(iterations = 2)
    @Warmup(iterations = 2)
    public void iterateAndMutateKotlinSimpleSlidingWindowBuffer(Blackhole hole) {
        for(int i = 0; i <= kotlinSimpleSlidingWindowBuffer.capacity() - 12; i+=12) {
            kotlinSimpleSlidingWindow.setBaseByteOffset(i);
            kotlinSimpleSlidingWindow.setX(kotlinSimpleSlidingWindow.getX() + 1);
            kotlinSimpleSlidingWindow.setY(kotlinSimpleSlidingWindow.getY() + 2);
            kotlinSimpleSlidingWindow.setZ(kotlinSimpleSlidingWindow.getZ() + 3);
            hole.consume(kotlinSimpleSlidingWindow);
        }
    }

    @Benchmark
    @Measurement(iterations = 2)
    @Warmup(iterations = 2)
    public void iterateAndMutateKotlinDelegatedPropertySlidingWindowBuffer(Blackhole hole) {
        for(int i = 0; i <= kotlinDelegatedPropertySimpleSlidingWindowBuffer.capacity() - 12; i+=12) {
            kotlinDelegatedPropertySimpleSlidingWindow.setBaseByteOffset(i);
            kotlinDelegatedPropertySimpleSlidingWindow.setX(kotlinDelegatedPropertySimpleSlidingWindow.getX() + 1);
            kotlinDelegatedPropertySimpleSlidingWindow.setY(kotlinDelegatedPropertySimpleSlidingWindow.getY() + 2);
            kotlinDelegatedPropertySimpleSlidingWindow.setZ(kotlinDelegatedPropertySimpleSlidingWindow.getZ() + 3);
            hole.consume(kotlinDelegatedPropertySimpleSlidingWindow);
        }
    }
    @Benchmark
    @Measurement(iterations = 2)
    @Warmup(iterations = 2)
    public void iterateAndMutateKotlinDelegatedPropertyUnsafeSimpleSlidingWindowBuffer(Blackhole hole) {
        for(int i = 0; i <= kotlinDelegatedPropertyUnsafeSimpleSlidingWindowBuffer.capacity() - 12; i+=12) {
            kotlinDelegatedPropertyUnsafeSimpleSlidingWindow.setBaseByteOffset(i);
            kotlinDelegatedPropertyUnsafeSimpleSlidingWindow.setX(kotlinDelegatedPropertyUnsafeSimpleSlidingWindow.getX() + 1);
            kotlinDelegatedPropertyUnsafeSimpleSlidingWindow.setY(kotlinDelegatedPropertyUnsafeSimpleSlidingWindow.getY() + 2);
            kotlinDelegatedPropertyUnsafeSimpleSlidingWindow.setZ(kotlinDelegatedPropertyUnsafeSimpleSlidingWindow.getZ() + 3);
            hole.consume(kotlinDelegatedPropertyUnsafeSimpleSlidingWindow);
        }
    }

    @Benchmark
    @Measurement(iterations = 2)
    @Warmup(iterations = 2)
    public void iterateAndMutateKotlinDelegatedPropertyUnsafeSlidingWindowBuffer(Blackhole hole) {
        for(int i = 0; i <= kotlinDelegatedPropertyUnsafeSlidingWindowBuffer.capacity() - 12; i+=12) {
            kotlinDelegatedPropertyUnsafeSlidingWindow.setSlidingWindowOffset(i);
            kotlinDelegatedPropertyUnsafeSlidingWindow.setX(kotlinDelegatedPropertyUnsafeSlidingWindow.getX() + 1);
            kotlinDelegatedPropertyUnsafeSlidingWindow.setY(kotlinDelegatedPropertyUnsafeSlidingWindow.getY() + 2);
            kotlinDelegatedPropertyUnsafeSlidingWindow.setZ(kotlinDelegatedPropertyUnsafeSlidingWindow.getZ() + 3);
            hole.consume(kotlinDelegatedPropertyUnsafeSlidingWindow);
        }
    }

    @Benchmark
    @Measurement(iterations = 2)
    @Warmup(iterations = 2)
    public void iterateAndMutateResizableStruct(Blackhole hole) {
        forEach(resizableMutableStructArray, false, (SimpleMutableStruct struct) -> {
            struct.setA(struct.getA() + 1);
            struct.setB(struct.getB() + 2);
            struct.setC(struct.getC() + 3);
            hole.consume(struct);
            return Unit.INSTANCE;
        });
    }

    @Benchmark
    @Measurement(iterations = 2)
    @Warmup(iterations = 2)
    public void iterateAndMutateVanilla(Blackhole hole) {
        mutableVanillaArrayList.forEach((JavaMutableVanilla nonStruct) -> {
            nonStruct.setA(nonStruct.getA() + 1);
            nonStruct.setB(nonStruct.getB() + 2);
            nonStruct.setC(nonStruct.getC() + 3);
            hole.consume(nonStruct);
        });
    }

}
