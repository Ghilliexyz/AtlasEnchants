package com.atlasplugins.atlasenchants.managers;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.entity.Player;

public class ExperienceManager {

    private final Main main;
    public ExperienceManager(Main main) {
        this.main = main;
    }


    public int getExp(Player player) {
        return getExpFromLevel(player.getLevel()) +
                Math.round(getExpToNext(player.getLevel()) * player.getExp());
    }

    public static int getExpFromLevel(int level) {
        if (level > 30)
            return (int)(4.5D * level * level - 162.5D * level + 2220.0D);
        if (level > 15)
            return (int)(2.5D * level * level - 40.5D * level + 360.0D);
        return level * level + 6 * level;
    }

    public static double getLevelFromExp(long exp) {
        if (exp > 1395L)
            return (Math.sqrt((72L * exp - 54215L)) + 325.0D) / 18.0D;
        if (exp > 315L)
            return Math.sqrt((40L * exp - 7839L)) / 10.0D + 8.1D;
        if (exp > 0L)
            return Math.sqrt((exp + 9L)) - 3.0D;
        return 0.0D;
    }

    private static int getExpToNext(int level) {
        if (level > 30)
            return 9 * level - 158;
        if (level > 15)
            return 5 * level - 38;
        return 2 * level + 7;
    }

    public void changeExp(Player player, int exp) {
        exp += getExp(player);
        if (exp < 0)
            exp = 0;
        double levelAndExp = getLevelFromExp(exp);
        int level = (int)levelAndExp;
        player.setLevel(level);
        player.setExp((float)(levelAndExp - level));
    }
}
