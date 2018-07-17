package com.mauriciotogneri.android.publisher;

import com.google.gson.Gson;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

class Config
{
    private final String packageName;
    private final String serviceAccountEmail;
    private final String serviceAccountP12;
    private final String listingPath;
    private final String apkPath;
    private final String track;

    Config(String configPath) throws Exception
    {
        Properties properties = new Properties();
        properties.load(new FileInputStream(configPath));

        this.packageName = properties.getProperty("package.name");
        this.serviceAccountEmail = properties.getProperty("service.account.email");
        this.serviceAccountP12 = properties.getProperty("service.account.p12.path");
        this.listingPath = properties.getProperty("listing.path");
        this.apkPath = properties.getProperty("apk.path");
        this.track = properties.getProperty("track");
    }

    String packageName()
    {
        return packageName;
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