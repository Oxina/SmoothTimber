package com.syntaxphoenix.spigot.smoothtimber.utilities;

public abstract class Strings {

    public static String formatLongNumber(Number number) {
        String value = number.toString();
        StringBuilder builder = new StringBuilder();
        int length = value.length();
        int index = length;
        for (; index > 3; index -= 3) {
            builder.insert(0, value.substring(index - 4, index - 1));
            builder.insert(0, '.');
        }
        return index > 0 ? builder.insert(0, value.substring(0, index)).toString() : builder.substring(1);
    }

}
