package net.jadedmc.jadedvelocity.party;

import com.velocitypowered.api.proxy.Player;
import net.jadedmc.jadedvelocity.JadedVelocityPlugin;
import org.bson.Document;

import java.util.*;

public class Party {
    private final JadedVelocityPlugin plugin;
    private final UUID uuid;
    private final Collection<PartyPlayer> players = new HashSet<>();
    private final Collection<UUID> invites = new HashSet<>();

    public Party(final JadedVelocityPlugin plugin, final Document document) {
        this.plugin = plugin;
        this.uuid = UUID.fromString(document.getString("uuid"));

        // Load the players from the document.
        Document playersDocument = document.get("players", Document.class);
        for(String player : playersDocument.keySet()) {
            players.add(new PartyPlayer(playersDocument.get(player, Document.class)));
        }

        // Load the pending invites of the party.
        List<String> inviteUUIDs = document.getList("invites", String.class);
        for(String uuid : inviteUUIDs) {
            this.invites.add(UUID.fromString(uuid));
        }
    }

    /**
     * Adds an invite to the party.
     * @param playerUUID UUID of the player being invited.
     */
    public void addInvite(final UUID playerUUID) {
        this.invites.add(playerUUID);
    }

    /**
     * Disbands the party.
     */
    public void disband() {
        plugin.getRedis().publish("party", "disband " + this.uuid.toString());
        plugin.getRedis().del("parties:" + this.uuid.toString());
    }

    public boolean hasPlayer(UUID playerUUID) {
        for(PartyPlayer partyPlayer : players) {
            if(partyPlayer.getUniqueID().equals(playerUUID)) {
                return true;
            }
        }

        return false;
    }

    public void broadcast(final String message) {
        final StringBuilder builder = new StringBuilder();
        players.forEach(partyPlayer -> {
            builder.append(partyPlayer.getUniqueID());
            builder.append(",");
        });

        final String targets = builder.substring(0, builder.length() - 1);

        plugin.getRedis().publish("proxy", "message " + targets + " " + message);
    }

    public void removePlayer(final Player player) {
        for(PartyPlayer partyPlayer : players) {
            if(partyPlayer.getUniqueID().equals(player.getUniqueId())) {
                players.remove(partyPlayer);
                break;
            }
        }

        plugin.getRedis().publish("party", "leave " + this.uuid.toString() + " " + player.getUniqueId().toString());
        update();
    }

    public PartyPlayer getPlayer(UUID playerUUID) {
        for(PartyPlayer partyPlayer : players) {
            if(partyPlayer.getUniqueID().equals(playerUUID)) {
                return partyPlayer;
            }
        }

        return null;
    }

    public UUID getUniqueID() {
        return uuid;
    }

    /**
     * Converts the cached party into a Bson Document.
     * @return Bson document of the party.
     */
    public Document toDocument() {
        final Document document = new Document();
        document.append("uuid", uuid.toString());

        final Document playersDocument = new Document();
        for(PartyPlayer player : players) {
            playersDocument.append(player.getUniqueID().toString(), player.toDocument());
        }
        document.append("players", playersDocument);

        final List<String> invites = new ArrayList<>();
        this.invites.forEach(invite -> invites.add(invite.toString()));
        document.append("invites", invites);

        return document;
    }

    /**
     * Updates the party in Redis.
     */
    public void update() {
        plugin.getRedis().set("parties:" + uuid.toString(), toDocument().toJson());
        plugin.getRedis().publish("party", "update " + this.uuid);
    }
}