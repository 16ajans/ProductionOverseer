package productionoverseer;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class BundledOrder {

	HASPOrder order;
	
	List<Path> orderReportFiles;
	List<Path> drawingFiles;

	BundledOrder(HASPOrder order, List<Path> orderReportFiles, List<Path> drawingFiles) {
		this.order = order;
		this.orderReportFiles = orderReportFiles;
		this.drawingFiles = drawingFiles;
	}
	
	HASPOrder getOrder() {
		return order;
	}
	
	List<String> getOrderReportFiles() {
		return orderReportFiles.stream()
				.map(path -> path.toString().substring(3))
				.collect(Collectors.toList());
	}
	
	List<String> getDrawingFiles() {
		return drawingFiles.stream()
				.map(path -> path.toString().substring(3))
				.collect(Collectors.toList());
	}
	
}
