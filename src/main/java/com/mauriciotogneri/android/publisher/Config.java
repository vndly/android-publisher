package com.mauriciotogneri.android.publisher;

import java.io.FileInputStream;
import java.util.Properties;

public class Config
{
    private final String applicationName;
    private final String packageName;
    private final String projectPath;
    private final String clientId;
    private final String clientSecret;
    private final String serviceAccountEmail;
    private final String keyP12Path;
    private final String apkPath;
    private final String track;

    public Config(String configPath) throws Exception
    {
        Properties properties = new Properties();
        properties.load(new FileInputStream(configPath));

        this.applicationName = properties.getProperty("application.name");
        this.packageName = properties.getProperty("package.name");
        this.projectPath = properties.getProperty("project.path");
        this.clientId = properties.getProperty("client.id");
        this.clientSecret = properties.getProperty("client.secret");
        this.serviceAccountEmail = properties.getProperty("service.account.email");
        this.keyP12Path = properties.getProperty("key.p12.path");
        this.apkPath = properties.getProperty("apk.path");
        this.track = properties.getProperty("track");
    }

    public String applicationName()
    {
        return applicationName;
    }

    public String packageName()
    {
        return packageName;
    }

    public String projectPath()
    {
        return projectPath;
    }

    public String clientId()
    {
        return clientId;
    }

    public String clientSecret()
    {
        return clientSecret;
    }

    public String serviceAccountEmail()
    {
        return serviceAccountEmail;
    }

    public String keyP12Path()
    {
        return keyP12Path;
    }

    public String apkPath()
    {
        return apkPath;
    }

    public String track()
    {
        return track;
    }
}