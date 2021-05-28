package ch.bettelini.server.game;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.java_websocket.WebSocket;

public class GamesHandler {

	private static Map<String, Game> games;

	static {
		games = new HashMap<>();
	}

	public static void processRequest(WebSocket socket, ByteBuffer buff) {
		if (buff.capacity() == 0) {
			return;
		}
		
		byte[] data = buff.slice(1, buff.capacity() - 1).array();

		switch (buff.get() & 0xFF) {
			case Game.JOIN_GAME:

				break;
			case Game.CREATE_GAME:
				// Read packet data
				boolean open = data[0] != 0;
				int maxPlayers = data[1] & 0xFF;
				int rounds = data[2] & 0xFF;
				int roundDuration = data[3] & 0xFF;
				StringBuilder builder = new StringBuilder();
				for (int i = 4; i < data.length; i++) {
					builder.append((char) data[i]);
				}
				String username = builder.toString();

				// Check values

				// Generate token
				String token = generateToken(5);
				
				// Send token to client
				socket.send(createPacket((byte) Game.TOKEN_SERVED, token.getBytes()));
				
				// Create new game
				Game game = new Game(open, maxPlayers, rounds, roundDuration);
				game.addPlayer(socket, username.toString());
				games.put(token, game);
				break;
			case Game.START_GAME:

				break;
			case Game.DRAW_BUFFER:
			case Game.END_DRAWING:

				break;
			default:
				break;
		}
	}

	private static byte[] createPacket(byte cmd, byte[] data) {
		byte[] packet = new byte[data.length + 1];

		packet[0] = cmd;

		for (int i = 1; i <= data.length; i++) {
			packet[i] = data[i];
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