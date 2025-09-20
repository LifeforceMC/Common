package dev.piny.lifeforce.common;

import dev.piny.lifeforce.common.anticheat.AntiCheat;
import dev.piny.lifeforce.common.commands.Commands;
import dev.piny.lifeforce.common.function.FunctionReplacer;
import dev.piny.lifeforce.common.inventory.InvSeePlayer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.http.HttpClient;

public final class LifeforceCommon extends JavaPlugin {

    private static LifeforceCommon instance;

    @Override
    public void onEnable() {
        instance = this;

        FileConfiguration config = getConfig();
        // Webhook + feature defaults
        config.addDefault("commandbroadcast.webhook", "");
        config.addDefault("channels.mc-link.webhook", "");
        config.addDefault("anticheat.broadcastEnabled", true);
        config.options().copyDefaults(true);
        saveConfig();

        // Register core systems
        new AntiCheat(this);
        new Commands(this);
        new InvSeePlayer(this);
        new FunctionReplacer(this); // stub

        sendDiscordMessage("{\"content\": \"LifeforceCommon enabled.\"}", "commandbroadcast");
        getLogger().info("LifeforceCommon has been enabled.");
    }

    @Override
    public void onDisable() {
        sendDiscordMessage("{\"content\": \"LifeforceCommon disabled.\"}", "commandbroadcast");
        getLogger().info("LifeforceCommon has been disabled.");
        instance = null;
    }

    public static LifeforceCommon getInstance() {
        return instance;
    }

    public void sendDiscordMessage(String messagePayload, String webhookIdentifier) {
        String webhookUrl = getConfig().getString("%s.webhook".formatted(webhookIdentifier));
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            getLogger().fine("Webhook URL for %s is not set.".formatted(webhookIdentifier));
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                var request = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(webhookUrl))
                        .header("Content-Type", "application/json")
                        .POST(java.net.http.HttpRequest.BodyPublishers.ofString(messagePayload))
                        .build();
                var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 204) {
                    getLogger().severe("Failed to send webhook: " + response.body());
                } else {
                    getLogger().info("Webhook sent successfully.");
                }
            } catch (Exception e) {
                getLogger().severe("Error sending webhook: " + e.getMessage());
            }
        });
    }
}