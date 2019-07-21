package com.mauriciotogneri.androidpublisher;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.FileContent;
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
import com.google.api.services.androidpublisher.model.Apk;
import com.google.api.services.androidpublisher.model.AppEdit;
import com.google.api.services.androidpublisher.model.Bundle;
import com.google.api.services.androidpublisher.model.Track;
import com.google.api.services.androidpublisher.model.TrackRelease;
import com.mauriciotogneri.androidpublisher.Config.ArtifactType;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.google.api.services.androidpublisher.AndroidPublisherScopes.ANDROIDPUBLISHER;

public class Publisher
{
    public static void main(String[] args) throws Exception
    {
        try
        {
            Publisher publisher = new Publisher();
            Config config = publisher.config(args);

            publisher.publish(
                    config.packageName(),
                    config.serviceAccountEmail(),
                    config.serviceAccountP12(),
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
                                 "-email SERVICE_ACCOUNT_EMAIL " +
                                 "-p12 SERVICE_ACCOUNT_P12_FILE_PATH " +
                                 "-apk APK_FILE_PATH " +
                                 "-bundle BUNDLE_FILE_PATH " +
                                 "-track TRACK_NAME");

            System.exit(-1);

            return null;
        }
    }

    public void publishApk(String packageName,
                           String email,
                           String p12,
                           String path,
                           String trackName) throws Exception
    {
        publish(packageName,
                email,
                p12,
                ArtifactType.apk,
                path,
                trackName);
    }

    public void publishBundle(String packageName,
                              String email,
                              String p12,
                              String path,
                              String trackName) throws Exception
    {
        publish(packageName,
                email,
                p12,
                ArtifactType.bundle,
                path,
                trackName);
    }

    private void publish(String packageName,
                         String email,
                         String p12,
                         ArtifactType type,
                         String path,
                         String trackName) throws Exception
    {
        Logger.log("Authorizing using service account: %s", email);

        AndroidPublisher service = publisher(email, p12);
        Edits edits = service.edits();

        Insert editRequest = edits.insert(packageName, null);
        AppEdit edit = editRequest.execute();
        String editId = edit.getId();

        Logger.log("Uploading Artifact '%s'", path);

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

    private AndroidPublisher publisher(String email, String p12) throws Exception
    {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        Credential credential = new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .setServiceAccountId(email)
                .setServiceAccountScopes(Collections.singleton(ANDROIDPUBLISHER))
                .setServiceAccountPrivateKeyFromP12File(new File(p12))
                .build();

        return new AndroidPublisher.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName("AndroidPublisher")
                .build();
    }
}