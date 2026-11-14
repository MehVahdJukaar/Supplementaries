import com.possible_triangle.gradle.settings.HelperExtension
import com.possible_triangle.gradle.settings.ResolutionStrategy

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenLocal()
    }
}

plugins {
    id("com.possible-triangle.helper") version ("1.4")
}

configure<HelperExtension> {
    versionStrategy.set(ResolutionStrategy.NONE)
}

include("common", "fabric", "neoforge")