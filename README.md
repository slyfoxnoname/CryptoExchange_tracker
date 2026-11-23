# Фінальний проєкт: «Біржа криптовалют»

Інформаційний Android-застосунок, який відображає рейтинг **топ-30 криптовалют у доларах США (USD)**, зберігає дані локально в Room для офлайн-перегляду та автоматично оновлює курси у фоні за допомогою WorkManager.

## Автор

- **Єгор Орлик** — повна реалізація проєкту (індивідуальна робота):
  - **Мережа та локальне сховище (`data`):** `Retrofit`, `CoinGeckoApi`, `AuthInterceptor`, DTO, `Room`, `CryptoDao` з транзакційною синхронізацією.
  - **Фонова синхронізація (`worker`, `di`):** `CoroutineWorker`, `CryptoWorkScheduler` та ручний контейнер залежностей `AppContainer`.
  - **Бізнес-логіка (`domain`):** доменні моделі, інтерфейс репозиторію та use cases.
  - **Інтерфейс (`presentation`):** `CryptoViewModel` (`StateFlow`), UI на `Jetpack Compose` + `Coil 3`, світла/темна тема, збереження стану при повороті екрана, пошук, деталі та «Обране».
  - **Тестування:** unit-тести репозиторію та форматувальників (`CryptoRepositoryImplTest`, `PriceFormatterTest`).
## Демонстраційне відео

* **Посилання на відео:** `[Додайте посилання на демонстраційне відео]`

## Архітектура проєкту

Проєкт побудовано за принципами **Clean Architecture** та **MVVM** із трьома відокремленими шарами та єдиним джерелом істини (Single Source of Truth — **Room**):

```text
WorkManager -> CryptoSyncWorker -> SyncCryptosUseCase -> CryptoRepositoryImpl -> CoinGeckoApi -> Room
Room -> Flow -> CryptoRepositoryImpl -> ObserveCryptosUseCase -> CryptoViewModel -> StateFlow -> Compose
```

* **`domain`** (`com.example.cryptoexchange.domain`) — чистий Kotlin без залежностей від Android UI, Room, Retrofit чи WorkManager: модель `CryptoCurrency`, `SyncResult`, інтерфейс `CryptoRepository` та сценарії `ObserveCryptosUseCase`, `ObserveLastSyncTimeUseCase`, `SyncCryptosUseCase`, `ToggleFavoriteUseCase`.
* **`data`** (`com.example.cryptoexchange.data`) — `CoinGeckoApi`, окремі DTO (`CoinGeckoMarketDto`), `AuthInterceptor`, Room Entity (`CryptoCurrencyEntity`), `CryptoDao` з `@Transaction` методом `replaceTopListAtomically()`, мапери та `CryptoRepositoryImpl`.
* **`worker` і `di`** (`com.example.cryptoexchange.worker`, `com.example.cryptoexchange.di`) — `CryptoSyncWorker` (`CoroutineWorker`), `CryptoWorkScheduler` та ручний контейнер залежностей `AppContainer`.
* **`presentation`** (`com.example.cryptoexchange.presentation`) — `CryptoViewModel`, `CryptoUiState`, форматувальники (`PriceFormatter`, `DateFormatter`) та Compose UI (`CryptoExchangeScreen`, `CryptoListItem`, `SyncStatusBanner`, `CryptoDetailSheet`).

## Запуск та налаштування API-ключа

1. Відкрийте папку `CryptoExchange` в **Android Studio**, дочекайтеся синхронізації Gradle та запустіть `app` (Android 8.0+ / API 26+).
2. **Працює одразу без ключа** через публічний [CoinGecko API](https://docs.coingecko.com/demo/reference/coins-markets).
3. *(Необов'язково)* Для стабільніших лімітів (30 запитів/хв) можна створити безкоштовний Demo API Key на [CoinGecko API](https://www.coingecko.com/en/api/pricing) і додати його в `local.properties` (файл ігнорується Git, заголовок `x-cg-demo-api-key` приховано в логах OkHttp):

```properties
COINGECKO_API_KEY=
```

## Використаний API-запит

* **Базова адреса:** `https://api.coingecko.com/api/v3/`
* **Ендпоінт:** `GET coins/markets`
* **Параметри запиту:**
  * `vs_currency=usd`
  * `order=market_cap_desc`
  * `per_page=30`
  * `page=1`
  * `sparkline=false`
* **Автентифікація:** необов'язковий HTTP-заголовок `x-cg-demo-api-key: <YOUR_DEMO_API_KEY>`
* **Зображення:** завантажуються через **Coil 3 (`3.6.3`)** за полем `image` із заглушкою під час завантаження та офлайн.

## Фонова синхронізація (WorkManager)

* **Періодичне оновлення:** інтервал **1 година**, унікальне ім'я `crypto_periodic_sync_work`, політика `ExistingPeriodicWorkPolicy.KEEP`, обмеження `NetworkType.CONNECTED`, експоненційний backoff (30 с). Реєструється один раз у `CryptoExchangeApp.onCreate()`.
* **Ручне оновлення:** кнопка «Оновити» у `TopAppBar` запускає унікальне одноразове завдання `crypto_manual_sync_work` із політикою `ExistingWorkPolicy.KEEP` та обмеженням `NetworkType.CONNECTED`.
* **Обробка помилок:** тимчасові збої мережі та HTTP 429 повторюються з backoff (до 3 спроб); помилки ключа (HTTP 401/403) завершуються негайно без нескінченних повторів.

## Реалізовані додаткові можливості (⭐)

* Пошук за назвою або тикером серед локально збережених валют (працює офлайн).
* Екран деталей валюти (`ModalBottomSheet`) з ринковою капіталізацією, обсягом за 24 год, зміною за 24 год та кількістю монет в обігу.
* «Обране» зі збереженням вибору користувача після транзакційної синхронізації.
* Unit-тести (`CryptoRepositoryImplTest`, `PriceFormatterTest`).
