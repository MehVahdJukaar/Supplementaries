package net.mehvahdjukaar.supplementaries.datagen.types;

import java.util.HashMap;
import java.util.Map;

public class WoodTypes {
    //nbt translation map
    public static final Map<String,IWoodType> TYPES = new HashMap<>();

    static {
        for (IWoodType w : VanillaWoodTypes.values()){
            TYPES.put(w.toString(),w);
        }
        for (IWoodType w : AtmosphericWoodTypes.values()){
            TYPES.put(w.toString(),w);
        }
        for (IWoodType w : AutumnityWoodTypes.values()){
            TYPES.put(w.toString(),w);
        }
        for (IWoodType w : EndergeticWoodTypes.values()){
            TYPES.put(w.toString(),w);
        }
        for (IWoodType w : UpgradeAquaticWoodTypes.values()){
            TYPES.put(w.toString(),w);
        }
        for (IWoodType w : EnhancedMushroomsWoodTypes.values()){
            //TYPES.put(w.toString(),w);
        }
    }
    public static IWoodType fromString(String s){
        return TYPES.getOrDefault(s, VanillaWoodTypes.OAK);
    }


}
