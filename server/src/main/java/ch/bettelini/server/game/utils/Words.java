package ch.bettelini.server.game.utils;

/**
 * This class is used to choose a random noun to draw.
 * 
 * @author Paolo Bettelii
 * @version 14.06.2021
 */
public class Words {
	
	/**
	 * The words
	 */
	private static String[] words;

	/**
	 * Statis words initializer.
	 */
	static {
		words = new String[] {
			"toothpaste",
			"lightsaber",
			"telescope",
			"swordfish",
			"spongebob",
			"spaceship",
			"breakfast",
			"cellphone",
			"hamburger",
			"jellyfish",
			"trashcan",
			"backpack",
			"campfire",
			"elephant",
			"internet",
			"sandwich",
			"scissors",
			"skeleton",
			"snowball",
			"computer",
			"honey",
			"bread",
			"database",
			"chocolate",
			"tennis",
			"speech",
			"river",
			"hair",
			"cigarette",
			"lake",
			"highway",
			"bow",
			"youtube",
			"cookie",
			"bitcoin",
			"fire truck",
			"diamond",
			"sunrise",
			"ketchup",
			"atom",
			"solar system",
			"baguette",
			"crocodile",
			"hexagon",
			"space suit",
			"time machine",
			"x-ray",
			"gold",
			"bed",
			"chess",
			"bottle",
			"anvil",
			"frog",
			"sausage",
			"blockchain",
			"UFO",
			"DNA",
			"cactus",
			"ice cream",
			"google",
			"nutella",
			"thunder",
			"strawberry",
			"circus",
			"injection",
			"mirror",
			"whale",
			"fountain",
			"programming"
		};
	}

	/**
	 * Returns a random word.
	 * 
	 * @return a random word
	 * @see {@link #words} the array of words
	 */
	public static String random() {
		return words[(int) (Math.random() * words.length)];
	}

	/**
	 * Obfuscates a <code>String</code> such that
	 * it is possible to understand its length
	 * and the position of spaces.
	 * Each non-space character gets encoded to a '_'.
	 * 
	 * @param word the <code>String</code> to obfuscate.
	 * @return the obfuscated word
	 */
	public static String obfuscate(String word) {
		StringBuilder builder = new StringBuilder();

		char c;
		for (int i = 0; i < word.length(); i++) {
			c = word.charAt(i);
			builder.append(c == ' ' || c == '-' ? c : '_');
		}

		return builder.toString();
	}

}
