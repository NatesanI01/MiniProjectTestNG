package testScript;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import commonutils.Utility;
import io.github.bonigarcia.wdm.WebDriverManager;

public class ProductStorePageTest {
  WebDriver driver;
  Properties temProp;
  ExtentReports reports;
  ExtentSparkReporter spark;
  ExtentTest extentTest;
  WebDriverWait wait;
  
  @BeforeClass(groups={"featureOne","featureTwo"})
  public void getExtent() {
	  reports=new ExtentReports();
	  spark=new ExtentSparkReporter("target//ProductStore.html");
	  reports.attachReporter(spark);
	  wait=new WebDriverWait(driver,Duration.ofSeconds(10));
  }
  
  @Parameters("browser")
  @BeforeTest(groups={"featureOne","featureTwo"})
  public void setup(String strBrowser) throws IOException {
	  if(strBrowser.equalsIgnoreCase("chrome")) {
		  WebDriverManager.chromedriver().setup();
		  driver=new ChromeDriver();
	  }
	  driver.manage().window().maximize();
	  driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
	  driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
	  temProp=new Properties();
	  String path=System.getProperty("user.dir")+"//src//test//resources//configFiles//productStore.properties";
	  FileInputStream obtained=new FileInputStream(path);
	  temProp.load(obtained);
	  driver.get(temProp.getProperty("url"));
  }
  
  @Test(priority=1,groups={"featureOne","featureTwo"})
  public void loginTest() throws InvalidFormatException, IOException, InterruptedException {
	  extentTest=reports.createTest("Login Page Test");
	  driver.findElement(By.xpath(readExcelData("login"))).click();
	  driver.findElement(By.xpath(readExcelData("username"))).sendKeys(temProp.getProperty("username"));
	  driver.findElement(By.xpath(readExcelData("pwd"))).sendKeys(temProp.getProperty("pwd"));
	  driver.findElement(By.xpath(readExcelData("loginBtn"))).click();
	  Thread.sleep(4000);
	  Assert.assertEquals(driver.findElement(By.xpath(readExcelData("successMsg"))).getText(), "Welcome glass");
  }
  
  public String readExcelData(String objName) throws InvalidFormatException, IOException {
	  String objPath="";
	  String path=System.getProperty("user.dir")+"//src//test//resources//testdata//ProductStore.xlsx";
	  XSSFWorkbook workbook=new XSSFWorkbook(new File(path));
	  XSSFSheet sheet=workbook.getSheet("loginPage");
	  int numRow=sheet.getLastRowNum();
	  for(int i=1;i<=numRow;i++) {
		  XSSFRow row=sheet.getRow(i);
		  if(row.getCell(0).getStringCellValue().equalsIgnoreCase(objName)) {
			  objPath=row.getCell(1).getStringCellValue();
		  }
	  }
	  return objPath;
  }
  
//  @Test(priority=2,groups="featureOne",dataProvider="searchsin")
//  public void addItemtoCart(String search) throws InvalidFormatException, IOException, InterruptedException {
//	  extentTest=reports.createTest("Add Single Item To Cart Test");
//	  driver.findElement(By.linkText(search)).click();
//	  String phoneName=driver.findElement(By.xpath("//h2")).getText();
////	  Thread.sleep(2000);
//	  driver.findElement(By.xpath(readExcelData("addtocart"))).click();
////	  Thread.sleep(2000);
//	  Alert alert=driver.switchTo().alert();
//	  Assert.assertEquals(alert.getText(), "Product added.");
//	  alert.accept();
//	  driver.findElement(By.xpath(readExcelData("cart"))).click();
//	  List<WebElement> items=driver.findElements(By.xpath("//td[2]"));
//	  Assert.assertEquals(items.get(0).getText(), phoneName);
//  }
// 
  
  @Test(priority=2,dataProvider="search",groups="featureTwo")
  public void addMulItemtoCart(String search,String catagory) throws InvalidFormatException, IOException, InterruptedException {
	  extentTest=reports.createTest("Add Multiple Item To Cart Test");
	  driver.findElement(By.xpath(readExcelData("home"))).click();
	  String path="//a[text()='"+catagory+"']";
	  driver.findElement(By.xpath(path)).click();
	  Thread.sleep(2000);
	  String searchPath="//a[text()='"+search+"']"; 
	  driver.findElement(By.xpath(searchPath)).click();
	  String name=driver.findElement(By.xpath("//h2")).getText();
	  Thread.sleep(2000);
	  driver.findElement(By.xpath(readExcelData("addtocart"))).click();
	  Thread.sleep(2000);
	  Alert alert=driver.switchTo().alert();
	  Assert.assertEquals(alert.getText(), "Product added.");
	  alert.accept();
	  driver.findElement(By.xpath(readExcelData("cart"))).click();
	  List<WebElement> items=driver.findElements(By.xpath("//td[2]"));
	  for(WebElement item:items) {
		  if(item.getText().equalsIgnoreCase(name)) {
			  Assert.assertEquals(item.getText(), name);
		  }
	  }
  }
  
  @DataProvider(name="search")
  public Object[][] getData() throws CsvValidationException, IOException{
	  String path=System.getProperty("user.dir")+"//src//test//resources//testdata//ProductStoreSin.csv";
	  CSVReader reader=new CSVReader(new FileReader(path));
	  String[] cols;
	  ArrayList<Object> dataList=new ArrayList<Object>();
	  while((cols=reader.readNext())!=null) {
		  Object[] records= {cols[0],cols[1]};
		  dataList.add(records);
	  }
	  return dataList.toArray(new Object[dataList.size()][]);
  }
  
  @Test(priority=3,groups="featureTwo")
  public void deleteanItemTest() throws InvalidFormatException, IOException, InterruptedException {
	  extentTest=reports.createTest("Delete an Item in Cart Test");
	  driver.findElement(By.xpath(readExcelData("home"))).click();
	  driver.findElement(By.xpath(readExcelData("cart"))).click();
	  List<WebElement> itemsBefore=driver.findElements(By.xpath("//tr[@class='success']"));
	  int num=itemsBefore.size();
	  driver.findElement(By.xpath("(//td[4]//a)[1]")).click();
	  Thread.sleep(5000);
	  List<WebElement> itemsAfter=driver.findElements(By.xpath("//tr[@class='success']"));
	  int num1=itemsAfter.size();
	  Thread.sleep(2000);
	  Assert.assertNotEquals(num1, num);
  }
  
  @Test(priority=4,groups= {"featureOne","featureTwo"})
  public void placeAnOrderTest() throws InterruptedException, InvalidFormatException, IOException {
	  extentTest=reports.createTest("Place an Order Test");
	  List<WebElement> items=driver.findElements(By.xpath("//tbody"));
	  if(items.size()!=0) {
		  driver.findElement(By.xpath(readExcelData("placeOrder"))).click();
		  driver.findElement(By.xpath(readExcelData("name"))).sendKeys(temProp.getProperty("name"));
		  driver.findElement(By.xpath(readExcelData("country"))).sendKeys(temProp.getProperty("country"));
		  driver.findElement(By.xpath(readExcelData("city"))).sendKeys(temProp.getProperty("city"));
		  driver.findElement(By.xpath(readExcelData("card"))).sendKeys(temProp.getProperty("cardNo"));
		  driver.findElement(By.xpath(readExcelData("month"))).sendKeys(temProp.getProperty("month"));
		  driver.findElement(By.xpath(readExcelData("year"))).sendKeys(temProp.getProperty("year"));
		  driver.findElement(By.xpath(readExcelData("purchase"))).click();
		  Assert.assertEquals(driver.findElement(By.xpath("(//h2)[3]")).getText(), "Thank you for your purchase!");
		  Thread.sleep(2000);
		  driver.findElement(By.xpath(readExcelData("okBtn"))).click();
		  Thread.sleep(2000);
	  }
  }
  
  @AfterMethod(groups={"featureOne","featureTwo"})
  public void finishExtent(ITestResult results) throws IOException {
	  if(ITestResult.FAILURE== results.getStatus()) {
		  extentTest.log(Status.FAIL, results.getThrowable().getMessage());
		  String path=Utility.getScreenshotPath(driver);
		  extentTest.addScreenCaptureFromPath(path);
	  }
  }
  
  @AfterTest(groups={"featureOne","featureTwo"})
  public void tearDown() {
	  driver.close();
	  reports.flush();
  }
}
