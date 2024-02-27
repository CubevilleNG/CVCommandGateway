package org.cubeville.cvcommandgateway;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class CommandGatewayConnection extends Thread
{
    Socket socket;
    OutputStreamWriter out;
    BufferedReader in;
    String connectionAddress;
    UUID player;
    CVCommandGateway plugin;

    public CommandGatewayConnection(CVCommandGateway plugin, Socket socket) {
        this.socket = socket;
        this.plugin = plugin;

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new OutputStreamWriter(socket.getOutputStream());
            
            player = null;
            
            start();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        
    }

    public void run() {
        try {
            String loginString = in.readLine();
            int idx = loginString.indexOf(':');
            if(idx == -1) throw new RuntimeException("Invalid login string: " + loginString);
            String loginLabel = loginString.substring(0, idx);
            String loginData = loginString.substring(idx + 1);
            if(loginLabel.equals("IP")) {
                idx = loginData.indexOf(':');
                if(idx == -1) throw new RuntimeException("Invalid login address: " + loginString);
                String loginAddress = loginData.substring(0, idx);
                String loginPort = loginData.substring(idx + 1);
                try {
                    Inet4Address clientAddress = (Inet4Address) Inet4Address.getByName(loginAddress);
                    for(ProxiedPlayer p: ProxyServer.getInstance().getPlayers()) {
                        Inet4Address playerAddress = (Inet4Address)(((InetSocketAddress)p.getSocketAddress()).getAddress());
                        if(clientAddress.equals(playerAddress)) {
                            player = p.getUniqueId();
                            break;
                        }
                    }
                    if(player == null) {
                        throw new RuntimeException("Player not found by address: " + loginString);
                    }
                }
                catch(UnknownHostException e) {
                    throw new RuntimeException("Invalid login address: " + loginString);
                }
                connectionAddress = loginData;
                plugin.registerConnection(connectionAddress, this);
            }
            else {
                throw new RuntimeException("Invalid login string: " + loginString);
            }

            while(true) {
                String message = in.readLine();
                if(message == null) break;
                if(message.length() == 0) continue;

                ProxiedPlayer p = ProxyServer.getInstance().getPlayer(player);
                if(p == null) break;

                String servername = p.getServer().getInfo().getName();
                String ipccmd = "cmdgateway|" + connectionAddress + "|" + player + "|" + message;

                plugin.ipc.sendMessage(servername, ipccmd);
            }
        }
        catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            plugin.deregisterConnection(connectionAddress);
            try {
                socket.close();
            }
            catch(IOException e) {}
        }
    }

    public void sendResponse(String message) {
        try {
            out.write(message + "\u001a", 0, message.length() + 1);
            out.flush();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }
}
