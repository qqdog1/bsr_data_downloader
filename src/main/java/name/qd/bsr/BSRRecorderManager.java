package name.qd.bsr;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.qd.analysis.Constants.Exchange;
import name.qd.analysis.dataSource.DataSource;
import name.qd.analysis.dataSource.DataSourceFactory;
import name.qd.analysis.dataSource.TWSE.utils.TWSEPathUtil;
import name.qd.analysis.dataSource.vo.ProductClosingInfo;
import name.qd.analysis.utils.TimeUtil;

public class BSRRecorderManager {
	private Logger log;
	private static int WORKER_COUNT = 1;
	private static String CONF_PATH = "./config/bsr.conf";
	private static String LOG_CONF_PATH = "./config/log4j2.xml";
	private static String CHROME_DRIVER = "./bsr/driver/chromedriver.exe";
	private static String BSR_DOWNLOAD_FOLDER = "bsr_download_folder";
	private final ExecutorService executor = Executors.newFixedThreadPool(WORKER_COUNT);
	private SimpleDateFormat sdf = TimeUtil.getDateFormat();
	private Date date;
	private DataSource dataSource;
	private String targetFolder;
	private int total;
	private List<List<String>> lstWorkerProducts;
	private Properties properties;
	private String baseFolder;
	
	public BSRRecorderManager() {
		initSysProp();
		initDate();
		initConfig();
		dataSource = DataSourceFactory.getInstance().getDataSource(Exchange.TWSE, baseFolder);
		initFolder();
		initProducts();
		initWorkers();
	}
	
	private void initDate() {
		date = TimeUtil.getToday();
//		try {
//			date = TimeUtil.getDateFormat().parse("20200102");
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
		log.info("{}", date);
	}
	
	private void initConfig() {
		try {
			properties = new Properties();
			FileInputStream fIn = new FileInputStream(CONF_PATH);
			properties.load(fIn);
			fIn.close();
		} catch (IOException e) {
			log.error("Init config failed.", e);
		}
		
		baseFolder = properties.getProperty(BSR_DOWNLOAD_FOLDER);
	}
	
	private void initFolder() {
		Path path = TWSEPathUtil.getBuySellInfoFolder(baseFolder, sdf.format(date));
		targetFolder = path.toString();
		if(!Files.exists(path)) {
			try {
				Files.createDirectories(path);
			} catch (IOException e) {
				log.error("Create dir failed.", e);
			}
		}
	}
	
	private void initProducts() {
		Map<Date, List<ProductClosingInfo>> map = null;
		List<String> lst = new ArrayList<>();
		try {
			map = dataSource.getAllProductClosingInfo(date, date);
		} catch (Exception e) {
			log.error("Get product list failed.", e);
		}
		
		for(List<ProductClosingInfo> lstProducts : map.values()) {
			for(ProductClosingInfo productInfo : lstProducts) {
				lst.add(productInfo.getProduct());
			}
		}
		
		total = lst.size();
		log.info("Total products : {}", total);
		
		List<String> lstRemain = new ArrayList<>();
		for(String product : lst) {
			if(!Files.exists(Paths.get(targetFolder, product + ".csv"))) {
				lstRemain.add(product);
			}
		}
		
		lstWorkerProducts = new ArrayList<>();
		
		for(int i = 0 ; i < WORKER_COUNT ; i++) {
			lstWorkerProducts.add(new ArrayList<>());
		}
		
		for(int i = 0 ; i < lstRemain.size() ; i++) {
			int index = i % WORKER_COUNT;
			lstWorkerProducts.get(index).add(lstRemain.get(i));
		}
	}
	
	private void initSysProp() {
		Properties prop = System.getProperties();
		prop.setProperty("log4j.configurationFile", LOG_CONF_PATH);
		prop.setProperty("webdriver.chrome.driver", CHROME_DRIVER);
		log = LoggerFactory.getLogger(BSRRecorderManager.class);
	}
	
	private void initWorkers() {
		for(int i = 0 ; i < WORKER_COUNT ; i++) {
			executor.execute(new BuySellRecorder(i, lstWorkerProducts.get(i), targetFolder));
		}
		executor.shutdown();
	}
	
	public static void main(String[] s) {
		new BSRRecorderManager();
	}
}
