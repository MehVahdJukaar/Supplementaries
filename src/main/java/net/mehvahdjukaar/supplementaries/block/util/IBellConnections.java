package net.mehvahdjukaar.supplementaries.block.util;

import net.minecraft.util.StringRepresentable;

public interface IBellConnections {

    BellConnection getConnected();
    void setConnected(BellConnection connected);

    enum BellConnection implements StringRepresentable {
        NONE,CHAIN,ROPE;
        public boolean isRope(){
            return this==ROPE;
        }
        public boolean isEmpty(){
            return this==NONE;
        }
        public boolean isChain(){
            return this==CHAIN;
        }

        @Override
        public String getSerializedName() {
            switch (this){
                default:
                case NONE:return "none";
                case ROPE:return "rope";
                case CHAIN:return "chain";
            }
        }
    }
}
