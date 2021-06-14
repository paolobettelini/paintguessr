package ch.bettelini.server.game;

/**
 * This class contains the game server protocol implementation.
 * 
 * @author Paolo Bettelini
 * @version 16.06.2021
 */
public class Protocol {
	
	/**
	 * The charset used to generate tokens.
	 */
	private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	
	/**
	 * The token length.
	 */
	public static final int TOKEN_SIZE = 5;

	/**
	 * <p>Game served packet<p>
	 * <p>Structure:</p>
	 * <pre>
	 * >+-------+--------+---------------+
	 * | type  | length |     name      |
	 * +-------+--------+---------------+
	 * | uint8 |      1 | cmd           |
	 * | UTF-8 |      5 | token         |
	 * | uint8 |      1 | public        |
	 * | uint8 |      1 | max players   |
	 * | uint8 |      1 | rounds        |
	 * | uint8 |      1 | turn duration |
	 * +-------+--------+---------------+
	 * </pre>
	 */
	public static final int GAME_SERVED	= 0;

	/**
	 * <p>Join game packet<p>
	 * <p>Structure:</p>
	 * <pre>
	 * >+-------+--------+----------+
	 * | type  | length |   name   |
	 * +-------+--------+----------+
	 * | uint8 |      1 | cmd      |
	 * | UTF-8 |      5 | token    |
	 * | UTF-8 |      N | username |
	 * +-------+--------+----------+
	 * </pre>
	 */
	public static final int JOIN_GAME = 1;

	/**
	 * <p>Create game packet<p>
	 * <p>Structure:</p>
	 * <pre>
	 * >+-------+--------+---------------+
	 * | type  | length |     name      |
	 * +-------+--------+---------------+
	 * | uint8 |      1 | cmd           |
	 * | uint8 |      1 | public        |
	 * | uint8 |      1 | max players   |
	 * | uint8 |      1 | rounds        |
	 * | uint8 |      1 | turn duration |
	 * | UTF-8 |      N | username      |
	 * +-------+--------+---------------+
	 * </pre>
	 */
	public static final int CREATE_GAME = 2;
	
	/**
	 * <p>Join random packet<p>
	 * <p>Structure:</p>
	 * <pre>
	 * >+-------+--------+----------+
	 * | type  | length |   name   |
	 * +-------+--------+----------+
	 * | uint8 |      1 | cmd      |
	 * | UTF-8 |      N | username |
	 * +-------+--------+----------+
	 * </pre>
	 */
	public static final int JOIN_RND = 3;
	
	/**
	 * <p>Start packet<p>
	 * <p>Structure:</p>
	 * <pre>
	 * >+-------+--------+------+
	 * | type  | length | name |
	 * +-------+--------+------+
	 * | uint8 |      1 | cmd  |
	 * +-------+--------+------+
	 * </pre>
	 */
	public static final int START = 4;
	
	/**
	 * <p>Player joined packet<p>
	 * <p>Structure:</p>
	 * <pre>
	 * >+-------+--------+----------+
	 * | type  | length |   name   |
	 * +-------+--------+----------+
	 * | uint8 |      1 | cmd      |
	 * | UTF-8 |      N | username |
	 * +-------+--------+----------+
	 * </pre>
	 */
	public static final int PLAYER_JOIN	= 5;
	
	/**
	 * <p>Player left packet<p>
	 * <p>Structure:</p>
	 * <pre>
	 * >+-------+--------+------------+
	 * | type  | length |    name    |
	 * +-------+--------+------------+
	 * | uint8 |      1 | cmd        |
	 * | uint8 |      1 | wasDrawing |
	 * | UTF-8 |      N | username   |
	 * +-------+--------+------------+
	 * </pre>
	 */
	public static final int PLAYER_LEFT	= 6;
	
	/**
	 * <p>Next turn packet<p>
	 * <p>Structure:</p>
	 * <pre>
	 * >+-------+--------+---------+
	 * | type  | length |  name   |
	 * +-------+--------+---------+
	 * | uint8 |      1 | cmd     |
	 * | uint8 |      1 | drawing |
	 * | UTF-8 |      N | word    |
	 * +-------+--------+---------+
	 * </pre>
	 */
	public static final int NEXT_TURN = 7;
	
	/**
	 * <p>Game over packet<p>
	 * <p>Structure:</p>
	 * <pre>
	 * >+-------+--------+------+
	 * | type  | length | name |
	 * +-------+--------+------+
	 * | uint8 |      1 | cmd  |
	 * +-------+--------+------+
	 * </pre>
	 */
	public static final int GAME_OVER = 8;

	/**
	 * <p>Draw buffer packet<p>
	 * <p>Structure:</p>
	 * <pre>
	 * >X
	 * </pre>
	 */
	public static final int DRAW_BUFFER	= 20;
	
	/**
	 * <p>Mouse up packet<p>
	 * <p>A point is a 4-byte structure containing the
	 * x and y position.</p>
	 * <p>Each coordinate is a point within the range [0;1]</p>
	 * <p>coord=(b1 << 8 | b0) / 2^16</p>
	 * <p>Structure:</p>
	 * <pre>
	 * >+-------+--------+--------+
	 * | type  | length |  name  |
	 * +-------+--------+--------+
	 * | uint8 |      1 | cmd    |
	 * | Point |      N | points |
	 * +-------+--------+--------+
	 * </pre>
	 */
	public static final int MOUSE_UP = 21;
	
	/**
	 * <p>Set color packet<p>
	 * <p>Structure:</p>
	 * <pre>
	 * >+-------+--------+------+
	 * | type  | length | name |
	 * +-------+--------+------+
	 * | uint8 |      1 | cmd  |
	 * | uint8 |      1 | r    |
	 * | uint8 |      1 | g    |
	 * | uint8 |      1 | b    |
	 * +-------+--------+------+
	 * </pre>
	 */
	public static final int SET_COLOR = 22;
	
	/**
	 * <p>Set width packet<p>
	 * <p>Structure:</p>
	 * <pre>
	 * >+-------+--------+-----------+
	 * | type  | length |   name    |
	 * +-------+--------+-----------+
	 * | uint8 |      1 | cmd       |
	 * | uint8 |      1 | thickness |
	 * +-------+--------+-----------+
	 * </pre>
	 */
	public static final int SET_WIDTH = 23;
	
	/**
	 * <p>Undo packet<p>
	 * <p>Structure:</p>
	 * <pre>
	 * >+-------+--------+------+
	 * | type  | length | name |
	 * +-------+--------+------+
	 * | uint8 |      1 | cmd  |
	 * +-------+--------+------+
	 * </pre>
	 */
	public static final int UNDO = 24;

	/**
	 * <p>Chat message packet<p>
	 * <p>Structure:</p>
	 * <pre>
	 * >+-------+--------+-----------+
	 * | type  | length |   name    |
	 * +-------+--------+-----------+
	 * | uint8 |      1 | cmd       |
	 * | uint8 |      1 | spectator |
	 * | UTF-8 |      N | message   |
	 * +-------+--------+-----------+
	 * </pre>
	 */
	public static final int MSG	= 30;
	
	/**
	 * <p>Add score packet<p>
	 * <p>Structure:</p>
	 * <pre>
	 * >+-------+--------+----------+
	 * | type  | length |   name   |
	 * +-------+--------+----------+
	 * | uint8 |      1 | cmd      |
	 * | uint8 |      1 | amount   |
	 * | UTF-8 |      N | username |
	 * +-------+--------+----------+
	 * </pre>
	 */
	public static final int ADD_SCORE = 31;

	/**
	 * <p>Join error packet<p>
	 * <p>Structure:</p>
	 * <pre>
	 * >+-------+--------+--------+
	 * | type  | length |  name  |
	 * +-------+--------+--------+
	 * | uint8 |      1 | cmd    |
	 * | UTF-8 |      1 | reason |
	 * +-------+--------+--------+
	 * </pre>
	 */
	public static final int JOIN_ERROR	= 201;

	/**
	 * Creates a game over packet.
	 * 
	 * @return the packet
	 */
	public static byte[] createGameOverPacket() {
		return new byte[] {(byte) GAME_OVER};
	}

	/**
	 * Creates a message packet.
	 * 
	 * @param message the message
	 * @param spectator <code>true</code> if the sender is a spectator
	 * @return the packet
	 */
	public static byte[] createMessagePacket(byte[] message, boolean spectator) {
		byte[] packet = new byte[2 + message.length];

		packet[0] = (byte) MSG;
		packet[1] = (byte) (spectator ? ~0 : 0);

		for (int i = 0; i < message.length; i++) {
			packet[i + 2] = message[i];
		}

		return packet;
	}

	/**
	 * Creates a join error packet.
	 * 
	 * @param reason the error reason
	 * @return the packet
	 */
	public static byte[] createJoinErrorPacket(byte[] reason) {
		return createPacket((byte) JOIN_ERROR, reason);
	}

	/**
	 * Creates an add score packet.
	 * 
	 * @param amount the amount to add
	 * @param username the username of the receiver
	 * @return the packet
	 */
	public static byte[] createAddScorePacket(int amount, byte[] username) {
		byte[] packet = new byte[username.length + 2];

		packet[0] = ADD_SCORE;
		packet[1] = (byte) amount;

		for (int i = 0; i < username.length; i++) {
			packet[i + 2] = username[i];
		}

		return packet;
	}

	/**
	 * Creates a next turn packet.
	 * 
	 * @param drawing <code>true</code> if the receiver shall now draw
	 * @param word the word (obfuscated if the receiver is not going to draw it)
	 * @return the packet
	 */
	public static byte[] createNextTurnPacket(boolean drawing, byte[] word) {
		byte[] packet = new byte[word.length + 2];

		packet[0] = (byte) NEXT_TURN;
		packet[1] = (byte) (drawing ? ~0 : 0);

		for (int i = 0; i < word.length; i++) {
			packet[i + 2] = word[i];
		}

		return packet;
	}

	/**
	 * Creates a player joined packet
	 * 
	 * @param username the username of the player
	 * @return the packet
	 */
	public static byte[] createPlayerJoinedPacket(byte[] username) {
		return createPacket((byte) PLAYER_JOIN, username);
	}

	/**
	 * Creates a player left packet.
	 * 
	 * @param username the username of the player
	 * @param drawing <code>true</code> if the player was drawing
	 * @return the packet
	 */
	public static byte[] createPlayerLeftPacket(byte[] username, boolean drawing) {
		byte[] packet = new byte[2 + username.length];

		packet[0] = (byte) PLAYER_LEFT;
		packet[1] = (byte) (drawing ? ~0 : 0);

		for (int i = 0; i < username.length; i++) {
			packet[i + 2] = username[i];
		}

		return packet;
	}

	/**
	 * Creates a game served packet.
	 * 
	 * @param token the token
	 * @param open <code>true</code> if the game is public
	 * @param maxPlayers the maximum number of players
	 * @param rounds the number of rounds
	 * @param turnDuration the duration of each turn
	 * @return the packet
	 */
	public static byte[] createGameServedPacket(byte[] token, boolean open, int maxPlayers, int rounds, int turnDuration) {
		byte[] packet = new byte[TOKEN_SIZE + 5];

		packet[0] = GAME_SERVED;

		for (int i = 0; i < TOKEN_SIZE; i++) {
			packet[i + 1] = token[i];
		}

		packet[TOKEN_SIZE + 1] = (byte) (open ? ~0 : 0);
		packet[TOKEN_SIZE + 2] = (byte) maxPlayers;
		packet[TOKEN_SIZE + 3] = (byte) rounds;
		packet[TOKEN_SIZE + 4] = (byte) turnDuration;

		return packet;
	}

	/**
	 * Creates a generic packet.
	 * 
	 * @param cmd the command
	 * @param data the data of the packet
	 * @return the packet
	 */
	private static byte[] createPacket(byte cmd, byte[] data) {
		byte[] packet = new byte[data.length + 1];

		packet[0] = cmd;

		for (int i = 0; i < data.length; i++) {
			packet[i + 1] = data[i];
		}

		return packet;
	}

	/**
	 * Generates a random token.
	 * 
	 * @return the generated token
	 */
	public static String generateRandomToken() {
		StringBuilder result = new StringBuilder();

		for (int i = 0; i < TOKEN_SIZE; i++) {
			result.append(CHARACTERS.charAt((int) (Math.random() * CHARACTERS.length())));
		}

		return result.toString();
	}

}