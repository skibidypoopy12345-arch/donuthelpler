package com.donutahelper;

public class Util {
    /** Parses "1.5k", "2m", "3b", "1,000". Returns NaN if invalid. */
    public static double parse(String s) {
        if (s == null) return Double.NaN;
        s = s.trim().toLowerCase().replace(",", "").replace("$", "");
        if (s.isEmpty()) return Double.NaN;
        double mult = 1;
        char c = s.charAt(s.length() - 1);
        if (c == 'k') mult = 1e3; else if (c == 'm') mult = 1e6; else if (c == 'b') mult = 1e9; else if (c == 't') mult = 1e12;
        if (mult != 1) s = s.substring(0, s.length() - 1);
        try { return Double.parseDouble(s) * mult; } catch (NumberFormatException e) { return Double.NaN; }
    }

    public static String fmt(double v) {
        double a = Math.abs(v);
        if (a >= 1e12) return String.format("%.2fT", v / 1e12);
        if (a >= 1e9) return String.format("%.2fB", v / 1e9);
        if (a >= 1e6) return String.format("%.2fM", v / 1e6);
        if (a >= 1e3) return String.format("%.2fK", v / 1e3);
        return String.format("%.2f", v);
    }

    public static String time(long ms) {
        if (ms < 0) ms = 0;
        long s = ms / 1000;
        return String.format("%02d:%02d:%02d", s / 3600, (s / 60) % 60, s % 60);
    }
}
