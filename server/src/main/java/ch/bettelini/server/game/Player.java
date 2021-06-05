package ch.bettelini.server.game;

public class Player {

	private String username;
	private boolean wonTurn, alreadyDrawn;

	public Player(String username) {
		this.username = username;
		this.wonTurn = false;
		this.alreadyDrawn = false;
	}

	public String getUsername() {
		return username;
	}

	public boolean hasWonTurn() {
		return wonTurn;
	}

	public boolean hasAlreadyDrawn() {
		return alreadyDrawn;
	}

	public void nextTurn() {
		this.wonTurn = false;
	}

	public void nextRound() {
		this.alreadyDrawn = false;
	}

	public void hasAlreadyDrawn(boolean alreadyDrawn) {
		this.alreadyDrawn = alreadyDrawn;
	}
	
	public void hasWonTurn(boolean wonTurn) {
		this.wonTurn = wonTurn;
	}

}