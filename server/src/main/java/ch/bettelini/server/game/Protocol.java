package ch.bettelini.server.game;

public class Protocol {
	
	private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	public static final int TOKEN_SIZE 		= 5;

	public static final int GAME_SERVED		= 0;	// token, public, max_players, rounds, turn_duration
	public static final int JOIN_GAME		= 1;	// token, username
	public static final int CREATE_GAME		= 2;	// public, max_players, rounds, turn_duration, username
	public static final int JOIN_RND		= 3;	// username
	public static final int START			= 4;	// -
	public static final int PLAYER_JOIN		= 5;	// username
	public static final int PLAYER_LEFT		= 6;	// username
	public static final int NEXT_TURN		= 7;	// -
	public static final int YOURE_DRAWING	= 8;	// -

	public static final int DRAW_BUFFER		= 20;	// point...
	public static final int MOUSE_UP		= 21;	// -
	public static final int SET_COLOR		= 22;	// r, g, b
	public static final int SET_WIDTH		= 23;	// line width

	public static final int MSG				= 30;	// message
	public static final int UPDATE_WORD		= 31;	// word

	public static final int JOIN_ERROR		= 201;	// reason

	public static byte[] createJoinErrorPacket(byte[] reason) {
		return createPacket((byte) JOIN_ERROR, reason);
	}

	public static byte[] createUpdateWordPacket(byte[] word) {
		return createPacket((byte) UPDATE_WORD, word);
	}

	public static byte[] createYouReDrawingPacket() {
		return new byte[] { (byte) YOURE_DRAWING };
	}
	
	public static byte[] createNextTurnPaclet() {
		return new byte[] { (byte) NEXT_TURN };
	}

	public static byte[] createPlayerJoinedPacket(byte[] username) {
		return createPacket((byte) PLAYER_JOIN, username);
	}

	public static byte[] createPlayerLeftPacket(byte[] username) {
		return createPacket((byte) PLAYER_LEFT, username);
	}

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

	private static byte[] createPacket(byte cmd, byte[] data) {
		byte[] packet = new byte[data.length + 1];

		packet[0] = cmd;

		for (int i = 0; i < data.length; i++) {
			packet[i + 1] = data[i];
		}

		return packet;
	}

	public static String generateRandomToken() {
		StringBuilder result = new StringBuilder();

		for (int i = 0; i < TOKEN_SIZE; i++) {
			result.append(CHARACTERS.charAt((int) (Math.random() * CHARACTERS.length())));
		}

		return result.toString();
	}

}