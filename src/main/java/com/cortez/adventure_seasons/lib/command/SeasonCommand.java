package com.cortez.adventure_seasons.lib.command;

import com.cortez.adventure_seasons.lib.network.SeasonNetworkServer;
import com.cortez.adventure_seasons.lib.season.Season;
import com.cortez.adventure_seasons.lib.season.SeasonState;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class SeasonCommand
{
    private static final SuggestionProvider<ServerCommandSource> SEASON_SUGGESTIONS =
            (context, builder) -> {
                return CommandSource.suggestMatching(
                        new String[]{"SPRING", "SUMMER", "AUTUMN", "WINTER"},
                        builder
                );
            };

    private static final SuggestionProvider<ServerCommandSource> SUBSEASON_SUGGESTIONS =
            (context, builder) -> {
                return CommandSource.suggestMatching(
                        new String[]{
                                "EARLY_SPRING", "MID_SPRING", "LATE_SPRING",
                                "EARLY_SUMMER", "MID_SUMMER", "LATE_SUMMER",
                                "EARLY_AUTUMN", "MID_AUTUMN", "LATE_AUTUMN",
                                "EARLY_WINTER", "MID_WINTER", "LATE_WINTER"
                        },
                        builder
                );
            };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("season")
                        .executes(SeasonCommand::getCurrentSeason)
                        .then(CommandManager.literal("set")
                                .requires(source -> source.hasPermissionLevel(2))
                                .then(CommandManager.argument("subseason", StringArgumentType.string())
                                        .suggests(SUBSEASON_SUGGESTIONS)
                                        .executes(SeasonCommand::setSubSeason)
                                )
                        )
                        .then(CommandManager.literal("setseason")
                                .requires(source -> source.hasPermissionLevel(2))
                                .then(CommandManager.argument("season", StringArgumentType.string())
                                        .suggests(SEASON_SUGGESTIONS)
                                        .executes(SeasonCommand::setSeason)
                                )
                        )
                        .then(CommandManager.literal("next")
                                .requires(source -> source.hasPermissionLevel(2))
                                .executes(SeasonCommand::nextSubSeason)
                        )
        );
    }

    private static int getCurrentSeason(CommandContext<ServerCommandSource> context) {
        Season.SubSeason currentSubSeason = SeasonState.getSubSeason();
        Season currentSeason = SeasonState.get();

        context.getSource().sendFeedback(
                () -> Text.translatable("command.season.current",
                        currentSeason.getDisplayName(),
                        currentSubSeason.getDisplayName()),
                false
        );
        return 1;
    }

    private static int setSubSeason(CommandContext<ServerCommandSource> context) {
        String subSeasonName = StringArgumentType.getString(context, "subseason").toUpperCase();

        try {
            Season.SubSeason subSeason = Season.SubSeason.valueOf(subSeasonName);

            SeasonState.set(subSeason);

            context.getSource().sendFeedback(
                    () -> Text.translatable("command.season.set.subseason.success",
                            subSeason.getSeason().getDisplayName(),
                            subSeason.getDisplayName()),
                    true
            );

            context.getSource().getServer().getPlayerManager().broadcast(
                    Text.translatable("command.season.set.subseason.broadcast",
                            subSeason.getSeason().getDisplayName(),
                            subSeason.getDisplayName()),
                    false
            );

            // Sincroniza a nova estação com todos os clientes
            SeasonNetworkServer.forceSyncToAllPlayers(context.getSource().getServer());

            return 1;
        } catch (IllegalArgumentException e) {
            context.getSource().sendError(
                    Text.translatable("command.season.set.subseason.invalid")
            );
            return 0;
        }
    }

    private static int setSeason(CommandContext<ServerCommandSource> context) {
        String seasonName = StringArgumentType.getString(context, "season").toUpperCase();

        try {
            Season season = Season.valueOf(seasonName);

            Season.SubSeason subSeason = switch (season) {
                case SPRING -> Season.SubSeason.EARLY_SPRING;
                case SUMMER -> Season.SubSeason.EARLY_SUMMER;
                case AUTUMN -> Season.SubSeason.EARLY_AUTUMN;
                case WINTER -> Season.SubSeason.EARLY_WINTER;
            };

            SeasonState.set(subSeason);

            context.getSource().sendFeedback(
                    () -> Text.translatable("command.season.set.season.success",
                            season.getDisplayName()),
                    true
            );

            context.getSource().getServer().getPlayerManager().broadcast(
                    Text.translatable("command.season.set.season.broadcast",
                            season.getDisplayName()),
                    false
            );

            // Sincroniza a nova estação com todos os clientes
            SeasonNetworkServer.forceSyncToAllPlayers(context.getSource().getServer());

            return 1;
        } catch (IllegalArgumentException e) {
            context.getSource().sendError(
                    Text.translatable("command.season.set.season.invalid")
            );
            return 0;
        }
    }

    private static int nextSubSeason(CommandContext<ServerCommandSource> context) {
        Season.SubSeason oldSubSeason = SeasonState.getSubSeason();
        SeasonState.next();
        Season.SubSeason newSubSeason = SeasonState.getSubSeason();

        context.getSource().sendFeedback(
                () -> Text.translatable("command.season.next.success",
                        oldSubSeason.getSeason().getDisplayName(),
                        oldSubSeason.getDisplayName(),
                        newSubSeason.getSeason().getDisplayName(),
                        newSubSeason.getDisplayName()),
                true
        );

        context.getSource().getServer().getPlayerManager().broadcast(
                Text.translatable("command.season.next.broadcast",
                        newSubSeason.getSeason().getDisplayName(),
                        newSubSeason.getDisplayName()),
                false
        );

        // Sincroniza a nova estação com todos os clientes
        SeasonNetworkServer.forceSyncToAllPlayers(context.getSource().getServer());

        return 1;
    }
}