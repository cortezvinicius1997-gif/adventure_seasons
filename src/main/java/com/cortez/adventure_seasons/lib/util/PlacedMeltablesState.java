package com.cortez.adventure_seasons.lib.util;

import com.cortez.adventure_seasons.AdventureSeasons;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class PlacedMeltablesState extends SavedData {

    private static final String DATA_ID = "seasons_placed_meltables";

    Long2ObjectArrayMap<LongArraySet> chunkToPlaced = new Long2ObjectArrayMap<>();

    public PlacedMeltablesState() {
        super();
    }

    public boolean isManuallyPlaced(BlockPos blockPos) {
        long chunkPack = ChunkPos.pack(blockPos);
        LongArraySet longArray = chunkToPlaced.get(chunkPack);
        return longArray != null && longArray.contains(blockPos.asLong());
    }

    public void setManuallyPlaced(BlockPos blockPos, Boolean manuallyPlaced) {
        long chunkPack = ChunkPos.pack(blockPos);
        LongArraySet longArray = chunkToPlaced.get(chunkPack);
        if (longArray != null) {
            if (manuallyPlaced) {
                longArray.add(blockPos.asLong());
            } else {
                longArray.remove(blockPos.asLong());
                if (longArray.isEmpty()) {
                    chunkToPlaced.remove(chunkPack);
                }
            }
        } else if (manuallyPlaced) {
            longArray = new LongArraySet();
            longArray.add(blockPos.asLong());
            chunkToPlaced.put(chunkPack, longArray);
        }
        setDirty();
    }

    private CompoundTag toTag() {
        CompoundTag nbt = new CompoundTag();
        chunkToPlaced.long2ObjectEntrySet().fastForEach(entry -> {
            if (!entry.getValue().isEmpty()) {
                nbt.put(entry.getLongKey() + "", new LongArrayTag(entry.getValue().toLongArray()));
            }
        });
        return nbt;
    }

    private static DataResult<PlacedMeltablesState> fromTag(CompoundTag nbt) {
        PlacedMeltablesState state = new PlacedMeltablesState();
        nbt.keySet().forEach(key -> {
            try {
                long longKey = Long.parseLong(key);
                long[] longArray = nbt.getLongArray(key).orElse(new long[0]);
                state.chunkToPlaced.put(longKey, new LongArraySet(longArray));
            } catch (NumberFormatException exception) {
                AdventureSeasons.LOGGER.error("[Adventure mod] Error reading manually placed meltable blocks at " + key, exception);
            }
        });
        return DataResult.success(state);
    }

    public static final Codec<PlacedMeltablesState> CODEC = CompoundTag.CODEC.comapFlatMap(
            PlacedMeltablesState::fromTag,
            state -> state.toTag()
    );

    public static SavedDataType<PlacedMeltablesState> getPersistentStateType() {
        return new SavedDataType<>(
                Identifier.fromNamespaceAndPath(AdventureSeasons.MODID, DATA_ID),
                PlacedMeltablesState::new,
                CODEC,
                DataFixTypes.LEVEL
        );
    }
}
