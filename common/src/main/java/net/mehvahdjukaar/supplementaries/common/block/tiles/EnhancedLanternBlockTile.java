package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.supplementaries.common.block.blocks.WallLanternBlock;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class EnhancedLanternBlockTile extends SwayingBlockTile{

    protected double attachmentOffset = 0;

    public EnhancedLanternBlockTile(BlockPos pos, BlockState state) {
        this(ModRegistry.WALL_LANTERN_TILE.get(), pos, state);
    }

    public EnhancedLanternBlockTile(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
    }

    public double getAttachmentOffset() {
        return attachmentOffset;
    }

    static {
        maxSwingAngle = 45f;
        minSwingAngle = 1.9f;
        maxPeriod = 28f;
        angleDamping = 80f;
        periodDamping = 70f;
    }

    @Override
    public Vec3i getNormalRotationAxis(BlockState state) {
        return state.getValue(WallLanternBlock.FACING).getClockWise().getNormal();
    }
}
