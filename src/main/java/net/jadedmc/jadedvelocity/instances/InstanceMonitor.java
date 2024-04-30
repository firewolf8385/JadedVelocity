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
package net.jadedmc.jadedvelocity.instances;

import net.jadedmc.jadedvelocity.JadedVelocityPlugin;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Monitors all currently existing Instances and provides useful methods for working with them.
 */
public class InstanceMonitor {
    private final JadedVelocityPlugin plugin;

    /**
     * Creates the InstanceMonitor.
     * @param plugin Instance of the plugin.
     */
    public InstanceMonitor(final JadedVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Get an instance based on its name.
     * @param name Name of the instance.
     * @return Instance with that name.
     */
    public Instance getInstance(String name) {
        try(Jedis jedis = plugin.getRedis().jedisPool().getResource()) {
            return new Instance(jedis.get("servers:" + name));
        }
    }

    /**
     * Get an instance from its name, async.
     * @param name Name of the instance.[l
     * @return Instance with that name.
     */
    public CompletableFuture<Instance> getInstanceAsync(String name) {
        return CompletableFuture.supplyAsync(() -> getInstance(name));
    }

    /**
     * Get a Collection of currently registered Instances.
     * Warning: Does so on whatever thread it is called from.
     * @return Collection of Instances.
     */
    public Collection<Instance> getInstances() {
        Collection<Instance> instances = new HashSet<>();

        // Get the Instances from Redis.
        try(Jedis jedis = plugin.getRedis().jedisPool().getResource()) {
            Set<String> names = jedis.keys("servers:*");

            for(String key : names) {
                instances.add(new Instance(jedis.get(key)));
            }
        }

        return instances;
    }

    /**
     * Get a collection of currently registered Instances, wrapped in a CompletableFuture.
     * @return CompletableFuture with a Collection of Instances.
     */
    public CompletableFuture<Collection<Instance>> getInstancesAsync() {
        return CompletableFuture.supplyAsync(this::getInstances);
    }
}