package productionoverseer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverService;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class EIMMTLink {

	private WebDriver driver;
	private HASPOrder lastOrder;

	EIMMTLink(Boolean headless) {
		FirefoxOptions options = new FirefoxOptions();
		if (headless)
			options.addArguments("-headless");

		FirefoxDriverService service = new GeckoDriverService.Builder().withLogOutput(System.out).build();

		driver = new FirefoxDriver(service, options);
		System.out.println("Browser started.");
		driver.get("https://eimmt.web.boeing.com/eimmt-web/app/");

		new WebDriverWait(driver, Duration.ofSeconds(90))
				.until(ExpectedConditions.elementToBeClickable(By.id("menuHaspInquireLink")));

		System.out.println("Passed authentication.");
		lastOrder = null;
	}

	public void close() {
		driver.quit();
		System.out.println("Browser closed.");
	}

	public List<HASPOrder> queryHASPOrders(String ordDeskUser, String orderDateTimeFrom, String orderDateTimeTo) {

		String xpath;
		String uri;
		if (ordDeskUser != null) {
			uri = "https://eimmt.web.boeing.com/eimmt-web/app/#/hasp/inquire?aopn=1&orderDeskPerson=" + ordDeskUser;
			xpath = "/html/body/div[3]/div/div[3]/table/thead/tr/th[8]";
		} else {
			uri = "https://eimmt.web.boeing.com/eimmt-web/app/#/hasp/inquire?aopn=1";
			xpath = "/html/body/div[3]/div/div[3]/table/thead/tr/th[7]";
		}

		driver.get(uri);
		new WebDriverWait(driver, Duration.ofSeconds(3))
				.until(ExpectedConditions.presenceOfElementLocated(By.id("searchResult")));

		if (orderDateTimeFrom != null || orderDateTimeTo != null) {
			driver.findElement(By.id("addParamSelectComboboxInput")).sendKeys("Order Date Time");
			driver.findElement(By.id("addCriteriaBtn")).click();
		}
		if (orderDateTimeFrom != null) {
			driver.findElement(By.id("{{cr.id + '_1'}}Date")).sendKeys(orderDateTimeFrom);
		}
		if (orderDateTimeTo != null) {
			driver.findElement(By.id("{{cr.id + '_2'}}Date")).sendKeys(orderDateTimeTo);
		}
		driver.findElement(By.id("addParamSelectComboboxInput")).sendKeys("SITE Requesting");
		driver.findElement(By.id("addCriteriaBtn")).click();
		driver.findElement(By.id("siteRequesting")).sendKeys("KENT");

		driver.findElement(By.id("submit")).click();
		new WebDriverWait(driver, Duration.ofSeconds(3))
				.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));

		WebElement searchResult = driver.findElement(By.id("searchResult"));

		Document resultDoc = Jsoup.parseBodyFragment(searchResult.getAttribute("outerHTML"));
		Element resultTable = resultDoc.getElementById("searchResult");
		Elements rows = resultTable.getElementsByTag("tr");
		rows.remove(0);

		List<HASPOrder> orders = new ArrayList<HASPOrder>();
		for (Element row : rows) {
			String orderUri = row.select("a").first().attr("href");
			String orderId = row.select("td[name=orderId]").first().text();
			String drawingNumber = row.select("td[name=drawingNbr]").first().text();
			String sheetId = row.select("td[name=sheetId]").first().text();

			orders.add(new HASPOrder(orderUri, orderId, drawingNumber, sheetId));
		}

		System.out.println("Found " + orders.size() + " orders.");
		return orders;
	}

//	private List<HAPRequest> getHAPRequests(HASPOrder parent) {
//		List<HAPRequest> requests = new ArrayList<>();
//
//		Pattern numbers = Pattern.compile("[0-9]+");
//
//		WebElement haprs = driver.findElement(By.id("showHaprs"));
//		Matcher matcher = numbers.matcher(haprs.getAttribute("innerHTML"));
//		matcher.find();
//		if (Integer.parseInt(matcher.group()) > 0) {
//			haprs.click();
//
//			WebElement searchResult = new WebDriverWait(driver, Duration.ofSeconds(3))
//					.until(ExpectedConditions.presenceOfElementLocated(
//							By.xpath("/html/body/div[3]/div/div[3]/div/div/div/div[2]/div/div[1]/div[1]/table")));
//
//			Document resultDoc = Jsoup.parseBodyFragment(searchResult.getAttribute("outerHTML"));
//			Elements rows = resultDoc.getElementsByTag("tr");
//			rows.remove(0);
//
//			for (int i = 0; i < rows.size(); i++) {
//				String requestId = rows.get(i).select("td:nth-child(1) > a").first().text();
//				String ploperator = rows.get(i).select("td:nth-child(3) > a").first().text();
//
//				new WebDriverWait(driver, Duration.ofSeconds(3))
//						.until(ExpectedConditions.elementToBeClickable(
//								By.xpath("//*[@id=\"haprsTable\"]/div[1]/table/tbody/tr[" + (i + 1) + "]/td[1]/a")))
//						.click();
//
//				new WebDriverWait(driver, Duration.ofSeconds(3)).until(
//						ExpectedConditions.textToBePresentInElementLocated(By.id("haprsPopupHeadline"), requestId));
//
//				WebElement requestInfo = driver.findElement(By.id("sheetPlotAttrs"));
//				requests.add(new HAPRequest(parent, requestId, ploperator, requestInfo.getAttribute("outerHTML")));
//
//				driver.findElement(By.id("back2haprsTable")).click();
//			}
//
//			driver.findElement(By.xpath("/html/body/div[3]/div/div[3]/div/div/div/div[1]/button")).click();
//		}
//
//		return requests;
//	}

	public void hydrateHASPOrder(HASPOrder order) throws FoundDuplicateOrderException {
		driver.get(order.uri);

		new WebDriverWait(driver, Duration.ofMillis(500)).until(ExpectedConditions.and(
				ExpectedConditions.textToBePresentInElementLocated(By.id("drawingNumber"), order.drawingNumber),
				ExpectedConditions.textToBePresentInElementLocated(By.id("sheetId"), order.sheetId)));
		if (lastOrder != null) {
			if (lastOrder.cancelled != null && lastOrder.drawingNumber == order.drawingNumber
					&& lastOrder.sheetId == order.sheetId) {
				if (driver.findElement(By.id("otherSys")).getText() == lastOrder.otherSys) {
					throw new FoundDuplicateOrderException(lastOrder, order);
				}
			}
		}

		WebElement orderInfo = driver.findElement(By.xpath("/html/body/div[3]/div/div[4]"));
		order.hydrate(orderInfo.getAttribute("outerHTML"));

		lastOrder = order;

//		return getHAPRequests(order);
	}

	public class FoundDuplicateOrderException extends Exception {

		private static final long serialVersionUID = -1261435400048557674L;

		final HASPOrder o1;
		final HASPOrder o2;

		public FoundDuplicateOrderException(HASPOrder o1, HASPOrder o2) {
			super("Found duplicate HASP Order in EIMMT.");
			this.o1 = o1;
			this.o2 = o2;
		}

		public void printOrderIds() {
			System.out.println("Duplicates: " + o1.orderId + " and " + o2.orderId);
		}
	}

}