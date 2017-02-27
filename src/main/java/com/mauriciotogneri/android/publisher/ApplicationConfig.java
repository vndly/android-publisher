package com.mauriciotogneri.android.publisher;

/**
 * Contains global application configuration, which is required by all samples.
 */
public final class ApplicationConfig
{
    private ApplicationConfig()
    {
        // no instance
    }

    /**
     * Specify the name of your application. If the application name is
     * {@code null} or blank, the application will log a warning. Suggested
     * format is "MyCompany-Application/1.0".
     */
    static final String APPLICATION_NAME = "";

    /**
     * Specify the package name of the app.
     */
    static final String PACKAGE_NAME = "";

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
    static final String SERVICE_ACCOUNT_EMAIL = "";

    /**
     * Specify the apk file path of the apk to upload, i.e. /resources/your_apk.apk
     * <p>
     * This needs to be set for running {@link BasicUploadApk} and {@link UploadApkWithListing}
     * samples.
     * </p>
     */
    public static final String APK_FILE_PATH = "";
}