package com.atlasplugins.atlasenchants.listeners.enchantevents;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds "Circe's Grimoire", a vanilla WRITTEN_BOOK that documents how every custom
 * item in the plugin is obtained, including the live crafting grids for the Altar of
 * Circe and Circe's Anvil.
 * <p>
 * The recipe pages are rendered straight from the crafting rows/materials in
 * enchantments.yml every time the book is created, so an admin who retunes a recipe
 * (then reloads) gets an accurate book with no external wiki required. The book is sold
 * by the Wandering Trader (see {@link WanderingTraderEvent}).
 */
public class CreateRecipeBook {

    private final Main main;

    public CreateRecipeBook(Main main) {
        this.main = main;
    }

    public ItemStack createRecipeBook(int amount, Player p) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();

        String title = main.getEnchantmentsConfig().getString("CircesGrimoire.CircesGrimoire-Title", "Circe's Grimoire");
        String author = main.getEnchantmentsConfig().getString("CircesGrimoire.CircesGrimoire-Author", "Circe, Daughter of Helios");
        String displayName = main.getEnchantmentsConfig().getString("CircesGrimoire.CircesGrimoire-DisplayName", "&f&k*&l &5&lCirce's Grimoire &f&k*");
        meta.setTitle(Main.color(main.setPlaceholders(p, title)));
        meta.setAuthor(Main.color(main.setPlaceholders(p, author)));

        // Match the display-name + lore styling used by the plugin's other custom items.
        meta.setDisplayName(Main.color(main.setPlaceholders(p, displayName)));

        List<String> lore = new ArrayList<>();
        for (String line : main.getEnchantmentsConfig().getStringList("CircesGrimoire.CircesGrimoire-Lore")) {
            lore.add(Main.color(main.setPlaceholders(p, line)));
        }
        if (!lore.isEmpty()) meta.setLore(lore);

        if (main.getEnchantmentsConfig().getBoolean("CircesGrimoire.CircesGrimoire-Glint-Toggle", true)) {
            meta.addEnchant(Enchantment.INFINITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        List<String> pages = new ArrayList<>();

        // --- Cover / lore intro ---
        pages.add(page(
                "&5&lCirce's Grimoire",
                "",
                "&0Bound in phantom-hide,",
                "&0inked with star-fire.",
                "",
                "&0Herein lies the craft of",
                "&0the enchantress Circe,",
                "&0secrets the gods would",
                "&0keep from mortal hands.",
                "",
                "&8Turn the page, and learn."));

        // --- Craftable relics: rendered live from config ---
        Map<Character, String> altarSpecials = new LinkedHashMap<>();
        altarSpecials.put('X', "Oracle of Enchantment");
        altarSpecials.put('Y', "Scrap of Circe");
        addRecipePages(pages, "Altar of Circe", "AltarOfCirce.AltarOfCirce",
                "&0Where mortals beg the gods",
                "&0for fickle, unknown boons.",
                altarSpecials);

        Map<Character, String> anvilSpecials = new LinkedHashMap<>();
        anvilSpecials.put('Y', "Scrap of Circe");
        anvilSpecials.put('Z', "Circe's Ember");
        addRecipePages(pages, "Circe's Anvil", "CircesAnvil.CircesAnvil",
                "&0Forge upon it to bind",
                "&0her weave ever stronger.",
                anvilSpecials);

        // --- Found relics: obtained, not crafted ---
        pages.add(page(
                "&5&lWhispered Relics",
                "",
                "&0Not all of Circe's works",
                "&0are forged by hand. Some",
                "&0are found, for those who",
                "&0wander far enough.",
                "",
                "&8The pages that follow name",
                "&8where each may be sought."));

        pages.add(page(
                "&5&lThe Oracle",
                "&8Oracle of Enchantment",
                "",
                "&0A cryptic tome carried by",
                "&0&owandering merchants&r&0 who",
                "&0traffic in whispers.",
                "",
                "&0Rest it upon a &blectern&0 to",
                "&0read the enchantments it",
                "&0would grant you."));

        pages.add(page(
                "&5&lThe Scrap",
                "&8Scrap of Circe",
                "",
                "&0Dissolve an enchanted tome",
                "&0within a filled &bwater",
                "&bcauldron&0, and reclaim the",
                "&0raw weave left behind.",
                "",
                "&8A reagent for her Altar",
                "&8and her Anvil."));

        pages.add(page(
                "&5&lThe Ember",
                "&8Circe's Ember",
                "",
                "&0A cinder of her eternal",
                "&0hearth, scattered among",
                "&0the &6loot of the world&0.",
                "",
                "&8Seek it in the chests of",
                "&8the forgotten."));

        pages.add(page(
                "&5&lThe Shard",
                "&8Oblivion Shard",
                "",
                "&0A splinter of things the",
                "&0world chose to forget,",
                "&0unearthed in &6loot chests&0.",
                "",
                "&0Press it upon enchanted",
                "&0gear to unmake what binds",
                "&0there."));

        pages.add(page(
                "&5&lThe Enchanted Tomes",
                "&8Enchantment Books",
                "",
                "&0Her enchantments hide in",
                "&0&6worldly loot&0, in the Oracle's",
                "&0knowledge, and upon the",
                "&0Altar's gift.",
                "",
                "&0Drag one onto fitting gear",
                "&0to bind its power there."));

        meta.setPages(pages);
        book.setItemMeta(meta);

        // Give to the player if one was supplied (null = just build the item, e.g. for a trade).
        if (p != null) {
            for (int i = 0; i < amount; i++) {
                HashMap<Integer, ItemStack> remaining = p.getInventory().addItem(book);
                if (!remaining.isEmpty()) {
                    for (ItemStack leftover : remaining.values()) {
                        p.getWorld().dropItemNaturally(p.getLocation(), leftover);
                    }
                }
            }
        }

        return book;
    }

    /**
     * Renders a craftable item across two pages: a flavour + 3x3 grid page, then a
     * reagent legend page. The grid and legend are read live from config.
     */
    private void addRecipePages(List<String> pages, String displayName, String prefix,
                                String flavour1, String flavour2, Map<Character, String> specials) {
        String row1 = pad(main.getEnchantmentsConfig().getString(prefix + "-Crafting-Row-1", "   "));
        String row2 = pad(main.getEnchantmentsConfig().getString(prefix + "-Crafting-Row-2", "   "));
        String row3 = pad(main.getEnchantmentsConfig().getString(prefix + "-Crafting-Row-3", "   "));

        pages.add(page(
                "&5&l" + displayName,
                "",
                flavour1,
                flavour2,
                "",
                gridLine(row1, specials),
                gridLine(row2, specials),
                gridLine(row3, specials),
                "",
                "&8Reagents on the next page."));

        // Legend: one entry per distinct letter used across the three rows, in reading order.
        List<String> legend = new ArrayList<>();
        legend.add("&5&l" + displayName);
        legend.add("&8Reagents");
        legend.add("");
        List<Character> seen = new ArrayList<>();
        for (char c : (row1 + row2 + row3).toCharArray()) {
            if (c == ' ' || seen.contains(c)) continue;
            seen.add(c);
            legend.add(letterColor(c, specials) + c + " &0" + resolveName(prefix, c, specials));
        }
        pages.add(page(legend.toArray(new String[0])));
    }

    /** A grid row like " [A][B][C]", custom-item slots tinted purple, empties shown as a dot. */
    private String gridLine(String row, Map<Character, String> specials) {
        StringBuilder sb = new StringBuilder(" ");
        for (char c : row.toCharArray()) {
            if (c == ' ') {
                sb.append("&8[&7·&8]");
            } else {
                sb.append("&8[").append(letterColor(c, specials)).append(c).append("&8]");
            }
        }
        return sb.toString();
    }

    private String letterColor(char c, Map<Character, String> specials) {
        return specials.containsKey(c) ? "&5&l" : "&0";
    }

    /** Resolve a grid letter to a human name: custom items via the specials map, everything else from config. */
    private String resolveName(String prefix, char c, Map<Character, String> specials) {
        if (specials.containsKey(c)) return specials.get(c);
        String mat = main.getEnchantmentsConfig().getString(prefix + "-Crafting-Materials-" + c);
        if (mat == null) return "Unknown";
        return prettify(mat);
    }

    /** "END_CRYSTAL" -> "End Crystal". */
    private String prettify(String materialName) {
        String[] words = materialName.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (w.isEmpty()) continue;
            sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    /** Guarantee a 3-character row so grid rendering never index-faults on a short config value. */
    private String pad(String row) {
        if (row == null) row = "";
        if (row.length() >= 3) return row.substring(0, 3);
        return (row + "   ").substring(0, 3);
    }

    private String page(String... lines) {
        return Main.color(String.join("\n", lines));
    }
}
