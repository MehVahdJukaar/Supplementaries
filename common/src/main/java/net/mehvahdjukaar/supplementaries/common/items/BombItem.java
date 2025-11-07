package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.ProjectileStats;
import net.mehvahdjukaar.supplementaries.common.entities.BombEntity;
import net.mehvahdjukaar.supplementaries.common.utils.VibeChecker;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;

public class BombItem extends Item implements ProjectileItem {
    private final BombEntity.BombType type;
    private final boolean glint;

    public BombItem(Properties builder) {
        this(builder, BombEntity.BombType.NORMAL, false);
    }

    public BombItem(Properties builder, BombEntity.BombType type, boolean glint) {
        super(builder);
        this.type = type;
        this.glint = glint;
    }

    public BombEntity.BombType getType() {
        return type;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return !stack.has(DataComponents.CUSTOM_MODEL_DATA) && glint;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand handIn) {
        VibeChecker.assertSameLevel(level, player);

        ItemStack itemstack = player.getItemInHand(handIn);

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.random.nextFloat() * 0.4F + 0.8F));
        if (CommonConfigs.Tools.BOMB_COOLDOWN.get()) {
            player.getCooldowns().addCooldown(this, 30);
        }
        if (!level.isClientSide) {
            BombEntity bombEntity = new BombEntity(level, player, type);
            float pitch = -10;//player.isSneaking()?0:-20;
            bombEntity.shootFromRotation(player, player.getXRot(), player.getYRot(),
                    pitch, bombEntity.getDefaultShootVelocity(), 1);
            level.addFreshEntity(bombEntity);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.getAbilities().instabuild) {
            itemstack.shrink(1);

        }

        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }


    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        var bomb = new BombEntity(level, pos.x(), pos.y(), pos.z(), type);
        ItemStack s= stack.copy();
        s.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(1));
        bomb.setItem(stack);
        return bomb;
    }

    @Override
    public DispenseConfig createDispenseConfig() {
        return DispenseConfig.builder()
                .power(ProjectileStats.BOMB_DISPENSER_SPEED)
                .uncertainty(ProjectileStats.BOMB_DISPENSER_INACCURACY)
                .build();
    }
}

