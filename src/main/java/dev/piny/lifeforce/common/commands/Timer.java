package dev.piny.lifeforce.common.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.arguments.TextArgument;
import dev.piny.lifeforce.common.LifeforceCommon;
import dev.piny.pineLib.tasks.countdown.BukkitBossBarCountdown;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.Map;

public class Timer {

    public Timer() {}

    public CommandAPICommand command(LifeforceCommon plugin) {
        return new CommandAPICommand("timer")
                .withArguments(
                        new IntegerArgument("seconds", 1, 6 * 3600),
                        new TextArgument("title"),
                        new StringArgument("colour").replaceSuggestions(
                                ArgumentSuggestions.strings("red","blue","green","purple","yellow","white","pink")
                        )
                )
                .executes((sender, args) -> {
                    int seconds = (int) args.get("seconds");
                    String title = (String) args.get("title");
                    String colour = (String) args.get("colour");

                    Map<String, BarColor> colourMap = Map.of(
                            "red", BarColor.RED,
                            "blue", BarColor.BLUE,
                            "green", BarColor.GREEN,
                            "purple", BarColor.PURPLE,
                            "yellow", BarColor.YELLOW,
                            "white", BarColor.WHITE,
                            "pink", BarColor.PINK
                    );

                    BossBar bar = Bukkit.createBossBar(title, colourMap.getOrDefault(colour, BarColor.WHITE), BarStyle.SEGMENTED_20);
                    bar.setVisible(true);
                    bar.setProgress(1.0);

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        bar.addPlayer(player);
                    }

                    // Use PineLib countdown (max and interval are in ticks)
                    new BukkitBossBarCountdown(bar, seconds * 20, 1, true);

                    sender.sendMessage("Timer started for " + seconds + " seconds.");
                });
    }
}