package com.mauriciotogneri.android.publisher;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets.Details;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import static com.google.api.services.androidpublisher.AndroidPublisherScopes.ANDROIDPUBLISHER;

public class Authentication
{
    private Credential authorizeWithServiceAccount(String serviceAccountEmail,
                                                   String keyP12Path,
                                                   HttpTransport httpTransport,
                                                   JsonFactory jsonFactory) throws Exception
    {
        System.out.println(String.format("Authorizing using Service Account: %s", serviceAccountEmail));

        return new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .setServiceAccountId(serviceAccountEmail)
                .setServiceAccountScopes(Collections.singleton(ANDROIDPUBLISHER))
                .setServiceAccountPrivateKeyFromP12File(new File(keyP12Path))
                .build();
    }

    private Credential authorizeWithInstalledApplication(String clientId,
                                                         String clientSecret,
                                                         HttpTransport httpTransport,
                                                         JsonFactory jsonFactory) throws Exception
    {
        System.out.println("Authorizing using installed application");

        Details details = new Details();
        details.setClientId(clientId);
        details.setClientSecret(clientSecret);
        details.setRedirectUris(Arrays.asList("http://localhost:35652/Callback"));
        details.setAuthUri("https://accounts.google.com/o/oauth2/auth");
        details.setAuthUri("https://accounts.google.com/o/oauth2/token");

        GoogleClientSecrets clientSecrets = new GoogleClientSecrets();
        clientSecrets.setInstalled(details);

        FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(new File(System.getProperty("user.home"),
                                                                                  ".store/android_publisher_api"));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow
                .Builder(httpTransport, jsonFactory, clientSecrets, Collections.singleton(ANDROIDPUBLISHER))
                .setDataStoreFactory(dataStoreFactory).build();

        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    public AndroidPublisher publisher(Config config) throws Exception
    {
        Credential credential;

        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        if (config.hasServiceAccount())
        {
            credential = authorizeWithServiceAccount(config.serviceAccountEmail(), config.keyP12Path(), httpTransport, jsonFactory);
        }
        else if (config.hasInstalledApplication())
        {
            credential = authorizeWithInstalledApplication(config.clientId(), config.clientSecret(), httpTransport, jsonFactory);
        }
        else
        {
            throw new RuntimeException("No authentication method defined");
        }

        return new AndroidPublisher.Builder(httpTransport, jsonFactory, credential).setApplicationName(config.applicationName()).build();
    }
}