package com.cortez.adventure_seasons.lib.util;

import com.cortez.adventure_seasons.AdventureSeasons;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class ReplacedMeltablesState extends SavedData {

    private static final String DATA_ID = "seasons_replaced_meltables";

    Long2ObjectArrayMap<Long2ObjectArrayMap<BlockState>> chunkToReplaced = new Long2ObjectArrayMap<>();

    public ReplacedMeltablesState() {
        super();
    }

    public BlockState getReplaced(BlockPos blockPos) {
        long chunkPack = ChunkPos.pack(blockPos);
        Long2ObjectArrayMap<BlockState> posToReplaced = chunkToReplaced.get(chunkPack);
        if (posToReplaced != null) {
            return posToReplaced.get(blockPos.asLong());
        }
        return null;
    }

    public void setReplaced(BlockPos blockPos, BlockState replacedState) {
        long chunkPack = ChunkPos.pack(blockPos);
        Long2ObjectArrayMap<BlockState> posToReplaced = chunkToReplaced.get(chunkPack);
        if (posToReplaced != null) {
            if (replacedState != null) {
                posToReplaced.put(blockPos.asLong(), replacedState);
            } else {
                posToReplaced.remove(blockPos.asLong());
                if (posToReplaced.isEmpty()) {
                    chunkToReplaced.remove(chunkPack);
                }
            }
        } else if (replacedState != null) {
            posToReplaced = new Long2ObjectArrayMap<>();
            posToReplaced.put(blockPos.asLong(), replacedState);
            chunkToReplaced.put(chunkPack, posToReplaced);
        }
        setDirty();
    }

    private CompoundTag toTag() {
        CompoundTag nbt = new CompoundTag();
        chunkToReplaced.long2ObjectEntrySet().fastForEach(entry -> {
            if (!entry.getValue().isEmpty()) {
                CompoundTag innerNbt = new CompoundTag();
                entry.getValue().long2ObjectEntrySet().fastForEach(innerEntry -> {
                    BlockState.CODEC.encodeStart(NbtOps.INSTANCE, innerEntry.getValue()).ifSuccess(tag -> {
                        innerNbt.put(innerEntry.getLongKey() + "", tag);
                    });
                });
                nbt.put(entry.getLongKey() + "", innerNbt);
            }
        });
        return nbt;
    }

    private static DataResult<ReplacedMeltablesState> fromTag(CompoundTag nbt) {
        ReplacedMeltablesState state = new ReplacedMeltablesState();
        nbt.keySet().forEach(key -> {
            try {
                long longKey = Long.parseLong(key);
                Long2ObjectArrayMap<BlockState> posToReplaced = new Long2ObjectArrayMap<>();
                CompoundTag innerNbt = nbt.getCompoundOrEmpty(key);
                innerNbt.keySet().forEach(innerKey -> {
                    long innerLongKey = Long.parseLong(innerKey);
                    Tag innerTag = innerNbt.get(innerKey);
                    if (innerTag != null) {
                        BlockState.CODEC.decode(NbtOps.INSTANCE, innerTag).ifSuccess(pair -> {
                            posToReplaced.put(innerLongKey, pair.getFirst());
                        });
                    }
                });
                state.chunkToReplaced.put(longKey, posToReplaced);
            } catch (NumberFormatException exception) {
                AdventureSeasons.LOGGER.error("[Adventure Mod] Error reading replaced meltable blocks at " + key, exception);
            }
        });
        return DataResult.success(state);
    }

    public static final Codec<ReplacedMeltablesState> CODEC = CompoundTag.CODEC.comapFlatMap(
            ReplacedMeltablesState::fromTag,
            state -> state.toTag()
    );

    public static SavedDataType<ReplacedMeltablesState> getPersistentStateType() {
        return new SavedDataType<>(
                Identifier.fromNamespaceAndPath(AdventureSeasons.MODID, DATA_ID),
                ReplacedMeltablesState::new,
                CODEC,
                DataFixTypes.LEVEL
        );
    }
}
