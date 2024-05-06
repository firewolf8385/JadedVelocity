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
package net.jadedmc.jadedvelocity.databases;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.jadedmc.jadedvelocity.JadedVelocityPlugin;
import net.jadedmc.jadedvelocity.utils.StringUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages the connection process to Redis.
 */
public class Redis {
    private final JadedVelocityPlugin plugin;
    private final JedisPool jedisPool;

    /**
     * Connects to Redis.
     * @param plugin Instance of the plugin.
     */
    public Redis(final JadedVelocityPlugin plugin) {
        this.plugin = plugin;

        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(Integer.MAX_VALUE);

        String host = plugin.getConfig().getString("Redis.host");
        int port = plugin.getConfig().getInt("Redis.port");

        jedisPool = new JedisPool(jedisPoolConfig, host, port);

        subscribe();
    }

    public JedisPool jedisPool() {
        return jedisPool;
    }

    public void publish(String channel,  String message) {
        try(Jedis publisher = jedisPool.getResource()) {
            publisher.publish(channel, message);
        }
    }

    public void set(String key, String value) {
        try(Jedis jedis = jedisPool.getResource()) {
            jedis.set(key, value);
        }
    }

    public void sadd(String key, String value) {
        try(Jedis jedis = jedisPool.getResource()) {
            jedis.sadd(key, value);
        }
    }

    public void del(String key) {
        try(Jedis jedis = jedisPool.getResource()) {
            jedis.del(key);
        }
    }

    public void subscribe() {
        new Thread("Redis Subscriber") {
            @Override
            public void run() {

                try (Jedis jedis = jedisPool.getResource()) {
                    jedis.subscribe(new JedisPubSub() {
                        @Override
                        public void onMessage(String channel, String msg) {
                            if(!channel.equals("proxy")) {
                               return;
                            }

                            String[] args = msg.split(" ");

                            if(args[0].equals("message")) {
                                String[] playerUUIDs = args[1].split(",");
                                String message = StringUtils.join(Arrays.copyOfRange(args, 2, args.length), " ");
                                Component formattedMessage = MiniMessage.miniMessage().deserialize(message);

                                for(String playerUUID : playerUUIDs) {
                                    UUID uuid = UUID.fromString(playerUUID);

                                    if(plugin.getProxyServer().getPlayer(uuid).isEmpty()) {
                                        continue;
                                    }

                                    Player player = plugin.getProxyServer().getPlayer(uuid).get();
                                    player.sendMessage(formattedMessage);
                                }
                            }
                            else if(args[0].equals("connect")) {
                                String[] playerUUIDs = args[1].split(",");
                                String serverName = args[2];
                                Optional<RegisteredServer> server = plugin.getProxyServer().getServer(serverName);

                                for(String playerUUID : playerUUIDs) {
                                    UUID uuid = UUID.fromString(playerUUID);

                                    if(plugin.getProxyServer().getPlayer(uuid).isEmpty()) {
                                        return;
                                    }

                                    Player player = plugin.getProxyServer().getPlayer(uuid).get();
                                    server.ifPresent(request -> player.createConnectionRequest(request).connect());
                                }
                            }

                        }
                    }, "proxy");
                }
                catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }.start();
    }
}