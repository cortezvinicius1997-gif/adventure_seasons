package com.cortez.adventure_seasons.block.custom.state;

import net.minecraft.util.StringIdentifiable;

public enum SeasonSensorState implements StringIdentifiable
{
    SPRING("spring"),
    SUMMER("summer"),
    AUTUMN("autumn"),
    WINTER("winter");

    private final String id;

    SeasonSensorState(String id) {
        this.id = id;
    }

    @Override
    public String asString() {
        return id;
    }

    public SeasonSensorState next() {
        SeasonSensorState[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }
}
