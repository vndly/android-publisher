package com.mauriciotogneri.android.publisher;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;

import java.io.File;
import java.util.Collections;

import static com.google.api.services.androidpublisher.AndroidPublisherScopes.ANDROIDPUBLISHER;

class Authentication
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

    AndroidPublisher publisher(Config config) throws Exception
    {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        Credential credential = authorizeWithServiceAccount(config.serviceAccountEmail(), config.keyP12Path(), httpTransport, jsonFactory);

        return new AndroidPublisher.Builder(httpTransport, jsonFactory, credential).setApplicationName("AndroidPublisher").build();
    }
}