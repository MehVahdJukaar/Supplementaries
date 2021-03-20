package net.mehvahdjukaar.supplementaries.entities;

import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class MashlingEntity extends CreatureEntity {

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public MashlingEntity(EntityType<? extends MashlingEntity> type, World worldIn) {
        super(type, worldIn);
        this.setPathPriority(PathNodeType.DAMAGE_CACTUS, 1.0F);
    }

    public static AttributeModifierMap.MutableAttribute setCustomAttributes() {
        return MobEntity.func_233666_p_()
                .createMutableAttribute(Attributes.FOLLOW_RANGE, 48.0D)
                .createMutableAttribute(Attributes.MOVEMENT_SPEED, ServerConfigs.entity.FIREFLY_SPEED.get())
                .createMutableAttribute(Attributes.MAX_HEALTH, 1)
                .createMutableAttribute(Attributes.ARMOR, 0)
                .createMutableAttribute(Attributes.ATTACK_DAMAGE, 0D);
    }

    @Override
    public void tick() {
        super.tick();

        if (inWater)
            stepHeight = 1F;
        else
            stepHeight = 0.6F;

        if (!world.isRemote && world.getDifficulty() == Difficulty.PEACEFUL) {
            remove();
            for (Entity passenger : getRecursivePassengers())
                if (!(passenger instanceof PlayerEntity))
                    passenger.remove();
        }

        this.prevRenderYawOffset = this.prevRotationYaw;
        this.renderYawOffset = this.rotationYaw;
    }


    @Override
    protected void registerGoals() {
        goalSelector.addGoal(5, new WaterAvoidingRandomWalkingGoal(this, 0.2, 0.98F));
    }
    /*
    public static AttributeModifierMap.MutableAttribute prepareAttributes() {
        return MobEntity.func_233666_p_()
                .createMutableAttribute(Attributes.MAX_HEALTH, 8.0D)
                .createMutableAttribute(Attributes.KNOCKBACK_RESISTANCE, 1D);
    }



    @Override
    public EntityClassification getClassification(boolean forSpawnCount) {
        if (isTame)
            return EntityClassification.CREATURE;
        return EntityClassification.MONSTER;
    }

    @Override
    public boolean canDespawn(double distanceToClosestPlayer) {
        return !isTame;
    }

    @Override
    public void checkDespawn() {
        boolean wasAlive = isAlive();
        super.checkDespawn();
        if (!isAlive() && wasAlive)
            for (Entity passenger : getRecursivePassengers())
                if (!(passenger instanceof PlayerEntity))
                    passenger.remove();
    }

    @Override // processInteract
    public ActionResultType func_230254_b_(PlayerEntity player, @Nonnull Hand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if(!stack.isEmpty() && stack.getItem() == Items.NAME_TAG)
            return stack.getItem().itemInteractionForEntity(stack, player, this, hand);
        else
            return super.func_230254_b_(player, hand);
    }


    @Nullable
    @Override
    public ILivingEntityData onInitialSpawn(IServerWorld world, DifficultyInstance difficulty, SpawnReason spawnReason, @Nullable ILivingEntityData data, @Nullable CompoundNBT compound) {
        byte variant;
        if (data instanceof EnumStonelingVariant)
            variant = ((EnumStonelingVariant) data).getIndex();
        else
            variant = (byte) world.getRandom().nextInt(EnumStonelingVariant.values().length);

        dataManager.set(VARIANT, variant);
        dataManager.set(HOLD_ANGLE, world.getRandom().nextFloat() * 90 - 45);

        if(!isTame && !world.isRemote() && world instanceof IForgeWorldServer) {
            if (ModuleLoader.INSTANCE.isModuleEnabled(FrogsModule.class) && rand.nextDouble() < 0.01) {
                FrogEntity frog = new FrogEntity(FrogsModule.frogType, world.getWorld(), 0.25f);
                Vector3d pos = getPositionVec();

                frog.setPosition(pos.x, pos.y, pos.z);
                world.addEntity(frog);
                frog.startRiding(this);
            } else {
                List<ItemStack> items = ((IForgeWorldServer) world).getWorldServer().getServer().getLootTableManager()
                        .getLootTableFromLocation(CARRY_LOOT_TABLE).generate(new LootContext.Builder((ServerWorld) world).build(LootParameterSets.EMPTY));
                if (!items.isEmpty())
                    dataManager.set(CARRYING_ITEM, items.get(0));
            }
        }

        return super.onInitialSpawn(world, difficulty, spawnReason, data, compound);
    }


    @Override
    public boolean isInvulnerableTo(@Nonnull DamageSource source) {
        return source == DamageSource.CACTUS || source.isProjectile() || super.isInvulnerableTo(source);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }


    @Override
    public boolean isNotColliding(IWorldReader worldReader) {
        return worldReader.checkNoEntityCollision(this, VoxelShapes.create(getBoundingBox()));
    }

    @Override
    public double getMountedYOffset() {
        return this.getHeight();
    }

    @Override
    public boolean isPushedByWater() {
        return false;
    }

    @Override
    protected int decreaseAirSupply(int air) {
        return air;
    }

    @Override
    public boolean onLivingFall(float distance, float damageMultiplier) {
        return false;
    }

    @Override
    protected void damageEntity(@Nonnull DamageSource damageSrc, float damageAmount) {
        super.damageEntity(damageSrc, damageAmount);

        if(!isPlayerMade() && damageSrc.getTrueSource() instanceof PlayerEntity) {
            startle();
            for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(this,
                    getBoundingBox().grow(16))) {
                if (entity instanceof StonelingEntity) {
                    StonelingEntity stoneling = (StonelingEntity) entity;
                    if (!stoneling.isPlayerMade() && stoneling.getEntitySenses().canSee(this)) {
                        startle();
                    }
                }
            }
        }
    }

    public boolean isStartled() {
        return waryGoal.isStartled();
    }

    public void startle() {
        waryGoal.startle();
        Set<PrioritizedGoal> entries = Sets.newHashSet(goalSelector.goals);

        for (PrioritizedGoal task : entries)
            if (task.getGoal() instanceof TemptGoal)
                goalSelector.removeGoal(task.getGoal());
    }

    @Override
    protected void dropSpecialItems(DamageSource damage, int looting, boolean wasRecentlyHit) {
        super.dropSpecialItems(damage, looting, wasRecentlyHit);

        ItemStack stack = getCarryingItem();
        if(!stack.isEmpty())
            entityDropItem(stack, 0F);
    }

    public void setPlayerMade(boolean value) {
        isTame = value;
    }

    public ItemStack getCarryingItem() {
        return dataManager.get(CARRYING_ITEM);
    }

    public EnumStonelingVariant getVariant() {
        return EnumStonelingVariant.byIndex(dataManager.get(VARIANT));
    }

    public float getItemAngle() {
        return dataManager.get(HOLD_ANGLE);
    }

    public boolean isPlayerMade() {
        return isTame;
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);

        if(compound.contains(TAG_CARRYING_ITEM, 10)) {
            CompoundNBT itemCmp = compound.getCompound(TAG_CARRYING_ITEM);
            ItemStack stack = ItemStack.read(itemCmp);
            dataManager.set(CARRYING_ITEM, stack);
        }

        dataManager.set(VARIANT, compound.getByte(TAG_VARIANT));
        dataManager.set(HOLD_ANGLE, compound.getFloat(TAG_HOLD_ANGLE));
        setPlayerMade(compound.getBoolean(TAG_PLAYER_MADE));
    }

    @Override
    public boolean canEntityBeSeen(Entity entityIn) {
        Vector3d pos = getPositionVec();
        Vector3d epos = entityIn.getPositionVec();

        Vector3d origin = new Vector3d(pos.x, pos.y + getEyeHeight(), pos.z);
        float otherEyes = entityIn.getEyeHeight();
        for (float height = 0; height <= otherEyes; height += otherEyes / 8) {
            if (this.world.rayTraceBlocks(new RayTraceContext(origin, epos.add(0, height, 0), RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this)).getType() == RayTraceResult.Type.MISS)
                return true;
        }

        return false;
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);

        compound.put(TAG_CARRYING_ITEM, getCarryingItem().serializeNBT());

        compound.putByte(TAG_VARIANT, getVariant().getIndex());
        compound.putFloat(TAG_HOLD_ANGLE, getItemAngle());
        compound.putBoolean(TAG_PLAYER_MADE, isPlayerMade());
    }

    public static boolean spawnPredicate(EntityType<? extends StonelingEntity> type, IServerWorld world, SpawnReason reason, BlockPos pos, Random rand) {
        return pos.getY() <= StonelingsModule.maxYLevel && MiscUtil.validSpawnLight(world, pos, rand) && MiscUtil.validSpawnLocation(type, world, reason, pos);
    }

    @Override
    public boolean canSpawn(@Nonnull IWorld world, SpawnReason reason) {
        BlockState state = world.getBlockState(new BlockPos(getPositionVec()).down());
        if (state.getMaterial() != Material.ROCK)
            return false;

        return StonelingsModule.dimensions.canSpawnHere(world) && super.canSpawn(world, reason);
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return QuarkSounds.ENTITY_STONELING_CRY;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return QuarkSounds.ENTITY_STONELING_DIE;
    }

    @Override
    public int getTalkInterval() {
        return 1200;
    }

    @Override
    public void playAmbientSound() {
        SoundEvent sound = this.getAmbientSound();

        if (sound != null) this.playSound(sound, this.getSoundVolume(), 1f);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (hasCustomName()) {
            String customName = getName().getString();
            if (customName.equalsIgnoreCase("michael stevens") || customName.equalsIgnoreCase("vsauce"))
                return QuarkSounds.ENTITY_STONELING_MICHAEL;
        }

        return null;
    }



    @Override
    public float getBlockPathWeight(BlockPos pos, IWorldReader world) {
        return 0.5F - world.getBrightness(pos);
    }*/
}
