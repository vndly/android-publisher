[![License](https://img.shields.io/badge/license-MIT-green.svg)](https://github.com/mauriciotogneri/android-publisher/blob/master/LICENSE.md)

# Android Publisher
Java tool to automate the publishing of Android applications.

## Installation

### Maven

Add the following code to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

And the dependency:

```xml
<dependency>
    <groupId>com.github.mauriciotogneri</groupId>
    <artifactId>android-publisher</artifactId>
    <version>1.4.0</version>
</dependency>
```

### Gradle

Add the following code to your root `build.gradle`:

```groovy
allprojects
{
    repositories
    {
        maven
        {
            url 'https://jitpack.io'
        }
    }
}
```

Add the following code to your module `build.gradle`:
```groovy
implementation 'com.github.mauriciotogneri:android-publisher:1.4.0'
```

## Usage

From console:
```bash
java -jar android-publisher.jar -package <PACKAGE_NAME> -serviceAccount <SERVICE_ACCOUNT_FILE_PATH> -apk <APK_FILE_PATH> -bundle <BUNDLE_FILE_PATH> -track <TRACK_NAME>
```

From Java:
```
Publisher publisher = new Publisher();

publisher.publishApk(
        "PACKAGE_NAME",
        "SERVICE_ACCOUNT_FILE_PATH",
        "APK_FILE_PATH",
        "TRACK_NAME"
);

publisher.publishBundle(
        "PACKAGE_NAME",
        "SERVICE_ACCOUNT_FILE_PATH",
        "BUNDLE_FILE_PATH",
        "TRACK_NAME"
);
```