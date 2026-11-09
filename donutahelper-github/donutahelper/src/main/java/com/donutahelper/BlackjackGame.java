package com.donutahelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Practice blackjack with play money: hit, stand, double down, split (up to 4 hands). Purely local. */
public class BlackjackGame {
    public enum Phase { BETTING, PLAYER, DONE }

    public static class Hand {
        public final List<Integer> cards = new ArrayList<>();
        public long bet;
        public boolean doubled, done;
    }

    public final List<Hand> hands = new ArrayList<>();
    public final List<Integer> dealer = new ArrayList<>();
    private final List<Integer> deck = new ArrayList<>();
    public int active;
    public Phase phase = Phase.BETTING;
    public long bankroll = 10000;
    public long bet = 100;
    public int wins, losses, pushes;
    public String message = "Place your bet.";

    public static String card(int c) {
        String[] r = {"A","2","3","4","5","6","7","8","9","10","J","Q","K"};
        String[] s = {"S","H","D","C"};
        return r[c % 13] + s[c / 13];
    }

    private static int value(int c) { return Math.min(c % 13 + 1, 10); }

    public static int total(List<Integer> hand) {
        int sum = 0, aces = 0;
        for (int c : hand) {
            int r = c % 13;
            if (r == 0) { aces++; sum += 11; } else sum += Math.min(r + 1, 10);
        }
        while (sum > 21 && aces-- > 0) sum -= 10;
        return sum;
    }

    private int draw() {
        if (deck.size() < 15) {
            deck.clear();
            for (int i = 0; i < 52; i++) deck.add(i);
            Collections.shuffle(deck);
        }
        return deck.remove(deck.size() - 1);
    }

    private long committed() {
        long t = 0;
        for (Hand h : hands) t += h.bet;
        return t;
    }

    private Hand cur() { return hands.get(active); }

    public void adjustBet(long delta) {
        if (phase == Phase.PLAYER) return;
        bet = Math.max(1, Math.min(Math.max(1, bankroll), bet + delta));
    }

    public void resetBankroll() {
        bankroll = 10000; bet = 100; wins = losses = pushes = 0;
        hands.clear(); dealer.clear(); active = 0;
        phase = Phase.BETTING; message = "Bankroll reset.";
    }

    public void deal() {
        if (phase == Phase.PLAYER) return;
        if (bankroll <= 0) { message = "Out of chips. Reset bankroll."; return; }
        bet = Math.min(bet, bankroll);
        hands.clear(); dealer.clear(); active = 0;
        Hand h = new Hand();
        h.bet = bet;
        h.cards.add(draw()); dealer.add(draw()); h.cards.add(draw()); dealer.add(draw());
        hands.add(h);

        boolean pbj = total(h.cards) == 21, dbj = total(dealer) == 21;
        if (pbj || dbj) {
            h.done = true;
            phase = Phase.DONE;
            if (pbj && dbj) { pushes++; message = "Both blackjack. Push."; }
            else if (pbj) { long w = bet * 3 / 2; bankroll += w; wins++; message = "Blackjack! +" + w; }
            else { bankroll -= bet; losses++; message = "Dealer blackjack. -" + bet; }
            return;
        }
        phase = Phase.PLAYER;
        message = "Hit, stand, double or split?";
    }

    public void hit() {
        if (phase != Phase.PLAYER) return;
        Hand h = cur();
        h.cards.add(draw());
        if (total(h.cards) >= 21) h.done = true;
        advance();
    }

    public void stand() {
        if (phase != Phase.PLAYER) return;
        cur().done = true;
        advance();
    }

    public boolean canDouble() {
        return phase == Phase.PLAYER && cur().cards.size() == 2 && bankroll - committed() >= cur().bet;
    }

    public void doubleDown() {
        if (!canDouble()) return;
        Hand h = cur();
        h.bet *= 2; h.doubled = true;
        h.cards.add(draw());
        h.done = true;
        advance();
    }

    public boolean canSplit() {
        if (phase != Phase.PLAYER || hands.size() >= 4) return false;
        Hand h = cur();
        return h.cards.size() == 2 && value(h.cards.get(0)) == value(h.cards.get(1))
                && bankroll - committed() >= h.bet;
    }

    public void split() {
        if (!canSplit()) return;
        Hand h = cur();
        Hand n = new Hand();
        n.bet = h.bet;
        n.cards.add(h.cards.remove(1));
        h.cards.add(draw());
        n.cards.add(draw());
        hands.add(active + 1, n);
        if (h.cards.get(0) % 13 == 0) { h.done = true; n.done = true; }   // split aces get one card each
        else {
            if (total(h.cards) == 21) h.done = true;
            if (total(n.cards) == 21) n.done = true;
        }
        advance();
    }

    private void advance() {
        while (active < hands.size() && hands.get(active).done) active++;
        if (active >= hands.size()) finish();
    }

    private void finish() {
        boolean live = false;
        for (Hand h : hands) if (total(h.cards) <= 21) live = true;
        if (live) while (total(dealer) < 17) dealer.add(draw());

        int d = total(dealer);
        long net = 0;
        for (Hand h : hands) {
            int p = total(h.cards);
            if (p > 21) { net -= h.bet; losses++; }
            else if (d > 21 || p > d) { net += h.bet; wins++; }
            else if (p < d) { net -= h.bet; losses++; }
            else pushes++;
        }
        bankroll += net;
        phase = Phase.DONE;
        message = net > 0 ? "You win! +" + net : net < 0 ? "Dealer wins. " + net : "Push.";
    }

    public String hand(List<Integer> h, boolean hideSecond) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < h.size(); i++) {
            if (i > 0) sb.append(' ');
            sb.append(hideSecond && i == 1 ? "??" : card(h.get(i)));
        }
        return sb.toString();
    }
}
