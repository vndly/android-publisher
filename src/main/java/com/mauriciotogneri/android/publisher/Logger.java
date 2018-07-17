package com.mauriciotogneri.android.publisher;

public class Logger
{
    public static void log(String text, Object... parameters)
    {
        System.out.println(String.format(text, parameters));
    }
}