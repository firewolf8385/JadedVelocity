package net.jadedmc.jadedvelocity.player;

public enum Rank {
    OWNER("&c&lOwner "),
    ADMIN("&c&lAdmin "),
    MOD("&6&lMod "),
    TRIAL("&6&lTrial "),
    BUILDER("&e&lBuilder "),
    DEVELOPER("&e&lDeveloper "),
    JADED("&a&lJaded "),
    SAPPHIRE("&9&lSapphire "),
    AMETHYST("&5&lAmethyst "),
    DEFAULT("");

    private final String prefix;

    Rank(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}