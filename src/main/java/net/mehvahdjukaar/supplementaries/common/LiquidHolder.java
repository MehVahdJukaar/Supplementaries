package net.mehvahdjukaar.supplementaries.common;

import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeColors;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LiquidHolder {
    private World world;
    private BlockPos pos;

    public int color = 0xffffff;
    public float liquidLevel = 0;
    public CommonUtil.JarLiquidType liquidType = CommonUtil.JarLiquidType.EMPTY;

    public LiquidHolder(World world, BlockPos pos){
        this.world = world;
        this.pos = pos;
    }

    public void setWorldAndPos(World world, BlockPos pos){
        this.world = world;
        this.pos = pos;
    }

    public void read(CompoundNBT compound) {
        if(compound.contains("LiquidHolder")) {
            CompoundNBT cmp = compound.getCompound("LiquidHolder");
            this.liquidLevel = cmp.getFloat("Level");
            this.color = cmp.getInt("Color");
            this.liquidType = CommonUtil.JarLiquidType.values()[cmp.getInt("Type")];
        }
    }

    public CompoundNBT write(CompoundNBT compound) {
        if(this.liquidType!= CommonUtil.JarLiquidType.EMPTY) {
            CompoundNBT cmp = new CompoundNBT();
            cmp.putInt("Color", this.color);
            cmp.putFloat("Level", this.liquidLevel);
            cmp.putInt("Type", this.liquidType.ordinal());

            compound.put("LiquidHolder", cmp);
        }
        return compound;
    }

    @OnlyIn(Dist.CLIENT)
    public int updateClientWaterColor(){
        this.color = BiomeColors.getWaterColor(this.world, this.pos);
        return this.color;
    }

    public void updateLiquid(ItemStack stack) {

        this.liquidType = CommonUtil.getJarContentTypeFromItem(stack);
        //level
        if(this.liquidType.isFish()){
            this.liquidLevel = 0.625f;
        }
        else{
            this.liquidLevel = ((float) stack.getCount()/ (float) ServerConfigs.cached.JAR_CAPACITY)*0.75f;
        }
        //color
        if(this.liquidType.isWater()){
            this.color=-1;//let client get biome color on next rendering. ugly i know but that class is client side
        }
        else if(this.liquidType == CommonUtil.JarLiquidType.POTION){
            this.color = PotionUtils.getColor(stack);
        }
        else{
            this.color = this.liquidType.color;
        }
        //lava light
        if(!this.world.isRemote){
            int light = this.liquidType.getLightLevel();
            BlockState state = this.world.getBlockState(this.pos);
            if(state.get(Resources.LIGHT_LEVEL_0_15)!=light){
                this.world.setBlockState(this.pos, state.with(Resources.LIGHT_LEVEL_0_15,light));
            }
        }

    }

}
