plugins {
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

    implementation("com.github.bawnorton.mixinsquared:mixinsquared-common:${mixin_squared_version}")
    annotationProcessor("com.github.bawnorton.mixinsquared:mixinsquared-common:${mixin_squared_version}")

    implementation("com.github.bawnorton.mixinsquared:mixinsquared-forge:${mixin_squared_version}")

    modCompileOnly("net.mehvahdjukaar:moonlight-neoforge:${moonlight_version}")
    accessTransformers("net.mehvahdjukaar:moonlight-neoforge:${project.extra["moonlight_version"]}")

    modCompileOnly("curse.maven:irisshaders-455508:5726473")
    modCompileOnly("curse.maven:cave-enhancements-597562:4388535")
    //implementation fileTree(dir: 'mods', include: '*.jar')
    modCompileOnly("com.lowdragmc.shimmer:Shimmer-common:1.19.2-0.1.14")
    //modCompileOnly("curse.maven:roughly-enough-items-310111:4357860")
    modCompileOnly("curse.maven:jei-238222:5846878")
    modCompileOnly("curse.maven:roughly-enough-items-310111:5731643")
    modCompileOnly("curse.maven:emi-580555:6361996")

    modCompileOnly("curse.maven:farmers-respite-551453:4081312")
    modCompileOnly("curse.maven:flan-404578:5290167")
    modCompileOnly("dev.onyxstudios.cardinal-components-api:cardinal-components-base:${cca_version}")
    modCompileOnly("dev.onyxstudios.cardinal-components-api:cardinal-components-entity:${cca_version}")
    modCompileOnly("dev.emi:trinkets:${trinkets_version}")
    modCompileOnly("curse.maven:curios-309927:4581099")
    modCompileOnly("curse.maven:jade-324717:5884237")
    modCompileOnly("curse.maven:quark-243121:7640331")
    modCompileOnly("curse.maven:zeta-968868:7640154")

    modImplementation("curse.maven:exposure-871755:7033927")

    modCompileOnly("dev.engine-room.flywheel:flywheel-neoforge-${flywheel_forge_version}")
    modCompileOnly("dev.engine-room.vanillin:vanillin-neoforge-${vanillin_version}")
    // modCompileOnly("net.createmod.ponder:Ponder-NeoForge-${ponder_version}")

    modCompileOnly("curse.maven:farmers-delight-398521:5772720")

    modCompileOnly("curse.maven:soul-fire-d-662413:6248773")
    modCompileOnly("curse.maven:entity-model-features-844662:6001148")

    modCompileOnly("maven.modrinth:immediatelyfast:1.6.1+1.21.1-neoforge")

    //    modCompileOnly("curse.maven:immediatelyfast-686911:5894662")
    //modCompileOnly("maven.modrinth:immediatelyfast:1.2.8+1.20.4-forge")
    modCompileOnly("maven.modrinth:wilder-wild:2.4.5-mc1.20.1")
    modCompileOnly("curse.maven:buzzier-bees-355458:4776328")
    modCompileOnly("maven.modrinth:frozenlib:1.7.4-mc1.20.1")
    modCompileOnly("curse.maven:the-twilight-forest-227639:7398100")

    modCompileOnly("net.mehvahdjukaar:amendments-fabric:1.21-2.0.9")

    modCompileOnly("com.misterpemodder:shulkerboxtooltip-common:${shulker_box_tooltip_version}")
    modCompileOnly("curse.maven:blueprint-382216:5292242")
    modCompileOnly("curse.maven:environmental-388992:6060255")
}
