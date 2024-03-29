package name.qd.bsr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.imageio.ImageIO;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.qd.bsr.utils.TWSECaptchaSolver;

public class BuySellRecorder implements Runnable {
	private static Logger log = LoggerFactory.getLogger(BuySellRecorder.class);
	private WebDriver webDriver;
	private BufferedImage bufferedImage;
	private TWSECaptchaSolver captchaSolver;
	private String captchaPath;
	private List<String> lst;
	private List<String> lstRemain = new ArrayList<>();
	private String dir;
	private int total;
	private final int workerId;
	private CountDownLatch countDownLatch;
	
	public BuySellRecorder(int workerId, List<String> lst, String dir, CountDownLatch countDownLatch) {
		this.lst = lst;
		total = lst.size();
		for(String product : lst) {
			lstRemain.add(product);
		}
		this.workerId = workerId;
		this.dir = dir;
		captchaSolver = new TWSECaptchaSolver();
		this.countDownLatch = countDownLatch;
	}
	
	@Override
	public void run() {
		init();
		startDownload();
		end();
		log.info("{} worker done. {}", workerId, total);
		countDownLatch.countDown();
	}
	
	private void downloadData(String product) throws Exception {
		downloadCaptcha();
		String ans = captchaSolver.solve(captchaPath);
		downloadFile(product, ans);
	}
	
	private void downloadFile(String product, String ans) throws Exception {
		WebElement inputStock = webDriver.findElement(By.name("TextBox_Stkno"));
		inputStock.clear();
		inputStock.sendKeys(product);
		WebElement inputCaptcha = webDriver.findElement(By.name("CaptchaControl1"));
		inputCaptcha.sendKeys(ans);
		WebElement btn = webDriver.findElement(By.name("btnOK"));
		btn.click();
		WebElement downloadLink = webDriver.findElement(By.id("HyperLink_DownloadCSV"));
		String downloadPath = downloadLink.getAttribute("href");
		webDriver.get(downloadPath);
	}
	
	private void downloadCaptcha() throws Exception {
		List<WebElement> images = webDriver.findElements(By.tagName("img"));
		URL url;
		url = new URL(images.get(1).getAttribute("src"));
		bufferedImage = ImageIO.read(url);
		ImageIO.write(bufferedImage, "jpg", new File(captchaPath));
	}
	
	private void startDownload() {
		webDriver.get("http://bsr.twse.com.tw/bshtm/bsMenu.aspx");
		for(String product : lst) {
			try {
				if(isFileExist(product)) {
					log.info("[{}] File downloaded. {}", workerId, product);
					lstRemain.remove(product);
					continue;
				}
				downloadData(product);
			} catch (Exception e) {
				log.error("[{}] Download {} failed. Remain:{}", workerId, product, lstRemain.size());
				break;
			}
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		lst.clear();
		for(String product : lstRemain) {
			lst.add(product);
		}
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if(lst.size() > 0) {
			startDownload();
		}
	}
	
	private boolean isFileExist(String product) {
		return Files.exists(Paths.get(dir, product + ".csv"));
	}
	
	private void init() {
		ChromeOptions options = new ChromeOptions();
		options.setHeadless(true);
		Map<String, Object> chromePrefs = new HashMap<String, Object>();
		chromePrefs.put("download.default_directory", dir);
		options.setExperimentalOption("prefs", chromePrefs);
		webDriver = new ChromeDriver(options);
		Path path = Paths.get("./bsr/", String.valueOf(workerId));
		if(!Files.exists(path)) {
			try {
				Files.createDirectory(path);
			} catch (IOException e) {
				log.error("Create worker captcha folder failed. {}", path.toString());
			}
		}
		
		captchaPath = "./bsr/" + workerId + "/twse.jpg";
	}
	
	private void end() {
		captchaSolver.end();
		webDriver.close();
		webDriver.quit();
	}
}
