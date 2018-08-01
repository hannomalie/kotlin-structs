package de.hanno.struct.benchmark;

import de.hanno.struct.StructArray;
import kotlin.Unit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;

import static de.hanno.struct.StructArrayKt.copyTo;

public class StructBenchmark {

    static final int size = 5000;

    static StructArray<JavaStruct> structArray = new StructArray<>(null, size, it -> new JavaStruct(it));
    static ArrayList<JavaVanilla> vanillaArrayList = new ArrayList<>(size);

    static StructArray<JavaMutableStruct> mutableStructArray = new StructArray<>(null, size, it -> new JavaMutableStruct(it));
    static ArrayList<JavaMutableVanilla> mutableVanillaArrayList = new ArrayList<>(size);

    static {
        for (int i = 0; i < size; i++) {
            vanillaArrayList.add(new JavaVanilla(3, 0.5f, 234234L));
            mutableVanillaArrayList.add(new JavaMutableVanilla(3, 0.5f, 234234L));
        }

    }

    static StructArray<JavaStruct> structArrayToCopySource = new StructArray<>(null, 20000, it -> new JavaStruct(it));
    static StructArray<JavaStruct> structArrayToCopyTarget = new StructArray<>(null, 20000, it -> new JavaStruct(it));

    @Benchmark
    @Measurement(iterations = 2)
    @Warmup(iterations = 1)
    public void iterateStruct(Blackhole hole) {
        structArray.forEach((JavaStruct struct) -> {
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
        mutableStructArray.forEach((JavaMutableStruct struct) -> {
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
