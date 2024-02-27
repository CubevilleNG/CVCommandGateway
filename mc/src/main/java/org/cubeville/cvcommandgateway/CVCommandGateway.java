package org.cubeville.cvcommandgateway;

import java.util.StringTokenizer;

import org.bukkit.plugin.PluginManager;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import org.cubeville.cvtools.CVTools;

import org.cubeville.cvipc.CVIPC;
import org.cubeville.cvipc.IPCInterface;

public class CVCommandGateway extends JavaPlugin implements IPCInterface
{
    CVIPC ipc;
    
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();
        ipc = (CVIPC) pm.getPlugin("CVIPC");
        ipc.registerInterface("cmdgateway", this);
    }

    public void onDisable() {
        ipc.deregisterInterface("cmdgateway");
    }

    public void process(String channel, String message) {
        if(! channel.equals("cmdgateway")) return;

        StringTokenizer tk = new StringTokenizer(message, "|");
        if(tk.countTokens() != 3) return;

        String connectionId = tk.nextToken();
        String playerId = tk.nextToken();
        String command = tk.nextToken();

        String response = CVTools.getInstance().runCommand(playerId, command);
        ipc.sendMessage("cmdgateway|" + connectionId + "|" + response);
    }
}
