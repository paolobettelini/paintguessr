package ch.bettelini.server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import ch.bettelini.server.game.Game;

import java.util.HashMap;
import java.util.Map;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class Server extends WebSocketServer {

	private Map<String, Game> games;

	public static void main(String[] args) {
		new Server(new InetSocketAddress("127.0.0.1", 3333)).start();
	}

	public Server(InetSocketAddress address) {
		super(address);

		games = new HashMap<>();
	}

	@Override
	public void onStart() {
		System.out.println("Server started!");

		setConnectionLostTimeout(0);
		setConnectionLostTimeout(100);
	}

	@Override
	public void onOpen(WebSocket client, ClientHandshake handshake) {

	}

	@Override
	public void onClose(WebSocket client, int code, String reason, boolean remote) {

	}

	@Override
	public void onMessage(WebSocket client, String message) {
		client.send(new byte[]{2});
		
		int a = 0;
		if (a == 0) {return;}
		
		String[] args = message.split(" ");

		switch (args[0]) {// AAAAAAAAAAA
			case "create":
				Game room = new Game();
				room.addPlayer(client, args[1]);
				
				String token = generateToken(5);
				games.put(token, room);
				client.send("your room token: " + token);
				break;
			case "join":
				for (String tok : games.keySet()) {
					if (tok.equals(args[1])) {
						//games.get(tok).addPlayer(client, args[2]);
						break;
					}
				}

				break;
			default:
				break;
		}
	}

	@Override
	public void onMessage(WebSocket client, ByteBuffer message) {
		byte[] data = message.array();
		System.out.println(data[0]); // Sempre 0
		client.send(message);
	}

	@Override
	public void onError(WebSocket client, Exception e) {
		e.printStackTrace();
	}

	private String generateToken(int length) {
		String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		StringBuilder result;
		
		do {
			result = new StringBuilder();

			for (int i = 0; i < length; i++) {
				result.append(characters.charAt((int) (Math.random() * characters.length())));
			}
		} while (games.containsKey(result.toString()));

		return result.toString();
	}

}