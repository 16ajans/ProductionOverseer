package productionoverseer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileSearch implements Runnable {

	private final Pattern pattern;
	private final Path root;
	
	private List<Path> results;

	@Override
	public void run() {
		try {
			System.out.println(String.format("Started search at %s", root.toString()));
			try (Stream<Path> pathStream = Files.find(root, Integer.MAX_VALUE, (path, attributes) -> {
				Matcher matcher = pattern.matcher(path.getFileName().toString());
				return matcher.find();
			})) {
				results = pathStream.collect(Collectors.toList());
			}
			System.out.println(String.format("Finished search at %s", root.toString()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<Path> dump() {
		return results;
	}

	FileSearch(Pattern pattern, Path root) {
		this.pattern = pattern;
		this.root = root;
	}

}