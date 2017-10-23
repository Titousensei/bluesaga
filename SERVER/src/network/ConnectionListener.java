package network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import game.ServerSettings;
import utils.ServerMessage;

/**
 * @author Tyson Domitrovits
 * @date Feb 28, 2014
 */
public class ConnectionListener implements Runnable {

  /**
   * Fields
   */
  protected Server server;
  protected ServerSocket socket;
  protected int port;
  protected boolean listening;

  /**
   * Constructor
   * @param server
   * @param port
   */
  public ConnectionListener(Server server, int port) {
    this.server = server;
    this.port = port;
  }

  @Override
  public void run() {

    ServerMessage.println(false, "STARTED on port ", port);

    // Initialize the server socket
    try {
      socket = new ServerSocket(port);
    } catch (IOException e) {
      ServerMessage.println(false, "ServerListener failed to start - ", e.getMessage());
      System.exit(0);
    }

    // Start listening
    listening = true;

    // Start the loop for accepting connections
    while (listening) {

      try {

        // Accept a client connection
        Socket clientSocket = socket.accept();
        clientSocket.setSoTimeout(15000);

        // Add the client to the server
        Client clientObject = new Client(clientSocket);

        clientObject.IP = clientSocket.getInetAddress().getHostAddress().toString();
        ServerMessage.println(false, "===================== NEW CONNECTION: ", clientObject.IP);
        if (ServerSettings.geoip_cmd != null) {
          try {
            Process pr = Runtime.getRuntime()
                                .exec(ServerSettings.geoip_cmd + " " + clientObject.IP);
            pr.waitFor();
            BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line = "";
            while ((line=buf.readLine())!=null) {
              ServerMessage.println(false, "GEOIP: ", line);
            }
          }
          catch (Exception ex) {
            ex.printStackTrace();
          }
        }

        server.addClient(clientObject);

        // Start a client listener thread
        Thread th = new Thread(clientObject);
        th.setName("Client-" + clientObject.IP);
        th.start();

      } catch (IOException e) {
        ServerMessage.println(false,
            "ServerListener failed to accept connection from client - ", e.getMessage());
      }
    }

    /*
    ServerMessage.printMessage("ConnectionListener starting to listen on port " + port);

    // Initialize the server socket
    try {
      SSLServerSocketFactory sslserversocketfactory =
                      (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
      socketSSL = (SSLServerSocket) sslserversocketfactory.createServerSocket(port);

    } catch (IOException e) {
      ServerMessage.printMessage("ServerListener failed to start - " + e.getMessage());
      System.exit(0);
    }

    // Start listening
    listening = true;

    // Start the loop for accepting connections
    while (listening) {

      try {

        // Accept a client connection
        Socket clientSocket = socketSSL.accept();
        clientSocket.setSoTimeout(15000);

        // Add the client to the server
        Client clientObject = new Client(clientSocket);
        server.addClient(clientObject);

        // Start a client listener thread
        new Thread(clientObject).start();

      } catch (IOException e) {
        ServerMessage.printMessage("ServerListener failed to accept connection from client - " + e.getMessage());
      }
    }
    */
  }

  public synchronized void stop() {
    this.listening = false;
  }
}
