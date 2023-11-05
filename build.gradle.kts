buildscript {
    repositories {
        mavenCentral()
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
    }
}
