package productionoverseer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelLink {

	public static void export(String path, List<HASPOrder> orders) throws IOException {
		XSSFWorkbook wb = new XSSFWorkbook();
		Sheet sh = wb.createSheet("HO Records");
		
		int i = 0;
		for (HASPOrder order : orders) {
			Row row = sh.createRow(i);
			List<String> orderData = order.toList();
			int j = 0;
			for (String data : orderData) {
				Cell cell = row.createCell(j);
				try {
					cell.setCellValue(Integer.parseInt(data));
				} catch(NumberFormatException e) {
					cell.setCellValue(data);
				}

				j++;
			}
			
			i++;
		}
		System.out.println("Created " + i + " rows.");
		
		FileOutputStream out = new FileOutputStream(path);
		
		wb.write(out);
		out.close();
		wb.close();
		System.out.println("Workbook closed.");
	}
	
}
