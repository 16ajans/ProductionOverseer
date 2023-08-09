package productionoverseer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

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
	private boolean headless;

	List<HASPOrder> orders;
	List<HAPRequest> requests;

	String ordDeskUser;
	String dateFrom;
	String dateTo;

	EIMMTLink(Boolean headless, String ordDeskUser, String dateFrom, String dateTo) {
		this.headless = headless;
		lastOrder = null;

		this.ordDeskUser = ordDeskUser;
		this.dateFrom = dateFrom;
		this.dateTo = dateTo;
	}

	public void open() {
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
	}

	public void close() {
		driver.quit();
		System.out.println("Browser closed.");
	}

	public void queryHASPOrders() {

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

		if (dateFrom != null || dateTo != null) {
			driver.findElement(By.id("addParamSelectComboboxInput")).sendKeys("Order Date Time");
			driver.findElement(By.id("addCriteriaBtn")).click();
		}
		if (dateFrom != null) {
			driver.findElement(By.id("{{cr.id + '_1'}}Date")).sendKeys(dateFrom);
		}
		if (dateTo != null) {
			driver.findElement(By.id("{{cr.id + '_2'}}Date")).sendKeys(dateTo);
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
		this.orders = orders;
	}

	public void queryHAPRequests() {

		String xpath;
		String uri;
		uri = "https://eimmt.web.boeing.com/eimmt-web/app/#/hapr/inquire?aopn=1";
		xpath = "/html/body/div[3]/div/div[3]/table/thead/tr/th[7]";

		driver.get(uri);
		new WebDriverWait(driver, Duration.ofSeconds(3))
				.until(ExpectedConditions.presenceOfElementLocated(By.id("searchResult")));

		if (dateFrom != null || dateTo != null) {
			driver.findElement(By.id("addParamSelectComboboxInput")).sendKeys("Plotted Date");
			driver.findElement(By.id("addCriteriaBtn")).click();
		}
		if (dateFrom != null) {
			driver.findElement(By.id("{{cr.id + '_1'}}Date")).sendKeys(dateFrom);
		}
		if (dateTo != null) {
			driver.findElement(By.id("{{cr.id + '_2'}}Date")).sendKeys(dateTo);
		}

		driver.findElement(By.id("submit")).click();
		new WebDriverWait(driver, Duration.ofSeconds(3))
				.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));

		WebElement searchResult = driver.findElement(By.id("searchResult"));

		Document resultDoc = Jsoup.parseBodyFragment(searchResult.getAttribute("outerHTML"));
		Element resultTable = resultDoc.getElementById("searchResult");
		Elements rows = resultTable.getElementsByTag("tr");
		rows.remove(0);

		List<HAPRequest> requests = new ArrayList<>();
		for (Element row : rows) {
			String requestUri = row.select("a").first().attr("href");
			String requestId = row.select("td").first().text();

			requests.add(new HAPRequest(requestUri, requestId));
		}

		System.out.println("Found " + requests.size() + " requests.");
		this.requests = requests;
	}

	public void hydrateHASPOrder(HASPOrder order) throws FoundDuplicateOrderException {
		driver.get(order.uri);

		new WebDriverWait(driver, Duration.ofMillis(500)).until(ExpectedConditions.and(
				ExpectedConditions.textToBePresentInElementLocated(By.id("headline"), order.orderId),
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

		String orderInfo = driver.findElement(By.xpath("/html/body/div[3]/div/div[4]")).getAttribute("outerHTML");
		order.hydrate(orderInfo);

		lastOrder = order;
	}

	public void hydrateHAPRequest(HAPRequest request) {
		driver.get(request.uri);

		new WebDriverWait(driver, Duration.ofMillis(500))
				.until(ExpectedConditions.textToBePresentInElementLocated(By.id("headline"), request.requestId));

		WebElement requestInfo = driver.findElement(By.id("sheetPlotAttrs"));
		WebElement parent = driver.findElement(By.xpath("//*[@id=\"showHasp\"]/span"));
		request.hydrate(requestInfo.getAttribute("outerHTML"), parent.getText().split("#")[1]);
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