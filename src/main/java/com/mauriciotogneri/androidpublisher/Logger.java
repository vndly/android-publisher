package com.mauriciotogneri.androidpublisher;

class Logger
{
    static void log(String text, Object... parameters)
    {
        System.out.printf(text + "%n", parameters);
    }

    static void error(String text, Object... parameters)
    {
        System.err.printf(text + "%n", parameters);
    }
}