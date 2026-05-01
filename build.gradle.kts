import org.apache.commons.io.output.ByteArrayOutputStream
import org.gradle.internal.extensions.core.serviceOf
import java.nio.charset.Charset

plugins {
    id("com.possible-triangle.core")
    id("com.possible-triangle.common") apply false
    id("com.possible-triangle.fabric") apply false
    id("com.possible-triangle.neoforge") apply false
    id("net.mehvahdjukaar.candlelight") version "1.1.6" apply false
    id("dev.mixinmcp.decompile") version "0.9.0" apply false

}

mod {
    val mod_description: String by extra
    val mod_credits: String by extra
    val mod_license: String by extra
    val mod_homepage: String by extra
    val mod_github: String by extra
    val moonlight_min_version: String by extra
    additional.add("mod_description", provider { mod_description })
    additional.add("mod_credits", provider { mod_credits })
    additional.add("mod_license", provider { mod_license })
    additional.add("mod_homepage", provider { mod_homepage })
    additional.add("mod_github", provider { mod_github })
    additional.add("moonlight_min_version", provider { moonlight_min_version })
}


subprojects {

    apply(plugin = "com.possible-triangle.core")
    apply(plugin = "net.mehvahdjukaar.candlelight")
    apply(plugin = "dev.mixinmcp.decompile")
    apply(plugin = "maven-publish")

    repositories {
        nexus()
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.addAll(listOf("-Xmaxerrs", "4000"))
    }

    /*

    upload {
        maven {
            nexus()
        }
        curseforge {
            dependencies {
            }
        }
        modrinth {
            dependencies {

            }
        }

        forEach {
            changelog = rootProject.file("changelog.md").readText()
            versionName = "${mod.id.get()}-${mod.version.get()}-${project.name}"
        }
    }

     */

    repositories {
        // Standard repositories
        mavenLocal()
        mavenCentral()

        flatDir {
            dirs("mods")
        }

        maven { url = uri("https://jitpack.io") }

        maven { url = uri("https://maven.neoforged.net/releases") }
        maven { url = uri("https://maven.architectury.dev") }
        maven { url = uri("https://maven.parchmentmc.org") }
        maven { url = uri("https://maven.neoforged.net") }

        maven { url = uri("https://maven.createmod.net") } // Create Mod, Ponder, Flywheel
        maven { url = uri("https://maven.blamejared.com") } // JEI, Vazkii's Mods
        maven { url = uri("https://maven.ladysnake.org/releases") } // Ladysnake mods
        maven { url = uri("https://maven.tterrag.com/") } // Flywheel, EnderIO
        maven { url = uri("https://mvn.devos.one/releases/") } // Registrate, Porting Lib (releases)
        maven { url = uri("https://mvn.devos.one/snapshots/") } // Registrate, Porting Lib (snapshots)
        maven { url = uri("https://maven.terraformersmc.com/") } // TerraformersMC mods
        maven { url = uri("https://maven.saps.dev/releases") } // FTB Mods
        maven { url = uri("https://dl.cloudsmith.io/public/tslat/sbl/maven/") }
        maven { url = uri("https://maven.theillusivec4.top/") } // Curios API
        maven { url = uri("https://maven.squiddev.cc") } // CC: Tweaked
        maven { url = uri("https://maven.su5ed.dev/releases") } // SU5ED mods
        maven { url = uri("https://harleyoconnor.com/maven") } // Dynamic Trees
        maven { url = uri("https://maven.misterpemodder.com/libs-release/") } // ShulkerBoxTooltip
        maven { url = uri("https://maven.firstdarkdev.xyz/snapshots") } // FirstDarkDev (snapshots)
        maven { url = uri("https://raw.githubusercontent.com/Fuzss/modresources/main/maven") } // Fuzss' Mod Resources
        maven { url = uri("https://maven.jamieswhiteshirt.com/libs-release") } // Jamie's Mods
        maven { url = uri("https://maven.ryanhcode.dev/releases") }
    }
}



tasks.register("buildAndPublishAll") {
    group = "build"
    description = "Runs clean, build, publish for all projects"

    dependsOn(subprojects.map { it.tasks.named("clean") })
    dependsOn(subprojects.map { it.tasks.named("build") })
    dependsOn(subprojects.map { it.tasks.named("publish") })

    finalizedBy("gitTag")
}

tasks.register("gitTag") {
    group = "build"
    doLast {
        val execOps = serviceOf<ExecOperations>() // Fetches the service
        val tag = project.version.toString()
        val stdout = ByteArrayOutputStream()

        execOps.exec {
            commandLine("git", "tag", "-l", tag)
            standardOutput = stdout
        }

        if (!stdout.toString(Charset.defaultCharset()).trim().isEmpty()) {
            logger.warn("Git tag '${tag}' already exists")
        } else {
            execOps.exec {
                commandLine("git", "tag", "-a", tag, "-m", "Release $tag")
            }
            execOps.exec {
                commandLine("git", "push", "origin", tag)
            }
        }
    }
}