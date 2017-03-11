# Android Publisher

The main class receives as a first parameter the path of the config file with has the following format:

```ini
application.name=???
package.name=???
project.path=???
client.id=???
client.secret=???
service.account.email=???
key.p12.path=???
listing.path=???
apk.path=???
track=(alpha, beta, production or rollout)
```

The second parameter can be:
* 1: Upload APK
* 2: Update listing

The listing json file must have the following format:

```json
[
    {
        "locale": "en_US",
        "title": "Test Publish",
        "shortDescription": "This is the short description.",
        "fullDescription": "This is the full description."
    },
    {
        "locale": "es_ES",
        "title": "Test Publish",
        "shortDescription": "Ésta es la descripción corta.",
        "fullDescription": "Ésta es la descripción larga."
    }
]
```