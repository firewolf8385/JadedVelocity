package net.jadedmc.jadedvelocity.party;

import net.jadedmc.jadedvelocity.player.Rank;
import org.bson.Document;

import java.util.UUID;

public class PartyPlayer {
    private final UUID uuid;
    private final String username;
    private PartyRole role;
    private final Rank rank;

    public PartyPlayer(final Document document) {
        this.uuid = UUID.fromString(document.getString("uuid"));
        this.username = document.getString("username");
        this.role = PartyRole.valueOf(document.getString("role"));
        this.rank = Rank.valueOf(document.getString("rank"));
    }

    public Rank getRank() {
        return rank;
    }

    public PartyRole getRole() {
        return role;
    }

    public UUID getUniqueID() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }
}