package dev.piny.lifeforce.common.anticheat;

import dev.piny.lifeforce.common.LifeforceCommon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.*;

public class AntiCheat implements Listener {

    // Per-player allowed commands (excluded from broadcast even if not globally whitelisted)
    private static final UUID MODERATING_PLAYER_UUID = UUID.fromString("6624f5a4-777e-42b2-870f-f24cb0d19244");
    private static final UUID PINY_ALT_UUID = UUID.fromString("46fcc44e-026b-49c1-afb1-b062989ecd9e");

    private final Map<UUID, List<String>> allowedCommandsPerPlayer = new HashMap<>();

    private static final String[] WHITELISTED_COMMANDS = new String[] {
            "msg", "tell", "w", "tpa", "voicechat", "dvc", "discord", "sethome", "home",
            "delhome", "hearts", "spawn", "book", "lifeforce timer", "lifeforce commandlink",
            "say", "tellraw", "vanish", "co", "vinvsee", "sit", "treeminer", "afk"
    };

    private final LifeforceCommon plugin;

    public AntiCheat(LifeforceCommon plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        initializeAllowedCommands();
    }

    private void initializeAllowedCommands() {
        addAllowedCommand(MODERATING_PLAYER_UUID, "/gamemode");
        addAllowedCommand(MODERATING_PLAYER_UUID, "/tp");
        addAllowedCommand(PINY_ALT_UUID, "/gamemode");
        addAllowedCommand(PINY_ALT_UUID, "/tp");
    }

    private void addAllowedCommand(UUID playerUUID, String commandPrefix) {
        allowedCommandsPerPlayer.computeIfAbsent(playerUUID, k -> new ArrayList<>()).add(commandPrefix);
    }

    private boolean isCommandAllowed(UUID playerUUID, String command) {
        List<String> allowedCommands = allowedCommandsPerPlayer.get(playerUUID);
        if (allowedCommands == null) return false;
        return allowedCommands.stream().anyMatch(command::startsWith);
    }

    private boolean shouldBroadcastCommand(String command, UUID playerUUID) {
        String cmd = command.startsWith("/") ? command.substring(1) : command;
        for (String white : WHITELISTED_COMMANDS) {
            if (cmd.toLowerCase().startsWith(white.toLowerCase())) {
                return false;
            }
        }
        // Per-player exclusions
        if (playerUUID != null && isCommandAllowed(playerUUID, command)) {
            return false;
        }
        return plugin.getConfig().getBoolean("anticheat.broadcastEnabled", true);
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage();
        Player sender = event.getPlayer();
        if (!shouldBroadcastCommand(command, sender.getUniqueId())) return;

        broadcastAndNotify(sender.getName(), command, sender.isOp());
    }

    @EventHandler
    public void onServerCommand(ServerCommandEvent event) {
        if (event.getSender() instanceof BlockCommandSender) return;
        String command = event.getCommand();
        // No per-player exclusion for console
        if (!shouldBroadcastCommand(command, null)) return;

        String sender = event.getSender().getName();
        broadcastAndNotify(sender, command, true);
    }

    private void broadcastAndNotify(String sender, String command, boolean isOp) {
        Bukkit.broadcast(Component.text(sender + " just ran a command: " + command));

        if (!plugin.getConfig().contains("commandbroadcast.webhook", true)) {
            Bukkit.broadcast(Component.text("Warning: Not currently broadcasting commands to discord (webhook not set)").color(NamedTextColor.YELLOW));
            return;
        }

        // Don't currently have the role ids on hand
        plugin.sendDiscordMessage("{\"content\": \"<@&[INSERT ROLE ID HERE]>" + (isOp ? " <@&[INSERT OP ROLE PING HERE]>" : " ")  + "**" + sender + "** ran: `" + command.replace("\"","\\\"") + "`\"}", "commandbroadcast");
    }
}