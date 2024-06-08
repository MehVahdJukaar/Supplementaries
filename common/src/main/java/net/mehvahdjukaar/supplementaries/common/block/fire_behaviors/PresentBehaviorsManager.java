package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

import net.mehvahdjukaar.supplementaries.common.block.blocks.TrappedPresentBlock;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.TntBlock;

//TODO: add eggs and snowballs
public class PresentBehaviorsManager {

    public static void registerBehaviors() {
        TntBehavior tntBehavior = new TntBehavior();
        SpawnEggBehavior spawnEggBehavior = new SpawnEggBehavior();
        for (Item i : BuiltInRegistries.ITEM) {
            if (i instanceof BlockItem bi && bi.getBlock() instanceof TntBlock) {
                TrappedPresentBlock.registerBehavior(i, tntBehavior);
            }
            if (i instanceof SpawnEggItem sp) {
                TrappedPresentBlock.registerBehavior(sp, spawnEggBehavior);
            }
        }

        TrappedPresentBlock.registerBehavior(Items.FIREWORK_ROCKET, new FireworkBehavior());
        TrappedPresentBlock.registerBehavior(Items.SPLASH_POTION, SPLASH_POTION_BEHAVIOR);
        TrappedPresentBlock.registerBehavior(Items.LINGERING_POTION, SPLASH_POTION_BEHAVIOR);


        TrappedPresentBlock.registerBehavior(ModRegistry.BOMB_ITEM.get(), BOMB_BEHAVIOR);
        TrappedPresentBlock.registerBehavior(ModRegistry.BOMB_ITEM_ON.get(), BOMB_BEHAVIOR);
        TrappedPresentBlock.registerBehavior(ModRegistry.BOMB_BLUE_ITEM.get(), BOMB_BEHAVIOR);
        TrappedPresentBlock.registerBehavior(ModRegistry.BOMB_BLUE_ITEM_ON.get(), BOMB_BEHAVIOR);
        TrappedPresentBlock.registerBehavior(ModRegistry.BOMB_SPIKY_ITEM.get(), BOMB_BEHAVIOR);
        TrappedPresentBlock.registerBehavior(ModRegistry.BOMB_SPIKY_ITEM_ON.get(), BOMB_BEHAVIOR);

        TrappedPresentBlock.registerBehavior(ModRegistry.HAT_STAND.get(), new SkibidiBehavior());

        var nuke = CompatObjects.NUKE_BLOCK.get();
        if (nuke != null) {
            TrappedPresentBlock.registerBehavior(nuke, tntBehavior);
        }
    }

}



