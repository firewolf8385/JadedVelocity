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
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.util.GameProfile;
import net.jadedmc.jadedvelocity.JadedVelocityPlugin;
import org.bson.Document;

public class ServerConnectedListener {
    private final JadedVelocityPlugin plugin;

    public ServerConnectedListener(final JadedVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onConnect(ServerConnectedEvent event) {
        Player player = event.getPlayer();

        String skin = "";
        for(GameProfile.Property property : player.getGameProfile().getProperties()) {
            if(property.getName().equals("textures")) {
                skin = property.getValue();
            }
        }

        String uuid = player.getUniqueId().toString();
        String username = player.getUsername();
        String server = event.getServer().getServerInfo().getName();
        String game;

        switch (server.split("-")[0].toLowerCase()) {
            case "mdl", "mdg" -> game = "DUELS_MODERN";
            case "ldl", "ldg" -> game = "DUELS_LEGACY";
            case "mtl" -> game = "TOURNAMENTS_MODERN";
            case "ltl" -> game = "TOURNAMENTS_LEGACY";
            case "crl", "crg" -> game = "CACTUS_RUSH";
            default -> game = "LEGACY";
        }

        Document document = new Document()
                .append("uuid", uuid)
                .append("displayName", username)
                .append("server", server)
                .append("game", game)
                .append("skin", skin);
        plugin.getRedis().set("jadedplayers:" + uuid, document.toJson());
    }
}
