package productionoverseer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class HASPOrder {

	static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");

	Boolean hydrated;

	String uri;
	String orderId;

	String drawingNumber;
	String sheetId;
	String revision;
	String disclosureValue;
	String airplaneModel;

	String suppCode;
	String suppName;
	String custBemsid;
	String custName;
	String deliverTo;
	String buLocDept;

	String ordDeskUser;
	String ordDeskUserName;
	String siteRequesting;
	String sitePerformingLoc;
	String otherSys;
	String priority;
	String media;
	String convVendor;

	String orderComments;

	LocalDateTime order;
	LocalDateTime customerRequest;
	LocalDateTime orderDeskFtpHap;
	LocalDateTime cancelled;
	LocalDateTime vendorProcess;
	LocalDateTime hapPdtCompleted;

	HASPOrder(String uri, String orderId, String drawingNumber, String sheetId) {
		this.uri = "https://eimmt.web.boeing.com/eimmt-web/app/" + uri;
		this.orderId = orderId;
		this.drawingNumber = drawingNumber;
		this.sheetId = sheetId;
		hydrated = false;
	}

	public void hydrate(String orderData) {
		Document orderDoc = Jsoup.parseBodyFragment(orderData);

		Element drawingAttributes = orderDoc.getElementById("drawingAttributes");
		revision = drawingAttributes.getElementById("revision").text();
		disclosureValue = drawingAttributes.getElementById("disclosureValue").text();
		airplaneModel = drawingAttributes.getElementById("airplaneModel").text();

		Element suppParameters = orderDoc.getElementById("suppParameters");
		String[] suppInfo = suppParameters.getElementById("suppCode").text().split("-");
		if (suppInfo.length > 1) {
			suppCode = suppInfo[0].strip();
			suppName = suppInfo[1].strip();
		} else {
			suppCode = "";
			suppName = "";
		}
		String[] custInfo = suppParameters.getElementById("custBemsid").text().split("-");
		if (custInfo.length > 1) {
			custBemsid = custInfo[0].strip();
			custName = custInfo[1].strip();
		} else {
			custBemsid = "";
			custName = "";
		}
		deliverTo = suppParameters.getElementById("deliverTo").text();
		buLocDept = suppParameters.getElementById("buLocDept").text();

		Element orderParameters = orderDoc.getElementById("orderParameters");
		String[] ordDeskUserInfo = orderParameters.getElementById("ordDeskUser").text().split("-");
		ordDeskUser = ordDeskUserInfo[0].strip();
		ordDeskUserName = ordDeskUserInfo[1].strip();
		siteRequesting = orderParameters.getElementById("siteRequesting").text();
		sitePerformingLoc = orderParameters.getElementById("sitePerformingLoc").text();
		otherSys = orderParameters.getElementById("otherSys").text();
		priority = orderParameters.getElementById("priority").text();
		media = orderParameters.getElementById("media").text();
		convVendor = orderParameters.getElementById("convVendor").text();

		orderComments = orderParameters.getElementById("orderComments").text();

		order = tryDateTime(orderParameters, "order");
		customerRequest = tryDateTime(orderParameters, "customerRequest");
		orderDeskFtpHap = tryDateTime(orderParameters, "orderDeskFtpHap");
		cancelled = tryDateTime(orderParameters, "cancelled");
		vendorProcess = tryDateTime(orderParameters, "vendorProcess");
		hapPdtCompleted = tryDateTime(orderParameters, "hapPdtCompleted");

		hydrated = true;
	}

	public List<String> toList() {

		return Arrays.asList(orderId, drawingNumber, sheetId, revision, disclosureValue, airplaneModel, suppCode,
				suppName, custBemsid, custName, deliverTo, buLocDept, ordDeskUser, ordDeskUserName, siteRequesting,
				sitePerformingLoc, otherSys, priority, media, convVendor, orderComments, tryDateTime(order),
				tryDateTime(customerRequest), tryDateTime(orderDeskFtpHap), tryDateTime(cancelled),
				tryDateTime(vendorProcess), tryDateTime(hapPdtCompleted));
	}

	private static LocalDateTime tryDateTime(Element parent, String baseId) {
		try {
			String date = parent.getElementById(baseId + "Date").text();
			String time = parent.getElementById(baseId + "Time").text();
			return LocalDateTime.parse(String.join(" ", date, time), formatter);
		} catch (DateTimeParseException e) {
			return null;
		}

	}

	private static String tryDateTime(LocalDateTime dateTime) {
		try {
			return dateTime.format(formatter);
		} catch (NullPointerException e) {
			return "";
		}

	}

}
