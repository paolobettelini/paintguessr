package ch.bettelini.server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import ch.bettelini.server.game.GamesHandler;

/**
 * This class is used to handle every <code>WebSocket</code> connection.
 * 
 * @author Paolo Bettelini
 * @version 16.06.2021
 */
public class Server extends WebSocketServer {

	/**
	 * Main function.
	 * 
	 * @param args the ip address and the port
	 */
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Parameters: <IPv4> <port>");
			System.out.println("Example:");
			System.out.println("java -jar -Xmx1024m server.jar 192.168.1.2 4242");
			return;
		}
		
		Server server;
		try {
			server = new Server(new InetSocketAddress(args[0], Integer.parseInt(args[1])));
		} catch (IllegalArgumentException e) {
			System.err.println("Invalid port number");
			return;
		} catch (SecurityException e) {
			System.err.println("Couldn't resolve the hostname - Permission denied");
			return;
		}

		server.start();
	}

	/**
	 * Creates a new server.
	 * 
	 * @param address the address and port
	 */
	public Server(InetSocketAddress address) {
		super(address);

		try { Class.forName("ch.bettelini.server.game.GamesHandler"); }
		catch (ClassNotFoundException e) { e.printStackTrace(); }
	}

	@Override
	public void onStart() {
		System.out.println("Server started!");

		setConnectionLostTimeout(0);
		setConnectionLostTimeout(100);
	}

	@Override
	public void onMessage(WebSocket client, ByteBuffer buff) {
		GamesHandler.processRequest(client, buff);
	}

	@Override
	public void onError(WebSocket client, Exception e) {
		e.printStackTrace();
	}

	@Override
	public void onOpen(WebSocket client, ClientHandshake handshake) {

	}

	@Override
	public void onClose(WebSocket client, int code, String reason, boolean remote) {
		GamesHandler.removeSocket(client);
	}

	@Override
	public void onMessage(WebSocket client, String message) {

	}

}