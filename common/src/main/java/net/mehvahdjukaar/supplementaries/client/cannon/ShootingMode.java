package net.mehvahdjukaar.supplementaries.client.cannon;

public enum ShootingMode {
    DOWN,
    UP,
    MANUAL;

    ShootingMode cycle(){
        return switch(this){
            case DOWN -> UP;
            case UP -> MANUAL;
            case MANUAL -> DOWN;
        };
    }
}
