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

		// load Words class
		try { Class.forName("ch.bettelini.server.game.utils.Words"); }
		catch (ClassNotFoundException e) { e.printStackTrace(); }
	}

	public static void processRequest(WebSocket socket, ByteBuffer buff) {
		if (buff.capacity() == 0) {
			return;
		}
		
		byte[] data = buff.array();
		int cmd = data[0] & 0xFF;

		Game game = getGame(socket);
		
		if (game == null) {
			switch (cmd) {
				case Protocol.CREATE_GAME:
					createGame(socket, data);
					break;
				case Protocol.JOIN_GAME:
					joinGame(socket, data);
					break;
				case Protocol.JOIN_RND:
					joinRandom(socket, data);
					break;
			}
		} else {
			game.processPacket(socket, data);
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

	private static void createGame(WebSocket socket, byte[] data) {
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
	}

	private static void joinGame(WebSocket socket, byte[] data) {
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
			socket.send(Protocol.createJoinErrorPacket("This username already is laready used in this game".getBytes()));
			return;
		}

		socket.send(Protocol.createGameServedPacket(
			token.getBytes(),
			game.isPublic(),
			game.getMaxPlayers(),
			game.getRounds(),
			game.getTurnDuration()));
		
		game.addPlayer(socket, username);
	}

	private static void joinRandom(WebSocket socket, byte[] data) {
		String username = new String(data, 1, data.length - 1);
			
		Game game;
		for (String randomToken : games.keySet()) {
			game = games.get(randomToken);

			if (game.isPublic() && game.canJoin()) {
				socket.send(Protocol.createGameServedPacket(
					randomToken.getBytes(),
					game.isPublic(),
					game.getMaxPlayers(),
					game.getRounds(),
					game.getTurnDuration()));

				game.addPlayer(socket, username);
				return;
			}
		}

		socket.send(Protocol.createJoinErrorPacket("No games available".getBytes()));
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

	public static void removeSocket(WebSocket socket) {
		Game game = getGame(socket);

		if (game != null) {
			game.removePlayer(socket);
		}
	}

	protected static void log() {
		for (String token : games.keySet()) {
			System.out.println(token + "\t" + games.get(token).size() + " players");
		}
		System.out.println("-------------------------------");
	}

}