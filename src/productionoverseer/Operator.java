package productionoverseer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import productionoverseer.EIMMTLink.FoundDuplicateOrderException;

public class Operator {

	public static void main(String... args) {
		String inquiryDate = "07252023";
		String ordDeskUser = "3605982";
		String excelDest = "C:/temp/" + inquiryDate + ".xlsx";
		List<String> roots = List.of("T:/Auburn/Retained", "T:/Auburn/Request", "T:/Auburn/CGM", "T:/Auburn/TIFF",
				"T:/Everett/Retained", "T:/Everett/Request", "T:/Everett/CGM", "T:/Everett/TIFF",
				"T:/St_Louis/Retained", "T:/St_Louis/Request", "T:/St_Louis/CGM", "T:/St_Louis/TIFF");

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

		List<BundledOrder> bundledOrders = orders.stream()
				.map(order -> new BundledOrder(order, null, Validator.matchFiles(order, searchResults)))
				.collect(Collectors.toList());

		try {
			ExcelLink.export(excelDest, bundledOrders);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}