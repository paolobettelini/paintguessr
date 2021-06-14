package ch.bettelini.server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import ch.bettelini.server.game.GamesHandler;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * This class is used to handle every <code>WebSocket</code connection.
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
			System.out.println("Parameters: <IPv4> <port> [ram]");
			System.out.println("Example:");
			System.out.println("java -jar server.jar 192.168.1.2 4242 -Xmx1024m");
			return;
		}
		try {
			new Server(new InetSocketAddress(args[0], Integer.parseInt(args[1]))).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
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