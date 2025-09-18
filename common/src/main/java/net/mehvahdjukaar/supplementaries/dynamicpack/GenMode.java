package net.mehvahdjukaar.supplementaries.dynamicpack;

import net.mehvahdjukaar.moonlight.api.resources.pack.PackGenerationStrategy;

public enum GenMode {
    ALWAYS,
    CACHED,
    CACHED_ZIPPED,
    NEVER;

    public PackGenerationStrategy toStrategy() {
        return switch (this) {
            case ALWAYS -> PackGenerationStrategy.REGEN_ON_EVERY_RELOAD;
            case CACHED -> PackGenerationStrategy.CACHED;
            case CACHED_ZIPPED -> PackGenerationStrategy.CACHED_ZIPPED;
            case NEVER -> PackGenerationStrategy.NO_OP;
        };
    }
}
