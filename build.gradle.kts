buildscript {

    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.0")
    }
}

allprojects {
    group = "de.hanno.structs"

    apply(plugin = "maven-publish")
}
subprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        maven {
            setUrl("https://dl.bintray.com/kotlin/kotlinx")
        }
    }
}
