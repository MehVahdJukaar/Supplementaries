package net.mehvahdjukaar.supplementaries.entities;

import net.mehvahdjukaar.supplementaries.block.blocks.RopeBlock;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

public class RopeArrowEntity extends AbstractArrowEntity {
    private int charges = 0;
    private BlockPos prevPlacedPos = null;
    public RopeArrowEntity(EntityType<? extends RopeArrowEntity> type, World world) {
        super(type, world);
    }

    public RopeArrowEntity(World worldIn, LivingEntity throwerIn, int charges) {
        super(Registry.ROPE_ARROW.get(), throwerIn, worldIn);
        this.charges = charges;
    }

    public RopeArrowEntity(World worldIn, double x, double y, double z, int charges) {
        super(Registry.ROPE_ARROW.get(), x, y, z, worldIn);
        this.charges = charges;
    }

    public RopeArrowEntity(World worldIn, double x, double y, double z) {
        super(Registry.ROPE_ARROW.get(), x, y, z, worldIn);
    }

    public RopeArrowEntity(FMLPlayMessages.SpawnEntity packet, World world) {
        super(Registry.ROPE_ARROW.get(), world);
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Ropes",this.charges);
        if(this.prevPlacedPos!=null) {
            compound.put("PrevPlacedPos",NBTUtil.writeBlockPos(this.prevPlacedPos));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT compound) {
        super.readAdditionalSaveData(compound);
        this.charges = compound.getInt("Ropes");
        if(compound.contains("PrevPlacedPos")) {
            this.prevPlacedPos = NBTUtil.readBlockPos(compound.getCompound("PrevPlacedPos"));
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        if(this.charges!=0) {
            ItemStack stack = new ItemStack(Registry.ROPE_ARROW_ITEM.get());
            stack.setDamageValue(stack.getMaxDamage() - this.charges);
            return stack;
        }
        return new ItemStack(Items.ARROW);
    }

    //TODO: add sound
    //on block hit.
    @Override
    protected void onHitBlock(BlockRayTraceResult rayTraceResult) {
        super.onHitBlock(rayTraceResult);

        Block ropeBlock = ServerConfigs.cached.ROPE_ARROW_BLOCK;

        if (this.charges<=0)return;
        if (!this.level.isClientSide) {
            this.prevPlacedPos = null;
            Entity entity = this.getOwner();
            PlayerEntity player = null;
            if (!(entity instanceof MobEntity) || this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) || net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level, this.getEntity())) {
                if(entity instanceof PlayerEntity && ((PlayerEntity) entity).mayBuild()){
                    //TODO: i might just give null here since player isn't actually placing these blocks
                    player = (PlayerEntity) entity;
                }
                //Ugly but works
                //try finding existing ropes
                BlockPos hitPos = rayTraceResult.getBlockPos();
                Block hitBlock = this.level.getBlockState(hitPos).getBlock();
                //try adding rope down
                if(hitBlock == ropeBlock && RopeBlock.addRope(hitPos, level, player, Hand.MAIN_HAND, ropeBlock)){
                    this.prevPlacedPos = hitPos;
                    this.removeCharge();
                    return;
                }
                hitPos = hitPos.relative(rayTraceResult.getDirection());
                hitBlock = this.level.getBlockState(hitPos).getBlock();
                //rope to the side
                if(hitBlock == ropeBlock && RopeBlock.addRope(hitPos, level, player, Hand.MAIN_HAND, ropeBlock)){
                    this.prevPlacedPos = hitPos;
                    this.removeCharge();
                    return;
                }

                //try placing it normally
                ItemStack ropes = new ItemStack(ropeBlock);
                BlockItemUseContext context = new BlockItemUseContext((PlayerEntity) entity, Hand.MAIN_HAND,ropes,rayTraceResult);
                if (context.canPlace()) {
                    BlockState state =  ropeBlock.getStateForPlacement(context);
                    if(state!=null && CommonUtil.canPlace(context, state)) {
                        this.level.setBlock(context.getClickedPos(),state, 11);
                        this.prevPlacedPos = context.getClickedPos();
                        this.removeCharge();
                    }
                }
            }
        }
    }

    private void removeCharge(){
        this.charges = Math.max(0,this.charges -1);
        this.level.playSound(null, this.prevPlacedPos,SoundEvents.LEASH_KNOT_PLACE,SoundCategory.BLOCKS,0.2f,1.7f);
    }

    private void continueUnwindingRope(){
        Block ropeBlock = ServerConfigs.cached.ROPE_ARROW_BLOCK;
        //no need to do other checks since this only happens after a onBlockCollision()
        PlayerEntity player = null;
        Entity entity = this.getOwner();
        if(entity instanceof PlayerEntity && ((PlayerEntity) entity).mayBuild()){
            player = (PlayerEntity) entity;
        }
        BlockPos hitPos = this.prevPlacedPos;
        Block hitBlock = this.level.getBlockState(hitPos).getBlock();
        //try adding rope down
        if(hitBlock == ropeBlock && RopeBlock.addRope(hitPos.below(), level, player, Hand.MAIN_HAND, ropeBlock)){
            this.prevPlacedPos = hitPos.below();
            this.removeCharge();
        }
        else {
            this.prevPlacedPos = null;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level.isClientSide) {
            if (this.charges != 0 && this.prevPlacedPos != null) {
                this.continueUnwindingRope();
            }
        }

    }
}
