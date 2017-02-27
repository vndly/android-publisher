# Android Publisher

https://github.com/googlesamples/android-play-publisher-api/tree/master/v2/java

Add a file called `client_secrets.json` in `src/main/resources` with the following content:

```json
{
    "installed": {
        "client_id": "xxx",
        "client_secret": "xxx",
        "redirect_uris": [],
        "auth_uri": "https://accounts.google.com/o/oauth2/auth",
        "token_uri": "https://accounts.google.com/o/oauth2/token"
    }
}
```