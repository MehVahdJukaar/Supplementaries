package net.mehvahdjukaar.supplementaries.blocks.tiles;


import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.mehvahdjukaar.supplementaries.common.CommonUtil.WoodType;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BannerBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.FilledMapItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.function.Function;


public class SignPostBlockTile extends TileEntity {
    public final ITextComponent[] signText = new ITextComponent[]{new StringTextComponent(""), new StringTextComponent("")};
    private boolean isEditable = true;
    private PlayerEntity player;
    //private final String[] renderText = new String[2];
    private final IReorderingProcessor[] renderText = new IReorderingProcessor[2];
    private DyeColor textColor = DyeColor.BLACK;

    public BlockState fenceBlock = Blocks.OAK_FENCE.getDefaultState();
    public float yawUp = 0;
    public float yawDown = 0;
    public boolean leftUp = true;
    public boolean leftDown = false;
    public boolean up = false;
    public boolean down = false;
    public WoodType woodTypeUp = WoodType.OAK;
    public WoodType woodTypeDown = WoodType.OAK;

    public SignPostBlockTile() {
        super(Registry.SIGN_POST_TILE);
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return 128;
    }

    @Override
    public void markDirty() {
        this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
        super.markDirty();
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox(){
        return new AxisAlignedBB(this.getPos().add(-0.25,0,-0.25), this.getPos().add(1.25,1,1.25));
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        // sign code
        this.isEditable = false;
        this.textColor = DyeColor.byTranslationKey(compound.getString("Color"), DyeColor.BLACK);

        for(int i = 0; i < 2; ++i) {
            String s = compound.getString("Text" + (i + 1));
            ITextComponent itextcomponent = ITextComponent.Serializer.getComponentFromJson(s.isEmpty() ? "\"\"" : s);
            if (this.world instanceof ServerWorld) {
                try {
                    this.signText[i] = TextComponentUtils.func_240645_a_(this.getCommandSource(null), itextcomponent, null, 0);
                } catch (CommandSyntaxException commandsyntaxexception) {
                    this.signText[i] = itextcomponent;
                }
            } else {
                this.signText[i] = itextcomponent;
            }

            this.renderText[i] = null;
        }

        this.fenceBlock = NBTUtil.readBlockState(compound.getCompound("Fence"));
        this.yawUp = compound.getFloat("Yaw_up");
        this.yawDown = compound.getFloat("Yaw_down");
        this.leftUp = compound.getBoolean("Left_up");
        this.leftDown = compound.getBoolean("Left_down");
        this.up = compound.getBoolean("Up");
        this.down = compound.getBoolean("Down");
        this.woodTypeUp = WoodType.values()[compound.getInt("Wood_type_up")];
        this.woodTypeDown = WoodType.values()[compound.getInt("Wood_type_down")];

    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);

        for (int i = 0; i < 2; ++i) {
            String s = ITextComponent.Serializer.toJson(this.signText[i]);
            compound.putString("Text" + (i + 1), s);
        }

        compound.putString("Color", this.textColor.getTranslationKey());
        compound.put("Fence", NBTUtil.writeBlockState(fenceBlock));
        compound.putFloat("Yaw_up",this.yawUp);
        compound.putFloat("Yaw_down",this.yawDown);
        compound.putBoolean("Left_up",this.leftUp);
        compound.putBoolean("Left_down",this.leftDown);
        compound.putBoolean("Up", this.up);
        compound.putBoolean("Down", this.down);
        compound.putInt("Wood_type_up", this.woodTypeUp.ordinal());
        compound.putInt("Wood_type_down", this.woodTypeDown.ordinal());

        return compound;
    }

    // lots of sign code coming up
    @OnlyIn(Dist.CLIENT)
    public ITextComponent getText(int line) {
        return this.signText[line];
    }

    public void setText(int line, ITextComponent p_212365_2_) {
        this.signText[line] = p_212365_2_;
        this.renderText[line] = null;
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public IReorderingProcessor getRenderText(int line, Function<ITextComponent, IReorderingProcessor> p_242686_2_) {
        if (this.renderText[line] == null && this.signText[line] != null) {
            this.renderText[line] = p_242686_2_.apply(this.signText[line]);
        }

        return this.renderText[line];
    }

    public boolean getIsEditable() {
        return this.isEditable;
    }

    /**
     * Sets the sign's isEditable flag to the specified parameter.
     */
    @OnlyIn(Dist.CLIENT)
    public void setEditable(boolean isEditableIn) {
        this.isEditable = isEditableIn;
        if (!isEditableIn) {
            this.player = null;
        }
    }

    public void setPlayer(PlayerEntity playerIn) {
        this.player = playerIn;
    }

    public PlayerEntity getPlayer() {
        return this.player;
    }

    public boolean executeCommand(PlayerEntity playerIn) {
        for (ITextComponent itextcomponent : this.signText) {
            Style style = itextcomponent == null ? null : itextcomponent.getStyle();
            if (style != null && style.getClickEvent() != null) {
                ClickEvent clickevent = style.getClickEvent();
                if (clickevent.getAction() == ClickEvent.Action.RUN_COMMAND) {
                    playerIn.getServer().getCommandManager().handleCommand(this.getCommandSource((ServerPlayerEntity) playerIn),
                            clickevent.getValue());
                }
            }
        }
        return true;
    }

    public CommandSource getCommandSource(@Nullable ServerPlayerEntity playerIn) {
        String s = playerIn == null ? "Sign" : playerIn.getName().getString();
        ITextComponent itextcomponent = playerIn == null ? new StringTextComponent("Sign") : playerIn.getDisplayName();
        return new CommandSource(ICommandSource.DUMMY,
                new Vector3d((double) this.pos.getX() + 0.5D, (double) this.pos.getY() + 0.5D, (double) this.pos.getZ() + 0.5D), Vector2f.ZERO,
                (ServerWorld) this.world, 2, s, itextcomponent, this.world.getServer(), playerIn);
    }

    public DyeColor getTextColor() {
        return this.textColor;
    }

    public boolean setTextColor(DyeColor newColor) {
        if (newColor != this.getTextColor()) {
            this.textColor = newColor;
            this.markDirty();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onlyOpsCanSetNbt() {
        return true;
    }

    // end of sign code

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.read(this.getBlockState(), pkt.getNbtCompound());
    }
}