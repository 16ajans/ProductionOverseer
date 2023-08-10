package productionoverseer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import productionoverseer.EIMMTLink.FoundDuplicateOrderException;

public class Operator {

	public static void main(String... args) {

		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMddyyyy");
		String today = dateFormatter.format(LocalDate.now());

		String dateFrom = today;
		String dateTo = null;
		String ordDeskUser = null;
		Boolean headless = true;

		String outputDir = "C:/temp";

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

		File excelDest = getUniqueFileName(new File(outputDir + "/" + dateFrom
				+ (dateFrom.equals(today) ? "" : "_THRU_" + (dateTo != null ? dateTo : today))
				+ (ordDeskUser != null ? "_FOR_" + ordDeskUser : "") + ".xlsx"));
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
			ExcelLink.export(excelDest.toString(), bundledOrders, null);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			Runtime.getRuntime().exec(
					"C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe -Command \"& '" + excelDest.toString() + "'\"");
			System.out.println("Opening report . . .");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static File getUniqueFileName(File file) {
	    File originalFile = file;
	    try {
	        while (file.exists()) {
	            String newFileName = file.getName();
	            String baseName = newFileName.substring(0, newFileName.lastIndexOf("."));
	            String extension = getExtension(newFileName);

	            Pattern pattern = Pattern.compile("( \\(\\d+\\))\\."); // Find ' (###).' in the file name, if it exists
	            Matcher matcher = pattern.matcher(newFileName);

	            String strDigits = "";
	            if (matcher.find()) {
	                baseName = baseName.substring(0, matcher.start(0)); // Remove the (###)
	                strDigits = matcher.group(0); // Grab the ### we'll want to increment
	                strDigits = strDigits.substring(strDigits.indexOf("(") + 1, strDigits.lastIndexOf(")")); // Strip off the ' (' and ').' from the match
	                // Increment the found digit and convert it back to a string
	                int count = Integer.parseInt(strDigits);
	                strDigits = Integer.toString(count + 1);
	            } else {
	                strDigits = "1"; // If there is no (###) match then start with 1
	            }
	            file = new File(file.getParent() + "/" + baseName + " (" + strDigits + ")" + extension); // Put the pieces back together
	        }
	        return file;
	    } catch (Error e) {
	        return originalFile; // Just overwrite the original file at this point...
	    }
	}

	private static String getExtension(String name) {
	    return name.substring(name.lastIndexOf("."));
	}

}