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
package net.jadedmc.jadedvelocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import net.jadedmc.jadedvelocity.databases.Redis;
import net.jadedmc.jadedvelocity.listeners.PlayerChooseInitialServerListener;
import org.bson.Document;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;

@Plugin(
        id = "jadedvelocity",
        name = "JadedVelocity",
        version = "1.0",
        url = "https://www.jadedmc.net"
)
public class JadedVelocityPlugin {
    private final Logger logger;
    private final ProxyServer proxyServer;
    private final Redis redis;

    private YamlDocument config;

    /**
     * Creates the plugin.
     * @param proxyServer Instance of the ProxyServer.
     * @param logger Instance of the Logger.
     * @param dataDirectory Plugin's Data Directory.
     */
    @Inject
    public JadedVelocityPlugin(ProxyServer proxyServer, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxyServer = proxyServer;
        this.logger = logger;

        // Load the configuration file.
        try {
            config = YamlDocument.create(new File(dataDirectory.toFile(), "config.yml"),
                    Objects.requireNonNull(getClass().getResourceAsStream("/config.yml")),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("file-version")).setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS).build());
            config.update();
            config.save();
        }
        catch(IOException exception) {
            exception.printStackTrace();
        }

        // Connect to redis.
        redis = new Redis(this);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // Register events.
        proxyServer.getEventManager().register(this, new PlayerChooseInitialServerListener(this));


        // Register loaded servers.
        try(Jedis jedis = redis.jedisPool().getResource()) {
            Set<String> names = jedis.keys("servers:*");

            for(String key : names) {
                Document instance = Document.parse(jedis.get(key));

                String name = instance.getString("serverName");
                InetSocketAddress address = new InetSocketAddress(instance.getInteger("port"));
                ServerInfo server = new ServerInfo(name, address);
                proxyServer.registerServer(server);
            }
        }
    }
    public YamlDocument getConfig() {
        return config;
    }

    public ProxyServer getProxyServer() {
        return proxyServer;
    }

    public Redis getRedis() {
        return redis;
    }
}