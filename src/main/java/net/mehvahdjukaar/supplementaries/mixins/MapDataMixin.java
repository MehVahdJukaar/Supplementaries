package net.mehvahdjukaar.supplementaries.mixins;

import com.google.common.collect.Maps;
import net.mehvahdjukaar.supplementaries.block.util.MapPost;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;
import net.minecraft.world.storage.WorldSavedData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

@Mixin(MapData.class)
public abstract class MapDataMixin extends WorldSavedData {
    public MapDataMixin(String name) {
        super(name);
        mapDecorations= Maps.newLinkedHashMap();
    }

    @Shadow
    @Final
    private void updateDecorations(MapDecoration.Type type, @Nullable IWorld worldIn, String decorationName, double worldX, double worldZ, double rotationIn, @Nullable ITextComponent name){};

    @Final
    private final Map<String, MapPost> posts = Maps.newHashMap();

    @Shadow
    @Final
    public int xCenter;

    @Shadow
    @Final
    public int zCenter;

    @Shadow
    @Final
    public byte scale;

    @Shadow
    @Final
    public final Map<String, MapDecoration> mapDecorations;
    //TODO: it says this should fe final but as you can see it clearly is. if you know a solution tell me



    //hijacking to access it outside
    @Inject(method = "tryAddBanner", at = @At("HEAD"), cancellable = true)
    public void tryAddBanner(IWorld world, BlockPos pos, CallbackInfo info) {
        double d0 = (double)pos.getX() + 0.5D;
        double d1 = (double)pos.getZ() + 0.5D;
        int i = 1 << this.scale;
        double d2 = (d0 - (double)this.xCenter) / (double)i;
        double d3 = (d1 - (double)this.zCenter) / (double)i;
        if (d2 >= -63.0D && d3 >= -63.0D && d2 <= 63.0D && d3 <= 63.0D) {
            MapPost mappost = MapPost.fromWorld(world, pos);
            if (mappost == null) {
                return;
            }
            //add or remove banner
            if (this.posts.containsKey(mappost.getId()) && this.posts.get(mappost.getId()).equals(mappost)) {
                this.posts.remove(mappost.getId());
                this.mapDecorations.remove(mappost.getId());
            }
            else{
                this.posts.put(mappost.getId(), mappost);
                this.updateDecorations(MapDecoration.Type.TARGET_POINT, world, mappost.getId(), d0, d1, 180.0D, mappost.name);
            }

            this.markDirty();

        }
    }

    //removes posts too
    @Inject(method = "removeStaleBanners", at = @At("TAIL"), cancellable = true)
    public void removeStaleBanners(IBlockReader reader, int x, int z, CallbackInfo info) {
        Iterator<MapPost> iterator = this.posts.values().iterator();

        while(iterator.hasNext()) {
            MapPost mampost = iterator.next();
            if (mampost.pos.getX() == x && mampost.pos.getZ() == z) {
                MapPost post2 = MapPost.fromWorld(reader, mampost.pos);
                if (post2==null) {
                    iterator.remove();
                    this.mapDecorations.remove(mampost.getId());
                }
                else if(!Objects.equals(post2.name,mampost.name)){
                    //update name
                    MapDecoration p = this.mapDecorations.get(mampost.getId());
                    p = new MapDecoration(p.getType(),p.getX(),p.getY(),p.getRotation(),post2.name);
                    iterator.remove();
                    this.mapDecorations.remove(mampost.getId());
                    this.mapDecorations.put(mampost.getId(),p);
                    this.posts.remove(mampost.getId());
                    MapPost p2 = new MapPost(mampost.pos,post2.name);
                    this.posts.put(mampost.getId(),p2);

                }
            }
        }
    }

    @Inject(method = "read", at = @At("TAIL"), cancellable = true)
    public void read(CompoundNBT nbt, CallbackInfo info) {
        ListNBT listnbt2 = nbt.getList("posts", 10);

        for(int j = 0; j < listnbt2.size(); ++j) {
            MapPost mappost = MapPost.read(listnbt2.getCompound(j));
            this.posts.put(mappost.getId(), mappost);

            this.updateDecorations(MapDecoration.Type.TARGET_POINT, null, mappost.getId(), mappost.pos.getX(), mappost.pos.getZ(), 180, mappost.name);
        }
    }

    @Inject(method = "write", at = @At("TAIL"), cancellable = true)
    public CompoundNBT write(CompoundNBT compound, CallbackInfoReturnable<CompoundNBT> info) {

        ListNBT listnbt2 = new ListNBT();

        for(MapPost mappost : this.posts.values()) {
            listnbt2.add(mappost.write());
        }

        info.getReturnValue().put("posts", listnbt2);
        return compound;
    }

}
