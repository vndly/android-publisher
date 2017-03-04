package com.mauriciotogneri.android.publisher;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits.Insert;
import com.google.api.services.androidpublisher.model.Apk;
import com.google.api.services.androidpublisher.model.ApksListResponse;
import com.google.api.services.androidpublisher.model.AppEdit;

public class ListApks
{
    public static void main(String[] args)
    {
        try
        {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(ApplicationConfig.PACKAGE_NAME), "ApplicationConfig.PACKAGE_NAME cannot be null or empty!");

            // Create the API service.
            final AndroidPublisher service = AndroidPublisherHelper.init(
                    ApplicationConfig.APPLICATION_NAME, ApplicationConfig.SERVICE_ACCOUNT_EMAIL);
            final Edits edits = service.edits();

            // Create a new edit to make changes.
            Insert editRequest = edits
                    .insert(ApplicationConfig.PACKAGE_NAME,
                            null);
            AppEdit appEdit = editRequest.execute();

            // Get a list of apks.
            ApksListResponse apksResponse = edits
                    .apks()
                    .list(ApplicationConfig.PACKAGE_NAME,
                          appEdit.getId()).execute();

            // Print the apk info.
            for (Apk apk : apksResponse.getApks())
            {
                System.out.println(
                        String.format("Version: %d - Binary sha1: %s", apk.getVersionCode(),
                                      apk.getBinary().getSha1()));
            }
        }
        catch (Exception e)
        {
            System.err.println("Exception was thrown while updating listing: " + e.getMessage());
        }
    }
}