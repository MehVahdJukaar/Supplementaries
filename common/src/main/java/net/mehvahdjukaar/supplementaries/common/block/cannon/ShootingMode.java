package net.mehvahdjukaar.supplementaries.common.block.cannon;

public enum ShootingMode {
    DOWN,
    UP,
    STRAIGHT;

    public ShootingMode cycle(){
        return switch(this){
            case DOWN -> UP;
            case UP -> STRAIGHT;
            case STRAIGHT -> DOWN;
        };
    }
}
