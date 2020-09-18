package com.mauriciotogneri.androidpublisher;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits.Apks;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits.Bundles;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits.Commit;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits.Insert;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits.Tracks;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.api.services.androidpublisher.model.Apk;
import com.google.api.services.androidpublisher.model.AppEdit;
import com.google.api.services.androidpublisher.model.Bundle;
import com.google.api.services.androidpublisher.model.Track;
import com.google.api.services.androidpublisher.model.TrackRelease;
import com.mauriciotogneri.androidpublisher.Config.ArtifactType;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Publisher
{
    private static final int TIMEOUT = 600000; // 10 minutes

    public static void main(String[] args) throws Exception
    {
        try
        {
            Publisher publisher = new Publisher();
            Config config = publisher.config(args);

            publisher.publish(
                    config.packageName(),
                    config.serviceAccount(),
                    config.type(),
                    config.path(),
                    config.trackName()
            );
        }
        catch (GoogleJsonResponseException e)
        {
            Logger.log((char) 27 + "[31mERROR: " + e.getDetails().getMessage() + (char) 27 + "[0m");

            System.exit(-1);
        }
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
                                 "-serviceAccount SERVICE_ACCOUNT_FILE_PATH " +
                                 "-apk APK_FILE_PATH " +
                                 "-bundle BUNDLE_FILE_PATH " +
                                 "-track TRACK_NAME");

            System.exit(-1);

            return null;
        }
    }

    public void publishApk(String packageName,
                           String serviceAccount,
                           String path,
                           String trackName) throws Exception
    {
        publish(packageName,
                serviceAccount,
                ArtifactType.apk,
                path,
                trackName);
    }

    public void publishBundle(String packageName,
                              String serviceAccount,
                              String path,
                              String trackName) throws Exception
    {
        publish(packageName,
                serviceAccount,
                ArtifactType.bundle,
                path,
                trackName);
    }

    private void publish(String packageName,
                         String serviceAccount,
                         ArtifactType type,
                         String path,
                         String trackName) throws Exception
    {
        Logger.log("Authorizing using service account: %s", serviceAccount);

        AndroidPublisher service = publisher(packageName, serviceAccount);
        Edits edits = service.edits();

        Insert editRequest = edits.insert(packageName, null);
        AppEdit edit = editRequest.execute();
        String editId = edit.getId();

        Logger.log("Uploading artifact '%s'", path);

        TrackRelease trackRelease = new TrackRelease();
        trackRelease.setStatus("completed");

        AbstractInputStreamContent file = new FileContent("application/octet-stream", new File(path));

        if (type == ArtifactType.apk)
        {
            Apks.Upload uploadRequest = edits.apks().upload(packageName, editId, file);
            Apk apk = uploadRequest.execute();

            Logger.log("APK with version code '%d' has been uploaded", apk.getVersionCode());
            trackRelease.setVersionCodes(Arrays.asList(apk.getVersionCode().longValue()));
        }
        else if (type == ArtifactType.bundle)
        {
            Bundles.Upload uploadRequest = edits.bundles().upload(packageName, editId, file);
            Bundle bundle = uploadRequest.execute();

            Logger.log("Bundle with version code '%d' has been uploaded", bundle.getVersionCode());
            trackRelease.setVersionCodes(Arrays.asList(bundle.getVersionCode().longValue()));
        }
        else
        {
            throw new RuntimeException();
        }

        List<TrackRelease> releases = new ArrayList<>();
        releases.add(trackRelease);

        Track track = new Track();
        track.setReleases(releases);

        Tracks.Update updateTrackRequest = edits
                .tracks()
                .update(packageName,
                        editId,
                        trackName,
                        track);

        Track updatedTrack = updateTrackRequest.execute();

        Logger.log("Artifact added to track '%s'", updatedTrack.getTrack());

        Commit commitRequest = edits.commit(packageName, editId);
        commitRequest.execute();

        Logger.log("Changes have been committed");
    }

    private AndroidPublisher publisher(String packageName, String serviceAccount) throws Exception
    {
        InputStream serviceAccountStream = new FileInputStream(new File(serviceAccount));
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        GoogleCredential credential = GoogleCredential
                .fromStream(serviceAccountStream, httpTransport, jsonFactory)
                .createScoped(Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER));

        HttpRequestFactory requestFactory = httpTransport.createRequestFactory(
                request -> {
                    credential.initialize(request);
                    request.setConnectTimeout(TIMEOUT);
                    request.setReadTimeout(TIMEOUT);
                });

        return new AndroidPublisher.Builder(credential.getTransport(),
                                            credential.getJsonFactory(),
                                            requestFactory.getInitializer())
                .setApplicationName(packageName)
                .build();
    }
}