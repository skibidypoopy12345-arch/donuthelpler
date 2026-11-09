package com.donutahelper;

import java.util.Random;

/** Rock-Paper-Scissors practice game. */
public class PaperGame {
    public static final String[] NAMES = {"Rock", "Paper", "Scissors"};
    private final Random rng = new Random();
    public int wins, losses, ties, streak, bestStreak;
    public String message = "Pick one.";

    public void play(int choice) {
        int cpu = rng.nextInt(3);
        if (choice == cpu) { ties++; message = "Both " + NAMES[cpu] + ". Tie."; }
        else if ((choice + 1) % 3 == cpu) { losses++; streak = 0; message = NAMES[choice] + " vs " + NAMES[cpu] + ". You lose."; }
        else { wins++; streak++; bestStreak = Math.max(bestStreak, streak); message = NAMES[choice] + " vs " + NAMES[cpu] + ". You win!"; }
    }
}
