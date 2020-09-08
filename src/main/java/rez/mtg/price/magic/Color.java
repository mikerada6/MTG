package rez.mtg.price.magic;

public enum Color {
    WHITE, BLUE, GREEN, BLACK, RED, COLORLESS, GOLD;

    public static Color fromLabel(String label) {
        Color ans = COLORLESS;
        int count = 0;
        if (label.contains("W") || label.contains("w")) {
            ans = WHITE;
            count++;
        }
        if (label.contains("U") || label.contains("u")) {
            ans = BLUE;
            count++;
        }
        if (label.contains("B") || label.contains("b")) {
            ans = BLACK;
            count++;
        }
        if (label.contains("R") || label.contains("r")) {
            ans = RED;
            count++;
        }
        if (label.contains("G") || label.contains("G")) {
            ans = GREEN;
            count++;
        }
        if (count>1)
        {
            ans = GOLD;
        }
        return ans;
    }
}
