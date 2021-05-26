package ch.bettelini.server;

import java.util.HashMap;
import java.util.UUID;

import org.java_websocket.WebSocket;

public class Game {

	/* COMMANDS */

	/**
	 * Server -> Client
	 * 
	 * 0  -> Here is your token
	 * 1  -> Couldn't join that game <reason>
	 * 
	 */

	 /**
	  * Client -> Server
	  *
	  * 0  -> Create a new game and join it <username>
	  * 1  -> Join this game <token> <username>
	  * 
	  * 16 -> My guess is <word>
	  *
	  * <command> <gameCode> <authToken>
	  */

	private Player drawing;

	private HashMap<WebSocket, Player> players;

	public Game() {
		players = new HashMap<>();
	}

	protected void addPlayer(WebSocket socket, String username) {
		players.put(socket, new Player(username));
	}

	protected boolean contains(WebSocket socket) {
		return players.containsKey(socket);
	}

	protected void removePlayer(WebSocket socket) {
		players.remove(socket);
	}

}