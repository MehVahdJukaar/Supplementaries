package net.mehvahdjukaar.supplementaries.integration.create;

/*
import com.simibubi.create.content.schematics.ISpecialBlockItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement;
import com.simibubi.create.content.schematics.SchematicWorld;
import net.mehvahdjukaar.supplementaries.common.block.blocks.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

public class SchematicCannonStuff {

    private interface ICopyDropItemRequirement extends ISpecialBlockItemRequirement {
        @Override
        default ItemRequirement getRequiredItems(BlockState state, BlockEntity te) {
            if (te.getLevel() instanceof SchematicWorld serverLevel) {
                LootContext.Builder builder = new LootContext.Builder(serverLevel.getLevel());
                builder = builder.withOptionalParameter(LootContextParams.BLOCK_ENTITY, te)
                        .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                        .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(te.getBlockPos()));

                return new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, state.getDrops(builder));
            }
            return ItemRequirement.INVALID;
        }
    }

    public static WallLanternBlock makeWallLantern(BlockBehaviour.Properties properties) {

        class WallLanternBlockC extends WallLanternBlock implements ICopyDropItemRequirement {

            public WallLanternBlockC(BlockBehaviour.Properties properties) {
                super(properties);
            }
        }
        return new WallLanternBlockC(properties);
    }

    public static StickBlock makeStick(BlockBehaviour.Properties properties) {
        return makeSticks(properties, 60);
    }

    public static StickBlock makeSticks(BlockBehaviour.Properties properties, int fireSpread) {

        class StickBlockC extends StickBlock implements ICopyDropItemRequirement {

            public StickBlockC(Properties properties, int fireSpread ) {
                super(properties, fireSpread);
            }
        }
        return new StickBlockC(properties, fireSpread);
    }

    public static HangingFlowerPotBlock makeFlowerPot(BlockBehaviour.Properties p) {
        class HangingFlowerPotBlockC extends HangingFlowerPotBlock implements ICopyDropItemRequirement {

            public HangingFlowerPotBlockC(Properties properties) {
                super(properties);
            }
        }
        //return new HangingFlowerPotBlockC(p);
        //schematic cannon removes inventory content...
        return new HangingFlowerPotBlock(p);
    }

    public static FlowerBoxBlock makeFlowerBox(BlockBehaviour.Properties p) {
        class FlowerBoxBlockC extends FlowerBoxBlock implements ICopyDropItemRequirement {

            public FlowerBoxBlockC(Properties properties) {
                super(properties);
            }
        }
        //return new FlowerBoxBlockC(p);
        return new FlowerBoxBlock(p);
    }

    public static DoubleSkullBlock makeDoubleSkull(BlockBehaviour.Properties p) {
        class DoubleSkullBlockC extends DoubleSkullBlock implements ICopyDropItemRequirement {

            public DoubleSkullBlockC(Properties properties) {
                super(properties);
            }
        }
        return new DoubleSkullBlockC(p);
    }

    public static CandleSkullBlock makeCandleSkull(BlockBehaviour.Properties p) {
        class CandleSkullBlockC extends CandleSkullBlock implements ICopyDropItemRequirement {

            public CandleSkullBlockC(Properties properties) {
                super(properties);
            }
        }
        return new CandleSkullBlockC(p);
    }

    public static SignPostBlock makeSignPost(BlockBehaviour.Properties p) {
        class SignPostBlockC extends SignPostBlock implements ICopyDropItemRequirement {

            public SignPostBlockC(Properties properties) {
                super(properties);
            }
        }
        return new SignPostBlockC(p);
    }

    public static BookPileBlock makeBookPile(BlockBehaviour.Properties p) {
        class BookPileBlockC extends BookPileBlock implements ICopyDropItemRequirement {

            public BookPileBlockC(Properties properties) {
                super(properties);
            }
        }
        return new BookPileBlockC(p);
    }

    public static FrameBlock makeFramedBlock(BlockBehaviour.Properties p, Supplier<Block> daub) {
        class FrameBlockC extends FrameBlock implements ICopyDropItemRequirement {

            public FrameBlockC(Properties properties, Supplier<Block> daub) {
                super(properties, daub);
            }
        }
        //return new FrameBlockC(p, daub);
        return new FrameBlock(p, daub);
    }

    public static FrameBraceBlock makeFrameBraceBlock(BlockBehaviour.Properties p, Supplier<Block> daub) {
        class FrameBraceBlockC extends FrameBraceBlock implements ICopyDropItemRequirement {

            public FrameBraceBlockC(Properties properties, Supplier<Block> daub) {
                super(properties, daub);
            }
        }
        //return new FrameBraceBlockC(p, daub);
        return new FrameBraceBlock(p, daub);
    }

    public static AshLayerBlock makeAshPile(BlockBehaviour.Properties p) {
        class AshLayerBlockC extends AshLayerBlock implements ICopyDropItemRequirement {

            public AshLayerBlockC(Properties properties) {
                super(properties);
            }
        }
        return new AshLayerBlock(p);
    }

    public static DoubleCakeBlock makeDoubleCake(BlockBehaviour.Properties p) {
        class DoubleCakeBlockC extends DoubleCakeBlock implements ISpecialBlockItemRequirement {

            public DoubleCakeBlockC(Properties properties) {
                super(properties);
            }

            @Override
            public ItemRequirement getRequiredItems(BlockState state, BlockEntity te) {
                return new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, new ItemStack(Items.CAKE, 2));
            }
        }
        return new DoubleCakeBlockC(p);
    }
    //TODO: add more once create decently supports this for normal blocks too
}
*/
