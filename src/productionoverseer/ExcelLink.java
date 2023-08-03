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

	public static List<String> haspHeaders = Arrays.asList("orderId", "drawingNumber", "sheetId", "revision",
			"disclosureValue", "airplaneModel", "suppCode", "suppName", "custBemsid", "custName", "deliverTo",
			"buLocDept", "ordDeskUser", "ordDeskUserName", "siteRequesting", "sitePerformingLoc", "otherSys",
			"priority", "media", "convVendor", "orderComments", "orderDateTime", "customerRequestDateTime",
			"orderDeskFtpHapDateTime", "cancelledDateTime", "vendorProcessDateTime", "hapPdtCompletedDateTime",
			"orderReportFiles", "drawingFiles");

	public static void export(String path, List<BundledOrder> bundledOrders) throws IOException {

		XSSFWorkbook wb = new XSSFWorkbook();
		
		buildHASPSheet(wb, bundledOrders);

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
	
	private static Sheet buildHASPSheet(XSSFWorkbook wb, List<BundledOrder> bundledOrders) {
		Sheet sh = wb.createSheet("HO Records");
		ExcelLink.insertHeaders(sh, haspHeaders);
		
		DataFormat format = wb.createDataFormat();
		CellStyle style;

		int i = 1;
		for (BundledOrder bundle : bundledOrders) {

			HASPOrder order = bundle.getOrder();

			Row row = sh.createRow(i);

			List<String> orderData = order.listAttrs();
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
			
			style = wb.createCellStyle();
			style.setWrapText(true);
			
			orderReportCell.setCellStyle(style);
			drawingFileCell.setCellStyle(style);

			List<String> orderReportFiles = bundle.getOrderReportFiles();
			List<String> drawingFiles = bundle.getDrawingFiles();

			orderReportCell.setCellValue(String.join(", ", orderReportFiles));
			drawingFileCell.setCellValue(String.join(", ", drawingFiles));

			i++;
		}
		System.out.println("Created " + (i - 1) + " rows.");

		for (int k = 0; k < haspHeaders.size(); k++) {
			sh.autoSizeColumn(k);
		}
		
		return sh;
	}
	
//	private Sheet buildHAPSheet(XSSFWorkbook wb) {
//		return null;
//	}

}
