package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.block.IOwnerProtected;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.supplementaries.client.screens.HangingSignGui;
import net.mehvahdjukaar.supplementaries.common.block.blocks.HangingSignBlock;
import net.mehvahdjukaar.supplementaries.common.block.IMapDisplay;
import net.mehvahdjukaar.supplementaries.common.block.ITextHolderProvider;
import net.mehvahdjukaar.supplementaries.common.block.TextHolder;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;


public class HangingSignBlockTile extends SwayingBlockTile implements IMapDisplay, ITextHolderProvider, IOwnerProtected {
    public static final int MAX_LINES = 7;

    public final WoodType woodType;

    private boolean fakeItem = true;
    private UUID owner = null;

    private TextHolder textHolder;
    private NonNullList<ItemStack> stacks = NonNullList.withSize(1, ItemStack.EMPTY);

    static {
        maxSwingAngle = 45f;
        minSwingAngle = 2.5f;
        maxPeriod = 25f;
        angleDamping = 150f;
        periodDamping = 100f;
    }

    public HangingSignBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.HANGING_SIGN_TILE.get(), pos, state);
        this.textHolder = new TextHolder(MAX_LINES, 75);

        if (this.getBlockState().getBlock() instanceof HangingSignBlock block) {
            this.woodType = block.woodType;
        } else this.woodType = WoodTypeRegistry.OAK_TYPE;
    }

    public boolean hasFakeItem() {
        return fakeItem;
    }

    public void setFakeItem(boolean fakeItem) {
        this.fakeItem = fakeItem;
    }

    @Override
    public boolean isAlwaysFast() {
        return ClientConfigs.Blocks.FAST_SIGNS.get();
    }

    @Override
    public boolean isFlipped() {
        return this.getBlockState().getValue(HangingSignBlock.AXIS) != Direction.Axis.Z;
    }

    @Nullable
    @Override
    public UUID getOwner() {
        return owner;
    }

    @Override
    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    @Override
    public TextHolder getTextHolder() {
        return this.textHolder;
    }

    @Override
    public void openScreen(Level level, BlockPos pos, Player player) {
        HangingSignGui.open(this);
    }

    @Override
    public ItemStack getMapStack() {
        return this.getItem();
    }

    @Override
    public void setChanged() {
        if (this.level == null) return;
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        super.setChanged();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.stacks = NonNullList.withSize(1, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.stacks);

        this.textHolder.load(tag);
        this.loadOwner(tag);
        if(tag.contains("FakeItem")) {
            this.fakeItem = tag.getBoolean("FakeItem");
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, this.stacks);

        this.textHolder.save(tag);
        this.saveOwner(tag);
        if(fakeItem){
            tag.putBoolean("FakeItem", true);
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public boolean isEmpty() {
        for (ItemStack itemstack : this.stacks)
            if (!itemstack.isEmpty())
                return false;
        return true;
    }

    public ItemStack removeItem() {
        return ContainerHelper.takeItem(this.stacks, 0);
    }

    public void setItem(ItemStack item) {
       this.stacks = NonNullList.withSize(1, item);
    }

    public ItemStack getItem() {
        return this.stacks.get(0);
    }

    @Override
    public Vec3i getNormalRotationAxis(BlockState state) {
        return state.getValue(HangingSignBlock.AXIS) == Direction.Axis.X ? new Vec3i(0, 0, -1) : new Vec3i(1, 0, 0);
    }

}

