package net.mehvahdjukaar.supplementaries.world.data.map.markers;

import net.mehvahdjukaar.selene.map.CustomDecoration;
import net.mehvahdjukaar.selene.map.markers.NamedMapWorldMarker;
import net.mehvahdjukaar.supplementaries.block.tiles.SignPostBlockTile;
import net.mehvahdjukaar.supplementaries.world.data.map.CMDreg;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class SignPostMarker extends NamedMapWorldMarker<CustomDecoration> {


    public SignPostMarker() {
        super(CMDreg.SIGN_POST_DECORATION_TYPE);
    }

    public SignPostMarker(BlockPos pos, ITextComponent name) {
        this();
        this.pos = pos;
        this.name = name;
    }

    @Nullable
    public static SignPostMarker getFromWorld(IBlockReader world, BlockPos pos){
        TileEntity tileentity = world.getBlockEntity(pos);
        if (tileentity instanceof SignPostBlockTile) {
            SignPostBlockTile te = ((SignPostBlockTile) tileentity);
            ITextComponent t = new StringTextComponent("");
            if(te.up)t=te.textHolder.signText[0];
            if(te.down && t.getString().isEmpty())
                t=te.textHolder.signText[1];
            if(t.getString().isEmpty())t=null;
            return new SignPostMarker(pos,t);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public CustomDecoration doCreateDecoration(byte mapX, byte mapY, byte rot) {
        return new CustomDecoration(this.getType(),mapX,mapY,rot,name);
    }

}
