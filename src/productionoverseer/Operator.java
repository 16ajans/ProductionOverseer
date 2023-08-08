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

		LocalDate today = LocalDate.now();
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMddyyyy");
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmm");

		String dateFrom = dateFormatter.format(today);
		String dateTo = null;
		String ordDeskUser = null;
		Boolean headless = true;

		String outputDir = "C:/temp/";

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
			if (args[i].equals("--visible")) {
				headless = false;
			}
			if (args[i].equals("--output")) {
				outputDir = args[i + 1];
			}
		}

		String excelDest = outputDir + dateFrom + "_THRU_" + (dateTo != null ? dateTo + "T" : "")
				+ timeFormatter.format(LocalDateTime.now()) + (ordDeskUser != null ? "_FOR_" + ordDeskUser : "")
				+ ".xlsx";
		String hapShare = "//Mw/wch-mil/PEDS_HAP_SHARE/";

		List<String> roots = List
				.of("Auburn/Retained", "Auburn/Request", "Auburn/CGM", "Auburn/TIFF", "Everett/Retained",
						"Everett/Request", "Everett/CGM", "Everett/TIFF", "St_Louis/Retained", "St_Louis/Request",
						"St_Louis/CGM", "St_Louis/TIFF")
				.parallelStream().map(dir -> hapShare + dir).collect(Collectors.toList());

		EIMMTLink orderWindow = new EIMMTLink(headless, ordDeskUser, dateFrom, dateTo);

		Thread orderFetch = new Thread(new Runnable() {
			public void run() {
				orderWindow.open();

				orderWindow.queryHASPOrders();
			}
		});

		orderFetch.start();

		try {
			orderFetch.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		List<HASPOrder> orders = orderWindow.orders;

		Thread orderHydration = new Thread(new Runnable() {
			public void run() {
				orders.stream().forEach(order -> {
					try {
						orderWindow.hydrateHASPOrder(order);
					} catch (FoundDuplicateOrderException e) {
						e.printStackTrace();
						e.printOrderIds();
					}
				});
			}
		});

		orderHydration.start();

		SearchManager searchManager = new SearchManager(roots, orders);
		searchManager.start();

		try {
			orderHydration.join();
			orderWindow.close();

			searchManager.join();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		List<Path> searchResults = searchManager.getResults();

		List<BundledOrder> bundledOrders = orders.parallelStream()
				.map(order -> new BundledOrder(order, searchResults, hapShare)).collect(Collectors.toList());

		try {
			ExcelLink.export(excelDest, bundledOrders, null);
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