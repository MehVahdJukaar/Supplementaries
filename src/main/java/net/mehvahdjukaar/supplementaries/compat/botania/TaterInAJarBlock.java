package net.mehvahdjukaar.supplementaries.compat.botania;

import net.mehvahdjukaar.selene.util.Utils;
import net.mehvahdjukaar.supplementaries.block.blocks.JarBlock;
import net.mehvahdjukaar.supplementaries.items.JarItem;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.block.decor.BlockTinyPotato;
import vazkii.botania.common.block.tile.TileTinyPotato;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class TaterInAJarBlock extends BlockTinyPotato {
    private static final VoxelShape SHAPE = JarBlock.SHAPE;

    public TaterInAJarBlock(Properties builder) {
        super(builder);
    }

    @NotNull
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext ctx) {
        return SHAPE;
    }

    @Override
    public BlockState rotate(BlockState state, IWorld world, BlockPos pos, Rotation direction) {
        return state.setValue(BlockStateProperties.HORIZONTAL_FACING, direction.rotate(state.getValue(BlockStateProperties.FACING)));
    }

    @Nonnull
    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nonnull
    public TileEntity newBlockEntity(@Nonnull IBlockReader world) {
        return new TaterInAJarBlockTile();
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TaterInAJarBlockTile) {
            ((TaterInAJarBlockTile) tile).interact(player, hand, player.getItemInHand(hand), hit.getDirection());
            if (!world.isClientSide) {
                AxisAlignedBB box = SHAPE.bounds();
                ((ServerWorld) world).sendParticles(ParticleTypes.ANGRY_VILLAGER, (double) pos.getX() + box.minX + Math.random() * (box.maxX - box.minX), (double) pos.getY() + box.maxY - 1, (double) pos.getZ() + box.minZ + Math.random() * (box.maxZ - box.minZ), 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }

        return ActionResultType.SUCCESS;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        ItemStack stack = new ItemStack(this);
        TileEntity tile = builder.getOptionalParameter(LootParameters.BLOCK_ENTITY);
        if (tile instanceof TileTinyPotato) {
            TileTinyPotato te = ((TileTinyPotato) tile);

            if (te.hasCustomName())
                stack.setHoverName(te.getCustomName());
        }
        return Collections.singletonList(stack);
    }

    @NotNull
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext ctx) {
        World level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        PlayerEntity player = ctx.getPlayer();
        if (!player.isShiftKeyDown()) {
            FluidState fluidState = level.getFluidState(pos);
            JarItem i = ModRegistry.JAR_ITEM.get();
            // i.playReleaseSound( level, ctx.getClickLocation());
            if (!level.isClientSide) {
                Utils.swapItemNBT(player, ctx.getHand(), ctx.getItemInHand(), new ItemStack(i));
            }
            BlockState state = ModBlocks.tinyPotato.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, fluidState.getType() == Fluids.WATER);
            return state.setValue(BlockStateProperties.HORIZONTAL_FACING, ctx.getHorizontalDirection().getOpposite());
        }
        return super.getStateForPlacement(ctx);
    }
}
