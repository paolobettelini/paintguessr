package ch.bettelini.server.game;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.java_websocket.WebSocket;

public class GamesHandler {

	private final static int TOKEN_SIZE = 5;
	private static Map<String, Game> games;

	static {
		games = new HashMap<>();
	}

	public static void processRequest(WebSocket socket, ByteBuffer buff) {
		if (buff.capacity() == 0) {
			return;
		}
		
		byte[] data = buff.array();
		int cmd = data[0] & 0xFF;

		if (cmd == Game.JOIN_GAME) {
			String token = new String(data, 1, TOKEN_SIZE);

			if (!games.containsKey(token)) {
				socket.send(createPacket((byte) Game.JOIN_ERROR, "Invalid token".getBytes()));
				return;
			}

			Game game = games.get(token);

			String username = new String(data, TOKEN_SIZE + 1, data.length - TOKEN_SIZE - 1);
			socket.send(createPacket((byte) Game.TOKEN_SERVED, token.getBytes()));
			game.addPlayer(socket, username);
			System.out.println("Added player to game");
		} else if (cmd == Game.CREATE_GAME) {
			// Read packet data
			boolean open = data[1] != 0;
			int maxPlayers = data[2] & 0xFF;
			int rounds = data[3] & 0xFF;
			int roundDuration = data[4] & 0xFF;
			String username = new String(data, 5, data.length - 5);

			// Check values

			// Generate token
			String token = generateToken(TOKEN_SIZE);
			
			// Send token to client
			socket.send(createPacket((byte) Game.TOKEN_SERVED, token.getBytes()));
			
			// Create new game
			Game game = new Game(open, maxPlayers, rounds, roundDuration);
			game.addPlayer(socket, username.toString());
			games.put(token, game);
		} else if (cmd == Game.JOIN_RND) {
			for (String token : games.keySet()) {
				Game game = games.get(token);

				if (game.isPublic() && !game.isFull()) {
					String username = new String(data, 1, data.length - 1);
					game.addPlayer(socket, username);
					socket.send(createPacket((byte) Game.TOKEN_SERVED, token.getBytes()));
					return;
				}
			}

			socket.send(createPacket((byte) Game.JOIN_ERROR, "No games available".getBytes()));
		} else if (cmd == Game.NEXT_ROUND) {
			Game game = getGame(socket);

			if (game != null) {

			}
		} else if (cmd == Game.DRAW_BUFFER || cmd == Game.MOUSE_UP || cmd == Game.SET_COLOR || cmd == Game.SET_WIDTH) {
			Game game = getGame(socket);

			if (game != null) {
				game.broadcast(data);
			}
		} else if (cmd == Game.MSG) {
			Game game = getGame(socket);

			if (game != null) {
				game.messageFrom(socket, data);
			}
		}
	}

	protected static byte[] createPacket(byte cmd, byte[] data) {
		byte[] packet = new byte[data.length + 1];

		packet[0] = cmd;

		for (int i = 0; i < data.length; i++) {
			packet[i + 1] = data[i];
		}

		return packet;
	}

	private static Game getGame(WebSocket socket) {
		for (Game game : games.values()) {
			if (game.contains(socket)) {
				return game;
			}
		}

		return null;
	}

	private static String generateToken(int length) {
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