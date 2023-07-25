package productionoverseer;

import java.nio.file.Path;
import java.util.List;

public class BundledOrder {

	HASPOrder order;
	List<HAPRequest> requests;
	List<Path> foundFiles;

	BundledOrder(HASPOrder order, List<HAPRequest> requests, List<Path> foundFiles) {
		this.order = order;
		this.requests = requests;
		this.foundFiles = foundFiles;
	}

}
