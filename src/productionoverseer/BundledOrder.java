package productionoverseer;

import java.nio.file.Path;
import java.util.List;

public class BundledOrder {

	HASPOrder order;
	List<HAPRequest> requests;
	
	List<Path> orderReportFiles;
	List<Path> drawingFiles;

	BundledOrder(HASPOrder order, List<HAPRequest> requests, List<Path> orderReportFiles, List<Path> drawingFiles) {
		this.order = order;
		this.requests = requests;
		this.orderReportFiles = orderReportFiles;
		this.drawingFiles = drawingFiles;
	}
	
	HASPOrder getOrder() {
		return order;
	}
	
	List<Path> getOrderReportFiles() {
		return orderReportFiles;
	}
	
	List<Path> getDrawingFiles() {
		return drawingFiles;
	}
	
}
