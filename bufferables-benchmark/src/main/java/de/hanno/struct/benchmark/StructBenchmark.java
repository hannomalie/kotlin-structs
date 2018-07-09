package de.hanno.struct.benchmark;

import de.hanno.struct.StructArray;
import kotlin.Unit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;

import static de.hanno.struct.StructArrayKt.copyTo;

public class StructBenchmark {

    static final int size = 5000;

    static StructArray<JavaStruct> structArray = new StructArray<>(size, JavaStruct::new);
    static ArrayList<JavaVanilla> vanillaArrayList = new ArrayList<>(size);

    static StructArray<JavaMutableStruct> mutableStructArray = new StructArray<>(size, JavaMutableStruct::new);
    static ArrayList<JavaMutableVanilla> mutableVanillaArrayList = new ArrayList<>(size);

    static {
        for (int i = 0; i < size; i++) {
            vanillaArrayList.add(new JavaVanilla(3, 0.5f, 234234L));
            mutableVanillaArrayList.add(new JavaMutableVanilla(3, 0.5f, 234234L));
        }

    }


    static StructArray<JavaStruct> structArrayToCopySource = new StructArray<>(20000, JavaStruct::new);
    static StructArray<JavaStruct> structArrayToCopyTarget = new StructArray<>(20000, JavaStruct::new);

    @Benchmark
    public void iterateStruct(Blackhole hole) {
        structArray.forEach((JavaStruct struct) -> {
            hole.consume(struct);
            return Unit.INSTANCE;
        });
    }

    @Benchmark
    public void iterateVanilla(Blackhole hole) {
        vanillaArrayList.forEach((JavaVanilla nonStruct) -> hole.consume(nonStruct));
    }

    @Benchmark
    public void iterateAndMutateStruct(Blackhole hole) {
        mutableStructArray.forEach((JavaMutableStruct struct) -> {
            struct.setA(struct.getA() + 1);
            hole.consume(struct);
            return Unit.INSTANCE;
        });
    }

    @Benchmark
    public void iterateAndMutateVanilla(Blackhole hole) {
        mutableVanillaArrayList.forEach((JavaMutableVanilla nonStruct) -> {
            nonStruct.setA(nonStruct.getA() + 1);
            hole.consume(nonStruct);
        });
    }

    @Benchmark
    public void copyStructArray(Blackhole hole) {
        copyTo(structArrayToCopySource, structArrayToCopyTarget, true);
    }
}
