package dev.piny.lifeforce.common.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.piny.lifeforce.common.LifeforceCommon;

public class Commands {

    private final LifeforceCommon plugin;

    public Commands(LifeforceCommon plugin) {
        this.plugin = plugin;

        new CommandAPICommand("lifeforce")
                .withPermission("lifeforce.admin")
                .withSubcommands(
                        new Timer().command(plugin),
                        new CommandAPICommand("reload")
                                .executes((sender, args) -> {
                                    plugin.reloadConfig();
                                    sender.sendMessage("LifeforceCommon configuration reloaded.");
                                })
                )
                .executes((sender, args) -> {
                    sender.sendMessage("LifeforceCommon: use /lifeforce <reload|timer>");
                })
                .register();
    }
}