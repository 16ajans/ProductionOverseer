package productionoverseer;

import java.time.LocalDateTime;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class HAPRequest {
	
	HASPOrder parent;
	
	String requestId;
	String ploperator;
	String ploperatorName;
	
	int inches;
	int gridLen;
	int temp;
	int hum;
	
	LocalDateTime plot;
	
	String typeOfCheck;
	String plotter;
	
	boolean rejectRollValue;
	boolean processWasteValue;
	boolean lateValue;
	boolean customerReworkValue;
	boolean processReworkValue;
	
	String comments;

	HAPRequest(HASPOrder parent, String requestId, String ploperator, String requestData) {
		this.parent = parent;
		this.requestId = requestId;
		String[] ploperatorInfo = ploperator.split("-");
		this.ploperator = ploperatorInfo[0].strip();
		this.ploperatorName = ploperatorInfo[1].strip();
		
		Document requestDoc = Jsoup.parseBodyFragment(requestData);
		Element sheetPlotAttrs = requestDoc.getElementById("sheetPlotAttrs");
		
		inches = Integer.parseInt(sheetPlotAttrs.getElementById("inches").text());
		gridLen = Integer.parseInt(sheetPlotAttrs.getElementById("gridLen").text());
		temp = Integer.parseInt(sheetPlotAttrs.getElementById("temp").text());
		hum = Integer.parseInt(sheetPlotAttrs.getElementById("hum").text());

		plot = HASPOrder.tryDateTime(sheetPlotAttrs, "plot");

		typeOfCheck = sheetPlotAttrs.getElementById("typeOfCheck").text();
		plotter = sheetPlotAttrs.getElementById("plotter").text();
		
		rejectRollValue = sheetPlotAttrs.getElementById("rejectRollValue").hasClass("active");
		processWasteValue = sheetPlotAttrs.getElementById("processWasteValue").hasClass("active");
		lateValue = sheetPlotAttrs.getElementById("lateValue").hasClass("active");
		customerReworkValue = sheetPlotAttrs.getElementById("customerReworkValue").hasClass("active");
		processReworkValue = sheetPlotAttrs.getElementById("processReworkValue").hasClass("active");

		comments = sheetPlotAttrs.getElementById("comments").text();
	}
	
}
