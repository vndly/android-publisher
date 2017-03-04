package com.mauriciotogneri.android.publisher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class GenerateApk
{
    public static void main(String[] args) throws Exception
    {
        if (args.length > 0)
        {
            Properties properties = new Properties();
            properties.load(new FileInputStream(args[0]));

            String projectPath = properties.getProperty("project.path");

            GenerateApk generateApk = new GenerateApk();
            generateApk.generateApk(new File(projectPath));
        }
        else
        {
            System.err.println("Usage: java -jar app.jar CONFIG_FILE_PATH");
        }
    }

    public void generateApk(File projectPath) throws Exception
    {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec("./gradlew assembleRelease", new String[0], projectPath);

        InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String line;

        while ((line = bufferedReader.readLine()) != null)
        {
            System.out.println(line);
        }

        process.waitFor();
    }
}