package ch.bettelini.server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import ch.bettelini.server.game.GamesHandler;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class Server extends WebSocketServer {

	public static void main(String[] args) {
		//new Server(new InetSocketAddress("192.168.1.104", 4242)).start();
		new Server(new InetSocketAddress("127.0.0.1", 3333)).start();
	}

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