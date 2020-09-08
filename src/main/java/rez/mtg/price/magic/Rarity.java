package rez.mtg.price.magic;

public enum Rarity {
    MYTHIC, RARE, UNCOMMON, COMMON;

    public static Rarity fromLabel(String label) {
        if (label == null)
            return null;
        if (label.equalsIgnoreCase("RARE"))
            return RARE;
        if (label.equalsIgnoreCase("mythic"))
            return MYTHIC;
        if (label.equalsIgnoreCase("UNCOMMON"))
            return UNCOMMON;
        if (label.equalsIgnoreCase("COMMON"))
            return COMMON;
        return null;
    }
}
