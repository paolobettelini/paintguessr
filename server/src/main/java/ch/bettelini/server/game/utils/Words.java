package ch.bettelini.server.game.utils;

public class Words {
	
	private static String[] words;

	//private static final String FILENAME = "words.txt";

	static {
		words = new String[] {
			"toothpaste",
			"lightsaber",
			"skyscraper",
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
			"seahorse",
			"skeleton",
			"snowball",
			"computer",
			"honey",
			"bread",
			"database",
			"chocolate",
			"physics",
		};
	}

	public static String random() {
		return words[(int) (Math.random() * words.length)];
	}

	public static String obfuscate(String word) {
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < word.length(); i++) {
			builder.append(word.charAt(i) == ' ' ? ' ' : '_');
		}

		return builder.toString();
	}

}
