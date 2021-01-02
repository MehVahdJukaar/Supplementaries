package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.supplementaries.blocks.JarBlock;
import net.mehvahdjukaar.supplementaries.blocks.tiles.JarBlockTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class JarItem extends BlockItem {
    public JarItem(Block blockIn, Properties properties) {
        super(blockIn, properties);
    }


    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        CompoundNBT compoundnbt = stack.getChildTag("BlockEntityTag");
        if (compoundnbt != null) {
            if (compoundnbt.contains("LootTable", 8)) {
                tooltip.add(new StringTextComponent("???????").mergeStyle(TextFormatting.GRAY));
            }

            if (compoundnbt.contains("Items", 9)) {
                NonNullList<ItemStack> nonnulllist = NonNullList.withSize(27, ItemStack.EMPTY);
                ItemStackHelper.loadAllItems(compoundnbt, nonnulllist);
                int i = 0;
                int j = 0;

                for(ItemStack itemstack : nonnulllist) {
                    if (!itemstack.isEmpty()) {
                        ++j;
                        if (i <= 4) {
                            ++i;
                            IFormattableTextComponent iformattabletextcomponent = itemstack.getDisplayName().deepCopy();

                            String s = iformattabletextcomponent.getString();
                            s = s.replace(" Bucket", "");
                            s = s.replace(" Bottle", "");
                            s = s.replace("Bucket of ", "");
                            IFormattableTextComponent str = new StringTextComponent(s);

                            str.appendString(" x").appendString(String.valueOf(itemstack.getCount()));
                            tooltip.add(str.mergeStyle(TextFormatting.GRAY));
                        }
                    }
                }
                if (j - i > 0) {
                    tooltip.add((new TranslationTextComponent("container.shulkerBox.more", j - i)).mergeStyle(TextFormatting.ITALIC).mergeStyle(TextFormatting.GRAY));
                }
            }
        }
        //mob jar
        CompoundNBT com = stack.getChildTag("CachedJarMobValues");
        if (com != null){
            if(com.contains("Name")){
                tooltip.add(new StringTextComponent(com.getString("Name")).mergeStyle(TextFormatting.GRAY));
            }
        }
    }


    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        ItemStack stack = context.getItem();
        CompoundNBT com = stack.getTag();
        if(!context.getPlayer().isSneaking() && com!=null && com.contains("JarMob")){
            World world = context.getWorld();
            Entity entity  = EntityType.loadEntityAndExecute(com.getCompound("JarMob"), world, o -> o);
            if(entity!=null) {
                if(!world.isRemote) {
                    Vector3d v = context.getHitVec();
                    entity.setPositionAndRotation(v.getX(), v.getY(), v.getZ(), context.getPlacementYaw(), 0);

                    UUID temp = entity.getUniqueID();
                    if(com.contains("CachedJarMobValues")){
                        CompoundNBT c = com.getCompound("CachedJarMobValues");
                        if(c.contains("oldID")) {
                            UUID id = c.getUniqueId("oldID");
                            entity.setUniqueId(id);
                        }
                    }
                    if(!world.addEntity(entity)){
                        //spawn failed, reverting to old UUID
                        entity.setUniqueId(temp);
                        world.addEntity(entity);
                    }

                }
                if(!context.getPlayer().isCreative()) {
                   ItemStack returnItem = new ItemStack(((BlockItem)stack.getItem()).getBlock().asItem());
                   if(stack.hasDisplayName())returnItem.setDisplayName(stack.getDisplayName());
                   context.getPlayer().setHeldItem(context.getHand(), returnItem);
                }

            }
            return ActionResultType.SUCCESS;

        }
        return super.onItemUse(context);
    }

    @Override
    public ActionResultType tryPlace(BlockItemUseContext context) {
        ActionResultType placeresult = super.tryPlace(context);
        if(placeresult.isSuccessOrConsume()) {
            World world = context.getWorld();
            BlockPos pos = context.getPos();
            TileEntity te = world.getTileEntity(pos);
            if(te instanceof JarBlockTile){
                JarBlockTile mobjar = ((JarBlockTile)te);
                CompoundNBT compound = context.getItem().getTag();
                if(compound!=null&&compound.contains("JarMob")&&compound.contains("CachedJarMobValues")) {
                    CompoundNBT com2 = compound.getCompound("CachedJarMobValues");
                    CompoundNBT com = compound.getCompound("JarMob");

                    mobjar.entityData = com;
                    mobjar.yOffset = com2.getFloat("YOffset");
                    mobjar.scale = com2.getFloat("Scale");
                    if(com2.contains("oldID")) {
                        mobjar.uuid = com2.getUniqueId("oldID");
                    }
                    if (!world.isRemote){
                        int light = 0;
                        if(com.getString("id").equals("minecraft:endermite"))light = 5;
                        else if(com.getString("id").equals("iceandfire:pixie"))light = 10;
                        if(light!=0) {
                            BlockState state = world.getBlockState(pos);
                            world.setBlockState(pos, state.with(JarBlock.LIGHT_LEVEL, light));
                        }

                    }
                    mobjar.markDirty();
                    //mobjar.updateMob();

                }
            }
        }
        return placeresult;
    }

}
