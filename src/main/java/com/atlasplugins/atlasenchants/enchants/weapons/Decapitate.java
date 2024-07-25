package com.atlasplugins.atlasenchants.enchants.weapons;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Decapitate implements Listener {

    private Main main;
    public Decapitate (Main main) {
        this.main = main;
    }

    private final Random random = new Random();

    public boolean hasWeapon (Player p)
    {
        // Get the player's sword item
        ItemStack weapon = p.getInventory().getItemInMainHand();

        // Get the list of items the Enchant can be applied to from the config
        List<String> weaponMat = main.getEnchantmentsConfig().getStringList("Enchantments.DECAPITATE.Enchantment-Apply-Item");

        // Check if the player is wearing an applicable sword
        return weapon != null && weaponMat.contains(weapon.getType().toString());
    }

    @EventHandler
    public void onEntityDeathEvent(EntityDeathEvent e)
    {
        Player p = (Player) e.getEntity().getKiller();

        if(p == null){return;}

        // Check if the player has an enchanted sword
        if(hasWeapon(p)) {

            // Get Enchantment Enabled Status
            boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.DECAPITATE.Enchantment-Enabled");
            // if Enchantment Enabled = false return.
            if(!isEnchantmentEnabled) return;

            PersistentDataContainer enchantedItemPDC = p.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer();
            String enchantedItemData = enchantedItemPDC.get(Main.customEnchantKeys, PersistentDataType.STRING);

            // Ensure the enchantment data is not null or empty
            if (enchantedItemData != null && !enchantedItemData.isEmpty()) {
                String[] enchantments = enchantedItemData.split(",");

                for (String enchantment : enchantments) {
                    String[] enchantParts = enchantment.split(":");

                    // Ensure the format is correct
                    if (enchantParts.length == 2)
                    {
                        String enchantName = enchantParts[0];
                        int enchantLevel = Integer.parseInt(enchantParts[1]);

                        if (enchantName.contains("DECAPITATE")) {
                            //PUT ENCHANT LOGIC HERE
                            double decapitateProcChance = main.getEnchantmentsConfig().getDouble("Enchantments.DECAPITATE.Decapitate-Proc-Chance-" + enchantLevel);

                            if (random.nextDouble() < decapitateProcChance) {

                                Entity deadEntity = e.getEntity();
                                EntityType entityType = deadEntity.getType();
                                List<ItemStack> drops = e.getDrops();


                                if(deadEntity instanceof Player){
                                    drops.add(CreatePlayerHead(entityType, (OfflinePlayer) deadEntity));
                                }else if(deadEntity instanceof Mob) {
                                    drops.add(CreateMobHead(deadEntity, entityType, p));
                                }

                                // Get the location of the dead entity
                                Location entityLoc = deadEntity.getLocation();

                                // Particle Settings Controlled Via Config
                                // Get the bool to see if the user wants to display the particles
                                boolean useParticles = main.getEnchantmentsConfig().getBoolean("Enchantments.DECAPITATE.Decapitate-Particle-Settings.Decapitate-Particle-Toggle");
                                // Get the Particle 1 Name
                                Particle particle1Name = Particle.valueOf(main.getEnchantmentsConfig().getString("Enchantments.DECAPITATE.Decapitate-Particle-Settings.Decapitate-Particle-1.Decapitate-Particle-Name-1"));
                                // Get the Particle 1 Amount
                                int particle1Amount = main.getEnchantmentsConfig().getInt("Enchantments.DECAPITATE.Decapitate-Particle-Settings.Decapitate-Particle-1.Decapitate-Particle-Amount-1");
                                // Get the Particle 1 Size
                                float particle1Size = (float) main.getEnchantmentsConfig().getDouble("Enchantments.DECAPITATE.Decapitate-Particle-Settings.Decapitate-Particle-1.Decapitate-Particle-Size-1");

                                if (useParticles) {
                                    // Spawn particle effect
                                    e.getEntity().getWorld().spawnParticle(particle1Name, entityLoc, particle1Amount, 1, 1, 1, particle1Size);
                                }
                            }
                            //END ENCHANT LOGIC
                        }
                    }
                }
            }
        }
    }

    private ItemStack CreatePlayerHead(EntityType entityType, OfflinePlayer player) {
        if (entityType.equals(EntityType.PLAYER)) {
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta playerHeadMeta = (SkullMeta) playerHead.getItemMeta();

            assert playerHeadMeta != null;
            playerHeadMeta.setOwningPlayer(player);
            playerHeadMeta.setDisplayName("SKULL OF " + player.getName());

            playerHead.setItemMeta(playerHeadMeta);
            return playerHead;
        }else{
            return null;
        }
    }

    private ItemStack CreateMobHead(Entity entity, EntityType entityType, Player p) {
        ItemStack mobHead = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta skullMeta = (SkullMeta) mobHead.getItemMeta();

        // Reformat spawner name
        String headNameReformatted = reformatString(entityType.name());

        // Set Spawners Name
        String displayName = main.getEnchantmentsConfig().getString("Enchantments.DECAPITATE.Decapitate-Head-Title-Style");
        String withPAPISet = main.setPlaceholders(p, displayName);

        switch (entityType) {
            case ALLAY:
                PlayerProfile allayProfile = getProfile("https://textures.minecraft.net/texture/e50294a1747310f104124c6373cc639b712baa57b7d926297b645188b7bb9ab9", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(allayProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case ARMADILLO:
                PlayerProfile armadilloProfile = getProfile("https://textures.minecraft.net/texture/e78833844319909f1238405015d8740ae96c6fb3fa4c739e1cdfc379cd7e9a6", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(armadilloProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case AXOLOTL:
                Axolotl axolotl = (Axolotl) entity;
                Axolotl.Variant axolotlType = axolotl.getVariant();
                PlayerProfile axolotlProfile = null;
                if(axolotlType.equals(Axolotl.Variant.LUCY))
                {
                    axolotlProfile = getProfile("https://textures.minecraft.net/texture/debf2b113f4a81370a1cf9d2504e8756b66deef79e9433187da774b96c9f35ba", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(axolotlType.equals(Axolotl.Variant.WILD))
                {
                    axolotlProfile = getProfile("https://textures.minecraft.net/texture/5461c4f2cb1eda8e14cd59efb0bf89ea2076f54a08b8b6d0db0bd0797d99bee8", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(axolotlType.equals(Axolotl.Variant.GOLD))
                {
                    axolotlProfile = getProfile("https://textures.minecraft.net/texture/3a6534c32ab856028558478115d904d52f5f154d80ecb903b866b19099a25e46", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(axolotlType.equals(Axolotl.Variant.CYAN))
                {
                    axolotlProfile = getProfile("https://textures.minecraft.net/texture/1e8c70e6a56161ecdc6b75674070fb308714a2b2b70e1e6d76bc920ce6ce7de6", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(axolotlType.equals(Axolotl.Variant.BLUE))
                {
                    axolotlProfile = getProfile("https://textures.minecraft.net/texture/357b32c2933bebcd6502cbf406f7df996c8e46e7c6cbfefc5dcdc2fbf5bb54bb", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                assert skullMeta != null;
                skullMeta.setOwnerProfile(axolotlProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case BAT:
                PlayerProfile batProfile = getProfile("https://textures.minecraft.net/texture/6681a72da7263ca9aef066542ecca7a180c40e328c0463fcb114cb3b83057552", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(batProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case CAMEL:
                PlayerProfile camelProfile = getProfile("https://textures.minecraft.net/texture/8d4c32449ad23060c3f41ba9d27cd51ec2eece63bcff304ae44075043a2ec440", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(camelProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case CAT:
                Cat cat = (Cat) entity;
                Cat.Type catType = cat.getCatType();
                PlayerProfile catProfile = null;
                if(catType.equals(Cat.Type.ALL_BLACK))
                {
                    catProfile = getProfile("https://textures.minecraft.net/texture/593d90acc60c53a72531c7ca652b3f117699f6a9c206c391b23b9f7ebaee540a", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(catType.equals(Cat.Type.BRITISH_SHORTHAIR))
                {
                    catProfile = getProfile("https://textures.minecraft.net/texture/40d9a08f05c8df6b0f376f5e6e63d01c93f4a82ad7ce2ccef1b08674ee0e7336", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(catType.equals(Cat.Type.CALICO))
                {
                    catProfile = getProfile("https://textures.minecraft.net/texture/f3fa560da53b0ffa92c735ebe049a56235523b9cb5cd10269df902cc2052ae22", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(catType.equals(Cat.Type.JELLIE))
                {
                    catProfile = getProfile("https://textures.minecraft.net/texture/ee003c35ada30c60aa782643714b547e474a670ba427071221e7145075d08470", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(catType.equals(Cat.Type.PERSIAN))
                {
                    catProfile = getProfile("https://textures.minecraft.net/texture/918b1ff36d761c449340a682e785b5c45355df40aa8cfc52bb1dfbd542bb7686", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(catType.equals(Cat.Type.RAGDOLL))
                {
                    catProfile = getProfile("https://textures.minecraft.net/texture/667392f784a988b989a7544622000831373ed2ab5afc44a4ed4ea3e641927614", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(catType.equals(Cat.Type.RED))
                {
                    catProfile = getProfile("https://textures.minecraft.net/texture/2fee8f943b7f45d73b2ee58987cabfd49d5ea9153c0081d7a999ab7d617ac333", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(catType.equals(Cat.Type.SIAMESE))
                {
                    catProfile = getProfile("https://textures.minecraft.net/texture/6e911e24c8553fead5a2f0a0ec59c4af62f126ae70d6bed261aed4f99c14b492", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(catType.equals(Cat.Type.TABBY))
                {
                    catProfile = getProfile("https://textures.minecraft.net/texture/1210fb0dea505d06e2636cad046c6e27d8e4998a7529eb6add2f2913022d3d59", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(catType.equals(Cat.Type.BLACK))
                {
                    catProfile = getProfile("https://textures.minecraft.net/texture/9eba11c0a7d9088ebe2c659f191243e408ec94cb0ecabedbb41aa19f2b8d9023", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(catType.equals(Cat.Type.WHITE))
                {
                    catProfile = getProfile("https://textures.minecraft.net/texture/5e3bbae5aad148624c602b680ef007f015f24e9b94a26384e5c6635e5fea4389", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                assert skullMeta != null;
                skullMeta.setOwnerProfile(catProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case CHICKEN:
                PlayerProfile chickenProfile = getProfile("https://textures.minecraft.net/texture/3ad3dd0083faa69a062f9ad81418f5a596180bf1592e4b8d1303b230b64bc79e", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(chickenProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case COD:
                PlayerProfile codProfile = getProfile("https://textures.minecraft.net/texture/7892d7dd6aadf35f86da27fb63da4edda211df96d2829f691462a4fb1cab0", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(codProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case COW:
                PlayerProfile cowProfile = getProfile("https://textures.minecraft.net/texture/dc4b5f6d75126380f520a67ca57bc9a56aa11db8afe7e5dcb2a52dfcfeae0785", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(cowProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case DONKEY:
                PlayerProfile donkeyProfile = getProfile("https://textures.minecraft.net/texture/399bb50d1a214c394917e25bb3f2e20698bf98ca703e4cc08b42462df309d6e6", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(donkeyProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case FROG:
                Frog frog = (Frog) entity;
                Frog.Variant frogType = frog.getVariant();
                PlayerProfile frogProfile = null;
                if(frogType.equals(Frog.Variant.TEMPERATE))
                {
                    frogProfile = getProfile("https://textures.minecraft.net/texture/2ca4a8e494582c62aaa2c92474b16d69cd63baa3d3f50a4b631d6559ca0f33f5", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(frogType.equals(Frog.Variant.WARM))
                {
                    frogProfile = getProfile("https://textures.minecraft.net/texture/4e86332bdd8567e39605f0d11bae2e45e8603a6d71da06208d81aa3e7e3971cd", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(frogType.equals(Frog.Variant.COLD))
                {
                    frogProfile = getProfile("https://textures.minecraft.net/texture/45852a95928897746012988fbd5dbaa1b70b7a5fb65157016f4ff3f245374c08", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                assert skullMeta != null;
                skullMeta.setOwnerProfile(frogProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case GLOW_SQUID:
                PlayerProfile glowSquidProfile = getProfile("https://textures.minecraft.net/texture/45c999dd12dd1c866fdd0ee94a3973533428cd72d9296c62724f429365da8eeb", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(glowSquidProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case HORSE:
                Horse horse = (Horse) entity;
                Horse.Color horseType = horse.getColor();
                PlayerProfile horseProfile = null;
                if(horseType.equals(Horse.Color.BLACK))
                {
                    horseProfile = getProfile("https://textures.minecraft.net/texture/de88d423c17cf6aabd3f125424e9185a936f43eb7b3ac08c8132d4cb2a0bbb73", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(horseType.equals(Horse.Color.BROWN))
                {
                    horseProfile = getProfile("https://textures.minecraft.net/texture/85ce194a54315acc3bf9db7edf6e7da29f49524b1b8af0ef9e4ac3df2280b0d8", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(horseType.equals(Horse.Color.CHESTNUT))
                {
                    horseProfile = getProfile("https://textures.minecraft.net/texture/a996399fff9cbcfb7ba677dd0c2d104229d1cc2307a6f075a882da4694ef80ae", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(horseType.equals(Horse.Color.CREAMY))
                {
                    horseProfile = getProfile("https://textures.minecraft.net/texture/2990821951fd9ab9301aebf5d76a9b60f15ebf619e1c9e8d668919d394d9a933", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(horseType.equals(Horse.Color.DARK_BROWN))
                {
                    horseProfile = getProfile("https://textures.minecraft.net/texture/e5450aeb38f4c8baab4bd0d093d600afcdcbff6926b78e86a794ef0d8d97bda6", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(horseType.equals(Horse.Color.GRAY))
                {
                    horseProfile = getProfile("https://textures.minecraft.net/texture/64aabd68640a833bd30e47e8bb9a3f2fcb14d7e2f28c38af0bf56ceddb8fa59", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(horseType.equals(Horse.Color.WHITE))
                {
                    horseProfile = getProfile("https://textures.minecraft.net/texture/8b03eb13d798f3823703357aa6e8fd29dff3010871b4e54f928e270d7410969b", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                assert skullMeta != null;
                skullMeta.setOwnerProfile(horseProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case MOOSHROOM:
                MushroomCow mooshroomCow = (MushroomCow) entity;
                MushroomCow.Variant mushroomCowType = mooshroomCow.getVariant();
                PlayerProfile mushroomCowProfile = null;
                if(mushroomCowType.equals(MushroomCow.Variant.RED))
                {
                    mushroomCowProfile = getProfile("https://textures.minecraft.net/texture/1543b72def1b247685ad4d027df86c9632e7dac143a9552ec89c80035c3ba4ae", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(mushroomCowType.equals(MushroomCow.Variant.BROWN))
                {
                    mushroomCowProfile = getProfile("https://textures.minecraft.net/texture/199cd80c0a353b181b6588e9d820671c59ed9f27f1cfcd2195e65b918fb65e47", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                assert skullMeta != null;
                skullMeta.setOwnerProfile(mushroomCowProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case MULE:
                PlayerProfile muleProfile = getProfile("https://textures.minecraft.net/texture/46dcda265e57e4f51b145aacbf5b59bdc6099ffd3cce0a661b2c0065d80930d8", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(muleProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case OCELOT:
                PlayerProfile ocelotProfile = getProfile("https://textures.minecraft.net/texture/d03a2e37418e0cffaa2b513910c5282b9bb06c35a1d47039a5cc51b234a542f3", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(ocelotProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case PARROT:
                Parrot parrot = (Parrot) entity;
                Parrot.Variant parrotType = parrot.getVariant();
                PlayerProfile parrotProfile = null;
                if(parrotType.equals(Parrot.Variant.RED))
                {
                    parrotProfile = getProfile("https://textures.minecraft.net/texture/5d1a168bc72cb314f7c86feef9d9bc7612365244ce67f0a104fce04203430c1d", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(parrotType.equals(Parrot.Variant.BLUE))
                {
                    parrotProfile = getProfile("https://textures.minecraft.net/texture/20e03b10c15ee5601423867dfb8bcbcbc919ca96c0eea63073ec8e795eabd05f", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(parrotType.equals(Parrot.Variant.GREEN))
                {
                    parrotProfile = getProfile("https://textures.minecraft.net/texture/5fc9a3b9d5879c2150984dbfe588cc2e61fb1de1e60fd2a469f69dd4b6f6a993", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(parrotType.equals(Parrot.Variant.CYAN))
                {
                    parrotProfile = getProfile("https://textures.minecraft.net/texture/bc6471f23547b2dbdf60347ea128f8eb2baa6a79b0401724f23bd4e2564a2b61", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(parrotType.equals(Parrot.Variant.GRAY))
                {
                    parrotProfile = getProfile("https://textures.minecraft.net/texture/a3c34722ac64496c9b84d0c54019daae6185d6094990133ad6810eea3d24067a", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                assert skullMeta != null;
                skullMeta.setOwnerProfile(parrotProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case PIG:
                PlayerProfile pigProfile = getProfile("https://textures.minecraft.net/texture/9b1760e3778f8087046b86bec6a0a83a567625f30f0d6bce866d4bed95dba6c1", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(pigProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case PUFFERFISH:
                PlayerProfile pufferfishProfile = getProfile("https://textures.minecraft.net/texture/292350c9f0993ed54db2c7113936325683ffc20104a9b622aa457d37e708d931", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(pufferfishProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case RABBIT:
                Rabbit rabbit = (Rabbit) entity;
                Rabbit.Type rabbitType = rabbit.getRabbitType();
                PlayerProfile rabbitProfile = null;
                if(rabbitType.equals(Rabbit.Type.BROWN))
                {
                    rabbitProfile = getProfile("https://textures.minecraft.net/texture/c1db38ef3c1a1d59f779a0cd9f9e616de0cc9acc7734b8facc36fc4ea40d0235", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(rabbitType.equals(Rabbit.Type.WHITE))
                {
                    rabbitProfile = getProfile("https://textures.minecraft.net/texture/a0dcddc236972edcd48e825b6b0054b7b6e1a781e6f12ae04c14a07827ca8dcc", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(rabbitType.equals(Rabbit.Type.BLACK))
                {
                    rabbitProfile = getProfile("https://textures.minecraft.net/texture/19a675edb3cba0f3436ae9473cf03592b7a49d38813579084d637e7659999b8e", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(rabbitType.equals(Rabbit.Type.BLACK_AND_WHITE))
                {
                    rabbitProfile = getProfile("https://textures.minecraft.net/texture/32f39e0a603386ca1ee36236e0b490a1547e6e2a89911674509037fb6f711810", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(rabbitType.equals(Rabbit.Type.GOLD))
                {
                    rabbitProfile = getProfile("https://textures.minecraft.net/texture/2a6361fea24b111ed78c1fefc295212e8a59b0c88b656062527b17a2d7489c81", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(rabbitType.equals(Rabbit.Type.SALT_AND_PEPPER))
                {
                    rabbitProfile = getProfile("https://textures.minecraft.net/texture/cc4349fe9902dd76c1361f8d6a1f79bff6f433f3b7b18a47058f0aa16b9053f", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(rabbitType.equals(Rabbit.Type.THE_KILLER_BUNNY))
                {
                    rabbitProfile = getProfile("https://textures.minecraft.net/texture/a0dcddc236972edcd48e825b6b0054b7b6e1a781e6f12ae04c14a07827ca8dcc", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                assert skullMeta != null;
                skullMeta.setOwnerProfile(rabbitProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case SALMON:
                PlayerProfile salmonProfile = getProfile("https://textures.minecraft.net/texture/d4d001589b86c22cf24f1618fe7efef12932aa9148b5e4fc6ff4a614b990ae12", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(salmonProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case SHEEP:
                Sheep sheep = (Sheep) entity;
                DyeColor sheepColor = sheep.getColor();
                PlayerProfile sheepProfile = null;
                if(sheepColor.equals(DyeColor.WHITE))
                {
                    sheepProfile = getProfile("https://textures.minecraft.net/texture/a723893df4cfb9c7240fc47b560ccf6ddeb19da9183d33083f2c71f46dad290a", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(sheepColor.equals(DyeColor.ORANGE))
                {
                    sheepProfile = getProfile("https://textures.minecraft.net/texture/4271442d8a37db49f02a94c29352694962b5d0bd6bea05f1d93fe19eb4e7060e", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(sheepColor.equals(DyeColor.MAGENTA))
                {
                    sheepProfile = getProfile("https://textures.minecraft.net/texture/fe228b04e9b979a10b70b8db6f3fb199deeb581594a5aa4a7febe948db17228b", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(sheepColor.equals(DyeColor.LIGHT_BLUE))
                {
                    sheepProfile = getProfile("https://textures.minecraft.net/texture/c8eb0d17479870b3973e8e001b82dcde22efc9d10c90412c6733a0b136564d1f", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(sheepColor.equals(DyeColor.YELLOW))
                {
                    sheepProfile = getProfile("https://textures.minecraft.net/texture/12a5354c230e861aac72734a4582d1317026454b807ac353fc3a0bd0d8c422ba", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(sheepColor.equals(DyeColor.LIME))
                {
                    sheepProfile = getProfile("https://textures.minecraft.net/texture/1ce4090e1bccf992b36def74a6d7d3972c17db1b75554e2c509271680b8e7974", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(sheepColor.equals(DyeColor.PINK))
                {
                    sheepProfile = getProfile("https://textures.minecraft.net/texture/2e7cf1c58dbb7c3255b94c6043fa8f0d776c134f4d98b81ca31410965f47a25a", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(sheepColor.equals(DyeColor.GRAY))
                {
                    sheepProfile = getProfile("https://textures.minecraft.net/texture/e6c2a2755b20ddff551a6903f2dc7e61f13ebe39b1d5ca929c87bd8583ec801f", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(sheepColor.equals(DyeColor.LIGHT_GRAY))
                {
                    sheepProfile = getProfile("https://textures.minecraft.net/texture/74a59be620ae8b3ee0dd0fa22c80affed4a0f729295cb8c41e78ee783f4633ad", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(sheepColor.equals(DyeColor.CYAN))
                {
                    sheepProfile = getProfile("https://textures.minecraft.net/texture/60558387b6658f5e9dcffc719214b603f603c4b04e708b7aabe75bcae91e804c", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(sheepColor.equals(DyeColor.PURPLE))
                {
                    sheepProfile = getProfile("https://textures.minecraft.net/texture/343cbdae1f20a79281d3a71adf242a35c8cc58562b415f1120bca9d94b76f254", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(sheepColor.equals(DyeColor.BLUE))
                {
                    sheepProfile = getProfile("https://textures.minecraft.net/texture/e39efc4b4eadec48576a5700ec812395510327e5d1e7c108fd8abc7796685aa3", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(sheepColor.equals(DyeColor.BROWN))
                {
                    sheepProfile = getProfile("https://textures.minecraft.net/texture/e5813715c2f34f05649f8fa3eaaa67f1eda5e6f9cf930fa9c2e0412d1f9728e1", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(sheepColor.equals(DyeColor.GREEN))
                {
                    sheepProfile = getProfile("https://textures.minecraft.net/texture/e5813715c2f34f05649f8fa3eaaa67f1eda5e6f9cf930fa9c2e0412d1f9728e1", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(sheepColor.equals(DyeColor.RED))
                {
                    sheepProfile = getProfile("https://textures.minecraft.net/texture/e0ce5b5ca9165ac77a9c3e3f64df0d3170d5afcf9d5a5575e3f0c0f21e43b83", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(sheepColor.equals(DyeColor.BLACK))
                {
                    sheepProfile = getProfile("https://textures.minecraft.net/texture/634ac5b398cf7c86e3f6f188a5127d8b283d772bf5885c70e0c130805f069950", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                assert skullMeta != null;
                skullMeta.setOwnerProfile(sheepProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case SKELETON_HORSE:
                PlayerProfile skeletonHorseProfile = getProfile("https://textures.minecraft.net/texture/47effce35132c86ff72bcae77dfbb1d22587e94df3cbc2570ed17cf8973a", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(skeletonHorseProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case SNIFFER:
                PlayerProfile snifferProfile = getProfile("https://textures.minecraft.net/texture/87ad920a66e38cc3426a5bff084667e8772116915e298098567c139f222e2c42", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(snifferProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case SNOW_GOLEM:
                PlayerProfile snowGolemProfile = getProfile("https://textures.minecraft.net/texture/4208d5d8032fc98138237c0b0bfe5c3eab50fb2ff8a8022f32c1197ec16418dd", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(snowGolemProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case SQUID:
                PlayerProfile squidProfile = getProfile("https://textures.minecraft.net/texture/49c2c9ce67eb5971cc5958463e6c9abab8e599adc295f4d4249936b0095769dd", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(squidProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case STRIDER:
                PlayerProfile striderProfile = getProfile("https://textures.minecraft.net/texture/a13cb566124aed3b5d86bfaf1d1b01f69526645622ed8510aa86a66d57096fe4", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(striderProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case TADPOLE:
                PlayerProfile tadpoleProfile = getProfile("https://textures.minecraft.net/texture/b23ebf26b7a441e10a86fb5c2a5f3b519258a5c5dddd6a1a75549f517332815b", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(tadpoleProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case TROPICAL_FISH:
                PlayerProfile tropicalFishProfile = getProfile("https://textures.minecraft.net/texture/d6dd5e6addb56acbc694ea4ba5923b1b25688178feffa72290299e2505c97281", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(tropicalFishProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case TURTLE:
                PlayerProfile turtleProfile = getProfile("https://textures.minecraft.net/texture/0a4050e7aacc4539202658fdc339dd182d7e322f9fbcc4d5f99b5718a", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(turtleProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case VILLAGER:
                Villager villager = (Villager) entity;
                Villager.Profession villagerType = villager.getProfession();
                PlayerProfile villagerProfile = null;
                if(villagerType.equals(Villager.Profession.NONE))
                {
                    villagerProfile = getProfile("https://textures.minecraft.net/texture/d14bff1a38c9154e5ec84ce5cf00c58768e068eb42b2d89a6bbd29787590106b", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(villagerType.equals(Villager.Profession.NITWIT))
                {
                    villagerProfile = getProfile("https://textures.minecraft.net/texture/35e799dbfaf98287dfbafce970612c8f075168977aacc30989d34a4a5fcdf429", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(villagerType.equals(Villager.Profession.ARMORER))
                {
                    villagerProfile = getProfile("https://textures.minecraft.net/texture/f522db92f188ebc7713cf35b4cbaed1cfe2642a5986c3bde993f5cfb3727664c", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(villagerType.equals(Villager.Profession.BUTCHER))
                {
                    villagerProfile = getProfile("https://textures.minecraft.net/texture/c6774d2df515eceae9eed291c1b40f94adf71df0ab81c7191402e1a45b3a2087", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(villagerType.equals(Villager.Profession.CARTOGRAPHER))
                {
                    villagerProfile = getProfile("https://textures.minecraft.net/texture/94248dd0680305ad73b214e8c6b00094e27a4ddd8034676921f905130b858bdb", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(villagerType.equals(Villager.Profession.CLERIC))
                {
                    villagerProfile = getProfile("https://textures.minecraft.net/texture/a8856eaafad96d76fa3b5edd0e3b5f45ee49a3067306ad94df9ab3bd5b2d142d", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(villagerType.equals(Villager.Profession.FARMER))
                {
                    villagerProfile = getProfile("https://textures.minecraft.net/texture/55a0b07e36eafdecf059c8cb134a7bf0a167f900966f1099252d903276461cce", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(villagerType.equals(Villager.Profession.FISHERMAN))
                {
                    villagerProfile = getProfile("https://textures.minecraft.net/texture/ac15e5fb56fa16b0747b1bcb05335f55d1fa31561c082b5e3643db5565410852", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(villagerType.equals(Villager.Profession.FLETCHER))
                {
                    villagerProfile = getProfile("https://textures.minecraft.net/texture/17532e90c573a394c7802aa4158305802b59e67f2a2b7e3fd0363aa6ea42b841", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(villagerType.equals(Villager.Profession.LEATHERWORKER))
                {
                    villagerProfile = getProfile("https://textures.minecraft.net/texture/f76cf8b7378e889395d538e6354a17a3de6b294bb6bf8db9c701951c68d3c0e6", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(villagerType.equals(Villager.Profession.LIBRARIAN))
                {
                    villagerProfile = getProfile("https://textures.minecraft.net/texture/e66a53fc707ce1ff88a576ef40200ce8d49fae4acad1e3b3789c7d1cc1cc541a", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(villagerType.equals(Villager.Profession.MASON))
                {
                    villagerProfile = getProfile("https://textures.minecraft.net/texture/2c02c3ffd5705ab488b305d57ff0168e26de70fd3f739e839661ab947dff37b1", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(villagerType.equals(Villager.Profession.SHEPHERD))
                {
                    villagerProfile = getProfile("https://textures.minecraft.net/texture/19e04a752596f939f581930414561b175454d45a0506501e7d2488295a5d5de", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(villagerType.equals(Villager.Profession.TOOLSMITH))
                {
                    villagerProfile = getProfile("https://textures.minecraft.net/texture/7dfa07fd1244eb8945f4ededd00426750b77ef5dfbaf03ed775633459ece415a", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(villagerType.equals(Villager.Profession.WEAPONSMITH))
                {
                    villagerProfile = getProfile("https://textures.minecraft.net/texture/5e409b958bc4fe045e95d325e6e97a533137e33fec7042ac027b30bb693a9d42", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                assert skullMeta != null;
                skullMeta.setOwnerProfile(villagerProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case WANDERING_TRADER:
                PlayerProfile wanderingTraderProfile = getProfile("https://textures.minecraft.net/texture/ee011aac817259f2b48da3e5ef266094703866608b3d7d1754432bf249cd2234", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(wanderingTraderProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            // NEUTRAL MOBS ----------------------------------------
            case BEE:
                PlayerProfile beeProfile = getProfile("https://textures.minecraft.net/texture/886a509ff3cd471f1b428a194b6711470a54773e5de6ee07f7e601cc5e75a200", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(beeProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case CAVE_SPIDER:
                PlayerProfile caveSpiderProfile = getProfile("https://textures.minecraft.net/texture/eccc4a32d45d74e8b14ef1ffd55cd5f381a06d4999081d52eaea12e13293e209", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(caveSpiderProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case DOLPHIN:
                PlayerProfile dolphinProfile = getProfile("https://textures.minecraft.net/texture/8e9688b950d880b55b7aa2cfcd76e5a0fa94aac6d16f78e833f7443ea29fed3", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(dolphinProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case DROWNED:
                PlayerProfile drownedProfile = getProfile("https://textures.minecraft.net/texture/c84df79c49104b198cdad6d99fd0d0bcf1531c92d4ab6269e40b7d3cbbb8e98c", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(drownedProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case ENDERMAN:
                PlayerProfile endermanProfile = getProfile("https://textures.minecraft.net/texture/8a108a0a7a387859f2c44fb9702cf73dbafee3ecfdc4f5def46c0d651b7a49f7", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(endermanProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case FOX:
                Fox fox = (Fox) entity;
                Fox.Type foxType = fox.getFoxType();
                PlayerProfile foxProfile = null;
                if(foxType.equals(Fox.Type.RED))
                {
                    foxProfile = getProfile("https://textures.minecraft.net/texture/2e377c9e96fcdb4b4da3432518c033cb1828afa7f64177a5b3f3ae767cf39897", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(foxType.equals(Fox.Type.SNOW))
                {
                    foxProfile = getProfile("https://textures.minecraft.net/texture/635b7fc33eaa9fb20c16dc8e9db1897966ebecaee2f0209205123910da9886d5", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                assert skullMeta != null;
                skullMeta.setOwnerProfile(foxProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case GOAT:
                PlayerProfile goatProfile = getProfile("https://textures.minecraft.net/texture/f03330398a0d833f53ae8c9a1cb393c74e9d31e18885870e86a2133d44f0c63c", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(goatProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case IRON_GOLEM:
                PlayerProfile ironGolemProfile = getProfile("https://textures.minecraft.net/texture/e13f34227283796bc017244cb46557d64bd562fa9dab0e12af5d23ad699cf697", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(ironGolemProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case LLAMA:
                PlayerProfile llamaProfile = getProfile("https://textures.minecraft.net/texture/9f7d90b305aa64313c8d4404d8d652a96eba8a754b67f4347dcccdd5a6a63398", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(llamaProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case PANDA:
                Panda panda = (Panda) entity;
                Panda.Gene pandaType = panda.getMainGene();
                PlayerProfile pandaProfile = null;
                if(pandaType.equals(Panda.Gene.NORMAL))
                {
                    pandaProfile = getProfile("https://textures.minecraft.net/texture/ba6e3ad823f96d4a80a14556d8c9c7632163bbd2a876c0118b458925d87a5513", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(pandaType.equals(Panda.Gene.AGGRESSIVE))
                {
                    pandaProfile = getProfile("https://textures.minecraft.net/texture/5880e236494e7135db8ec45f64ba9249cadb94a5c1a7a5157f3e02b01bfdb0f6", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(pandaType.equals(Panda.Gene.LAZY))
                {
                    pandaProfile = getProfile("https://textures.minecraft.net/texture/962a024e871bfb2eb995dad21e9e70489043d3cbc73d7fa5520aeb765993347", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(pandaType.equals(Panda.Gene.WORRIED))
                {
                    pandaProfile = getProfile("https://textures.minecraft.net/texture/2266672e4ba54c5c0be59e0461a6e32ea0a7cf115d711867d2922ee9ca523690", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(pandaType.equals(Panda.Gene.PLAYFUL))
                {
                    pandaProfile = getProfile("https://textures.minecraft.net/texture/9fc1527246fda3e83112534414bdbcc3c10f3ba5cf08e47af48a18e76ee148fd", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(pandaType.equals(Panda.Gene.WEAK))
                {
                    pandaProfile = getProfile("https://textures.minecraft.net/texture/d67219a46d3957e3292b81d2e28f86c63501ecb6673ace326035b5229bd8db4a", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(pandaType.equals(Panda.Gene.BROWN))
                {
                    pandaProfile = getProfile("https://textures.minecraft.net/texture/b4f7c73fda6a34cf8be4c7907dd0f5f0865dd77fd882fc633563649c57517cae", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                assert skullMeta != null;
                skullMeta.setOwnerProfile(pandaProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case PIGLIN:
                mobHead.setType(Material.PIGLIN_HEAD);
                assert skullMeta != null;
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case POLAR_BEAR:
                PlayerProfile polarBearProfile = getProfile("https://textures.minecraft.net/texture/3d3cd8548e7dceb5c2394d1b00da2c61ffc0dde46229b10509eb27a0dcb23bfb", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(polarBearProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case SPIDER:
                PlayerProfile spiderProfile = getProfile("https://textures.minecraft.net/texture/35e248da2e108f09813a6b848a0fcef111300978180eda41d3d1a7a8e4dba3c3", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(spiderProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case TRADER_LLAMA:
                PlayerProfile traderLlamaProfile = getProfile("https://textures.minecraft.net/texture/56307f42fc88ebc211e04ea2bb4d247b7428b711df9a4e0c6d1b921589e443a1", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(traderLlamaProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case WOLF:
                Wolf wolf = (Wolf) entity;
                Wolf.Variant wolfType = wolf.getVariant();
                PlayerProfile wolfProfile = null;
                if(wolfType.equals(Wolf.Variant.ASHEN))
                {
                    wolfProfile = getProfile("https://textures.minecraft.net/texture/d39754f0cff419578ce94b5c88666d26d0275ff2cc713b6e3461b935baac844f", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(wolfType.equals(Wolf.Variant.BLACK))
                {
                    wolfProfile = getProfile("https://textures.minecraft.net/texture/982f647315a78c4e10e40571a4b82996ef9e6d38b525f7ba770cdfe8eeedbac6", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(wolfType.equals(Wolf.Variant.CHESTNUT))
                {
                    wolfProfile = getProfile("https://textures.minecraft.net/texture/351a0748ba0f7f0df4cde3d9b9edcb35db2273cd45b2191a5d2315cf8e756caf", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(wolfType.equals(Wolf.Variant.PALE))
                {
                    wolfProfile = getProfile("https://textures.minecraft.net/texture/bc83f26f420004dbd3ef15ae0521361887e1e4d7fcc4f4bfb0fe1f54b16e7711", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(wolfType.equals(Wolf.Variant.RUSTY))
                {
                    wolfProfile = getProfile("https://textures.minecraft.net/texture/b704777cc5363757a8637a17638eda83487b03615c999f12a2cd65358afe4a08", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(wolfType.equals(Wolf.Variant.SNOWY))
                {
                    wolfProfile = getProfile("https://textures.minecraft.net/texture/44877e52d9361278a3736fcaaa5d70eb14785c1a995bdaba5de91ed886459ea8", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(wolfType.equals(Wolf.Variant.SPOTTED))
                {
                    wolfProfile = getProfile("https://textures.minecraft.net/texture/98ec61fd9cbc4f663c3f849ea45383d43319c2c770a2f28b8c7a50975b81b3c9", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(wolfType.equals(Wolf.Variant.STRIPED))
                {
                    wolfProfile = getProfile("https://textures.minecraft.net/texture/bc46cf492aa556129f791e0bedc04964263b499760c988ae1740fe278c76e10a", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(wolfType.equals(Wolf.Variant.WOODS))
                {
                    wolfProfile = getProfile("https://textures.minecraft.net/texture/6e8c2a6e104a3e9d7397326f01bf25862f0aaf9e3830d49dbb9b4a4305358404", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                assert skullMeta != null;
                skullMeta.setOwnerProfile(wolfProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case ZOMBIFIED_PIGLIN:
                PlayerProfile zombifiedPiglinProfile = getProfile("https://textures.minecraft.net/texture/7eabaecc5fae5a8a49c8863ff4831aaa284198f1a2398890c765e0a8de18da8c", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(zombifiedPiglinProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            // NEUTRAL MOBS ----------------------------------------
            case BLAZE:
                PlayerProfile blazeProfile = getProfile("https://textures.minecraft.net/texture/b20657e24b56e1b2f8fc219da1de788c0c24f36388b1a409d0cd2d8dba44aa3b", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(blazeProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case BOGGED:
                PlayerProfile boggedProfile = getProfile("https://textures.minecraft.net/texture/a3b9003ba2d05562c75119b8a62185c67130e9282f7acbac4bc2824c21eb95d9", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(boggedProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case BREEZE:
                PlayerProfile breezeProfile = getProfile("https://textures.minecraft.net/texture/a275728af7e6a29c88125b675a39d88ae9919bb61fdc200337fed6ab0c49d65c", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(breezeProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case CREEPER:
                mobHead.setType(Material.CREEPER_HEAD);
                assert skullMeta != null;
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case ELDER_GUARDIAN:
                PlayerProfile elderGuardianProfile = getProfile("https://textures.minecraft.net/texture/92dd2579ed5bb1853805ff6341d4118b4a4f485fb07ad550320a86ac586ba993", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(elderGuardianProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case ENDERMITE:
                PlayerProfile endermiteProfile = getProfile("https://textures.minecraft.net/texture/1730127e3ac7677122422df0028d9e7368bd157738c8c3cddecc502e896be01c", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(endermiteProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case ENDER_DRAGON:
                mobHead.setType(Material.DRAGON_HEAD);
                assert skullMeta != null;
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case EVOKER:
                PlayerProfile evokerProfile = getProfile("https://textures.minecraft.net/texture/3433322e2ccbd9c55ef41d96f38dbc666c803045b24391ac9391dccad7cd", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(evokerProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case GHAST:
                PlayerProfile ghastProfile = getProfile("https://textures.minecraft.net/texture/64ab8a22e7687cc4c78f3b6ff5b1eb04917b51cd3cd7dbce36171160b3c77ced", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(ghastProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case GUARDIAN:
                PlayerProfile guardianProfile = getProfile("https://textures.minecraft.net/texture/b8e725779c234c590cce854db5c10485ed8d8a33fa9b2bdc3424b68bb1380bed", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(guardianProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case HOGLIN:
                PlayerProfile hoglinProfile = getProfile("https://textures.minecraft.net/texture/9bb9bc0f01dbd762a08d9e77c08069ed7c95364aa30ca1072208561b730e8d75", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(hoglinProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case HUSK:
                PlayerProfile huskProfile = getProfile("https://textures.minecraft.net/texture/c096164f81950a5cc0e33e87999f98cde792517f4d7f99a647a9aedab23ae58", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(huskProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case MAGMA_CUBE:
                PlayerProfile magmaCubeProfile = getProfile("https://textures.minecraft.net/texture/38957d5023c937c4c41aa2412d43410bda23cf79a9f6ab36b76fef2d7c429", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(magmaCubeProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case PHANTOM:
                PlayerProfile phantomProfile = getProfile("https://textures.minecraft.net/texture/b4ad63b697a4c4790d00c435460baf49191657e61bee611f7588dbcda7198bbd", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(phantomProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case PIGLIN_BRUTE:
                PlayerProfile piglinBruteProfile = getProfile("https://textures.minecraft.net/texture/a792b6997d739f535beed3ab1d4aeadfa76777bf8e38a666f54f82ff9f858186", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(piglinBruteProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case PILLAGER:
                PlayerProfile pillagerProfile = getProfile("https://textures.minecraft.net/texture/32fb80a6b6833e31d9ce8313a54777645f9c1e55b810918a706e7bcc8d35a5a2", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(pillagerProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case RAVAGER:
                PlayerProfile ravagerProfile = getProfile("https://textures.minecraft.net/texture/5c73e16fa2926899cf18434360e2144f84ef1eb981f996148912148dd87e0b2a", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(ravagerProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case SHULKER:
                PlayerProfile shulkerProfile = getProfile("https://textures.minecraft.net/texture/f8c9657d237773c3596e2c743c94a5be3849ebc28e1d4667b7accd93a48b04ab", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(shulkerProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case SILVERFISH:
                PlayerProfile silverFishProfile = getProfile("https://textures.minecraft.net/texture/da91dab8391af5fda54acd2c0b18fbd819b865e1a8f1d623813fa761e924540", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(silverFishProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case SKELETON:
                mobHead.setType(Material.SKELETON_SKULL);
                assert skullMeta != null;
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case SLIME:
                PlayerProfile slimeProfile = getProfile("https://textures.minecraft.net/texture/86c27b013f1bf3344869e81e5c610027bc45ec5b79514fdc96e01df1b7e3a387", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(slimeProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case STRAY:
                PlayerProfile strayProfile = getProfile("https://textures.minecraft.net/texture/2c5097916bc0565d30601c0eebfeb287277a34e867b4ea43c63819d53e89ede7", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(strayProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case VEX:
                PlayerProfile vexProfile = getProfile("https://textures.minecraft.net/texture/b663134d7306bb604175d2575d686714b04412fe501143611fcf3cc19bd70abe", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(vexProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case VINDICATOR:
                PlayerProfile vindicatorProfile = getProfile("https://textures.minecraft.net/texture/9e1cab382458e843ac4356e3e00e1d35c36f449fa1a84488ab2c6557b392d", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(vindicatorProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case WARDEN:
                PlayerProfile wardenProfile = getProfile("https://textures.minecraft.net/texture/b0b202de27ce278893031f93005fb1310bbf952058bc41fe079074f77605a906", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(wardenProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case WITCH:
                PlayerProfile witchProfile = getProfile("https://textures.minecraft.net/texture/8aa986a6e1c2d88ff198ab2c3259e8d2674cb83a6d206f883bad2c8ada819", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(witchProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case WITHER:
                mobHead.setType(Material.WITHER_SKELETON_SKULL);
                assert skullMeta != null;
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case WITHER_SKELETON:
                PlayerProfile witherSkeletonProfile = getProfile("https://textures.minecraft.net/texture/1e4d204ebc242eca2148f5853e3af00f84f0d674099dc394f6d2924b240ca2e3", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(witherSkeletonProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case ZOGLIN:
                PlayerProfile zoglinProfile = getProfile("https://textures.minecraft.net/texture/e67e18602e03035ad68967ce090235d8996663fb9ea47578d3a7ebbc42a5ccf9", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                assert skullMeta != null;
                skullMeta.setOwnerProfile(zoglinProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case ZOMBIE:
                mobHead.setType(Material.ZOMBIE_HEAD);
                assert skullMeta != null;
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            case ZOMBIE_VILLAGER:
                ZombieVillager zombieVillager = (ZombieVillager) entity;
                Villager.Profession zombieVillagerType = zombieVillager.getVillagerProfession();
                PlayerProfile zombieVillagerProfile = null;
                if(zombieVillagerType.equals(Villager.Profession.NONE))
                {
                    zombieVillagerProfile = getProfile("https://textures.minecraft.net/texture/fb552c90f212e855d12255d5cd62ed38b9cd7e30e73f0ea779d1764330e69264", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(zombieVillagerType.equals(Villager.Profession.NITWIT))
                {
                    zombieVillagerProfile = getProfile("https://textures.minecraft.net/texture/da27e420f44f9bb06c7b368402b22af6dc57bf9e8ca3475840f6f75e217809c1", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(zombieVillagerType.equals(Villager.Profession.ARMORER))
                {
                    zombieVillagerProfile = getProfile("https://textures.minecraft.net/texture/c8679e034767d518660d9416dc5eaf319d697682ac40c886e3c2bc8dfa1de1d", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(zombieVillagerType.equals(Villager.Profession.BUTCHER))
                {
                    zombieVillagerProfile = getProfile("https://textures.minecraft.net/texture/9cce8d6ce4124cec3e84a852e70f50293f244ddc9ee8578f7d6d8929e16bad69", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(zombieVillagerType.equals(Villager.Profession.CARTOGRAPHER))
                {
                    zombieVillagerProfile = getProfile("https://textures.minecraft.net/texture/e60800b01012e963e7c20c8ba14b70a0264d146a850deffbca7bfe512f4cb23d", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(zombieVillagerType.equals(Villager.Profession.CLERIC))
                {
                    zombieVillagerProfile = getProfile("https://textures.minecraft.net/texture/2958578be0e12172734a78242dab14964abc85ab9b596361f7c5daf8f14a0feb", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(zombieVillagerType.equals(Villager.Profession.FARMER))
                {
                    zombieVillagerProfile = getProfile("https://textures.minecraft.net/texture/f77d415f9baa4fa4b5e058f5b81bf7f003b0a2c90a4831e53a7dbc09841c5511", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(zombieVillagerType.equals(Villager.Profession.FISHERMAN))
                {
                    zombieVillagerProfile = getProfile("https://textures.minecraft.net/texture/6905d53fe4faeb0b315a6878c9ab81b4be52c31cd478c027f0d7ece9f6da8914", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(zombieVillagerType.equals(Villager.Profession.FLETCHER))
                {
                    zombieVillagerProfile = getProfile("https://textures.minecraft.net/texture/2ea26ac0e25498adada4ecea58bb4e76da32d5ca2de307efe5e4218fb7c5ef89", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(zombieVillagerType.equals(Villager.Profession.LEATHERWORKER))
                {
                    zombieVillagerProfile = getProfile("https://textures.minecraft.net/texture/9b2388b23ddd6a9a5776ff2e8ca55b4efe3090caaf967c22769c576b0a40b885", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(zombieVillagerType.equals(Villager.Profession.LIBRARIAN))
                {
                    zombieVillagerProfile = getProfile("https://textures.minecraft.net/texture/62211a1f409cca4249c70d20ca80399fa4844ea417458be988cc21eb4797375e", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(zombieVillagerType.equals(Villager.Profession.MASON))
                {
                    zombieVillagerProfile = getProfile("https://textures.minecraft.net/texture/fb552c90f212e855d12255d5cd62ed38b9cd7e30e73f0ea779d1764330e69264", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(zombieVillagerType.equals(Villager.Profession.SHEPHERD))
                {
                    zombieVillagerProfile = getProfile("https://textures.minecraft.net/texture/691391bef3a46ef267d3b7171086ba4c8d17f2a6b0f83fa2ac30efe914b7c249", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(zombieVillagerType.equals(Villager.Profession.TOOLSMITH))
                {
                    zombieVillagerProfile = getProfile("https://textures.minecraft.net/texture/fb552c90f212e855d12255d5cd62ed38b9cd7e30e73f0ea779d1764330e69264", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                if(zombieVillagerType.equals(Villager.Profession.WEAPONSMITH))
                {
                    zombieVillagerProfile = getProfile("https://textures.minecraft.net/texture/f354a4172a9ba9c47fb853ab284fdc0a344326013e5d73c4bec7800d83f4e399", UUID.fromString("92deafa9-4307-42d9-b003-88601598d6c0"));
                }
                assert skullMeta != null;
                skullMeta.setOwnerProfile(zombieVillagerProfile);
                skullMeta.setDisplayName(Main.color(withPAPISet)
                        .replace("{headType}", headNameReformatted));
                break;
            // Add cases for other entities as needed
            default:
                return null;
        }

        mobHead.setItemMeta(skullMeta);
        return mobHead;
    }

    private static PlayerProfile getProfile(String url, UUID Random_UUID) {
        PlayerProfile profile = Bukkit.createPlayerProfile(Random_UUID); // Get a new player profile
        PlayerTextures textures = profile.getTextures();
        URL urlObject;
        try {
            urlObject = new URL(url); // The URL to the skin, for example: https://textures.minecraft.net/texture/18813764b2abc94ec3c3bc67b9147c21be850cdf996679703157f4555997ea63a
        } catch (MalformedURLException exception) {
            throw new RuntimeException("Invalid URL", exception);
        }
        textures.setSkin(urlObject); // Set the skin of the player profile to the URL
        profile.setTextures(textures); // Set the textures back to the profile
        return profile;
    }

    public static String reformatString(String input) {
        // Convert the input string to lower case and split it by underscores
        String[] parts = input.toLowerCase().split("_");

        // Create a StringBuilder to build the formatted string
        StringBuilder formatted = new StringBuilder();

        // Capitalize the first letter of each part and append to the StringBuilder
        for (String part : parts) {
            if (part.length() > 0) {
                formatted.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1))
                        .append(" ");
            }
        }

        // Trim the trailing space and return the formatted string
        return formatted.toString().trim();
    }
}
