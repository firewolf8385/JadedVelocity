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

import net.jadedmc.jadedvelocity.minigames.Minigame;
import org.bson.Document;

/**
 * Stores information from a server instance, as obtained through Redis.
 */
public class Instance {
    private final String name;
    private final Minigame minigame;
    private final int online;
    private final int capacity;
    private final InstanceStatus status;
    private final long lastHeartbeat;
    private final int majorVersion;
    private final int minorVersion;
    private final String address;
    private final int port;
    private final InstanceType type;
    private final long startTime;

    /**
     * Creates an instance with a given BSON document.
     * @param document Document to create instance with.
     */
    public Instance(Document document) {
        this.name = document.getString("serverName");
        this.online = document.getInteger("online");
        this.capacity = document.getInteger("capacity");
        this.majorVersion = document.getInteger("majorVersion");
        this.minorVersion = document.getInteger("minorVersion");
        this.address = document.getString("address");
        this.port = document.getInteger("port");
        this.type = InstanceType.valueOf(document.getString("type"));
        this.minigame = Minigame.valueOf(document.getString("mode"));
        this.lastHeartbeat = document.getLong("heartbeat");
        this.startTime = document.getLong("startTime");

        if((System.currentTimeMillis() - lastHeartbeat > 90000)) {
            // If the server has not responded in 90 seconds, mark it as unresponsive.
            this.status = InstanceStatus.UNRESPONSIVE;
        }
        else if(capacity == online) {
            // If the server is at capacity, mark it as full.
            this.status = InstanceStatus.FULL;
        }
        else {
            // Otherwise, read the status from the document.
            this.status = InstanceStatus.valueOf(document.getString("status"));
        }
    }

    /**
     * Create an Instance with a JSON String.
     * Does so by creating a BSON document with the JSON.
     * @param json JSON to create instance with.
     */
    public Instance(String json) {
        this(Document.parse(json));
    }

    /**
     * Gets the address of the machine the instance is running on.
     * @return Instance address.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Get the maximum capacity of the Instance.
     * @return Maximum number of players the Instance can hold.
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Get the last time (in ms since epoch) that the Instance sent a heartbeat message.
     * @return Last time a heartbeat message was sent.
     */
    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    /**
     * Get the Minigame being run on the Instance.
     * @return Instance Minigame.
     */
    public Minigame getMinigame() {
        return minigame;
    }

    /**
     * Get the name of the Instance.
     * @return Instance name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the number of players currently on the Instance.
     * @return Players online.
     */
    public int getOnline() {
        return online;
    }

    /**
     * Get the port the Instance is running on.
     * @return Port of the Instance.
     */
    public int getPort() {
        return port;
    }

    /**
     * Get the time (in milliseconds since epoch) that the server was started.
     * @return Server start time.
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Gets the current status of the Instance.
     * @return Instance Status.
     */
    public InstanceStatus getStatus() {
        return status;
    }

    /**
     * Get the type of server that is being run.
     * Can be GAME, LOBBY, or OTHER.
     * @return Server type.
     */
    public InstanceType getType() {
        return type;
    }

    /**
     * Get the full version the server is running, as a String.
     * @return String form of the server version.
     */
    public String getVersion() {
        return "1." + majorVersion + "." + minorVersion;
    }
}