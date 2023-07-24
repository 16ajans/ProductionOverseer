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
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class EIMMTLink {

	private WebDriver driver;
	
	EIMMTLink() {
		FirefoxOptions options = new FirefoxOptions();
		//options.addArguments("-headless");
		
		driver = new FirefoxDriver(options);
		System.out.println("Browser started.");
		driver.get("https://eimmt.web.boeing.com/eimmt-web/app/");
		
		new WebDriverWait(driver, Duration.ofSeconds(30))
		        .until(ExpectedConditions.elementToBeClickable(By.id("menuHaspInquireLink")));
		}
	
	public void close() {
		driver.quit();
		System.out.println("Browser closed.");
	}
	
	public List<HASPOrder> queryHASPOrders(String orderDeskPerson, String orderDateTime) {
		
		String xpath;
		String uri;
		if (orderDeskPerson != null) {
			uri = "https://eimmt.web.boeing.com/eimmt-web/app/#/hasp/inquire?aopn=1&orderDeskPerson=" + orderDeskPerson;
			xpath = "/html/body/div[3]/div/div[3]/table/thead/tr/th[8]";
		} else {
			uri = "https://eimmt.web.boeing.com/eimmt-web/app/#/hasp/inquire?aopn=1";
			xpath = "/html/body/div[3]/div/div[3]/table/thead/tr/th[7]";
		}
		
		driver.get(uri);
		new WebDriverWait(driver, Duration.ofSeconds(3)).until(ExpectedConditions.presenceOfElementLocated(By.id("searchResult")));
		
		driver.findElement(By.id("addParamSelectComboboxInput")).sendKeys("Order Date Time");
		driver.findElement(By.id("addCriteriaBtn")).click();
		driver.findElement(By.id("{{cr.id + '_1'}}Date")).sendKeys(orderDateTime);
		if (orderDeskPerson == null) {
			driver.findElement(By.id("addParamSelectComboboxInput")).sendKeys("SITE Requesting");
			driver.findElement(By.id("addCriteriaBtn")).click();
			driver.findElement(By.id("siteRequesting")).sendKeys("KENT");
		}
		
		driver.findElement(By.id("submit")).click();
		new WebDriverWait(driver, Duration.ofSeconds(3)).until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
		
		WebElement searchResult = driver.findElement(By.id("searchResult"));
		
		Document resultDoc = Jsoup.parseBodyFragment(searchResult.getAttribute("outerHTML"));
		Element resultTable = resultDoc.getElementById("searchResult");
		Elements rows = resultTable.getElementsByTag("tr");
		rows.remove(0);
		
		List<HASPOrder> orders = new ArrayList<HASPOrder>();
		for (Element row : rows) {
			String orderUri = row.select("a").first().attr("href");
			String orderId = row.select("td").first().text();
			orders.add(new HASPOrder(orderUri, orderId));
		}
		
		System.out.println("Found " + orders.size() + " orders.");
		return orders;
	}
	
	public void hydrateHASPOrder(HASPOrder order) {
				
		driver.get(order.uri);
		
		WebElement orderInfo = new WebDriverWait(driver, Duration.ofSeconds(3)).until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[3]/div/div[4]")));
		
		order.hydrate(orderInfo.getAttribute("outerHTML"));
	}
	
}