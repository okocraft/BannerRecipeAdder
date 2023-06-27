package net.okocraft.bannerrecipeadder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerTriedToCraftBanner(PrepareItemCraftEvent event) {
        CraftingInventory inv = event.getInventory();
        if (inv.getMatrix() == null) {
            return;
        }

        Set<ItemStack> matrix = new HashSet<>(Arrays.asList(inv.getMatrix()));
        matrix.removeIf(ingredient -> ingredient == null || ingredient.getType().isAir());
        if (matrix.size() != 4) {
            return;
        }

        ItemStack paper = null;
        ItemStack patternItem = null; // vine or bricks
        ItemStack dye = null;
        ItemStack banner = null;

        for (ItemStack ingredient : matrix) {
            if (ingredient.getAmount() >= 2) {
                // ingredients will dupe when result is 2 or more. why?
                return;
            }
            if (ingredient.getType() == Material.PAPER) {
                paper = ingredient.clone();
            } else if (ingredient.getType() == Material.BRICKS || ingredient.getType() == Material.VINE) {
                patternItem = ingredient.clone();
            } else if (ingredient.getType().name().endsWith("_DYE")) {
                dye = ingredient.clone();
            } else if (Tag.BANNERS.isTagged(ingredient.getType())) {
                banner = ingredient.clone();
            }
        }

        if (banner == null || patternItem == null || dye == null || paper == null) {
            return;
        }

        if (!(banner.getItemMeta() instanceof BannerMeta meta) || meta.getPatterns().size() >= 6) {
            return;
        }

        ItemStack result = banner.clone();
        Pattern pattern = new Pattern(
                DyeColor.valueOf(dye.getType().name().replace("_DYE", "")),
                patternItem.getType() == Material.BRICKS ? PatternType.BRICKS : PatternType.CURLY_BORDER
        );
        meta.addPattern(pattern);
        result.setItemMeta(meta);

        inv.setResult(result);
    }
}
