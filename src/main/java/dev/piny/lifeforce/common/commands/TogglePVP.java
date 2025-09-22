package dev.piny.lifeforce.common.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.piny.lifeforce.common.LifeforceCommon;
import org.bukkit.event.Listener;

public class TogglePVP implements Listener {
    public TogglePVP() {}

    public CommandAPICommand command(LifeforceCommon plugin) {
        return new CommandAPICommand("togglepvp")
                .withArguments(
                        new BooleanArgument("enable")
                )
                .executesPlayer((player, args) -> {
                    boolean enable = (boolean) args.get("enable");
                    player.getWorld().setPVP(enable);
                    String status = enable ? "enabled" : "disabled";
                    player.sendMessage("PVP has been " + status + ".");
                });
    }
}
