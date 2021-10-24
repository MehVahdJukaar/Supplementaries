//package net.mehvahdjukaar.supplementaries.compat.flywheel.instances;
//
//import com.jozufozu.flywheel.backend.gl.attrib.CommonAttributes;
//import com.jozufozu.flywheel.backend.gl.attrib.IAttribSpec;
//import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
//import com.jozufozu.flywheel.backend.gl.buffer.MappedBuffer;
//import com.jozufozu.flywheel.backend.instancing.IDynamicInstance;
//import com.jozufozu.flywheel.backend.instancing.Instancer;
//import com.jozufozu.flywheel.backend.instancing.tile.TileEntityInstance;
//import com.jozufozu.flywheel.backend.material.MaterialManager;
//import com.jozufozu.flywheel.backend.material.MaterialSpec;
//import com.jozufozu.flywheel.core.Formats;
//import com.jozufozu.flywheel.core.materials.BasicData;
//import com.simibubi.create.content.contraptions.base.KineticTileEntity;
//import com.simibubi.create.content.contraptions.base.RotatingData;
//import com.simibubi.create.content.contraptions.relays.belt.BeltData;
//import com.simibubi.create.foundation.render.AllMaterialSpecs;
//import com.simibubi.create.foundation.utility.Color;
//import net.mehvahdjukaar.supplementaries.block.blocks.WindVaneBlock;
//import net.mehvahdjukaar.supplementaries.block.tiles.WindVaneBlockTile;
//import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
//import net.minecraft.block.BlockState;
//import net.minecraft.util.ResourceLocation;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.vector.Vector3f;
//
//public class WindVaneInstance extends TileEntityInstance<WindVaneBlockTile> implements IDynamicInstance {
//    public static final ResourceLocation PROG_RES = new ResourceLocation("supplementaries","rotating");
//    public static final MaterialSpec<RotatingData> ROTATING;
//    public static final ResourceLocation RES = new ResourceLocation("supplementaries", "rotating");
//    public static VertexFormat FORMAT;
//    static{
//
//        FORMAT = kineticInstance().addAttributes(new IAttribSpec[]{CommonAttributes.NORMAL}).build();
//        ROTATING = new MaterialSpec(RES,
//                PROG_RES, Formats.UNLIT_MODEL, FORMAT, RotatingData::new);
//
//    }
//    private static VertexFormat.Builder kineticInstance() {
//        return Formats.litInstance().addAttributes(CommonAttributes.VEC3, CommonAttributes.FLOAT, CommonAttributes.FLOAT);
//    }
//
//    private final BlockState upModel = ModRegistry.WIND_VANE.get().defaultBlockState().setValue(WindVaneBlock.TILE,true);
//    //private final TextureAtlasSprite texture;
//
//    //private final MatrixTransformStack stack;
//    private float lastProgress = 0;
//
//    public WindVaneInstance(MaterialManager<?> materialManager, WindVaneBlockTile tile) {
//        super(materialManager, tile);
//
//        Instancer<BeltData> beltModel = materialManager.defaultSolid().material(AllMaterialSpecs.BELTS)
//                .getModel(this.upModel);
//
//        //How do I rotate ??? confusion
//
//
//    }
//
//    @Override
//    public void beginFrame() {
//
//
//
//    }
//
//    @Override
//    public void remove() {
//
//    }
//
//    @Override
//    public void updateLight() {
//
//    }
//
//
//
//
//    public static class WindVaneData extends BasicData {
//        private float x;
//        private float y;
//        private float z;
//        private float rotationalSpeed;
//        private float rotationOffset;
//
//        protected WindVaneData(Instancer<?> owner) {
//            super(owner);
//        }
//
//        public WindVaneData setPosition(BlockPos pos) {
//            return this.setPosition((float)pos.getX(), (float)pos.getY(), (float)pos.getZ());
//        }
//
//        public WindVaneData setPosition(Vector3f pos) {
//            return this.setPosition(pos.x(), pos.y(), pos.z());
//        }
//
//        public WindVaneData setPosition(float x, float y, float z) {
//            this.x = x;
//            this.y = y;
//            this.z = z;
//            this.markDirty();
//            return this;
//        }
//
//        public WindVaneData nudge(float x, float y, float z) {
//            this.x += x;
//            this.y += y;
//            this.z += z;
//            this.markDirty();
//            return this;
//        }
//
//        public WindVaneData setColor(KineticTileEntity te) {
//            if (te.hasNetwork()) {
//                this.setColor(Color.generateFromLong(te.network));
//            } else {
//                this.setColor(255, 255, 255);
//            }
//
//            return this;
//        }
//
//        public WindVaneData setColor(Color c) {
//            this.setColor(c.getRed(), c.getGreen(), c.getBlue());
//            return this;
//        }
//
//        public WindVaneData setRotationalSpeed(float rotationalSpeed) {
//            this.rotationalSpeed = rotationalSpeed;
//            return this;
//        }
//
//        public WindVaneData setRotationOffset(float rotationOffset) {
//            this.rotationOffset = rotationOffset;
//            return this;
//        }
//
//        public void write(MappedBuffer buf) {
//            super.write(buf);
//            buf.putFloatArray(new float[]{this.x, this.y, this.z, this.rotationalSpeed, this.rotationOffset});
//        }
//    }
//
//
//}
