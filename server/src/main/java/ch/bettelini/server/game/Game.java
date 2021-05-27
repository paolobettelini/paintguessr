package ch.bettelini.server.game;

import java.util.HashMap;
import java.util.Map;

import org.java_websocket.WebSocket;

public class Game {

	//private WebSocket drawing;

	private Map<WebSocket, Player> players;

	public Game() {
		players = new HashMap<>();
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