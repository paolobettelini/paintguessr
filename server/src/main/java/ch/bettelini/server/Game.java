package ch.bettelini.server;

import java.util.HashMap;

import org.java_websocket.WebSocket;

public class Game implements Runnable {

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
	  */

	private HashMap<WebSocket, Player> players;

	public Game() {
		players = new HashMap<>();
	}

	public void run() {

	}

	protected void addPlayer(WebSocket client, String username) {
		players.put(client, new Player(username));
	}

}