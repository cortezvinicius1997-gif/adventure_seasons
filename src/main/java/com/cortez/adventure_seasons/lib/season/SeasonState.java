package com.cortez.adventure_seasons.lib.season;

import com.cortez.adventure_seasons.AdventureSeasons;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;

public class SeasonState extends SavedData {
    private static final String KEY = "adventure_seasons_state";
    private static final int DIRTY_INTERVAL = 100;

    private Season.SubSeason currentSubSeason = Season.SubSeason.EARLY_SPRING;
    private int ticksInCurrentSubSeason = 0;
    private int ticksSinceLastSave = 0;

    private static SeasonState instance;

    public SeasonState() {
        super();
    }

    public static SeasonState getOrCreate(MinecraftServer server) {
        if (server == null) {
            return instance;
        }

        if (instance == null) {
            var overworld = server.getLevel(Level.OVERWORLD);
            if (overworld == null) {
                return null;
            }

            SavedDataStorage manager = overworld.getDataStorage();
            instance = manager.computeIfAbsent(getPersistentStateType());
        }
        return instance;
    }

    private CompoundTag toTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("SubSeason", currentSubSeason.name());
        nbt.putInt("TicksInSubSeason", ticksInCurrentSubSeason);
        return nbt;
    }

    private static DataResult<SeasonState> fromTag(CompoundTag nbt) {
        SeasonState state = new SeasonState();

        String subSeasonName = nbt.getString("SubSeason").orElse("");

        if (subSeasonName.isEmpty()) {
            String seasonName = nbt.getString("Season").orElse("");
            if (!seasonName.isEmpty()) {
                try {
                    Season oldSeason = Season.valueOf(seasonName);
                    state.currentSubSeason = switch (oldSeason) {
                        case SPRING -> Season.SubSeason.EARLY_SPRING;
                        case SUMMER -> Season.SubSeason.EARLY_SUMMER;
                        case AUTUMN -> Season.SubSeason.EARLY_AUTUMN;
                        case WINTER -> Season.SubSeason.EARLY_WINTER;
                    };
                } catch (IllegalArgumentException e) {
                    state.currentSubSeason = Season.SubSeason.EARLY_SPRING;
                }
            } else {
                state.currentSubSeason = Season.SubSeason.EARLY_SPRING;
            }
        } else {
            try {
                state.currentSubSeason = Season.SubSeason.valueOf(subSeasonName);
            } catch (IllegalArgumentException e) {
                state.currentSubSeason = Season.SubSeason.EARLY_SPRING;
            }
        }

        state.ticksInCurrentSubSeason = nbt.getInt("TicksInSubSeason").orElse(0);
        if (state.ticksInCurrentSubSeason == 0) {
            state.ticksInCurrentSubSeason = nbt.getInt("TicksInSeason").orElse(0);
        }

        AdventureSeasons.LOGGER.info("[Adventure Seasons] Subestação carregada: " + state.currentSubSeason +
                " (Ticks: " + state.ticksInCurrentSubSeason + ")");

        return DataResult.success(state);
    }

    public static final Codec<SeasonState> CODEC = CompoundTag.CODEC.comapFlatMap(
            SeasonState::fromTag,
            state -> state.toTag()
    );

    public static SavedDataType<SeasonState> getPersistentStateType() {
        return new SavedDataType<>(
                Identifier.fromNamespaceAndPath(AdventureSeasons.MODID, KEY),
                SeasonState::new,
                CODEC,
                DataFixTypes.LEVEL
        );
    }

    public Season.SubSeason getCurrentSubSeason() { return currentSubSeason; }
    public Season getCurrentSeason() { return currentSubSeason.getSeason(); }
    public int getTicksInCurrentSubSeason() { return ticksInCurrentSubSeason; }

    public void setCurrentSubSeason(Season.SubSeason subSeason) {
        this.currentSubSeason = subSeason;
        setDirty();
    }

    public void addTicks(int amount) {
        this.ticksInCurrentSubSeason += amount;
        setDirty();
    }

    public void incrementTicks() {
        this.ticksInCurrentSubSeason++;
        this.ticksSinceLastSave++;
        if (this.ticksSinceLastSave >= DIRTY_INTERVAL) {
            this.ticksSinceLastSave = 0;
            setDirty();
        }
    }

    public void resetTicks() {
        this.ticksInCurrentSubSeason = 0;
        setDirty();
    }

    public void nextSubSeason() {
        currentSubSeason = switch (currentSubSeason) {
            case EARLY_SPRING -> Season.SubSeason.MID_SPRING;
            case MID_SPRING -> Season.SubSeason.LATE_SPRING;
            case LATE_SPRING -> Season.SubSeason.EARLY_SUMMER;
            case EARLY_SUMMER -> Season.SubSeason.MID_SUMMER;
            case MID_SUMMER -> Season.SubSeason.LATE_SUMMER;
            case LATE_SUMMER -> Season.SubSeason.EARLY_AUTUMN;
            case EARLY_AUTUMN -> Season.SubSeason.MID_AUTUMN;
            case MID_AUTUMN -> Season.SubSeason.LATE_AUTUMN;
            case LATE_AUTUMN -> Season.SubSeason.EARLY_WINTER;
            case EARLY_WINTER -> Season.SubSeason.MID_WINTER;
            case MID_WINTER -> Season.SubSeason.LATE_WINTER;
            case LATE_WINTER -> Season.SubSeason.EARLY_SPRING;
        };
        resetTicks();
        AdventureSeasons.LOGGER.info("[Adventure Seasons] Nova subestação: " + currentSubSeason);
    }

    public static Season get() {
        return instance != null ? instance.getCurrentSeason() : Season.SPRING;
    }

    public static Season.SubSeason getSubSeason() {
        return instance != null ? instance.getCurrentSubSeason() : Season.SubSeason.EARLY_SPRING;
    }

    public static void set(Season.SubSeason subSeason) {
        if (instance != null) {
            instance.setCurrentSubSeason(subSeason);
        }
    }

    public static void next() {
        if (instance != null) {
            instance.nextSubSeason();
        }
    }

    public static void updateFromServer(Season.SubSeason subSeason, int ticks) {
        if (instance == null) {
            instance = new SeasonState();
        }
        instance.currentSubSeason = subSeason;
        instance.ticksInCurrentSubSeason = ticks;
    }

    public static void clearInstance() {
        instance = null;
    }
}
