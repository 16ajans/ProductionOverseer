package productionoverseer;

public class Operator {

	public static void main(String... args) {
        
        EIMMTLink connection = new EIMMTLink();
        
        connection.queryHASPOrders("3605982", "07212023");
        
       // connection.close();
        
	}

}
