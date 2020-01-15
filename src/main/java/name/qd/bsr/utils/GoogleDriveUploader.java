package name.qd.bsr.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

public class GoogleDriveUploader {
	private static final Logger log = LoggerFactory.getLogger(GoogleDriveUploader.class);
	private static final String APPLICATION_NAME = "BSR uploader";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    
    private String credentialsFilePath;
    private Drive drive;

    public GoogleDriveUploader(String credentialsFilePath) {
    	this.credentialsFilePath = credentialsFilePath;
    	
    	initDrive();
    }
    
    private void initDrive() {
		try {
			NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
	                .setApplicationName(APPLICATION_NAME)
	                .build();
		} catch (GeneralSecurityException | IOException e) {
			log.error("Init google drive failed.", e);
		}
    }
    
    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(new FileInputStream(credentialsFilePath)));
        
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
    
    public boolean uploadFile(String filePath) {
    	Path path = Paths.get(filePath);
    	
    	File file = new File();
        file.setName(path.getFileName().toString());
        file.setMimeType("application/zip");
        List<String> lstFolderId = new ArrayList<>();
        lstFolderId.add("1pJZZ33Biqlcs8oWOqIplDPvYe1nbmuHL");
        file.setParents(lstFolderId);
        
        java.io.File fileContent = new java.io.File(filePath);
        FileContent mediaContent = new FileContent("application/zip", fileContent);

		try {
			File fileUploaded = drive.files().create(file, mediaContent).execute();
			if(fileUploaded != null) {
				return true;
			}
		} catch (IOException e) {
			log.error("Upload file to google drive failed.", e);
		}
		return false;
    }
}
