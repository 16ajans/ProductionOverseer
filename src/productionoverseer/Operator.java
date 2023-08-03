package productionoverseer;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import productionoverseer.EIMMTLink.FoundDuplicateOrderException;

public class Operator {

	public static void main(String... args) {

		LocalDate today = LocalDate.now();
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMddyyyy");
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmm");

		String dateFrom = dateFormatter.format(today);
		String dateTo = null;
		String ordDeskUser = null;
		String outputDir = "C:/temp/";
		Boolean headless = false;
		
		for (int i = 0; i < args.length; i += 2) {
			if (args[i].equals("--bems")) {
				ordDeskUser = args[i + 1];
			}
			if (args[i].equals("--from")) {
				dateFrom = args[i + 1];
			}
			if (args[i].equals("--to")) {
				dateTo = args[i + 1];
			}
			if (args[i].equals("--headless")) {
				headless = true;
			}
		}

		String excelDest = outputDir + dateFrom + "_THRU_" + (dateTo != null ? dateTo + "T" : "") + timeFormatter.format(LocalDateTime.now()) + (ordDeskUser != null ? "_FOR_" + ordDeskUser : "") + ".xlsx";
		String hapShare = "//Mw/wch-mil/PEDS_HAP_SHARE/";

		List<String> roots = List
				.of("Auburn/Retained", "Auburn/Request", "Auburn/CGM", "Auburn/TIFF", "Everett/Retained",
						"Everett/Request", "Everett/CGM", "Everett/TIFF", "St_Louis/Retained", "St_Louis/Request",
						"St_Louis/CGM", "St_Louis/TIFF")
				.parallelStream().map(dir -> hapShare + dir).collect(Collectors.toList());

		EIMMTLink eimmtLink = new EIMMTLink(headless);

		List<HASPOrder> orders = eimmtLink.queryHASPOrders(ordDeskUser, dateFrom, dateTo);

		SearchManager searchManager = new SearchManager(roots, orders);
		searchManager.start();

//		List<HAPRequest> requests = new ArrayList<>();
		
		orders.stream().forEach(order -> {
			try {
//				requests.addAll(eimmtLink.hydrateHASPOrder(order));
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

		List<BundledOrder> bundledOrders = orders.parallelStream().map(order -> new BundledOrder(order, searchResults, hapShare))
				.collect(Collectors.toList());

		try {
			ExcelLink.export(excelDest, bundledOrders);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			Runtime.getRuntime().exec(
					"C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe -Command \"" + excelDest + "\"");
			System.out.println("Opening report . . .");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}