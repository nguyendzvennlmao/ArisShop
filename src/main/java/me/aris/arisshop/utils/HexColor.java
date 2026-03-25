package me.aris.arisshop.utils;

import net.md_5.bungee.api.ChatColor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HexColor {
    private static final Pattern PATTERN = Pattern.compile("#[a-fA-F0-0]{6}");

    public static String format(String message) {
        if (message == null) return "";
        Matcher matcher = PATTERN.matcher(message);
        while (matcher.find()) {
            String color = message.substring(matcher.start(), matcher.end());
            message = message.replace(color, ChatColor.of(color) + "");
            matcher = PATTERN.matcher(message);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
               }
