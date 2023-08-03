package productionoverseer;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class BundledOrder {

	HASPOrder order;

	String hapShare;
	List<Path> orderReportFiles;
	List<Path> drawingFiles;

	BundledOrder(HASPOrder order, List<Path> allFiles, String hapShare) {
		List<Path> matchingFiles = allFiles.parallelStream()
				.filter(path -> path.toString().contains(order.orderId.substring(2))).collect(Collectors.toList());
		List<Path> orderReportFiles = matchingFiles.parallelStream().filter(
				path -> path.toString().toLowerCase().endsWith("txt") || path.toString().toLowerCase().endsWith("pdf"))
				.collect(Collectors.toList());
		List<Path> drawingFiles = matchingFiles.parallelStream().filter(
				path -> path.toString().toLowerCase().endsWith("cgm") || path.toString().toLowerCase().endsWith("tif"))
				.collect(Collectors.toList());
		this.order = order;
		this.orderReportFiles = orderReportFiles;
		this.drawingFiles = drawingFiles;
		this.hapShare = hapShare;
	}

	HASPOrder getOrder() {
		return order;
	}

	List<String> getOrderReportFiles() {
		return orderReportFiles.stream().map(path -> path.toString()).map(str -> str.substring(hapShare.length())).collect(Collectors.toList());
	}

	List<String> getDrawingFiles() {
		return drawingFiles.stream().map(path -> path.toString()).map(str -> str.substring(hapShare.length())).collect(Collectors.toList());
	}

}
