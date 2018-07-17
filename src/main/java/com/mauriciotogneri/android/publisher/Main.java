package com.mauriciotogneri.android.publisher;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits.Apks.Upload;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits.Commit;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits.Insert;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits.Listings;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits.Tracks;
import com.google.api.services.androidpublisher.model.Apk;
import com.google.api.services.androidpublisher.model.AppEdit;
import com.google.api.services.androidpublisher.model.Listing;
import com.google.api.services.androidpublisher.model.Track;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.api.services.androidpublisher.AndroidPublisherScopes.ANDROIDPUBLISHER;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        if (args.length > 0)
        {
            Main main = new Main();
            main.start(new Config(args[0]), parameterOperation(args));
        }
        else
        {
            Logger.error("Usage: java -jar android-publisher.jar CONFIG_FILE_PATH");
        }
    }

    private static String parameterOperation(String[] args)
    {
        return (args.length > 1) ? args[1] : "";
    }

    private void start(Config config, String operation) throws Exception
    {
        switch (operation)
        {
            case "1":
                uploadApk(config);
                break;

            case "2":
                updateListing(config);
                break;
        }
    }

    private void uploadApk(Config config) throws Exception
    {
        AndroidPublisher service = publisher(config);
        Edits edits = service.edits();

        Insert editRequest = edits.insert(config.packageName(), null);
        AppEdit edit = editRequest.execute();
        String editId = edit.getId();

        Logger.log("\nUploading APK...");

        AbstractInputStreamContent apkFile = new FileContent("application/vnd.android.package-archive", new File(config.apkPath()));
        Upload uploadRequest = edits.apks().upload(config.packageName(), editId, apkFile);
        Apk apk = uploadRequest.execute();

        Logger.log("Version code %d has been uploaded", apk.getVersionCode());

        List<Integer> apkVersionCodes = new ArrayList<>();
        apkVersionCodes.add(apk.getVersionCode());
        Tracks.Update updateTrackRequest = edits
                .tracks()
                .update(config.packageName(),
                        editId,
                        config.track(),
                        new Track().setVersionCodes(apkVersionCodes));

        Track updatedTrack = updateTrackRequest.execute();

        Logger.log("Track '%s' has been updated", updatedTrack.getTrack());

        /*ApkListing newApkListing = new ApkListing();
        newApkListing.setRecentChanges("...");

        Apklistings.Update updateRecentChangesRequest = edits
                .apklistings()
                .update(config.packageName(),
                        editId,
                        apk.getVersionCode(),
                        Locale.US.toString(),
                        newApkListing);
        updateRecentChangesRequest.execute();*/

        Commit commitRequest = edits.commit(config.packageName(), editId);
        commitRequest.execute();

        Logger.log("Changes have been committed");
    }

    private void updateListing(Config config) throws Exception
    {
        AndroidPublisher service = publisher(config);
        Edits edits = service.edits();

        Insert editRequest = edits.insert(config.packageName(), null);
        AppEdit edit = editRequest.execute();
        String editId = edit.getId();

        for (ListingInfo listingInfo : config.listing())
        {
            updateListing(edits, editId, config.packageName(), listingInfo);
        }

        Commit commitRequest = edits.commit(config.packageName(), editId);
        commitRequest.execute();

        Logger.log("Changes have been committed");
    }

    private void updateListing(Edits edits, String editId, String packageName, ListingInfo listing) throws Exception
    {
        Listing newListing = new Listing();
        newListing.setTitle(listing.title());
        newListing.setShortDescription(listing.shortDescription());
        newListing.setFullDescription(listing.fullDescription());

        Listings.Update updateListingsRequest = edits
                .listings()
                .update(packageName,
                        editId,
                        listing.locale(),
                        newListing);

        updateListingsRequest.execute();

        Logger.log("Updated new '%s' app listing", listing.locale());
    }

    private AndroidPublisher publisher(Config config) throws Exception
    {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        Logger.log("Authorizing using Service Account: %s", config.serviceAccountEmail());

        Credential credential = new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .setServiceAccountId(config.serviceAccountEmail())
                .setServiceAccountScopes(Collections.singleton(ANDROIDPUBLISHER))
                .setServiceAccountPrivateKeyFromP12File(new File(config.serviceAccountP12()))
                .build();

        return new AndroidPublisher.Builder(httpTransport, jsonFactory, credential).setApplicationName("AndroidPublisher").build();
    }
}