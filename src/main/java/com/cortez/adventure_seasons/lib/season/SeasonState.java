package com.cortez.adventure_seasons.lib.season;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

public class SeasonState extends PersistentState {

    private static final String KEY = "adventure_seasons_state";
    private Season.SubSeason currentSubSeason = Season.SubSeason.EARLY_SPRING;
    private int ticksInCurrentSubSeason = 0;

    // Instância estática (carregada do mundo)
    private static SeasonState instance;

    public SeasonState() {
        super();
    }

    // Carrega ou cria o estado da estação
    public static SeasonState getOrCreate(MinecraftServer server) {
        if (instance == null) {
            PersistentStateManager manager = server.getWorld(World.OVERWORLD)
                    .getPersistentStateManager();

            instance = manager.getOrCreate(
                    new Type<>(
                            SeasonState::new,
                            SeasonState::fromNbt,
                            null
                    ),
                    KEY
            );
        }
        return instance;
    }

    // Lê os dados do NBT
    public static SeasonState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        SeasonState state = new SeasonState();

        String subSeasonName = nbt.getString("SubSeason");

        // Retrocompatibilidade: se não houver SubSeason, tenta carregar Season antiga
        if (subSeasonName.isEmpty()) {
            String seasonName = nbt.getString("Season");
            if (!seasonName.isEmpty()) {
                // Converte Season antiga para SubSeason (sempre começa no início)
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

        state.ticksInCurrentSubSeason = nbt.getInt("TicksInSubSeason");
        if (state.ticksInCurrentSubSeason == 0) {
            state.ticksInCurrentSubSeason = nbt.getInt("TicksInSeason"); // Retrocompatibilidade
        }

        System.out.println("[Adventure Seasons] Subestação carregada: " + state.currentSubSeason +
                " (Ticks: " + state.ticksInCurrentSubSeason + ")");

        return state;
    }

    // Salva os dados no NBT
    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putString("SubSeason", currentSubSeason.name());
        nbt.putInt("TicksInSubSeason", ticksInCurrentSubSeason);
        return nbt;
    }

    // Getters e Setters
    public Season.SubSeason getCurrentSubSeason() {
        return currentSubSeason;
    }

    public Season getCurrentSeason() {
        return currentSubSeason.getSeason();
    }

    public void setCurrentSubSeason(Season.SubSeason subSeason) {
        this.currentSubSeason = subSeason;
        markDirty();
    }

    public int getTicksInCurrentSubSeason() {
        return ticksInCurrentSubSeason;
    }

    public void addTicks(int amount) {
        this.ticksInCurrentSubSeason += amount;
        markDirty();
    }

    public void setTicksInCurrentSubSeason(int ticks) {
        this.ticksInCurrentSubSeason = ticks;
        markDirty();
    }

    public void incrementTicks() {
        this.ticksInCurrentSubSeason++;
        markDirty();
    }

    public void resetTicks() {
        this.ticksInCurrentSubSeason = 0;
        markDirty();
    }

    // Avança para a próxima subestação
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
        System.out.println("[Adventure Seasons] Nova subestação: " + currentSubSeason);
    }

    // Métodos estáticos de conveniência
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
}