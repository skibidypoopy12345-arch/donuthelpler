package com.donutahelper;

/** Session-only state shared between the GUI and the HUD. */
public class State {
    public static final BlackjackGame BLACKJACK = new BlackjackGame();
    public static final PaperGame PAPER = new PaperGame();
    public static long timerEnd = 0;
    public static String timerLabel = "";
}
