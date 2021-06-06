package ch.bettelini.server.game;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import org.java_websocket.WebSocket;

public class Game {

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
			socket.send(Protocol.createPlayerJoinedPacket(player.getUsername().getBytes()));
		}
		
		if (players.size() == 0) {
			admin = socket;
		}

		players.put(socket, new Player(username));

		// Notify everyone
		socket.send(Protocol.createPlayerJoinedPacket(username.getBytes()));

		if (players.size() == maxPlayers) {
			start();
		}
	}

	public void messageFrom(WebSocket socket, byte[] data) {
		String msg = new String(data, 1, data.length - 1);

		boolean status = players.get(socket).hasWonTurn();
		
		if (currentWord != null && !status && currentWord.equals(msg)) {
			// broadcast(data); notify victory
			
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
		drawing.send(new byte[]{(byte)Protocol.YOURE_DRAWING});
		
		// Choose word
		this.currentWord = "parola 1";
		byte[] obfuscated = obfuscate(currentWord).getBytes();

		drawing.send(Protocol.createUpdateWordPacket(currentWord.getBytes()));

		for (WebSocket player : players.keySet()) {
			if (player != drawing) {
				drawing.send(Protocol.createUpdateWordPacket(obfuscated));
			}

			player.send(new byte[]{(byte)Protocol.NEXT_TURN});
		}

		currentSchedulerTask = new TimerTask(){

			@Override
			public void run() {
				nextTurn();
			}
			
		};

		GamesHandler.scheduler.schedule(currentSchedulerTask, turnDuration * 1 + 3000);
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
			currentSchedulerTask.cancel();
		}
	}

	private static String obfuscate(String word) {
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < word.length(); i++) {
			builder.append(word.charAt(i) == ' ' ? ' ' : '_');
		}

		return builder.toString();
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