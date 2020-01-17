package name.qd.bsr.utils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.qd.bsr.BuySellRecorder;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LineNotifyUtils {
	private static Logger log = LoggerFactory.getLogger(LineNotifyUtils.class);
	private final OkHttpClient okHttpClient = new OkHttpClient.Builder().pingInterval(10, TimeUnit.SECONDS).build();
	private Request.Builder requestBuilder;
	
	public LineNotifyUtils(String token) {
		HttpUrl httpUrl = HttpUrl.parse("https://notify-api.line.me/api/notify");
		HttpUrl.Builder urlBuilder = httpUrl.newBuilder();
		requestBuilder = new Request.Builder().url(urlBuilder.build().url().toString());
		requestBuilder.addHeader("Authorization", "Bearer " + token);
	}
	
	public void sendMessage(String message) {
		FormBody.Builder bodyBuilder = new FormBody.Builder();
		bodyBuilder.addEncoded("message", message);
		Request request = requestBuilder.post(bodyBuilder.build()).build();
		try {
			Response response = okHttpClient.newCall(request).execute();
			log.info("Send message to LINE result:{}", response.body().string());
		} catch (IOException e) {
			log.error("Send message to LINE failed.", e);
		}
	}
}
