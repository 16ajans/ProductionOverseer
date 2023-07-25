package productionoverseer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SearchManager {

	private List<FileSearch> searches;
	private List<Thread> threads;

	private static Pattern compileSearchMask(List<HASPOrder> orders) {
		String mask = "";
		for (HASPOrder order : orders) {
			mask = String.join("|", mask, order.orderId.substring(2));
		}
		return Pattern.compile(mask.substring(1));
	}

	public void start() {
		for (Thread thread : threads) {
			thread.start();
		}
	}

	public void join() throws InterruptedException {
		for (Thread thread : threads) {
			thread.join();
		}
	}

	public List<Path> getResults() {
		return searches.stream().map(FileSearch::dump).flatMap(List::stream).collect(Collectors.toList());
	}

	SearchManager(List<String> roots, List<HASPOrder> orders) {
		searches = new ArrayList<FileSearch>();
		threads = new ArrayList<Thread>();

		Pattern pattern = SearchManager.compileSearchMask(orders);
		for (String root : roots) {
			FileSearch search = new FileSearch(pattern, Paths.get(root));
			searches.add(search);
			threads.add(new Thread(search));
		}
	}
}
