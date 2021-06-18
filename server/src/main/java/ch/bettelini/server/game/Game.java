package ch.bettelini.server.game;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import org.java_websocket.WebSocket;

import ch.bettelini.server.game.utils.Words;

/**
 * This class is used to handle a game instance.
 * 
 * @author Paolo Bettelii
 * @version 14.06.2021
 */
public class Game {

	/**
	 * Associates to each <code>WebSocket</code>
	 * a <code>Player</code> object.
	 */
	private Map<WebSocket, Player> players;
	
	/**
	 * The player who is drawing.
	 */
	private WebSocket drawing;
	
	/**
	 * The creator of the game.
	 */
	private WebSocket admin;

	/**
	 * <code>true</code> if this game is a public room
	 * <code>false</code> otherwise.
	 */
	private boolean open;

	/**
	 * The maximum number of players for this room.
	 */
	private int maxPlayers;

	/**
	 * The number of rounds for this game.
	 */
	private int rounds;

	/**
	 * <p>The duration of each turn in seconds.</p>
	 * <p>The turn will immediately end if all the
	 * players who are not drawing have guessed the word.</p>
	 */
	private int turnDuration;

	/**
	 * <p>The current round number.</p>
	 * <p>[<code>1</code>; {@link #rounds}].</p>
	 * <p><code>0</code> if the game hasn't started.</p>
	 */
	private int currentRound;

	/**
	 * The current turn number.
	 */
	private int currentTurn;
	
	/**
	 * Unix time for when the current round began.
	 */
	private long roundStartTime;

	/**
	 * The current word that is being drawn.
	 */
	private String currentWord;

	/**
	 * <code>List</code> of words that have already been drawn.
	 * @see {@link #currentWord} the word that is being drawn
	 */
	private List<String> lastWords;

	/**
	 * The <code>TimerTask</code> for scheduling the end
	 * of the current round.
	 */
	private TimerTask currentSchedulerTask;

	/**
	 * Default <code>Game</code> constructor.
	 * 
	 * @param open <code>true</code> if the room is public, <code>false</code> otherwise
	 * @param maxPlayers the maximum number of players
	 * @param rounds the number of rounds
	 * @param turnDuration the duration of each turn
	 */
	protected Game(boolean open, int maxPlayers, int rounds, int turnDuration) {
		this.open = open;
		this.maxPlayers = maxPlayers;
		this.rounds = rounds;
		this.turnDuration = turnDuration;
		
		this.lastWords = new LinkedList<>();
		this.currentSchedulerTask = null;
		this.players = new HashMap<>();
		this.currentRound = 0;
		this.currentTurn = 0;
	}

	/**
	 * Elaborates a package from a <code>WebSocket</code> playing in this game.
	 * 
	 * @param socket the packet source
	 * @param data the raw packet data
	 */
	protected void processPacket(WebSocket socket, byte[] data) {
		int cmd = data[0] & 0xFF;

		switch (cmd) {
			case Protocol.SET_WIDTH:
			case Protocol.MOUSE_UP:
			case Protocol.DRAW_BUFFER:
			case Protocol.SET_COLOR:
			case Protocol.UNDO:
				if (drawing != null && drawing == socket) {
					broadcastDrawingPacket(data);
				}
				break;
			case Protocol.START:
				start(socket);
				break;
			case Protocol.MSG:
				messageFrom(socket, data);
				break;
		}
	}

	/**
	 * Adds a player to the game.
	 * 
	 * @param socket the <code>WebSocket</code>
	 * @param username the username for this player
	 */
	protected void addPlayer(WebSocket socket, String username) {
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

	/**
	 * Elaborates a message from a <code>WebSocket</code>.
	 * 
	 * @param socket the message source
	 * @param data the raw packet data
	 */
	public void messageFrom(WebSocket socket, byte[] data) {
		Player sender = players.get(socket);
		boolean status = sender.hasWonTurn();

		String msg = new String(data, 1, data.length - 1);

		if (currentWord != null && !status && currentWord.equalsIgnoreCase(msg.trim())) {
			int amount = 100 - (int) (System.currentTimeMillis() - roundStartTime) / turnDuration / 10;
			broadcast(Protocol.createAddScorePacket(amount, sender.getUsername().getBytes()));
			
			sender.hasWonTurn(true);

			if (isTurnComplete()) {
				forceNextTurn();
			}

			return;
		}

		byte[] packet = Protocol.createMessagePacket((sender.getUsername() + ": " + msg).getBytes(), sender.hasWonTurn());

		for (WebSocket player : players.keySet()) {
			if (!status || players.get(player).hasWonTurn()) {
				player.send(packet);
			}
		}
	}

	/**
	 * Returns <code>true</code> if all the players have guessed the
	 * word for this turn. <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> the all the players have guessed the word.
	 * 	<code>false</code> otherwise
	 */
	private boolean isTurnComplete() {
		for (Player player : players.values()) {
			if (!player.hasWonTurn()) {
				return false;
			}
		}

		return true;
	}

	/**
	 * <p>Elaborates a start request from a <code>WebSocket</code>.</p>
	 * <p>The game will start is the packet source is the creator of the room
	 * and the room contains at least 2 players</p>
	 * 
	 * @param from the packet source.
	 */
	protected void start(WebSocket from) {
		if (from == admin && players.size() > 1 && !hasStarted()) {
			start();
		}
	}

	/**
	 * Forces the current turn to end.
	 */
	private void forceNextTurn() {
		release();
		nextTurn();
	}

	/**
	 * Forces the game to start.
	 */
	private void start() {
		nextTurn();
		++currentRound;
	}

	/**
	 * This method is invoked by the <code>TimerTask</code>
	 * at the end of a turn.
	 * @see {@link #start()} here the recursive operation is initialized
	 * @see {@link #currentSchedulerTask} the current scheduler task
	 */
	private void nextTurn() {
		if (++currentTurn > players.size()) {
			currentTurn = 1;

			if (++currentRound > rounds) {
				GameOver();
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
		this.currentWord = Words.random();
		do {
			this.currentWord = Words.random();
			// stop generating unique words after 25
		} while (lastWords.size() < 25 && lastWords.contains(this.currentWord));
		lastWords.add(currentWord);

		byte[] packet = Protocol.createNextTurnPacket(false, Words.obfuscate(currentWord).getBytes());
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

		GamesHandler.scheduler.schedule(currentSchedulerTask, turnDuration * 1000);
		roundStartTime = System.currentTimeMillis();
	}

	/**
	 * Returns the size of the game.
	 * @return
	 */
	public int size() {
		return players.size();
	}

	/**
	 * Return <code>true</code> if the game hasn't began and
	 * it has at least one free player slot. <code>false</code> otherwise.
	 *
	 * @return <code>true</code> if a player could join this game. <code>false</code> otherwise.
	 */
	public boolean canJoin() {
		return !hasStarted() && !isFull();
	}

	/**
	 * Returns <code>true</code> if the game has already started.
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if the game has already started,
	 * <code>false</code> otherwise
	 */
	public boolean hasStarted() {
		return currentRound != 0;
	}

	/**
	 * Returns <code>true</code> if this game contains a given
	 * <code>WebSocket</code> instance, <code>false</code> otherwise.
	 * 
	 * @param socket the <code>WebSocket</code> to check
	 * @return <code>true</code> if the <code>WebSocket</code> is present.
	 * 	<code>false</code> otherwise.
	 */
	public boolean contains(WebSocket socket) {
		return players.containsKey(socket);
	}

	/**
	 * Removes a <code>WebSocket</code> from this game.
	 * 
	 * @param socket the <code>WebSocket</code> to remove.
	 */
	public void removePlayer(WebSocket socket) {
		byte[] username = players.get(socket).getUsername().getBytes();
		players.remove(socket);

		boolean wasDrawing = socket == drawing;
		broadcast(Protocol.createPlayerLeftPacket(username, wasDrawing));

		if (players.size() < 2) {
			GameOver();
		} else if (wasDrawing) {
			--currentTurn;
			forceNextTurn();
		} else if (isTurnComplete()) {
			forceNextTurn();
		}
	}

	/**
	 * Returns <code>true</code> if this game is public,
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if this game is public,
	 * <code>false</code> otherwise
	 */
	public boolean isPublic() {
		return open;
	}

	/**
	 * Return <code>true</code> is this game is already full,
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> is this game is already full
	 */
	public boolean isFull() {
		return players.size() == maxPlayers;
	}
	
	/**
	 * Broadcastes a packet to every <code>WebSocket</code> in this game.
	 * 
	 * @param data the packet to broadcast
	 */
	private void broadcast(byte[] data) {
		for (WebSocket socket : players.keySet()) {
			socket.send(data);
		}
	}

	/**
	 * Broadcastes a packet to every <code>WebSocket</code> in this game
	 * except for the one who is drawing.
	 * 
	 * @param data the packet to broadcast
	 */
	private void broadcastDrawingPacket(byte[] data) {
		for (WebSocket socket : players.keySet()) {
			if (socket != drawing) {
				socket.send(data);
			}
		}
	}

	/**
	 * Returns <code>true</code> if a given username is already
	 * used within this game, <code>false</code> otherwise.
	 * 
	 * @param username the username to check
	 * @return <code>true</code> is this username is already used
	 */
	public boolean contains(String username) {
		for (Player player : players.values()) {
			if (player.getUsername().equals(username)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Forces the game to end.
	 */
	private void GameOver() {
		broadcast(Protocol.createGameOverPacket());

		GamesHandler.delete(this);
	}

	/**
	 * <p>Releases the resources that this instance is using.</p>
	 * <p>Call this method before deleting this instance</p>
	 */
	protected void release() {
		if (currentSchedulerTask != null) {
			currentSchedulerTask.cancel();
		}
	}

	/**
	 * Returns the number of rounds of this game.
	 * 
	 * @return the number of rounds
	 */
	public int getRounds() {
		return rounds;
	}

	/**
	 * Returns the duration in seconds of each turn.
	 * 
	 * @return the duration in seconds of each turn
	 */
	public int getTurnDuration() {
		return turnDuration;
	}

	/**
	 * Returns the maximum numbers of players for this game.
	 * 
	 * @return the maximum numbers of players.
	 */
	public int getMaxPlayers() {
		return maxPlayers;
	}

}