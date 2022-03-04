package net.mehvahdjukaar.supplementaries.common.entities;

import net.mehvahdjukaar.supplementaries.common.items.SackItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

public class FallingSackEntity extends FallingBlockEntity {

    public FallingSackEntity(EntityType<? extends FallingBlockEntity> type, Level level) {
        super(type, level);
        this.blockState = p_31957_;
        this.blocksBuilding = true;
        this.setPos(p_31954_, p_31955_, p_31956_);
        this.setDeltaMovement(Vec3.ZERO);
        this.xo = p_31954_;
        this.yo = p_31955_;
        this.zo = p_31956_;
        this.setStartPos(this.blockPosition());
    }

    @Override
    public ItemEntity spawnAtLocation(ItemLike itemIn, int offset) {
        ItemStack stack = new ItemStack(itemIn);
        if (itemIn instanceof Block) {
            stack.addTagElement("BlockEntityTag", this.blockData);
        }
        return this.spawnAtLocation(stack, (float) offset);
    }


}
