package com.mauriciotogneri.android.publisher;

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.FileContent;
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
import java.util.List;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        if (args.length > 0)
        {
            Main main = new Main();
            main.start(new Config(args[0]), parameterOption(args));
        }
        else
        {
            System.err.println("Usage: java -jar android-publisher.jar CONFIG_FILE_PATH");
        }
    }

    private static String parameterOption(String[] args)
    {
        return (args.length > 1) ? args[1] : null;
    }

    private void start(Config config, String defaultOption) throws Exception
    {
        switch (option(defaultOption))
        {
            case "1":
                uploadApk(config);
                break;

            case "2":
                updateListing(config);
                break;
        }
    }

    private String option(String defaultOption) throws Exception
    {
        if ((defaultOption == null) || (defaultOption.isEmpty()))
        {
            System.out.println("Choose an option:");
            System.out.println("1) Upload APK");
            System.out.println("2) Update Listing");

            System.out.print("Option: ");

            char option = (char) System.in.read();

            System.out.println();

            return String.valueOf(option);
        }
        else
        {
            return defaultOption;
        }
    }

    private void uploadApk(Config config) throws Exception
    {
        Authentication authentication = new Authentication();
        AndroidPublisher service = authentication.publisher(config);
        Edits edits = service.edits();

        Insert editRequest = edits.insert(config.packageName(), null);
        AppEdit edit = editRequest.execute();
        String editId = edit.getId();

        System.out.println("\nUploading APK...");

        AbstractInputStreamContent apkFile = new FileContent("application/vnd.android.package-archive", new File(config.apkPath()));
        Upload uploadRequest = edits.apks().upload(config.packageName(), editId, apkFile);
        Apk apk = uploadRequest.execute();

        System.out.println(String.format("Version code %d has been uploaded", apk.getVersionCode()));

        List<Integer> apkVersionCodes = new ArrayList<>();
        apkVersionCodes.add(apk.getVersionCode());
        Tracks.Update updateTrackRequest = edits
                .tracks()
                .update(config.packageName(),
                        editId,
                        config.track(),
                        new Track().setVersionCodes(apkVersionCodes));

        Track updatedTrack = updateTrackRequest.execute();

        System.out.println(String.format("Track '%s' has been updated", updatedTrack.getTrack()));

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

        System.out.println("Changes have been committed");
    }

    private void updateListing(Config config) throws Exception
    {
        Authentication authentication = new Authentication();
        AndroidPublisher service = authentication.publisher(config);
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

        System.out.println("Changes have been committed");
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

        System.out.println(String.format("Updated new '%s' app listing", listing.locale()));
    }
}