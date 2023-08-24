# Ble2

## dev

Для работы с проектом требуется Android Studio версии [Giraffe (2022.3.1)][1]. Можно использовать встроенный JDK, либо установить [Eclipse Temurin 17][2].

Все зависимости будут скачаны при первой сборке проекта.

Структура проекта:
- система сборки - стандартый gradle
- [подписывание release-сборки][12] (в корень нужно добавить файл keystore_release.properties, который содержит данные keystore)
- используемый паттерн - MVVM
- интерфейс - [Jetpack Compose][3]
- l10n - [стандартная Android][4] (ресурсные файлы strings.xml), [пример1][7] и [пример2][13]
- тема - стандартная [Material Design 3][6] из Compose

[1]: https://developer.android.com/studio
[2]: https://adoptium.net/
[3]: https://developer.android.com/jetpack/compose
[4]: https://developer.android.com/guide/topics/resources/localization
[6]: https://developer.android.com/jetpack/compose/themes
[7]: https://medium.com/i18n-and-l10n-resources-for-developers/a-deep-dive-into-internationalizing-jetpack-compose-android-apps-e4ed3dc2809c
[10]: https://github.com/JuulLabs/kable
[11]: https://developer.android.com/jetpack/compose/navigation
[12]: https://developer.android.com/studio/publish/app-signing#secure_key
[13]: https://proandroiddev.com/the-ultimate-guide-to-android-app-internationalization-and-localization-89b6c33fe741
