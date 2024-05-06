package net.jadedmc.jadedvelocity.party;

import com.velocitypowered.api.proxy.Player;
import net.jadedmc.jadedvelocity.JadedVelocityPlugin;
import org.bson.Document;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class PartyCache {
    private final JadedVelocityPlugin plugin;
    private final UUID uuid;
    private final Collection<PartyPlayer> players = new HashSet<>();

    public PartyCache(final JadedVelocityPlugin plugin, final Document document) {
        this.plugin = plugin;
        this.uuid = UUID.fromString(document.getString("uuid"));

        Document playersDocument = document.get("players", Document.class);
        for(String player : playersDocument.keySet()) {
            players.add(new PartyPlayer(playersDocument.get(player, Document.class)));
        }
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
        plugin.getRedis().publish("party", "leave " + this.uuid.toString() + " " + player.getUniqueId().toString());
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
}