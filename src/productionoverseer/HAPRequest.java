package productionoverseer;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class HAPRequest {

	String uri;

	boolean hydrated;

	String parent;
	String requestId;
	String plotOperator;
	String plotOperatorName; // TODO: how to get?

	String typeOfCheck;
	String plotter;
	String comments;

	LocalDateTime plot;

	String inches;
	String gridLen;
	String temp;
	String hum;

	boolean rejectRollValue;
	boolean processWasteValue;
	boolean lateValue;
	boolean customerReworkValue;
	boolean processReworkValue;

	HAPRequest(String uri, String requestId) {
		this.uri = "https://eimmt.web.boeing.com/eimmt-web/app/" + uri;
		this.requestId = requestId;

		hydrated = false;
	}

	public void hydrate(String requestData, String parent) {
		this.parent = parent;

		Document requestDoc = Jsoup.parseBodyFragment(requestData);
		Element sheetPlotAttrs = requestDoc.getElementById("sheetPlotAttrs");

		plotOperator = sheetPlotAttrs.getElementById("plotOperator").text();

		inches = sheetPlotAttrs.getElementById("inches").text();
		gridLen = sheetPlotAttrs.getElementById("gridLen").text();
		temp = sheetPlotAttrs.getElementById("temp").text();
		hum = sheetPlotAttrs.getElementById("hum").text();

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

	public List<String> listAttrs() {
		return Arrays.asList(parent, requestId, plotOperator, plotOperatorName, typeOfCheck, plotter, inches, gridLen,
				temp, hum, comments);
	}

	public List<LocalDateTime> listDates() {
		return Arrays.asList(plot);
	}

	public List<Boolean> listBool() {
		return Arrays.asList(rejectRollValue, processWasteValue, lateValue, customerReworkValue, processReworkValue);
	}
}
