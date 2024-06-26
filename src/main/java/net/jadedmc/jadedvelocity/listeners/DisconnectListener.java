/*
 * This file is part of JadedVelocity, licensed under the MIT License.
 *
 *  Copyright (c) JadedMC
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package net.jadedmc.jadedvelocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.proxy.Player;
import net.jadedmc.jadedvelocity.JadedVelocityPlugin;
import net.jadedmc.jadedvelocity.party.Party;
import net.jadedmc.jadedvelocity.party.PartyPlayer;
import net.jadedmc.jadedvelocity.party.PartyRole;
import org.bson.Document;
import redis.clients.jedis.Jedis;

import java.util.Set;

/**
 * This listens to the DisconnectEvent event, which is called every time a player leaves the server.
 */
public class DisconnectListener {
    private final JadedVelocityPlugin plugin;

    /**
     * Creates the Listener.
     * @param plugin Instance of the plugin.
     */
    public DisconnectListener(final JadedVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Runs when a player disconnects from the proxy.
     * @param event DisconnectEvent.
     */
    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        plugin.getRedis().del("jadedplayers:" + player.getUniqueId().toString());

        // Get all parties from redis.
        try(Jedis jedis = plugin.getRedis().jedisPool().getResource()) {
            Set<String> names = jedis.keys("parties:*");

            // Loops through each stored party.
            for(String key : names) {
                Document document = Document.parse(jedis.get("parties:" + key.replace("parties:", "")));
                Party party = new Party(plugin, document);

                // If the player is in that party, cache the party to memory.
                if(party.hasPlayer(player.getUniqueId())) {
                    PartyPlayer partyPlayer = party.getPlayer(player.getUniqueId());

                    if(partyPlayer.getRole() != PartyRole.LEADER) {
                        party.broadcast("<green><bold>Party</bold> <dark_gray>» " + partyPlayer.getRank().getPrefix() + "<gray>" + partyPlayer.getUsername() + " <green>has left the party.");
                        party.removePlayer(player);
                        return;
                    }

                    party.broadcast("<green><bold>Party</bold> <dark_gray>» <green>The party has been disbanded!");
                    party.disband();

                    break;
                }
            }
        }
    }
}