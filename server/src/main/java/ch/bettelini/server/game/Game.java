package ch.bettelini.server.game;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import org.java_websocket.WebSocket;

public class Game {

	public static final int GAME_SERVED		= 0;	// token, public, max_players, rounds, turn_duration
	public static final int JOIN_GAME		= 1;	// token, username
	public static final int CREATE_GAME		= 2;	// public, max_players, rounds, turn_duration, username
	public static final int JOIN_RND		= 3;	// username
	public static final int START			= 4;	// -
	public static final int PLAYER_JOIN		= 5;	// -
	public static final int NEXT_TURN		= 6;	// -
	public static final int YOURE_DRAWING	= 7;	// -

	public static final int DRAW_BUFFER		= 20;	// point...
	public static final int MOUSE_UP		= 21;	// -
	public static final int SET_COLOR		= 22;	// r, g, b
	public static final int SET_WIDTH		= 23;	// line width

	public static final int MSG				= 30;	// message
	public static final int UPDATE_WORD		= 31;	// word

	public static final int JOIN_ERROR		= 201;	// reason
	public static final int CREATE_ERROR	= 202;	// reason
	public static final int START_ERROR		= 203;	// reason

	private Map<WebSocket, Player> players;
	
	private WebSocket drawing, admin;

	private boolean open;
	private int maxPlayers;
	private int rounds;
	private int turnDuration;

	private int currentRound;
	private int currentTurn;
	private String currentWord;
	private TimerTask currentSchedulerTask;

	public Game(boolean open, int maxPlayers, int rounds, int turnDuration) {
		this.open = open;
		this.maxPlayers = maxPlayers;
		this.rounds = rounds;
		this.turnDuration = turnDuration;
		
		this.currentSchedulerTask = null;
		this.players = new HashMap<>();
		this.currentRound = 0;
		this.currentTurn = 0;
	}

	public void addPlayer(WebSocket socket, String username) {
		// Send all previous player names
		for (Player player : players.values()) {
			socket.send(createPlayerJoinedPacket(player.getUsername()));
		}
		
		if (players.size() == 0) {
			admin = socket;
		}

		players.put(socket, new Player(username));

		// Notify everyone
		broadcast(createPlayerJoinedPacket(username));

		if (players.size() == maxPlayers) {
			start();
		}
	}

	public void messageFrom(WebSocket socket, byte[] data) {
		String msg = new String(data, 1, data.length - 1);

		boolean status = players.get(socket).hasWonTurn();
		
		if (currentWord != null && !status && currentWord.equals(msg)) {
			// broadcast(data); NOTIFY VICRTORy
			
			players.get(socket).hasWonTurn(true);

			return;
		}

		for (WebSocket player : players.keySet()) {
			if (!status || players.get(player).hasWonTurn()) {
				player.send(data);
			}
		}
	}

	public void start(WebSocket from) {
		if (from == admin) {
			if (players.size() > 1) {
				start();
			} else {
				// Send error message
			}
		}
	}

	private void start() {
		nextTurn();
		++currentRound;
	}

	private void nextTurn() {
		if (++currentTurn > players.size()) {
			currentTurn = 1;

			if (++currentRound > rounds) {
				System.out.println("Game ended");
				return;
			}

			// Reset round dependent variables
			for (Player player : players.values()) {
				player.nextRound();
			}
		}

		// Reset turn dependent variables
		for (Player player : players.values()) {
			player.nextTurn();
		}

		// Generate who's drawing next
		for (WebSocket player : players.keySet()) {
			if (!players.get(player).hasAlreadyDrawn()) {
				drawing = player;
				break;
			}
		}

		Player _drawing = players.get(drawing);
		_drawing.hasAlreadyDrawn(true);
		_drawing.hasWonTurn(true);
		drawing.send(new byte[]{(byte)Game.YOURE_DRAWING});
		
		// Choose word
		this.currentWord = "parola 1";
		byte[] obfuscated = obfuscate(currentWord).getBytes();

		drawing.send(createPacket((byte)Game.UPDATE_WORD, currentWord.getBytes()));

		for (WebSocket player : players.keySet()) {
			if (player != drawing) {
				player.send(createPacket((byte)Game.UPDATE_WORD, obfuscated));
			}

			player.send(new byte[]{(byte)Game.NEXT_TURN});
		}

		GamesHandler.scheduler.schedule(new TimerTask(){

			@Override
			public void run() {
				nextTurn();
			}
			
		}, turnDuration * 1 + 3000);
	}

	public int size() {
		return players.size();
	}

	public boolean canJoin() {
		return !hasStarted() && !isFull();
	}

	public boolean hasStarted() {
		return currentRound != 0;
	}

	public boolean contains(WebSocket socket) {
		return players.containsKey(socket);
	}

	public void removePlayer(WebSocket socket) {
		players.remove(socket);
	}

	public boolean isPublic() {
		return open;
	}

	public boolean isFull() {
		return players.size() == maxPlayers;
	}
	
	public void broadcast(byte[] data) {
		for (WebSocket socket : players.keySet()) {
			socket.send(data);
		}
	}

	public boolean contains(String username) {
		for (Player player : players.values()) {
			if (player.getUsername().equals(username)) {
				return true;
			}
		}

		return false;
	}

	public void release() {
		if (currentSchedulerTask != null) {
			// cancel task
		}
	}

	private static byte[] createPlayerJoinedPacket(String username) {
		return createPacket((byte) PLAYER_JOIN, username.getBytes());
	}

	private static String obfuscate(String word) {
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < word.length(); i++) {
			builder.append(word.charAt(i) == ' ' ? ' ' : '_');
		}

		return builder.toString();
	}

	protected static byte[] createPacket(byte cmd, byte[] data) {
		byte[] packet = new byte[data.length + 1];

		packet[0] = cmd;

		for (int i = 0; i < data.length; i++) {
			packet[i + 1] = data[i];
		}

		return packet;
	}

    public int getRounds() {
        return rounds;
    }

    public int getTurnDuration() {
        return turnDuration;
    }

	public int getMaxPlayers() {
		return maxPlayers;
	}

}