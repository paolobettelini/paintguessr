/**
 * For more documentation about the protocol see
 * https://github.com/PaoloBettelini/paintguessr/blob/main/server/src/main/java/ch/bettelini/server/game/Protocol.java
 */
const GAME_SERVED	= 0;	// token, public, max_players, rounds, turn_duration
const JOIN_GAME		= 1;	// token, username
const CREATE_GAME	= 2;	// public, rounds, turn_duration, max_players, username
const JOIN_RND		= 3		// username
const START			= 4;	// -
const PLAYER_JOIN	= 5;	// username
const PLAYER_LEFT	= 6;	// wasDrawing, username
const NEXT_TURN		= 7;	// drawing, word
const GAME_OVER		= 8;	// -
const DRAW_BUFFER	= 20;	// point...
const MOUSE_UP		= 21;	// -
const SET_COLOR		= 22;	// r, g, b
const SET_WIDTH		= 23;	// line width
const UNDO			= 24;	// -
const MSG			= 30;	// spectator, message
const ADD_SCORE		= 31;	// amount, username
const JOIN_ERROR	= 201;	// reason