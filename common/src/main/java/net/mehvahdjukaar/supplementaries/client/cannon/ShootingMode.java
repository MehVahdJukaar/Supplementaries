package net.mehvahdjukaar.supplementaries.client.cannon;

public enum ShootingMode {
    DOWN,
    UP,
    STRAIGHT;

    ShootingMode cycle(){
        return switch(this){
            case DOWN -> UP;
            case UP -> STRAIGHT;
            case STRAIGHT -> DOWN;
        };
    }
}
