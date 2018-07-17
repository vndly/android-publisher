package com.mauriciotogneri.android.publisher;

class Logger
{
    static void log(String text, Object... parameters)
    {
        System.out.println(String.format(text, parameters));
    }

    static void error(String text, Object... parameters)
    {
        System.err.println(String.format(text, parameters));
    }
}