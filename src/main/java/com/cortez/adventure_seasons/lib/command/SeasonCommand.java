package com.cortez.adventure_seasons.lib.command;

import com.cortez.adventure_seasons.lib.network.SeasonNetworkServer;
import com.cortez.adventure_seasons.lib.season.Season;
import com.cortez.adventure_seasons.lib.season.SeasonState;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;

public class SeasonCommand
{
    private static final SuggestionProvider<CommandSourceStack> SEASON_SUGGESTIONS =
            (context, builder) -> {
                return SharedSuggestionProvider.suggest(
                        new String[]{"SPRING", "SUMMER", "AUTUMN", "WINTER"},
                        builder
                );
            };

    private static final SuggestionProvider<CommandSourceStack> SUBSEASON_SUGGESTIONS =
            (context, builder) -> {
                return SharedSuggestionProvider.suggest(
                        new String[]{
                                "EARLY_SPRING", "MID_SPRING", "LATE_SPRING",
                                "EARLY_SUMMER", "MID_SUMMER", "LATE_SUMMER",
                                "EARLY_AUTUMN", "MID_AUTUMN", "LATE_AUTUMN",
                                "EARLY_WINTER", "MID_WINTER", "LATE_WINTER"
                        },
                        builder
                );
            };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("season")
                        .executes(SeasonCommand::getCurrentSeason)
                        .then(Commands.literal("set")
                                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                                .then(Commands.argument("subseason", StringArgumentType.string())
                                        .suggests(SUBSEASON_SUGGESTIONS)
                                        .executes(SeasonCommand::setSubSeason)
                                )
                        )
                        .then(Commands.literal("setseason")
                                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                                .then(Commands.argument("season", StringArgumentType.string())
                                        .suggests(SEASON_SUGGESTIONS)
                                        .executes(SeasonCommand::setSeason)
                                )
                        )
                        .then(Commands.literal("next")
                                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                                .executes(SeasonCommand::nextSubSeason)
                        )
        );
    }

    private static int getCurrentSeason(CommandContext<CommandSourceStack> context) {
        Season.SubSeason currentSubSeason = SeasonState.getSubSeason();
        Season currentSeason = SeasonState.get();

        context.getSource().sendSuccess(
                () -> Component.translatable("command.season.current",
                        currentSeason.getDisplayName(),
                        currentSubSeason.getDisplayName()),
                false
        );
        return 1;
    }

    private static int setSubSeason(CommandContext<CommandSourceStack> context) {
        String subSeasonName = StringArgumentType.getString(context, "subseason").toUpperCase();

        try {
            Season.SubSeason subSeason = Season.SubSeason.valueOf(subSeasonName);

            SeasonState.set(subSeason);

            context.getSource().sendSuccess(
                    () -> Component.translatable("command.season.set.subseason.success",
                            subSeason.getSeason().getDisplayName(),
                            subSeason.getDisplayName()),
                    true
            );

            context.getSource().getServer().getPlayerList().broadcastSystemMessage(
                    Component.translatable("command.season.set.subseason.broadcast",
                            subSeason.getSeason().getDisplayName(),
                            subSeason.getDisplayName()),
                    false
            );

            // Sincroniza a nova estação com todos os clientes
            SeasonNetworkServer.forceSyncToAllPlayers(context.getSource().getServer());

            return 1;
        } catch (IllegalArgumentException e) {
            context.getSource().sendFailure(
                    Component.translatable("command.season.set.subseason.invalid")
            );
            return 0;
        }
    }

    private static int setSeason(CommandContext<CommandSourceStack> context) {
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

            context.getSource().sendSuccess(
                    () -> Component.translatable("command.season.set.season.success",
                            season.getDisplayName()),
                    true
            );

            context.getSource().getServer().getPlayerList().broadcastSystemMessage(
                    Component.translatable("command.season.set.season.broadcast",
                            season.getDisplayName()),
                    false
            );

            // Sincroniza a nova estação com todos os clientes
            SeasonNetworkServer.forceSyncToAllPlayers(context.getSource().getServer());

            return 1;
        } catch (IllegalArgumentException e) {
            context.getSource().sendFailure(
                    Component.translatable("command.season.set.season.invalid")
            );
            return 0;
        }
    }

    private static int nextSubSeason(CommandContext<CommandSourceStack> context) {
        Season.SubSeason oldSubSeason = SeasonState.getSubSeason();
        SeasonState.next();
        Season.SubSeason newSubSeason = SeasonState.getSubSeason();

        context.getSource().sendSuccess(
                () -> Component.translatable("command.season.next.success",
                        oldSubSeason.getSeason().getDisplayName(),
                        oldSubSeason.getDisplayName(),
                        newSubSeason.getSeason().getDisplayName(),
                        newSubSeason.getDisplayName()),
                true
        );

        context.getSource().getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("command.season.next.broadcast",
                        newSubSeason.getSeason().getDisplayName(),
                        newSubSeason.getDisplayName()),
                false
        );

        // Sincroniza a nova estação com todos os clientes
        SeasonNetworkServer.forceSyncToAllPlayers(context.getSource().getServer());

        return 1;
    }
}
