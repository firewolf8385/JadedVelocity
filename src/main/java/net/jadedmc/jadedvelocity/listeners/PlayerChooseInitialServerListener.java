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
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.jadedmc.jadedvelocity.JadedVelocityPlugin;
import org.bson.Document;
import redis.clients.jedis.Jedis;

import java.util.Optional;
import java.util.Set;

public class PlayerChooseInitialServerListener {
    private final JadedVelocityPlugin plugin;

    public PlayerChooseInitialServerListener(final JadedVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onServerChose(PlayerChooseInitialServerEvent event) {
        String serverName = "limbo";
        int playerCount = 9999;

        try(Jedis jedis = plugin.getRedis().jedisPool().getResource()) {
            Set<String> names = jedis.keys("servers:*");

            for(String key : names) {
                Document instance = Document.parse(jedis.get(key));

                if(!instance.getString("mode").equals("HUB")) {
                    continue;
                }

                if(instance.getInteger("online") > playerCount) {
                    continue;
                }

                serverName = instance.getString("serverName");
                playerCount = instance.getInteger("online");
            }
        }

        Optional<RegisteredServer> server = plugin.getProxyServer().getServer(serverName);
        server.ifPresent(event::setInitialServer);
    }
}
