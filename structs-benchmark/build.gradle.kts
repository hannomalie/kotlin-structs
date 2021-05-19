import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("application")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.openjdk.jmh:jmh-core:1.21")
    implementation("org.openjdk.jmh:jmh-generator-annprocess:1.21")
    implementation("org.spf4j:spf4j-jmh:8.5.15")

    implementation(project(":structs"))

    testImplementation("junit:junit:4.12")
}

application.mainClass.set("de.hanno.struct.benchmark.StructBenchmark")

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs = listOf("-Xno-call-assertions", "-Xno-param-assertions")
}
