package net.mehvahdjukaar.supplementaries.integration.forge;


import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionMatrices;
import com.simibubi.create.foundation.utility.VecHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.HourGlassBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BambooSpikesBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.HourGlassBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BambooSpikesBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.tiles.HourGlassBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ModDamageSources;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.UnaryOperator;

public class CreateCompatImpl {
    public static void initialize() {

        try {
            AllMovementBehaviours.registerBehaviour(ModRegistry.BAMBOO_SPIKES.get(), new BambooSpikesBehavior());
            AllMovementBehaviours.registerBehaviour(ModRegistry.HOURGLASS.get(), new HourglassBehavior());
        } catch (Exception e) {
            Supplementaries.LOGGER.warn("failed to register supplementaries create behaviors: " + e);
        }
    }

    public static void changeState(MovementContext context, BlockState newState) {
        Map<BlockPos, StructureTemplate.StructureBlockInfo> blocks = context.contraption.getBlocks();
        if (blocks.containsKey(context.localPos)) {
            context.state = newState;
            StructureTemplate.StructureBlockInfo info = blocks.get(context.localPos);
            StructureTemplate.StructureBlockInfo newInfo = new StructureTemplate.StructureBlockInfo(info.pos, newState, info.nbt);
            blocks.replace(context.localPos, newInfo);
        }
    }

    private static class BambooSpikesBehavior implements MovementBehaviour {

        public boolean isSameDir(MovementContext context) {
            return VecHelper.isVecPointingTowards(context.relativeMotion, context.state.getValue(BambooSpikesBlock.FACING));
        }

        @Override
        public boolean renderAsNormalTileEntity() {
            return true;
        }


        //@Override
        //public void visitNewPosition(MovementContext context, BlockPos pos) {
        //    World world = context.world;
        //    BlockState stateVisited = world.getBlockState(pos);

        //     if (!stateVisited.isRedstoneConductor(world, pos))
        //        damageEntities(context, pos, world);
        //}

        @Override
        public void tick(MovementContext context) {
            damageEntities(context);
        }

        public void damageEntities(MovementContext context) {
            Level world = context.world;
            Vec3 pos = context.position;
            DamageSource damageSource = getDamageSource();

            Entities:
            for (Entity entity : world.getEntitiesOfClass(Entity.class,
                    new AABB(pos.add(-0.5, -0.5, -0.5), pos.add(0.5, 0.5, 0.5)))) {
                if (entity instanceof ItemEntity) continue;
                if (entity instanceof AbstractContraptionEntity) continue;
                if (entity instanceof Player player && player.isCreative()) continue;
                if (entity instanceof AbstractMinecart)
                    for (Entity passenger : entity.getIndirectPassengers())
                        if (passenger instanceof AbstractContraptionEntity
                                && ((AbstractContraptionEntity) passenger).getContraption() == context.contraption)
                            continue Entities;
                //attack entities
                if (entity.isAlive() && entity instanceof LivingEntity) {
                    if (!world.isClientSide) {

                        double pow = 5 * Math.pow(context.relativeMotion.length(), 0.4) + 1;
                        float damage = !isSameDir(context) ? 1 :
                                (float) Mth.clamp(pow, 2, 6);
                        entity.hurt(damageSource, damage);
                        this.doTileStuff(context, world, (LivingEntity) entity);
                    }


                }
                //throw entities (i forgot why this is here. maybe its from creates saw)
                if (world.isClientSide == (entity instanceof Player)) {
                    Vec3 motionBoost = context.motion.add(0, context.motion.length() / 4f, 0);
                    int maxBoost = 4;
                    if (motionBoost.length() > maxBoost) {
                        motionBoost = motionBoost.subtract(motionBoost.normalize().scale(motionBoost.length() - maxBoost));
                    }
                    entity.setDeltaMovement(entity.getDeltaMovement().add(motionBoost));
                    entity.hurtMarked = true;
                }
            }
        }

        private static final BambooSpikesBlockTile DUMMY = new BambooSpikesBlockTile(BlockPos.ZERO, ModRegistry.BAMBOO_SPIKES.get().defaultBlockState());

        private void doTileStuff(MovementContext context, @Nonnull Level world, LivingEntity le) {
            CompoundTag com = context.tileData;
            long lastTicked = com.getLong("LastTicked");
            if (!this.isOnCooldown(world, lastTicked)) {
                DUMMY.load(com);
                if (DUMMY.interactWithEntity(le, world)) {
                    changeState(context, context.state.setValue(BambooSpikesBlock.TIPPED, false));
                }
                com = DUMMY.saveWithFullMetadata();
                lastTicked = world.getGameTime();
                com.putLong("LastTicked", lastTicked);
                context.tileData = com;
            }
        }


        public boolean isOnCooldown(Level world, long lastTicked) {
            return world.getGameTime() - lastTicked < 20;
        }

        protected DamageSource getDamageSource() {
            return ModDamageSources.SPIKE_DAMAGE;
        }

    }

    private static class HourglassBehavior implements MovementBehaviour {

        @Override
        public void tick(MovementContext context) {
            UnaryOperator<Vec3> rot = context.rotation;
            BlockState state = context.state;
            Direction dir = state.getValue(HourGlassBlock.FACING);
            var in = dir.getNormal();
            Vec3 v = new Vec3(in.getX(), in.getY(), in.getZ());
            Vec3 v2 = rot.apply(v);
            double dot = v2.dot(new Vec3(0, 1, 0));

            CompoundTag com = context.tileData;

            HourGlassBlockTile.HourGlassSandType sandType = HourGlassBlockTile.HourGlassSandType.values()[com.getInt("SandType")];
            float progress = com.getFloat("Progress");
            float prevProgress = com.getFloat("PrevProgress");


            if (!sandType.isEmpty()) {
                prevProgress = progress;


                //TODO: re do all of this

                if (dot > 0 && progress != 1) {
                    progress = Math.min(progress + sandType.increment, 1f);
                } else if (dot < 0 && progress != 0) {
                    progress = Math.max(progress - sandType.increment, 0f);
                }

            }

            com.remove("Progress");
            com.remove("PrevProgress");
            com.putFloat("Progress", progress);
            com.putFloat("PrevProgress", prevProgress);
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld, ContraptionMatrices matrices, MultiBufferSource buffer) {

            CompoundTag com = context.tileData;
            HourGlassBlockTile.HourGlassSandType sandType = HourGlassBlockTile.HourGlassSandType.values()[com.getInt("SandType")];
            float progress = com.getFloat("Progress");
            float prevProgress = com.getFloat("PrevProgress");
            NonNullList<ItemStack> stacks = NonNullList.withSize(1, ItemStack.EMPTY);
            ContainerHelper.loadAllItems(com, stacks);
            float partialTicks = 1;
            if (sandType.isEmpty()) return;

            Vec3 v = context.position;
            if (v == null) {
                v = new Vec3(0, 0, 0);
            }
            BlockPos pos = new BlockPos(v);

            int light = LevelRenderer.getLightColor(context.world, pos);

            TextureAtlasSprite sprite = sandType.getSprite(stacks.get(0), renderWorld);

            float h = Mth.lerp(partialTicks, prevProgress, progress);
            Direction dir = context.state.getValue(HourGlassBlock.FACING);
            HourGlassBlockTileRenderer.renderSand(matrices.getModelViewProjection(), buffer, light, 0, sprite, h, dir);
        }

    }


}
