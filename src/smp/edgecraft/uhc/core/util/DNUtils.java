package smp.edgecraft.uhc.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.regex.Pattern;


public class DNUtils {

    /**
     * Join collection items into a string by joiner.
     *
     * @param c      Collection to join items of. If null or empty, method returns empty string.
     * @param joiner String to join items with. If null, empty string is used.
     * @return String consisting of items from collection separated by given joiner.
     */
    public static String join(Collection<?> c, String joiner) {
        if (c == null || c.isEmpty()) return "";
        StringBuilder result = new StringBuilder(c.stream().findFirst().orElse(null) + "");
        String finjoiner = joiner == null ? "" : joiner;
        c.stream().skip(1).forEach(a -> result.append(finjoiner).append(a));
        return result + "";
    }

    /**
     * Join array items into a string by joiner.
     *
     * @param c      Array to join items of. If null or empty, method returns empty string.
     * @param joiner String to join items with. If null, empty string is used.
     * @return String consisting of items from collection separated by given joiner.
     */
    public static String join(Object[] c, String joiner) {
        if (c == null || c.length == 0) return "";
        StringBuilder result = new StringBuilder(c[0] + "");
        String finjoiner = joiner == null ? "" : joiner;
        Arrays.stream(c).skip(1).forEach(a -> result.append(finjoiner).append(a));
        return result + "";
    }

    /**
     * All substrings that are boxed.
     *
     * @param str   String to get substrings from.
     * @param open  Box entrance.
     * @param close Box exit.
     * @return Array of substrings. Null if any of arguments are null or box has empty walls.
     */
    public static String[] substringsBetween(String str, String open, String close) {
        if (str != null && open != null && close != null && open.length() != 0 && close.length() != 0) {
            int strLen = str.length();
            if (strLen == 0) return new String[]{};
            else {
                int closeLen = close.length();
                int openLen = open.length();
                ArrayList<String> list = new ArrayList<>();
                int end;
                for (int pos = 0; pos < strLen - closeLen; pos = end + closeLen) {
                    int start = str.indexOf(open, pos);
                    if (start < 0) break;
                    start += openLen;
                    end = str.indexOf(close, start);
                    if (end < 0) break;
                    list.add(str.substring(start, end));
                }
                return list.isEmpty() ? null : list.toArray(new String[list.size()]);
            }
        } else return null;
    }

    /**
     * Get last char in the string.
     *
     * @param s String to get last char from.
     * @return Last char of string.
     * @throws IllegalArgumentException If s is null or empty.
     */
    public static char lastChar(String s) {
        if (s == null) throw new IllegalArgumentException("null");
        if (s.isEmpty()) throw new IllegalArgumentException("Empty string");
        return s.charAt(s.length() - 1);
    }

    /**
     * Check if string equals a single char.
     *
     * @param c Char to check.
     * @param s String to check.
     * @return True if string is length of 1 and first character equals the given character.
     * @throws IllegalArgumentException If s is null.
     */
    public static boolean charEquals(char c, String s) {
        if (s == null) throw new IllegalArgumentException("null");
        return s.length() == 1 && s.charAt(0) == c;
    }

    /**
     * Trim character repeatedly from beginning and end of the string.
     *
     * @param totrim  String that will be trimmed.
     * @param trimmer Characters to trim, you can select multiple.
     * @return Trimmed string.
     */
    public static String trim(String totrim, char... trimmer) {
        if (trimmer.length > 1) for (int i = 1; i < trimmer.length; i++) totrim = trim(totrim, trimmer[i]);
        int len = totrim.length();
        int st = 0;
        char[] val = totrim.toCharArray();
        while ((st < len) && (val[st] == trimmer[0])) st++;
        while ((st < len) && (val[len - 1] == trimmer[0])) len--;
        return ((st > 0) || (len < totrim.length())) ? totrim.substring(st, len) : totrim;
    }

    /**
     * Repeat a string multiple times.
     *
     * @param s      String to repeat.
     * @param repeat Amount of repeats. If negative or 0 empty string is returned.
     * @return String repeated multiple times.
     */
    public static String repeat(String s, int repeat) {
        if (repeat <= 0) return "";
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < repeat; i++) b.append(s);
        return b + "";
    }

    /**
     * Count how many times does one string appear in the other.
     *
     * @param haystack String to check.
     * @param needle   String to find in haystack string.
     * @return Amount of times needle is found.
     */
    public static int count(String haystack, String needle) {
        if (!haystack.contains(needle)) return 0;
        for (int i = 0; true; i++) {
            String prehay = haystack;
            haystack = haystack.replaceFirst(Pattern.quote(needle), "");
            if (prehay.equals(haystack)) return i;
        }
    }

    /**
     * Run an operation with object if it is not null.
     *
     * @param a      Object to check if not null.
     * @param action Action to do if object is not null.
     * @return Operated object or null.
     */
    public static <T, R> R orElse(T a, Function<T, R> action) {
        return a == null ? null : action.apply(a);
    }

}
