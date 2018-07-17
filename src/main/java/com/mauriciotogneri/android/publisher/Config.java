package com.mauriciotogneri.android.publisher;

import java.io.FileInputStream;
import java.util.Properties;

class Config
{
    private final String packageName;
    private final String serviceAccountEmail;
    private final String serviceAccountP12;
    private final String apkPath;
    private final String track;

    Config(String configPath) throws Exception
    {
        Properties properties = new Properties();
        properties.load(new FileInputStream(configPath));

        this.packageName = properties.getProperty("package.name");
        this.serviceAccountEmail = properties.getProperty("service.account.email");
        this.serviceAccountP12 = properties.getProperty("service.account.p12");
        this.apkPath = properties.getProperty("apk");
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

    String serviceAccountP12()
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
}