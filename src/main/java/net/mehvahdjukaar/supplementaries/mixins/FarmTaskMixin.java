package net.mehvahdjukaar.supplementaries.mixins;

import net.minecraft.entity.ai.brain.task.FarmTask;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({FarmTask.class})
public abstract class FarmTaskMixin {
    /*
    @Shadow
    private BlockPos aboveFarmlandPos;
    @Shadow
    private long nextOkStartTime;
    @Shadow
    private List<BlockPos> validFarmlandAroundVillager;
    @Shadow
    private int timeWorkedSoFar;

    @Shadow
    protected abstract BlockPos getValidFarmland(ServerWorld p_212833_1_);

    /*
    @Inject(method = {"tick"},
            at = {@At(value = "INVOKE",
                    target = "Lnet/minecraft/inventory/Inventory;getContainerSize()I"
    )}, cancellable = true)
    private void isValidPosition(ServerWorld serverWorld, VillagerEntity villagerEntity, long time, CallbackInfo ci) {
        if(state.getValue(LanternBlock.HANGING) && RopeBlock.isSupportingCeiling(pos.above(),worldIn))
            callbackInfoReturnable.setReturnValue(true);
    }



    @Overwrite
    protected void tick(ServerWorld world, VillagerEntity villager, long p_212833_3_) {
        if (this.aboveFarmlandPos == null || this.aboveFarmlandPos.closerThan(villager.position(), 1.0D)) {
            if (this.aboveFarmlandPos != null && p_212833_3_ > this.nextOkStartTime) {
                BlockState blockstate = world.getBlockState(this.aboveFarmlandPos);
                Block block = blockstate.getBlock();
                Block block1 = world.getBlockState(this.aboveFarmlandPos.below()).getBlock();

                Item toReplace = Items.AIR;

                if (block instanceof CropsBlock && ((CropsBlock)block).isMaxAge(blockstate)) {
                    toReplace = block.asItem();
                    world.destroyBlock(this.aboveFarmlandPos, true, villager);
                }

                if (world.getBlockState(this.aboveFarmlandPos).isAir() && block1 instanceof FarmlandBlock) {
                    Inventory inventory = villager.getInventory();


                    ItemStack itemStack = ItemStack.EMPTY;
                    boolean canPlant = false;
                    int ind = 0;
                    if(toReplace!=Items.AIR && toReplace instanceof BlockItem) {
                        for (int i = 0; i < inventory.getContainerSize(); ++i) {
                            itemStack = inventory.getItem(i);
                            if (itemStack.getItem() == toReplace) {
                                canPlant = true;
                                ind = i;
                                break;
                            }
                        }
                    }


                    if(!canPlant) {
                        for (int i = 0; i < inventory.getContainerSize(); ++i) {
                            itemStack = inventory.getItem(i);
                            Item it = itemStack.getItem();
                            if (!itemStack.isEmpty() && it instanceof BlockItem) {
                                Block b = ((BlockItem) it).getBlock();
                                if (b instanceof IPlantable) {
                                    if (((IPlantable) b).getPlantType(world, aboveFarmlandPos) == PlantType.CROP) {
                                        canPlant = true;
                                        ind = i;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if (canPlant) {
                        world.setBlock(aboveFarmlandPos, ((IPlantable)((BlockItem)itemStack.getItem()).getBlock()).getPlant(world, aboveFarmlandPos), 3);

                        world.playSound(null, this.aboveFarmlandPos.getX(), this.aboveFarmlandPos.getY(), this.aboveFarmlandPos.getZ(), SoundEvents.CROP_PLANTED, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        itemStack.shrink(1);
                        if (itemStack.isEmpty()) {
                            inventory.setItem(ind, ItemStack.EMPTY);
                        }
                    }

                }

                if (block instanceof CropsBlock && !((CropsBlock)block).isMaxAge(blockstate)) {
                    this.validFarmlandAroundVillager.remove(this.aboveFarmlandPos);
                    this.aboveFarmlandPos = this.getValidFarmland(world);
                    if (this.aboveFarmlandPos != null) {
                        this.nextOkStartTime = p_212833_3_ + 20L;
                        villager.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosWrapper(this.aboveFarmlandPos), 0.5F, 1));
                        villager.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosWrapper(this.aboveFarmlandPos));
                    }
                }
            }

            ++this.timeWorkedSoFar;
        }
    }

    */

}

