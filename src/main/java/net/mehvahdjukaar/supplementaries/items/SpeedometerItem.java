package net.mehvahdjukaar.supplementaries.items;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class SpeedometerItem extends Item {

    public SpeedometerItem(Properties properties) {
        super(properties);
    }

    private static double roundToSignificantFigures(double num, int n) {
        if(num == 0) {
            return 0;
        }

        final double d = Math.ceil(Math.log10(num < 0 ? -num: num));
        final int power = n - (int) d;

        final double magnitude = Math.pow(10, power);
        final long shifted = Math.round(num*magnitude);
        return shifted/magnitude;
    }


    @Override
    public ActionResultType interactLivingEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
        if(player.level.isClientSide){
            calculateSpeed(player,entity);
        }
        return ActionResultType.sidedSuccess(player.level.isClientSide);
    }

    private void calculateSpeed(PlayerEntity player, Entity entity){
        double speed = getBPS(entity);
        double s = roundToSignificantFigures(speed,3);
        player.displayClientMessage(new TranslationTextComponent("message.supplementaries.speedometer",s), true);
    }

    private static double getBPS(Entity entity){
        Entity mount = entity.getVehicle();
        Entity e = entity;
        if(mount!=null)e = mount;
        Vector3d v = e.getDeltaMovement();
        if(e.isOnGround()) v = v.subtract(0,v.y,0);
        return v.length()*20;
    }


    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if(world.isClientSide){
            calculateSpeed(player,player);
        }
        return ActionResult.success(player.getItemInHand(hand));
    }

    public static class SpeedometerItemProperty implements IItemPropertyGetter{
        @Override
        public float call(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity player) {
            Entity entity = player != null ? player : stack.getEntityRepresentation();
            if (entity == null) {
                return 0.0F;
            } else {
                double speed = getBPS(entity);
                double max = 60;
                return (float) Math.min((speed / max),1);
            }
        }
    }
}

