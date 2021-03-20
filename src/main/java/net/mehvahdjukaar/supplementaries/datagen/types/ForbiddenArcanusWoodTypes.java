package net.mehvahdjukaar.supplementaries.datagen.types;


public enum ForbiddenArcanusWoodTypes implements IWoodType {
    ARCANE_EDELWOOD("arcane_edelwood"),
    CHERRYWOOD("cherrywood"),
    MYSTERYWOOD("mysterywood"),
    EDELWOOD("edelwood");

    private final String name;

    ForbiddenArcanusWoodTypes(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getNamespace() {
        return "forbidden_arcanus";
    }
}
