import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.1")
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
    kotlinOptions.freeCompilerArgs = listOf("-Xno-call-assertions", "-Xno-param-assertions")
}
java {
    withJavadocJar()
    withSourcesJar()
}
