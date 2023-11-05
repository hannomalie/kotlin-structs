import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.joml:joml:1.9.3")

    val lwjglVersion = "3.1.2"
    api("org.lwjgl:lwjgl:$lwjglVersion")
    api("org.lwjgl:lwjgl:$lwjglVersion:natives-windows")
    api("org.lwjgl:lwjgl:$lwjglVersion:natives-linux")
    api("org.lwjgl:lwjgl:$lwjglVersion:natives-macos")

    testImplementation("junit:junit:4.12")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs = listOf("-Xno-call-assertions", "-Xno-param-assertions", "-Xcontext-receivers")
}
java {
    withJavadocJar()
    withSourcesJar()
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    languageVersion = "1.8"
}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.ADOPTOPENJDK)
        implementation.set(JvmImplementation.VENDOR_SPECIFIC)
    }
}
tasks.withType<JavaCompile> {
    options.compilerArgs.add("--enable-preview")
}
tasks.withType<Test> {
    jvmArgs!!.add("--enable-preview")
}