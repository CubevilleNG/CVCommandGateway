package org.cubeville.cvcommandgateway;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.Inet4Address;
import java.io.IOException;

import net.md_5.bungee.api.ProxyServer;

public class CommandGatewayServer implements Runnable
{
    private CVCommandGateway plugin;
        
    public CommandGatewayServer(CVCommandGateway plugin) {
        this.plugin = plugin;
        ProxyServer.getInstance().getScheduler().runAsync(plugin, this);
    }

    public void run() {
        ServerSocket serverSocket;
        
        try {
            serverSocket = new ServerSocket(22222, 5, Inet4Address.getByName(null));
            while(true) {
                Socket socket = serverSocket.accept();
                CommandGatewayConnection connection = new CommandGatewayConnection(plugin, socket);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
}
