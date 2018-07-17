package com.mauriciotogneri.android.publisher;

import com.google.gson.Gson;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

class Config
{
    private final String applicationName;
    private final String packageName;
    private final String projectPath;
    private final String clientId;
    private final String clientSecret;
    private final String serviceAccountEmail;
    private final String serviceAccountP12;
    private final String listingPath;
    private final String apkPath;
    private final String track;

    Config(String configPath) throws Exception
    {
        Properties properties = new Properties();
        properties.load(new FileInputStream(configPath));

        this.applicationName = properties.getProperty("application.name");
        this.packageName = properties.getProperty("package.name");
        this.projectPath = properties.getProperty("project.path");
        this.clientId = properties.getProperty("client.id");
        this.clientSecret = properties.getProperty("client.secret");
        this.serviceAccountEmail = properties.getProperty("service.account.email");
        this.serviceAccountP12 = properties.getProperty("service.account.p12.path");
        this.listingPath = properties.getProperty("listing.path");
        this.apkPath = properties.getProperty("apk.path");
        this.track = properties.getProperty("track");
    }

    String applicationName()
    {
        return applicationName;
    }

    String packageName()
    {
        return packageName;
    }

    String projectPath()
    {
        return projectPath;
    }

    boolean hasInstalledApplication()
    {
        return (clientId != null) && (!clientId.isEmpty()) && (clientSecret != null) && (!clientSecret.isEmpty());
    }

    String clientId()
    {
        return clientId;
    }

    String clientSecret()
    {
        return clientSecret;
    }

    boolean hasServiceAccount()
    {
        return (serviceAccountEmail != null) && (!serviceAccountEmail.isEmpty()) && (serviceAccountP12 != null) && (!serviceAccountP12.isEmpty());
    }

    String serviceAccountEmail()
    {
        return serviceAccountEmail;
    }

    String keyP12Path()
    {
        return serviceAccountP12;
    }

    String apkPath()
    {
        return apkPath;
    }

    String track()
    {
        return track;
    }

    ListingInfo[] listing() throws Exception
    {
        String json = new String(Files.readAllBytes(Paths.get(listingPath)), "UTF-8");
        Gson gson = new Gson();

        return gson.fromJson(json, ListingInfo[].class);
    }
}