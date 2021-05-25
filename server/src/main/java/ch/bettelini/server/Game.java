package ch.bettelini.server;

import java.util.ArrayList;
import java.util.List;

import org.java_websocket.WebSocket;

public class Game implements Runnable {

	/* COMMANDS */

	/**
	 * Server -> Client
	 * 
	 * 0 -> Here is your token
	 */

	 /**
	  * Client -> Server
	  *
	  * 0 -> Create a new game
	  * 1 -> Join this game
	  */

	private List<WebSocket> players;

	public Game() {
		players = new ArrayList<>();
	}

	public void run() {

	}

	protected void addPlayer(WebSocket player) {		
		players.add(player);
	}

}