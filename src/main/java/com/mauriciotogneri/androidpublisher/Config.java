package com.mauriciotogneri.androidpublisher;

import java.util.HashMap;
import java.util.Map;

class Config
{
    private final String packageName;
    private final String serviceAccountEmail;
    private final String serviceAccountP12;
    private final String apkPath;
    private final String bundlePath;
    private final String trackName;

    public enum ArtifactType
    {
        apk,
        bundle
    }

    Config(String[] args)
    {
        Map<String, String> parameters = parameters(args);

        this.packageName = parameters.get("package");
        this.serviceAccountEmail = parameters.get("email");
        this.serviceAccountP12 = parameters.get("p12");
        this.apkPath = parameters.get("apk");
        this.bundlePath = parameters.get("bundle");
        this.trackName = parameters.get("track");

        if ((packageName == null)
                || (serviceAccountEmail == null)
                || (serviceAccountP12 == null)
                || (apkPath == null)
                || (trackName == null))
        {
            throw new RuntimeException();
        }
    }

    private Map<String, String> parameters(String[] args)
    {
        Map<String, String> result = new HashMap<>();

        String flag = null;

        for (String arg : args)
        {
            if ((flag == null) && (arg.startsWith("-")))
            {
                flag = arg.substring(1);
            }
            else if (flag != null)
            {
                result.put(flag, arg);
                flag = null;
            }
        }

        return result;
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

    ArtifactType type()
    {
        return ((apkPath != null) && (!apkPath.isEmpty())) ? ArtifactType.apk : ArtifactType.bundle;
    }

    String path()
    {
        return (type() == ArtifactType.apk) ? apkPath : bundlePath;
    }

    String trackName()
    {
        return trackName;
    }
}