package net.mehvahdjukaar.supplementaries.entities;

/*
public class BombPaintingEntity extends HangingEntity {
    public BombPaintingEntity(EntityType<? extends HangingEntity> type, World world) {
        super(type, world);
    }

    public BombPaintingEntity(World p_i45849_1_, BlockPos p_i45849_2_, Direction direction) {
        super(EntityType.PAINTING, p_i45849_1_, p_i45849_2_);

        this.setDirection(direction);
    }

    public BombPaintingEntity(World worldIn, double x, double y, double z) {
        super(Registry.ROPE_ARROW.get(), x, y, z, worldIn);
    }

    public BombPaintingEntity(FMLPlayMessages.SpawnEntity packet, World world) {
        super(Registry.ROPE_ARROW.get(), world);
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public int getWidth() {
        return 2;
    }

    @Override
    public int getHeight() {
        return 2;
    }

    @Override
    public void dropItem(@Nullable Entity entity) {
        if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            this.playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);
            if (entity instanceof PlayerEntity) {
                PlayerEntity playerentity = (PlayerEntity)entity;
                if (playerentity.abilities.instabuild) {
                    return;
                }
            }

            this.spawnAtLocation(Items.PAINTING);
        }
    }

    @Override
    public void playPlacementSound() {
        this.playSound(SoundEvents.PAINTING_PLACE, 1.0F, 1.0F);
    }

    @Override
    public void moveTo(double p_70012_1_, double p_70012_3_, double p_70012_5_, float p_70012_7_, float p_70012_8_) {
        this.setPos(p_70012_1_, p_70012_3_, p_70012_5_);
    }

    @OnlyIn(Dist.CLIENT)
    public void lerpTo(double p_180426_1_, double p_180426_3_, double p_180426_5_, float p_180426_7_, float p_180426_8_, int p_180426_9_, boolean p_180426_10_) {
        BlockPos blockpos = this.pos.offset(p_180426_1_ - this.getX(), p_180426_3_ - this.getY(), p_180426_5_ - this.getZ());
        this.setPos((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());
    }
}
*/