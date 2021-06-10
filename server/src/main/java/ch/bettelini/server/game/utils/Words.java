package ch.bettelini.server.game.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Words {
	
	private static String[] words;

	private static final String FILENAME = "words.txt";

	static {
		try {
			Path path = Path.of(Words.class.getResource("/" + FILENAME).toURI());
			List<String> list = Files.readAllLines(path);
			words = list.toArray(new String[list.size()]);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error whilst reading words.txt");
			System.exit(0);
		}
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
