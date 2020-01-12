package name.qd.bsr.utils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GoogleDriveUploader {
	private static Logger log = LoggerFactory.getLogger(GoogleDriveUploader.class); 
	private final OkHttpClient okHttpClient = new OkHttpClient.Builder().pingInterval(10, TimeUnit.SECONDS).build();
	private String token;
	
	public GoogleDriveUploader(String token) {
		this.token = token;
	}

	public void uploadZip(String filePath) {
		HttpUrl httpUrl = HttpUrl.parse("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart");
		HttpUrl.Builder urlBuilder = httpUrl.newBuilder();
		Request.Builder requestBuilder = new Request.Builder().url(urlBuilder.build().url().toString());
		requestBuilder.addHeader("Authorization", "Bearer " + token);
		
		FormBody.Builder formBuilder = new FormBody.Builder();
		formBuilder.addEncoded("file", "@" + filePath);
		formBuilder.addEncoded("type", "application/zip");
		FormBody body = formBuilder.build();
		
		Request request = requestBuilder.post(body).build();
		try {
			Response response = okHttpClient.newCall(request).execute();
			String result = response.body().string();
			log.info(result);
		} catch (IOException e) {
			log.error("Upload file to google drive failed.", e);
		}
	}
}
