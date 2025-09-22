package dev.piny.lifeforce.common.inventory;

import dev.piny.lifeforce.common.LifeforceCommon;
import dev.piny.pineLib.menus.ChestMenu;
import dev.piny.pineLib.menus.DropperMenu;
import dev.piny.pineLib.menus.MenuItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class InvSeePlayer implements Listener {

    public InvSeePlayer(LifeforceCommon plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onClickPlayer(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player target)) return;
        Player player = event.getPlayer();
        if (!player.hasPermission("lifeforce.invsee")) return;

        ItemStack blankItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        blankItem.editMeta(meta -> meta.itemName(Component.text("")));

        DropperMenu menu = new DropperMenu(Component.text(target.getName() + "'s Inventory"));
        menu.fill(MenuItem.of(blankItem));

        ItemStack enderChestIcon = new ItemStack(Material.ENDER_CHEST);
        enderChestIcon.editMeta(meta -> meta.itemName(Component.text("Ender Chest")));

        ItemStack inventoryIcon = new ItemStack(Material.PLAYER_HEAD);
        inventoryIcon.editMeta(meta -> meta.itemName(Component.text("Inventory")));

        menu.set(3, MenuItem.of(enderChestIcon, clickEvent -> {
            player.closeInventory();
            ChestMenu view = new ChestMenu(Component.text(target.getName() + "'s Ender Chest"), 3);
            Inventory enderChest = target.getEnderChest();
            for (int i = 0; i < enderChest.getSize(); i++) {
                ItemStack item = enderChest.getItem(i);
                if (item != null) {
                    view.set(i, MenuItem.of(item.clone()));
                }
            }
            view.show(player);
        }));

        menu.set(5, MenuItem.of(inventoryIcon, clickEvent -> {
            player.closeInventory();
            ChestMenu view = new ChestMenu(Component.text(target.getName() + "'s Inventory"), 5);
            PlayerInventory inv = target.getInventory();
            // main inventory (0-35)
            for (int i = 0; i < 36; i++) {
                ItemStack item = inv.getItem(i);
                if (item != null) {
                    view.set(i, MenuItem.of(item.clone()));
                }
            }

            int r = 9 * 4;
            view.fill(MenuItem.of(blankItem), false, r, r + 8);
            view.set(r,     MenuItem.of(inv.getHelmet()        != null ? inv.getHelmet().clone()        : new ItemStack(Material.AIR)));
            view.set(r + 1, MenuItem.of(inv.getChestplate()    != null ? inv.getChestplate().clone()    : new ItemStack(Material.AIR)));
            view.set(r + 2, MenuItem.of(inv.getLeggings()      != null ? inv.getLeggings().clone()      : new ItemStack(Material.AIR)));
            view.set(r + 3, MenuItem.of(inv.getBoots()         != null ? inv.getBoots().clone()         : new ItemStack(Material.AIR)));
            view.set(r + 8, MenuItem.of(inv.getItemInOffHand().clone()));

            view.show(player);
        }));

        menu.show(player);
    }
}