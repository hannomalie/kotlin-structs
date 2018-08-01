package de.hanno.struct.benchmark;

import de.hanno.struct.StaticStructArray;
import kotlin.Unit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;

import static de.hanno.struct.StructArrayKt.copyTo;
import static de.hanno.struct.StructArrayKt.forEach;

public class StructBenchmark {

    static final int size = 5000;

    static StaticStructArray<JavaStruct> structArray = new StaticStructArray<>(null, size, it -> new JavaStruct(it));
    static ArrayList<JavaVanilla> vanillaArrayList = new ArrayList<>(size);

    static StaticStructArray<JavaMutableStruct> mutableStructArray = new StaticStructArray<>(null, size, it -> new JavaMutableStruct(it));
    static ArrayList<JavaMutableVanilla> mutableVanillaArrayList = new ArrayList<>(size);

    static StaticStructArray<JavaMutableStruct> resizableMutableStructArray = new StaticStructArray<>(null, size, it -> new JavaMutableStruct(it));

    static {
        for (int i = 0; i < size; i++) {
            vanillaArrayList.add(new JavaVanilla(3, 0.5f, 234234L));
            mutableVanillaArrayList.add(new JavaMutableVanilla(3, 0.5f, 234234L));
        }

    }

    static StaticStructArray<JavaStruct> structArrayToCopySource = new StaticStructArray<>(null, 20000, it -> new JavaStruct(it));
    static StaticStructArray<JavaStruct> structArrayToCopyTarget = new StaticStructArray<>(null, 20000, it -> new JavaStruct(it));

    @Benchmark
    @Measurement(iterations = 2)
    @Warmup(iterations = 1)
    public void iterateStruct(Blackhole hole) {
        forEach(structArray, (JavaStruct struct) -> {
            hole.consume(struct);
            return Unit.INSTANCE;
        });
    }

    @Benchmark
    @Measurement(iterations = 2)
    @Warmup(iterations = 1)
    public void iterateVanilla(Blackhole hole) {
        vanillaArrayList.forEach(hole::consume);
    }

    @Benchmark
    @Measurement(iterations = 2)
    @Warmup(iterations = 1)
    public void iterateAndMutateStruct(Blackhole hole) {
        forEach(mutableStructArray, (JavaMutableStruct struct) -> {
            struct.setA(struct.getA() + 1);
            struct.setB(struct.getB() + 2);
            struct.setC(struct.getC() + 3);
            hole.consume(struct);
            return Unit.INSTANCE;
        });
    }

    @Benchmark
    @Measurement(iterations = 2)
    @Warmup(iterations = 1)
    public void iterateAndMutateResizableStruct(Blackhole hole) {
        forEach(resizableMutableStructArray, (JavaMutableStruct struct) -> {
            struct.setA(struct.getA() + 1);
            struct.setB(struct.getB() + 2);
            struct.setC(struct.getC() + 3);
            hole.consume(struct);
            return Unit.INSTANCE;
        });
    }

    @Benchmark
    @Measurement(iterations = 2)
    @Warmup(iterations = 1)
    public void iterateAndMutateVanilla(Blackhole hole) {
        mutableVanillaArrayList.forEach((JavaMutableVanilla nonStruct) -> {
            nonStruct.setA(nonStruct.getA() + 1);
            nonStruct.setB(nonStruct.getB() + 2);
            nonStruct.setC(nonStruct.getC() + 3);
            hole.consume(nonStruct);
        });
    }

    @Benchmark
    @Measurement(iterations = 2)
    @Warmup(iterations = 1)
    public void copyStructArray(Blackhole hole) {
        copyTo(structArrayToCopySource, structArrayToCopyTarget, true);
    }
}
