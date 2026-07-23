package com.atlasplugins.atlasenchants.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Colour-code handling for player-supplied item names (Circe's Brand).
 *
 * This is deliberately separate from {@link com.atlasplugins.atlasenchants.Main#color(String)}:
 * that method translates trusted config strings, whereas everything here is typed by a player and
 * therefore has to be sanitised (literal section signs stripped) and permission-gated (the
 * formatting codes &k&l&m&n&o can be withheld) before it is translated.
 */
public final class RenameUtils {

    private RenameUtils() {}

    /** &#RRGGBB - the hex form we accept from players. */
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([0-9A-Fa-f]{6})");

    /** The five non-colour codes. Kept separate so they can be gated behind a permission. */
    private static final String FORMAT_CODES = "KkLlMmNnOo";

    /** Colour codes plus the reset code - always allowed. */
    private static final String COLOR_CODES = "0123456789AaBbCcDdEeFfRr";

    /**
     * Translate a player-typed name into a coloured display name.
     *
     * @param allowFormatting when false, &k&l&m&n&o are removed rather than translated. &k in
     *                        particular renders as shifting unreadable glyphs, which is a known
     *                        item-scam vector on trading servers.
     */
    public static String translate(String input, boolean allowFormatting) {
        if (input == null) return null;

        // Strip any literal section sign the player typed. Without this a player could paste a
        // pre-coloured string and bypass the formatting permission entirely, since the codes
        // would already be in their final form and never pass through the checks below.
        String cleaned = input.replace('§', '&');

        StringBuilder out = new StringBuilder(cleaned.length());

        // Hex first, so the 6 hex digits are consumed before the single-character pass below
        // has a chance to read them as colour codes.
        Matcher hex = HEX_PATTERN.matcher(cleaned);
        int last = 0;
        while (hex.find()) {
            out.append(translateSimple(cleaned.substring(last, hex.start()), allowFormatting));
            out.append(toHexSequence(hex.group(1)));
            last = hex.end();
        }
        out.append(translateSimple(cleaned.substring(last), allowFormatting));

        return out.toString();
    }

    /** Translate the single-character &x codes, honouring the formatting gate. */
    private static String translateSimple(String input, boolean allowFormatting) {
        char[] chars = input.toCharArray();
        StringBuilder out = new StringBuilder(chars.length);

        for (int i = 0; i < chars.length; i++) {
            if (chars[i] != '&' || i == chars.length - 1) {
                out.append(chars[i]);
                continue;
            }

            char code = chars[i + 1];
            if (COLOR_CODES.indexOf(code) != -1) {
                out.append('§').append(Character.toLowerCase(code));
                i++;
            } else if (FORMAT_CODES.indexOf(code) != -1) {
                // Drop the code entirely when the player lacks the permission, rather than
                // leaving a visible "&k" in the name.
                if (allowFormatting) {
                    out.append('§').append(Character.toLowerCase(code));
                }
                i++;
            } else {
                // Not a code at all - "&" used as a literal ampersand.
                out.append(chars[i]);
            }
        }

        return out.toString();
    }

    /** Build the §x§R§R§G§G§B§B sequence a 1.16+ client reads as a hex colour. */
    private static String toHexSequence(String hex) {
        StringBuilder out = new StringBuilder(14);
        out.append('§').append('x');
        for (char c : hex.toCharArray()) {
            out.append('§').append(Character.toLowerCase(c));
        }
        return out.toString();
    }

    /**
     * The name as the player perceives it, with every colour/format sequence removed.
     * Used for length limits so a name is not rejected for the codes rather than the text.
     */
    public static String stripAll(String input) {
        if (input == null) return "";
        String cleaned = input.replace('§', '&');
        cleaned = HEX_PATTERN.matcher(cleaned).replaceAll("");
        return cleaned.replaceAll("&[0-9A-Fa-fK-Ok-oRr]", "");
    }

    /** True if the name contains a formatting code the player would need permission for. */
    public static boolean usesFormatting(String input) {
        if (input == null) return false;
        String cleaned = input.replace('§', '&');
        return cleaned.matches(".*&[" + FORMAT_CODES + "].*");
    }
}
