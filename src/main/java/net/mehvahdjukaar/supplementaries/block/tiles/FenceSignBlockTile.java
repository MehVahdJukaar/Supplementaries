package net.mehvahdjukaar.supplementaries.block.tiles;


import net.mehvahdjukaar.supplementaries.block.util.IBlockHolder;
import net.mehvahdjukaar.supplementaries.block.util.ITextHolder;
import net.mehvahdjukaar.supplementaries.block.util.TextHolder;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;


public class FenceSignBlockTile extends BlockEntity implements ITextHolder, IBlockHolder {

    public TextHolder textHolder;

    public BlockState fenceBlock = Blocks.AIR.defaultBlockState();
    public Direction signFacing = Direction.NORTH;
    public BlockState signBlock = Blocks.AIR.defaultBlockState();

    public static final int LINES = 4;

    public FenceSignBlockTile() {
        super(ModRegistry.SIGN_POST_TILE.get());
        this.textHolder = new TextHolder(LINES);
    }

    @Override
    public BlockState getHeldBlock(int index) {
        return this.fenceBlock;
    }

    @Override
    public boolean setHeldBlock(BlockState state, int index) {
        this.fenceBlock = state;
        return true;
    }

    @Override
    public TextHolder getTextHolder(){ return this.textHolder; }

    @Override
    public AABB getRenderBoundingBox(){
        return new AABB(this.getBlockPos().offset(-0.25,0,-0.25), this.getBlockPos().offset(1.25,1,1.25));
    }

    @Override
    public void load(BlockState state, CompoundTag compound) {
        super.load(state, compound);

        this.textHolder.read(compound);
        this.fenceBlock = NbtUtils.readBlockState(compound.getCompound("Fence"));
        this.signBlock = NbtUtils.readBlockState(compound.getCompound("Sign"));
        this.signFacing = Direction.from3DDataValue(compound.getInt("Facing"));

    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        super.save(compound);

        this.textHolder.write(compound);
        compound.put("Fence", NbtUtils.writeBlockState(fenceBlock));
        compound.put("Sign", NbtUtils.writeBlockState(signBlock));
        compound.putInt("Facing", this.signFacing.get3DDataValue());

        return compound;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 0, this.getUpdateTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.save(new CompoundTag());
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(this.getBlockState(), pkt.getTag());
    }
}