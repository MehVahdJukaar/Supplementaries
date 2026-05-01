plugins {
    id("com.possible-triangle.core")
    id("com.possible-triangle.common")
}

common {
    accessWidener()
}

val mixin_squared_version: String by extra
val moonlight_version: String by extra
val trinkets_version: String by extra
val cca_version: String by extra
val flywheel_forge_version: String by extra
val vanillin_version: String by extra
val shulker_box_tooltip_version: String by extra

dependencies {
    compileOnly("net.mehvahdjukaar:candlelight:1.1.6")

    compileOnly(project(":loom-deobf"))
    compileOnly(
        project(
            mapOf(
                "path" to ":loom-deobf",
                "configuration" to "modRuntimeClasspathMainMapped"
            )
        )
    )

    implementation("com.github.bawnorton.mixinsquared:mixinsquared-common:${mixin_squared_version}")
    annotationProcessor("com.github.bawnorton.mixinsquared:mixinsquared-common:${mixin_squared_version}")

    implementation("com.github.bawnorton.mixinsquared:mixinsquared-forge:${mixin_squared_version}")

    modCompileOnly("net.mehvahdjukaar:moonlight-neoforge:${moonlight_version}")
    accessTransformers("net.mehvahdjukaar:moonlight-neoforge:${moonlight_version}")


    modCompileOnly("curse.maven:farmers-delight-398521:8007613")
    modCompileOnly("com.misterpemodder:shulkerboxtooltip-neoforge:${shulker_box_tooltip_version}")
    modCompileOnly("net.mehvahdjukaar:amendments-neoforge:1.21-2.0.9")
    modCompileOnly("curse.maven:entity-model-features-844662:7998618")
    modCompileOnly("curse.maven:emi-580555:6420931")
    modImplementation("curse.maven:jei-238222:7420587")
    modCompileOnly("curse.maven:jade-324717:7545219")
    modCompileOnly("curse.maven:roughly-enough-items-310111:6199140")
    modCompileOnly("curse.maven:the-twilight-forest-227639:7797302")
    modCompileOnly("curse.maven:environmental-388992:7122147")



    modCompileOnly("curse.maven:irisshaders-455508:5726473")
    modCompileOnly("curse.maven:cave-enhancements-597562:4388535")
    //implementation fileTree(dir: 'mods', include: '*.jar')

    modCompileOnly("curse.maven:farmers-respite-551453:4081312")
    modCompileOnly("dev.onyxstudios.cardinal-components-api:cardinal-components-base:${cca_version}")
    modCompileOnly("dev.onyxstudios.cardinal-components-api:cardinal-components-entity:${cca_version}")
    modCompileOnly("curse.maven:curios-309927:4581099")
    modCompileOnly("curse.maven:quark-243121:7640331")
    modCompileOnly("curse.maven:zeta-968868:7640154")

    modImplementation("curse.maven:exposure-871755:7033927")

    modCompileOnly("dev.engine-room.flywheel:flywheel-neoforge-${flywheel_forge_version}")
    modCompileOnly("dev.engine-room.vanillin:vanillin-neoforge-${vanillin_version}")
    // modCompileOnly("net.createmod.ponder:Ponder-NeoForge-${ponder_version}")

    modCompileOnly("curse.maven:soul-fire-d-662413:6248773")

    modCompileOnly("maven.modrinth:immediatelyfast:1.6.1+1.21.1-neoforge")

    //    modCompileOnly("curse.maven:immediatelyfast-686911:5894662")
    //modCompileOnly("maven.modrinth:immediatelyfast:1.2.8+1.20.4-forge")

    modCompileOnly("curse.maven:buzzier-bees-355458:4776328")
    modCompileOnly("curse.maven:the-twilight-forest-227639:7398100")


    modCompileOnly("curse.maven:blueprint-382216:5292242")
}
