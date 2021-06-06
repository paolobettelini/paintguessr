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
	private long roundStartTime;
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
		broadcast(Protocol.createPlayerJoinedPacket(username.getBytes()));

		if (players.size() == maxPlayers) {
			start();
		}
	}

	public void messageFrom(WebSocket socket, byte[] data) {
		Player sender = players.get(socket);
		boolean status = sender.hasWonTurn();

		// Remove '<username>: '
		String msg = new String(data, 1, data.length - 1).substring(sender.getUsername().length() + 2);

		if (currentWord != null && !status && currentWord.equalsIgnoreCase(msg)) {
			int amount = (int) (System.currentTimeMillis() - roundStartTime - 3000) / turnDuration / 10 + 100;
			broadcast(Protocol.createAddScorePacket(amount, sender.getUsername().getBytes()));
			
			sender.hasWonTurn(true);
			return;
		}

		for (WebSocket player : players.keySet()) {
			if (!status || players.get(player).hasWonTurn()) {
				player.send(data);
			}
		}
	}

	public void start(WebSocket from) {
		if (from == admin && players.size() > 1 && !hasStarted()) {
			start();
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
				GamesHandler.delete(this);
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
		
		// Choose word
		this.currentWord = "parola 1";

		byte[] packet = Protocol.createNextTurnPacket(false, obfuscate(currentWord).getBytes());
		byte[] drawingPacket = Protocol.createNextTurnPacket(true, currentWord.getBytes());
		
		drawing.send(drawingPacket);

		for (WebSocket player : players.keySet()) {
			if (player != drawing) {
				player.send(packet);
			}
		}

		currentSchedulerTask = new TimerTask(){

			@Override
			public void run() {
				nextTurn();
			}
			
		};

		GamesHandler.scheduler.schedule(currentSchedulerTask, turnDuration * 1000 + 3000);
		roundStartTime = System.currentTimeMillis();
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