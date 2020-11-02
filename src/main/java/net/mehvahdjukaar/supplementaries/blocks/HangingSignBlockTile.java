package net.mehvahdjukaar.supplementaries.blocks;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
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

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.stream.IntStream;


public class HangingSignBlockTile extends LockableLootTileEntity implements ITickableTileEntity, ISidedInventory {

    public static final int MAXLINES = 5;

    private NonNullList<ItemStack> stacks = NonNullList.<ItemStack>withSize(1, ItemStack.EMPTY);
    public float angle = 0;
    public float prevAngle = 0;
    public int counter = 800;
    //lower counter is used by hitting animation
    public final ITextComponent[] signText = new ITextComponent[]{new StringTextComponent(""), new StringTextComponent(""),
            new StringTextComponent(""), new StringTextComponent(""), new StringTextComponent("")};
    private boolean isEditable = true;
    private PlayerEntity player;
    private final IReorderingProcessor[] renderText = new IReorderingProcessor[MAXLINES];
    private DyeColor textColor = DyeColor.BLACK;
    public HangingSignBlockTile() {
        super(Registry.HANGING_SIGN_TILE.get());
    }

    @Override
    public void markDirty() {
        // this.world.notifyBlockUpdate(this.getPos(), this.getBlockState(),
        // this.getBlockState(), 2);
        super.markDirty();
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        if (!this.checkLootAndRead(compound)) {
            this.stacks = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
        }
        ItemStackHelper.loadAllItems(compound, this.stacks);
        // sign code
        this.isEditable = false;
        this.textColor = DyeColor.byTranslationKey(compound.getString("Color"), DyeColor.BLACK);
        for(int i = 0; i < MAXLINES; ++i) {
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
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        if (!this.checkLootAndWrite(compound)) {
            ItemStackHelper.saveAllItems(compound, this.stacks);
        }
        for (int i = 0; i < MAXLINES; ++i) {
            String s = ITextComponent.Serializer.toJson(this.signText[i]);
            compound.putString("Text" + (i + 1), s);
        }
        compound.putString("Color", this.textColor.getTranslationKey());
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
        ITextComponent itextcomponent = (ITextComponent) (playerIn == null ? new StringTextComponent("Sign") : playerIn.getDisplayName());
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
            this.world.notifyBlockUpdate(this.getPos(), this.getBlockState(),
                    this.getBlockState(), 3);
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
        return new SUpdateTileEntityPacket(this.pos, 9, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.read(this.getBlockState(), pkt.getNbtCompound());
    }

    @Override
    public int getSizeInventory() {
        return stacks.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.stacks)
            if (!itemstack.isEmpty())
                return false;
        return true;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.stacks;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> stacks) {
        this.stacks = stacks;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, @Nullable Direction direction) {
        return false;
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, Direction direction) {
        return false;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return IntStream.range(0, this.getSizeInventory()).toArray();
    }

    @Override
    public ITextComponent getDefaultName() {
        return new StringTextComponent("hanging sing");
    }

    @Override
    public Container createMenu(int id, PlayerInventory player) {
        return ChestContainer.createGeneric9X3(id, player, this);
    }

    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent("Hanging sing");
    }

    public Direction getDirection() {
        return this.getBlockState().get(HangingSignBlock.FACING);
    }

    @Override
    public void tick() {
        if (this.world.isRemote) {
            this.counter++;

            this.prevAngle = this.angle;
            float maxswingangle = 45f;
            float minswingangle = 2.5f;
            float maxperiod = 25f;
            float angleledamping = 150f;
            float perioddamping = 100f;
            //actually tey are the inverse of damping. increase them to fave less damping

            float a = minswingangle;
            float k = 0.01f;
            if(counter<800){
                a = (float) Math.max((float) maxswingangle * Math.pow(Math.E, -(counter / angleledamping)), minswingangle);
                k = (float) Math.max(Math.PI*2*(float)Math.pow(Math.E, -(counter/perioddamping)), 0.01f);
            }

            this.angle = a * MathHelper.cos((counter/maxperiod) - k);
            // this.angle = 90*(float)
            // Math.cos((float)counter/40f)/((float)this.counter/20f);;
        }
    }
}

