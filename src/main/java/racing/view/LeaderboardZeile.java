package racing.view;

import racing.datastructure.Datenelement;

// eine zeile im leaderboard-panel: anzeigetext + seed zum draufklicken
public class LeaderboardZeile implements Datenelement {

    private final String text;
    private final int seed;

    public LeaderboardZeile(String text, int seed) {
        this.text = text;
        this.seed = seed;
    }

    public String gibText() {
        return text;
    }

    public int gibSeed() {
        return seed;
    }
}
