package productionoverseer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelLink {

	public static List<String> headers = Arrays.asList("orderId", "drawingNumber", "sheetId", "revision",
			"disclosureValue", "airplaneModel", "suppCode", "suppName", "custBemsid", "custName", "deliverTo",
			"buLocDept", "ordDeskUser", "ordDeskUserName", "siteRequesting", "sitePerformingLoc", "otherSys",
			"priority", "media", "convVendor", "orderComments", "orderDateTime", "customerRequestDateTime",
			"orderDeskFtpHapDateTime", "cancelledDateTime", "vendorProcessDateTime", "hapPdtCompletedDateTime");

	public static void export(String path, List<BundledOrder> bundledOrders) throws IOException {
		// TODO unfurl
		List<HASPOrder> orders = bundledOrders.stream()
									.map(bundle -> bundle.order)
									.collect(Collectors.toList());
		
		XSSFWorkbook wb = new XSSFWorkbook();
		Sheet sh = wb.createSheet("HO Records");
		ExcelLink.insertHeaders(sh, headers);

		int i = 1;
		for (HASPOrder order : orders) {
			Row row = sh.createRow(i);
			List<String> orderData = order.toList();
			int j = 0;
			for (String data : orderData) {
				Cell cell = row.createCell(j);
				try {
					cell.setCellValue(Integer.parseInt(data));
				} catch (NumberFormatException e) {
					cell.setCellValue(data);
				}

				j++;
			}

			i++;
		}
		System.out.println("Created " + (i - 1) + " rows.");

		FileOutputStream out = new FileOutputStream(path);

		wb.write(out);
		out.close();
		wb.close();
		System.out.println("Workbook closed.");
	}

	private static Sheet insertHeaders(Sheet sheet, List<String> headers) {
		Row row = sheet.createRow(0);
		int j = 0;
		for (String header : headers) {
			Cell cell = row.createCell(j);
			cell.setCellValue(header);

			j++;
		}

		return sheet;
	}

}
