package name.qd.bsr.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GoogleDriveUploader {
	private static Logger log = LoggerFactory.getLogger(GoogleDriveUploader.class);
	private final OkHttpClient okHttpClient = new OkHttpClient.Builder().pingInterval(10, TimeUnit.SECONDS).build();
	private String token;
	
	private static HttpTransport HTTP_TRANSPORT;
	private static FileDataStoreFactory DATA_STORE_FACTORY;
	private static final java.io.File DATA_STORE_DIR = new java.io.File("./config/credentials.json");

	
	private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE_METADATA_READONLY);
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	
	public GoogleDriveUploader(String token) {
		this.token = token;
		
		try {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		} catch (GeneralSecurityException | IOException e) {
			log.error("init http transport failed.", e);
		}
		
		try {
			DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
		} catch (IOException e) {
			log.error("", e);
		}
	}

	public static Credential authorize() throws IOException {
		InputStream in = GoogleDriveUploader.class.getResourceAsStream("./config/credentials.json");
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES).setDataStoreFactory(DATA_STORE_FACTORY).setAccessType("offline").build();
		
		
		
		
		
		
		Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
		System.out.println("Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
		return credential;
	}

	public void uploadZip(String filePath) {
		Path path = Paths.get(filePath);
		File file = new File();
		file.setName(path.getFileName().toString());
		file.setMimeType("application/zip");

		java.io.File localFile = new java.io.File(filePath);

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
