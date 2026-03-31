package dev.alexco.minecraft.util;

import java.util.Map;

public class Formatter {

    /**
     * Converts game ticks into human-readable elapsed time.
     */
    public static String formatTicksToTime(long ticks) {
        long totalSeconds = ticks / 20;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }
    /**
     * Taken from minecraft.wiki
     */
    static Map<Character, String> colours = Map.ofEntries(
            Map.entry('0', "#000000"), // black
            Map.entry('1', "#0000AA"), // dark_blue
            Map.entry('2', "#00AA00"), // dark_green
            Map.entry('3', "#00AAAA"), // dark_aqua
            Map.entry('4', "#AA0000"), // dark_red
            Map.entry('5', "#AA00AA"), // dark_purple
            Map.entry('6', "#FFAA00"), // gold
            Map.entry('7', "#AAAAAA"), // grey
            Map.entry('8', "#555555"), // dark_gray
            Map.entry('9', "#5555FF"), // blue
            Map.entry('a', "#55FF55"), // green
            Map.entry('b', "#55FFFF"), // aqua
            Map.entry('c', "#FF5555"), // red
            Map.entry('d', "#FF55FF"), // light_purple
            Map.entry('e', "#FFFF55"), // yellow
            Map.entry('f', "#FFFFFF") // white
    );
    static Map<Character, String> bgColours = Map.ofEntries(
            Map.entry('0', "#000000"), // black
            Map.entry('1', "#00002A"), // dark_blue
            Map.entry('2', "#002A00"), // dark_green
            Map.entry('3', "#002A2A"), // dark_aqua
            Map.entry('4', "#2A0000"), // dark_red
            Map.entry('5', "#2A002A"), // dark_purple
            Map.entry('6', "#3E2A00"), // gold
            Map.entry('7', "#2A2A2A"), // grey
            Map.entry('8', "#151515"), // dark_gray
            Map.entry('9', "#15153F"), // blue
            Map.entry('a', "#153F15"), // green
            Map.entry('b', "#153F3F"), // aqua
            Map.entry('c', "#3F1515"), // red
            Map.entry('d', "#3F153F"), // light_purple
            Map.entry('e', "#3F3F15"), // yellow
            Map.entry('f', "#3F3F3F") // white
    );

    /**
     * Applies legacy section-sign colour codes to foreground formatting tags.
     */
    public static String format(String text) {
        StringBuilder result = new StringBuilder();
            result.append("[").append(colours.get('f')).append("]");
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '\u00A7' && i + 1 < chars.length) {
                char code = chars[++i];
                String color = colours.get(code);
                if (color != null) {
                    result.append("[").append(color).append("]");
                }
            } else {
                result.append(chars[i]);
            }
        }
        return result.toString();
    }

    /**
     * Formats text with a default yellow foreground.
     */
    public static String formatYellow(String text) {
        StringBuilder result = new StringBuilder();
        result.append("[").append(colours.get('e')).append("]");
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '\u00A7' && i + 1 < chars.length) {
                char code = chars[++i];
                String color = colours.get(code);
                if (color != null) {
                    result.append("[").append(color).append("]");
                }
            } else {
                result.append(chars[i]);
            }
        }
        return result.toString();
    }

    /**
     * Applies legacy section-sign colour codes to background formatting tags.
     */
    public static String formatBg(String text) {
        StringBuilder result = new StringBuilder();
            result.append("[").append(bgColours.get('f')).append("]");
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '\u00A7' && i + 1 < chars.length) {
                char code = chars[++i];
                String color = bgColours.get(code);
                if (color != null) {
                    result.append("[").append(color).append("]");
                }
            } else {
                result.append(chars[i]);
            }
        }
        return result.toString();
    }

}
