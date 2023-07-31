package productionoverseer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelLink {

	public static List<String> headers = Arrays.asList("orderId", "drawingNumber", "sheetId", "revision",
			"disclosureValue", "airplaneModel", "suppCode", "suppName", "custBemsid", "custName", "deliverTo",
			"buLocDept", "ordDeskUser", "ordDeskUserName", "siteRequesting", "sitePerformingLoc", "otherSys",
			"priority", "media", "convVendor", "orderComments", "orderDateTime", "customerRequestDateTime",
			"orderDeskFtpHapDateTime", "cancelledDateTime", "vendorProcessDateTime", "hapPdtCompletedDateTime",
			"orderReportFiles", "drawingFiles");

	public static void export(String path, List<BundledOrder> bundledOrders) throws IOException {

		XSSFWorkbook wb = new XSSFWorkbook();
		Sheet sh = wb.createSheet("HO Records");
		ExcelLink.insertHeaders(sh, headers);

		DataFormat format = wb.createDataFormat();
		CellStyle style;

		int i = 1;
		for (BundledOrder bundle : bundledOrders) {

			HASPOrder order = bundle.getOrder();

			Row row = sh.createRow(i);

			List<String> orderData = order.listAttribs();
			List<LocalDateTime> orderDates = order.listDates();

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

			style = wb.createCellStyle();
			style.setDataFormat(format.getFormat("mm/dd/yyyy hh:mm;@"));
			for (LocalDateTime date : orderDates) {
				Cell cell = row.createCell(j);
				cell.setCellValue(date);
				cell.setCellStyle(style);

				j++;
			}

			Cell orderReportCell = row.createCell(j++);
			Cell drawingFileCell = row.createCell(j++);
			String orderReportFiles = bundle.getOrderReportFiles().toString();
			String drawingFiles = bundle.getDrawingFiles().toString();

			orderReportCell.setCellValue(orderReportFiles.substring(1, orderReportFiles.length() - 1));
			drawingFileCell.setCellValue(drawingFiles.substring(1, drawingFiles.length() - 1));

			i++;
		}
		System.out.println("Created " + (i - 1) + " rows.");

		for (int k = 0; k < headers.size(); k++) {
			sh.autoSizeColumn(k);
		}

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
