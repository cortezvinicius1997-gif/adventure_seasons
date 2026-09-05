package com.cortez.adventure_seasons.lib.hud;
import com.cortez.adventure_seasons.block.custom.SeasonCalendar;
import com.cortez.adventure_seasons.lib.AdventureSeason;
import com.cortez.adventure_seasons.lib.config.AdventureSeasonConfig;
import com.cortez.adventure_seasons.lib.network.SeasonNetworkClient;
import com.cortez.adventure_seasons.lib.season.Season;
import com.cortez.adventure_seasons.lib.season.SeasonState;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SeasonCalendarTooltipRenderer {
    public static void register() {
        HudElementRegistry.addLast(AdventureSeason.identifier("season_calendar_tooltip"), (graphics, deltaTracker) -> {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null || client.level == null || client.isPaused()) return;
            HitResult hit = client.hitResult;
            if (hit == null || hit.getType() != HitResult.Type.BLOCK) return;
            BlockHitResult blockHit = (BlockHitResult) hit;
            BlockPos pos = blockHit.getBlockPos();
            BlockState state = client.level.getBlockState(pos);
            if (!(state.getBlock() instanceof SeasonCalendar)) return;
            Season.SubSeason subSeason = state.getValue(SeasonCalendar.SUBSEASON);
            Season season = subSeason.getSeason();

            if (SeasonNetworkClient.isInitialized()) {
                renderTooltipWithNetworkData(graphics, client, season, subSeason);
                return;
            }

            MinecraftServer server = client.getSingleplayerServer();
            if (server == null || !client.hasSingleplayerServer()) {
                renderTooltipWithCachedState(graphics, client, season, subSeason);
                return;
            }
            SeasonState seasonState = SeasonState.getOrCreate(server);
            if (seasonState == null) {
                renderTooltipWithCachedState(graphics, client, season, subSeason);
                return;
            }
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
            } else {
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
            List<Component> tooltipLines = new ArrayList<>();
            tooltipLines.add(Component.translatable("block.adventure_seasons.season_calendar").withStyle(ChatFormatting.BLUE));
            tooltipLines.add(Component.translatable("tooltip.adventure_seasons.season", season.getDisplayName()).withStyle(ChatFormatting.GRAY));
            tooltipLines.add(Component.translatable("tooltip.adventure_seasons.duration", days).withStyle(ChatFormatting.GRAY));
            int windowWidth = graphics.guiWidth();
            int windowHeight = graphics.guiHeight();
            int x = windowWidth / 2 + 8;
            int y = windowHeight / 2 + 8;
            graphics.setComponentTooltipForNextFrame(client.font, tooltipLines, x, y);
        });
    }
    private static void renderTooltipWithCachedState(GuiGraphicsExtractor graphics,
                                                      Minecraft client,
                                                      Season season,
                                                      Season.SubSeason subSeason) {
        Season.SubSeason currentSubSeason = SeasonState.getSubSeason();
        int duration;
        if (season == Season.SPRING) {
            duration = AdventureSeasonConfig.getTicksPerSeason().getSpring().getEarlyLength()
                    + AdventureSeasonConfig.getTicksPerSeason().getSpring().getMidLength()
                    + AdventureSeasonConfig.getTicksPerSeason().getSpring().getLateLength();
        } else if (season == Season.SUMMER) {
            duration = AdventureSeasonConfig.getTicksPerSeason().getSummer().getEarlyLength()
                    + AdventureSeasonConfig.getTicksPerSeason().getSummer().getMidLength()
                    + AdventureSeasonConfig.getTicksPerSeason().getSummer().getLateLength();
        } else if (season == Season.AUTUMN) {
            duration = AdventureSeasonConfig.getTicksPerSeason().getAutumn().getEarlyLength()
                    + AdventureSeasonConfig.getTicksPerSeason().getAutumn().getMidLength()
                    + AdventureSeasonConfig.getTicksPerSeason().getAutumn().getLateLength();
        } else {
            duration = AdventureSeasonConfig.getTicksPerSeason().getWinter().getEarlyLength()
                    + AdventureSeasonConfig.getTicksPerSeason().getWinter().getMidLength()
                    + AdventureSeasonConfig.getTicksPerSeason().getWinter().getLateLength();
        }
        int total_days = duration / 24000;
        List<Component> tooltipLines = new ArrayList<>();
        tooltipLines.add(Component.translatable("block.adventure_seasons.season_calendar").withStyle(ChatFormatting.BLUE));
        tooltipLines.add(Component.translatable("tooltip.adventure_seasons.season", season.getDisplayName()).withStyle(ChatFormatting.GRAY));
        tooltipLines.add(Component.translatable("tooltip.adventure_seasons.total_duration", total_days).withStyle(ChatFormatting.GRAY));
        int windowWidth = graphics.guiWidth();
        int windowHeight = graphics.guiHeight();
        int x = windowWidth / 2 + 8;
        int y = windowHeight / 2 + 8;
        graphics.setComponentTooltipForNextFrame(client.font, tooltipLines, x, y);
    }

    private static void renderTooltipWithNetworkData(GuiGraphicsExtractor graphics,
                                                      Minecraft client,
                                                      Season season,
                                                      Season.SubSeason subSeason) {
        int currentTicks = SeasonNetworkClient.getTicks();
        Season.SubSeason currentSubSeason = SeasonNetworkClient.getSubSeason();
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
        } else {
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

        List<Component> tooltipLines = new ArrayList<>();
        tooltipLines.add(Component.translatable("block.adventure_seasons.season_calendar").withStyle(ChatFormatting.BLUE));
        tooltipLines.add(Component.translatable("tooltip.adventure_seasons.season", season.getDisplayName()).withStyle(ChatFormatting.GRAY));
        tooltipLines.add(Component.translatable("tooltip.adventure_seasons.duration", days).withStyle(ChatFormatting.GRAY));

        List<ClientTooltipComponent> tooltipComponents = tooltipLines.stream()
                .map(Component::getVisualOrderText)
                .map(ClientTooltipComponent::create)
                .collect(Collectors.toList());

        int windowWidth = graphics.guiWidth();
        int windowHeight = graphics.guiHeight();
        int x = windowWidth / 2 + 8;
        int y = windowHeight / 2 + 8;
        graphics.tooltip(client.font, tooltipComponents, x, y, DefaultTooltipPositioner.INSTANCE, null);
    }
}
