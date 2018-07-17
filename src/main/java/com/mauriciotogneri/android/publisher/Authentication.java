package com.mauriciotogneri.android.publisher;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits.Commit;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits.Insert;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits.Listings;
import com.google.api.services.androidpublisher.model.AppEdit;
import com.google.api.services.androidpublisher.model.Listing;

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

        return new AndroidPublisher.Builder(httpTransport, jsonFactory, credential).setApplicationName(config.applicationName()).build();
    }

    public static void main(String[] args) throws Exception
    {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .setServiceAccountId("")
                .setServiceAccountScopes(Collections.singleton(ANDROIDPUBLISHER))
                .setServiceAccountPrivateKeyFromP12File(new File("service-account.p12"))
                .build();

        AndroidPublisher publisher = new AndroidPublisher.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName("Test Publish")
                .build();

        System.out.println(publisher);

        Edits edits = publisher.edits();

        String packageName = "com.mauriciotogneri.testpublish";
        String locale = "en-us";

        Insert editRequest = edits.insert(packageName, null);
        AppEdit edit = editRequest.execute();
        String editId = edit.getId();

        Listing newListing = new Listing();
        newListing.setTitle("Title v1");
        newListing.setShortDescription("Short description v1");
        newListing.setFullDescription("Full description v1");

        Listings.Update updateListingsRequest = edits
                .listings()
                .update(packageName,
                        editId,
                        locale,
                        newListing);

        updateListingsRequest.execute();

        System.out.println(String.format("Updated new '%s' app listing", locale));

        Commit commitRequest = edits.commit(packageName, editId);
        commitRequest.execute();

        System.out.println("Changes have been committed");
    }
}