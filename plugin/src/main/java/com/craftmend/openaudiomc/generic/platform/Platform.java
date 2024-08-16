package com.craftmend.openaudiomc.generic.platform;

import net.minecraft.util.Formatting;

public class Platform {

    public static String translateColors(String input) {
        return translateAlternateColorCodes('&', input);
    }

    public static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
        char[] b = textToTranslate.toCharArray();

        for (int i = 0; i < b.length - 1; ++i) {
            if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                b[i] = 167;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }

        return new String(b);
    }

    public static String makeColor(String color) {
            Formatting mcColor = Formatting.byName(color);
            if(mcColor != null)
            {
                return  "\u00A7" + mcColor.getCode();
            }
            return ""; //Unknown color
        }
    }
