package productionoverseer;

import java.time.LocalDateTime;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class HAPRequest {
	
	String uri;
	HASPOrder parent;
	
	boolean hydrated;
	
	String requestId;
	String plotOperator;
	String plotOperatorName; // TODO: how to get?
	
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
	
	HAPRequest(String uri, String requestId) {
		this.uri = "https://eimmt.web.boeing.com/eimmt-web/app/" + uri;
		this.requestId = requestId;
		
		hydrated = false;
	}

	public void hydrate(String requestData) {
		Document requestDoc = Jsoup.parseBodyFragment(requestData);
		Element sheetPlotAttrs = requestDoc.getElementById("sheetPlotAttrs");
		
		plotOperator = sheetPlotAttrs.getElementById("plotOperator").text();
		
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
		
		hydrated = true;
	}
	
}
