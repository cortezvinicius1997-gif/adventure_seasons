package com.cortez.adventure_seasons.lib.hud;

import com.cortez.adventure_seasons.block.custom.SeasonCalendar;
import com.cortez.adventure_seasons.lib.config.AdventureSeasonConfig;
import com.cortez.adventure_seasons.lib.season.Season;
import com.cortez.adventure_seasons.lib.season.SeasonState;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class SeasonCalendarTooltipRenderer {
    public static void register() {
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null || client.isPaused()) return;


            HitResult hit = client.crosshairTarget;
            if (hit == null || hit.getType() != HitResult.Type.BLOCK) return;

            BlockHitResult blockHit = (BlockHitResult) hit;
            BlockPos pos = blockHit.getBlockPos();
            BlockState state = client.world.getBlockState(pos);

            if (!(state.getBlock() instanceof SeasonCalendar)) return;

            Season.SubSeason subSeason = state.get(SeasonCalendar.SUBSEASON);
            Season season = subSeason.getSeason();

            MinecraftServer server = client.getServer();
            SeasonState seasonState = SeasonState.getOrCreate(server);
            int currentTicks = seasonState.getTicksInCurrentSubSeason();
            Season.SubSeason currentSubSeason = seasonState.getCurrentSubSeason();

            int duration;
            int accumulatedTicks = currentTicks;

            if (season == Season.SPRING) {
                if (currentSubSeason == Season.SubSeason.MID_SPRING) {
                    accumulatedTicks += AdventureSeasonConfig.getTicksPerSeason().getSpring().getEarlyLength();
                } else if (currentSubSeason == Season.SubSeason.LATE_SPRING) {
                    accumulatedTicks += AdventureSeasonConfig.getTicksPerSeason().getSpring().getEarlyLength();
                    accumulatedTicks += AdventureSeasonConfig.getTicksPerSeason().getSpring().getMidLength();
                }
                duration = AdventureSeasonConfig.getTicksPerSeason().getSpring().getEarlyLength()
                        + AdventureSeasonConfig.getTicksPerSeason().getSpring().getMidLength()
                        + AdventureSeasonConfig.getTicksPerSeason().getSpring().getLateLength();

            } else if (season == Season.SUMMER) {
                if (currentSubSeason == Season.SubSeason.MID_SUMMER) {
                    accumulatedTicks += AdventureSeasonConfig.getTicksPerSeason().getSummer().getEarlyLength();
                } else if (currentSubSeason == Season.SubSeason.LATE_SUMMER) {
                    accumulatedTicks += AdventureSeasonConfig.getTicksPerSeason().getSummer().getEarlyLength();
                    accumulatedTicks += AdventureSeasonConfig.getTicksPerSeason().getSummer().getMidLength();
                }
                duration = AdventureSeasonConfig.getTicksPerSeason().getSummer().getEarlyLength()
                        + AdventureSeasonConfig.getTicksPerSeason().getSummer().getMidLength()
                        + AdventureSeasonConfig.getTicksPerSeason().getSummer().getLateLength();

            } else if (season == Season.AUTUMN) {
                if (currentSubSeason == Season.SubSeason.MID_AUTUMN) {
                    accumulatedTicks += AdventureSeasonConfig.getTicksPerSeason().getAutumn().getEarlyLength();
                } else if (currentSubSeason == Season.SubSeason.LATE_AUTUMN) {
                    accumulatedTicks += AdventureSeasonConfig.getTicksPerSeason().getAutumn().getEarlyLength();
                    accumulatedTicks += AdventureSeasonConfig.getTicksPerSeason().getAutumn().getMidLength();
                }
                duration = AdventureSeasonConfig.getTicksPerSeason().getAutumn().getEarlyLength()
                        + AdventureSeasonConfig.getTicksPerSeason().getAutumn().getMidLength()
                        + AdventureSeasonConfig.getTicksPerSeason().getAutumn().getLateLength();

            } else { // WINTER
                if (currentSubSeason == Season.SubSeason.MID_WINTER) {
                    accumulatedTicks += AdventureSeasonConfig.getTicksPerSeason().getWinter().getEarlyLength();
                } else if (currentSubSeason == Season.SubSeason.LATE_WINTER) {
                    accumulatedTicks += AdventureSeasonConfig.getTicksPerSeason().getWinter().getEarlyLength();
                    accumulatedTicks += AdventureSeasonConfig.getTicksPerSeason().getWinter().getMidLength();
                }
                duration = AdventureSeasonConfig.getTicksPerSeason().getWinter().getEarlyLength()
                        + AdventureSeasonConfig.getTicksPerSeason().getWinter().getMidLength()
                        + AdventureSeasonConfig.getTicksPerSeason().getWinter().getLateLength();
            }

            int current_days = accumulatedTicks / 24000;
            int total_days = duration / 24000;
            String days = current_days + "/" + total_days;

            // Cria lista de textos para o tooltip
            List<Text> tooltipLines = new ArrayList<>();
            tooltipLines.add(Text.translatable("block.adventure_seasons.season_calendar").formatted(Formatting.BLUE));
            tooltipLines.add(Text.translatable(
                    "tooltip.adventure_seasons.season",
                    season.getDisplayName()
            ).formatted(Formatting.GRAY));
            tooltipLines.add(Text.translatable(
                    "tooltip.adventure_seasons.duration",
                    days
            ).formatted(Formatting.GRAY));

            // Renderiza tooltip estilo vanilla
            int windowWidth = context.getScaledWindowWidth();
            int windowHeight = context.getScaledWindowHeight();
            int x = windowWidth / 2 + 8;
            int y = windowHeight / 2 + 8;

            context.drawTooltip(client.textRenderer, tooltipLines, x, y);
        });
    }
}