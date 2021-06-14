package ch.bettelini.server.game;

/**
 * This class is used to represent a player statistics.
 * 
 * @author Paolo Bettelii
 * @version 14.06.2021
 */
public class Player {

	/**
	 * The username of this player.
	 */
	private String username;

	/**
	 * <code>true</code> if this player has already won
	 * this turn. <code>false</code> otherwise.
	 */
	private boolean wonTurn;
	
	/**
	 * <code>true</code> if this player has already drawn
	 * for this round. <code>false</code> otherwise.
	 */
	private boolean alreadyDrawn;

	/**
	 * Default <code>Player</code> constructor
	 * 
	 * @param username the username
	 */
	public Player(String username) {
		this.username = username;
		this.wonTurn = false;
		this.alreadyDrawn = false;
	}

	/**
	 * Returns the username of this player
	 * 
	 * @return the username of this player
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Returns <code>true</code> if this player has already won
	 * this turn. <code>false</code> otherwise.
	 *
	 * @return <code>true</code> if this player has already won
	 * this turn
	 */
	public boolean hasWonTurn() {
		return wonTurn;
	}

	/**
	 * Returns <code>true</code> if this player has already drawn
	 * for this round. <code>false</code> otherwise.
	 * 
	 * @return<code>true</code> if this player has already drawn
	 * for this round.
	 */
	public boolean hasAlreadyDrawn() {
		return alreadyDrawn;
	}

	/**
	 * Resets the turn statistics.
	 */
	public void nextTurn() {
		this.wonTurn = false;
	}

	/**
	 * Resets the round statistics
	 */
	public void nextRound() {
		this.alreadyDrawn = false;
	}

	/**
	 * Sets the <code>alreadyDrawn</code> property.
	 * 
	 * @param alreadyDrawn the <code>alreadyDrawn</code> property value.
	 */
	public void hasAlreadyDrawn(boolean alreadyDrawn) {
		this.alreadyDrawn = alreadyDrawn;
	}
	
	/**
	 * Sets the <code>wonTurn</code> property.
	 * 
	 * @param wonTurn the <code>wonTurn</code> property value.
	 */
	public void hasWonTurn(boolean wonTurn) {
		this.wonTurn = wonTurn;
	}

}