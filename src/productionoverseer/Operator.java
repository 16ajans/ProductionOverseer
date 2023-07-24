package productionoverseer;

import java.util.List;

public class Operator {

	public static void main(String... args) {
        
        EIMMTLink connection = new EIMMTLink();
        
        List<HASPOrder> orders = connection.queryHASPOrders("3605982", "07242023");
        
        for (HASPOrder order : orders) {
        	connection.hydrateHASPOrder(order);
        }
        
        connection.close();
        
	}

}