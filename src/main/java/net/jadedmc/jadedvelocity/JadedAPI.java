package net.jadedmc.jadedvelocity;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.jadedmc.jadedvelocity.instances.Instance;
import net.jadedmc.jadedvelocity.instances.InstanceType;
import net.jadedmc.jadedvelocity.minigames.Minigame;

import java.util.Optional;

public class JadedAPI {
    private static JadedVelocityPlugin plugin = null;

    public JadedAPI(JadedVelocityPlugin pl) {
        plugin = pl;
    }

    public static void sendToLobby(Player player, Minigame minigame) {
        plugin.getInstanceMonitor().getInstancesAsync().thenAccept(instances -> {
            String serverName = "";
            {
                int count = 999;

                // Loop through all online servers looking for a server to send the player to
                for (Instance instance : instances) {
                    // Make sure the server is the right mode
                    System.out.println("Instance " + instance.getName());
                    if (instance.getMinigame() != minigame) {
                        continue;
                    }

                    // Make sure the server isn't a game server.
                    if (instance.getType() != InstanceType.LOBBY) {
                        continue;
                    }

                    // Make sure there is room for another game.
                    if (instance.getOnline() >= instance.getCapacity()) {
                        System.out.println("Not enough room!");
                        continue;
                    }

                    //
                    if (instance.getOnline() < count) {
                        count = instance.getOnline();
                        serverName = instance.getName();
                    }
                }

                // If no server is found, give up ¯\_(ツ)_/¯
                if (count == 999) {
                    System.out.println("No Server Found!");
                    return;
                }

                Optional<RegisteredServer> server = plugin.getProxyServer().getServer(serverName);
                server.ifPresent((target) -> player.createConnectionRequest(target).connect());
            }
        }).whenComplete((results, error) -> error.printStackTrace());
    }
}