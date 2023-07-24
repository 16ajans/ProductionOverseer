package productionoverseer;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
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
		driver.get("https://eimmt.web.boeing.com/eimmt-web/app/");
		
		new WebDriverWait(driver, Duration.ofSeconds(30))
		        .until(ExpectedConditions.elementToBeClickable(By.id("menuHaspInquireLink")));
		}
	
	public void close() {
		driver.quit();
	}
	
	//public List<SparseHASPOrder> queryHASPOrders(String orderDeskPerson, Date orderDateTime) {
	public void queryHASPOrders(String orderDeskPerson, String orderDateTime) {
		
		String uri;
		if (orderDeskPerson != null) {
			uri = "https://eimmt.web.boeing.com/eimmt-web/app/#/hasp/inquire?aopn=1&orderDeskPerson=" + orderDeskPerson;
		} else {
			uri = "https://eimmt.web.boeing.com/eimmt-web/app/#/hasp/inquire?aopn=1";
		}
		
		driver.get(uri);
		
		new WebDriverWait(driver, Duration.ofSeconds(3)).until(ExpectedConditions.presenceOfElementLocated(By.id("searchResult")));
		driver.findElement(By.id("addParamSelectComboboxInput")).sendKeys("Order Date Time");
		driver.findElement(By.id("addCriteriaBtn")).click();
		driver.findElement(By.id("{{cr.id + '_1'}}Date")).sendKeys(orderDateTime);
		driver.findElement(By.id("submit")).click();
		
	}
	
}