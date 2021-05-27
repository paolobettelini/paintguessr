package ch.bettelini.server.game;

public class GamesHandler {

    static {
        
    }

    	/* COMMANDS */

        // code -> the room code
        // auth -> the token authentication
	/*

        Server -> Client

        00 | take code      | <code>
        10 | join error     | <reason>
        11 | create error   | <reason>
	 */

	 /*
        Client -> Server
	  
        00 | create | <username> <max_users> <public>
        01 | join   | <code> <username>
        02 | start  | <code>

        Eventi start drawing | end drawing
	  */

      // Point on canvas:
      // v = {uint8, uint8}
      // point = width / v
      /*
        Come rappresentare un punto sul canvas in un array di byte
        Un punto sul canvas (x, y) viene definito con  4 uint8
        I primi 2 byte rappresentano la coordinata X, mentre gli altri 2 la coordinata Y
        Una posizione X o Y viene rappresentata con 2 byte.
        la posizione del pxel è data dalla lettura di quest'ultimi in BigEndian (b0 | b1 << 8)  / 65535 * width
        e rispettivamente (b0 | b1 << 8)  / 65535 * height
      */

}