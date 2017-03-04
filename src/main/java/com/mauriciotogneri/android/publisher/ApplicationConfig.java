package com.mauriciotogneri.android.publisher;

public final class ApplicationConfig
{
    private ApplicationConfig()
    {
    }

    /**
     * Specify the name of your application. If the application name is
     * {@code null} or blank, the application will log a warning. Suggested
     * format is "MyCompany-Application/1.0".
     */
    static final String APPLICATION_NAME = "AndroidPublisher/1.0.0";

    /**
     * Specify the package name of the app.
     */
    static final String PACKAGE_NAME = "com.mauriciotogneri.testpublish";

    /**
     * Authentication.
     * <p>
     * Installed application: Leave this string empty and copy or
     * edit resources/client_secrets.json.
     * </p>
     * <p>
     * Service accounts: Enter the service
     * account email and add your key.p12 file to the resources directory.
     * </p>
     */
    static final String SERVICE_ACCOUNT_EMAIL = "publisher@android-pusblisher.iam.gserviceaccount.com";

    /**
     * Specify the apk file path of the apk to upload, i.e. /resources/your_apk.apk
     * <p>
     * This needs to be set for running {@link BasicUploadApk} and {@link UploadApkWithListing}
     * samples.
     * </p>
     */
    public static final String APK_FILE_PATH = "resources/app-release.apk";
}