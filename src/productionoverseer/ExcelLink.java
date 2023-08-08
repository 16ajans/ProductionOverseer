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

		CellStyle dateTimeError = wb.createCellStyle();
		dateTimeError.setDataFormat(format.getFormat("mm/dd/yyyy hh:mm;@"));
		dateTimeError.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		dateTimeError.setFillForegroundColor(IndexedColors.RED.getIndex());
		dateTimeError.setFont(font);

		int i = 1;
		for (BundledOrder bundle : bundledOrders) {

			HASPOrder order = bundle.getOrder();

			Row row = sh.createRow(i);

			createCell(row, 0, order.orderId);
			createCell(row, 1, order.drawingNumber);
			createCell(row, 2, order.sheetId);
			Cell revision = createCell(row, 3, order.revision);
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
			Cell otherSys = createCell(row, 16, order.otherSys);
			Cell priority = createCell(row, 17, order.priority);
			createCell(row, 18, order.media);
			createCell(row, 19, order.convVendor);
			createCell(row, 20, order.orderComments);
			createCell(row, 21, order.order, dateTime);
			Cell customerRequest = createCell(row, 22, order.customerRequest, dateTime);
			Cell orderDeskFtpHap = createCell(row, 23, order.orderDeskFtpHap, dateTime);
			createCell(row, 24, order.cancelled, dateTime);
			Cell vendorProcess = createCell(row, 25, order.vendorProcess, dateTime);
			createCell(row, 26, order.hapPdtCompleted, dateTime);

			Cell orderReports = row.createCell(27);
			Cell drawings = row.createCell(28);

			List<String> orderReportFiles = bundle.getOrderReportFiles();
			List<String> drawingFiles = bundle.getDrawingFiles();

			orderReports.setCellValue(String.join(", ", orderReportFiles));
			drawings.setCellValue(String.join(", ", drawingFiles));

			int SLA = 4;

			if (order.cancelled == null) {
				if (order.revision.equals(""))
					applyStyle(revision, error);
				if (order.otherSys.equals(""))
					applyStyle(otherSys, error);

				if (order.convVendor.equals("")) {
					if (order.orderDeskFtpHap == null)
						applyStyle(orderDeskFtpHap, dateTimeError);
					else if (!order.orderDeskFtpHap.toLocalDate().equals(order.order.toLocalDate()))
						applyStyle(orderDeskFtpHap, dateTimeError);
				} else {
					if (order.vendorProcess == null)
						applyStyle(vendorProcess, dateTimeError);
					else if (!order.vendorProcess.toLocalDate().equals(order.order.toLocalDate()))
						applyStyle(vendorProcess, dateTimeError);
				}

				if (order.suppCode.toLowerCase().startsWith("z0") || order.priority.equals("AOG-AG/Emergent")) {
					if (order.priority.equals("Standard"))
						applyStyle(priority, error);
					SLA = 1;
				} else if (order.priority.equals("Expedite"))
					SLA = 2;

				if (order.customerRequest == null)
					applyStyle(customerRequest, dateTimeError);
				else if (!order.customerRequest.toLocalDate().equals(order.order.toLocalDate().plusDays(SLA)))
					applyStyle(customerRequest, dateTimeError);

				chewFiles(order, orderReportFiles, orderReports, error);
				chewFiles(order, drawingFiles, drawings, error);

			}

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

	private static Cell applyStyle(Cell cell, CellStyle style) {
		cell.setCellStyle(style);
		return cell;
	};

	private static void chewFiles(HASPOrder order, List<String> files, Cell cell, CellStyle error) {
		if (order.media.equals("S03") || order.media.equals("S05")) {
			return;
		} else if (files.size() == 0) {
			applyStyle(cell, error);
			return;
		} else {
			for (String file : files) {
				file = file.toLowerCase();

				switch (order.sitePerformingLoc) {
				case "SEATTLE":
					if (file.startsWith("auburn\\")) {
						file = file.substring(7);
						break;
					}
				case "EVERETT":
					if (file.startsWith("everett\\")) {
						file = file.substring(8);
						break;
					}
				case "ST LOUIS":
					if (file.startsWith("st_louis\\")) {
						file = file.substring(9);
						break;
					}
				default:
					applyStyle(cell, error);
					return;
				}

				if ((file.startsWith("request\\") || file.startsWith("retained\\"))
						&& (file.endsWith("txt") || file.endsWith("pdf"))) {
					return;
				} else if ((file.startsWith("cgm\\") || file.startsWith("retained\\")) && file.endsWith("cgm")) {
					String parts[] = file.split("\\\\");
					file = parts[parts.length - 1];
					String sheetId = order.sheetId.trim().replaceFirst("^0+(?!$)", "");
					String combo = String.format("%sS%s", order.drawingNumber, String.format("%2s", sheetId).replace(" ", "0")).toLowerCase();
					
					int start = file.indexOf(combo);
					if (start > -1) {
						file = file.substring(start);
						return;
					}
				} else if ((file.startsWith("tiff\\") || file.startsWith("retained\\")) && file.endsWith("tif")) {
					String parts[] = file.split("\\\\");
					file = parts[parts.length - 1];
					String combo = String.join("_", order.drawingNumber, order.sheetId, order.revision, order.disclosureValue).toLowerCase();
					
					int start = file.indexOf(combo);
					if (start > -1) {
						file = file.substring(start);
						return;
					}
				}
				applyStyle(cell, error);
			}
		}
	}
}
