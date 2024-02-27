package org.cubeville.cvcommandgateway;

import java.util.HashMap;
import java.util.Map;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;

import org.cubeville.cvipc.CVIPC;
import org.cubeville.cvipc.IPCInterface;

public class CVCommandGateway extends Plugin implements IPCInterface {

    CVIPC ipc;
    CommandGatewayServer server;
    Map<String, CommandGatewayConnection> connections = new HashMap<>();

    @Override
    public void onEnable() {
        PluginManager pm = getProxy().getPluginManager();

        ipc = (CVIPC) pm.getPlugin("CVIPC");
        ipc.registerInterface("cmdgateway", this);

        server = new CommandGatewayServer(this);
    }

    public void registerConnection(String connectionName, CommandGatewayConnection connection) {
        connections.put(connectionName, connection);
    }

    public void deregisterConnection(String connectionName) {
        connections.remove(connectionName);
    }

    public void process(String serverName, String channel, String message) {
        if(!channel.equals("cmdgateway")) return;

        int idx = message.indexOf('|');
        if(idx == -1) return;

        String connectionAddress = message.substring(0, idx);
        String data = message.substring(idx + 1);
        if(data.length() == 0) return;

        if(! data.endsWith("\n"))
            data += '\n';

        CommandGatewayConnection connection = connections.get(connectionAddress);
        if(connection != null) {
            connection.sendResponse(data);
        }
    }
}
