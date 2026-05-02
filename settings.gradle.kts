pluginManagement {
    repositories {
        maven { url = uri("https://maven.muon.rip/releases") }
        gradlePluginPortal()
        mavenLocal()
    }
}

plugins {
    id("com.possible-triangle.helper") version ("99.0")
}

include("common", "fabric", "neoforge", "loom-deobf")