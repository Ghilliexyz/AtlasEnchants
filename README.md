# Atlas Custom Enchants

Atlas Custom Enchants is a Minecraft Spigot plugin that adds 21 custom enchantments with a tiered rarity system, special items, crafting stations, and a full GUI-based browsing experience. Originally developed for a private Survival Multiplayer (SMP) server, it bridges the gap between vanilla gameplay and RPG-style progression.

- **Version:** 1.3.5
- **Platform:** Spigot 1.20.6+
- **Java:** 21
- **Authors:** _Ghillie & Helix
- **Discord:** https://discord.gg/f9swPR7NnV

---

## Table of Contents

- [Features](#features)
- [Installation](#installation)
- [Rarity Tiers](#rarity-tiers)
- [Enchantments](#enchantments)
  - [Divine](#divine)
  - [Godly](#godly)
  - [Legendary](#legendary)
  - [Epic](#epic)
  - [Rare](#rare)
  - [Trash](#trash)
- [Special Items](#special-items)
  - [Oblivion Shard](#oblivion-shard)
  - [Oracle of Enchantment](#oracle-of-enchantment)
  - [Scrap of Circe's Weave](#scrap-of-circes-weave)
  - [Circe's Ember](#circes-ember)
  - [Altar of Circe](#altar-of-circe)
  - [Circe's Anvil](#circes-anvil)
- [Applying Enchantments](#applying-enchantments)
- [Upgrading Enchantments](#upgrading-enchantments)
- [Commands](#commands)
  - [Player Commands](#player-commands)
  - [Admin Commands](#admin-commands)
- [Permissions](#permissions)
- [Configuration](#configuration)
  - [enchantments.yml](#enchantmentsyml)
  - [settings.yml](#settingsyml)
  - [menus.yml](#menusyml)
- [Optional Integrations](#optional-integrations)
- [Building from Source](#building-from-source)

---

## Features

- 21 custom enchantments across weapons, armor, tools, and fishing rods
- 6 rarity tiers (Divine, Godly, Legendary, Epic, Rare, Trash) with configurable drop weights
- Special items with unique mechanics: Oblivion Shard, Oracle of Enchantment, Altar of Circe, Scrap of Circe's Weave, Circe's Ember, and Circe's Anvil
- Full GUI system for browsing enchantments, viewing guides, and upgrading enchant books
- Enchantment upgrade system that converts 10 books of the same rarity into 1 book of the next tier
- Craftable special items with custom shaped recipes
- Anti-exploit protection that tracks player-placed blocks to prevent abuse with mining enchants
- WorldGuard integration for region protection checks
- PlaceholderAPI support for message customization
- Every message, sound, color, and stat is fully configurable through YAML files

---

## Installation

1. Download the AtlasEnchants JAR file.
2. Place it in your server's `plugins/` folder.
3. Start or restart the server.
4. The plugin will generate three configuration files in `plugins/AtlasEnchants/`:
   - `enchantments.yml`
   - `settings.yml`
   - `menus.yml`
5. Edit the configuration files to suit your server, then run `/ae reload`.

---

## Rarity Tiers

Enchantments are organized into six tiers. When a custom enchantment book is found or generated, the tier is selected based on the configured drop weights.

| Tier | Color | Default Drop Weight |
| :--- | :--- | :--- |
| Divine | Dark Red | 1% |
| Godly | Red | 15% |
| Legendary | Gold | 20% |
| Epic | Pink | 25% |
| Rare | Cyan | 40% |
| Trash | Dark Gray | 0% (crafting only) |

Drop weights are configurable in `settings.yml`. The Trash tier has a 0% natural drop rate and can only be obtained through the Altar of Circe.

---

## Enchantments

### Divine

#### Poseidon's Bait
- **Applies to:** Fishing Rod
- **Max Level:** 3
- **Effect:** Grants a chance to find custom enchantment books while fishing. The chance increases per level: 1% at level 1, 2% at level 2, 3% at level 3.

---

### Godly

#### Fearsight
- **Applies to:** All Helmets (Leather, Chainmail, Iron, Golden, Turtle Shell, Diamond, Netherite)
- **Max Level:** 3
- **Effect:** While wearing the helmet, nearby entities glow through walls. Hostile mobs glow red, passive mobs glow green, and players glow white. The detection radius increases per level: 5 blocks at level 1, 10 blocks at level 2, 15 blocks at level 3.

#### Blessing of Knowledge
- **Applies to:** All Helmets
- **Max Level:** 1
- **Effect:** Displays a color-coded health bar above any mob you hit. The health bar is visible for 5 seconds and changes color based on the mob's remaining health percentage (green at full, red at low).

#### Miner's Touch
- **Applies to:** All Pickaxes
- **Max Level:** 1
- **Blacklist:** Safe Miner
- **Effect:** Allows you to mine and collect Mob Spawners. The spawner retains its mob type when picked up and can be placed elsewhere.

#### Asclepius
- **Applies to:** All Chestplates
- **Max Level:** 2
- **Effect:** Grants additional permanent heart containers while the chestplate is equipped. 1 extra heart at level 1, 2 extra hearts at level 2.

#### Leech
- **Applies to:** All Swords
- **Max Level:** 3
- **Blacklist:** Hunter
- **Effect:** Converts a percentage of damage dealt into healing for the player. 5% at level 1, 15% at level 2, 30% at level 3.

---

### Legendary

#### Safe Miner
- **Applies to:** All Pickaxes, Axes, and Shovels
- **Max Level:** 1
- **Blacklist:** Miner's Touch, Vein Seeker, Tree Hugger
- **Effect:** Teleports mined block drops directly into your inventory. If your inventory is full, items drop on the ground as normal. Handles multi-block structures like doors, beds, and tall flowers.

#### Extractor
- **Applies to:** All Swords
- **Max Level:** 2
- **Effect:** Multiplies experience dropped from mob kills. 1.5x XP at level 1, 2x XP at level 2. Produces particle effects on kill.

#### Vein Seeker
- **Applies to:** All Pickaxes
- **Max Level:** 1
- **Blacklist:** Miner's Touch, Safe Miner, Tree Hugger
- **Effect:** Instantly mines an entire ore vein when you break one ore block. Scans up to 256 connected blocks of the same type. Each mined block consumes durability from the pickaxe. Player-placed ores are tracked and excluded to prevent exploitation.

#### Wings of Aegis
- **Applies to:** All Chestplates
- **Max Level:** 1
- **Effect:** Provides passive damage reduction against incoming attacks using bonus armor and toughness values.

#### Final Guard
- **Applies to:** All Armor, Shields, and Tools
- **Max Level:** 2
- **Effect:** When an item is about to break from durability loss, there is a chance it will be repaired instead of breaking. 5% chance at level 1, 10% chance at level 2.

#### Rush
- **Applies to:** All Leggings
- **Max Level:** 2
- **Effect:** When you take damage, you receive a temporary Speed boost. The speed level and duration scale with enchantment level.

#### Stunning
- **Applies to:** All Swords
- **Max Level:** 2
- **Effect:** Chance to apply Slowness and Weakness to hit entities, temporarily immobilizing them. Produces particle effects on activation.

#### Energy Absorption
- **Applies to:** All Shields
- **Max Level:** 2
- **Effect:** When blocking with a shield while at low health, heals 1 heart at level 1 or 2 hearts at level 2.

---

### Epic

#### Poison Aspect
- **Applies to:** All Swords
- **Max Level:** 2
- **Effect:** Applies Poison to hit entities. Poison level and duration increase with enchantment level.

#### Ice Aspect
- **Applies to:** All Swords
- **Max Level:** 2
- **Effect:** Freezes hit entities in place. Freeze duration increases per level: 1 second at level 1, 2 seconds at level 2. Produces frost particle effects.

#### Tree Hugger
- **Applies to:** All Axes
- **Max Level:** 1
- **Blacklist:** Safe Miner
- **Effect:** Chops down entire trees in a single swing. Scans up to 256 connected log blocks and breaks them all at once. Each block consumes durability from the axe. Player-placed logs are tracked and excluded to prevent exploitation.

---

### Rare

#### Hunter
- **Applies to:** All Swords, Bow, Crossbow
- **Max Level:** 3
- **Blacklist:** Leech
- **Effect:** Deals bonus damage to passive mobs. 1x bonus at level 1, 1.5x bonus at level 2, 2x bonus at level 3.

#### Propel
- **Applies to:** All Swords
- **Max Level:** 2
- **Effect:** Launches hit entities upward. 3 blocks at level 1, 5 blocks at level 2.

#### Freezing Shot
- **Applies to:** Bow, Crossbow
- **Max Level:** 3
- **Effect:** Arrows freeze hit entities in place. Freeze duration increases per level: 1 second at level 1, 1.5 seconds at level 2, 2 seconds at level 3.

#### Regrowth
- **Applies to:** All Hoes
- **Max Level:** 1
- **Effect:** Harvesting crops automatically replants them. The crop is broken and drops are collected, but the block is replanted at growth stage 0.

#### Decapitate
- **Applies to:** All Swords, All Axes
- **Max Level:** 3
- **Effect:** Chance to drop a mob or player head on kill. 1% chance at level 1, 3% chance at level 2, 5% chance at level 3. Mob heads use custom textures for each mob type. Player heads drop with the correct skin.

---

### Trash

#### Tainted Book
- **Max Level:** 1
- **Effect:** A failed enchantment result with no functional effect. Obtained only through the Altar of Circe. Can be used in recycling or discarded.

---

## Special Items

### Oblivion Shard

A Prismarine Shard item that removes a random custom enchantment from equipment.

- **How to obtain:** 5% chance to appear in loot containers (chests, barrels, etc.)
- **How to use:** Drag and drop the shard onto an enchanted item in your inventory.
- **Effect:** Removes one random custom enchantment from the item. There is a 50% chance the removed enchantment is returned to you as a book.

### Oracle of Enchantment

A Written Book item that serves as the primary way for players to discover enchantments.

- **How to obtain:** 30% chance to appear in Wandering Trader trades for 32 Emeralds. Can also be given via admin command.
- **How to use:** Place on a Lectern and right-click to open the enchantment browsing GUI.
- **Effect:** Opens a rarity selection menu where you can browse all available enchantments organized by tier.

### Scrap of Circe's Weave

A Prismarine Crystals item used as a crafting material for the Altar of Circe.

- **How to obtain:** Wash an enchanted item in a Water Cauldron by right-clicking the cauldron with the item. The enchanted item is destroyed and you receive scraps.
- **How to use:** Used as a crafting ingredient in the Altar of Circe and Circe's Anvil recipes.

### Circe's Ember

An Amethyst Shard item used as a crafting material for Circe's Anvil.

- **How to obtain:** Found in loot containers (chests, barrels, etc.).
- **How to use:** Used as a crafting ingredient in the Circe's Anvil recipe.

### Altar of Circe

An Enchanting Table item that functions as a custom crafting station for generating enchantment books.

- **How to craft:**

```
End Crystal     | Netherite Ingot | End Crystal
Scrap of Circe  | Oracle Book     | Scrap of Circe
Echo Shard      | Echo Shard      | Echo Shard
```

- **How to use:** Place the Altar and right-click it to open the enchantment station GUI. Place a weapon or tool in the slot and the Altar will randomly apply a custom enchantment to it based on the rarity drop weights.
- **Can also be given via admin command.**

### Circe's Anvil

An Anvil item that functions as an enchantment upgrade station.

- **How to craft:**

```
Netherite Ingot | Netherite Ingot | Netherite Ingot
Circe's Ember   | Scrap of Circe  | Circe's Ember
Obsidian        | Anvil           | Obsidian
```

- **How to use:** Place the Anvil and right-click it to open the upgrade GUI. Place 10 enchantment books of the same rarity tier into the slots, then click the upgrade button to receive 1 enchantment book of the next rarity tier.
- **Can also be given via admin command.**

---

## Applying Enchantments

Custom enchantment books are applied to items by dragging and dropping in your inventory:

1. Open your inventory.
2. Pick up the enchantment book.
3. Click the book onto the target item (weapon, armor, or tool).
4. If the enchantment is compatible with the item, it will be applied and appear in the item's lore.

Enchantments cannot be applied to items they are not designed for. Each enchantment lists which item types it supports. Some enchantments also have blacklists, meaning they cannot coexist on the same item with certain other enchantments.

---

## Upgrading Enchantments

Enchantment books can be upgraded to a higher rarity tier using Circe's Anvil:

1. Obtain or craft a Circe's Anvil and place it.
2. Right-click the Anvil to open the upgrade GUI.
3. Place 10 enchantment books of the same rarity into the upgrade slots.
4. Click the upgrade button.
5. You will receive 1 enchantment book of the next rarity tier.

The upgrade can also be done through the `/ae upgrade` command, which opens the same interface.

---

## Commands

The base command is `/atlasenchants` with aliases `/aenchants` and `/ae`. All functionality is accessed through subcommands.

### Player Commands

| Command | Permission | Description |
| :--- | :--- | :--- |
| `/ae guide` | `atlasenchants.guide` | Opens the Plugin Guide GUI showing all enchantment types, special items, mechanics, and crafting recipes. |
| `/ae list` | `atlasenchants.enchantlist` | Opens the Enchant List GUI where you can browse all enchantments filtered by rarity tier. |
| `/ae upgrade` | `atlasenchants.upgrade` | Opens the Upgrade Menu where you can combine 10 books of the same rarity into 1 book of the next tier. |
| `/ae help` | `atlasenchants.help` | Displays all available commands. Shows admin commands only to players with the help permission. |

### Admin Commands

| Command | Permission | Description |
| :--- | :--- | :--- |
| `/ae reload` | `atlasenchants.reloadconfig` | Reloads all configuration files (enchantments.yml, settings.yml, menus.yml). |
| `/ae giveenchant <player> <enchant> <level> <amount>` | `atlasenchants.giveenchant` | Gives a specific enchantment book to a player. Level must be between 1 and the enchantment's max level. Amount can be 1-64. |
| `/ae giverandom <player> <amount>` | `atlasenchants.giverandomenchant` | Gives a random enchantment book to a player based on rarity drop weights. Amount can be 1-64. |
| `/ae giveshard <player> <amount>` | `atlasenchants.giveshard` | Gives Oblivion Shard(s) to a player. |
| `/ae giveoraclebook <player> <amount>` | `atlasenchants.giveoraclebook` | Gives Oracle of Enchantment book(s) to a player. |
| `/ae givealtarofcirce <player> <amount>` | `atlasenchants.givealtarofcirce` | Gives Altar of Circe item(s) to a player. |
| `/ae givescrapofcirce <player> <amount>` | `atlasenchants.givescrapofcirce` | Gives Scrap of Circe's Weave item(s) to a player. |
| `/ae givecircesember <player> <amount>` | `atlasenchants.givecircesember` | Gives Circe's Ember item(s) to a player. |
| `/ae givecircesanvil <player> <amount>` | `atlasenchants.givecircesanvil` | Gives Circe's Anvil item(s) to a player. |

---

## Permissions

All permissions default to `false` (OP bypasses all). The `atlasenchants.guide` permission defaults to `true`.

| Permission | Description |
| :--- | :--- |
| `atlasenchants.help` | View admin commands in the help menu. |
| `atlasenchants.reloadconfig` | Use the reload command. |
| `atlasenchants.giveenchant` | Use the give enchant command. |
| `atlasenchants.giverandomenchant` | Use the give random enchant command. |
| `atlasenchants.giveshard` | Use the give shard command. |
| `atlasenchants.giveoraclebook` | Use the give oracle book command. |
| `atlasenchants.givealtarofcirce` | Use the give Altar of Circe command. |
| `atlasenchants.givescrapofcirce` | Use the give Scrap of Circe's Weave command. |
| `atlasenchants.upgrade` | Use the upgrade command. |
| `atlasenchants.enchantlist` | Use the enchant list command. |
| `atlasenchants.enchantlistgrabber` | Grab any enchant book directly from the enchant list menu. |
| `atlasenchants.guide` | Use the guide command. Defaults to `true`. |
| `atlasenchants.givecircesember` | Use the give Circe's Ember command. |
| `atlasenchants.givecircesanvil` | Use the give Circe's Anvil command. |

---

## Configuration

The plugin generates three YAML configuration files in the `plugins/AtlasEnchants/` folder. All changes take effect after running `/ae reload`.

### enchantments.yml

Controls all enchantment definitions and special item settings. For each enchantment, you can configure:

- **Enabled** - Toggle the enchantment on or off.
- **Max Level** - The maximum level the enchantment can reach (affects book generation and upgrade limits).
- **Rarity** - Which rarity tier the enchantment belongs to (DIVINE, GODLY, LEGENDARY, EPIC, RARE, TRASH).
- **Title** - The display name shown on the enchantment book item.
- **Lore** - The description lines shown on the enchantment book, with placeholder support for level-specific stats.
- **Applicable Items** - The list of item materials the enchantment can be applied to.
- **Blacklist** - Other enchantments that cannot coexist on the same item.
- **Effect-specific settings** - Proc chances, durations, damage values, radius sizes, and other stats that vary per enchantment and per level.

This file also contains configuration for all special items: Oblivion Shard (spawn chance, return chance), Oracle of Enchantment (trader spawn chance, cost), Altar of Circe (crafting recipe, enabled state), Scrap of Circe's Weave (proc chances per level), Circe's Ember (spawn settings), and Circe's Anvil (crafting recipe).

### settings.yml

Controls messages, sounds, and global plugin settings:

- **Header/Footer** - Prefix and suffix for all plugin messages.
- **Command Messages** - Usage text, error messages, and success messages for every command.
- **Help Messages** - Separate help text for players and administrators.
- **Enchant Spawn Chance** - The global chance for custom enchantment books to appear (default: 10%).
- **Rarity Weights** - The percentage chance for each rarity tier when a book is generated.
- **Upgrade Settings** - Number of books required per upgrade (default: 10).
- **Sound Effects** - Configurable sounds for enchantment application, upgrades, shard usage, and other actions.
- **Message Toggles** - Enable or disable specific notification messages.

### menus.yml

Controls all GUI layouts and visual settings:

- **Upgrade Enchant GUI** - Slot positions, button materials, titles, and layout for the upgrade menu.
- **Enchant List GUI** - Rarity selection buttons, per-rarity browsing layout, and back button configuration.
- **Guide GUI** - Information pages for enchant books, special items, mechanics, and crafting recipes.
- **Recipe Display Menus** - Visual crafting recipe layouts for the Altar of Circe and Circe's Anvil.
- **Filler Items** - Background item materials and display names for empty GUI slots.

---

## Optional Integrations

### PlaceholderAPI

If PlaceholderAPI is installed, the plugin will register custom placeholders that can be used in messages and lore text throughout the configuration files.

### WorldGuard

If WorldGuard is installed, the plugin respects region protections. Enchantments like Vein Seeker, Tree Hugger, and Safe Miner will check region ownership and membership before allowing block breaking, preventing players from using enchantments to break blocks in regions they do not have access to.

---

## Building from Source

Requires Java 21 and Maven.

```bash
mvn clean package
```

The output JAR is placed in the `target/` directory. The Maven Shade plugin bundles all dependencies (GlowingEntities, Jackson) into the final JAR.

---

## License

This project is licensed under the [PolyForm Noncommercial License 1.0.0](LICENSE). You are free to view, fork, and modify the code for any **noncommercial** purpose. Commercial use of this software or derivative works is not permitted.
