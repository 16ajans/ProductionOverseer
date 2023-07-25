package productionoverseer;

import java.io.IOException;
import java.util.List;

import productionoverseer.EIMMTLink.FoundDuplicateOrderException;

public class Operator {

	public static void main(String... args) {
		String inquiryDate = "07242023";
		String ordDeskUser = null;
		String excelDest = "C:/temp/" + inquiryDate + ".xlsx";

		EIMMTLink eimmtLink = new EIMMTLink();

		List<HASPOrder> orders = eimmtLink.queryHASPOrders(ordDeskUser, inquiryDate);

		for (HASPOrder order : orders) {
			try {
				eimmtLink.hydrateHASPOrder(order);
			} catch (FoundDuplicateOrderException e) {
				e.printStackTrace();
			}
		}

		eimmtLink.close();

		try {
			ExcelLink.export(excelDest, orders);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}