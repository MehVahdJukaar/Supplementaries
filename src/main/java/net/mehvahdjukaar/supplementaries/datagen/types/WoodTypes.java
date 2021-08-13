package net.mehvahdjukaar.supplementaries.datagen.types;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class WoodTypes {
    //nbt translation map
    public static final Map<String,IWoodType> TYPES = new HashMap<>();

    static {

        for (IWoodType w : VanillaWoodTypes.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : AtmosphericWoodTypes.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : AutumnityWoodTypes.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : EndergeticWoodTypes.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : UpgradeAquaticWoodTypes.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : EnhancedMushroomsWoodTypes.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : OuterEndWoodTypes.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : BygWoodTypes.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : PokecubeLegendsWoodTypes.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : PokecubeWoodTypes.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : ForbiddenArcanusWoodTypes.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : ExtendedMushroomsWoodTypes.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : DruidcraftWoodTypes.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : BetterEndWoodTypes.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : GreekFantasyWoodTypes.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : GoodNightSleepWoodTypes.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : OmniWoodTypes.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : MysticalWorldWoodTypes.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : SimplyTeadWoodTypes.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : StructurizeWoodTypes.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : RediscoveredWoodTypes.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : ArchitectsPaletteWoodTypes.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : BotaniaWoodTypes.values()){ //#
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : PremiumWoodWoodTypes.values()){ //#
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : SilentGearWoodTypes.values()){ //#
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : TerraqueousWoodTypes.values()){ //#
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : TheBumblezoneWoodTypes.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : TraverseWoodTypes.values()){ //#
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : ToufucraftWoodTypes.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : BayouBluesWoodType.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : AbundanceWoodType.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : EnvironmentalWoodTypes.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : TerraincognitaWoodType.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : BiomesoplentyWoodTypes.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : MowzieMobsWoodTypes.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : TwilightForestWoodTypes.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : UndergardenWoodTypes.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : LotrWoodTypes.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : UnnamedAnimalModWoodType.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : AtumWoodType.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : MalumWoodType.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : HabitatWoodType.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : DesolationWoodType.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : ArsNouveauWoodType.values()){
            TYPES.put(w.toNBT(),w);
        }
        for (IWoodType w : BambooBlocksWoodType.values()){
            TYPES.put(w.toNBT(),w);
        }


    }
    public static IWoodType fromNBT(String s){
        return TYPES.getOrDefault(s,VanillaWoodTypes.OAK);
    }

    public static Collection<IWoodType> all(){
        return TYPES.values();
    }

}
