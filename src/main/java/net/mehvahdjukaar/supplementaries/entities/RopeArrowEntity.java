package net.mehvahdjukaar.supplementaries.entities;

import net.mehvahdjukaar.supplementaries.block.blocks.RopeBlock;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
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

    public RopeArrowEntity(World worldIn, double x, double y, double z) {
        super(Registry.ROPE_ARROW.get(), x, y, z, worldIn);
    }

    public RopeArrowEntity(FMLPlayMessages.SpawnEntity packet, World world) {
        super(Registry.ROPE_ARROW.get(), world);
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.putInt("Ropes",this.charges);
        if(this.prevPlacedPos!=null) {
            compound.put("PrevPlacedPos",NBTUtil.writeBlockPos(this.prevPlacedPos));
        }
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        this.charges = compound.getInt("Ropes");
        if(compound.contains("PrevPlacedPos")) {
            this.prevPlacedPos = NBTUtil.readBlockPos(compound.getCompound("PrevPlacedPos"));
        }
    }

    @Override
    protected ItemStack getArrowStack() {
        if(this.charges!=0) {
            ItemStack stack = new ItemStack(Registry.ROPE_ARROW_ITEM.get());
            stack.setDamage(stack.getMaxDamage() - this.charges);
            return stack;
        }
        return new ItemStack(Items.ARROW);
    }

    //TODO: add sound
    //on block hit.
    @Override
    protected void func_230299_a_(BlockRayTraceResult rayTraceResult) {
        super.func_230299_a_(rayTraceResult);
        if (this.charges<=0)return;
        if (!this.world.isRemote) {
            this.prevPlacedPos = null;
            Entity entity = this.func_234616_v_();
            PlayerEntity player = null;
            if (!(entity instanceof MobEntity) || this.world.getGameRules().getBoolean(GameRules.MOB_GRIEFING) || net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this.getEntity())) {
                if(entity instanceof PlayerEntity && ((PlayerEntity) entity).isAllowEdit()){
                    //TODO: i might just give null here since player isn't actually placing these blocks
                    player = (PlayerEntity) entity;
                }
                //Ugly but works
                //try finding existing ropes
                BlockPos hitPos = rayTraceResult.getPos();
                Block hitBlock = this.world.getBlockState(hitPos).getBlock();
                //try adding rope down
                if(hitBlock instanceof RopeBlock && ((RopeBlock)hitBlock).addRope(hitPos, world, player, Hand.MAIN_HAND)){
                    this.prevPlacedPos = hitPos;
                    this.removeCharge();
                    return;
                }
                hitPos = hitPos.offset(rayTraceResult.getFace());
                hitBlock = this.world.getBlockState(hitPos).getBlock();
                //rope to the side
                if(hitBlock instanceof RopeBlock && ((RopeBlock)hitBlock.getBlock()).addRope(hitPos, world, player, Hand.MAIN_HAND)){
                    this.prevPlacedPos = hitPos;
                    this.removeCharge();
                    return;
                }

                //try placing it normally
                ItemStack ropes = new ItemStack(Registry.ROPE_ITEM.get());
                BlockItemUseContext context = new BlockItemUseContext((PlayerEntity) entity, Hand.MAIN_HAND,ropes,rayTraceResult);
                if (context.canPlace()) {
                    BlockState state =  Registry.ROPE.get().getStateForPlacement(context);
                    if(state!=null && CommonUtil.canPlace(context, state)) {
                        this.world.setBlockState(context.getPos(),state, 11);
                        this.prevPlacedPos = context.getPos();
                        this.removeCharge();
                    }
                }
            }
        }
    }

    private void removeCharge(){
        this.charges = Math.max(0,this.charges -1);
        this.world.playSound(null, this.prevPlacedPos,SoundEvents.ENTITY_LEASH_KNOT_PLACE,SoundCategory.BLOCKS,0.2f,1.7f);
    }

    private void continueUnwindingRope(){
        //no need to do other checks since this only happens after a onBlockCollision()
        PlayerEntity player = null;
        Entity entity = this.func_234616_v_();
        if(entity instanceof PlayerEntity && ((PlayerEntity) entity).isAllowEdit()){
            player = (PlayerEntity) entity;
        }
        BlockPos hitPos = this.prevPlacedPos;
        Block hitBlock = this.world.getBlockState(hitPos).getBlock();
        //try adding rope down
        if(hitBlock instanceof RopeBlock && ((RopeBlock)hitBlock).addRope(hitPos.down(), world, player, Hand.MAIN_HAND)){
            this.prevPlacedPos = hitPos.down();
            this.removeCharge();
        }

    }

    @Override
    public void tick() {
        super.tick();
        if (!this.world.isRemote) {
            if (this.charges != 0 && this.prevPlacedPos != null) {
                this.continueUnwindingRope();
            }
        }

    }
}
