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
import com.google.api.services.androidpublisher.AndroidPublisher.Edits.Tracks;
import com.google.api.services.androidpublisher.model.Apk;
import com.google.api.services.androidpublisher.model.AppEdit;
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
        Main main = new Main();
        main.publish(main.config(args));
    }

    private Config config(String[] args)
    {
        try
        {
            return new Config(args);
        }
        catch (Exception e)
        {
            Logger.error("Usage: java -jar android-publisher.jar " +
                    "-package PACKAGE_NAME " +
                    "-email EMAIL " +
                    "-p12 P12_FILE_PATH " +
                    "-apk APK_FILE_PATH " +
                    "-track TRACK_NAME");

            System.exit(0);

            return null;
        }
    }

    private void publish(Config config) throws Exception
    {
        AndroidPublisher service = publisher(config);
        Edits edits = service.edits();

        Insert editRequest = edits.insert(config.packageName(), null);
        AppEdit edit = editRequest.execute();
        String editId = edit.getId();

        Logger.log("Uploading APK...");

        AbstractInputStreamContent apkFile = new FileContent("application/vnd.android.package-archive", new File(config.apk()));
        Upload uploadRequest = edits.apks().upload(config.packageName(), editId, apkFile);
        Apk apk = uploadRequest.execute();

        Logger.log("APK with version code '%d' has been uploaded", apk.getVersionCode());

        List<Integer> apkVersionCodes = new ArrayList<>();
        apkVersionCodes.add(apk.getVersionCode());
        Tracks.Update updateTrackRequest = edits
                .tracks()
                .update(config.packageName(),
                        editId,
                        config.track(),
                        new Track().setVersionCodes(apkVersionCodes));

        Track updatedTrack = updateTrackRequest.execute();

        Logger.log("APK added to track '%s'", updatedTrack.getTrack());

        Commit commitRequest = edits.commit(config.packageName(), editId);
        commitRequest.execute();

        Logger.log("Changes have been committed");
    }

    private AndroidPublisher publisher(Config config) throws Exception
    {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        Logger.log("Authorizing using service account");

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