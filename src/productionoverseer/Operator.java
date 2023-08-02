package productionoverseer;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import productionoverseer.EIMMTLink.FoundDuplicateOrderException;

public class Operator {

	public static void main(String... args) {
		for (String arg : args) System.out.println(arg);
		
		LocalDate today = LocalDate.now();
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMddyyyy");
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmm");

		String dateFrom = dateFormatter.format(today);
		String dateTo = null;
		String ordDeskUser = "3605982";
		String excelDest = "C:/temp/" + dateFrom + "_THRU_" + timeFormatter.format(LocalDateTime.now()) + ".xlsx";
		String hapShare = "//Mw/wch-mil/PEDS_HAP_SHARE/";

		List<String> roots = List
				.of("Auburn/Retained", "Auburn/Request", "Auburn/CGM", "Auburn/TIFF", "Everett/Retained",
						"Everett/Request", "Everett/CGM", "Everett/TIFF", "St_Louis/Retained", "St_Louis/Request",
						"St_Louis/CGM", "St_Louis/TIFF")
				.parallelStream().map(dir -> hapShare + dir).collect(Collectors.toList());

		EIMMTLink eimmtLink = new EIMMTLink();

		List<HASPOrder> orders = eimmtLink.queryHASPOrders(ordDeskUser, dateFrom, dateTo);

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

		List<BundledOrder> bundledOrders = orders.parallelStream().map(order -> new BundledOrder(order, searchResults))
				.collect(Collectors.toList());

		try {
			ExcelLink.export(excelDest, bundledOrders);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			Runtime.getRuntime().exec("C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe -Command \"" + excelDest + "\"");
			System.out.println("Opening report . . .");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}