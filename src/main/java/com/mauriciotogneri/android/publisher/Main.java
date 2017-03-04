package com.mauriciotogneri.android.publisher;

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.FileContent;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits.Apks.Upload;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits.Commit;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits.Insert;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits.Tracks.Update;
import com.google.api.services.androidpublisher.model.Apk;
import com.google.api.services.androidpublisher.model.AppEdit;
import com.google.api.services.androidpublisher.model.Track;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        if (args.length > 0)
        {
            Main main = new Main();
            main.start(new Config(args[0]));
        }
        else
        {
            System.err.println("Usage: java -jar android-publisher.jar CONFIG_FILE_PATH");
        }
    }

    private void start(Config config) throws Exception
    {
        switch (option())
        {
            case '1':
                basicUpload(config);
                break;

            case '2':
                break;

            case '3':
                break;
        }
    }

    private char option() throws Exception
    {
        System.out.println("Choose an option:");
        System.out.println("1) Upload APK");
        System.out.println("2) XXX");
        System.out.println("3) YYY\n");

        System.out.print("Option: ");

        char option = (char) System.in.read();

        System.out.println();

        return option;
    }

    private void generateApk(String projectPath) throws Exception
    {
        System.out.println("Building APK...");

        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec("./gradlew assembleRelease", new String[0], new File(projectPath));

        InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String line;

        while ((line = bufferedReader.readLine()) != null)
        {
            System.out.println(line);
        }

        process.waitFor();
    }

    private void basicUpload(Config config) throws Exception
    {
        generateApk(config.projectPath());

        AndroidPublisher service = AndroidPublisherHelper.init(config);
        Edits edits = service.edits();

        Insert editRequest = edits.insert(config.packageName(), null);
        AppEdit edit = editRequest.execute();
        String editId = edit.getId();

        System.out.println("\nUploading APK...");

        AbstractInputStreamContent apkFile = new FileContent(AndroidPublisherHelper.MIME_TYPE_APK, new File(config.apkPath()));
        Upload uploadRequest = edits.apks().upload(config.packageName(), editId, apkFile);
        Apk apk = uploadRequest.execute();

        System.out.println(String.format("Version code %d has been uploaded", apk.getVersionCode()));

        List<Integer> apkVersionCodes = new ArrayList<>();
        apkVersionCodes.add(apk.getVersionCode());
        Update updateTrackRequest = edits
                .tracks()
                .update(config.packageName(),
                        editId,
                        config.track(),
                        new Track().setVersionCodes(apkVersionCodes));

        Track updatedTrack = updateTrackRequest.execute();

        System.out.println(String.format("Track '%s' has been updated", updatedTrack.getTrack()));

        Commit commitRequest = edits.commit(config.packageName(), editId);
        commitRequest.execute();

        System.out.println("Changes have been committed");
    }
}