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

		if (cmd == Protocol.JOIN_GAME) {
			String token = new String(data, 1, TOKEN_SIZE);

			if (!games.containsKey(token)) {
				socket.send(Protocol.createJoinErrorPacket("Invalid Token".getBytes()));
				return;
			}

			Game game = games.get(token);

			String username = new String(data, TOKEN_SIZE + 1, data.length - TOKEN_SIZE - 1);
			
			if (username.isBlank() || username.isEmpty()) { // regex check
				return;
			}

			if (game.contains(username)) {
				socket.send(Protocol.createJoinErrorPacket("This username already exists in this game".getBytes()));
				return;
			}

			socket.send(Protocol.createGameServedPacket(
				token.getBytes(),
				game.isPublic(),
				game.getMaxPlayers(),
				game.getRounds(),
				game.getTurnDuration()));
			
				game.addPlayer(socket, username);
		} else if (cmd == Protocol.CREATE_GAME) {
			// Read packet data
			boolean open = data[1] != 0;
			int maxPlayers = data[2] & 0xFF;
			int rounds = data[3] & 0xFF;
			int turnDuration = data[4] & 0xFF;
			String username = new String(data, 5, data.length - 5);

			// Check values

			// Generate token
			String token = generateToken(TOKEN_SIZE);
			
			// Send game information to client
			socket.send(Protocol.createGameServedPacket(
				token.getBytes(),
				open, maxPlayers,
				rounds,
				turnDuration));
			
			// Create new game
			Game game = new Game(open, maxPlayers, rounds, turnDuration);
			game.addPlayer(socket, username.toString());
			games.put(token, game);
		} else if (cmd == Protocol.JOIN_RND) {
			String username = new String(data, 1, data.length - 1);
			
			for (String token : games.keySet()) {
				Game game = games.get(token);

				if (game.isPublic() && game.canJoin()) {
					socket.send(Protocol.createGameServedPacket( // method for sending packet and adding to game
						token.getBytes(),
						game.isPublic(),
						game.getMaxPlayers(),
						game.getRounds(),
						game.getTurnDuration()));

					game.addPlayer(socket, username);
					return;
				}
			}

			socket.send(Protocol.createJoinErrorPacket("No games available".getBytes()));
		} else if (cmd == Protocol.START) {
			Game game = getGame(socket);

			if (game != null) {
				game.start(socket);
			}
		} else if (cmd == Protocol.DRAW_BUFFER || cmd == Protocol.MOUSE_UP || cmd == Protocol.SET_COLOR || cmd == Protocol.SET_WIDTH) {
			Game game = getGame(socket);

			if (game != null) { // Check source
				game.broadcast(data);
			}
		} else if (cmd == Protocol.MSG) {
			Game game = getGame(socket);

			if (game != null) {
				game.messageFrom(socket, data);
			}
		}
	}

	static void delete(Game game) {
		for (String token : games.keySet()) {
			if (games.get(token) == game) {
				game.release();
				games.remove(token);
			}
		}
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
		String result;
		
		do {
			result = Protocol.generateRandomToken();
		} while (games.containsKey(result.toString()));

		return result;
	}

}