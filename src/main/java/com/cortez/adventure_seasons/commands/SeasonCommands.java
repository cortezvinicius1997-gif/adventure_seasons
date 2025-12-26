package com.cortez.adventure_seasons.commands;

import com.cortez.adventure_seasons.lib.command.SeasonCommand;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class SeasonCommands {
    public static void registerCommand()
    {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            SeasonCommand.register(dispatcher);
        });
    }
}
