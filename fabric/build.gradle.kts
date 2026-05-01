plugins {
    id("com.possible-triangle.fabric")
}


fabric {
    dependOn(project(":common"))
    accessWidener(project(":common"))
}


val moonlight_version: String by extra
val cloth_version: String by extra
val cca_version: String by extra
val mixin_squared_version: String by extra
val flywheel_fabric_version: String by extra
val trinkets_version: String by extra
val emi_version: String by extra
val shulker_box_tooltip_version: String by extra
dependencies {


    modImplementation("net.mehvahdjukaar:moonlight-fabric:${moonlight_version}")

    include("com.github.bawnorton.mixinsquared:mixinsquared-fabric:${mixin_squared_version}")
    implementation("com.github.bawnorton.mixinsquared:mixinsquared-fabric:${mixin_squared_version}")
    annotationProcessor("com.github.bawnorton.mixinsquared:mixinsquared-fabric:${mixin_squared_version}")


    modCompileOnly("com.misterpemodder:shulkerboxtooltip-fabric:${shulker_box_tooltip_version}")
    modCompileOnly("curse.maven:entity-model-features-844662:7998618")
    modCompileOnly("curse.maven:emi-580555:6420930")
    modCompileOnly("curse.maven:jei-238222:7420583")
    modCompileOnly("curse.maven:jade-324717:7545228")
    modCompileOnly("dev.emi:trinkets:${trinkets_version}")



    modImplementation("me.shedaniel.cloth:cloth-config-fabric:${cloth_version}")
    modCompileOnly("curse.maven:yacl-667299:4574163")

    modCompileOnly("curse.maven:modmenu-308702:3920481")


    modCompileOnly("curse.maven:irisshaders-455508:5726473")
    modCompileOnly("curse.maven:the-bumblezone-fabric-363949:5889007")
    modCompileOnly("curse.maven:goated-805646:5935888")
    //modImplementation("curse.maven:inmis-369254:6952335")
    modCompileOnly("maven.modrinth:sodium:mc1.21-0.6.0-beta.2-fabric")
    //modImplementation("curse.maven:resourceful-lib-570073:5793501") //v2.1.29 | Chipped, Handcrafted, Cozy
    //modImplementation("curse.maven:midnightlib-488090:5687799")

    //modImplementation("curse.maven:roughly-enough-items-310111:4440734")
    //modCompileOnly("curse.maven:architectury-419699:4581904")
    modCompileOnly("curse.maven:flan-404578:3902630")
    //modCompileOnly("curse.maven:farmers-delight-fabric-482834:4640640")
    //modCompileOnly("com.jozufozu.flywheel:flywheel-fabric-$flywheel_fabric_version")
    modCompileOnly ("dev.engine-room.flywheel:flywheel-fabric-${flywheel_fabric_version}")
    //modRuntimeOnly "curse.maven:forge-config-api-port-fabric-547434:6798213"


    modCompileOnly("curse.maven:farmers-delight-refabricated-993166:5887378")

    modCompileOnly("curse.maven:decorative-blocks-reborn-1327768:6897415")
    modCompileOnly("curse.maven:cc-tweaked-282001:5714511")

    modCompileOnly("net.mehvahdjukaar:amendments-fabric:1.21-2.0.9")

    modCompileOnly("curse.maven:jei-fabric-waystones-410902:4950461")
    modCompileOnly("curse.maven:jei-fabric-waystones-owo-lib-532610:4749199")
    modCompileOnly("curse.maven:owo-lib-532610:6297839")
    modCompileOnly("dev.onyxstudios.cardinal-components-api:cardinal-components-base:${cca_version}")
    modCompileOnly("dev.onyxstudios.cardinal-components-api:cardinal-components-entity:${cca_version}")
    //modImplementation("curse.maven:biome-makeover-412182:4572458")
    //modImplementation("curse.maven:twigs-496913:3943447")
    //modImplementation("curse.maven:frame-api-580137:3943404")
    modCompileOnly("dev.emi:emi:${emi_version}+1.19.4:api")


    //modImplementation("curse.maven:cyanide-541676:4824161")
    modCompileOnly("maven.modrinth:wilder-wild:2.4.5-mc1.20.1")
    modCompileOnly("maven.modrinth:frozenlib:1.7.4-mc1.20.1")

    modCompileOnly("curse.maven:immediatelyfast-686911:6285506")
    //modImplementation("curse.maven:spectrum-556967:5080474")
    //  modCompileOnly("curse.maven:create-328085:4835191")

    modCompileOnly("org.embeddedt:embeddium-fabric-1.20.1:0.3.1-git.b3f920f+mc1.20.1")

//    modImplementation("maven.modrinth:immediatelyfast:1.2.0+1.20.1") // Get latest version from releases

}
