package productionoverseer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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

	public static List<String> haspHeaders = Arrays.asList("Order ID", "DWG", "SHT", "REV", "Disclosure", "Model",
			"Ext Supp Code", "Supplier", "Cust BEMSID", "Cust Name", "Deliver to", "BU-LOC-DEPT", "Order Desk BEMSID",
			"Order Desk User", "Site Requesting", "Site Performing", "Other Sys", "Priority", "Media", "Conv Vendor",
			"Comments", "Order Date Time", "Customer Request Date Time", "Order Desk FTP HAP Date Time",
			"Cancelled Date Time", "Vendor Process Date Time", "HAP PDT Completed Date Time", "Order Report Files",
			"Drawing Files", "Error");
	public static List<String> hapHeaders = Arrays.asList("parent", "requestId", "plotOperator", "plotOperatorName",
			"typeOfCheck", "plotter", "inches", "gridLen", "temp", "hum", "comments", "plot", "rejectRollValue",
			"processWasteValue", "lateValue", "customerReworkValue", "processReworkValue");
	public static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMddyyyy");

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

		Font grey = wb.createFont();
		grey.setColor(IndexedColors.GREY_50_PERCENT.getIndex());

		Font white = wb.createFont();
		white.setColor(IndexedColors.WHITE.getIndex());

		DataFormat format = wb.createDataFormat();

		CellStyle dateTime = wb.createCellStyle();
		dateTime.setDataFormat(format.getFormat("mm/dd/yyyy hh:mm;@"));

		CellStyle error = wb.createCellStyle();
		error.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		error.setFillForegroundColor(IndexedColors.RED.getIndex());
		error.setFont(white);

		CellStyle dateTimeError = wb.createCellStyle();
		dateTimeError.setDataFormat(format.getFormat("mm/dd/yyyy hh:mm;@"));
		dateTimeError.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		dateTimeError.setFillForegroundColor(IndexedColors.RED.getIndex());
		dateTimeError.setFont(white);

		CellStyle cancelled = wb.createCellStyle();
		cancelled.setFont(grey);

		CellStyle dateTimeCancelled = wb.createCellStyle();
		dateTimeCancelled.setDataFormat(format.getFormat("mm/dd/yyyy hh:mm;@"));
		dateTimeCancelled.setFont(grey);

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

			String errorMsg = "";

			int SLA = 4;

			if (order.cancelled == null) {
				if (order.revision.equals("")) {
					applyStyle(revision, error);
					errorMsg += "Missing Revision. ";
				}
				if (order.otherSys.equals("")) {
					applyStyle(otherSys, error);
					errorMsg += "Missing Other Sys #. ";
				}
				if (order.convVendor.equals("")) {
					if (order.orderDeskFtpHap == null) {
						applyStyle(orderDeskFtpHap, dateTimeError);
						errorMsg += "Missing Order Desk FTP HAP Date Time. ";
					} else if (!order.orderDeskFtpHap.toLocalDate().equals(order.order.toLocalDate())) {
						applyStyle(orderDeskFtpHap, dateTimeError);
						errorMsg += "Order Desk FTP HAP Date doesn't match Order Date.";
					}
				} else {
					if (order.vendorProcess == null) {
						applyStyle(vendorProcess, dateTimeError);
						errorMsg += "Missing Vault Vendor Received Date Time. ";
					} else if (!order.vendorProcess.toLocalDate().equals(order.order.toLocalDate())) {
						applyStyle(vendorProcess, dateTimeError);
						errorMsg += "Vault Vendor Received Date doesn't match Order Date. ";
					}
				}

				if (order.suppCode.toLowerCase().startsWith("z0") || order.priority.equals("AOG-AG/Emergent")) {
					if (order.priority.equals("Standard")) {
						applyStyle(priority, error);
						errorMsg += "Expected Priority \"Expedite\" for CRIB order. ";
					}
					SLA = 1;
				} else if (order.priority.equals("Expedite") || order.media.equals("S03"))
					SLA = 2;

				if (order.customerRequest == null) {
					applyStyle(customerRequest, dateTimeError);
					errorMsg += "Missing Customer Required Date Time. ";
				} else if (!(calcWeekDaysBetween(order.order.toLocalDate(),
						order.customerRequest.toLocalDate()) == SLA)) {
					applyStyle(customerRequest, dateTimeError);
					errorMsg += "Expected " + SLA + " weekdays between Order Date and Customer Required Date (Got "
							+ calcWeekDaysBetween(order.order.toLocalDate(), order.customerRequest.toLocalDate())
							+ " days). ";
				}

				errorMsg += chewFiles(order, orderReportFiles, orderReports, error);
				errorMsg += chewFiles(order, drawingFiles, drawings, error);

				createCell(row, 29, errorMsg);

			} else {
				for (Cell cell : row) {
					if (cell.getColumnIndex() > 20 && cell.getColumnIndex() < 27)
						applyStyle(cell, dateTimeCancelled);
					else {
						applyStyle(cell, cancelled);
					}
				}
			}

			i++;
		}
		System.out.println("Created " + (i - 1) + " rows.");

		Row row = sh.createRow(i + 1);
		createCell(row, 0, "Retrieved:");
		createCell(row, 1, LocalDateTime.now(), dateTime);

		for (int k = 0; k < haspHeaders.size(); k++) {
			sh.autoSizeColumn(k);
		}

		sh.createFreezePane(0, 1);

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

	private static String chewFiles(HASPOrder order, List<String> files, Cell cell, CellStyle error) {
		if (!order.convVendor.equals("")) {
			return "";
		} else if (files.size() == 0) {
			applyStyle(cell, error);
			return "Found no files matching PlotID. ";
		} else {
			for (String file : files) {
				file = file.toLowerCase();

				switch (order.sitePerformingLoc) {
				case "SEATTLE":
					if (file.startsWith("auburn\\")) {
						file = file.substring(7);
						break;
					} else {
						applyStyle(cell, error);
						return "Found files in wrong HAP folder. ";
					}
				case "EVERETT":
					if (file.startsWith("everett\\")) {
						file = file.substring(8);
						break;
					} else {
						applyStyle(cell, error);
						return "Found files in wrong HAP folder. ";
					}
				case "ST LOUIS":
					if (file.startsWith("st_louis\\")) {
						file = file.substring(9);
						break;
					} else {
						applyStyle(cell, error);
						return "Found files in wrong HAP folder. ";
					}
				default:
					applyStyle(cell, error);
					return "Found files outside of regular HAP folders. ";
				}

				if ((file.endsWith("txt") || file.endsWith("pdf"))) {
					if (file.startsWith("request\\") || file.startsWith("retained\\")) {
						return "";
					} else {
						applyStyle(cell, error);
						return "Found Order Report file outside of Request/Retained. ";
					}
				} else if (file.endsWith("cgm")) {
					if (file.startsWith("cgm\\") || file.startsWith("retained\\")) {

					} else {
						applyStyle(cell, error);
						return "Found CGM file outside of CGM/Retained. ";
					}

					String msg;
					String match = null;
					if (order.sheetId.trim().endsWith("0") && order.revision.contains("-")) {
						match = order.drawingNumber.toLowerCase();
						msg = "D&D: DWG ";
					} else {
						String parts[] = file.split("\\\\");
						file = parts[parts.length - 1];
						String sheetId = order.sheetId.trim().replaceFirst("^0+(?!$)", "");
						match = String
								.format("%sS%s", order.drawingNumber, String.format("%2s", sheetId).replace(" ", "0"))
								.toLowerCase();
						msg = "DWG or SHT ";
					}

					int start = file.indexOf(match);
					if (start > -1) {
						file = file.substring(start, file.lastIndexOf("."));
						// TODO revision opportunistic match
						if (file.endsWith(dateFormatter.format(order.customerRequest))) {
							return "";
						} else {
							applyStyle(cell, error);
							return "Customer Request Date not matching found CGM filename. ";
						}
					} else {
						applyStyle(cell, error);
						return msg + "not matching found CGM filename. ";
					}
				} else if (file.endsWith("tif")) {
					if (file.startsWith("tiff\\") || file.startsWith("retained\\")) {

					} else {
						applyStyle(cell, error);
						return "Found TIF file outside of TIFF/Retained. ";
					}

					String msg;
					String match = null;
					if (order.sheetId.trim().endsWith("0") && order.revision.contains("-")) {
						match = order.drawingNumber.toLowerCase();
						msg = "D&D: DWG ";
					} else {
						String parts[] = file.split("\\\\");
						file = parts[parts.length - 1];
						match = String
								.join("_", order.drawingNumber, order.sheetId, order.revision, order.disclosureValue)
								.toLowerCase();
						msg = "DWG, SHT, or REV ";
					}

					int start = file.indexOf(match);
					if (start > -1) {
						file = file.substring(start, file.lastIndexOf("."));
						if (file.endsWith(dateFormatter.format(order.customerRequest))) {
							return "";
						} else {
							applyStyle(cell, error);
							return "Customer Request Date not matching found TIF filename. ";
						}
					} else {
						applyStyle(cell, error);
						return msg + "not matching found TIF filename. ";
					}
				}
				applyStyle(cell, error);
				return "Found file error. ";
			}
		}
		return "";
	}

	public static long calcWeekDaysBetween(final LocalDate start, final LocalDate end) {
		final DayOfWeek startW = start.getDayOfWeek();
		final DayOfWeek endW = end.getDayOfWeek();

		final long days = ChronoUnit.DAYS.between(start, end);
		final long daysWithoutWeekends = days - 2 * ((days + startW.getValue()) / 7);

		// adjust for starting and ending on a Sunday:
		return daysWithoutWeekends + (startW == DayOfWeek.SUNDAY ? 1 : 0) + (endW == DayOfWeek.SUNDAY ? 1 : 0);
	}
}
