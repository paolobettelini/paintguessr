package ch.bettelini.server.game;

import java.util.HashMap;
import java.util.Map;

import org.java_websocket.WebSocket;

public class Game {

	public static final int TOKEN_SERVED	= 0;	// token
	public static final int JOIN_GAME		= 1;	// token, username
	public static final int CREATE_GAME		= 2;	// public, max_players, rounds, round_duration, username
	public static final int START_GAME		= 3;	// -

	public static final int DRAW_BUFFER		= 20;	// point...
	public static final int END_DRAWING		= 21;	// -

	public static final int JOIN_ERROR		= 201;	// reason
	public static final int CREATE_ERROR	= 202;	// reason
	public static final int START_ERROR		= 203;	// reason

	//private WebSocket drawing;

	private Map<WebSocket, Player> players;
	private boolean open;
	private int maxPlayers;
	private int rounds;
	private int roundDuration;

	public Game(boolean open, int maxPlayers, int rounds, int roundDuration) {
		this.open = open;
		this.maxPlayers = maxPlayers;
		this.rounds = rounds;
		this.roundDuration = roundDuration;
		this.players = new HashMap<>();
	}

	public void addPlayer(WebSocket socket, String username) {
		players.put(socket, new Player(username));
	}

	public boolean contains(WebSocket socket) {
		return players.containsKey(socket);
	}

	public void removePlayer(WebSocket socket) {
		players.remove(socket);
	}

}