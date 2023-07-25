package productionoverseer;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class Validator {

	public static List<Path> matchFiles(HASPOrder order, List<Path> allFiles) {
		return allFiles.stream().filter(path -> path.toString().contains(order.orderId.substring(2)))
				.collect(Collectors.toList());
	}
}
