package productionoverseer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
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
	public static List<String> hapHeaders = Arrays.asList("parent", "requestId", "plotOperator", "plotOperatorName",
			"typeOfCheck", "plotter", "inches", "gridLen", "temp", "hum", "comments", "plot", "rejectRollValue",
			"processWasteValue", "lateValue", "customerReworkValue", "processReworkValue");

	public static void export(String path, List<BundledOrder> bundledOrders, List<HAPRequest> requests)
			throws IOException {

		XSSFWorkbook wb = new XSSFWorkbook();

		if (bundledOrders != null)
			buildHASPSheet(wb, bundledOrders);
		if (requests != null)
			buildHAPSheet(wb, requests);

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
		
		CellStyle error = wb.createCellStyle();
		error.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		error.setFillForegroundColor(IndexedColors.RED.getIndex());
		Font font = wb.createFont();
		font.setColor(IndexedColors.WHITE.getIndex());
		error.setFont(font);
		
		DataFormat format = wb.createDataFormat();
		CellStyle dateTime = wb.createCellStyle();
		dateTime.setDataFormat(format.getFormat("mm/dd/yyyy hh:mm;@"));

		int i = 1;
		for (BundledOrder bundle : bundledOrders) {

			HASPOrder order = bundle.getOrder();

			Row row = sh.createRow(i);

			createCell(row, 0, order.orderId);
			createCell(row, 1, order.drawingNumber);
			createCell(row, 2, order.sheetId);
			makeCellNonNullable(createCell(row, 3, order.revision), error);
			createCell(row, 4, order.disclosureValue);
			createCell(row, 5, order.airplaneModel);
			createCell(row, 6, order.suppCode);
			createCell(row, 7, order.suppName);
			createCell(row, 8, order.custBemsid);
			createCell(row, 9, order.custName);
			createCell(row, 10, order.deliverTo);
			createCell(row, 11, order.buLocDept);
			createCell(row, 12, order.ordDeskUser);
			createCell(row, 13, order.ordDeskUserName);
			createCell(row, 14, order.siteRequesting);
			createCell(row, 15, order.sitePerformingLoc);
			makeCellNonNullable(createCell(row, 16, order.otherSys), error);
			createCell(row, 17, order.priority);
			createCell(row, 18, order.media);
			createCell(row, 19, order.convVendor);
			createCell(row, 20, order.orderComments);
			makeCellNonNullable(createCell(row, 21, order.order, dateTime), error);
			makeCellNonNullable(createCell(row, 22, order.customerRequest, dateTime), error);
			createCell(row, 23, order.orderDeskFtpHap, dateTime);
			createCell(row, 24, order.cancelled, dateTime);
			createCell(row, 25, order.vendorProcess, dateTime);
			createCell(row, 26, order.hapPdtCompleted, dateTime);

			Cell orderReportCell = row.createCell(27);
			Cell drawingFileCell = row.createCell(28);

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

	private static Sheet buildHAPSheet(XSSFWorkbook wb, List<HAPRequest> requests) {
		Sheet sh = wb.createSheet("HR Records");
		ExcelLink.insertHeaders(sh, hapHeaders);

		DataFormat format = wb.createDataFormat();
		CellStyle style;

		int i = 1;
		for (HAPRequest request : requests) {
			Row row = sh.createRow(i);

			List<String> reqData = request.listAttrs();
			List<LocalDateTime> reqDates = request.listDates();
			List<Boolean> reqBools = request.listBool();

			int j = 0;

			for (String data : reqData) {
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
			for (LocalDateTime date : reqDates) {
				Cell cell = row.createCell(j);
				cell.setCellValue(date);
				cell.setCellStyle(style);

				j++;
			}

			for (Boolean bool : reqBools) {

			}

			i++;
		}
		System.out.println("Created " + (i - 1) + " rows.");

		for (int k = 0; k < hapHeaders.size(); k++) {
			sh.autoSizeColumn(k);
		}

		return sh;
	}

	private static Cell createCell(Row row, int index, String data) {
		Cell cell = row.createCell(index);
		try {
			cell.setCellValue(Integer.parseInt(data));
		} catch (NumberFormatException e) {
			cell.setCellValue(data);
		}
		return cell;
	};

	private static Cell createCell(Row row, int index, LocalDateTime data, CellStyle dateTime) {
		Cell cell = row.createCell(index);
		cell.setCellValue(data);
		cell.setCellStyle(dateTime);
		return cell;
	};

	private static Cell makeCellNonNullable(Cell cell, CellStyle error) {
		switch (cell.getCellType()) {
		case STRING:
			if (cell.getStringCellValue().equals(""))
				cell.setCellStyle(error);
			break;
		case NUMERIC:
			if (cell.getNumericCellValue() == 0.0)
				cell.setCellStyle(error);
		default:
			break;
		}
		return cell;
	}
}
