package productionoverseer;

import java.io.IOException;
import java.util.List;

import productionoverseer.EIMMTLink.FoundDuplicateOrderException;

public class Operator {

	public static void main(String... args) throws IOException, InterruptedException {
        String inquiryDate = "07252023";
		
        EIMMTLink connection = new EIMMTLink();
        
        List<HASPOrder> orders = connection.queryHASPOrders(null, inquiryDate);
        
        for (HASPOrder order : orders) {
        	try {
				connection.hydrateHASPOrder(order);
			} catch (FoundDuplicateOrderException e) {
				e.printStackTrace();
			}
        }
        
        connection.close();
        
        ExcelLink.export("C:/temp/" + inquiryDate + ".xlsx", orders);
        
	}

}