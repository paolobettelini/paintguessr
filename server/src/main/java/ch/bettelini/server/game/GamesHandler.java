package ch.bettelini.server.game;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import org.java_websocket.WebSocket;

public class GamesHandler {

	private final static int TOKEN_SIZE = 5;
	
	private static Map<String, Game> games;
	protected static Timer scheduler;

	static {
		games = new HashMap<>();
		scheduler = new Timer();
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
			socket.send(createGameServedPacket(token.getBytes(), game.isPublic(), (byte)game.getMaxPlayers(), (byte)game.getRounds(), (byte)game.getTurnDuration()));
			game.addPlayer(socket, username);
		} else if (cmd == Game.CREATE_GAME) {
			// Read packet data
			boolean open = data[1] != 0;
			int maxPlayers = data[2] & 0xFF;
			int rounds = data[3] & 0xFF;
			int turnDuration = data[4] & 0xFF;
			String username = new String(data, 5, data.length - 5);

			// Check values

			// Generate token
			String token = generateToken(TOKEN_SIZE);
			
			// Send token to client
			socket.send(createGameServedPacket(token.getBytes(), open, (byte)maxPlayers, (byte)rounds, (byte)turnDuration));
			
			// Create new game
			Game game = new Game(open, maxPlayers, rounds, turnDuration);
			game.addPlayer(socket, username.toString());
			games.put(token, game);
		} else if (cmd == Game.JOIN_RND) {
			for (String token : games.keySet()) {
				Game game = games.get(token);

				if (game.isPublic() && game.canJoin()) {
					String username = new String(data, 1, data.length - 1);
					socket.send(createGameServedPacket(token.getBytes(), game.isPublic(), (byte)game.getMaxPlayers(), (byte)game.getRounds(), (byte)game.getTurnDuration()));
					game.addPlayer(socket, username);
					return;
				}
			}

			socket.send(createPacket((byte) Game.JOIN_ERROR, "No games available".getBytes()));
		} else if (cmd == Game.START) {
			Game game = getGame(socket);

			if (game != null) {
				game.start(socket);
			}
		} else if (cmd == Game.DRAW_BUFFER || cmd == Game.MOUSE_UP || cmd == Game.SET_COLOR || cmd == Game.SET_WIDTH) {
			Game game = getGame(socket);

			if (game != null) { // Check source
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

	protected static byte[] createGameServedPacket(byte[] token, boolean open, byte maxPlayers, byte rounds, byte turnDuration) {
		byte[] packet = new byte[TOKEN_SIZE + 5];

		packet[0] = Game.GAME_SERVED;

		for (int i = 0; i < TOKEN_SIZE; i++) {
			packet[i + 1] = token[i];
		}

		packet[TOKEN_SIZE + 1] = (byte) (open ? ~0 : 0);
		packet[TOKEN_SIZE + 2] = maxPlayers;
		packet[TOKEN_SIZE + 3] = rounds;
		packet[TOKEN_SIZE + 4] = turnDuration;

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