package net.mehvahdjukaar.supplementaries.datagen.types;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class WoodTypes {
    public static final Set<IWoodType> TYPES = new HashSet<>();
    static {
        TYPES.addAll(Arrays.stream(VanillaWoodTypes.values()).collect(Collectors.toSet()));
    }
}
