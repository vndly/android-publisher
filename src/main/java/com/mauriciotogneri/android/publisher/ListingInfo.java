package com.mauriciotogneri.android.publisher;

public class ListingInfo
{
    private final String locale;
    private final String title;
    private final String shortDescription;
    private final String fullDescription;

    public ListingInfo(String locale, String title, String shortDescription, String fullDescription)
    {
        this.locale = locale;
        this.title = title;
        this.shortDescription = shortDescription;
        this.fullDescription = fullDescription;
    }

    public String locale()
    {
        return locale;
    }

    public String title()
    {
        return title;
    }

    public String shortDescription()
    {
        return shortDescription;
    }

    public String fullDescription()
    {
        return fullDescription;
    }
}