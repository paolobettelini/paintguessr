package ch.bettelini.server;

import org.java_websocket.WebSocket;

public class Player {

	private String username;
	private int score;
	private boolean wonRound;

	public Player(String username) {
		this.username = username;
		this.score = 0;
		this.wonRound = false;
	}

	public String getUsername() {
		return username;
	}

	public int getScore() {
		return score;
	}

	public void addPoints(int points) {
		this.score += points;
	}

	public boolean hasWonRound() {
		return wonRound;
	}

	public void nextRound() {
		this.wonRound = false;
	}

}