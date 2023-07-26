package productionoverseer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import productionoverseer.EIMMTLink.FoundDuplicateOrderException;

public class Operator {

	public static void main(String... args) {
		String inquiryDate = "07262023";
		String ordDeskUser = null;
		String excelDest = "C:/temp/" + inquiryDate + ".xlsx";
		String hapShareLetter = "T:/";

		List<String> roots = List
				.of("Auburn/Retained", "Auburn/Request", "Auburn/CGM", "Auburn/TIFF", "Everett/Retained",
						"Everett/Request", "Everett/CGM", "Everett/TIFF", "St_Louis/Retained", "St_Louis/Request",
						"St_Louis/CGM", "St_Louis/TIFF")
				.parallelStream().map(dir -> hapShareLetter + dir).collect(Collectors.toList());

		EIMMTLink eimmtLink = new EIMMTLink();

		List<HASPOrder> orders = eimmtLink.queryHASPOrders(ordDeskUser, inquiryDate);

		SearchManager searchManager = new SearchManager(roots, orders);
		searchManager.start();

		orders.stream().forEach(order -> {
			try {
				eimmtLink.hydrateHASPOrder(order);
			} catch (FoundDuplicateOrderException e) {
				e.printStackTrace();
				e.printOrderIds();
			}
		});

		eimmtLink.close();

		try {
			searchManager.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		List<Path> searchResults = searchManager.getResults();

		List<BundledOrder> bundledOrders = orders.parallelStream()
				.map(order -> Validator.bundle(order, searchResults))
				.collect(Collectors.toList());

		try {
			ExcelLink.export(excelDest, bundledOrders);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}