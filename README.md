<h1 align="center"> currency-exchange </h1>

<p align="center">
  <img src="docs/screenshots/welcome.png" alt="welcome" width="320">
</p>

Веб-приложение для работы с валютами и обменными курсами.
Приложение предоставляет REST API и браузерный интерфейс, доступный по корневому адресу приложения.

Приложение позволяет:

* просматривать список валют;
* добавлять новые валюты;
* просматривать список обменных курсов;
* добавлять и редактировать обменные курсы;
* рассчитывать конвертацию произвольной суммы из одной валюты в другую.

После запуска браузерный интерфейс доступен по корневому адресу приложения:

```text 
http://localhost:8080/
```

*Проект реализован на Java 21 с использованием MVC-архитектуры, Java Servlets, Apache Tomcat, JDBC, SQLite и HikariCP.*

---

## Содержание

* Конкретнее о возможностях API
* Быстрый старт
* Детали реализации
* Контакты

---

<details>
  <summary>Конкретнее о возможностях API</summary>

**Все ответы API возвращаются в формате JSON.**

Ошибки возвращаются в едином формате:

```json
{
  "message": "error message"
}
```

Для всех endpoint-ов возможен `500 Internal Server Error` при внутренней ошибке сервера или БД.

## Валюты

### `GET /currencies`

Получение списка всех валют.

**Успешный ответ:**
`200 OK` — возвращает список валют.

---

### `GET /currency/{code}`

Получение валюты по коду.

Пример:

```http
GET /currency/USD
```

Код валюты передаётся в URL path.

**Успешный ответ:**
`200 OK` — валюта найдена.

**Возможные проблемы:**

* `400 Bad Request` — код валюты не передан;
* `404 Not Found` — валюта не найдена.

---

### `POST /currencies`

Добавление новой валюты.

Параметры передаются в теле запроса в формате `x-www-form-urlencoded`.

Поля:

```text
name — полное название валюты
code — код валюты, строго 3 символа
sign — символ валюты, максимум 3 символа
```

Пример:

```http
POST /currencies

name=US Dollar&code=USD&sign=$
```

**Успешный ответ:**
`201 Created` — валюта создана.

**Возможные проблемы:**

* `400 Bad Request` — отсутствуют обязательные поля или данные некорректны;
* `409 Conflict` — валюта с таким кодом уже существует.

---

## Обменные курсы

### `GET /exchangeRates`

Получение списка всех обменных курсов.

**Успешный ответ:**
`200 OK` — возвращает список курсов.

---

### `GET /exchangeRate/{baseCode}{targetCode}`

Получение обменного курса по валютной паре.

Пример:

```http
GET /exchangeRate/USDRUB
```

Валютная пара передаётся в URL path одной строкой из 6 символов: `USDRUB`.

**Успешный ответ:**
`200 OK` — курс найден.

**Возможные проблемы:**

* `400 Bad Request` — код валютной пары не передан или имеет неверную длину;
* `404 Not Found` — валютная пара не найдена.

---

### `POST /exchangeRates`

Добавление нового обменного курса.

Параметры передаются в теле запроса в формате `x-www-form-urlencoded`.

Поля:

```text
baseCurrencyCode — код базовой валюты
targetCurrencyCode — код целевой валюты
rate — положительный курс обмена
```

Пример:

```http
POST /exchangeRates

baseCurrencyCode=USD&targetCurrencyCode=RUB&rate=90.5
```

**Успешный ответ:**
`201 Created` — обменный курс создан.

**Возможные проблемы:**

* `400 Bad Request` — отсутствуют обязательные поля, курс некорректен или валюты совпадают;
* `404 Not Found` — одна из валют не найдена;
* `409 Conflict` — такая валютная пара уже существует.

---

### `PATCH /exchangeRate/{baseCode}{targetCode}`

Обновление существующего обменного курса.

Курс передаётся в теле запроса в формате `x-www-form-urlencoded`.

Пример:

```http
PATCH /exchangeRate/USDRUB

rate=95.25
```

**Успешный ответ:**
`200 OK` — курс обновлён.

**Возможные проблемы:**

* `400 Bad Request` — код валютной пары некорректен, тело запроса пустое или `rate` некорректен;
* `404 Not Found` — валютная пара не найдена.

---

## Конвертация валют

### `GET /exchange?from={baseCode}&to={targetCode}&amount={amount}`

Расчёт конвертации суммы из одной валюты в другую.

Параметры передаются в query string:

```text
from — код исходной валюты
to — код целевой валюты
amount — положительная сумма для конвертации
```

Пример:

```http
GET /exchange?from=USD&to=RUB&amount=10
```

**Успешный ответ:**
`200 OK` — возвращает исходную валюту, целевую валюту, найденный курс, исходную сумму и сконвертированную сумму.

Поддерживаемые сценарии расчёта:

```text
1. Прямой курс: FROM -> TO
2. Обратный курс: TO -> FROM
3. Кросс-курс через USD: USD -> FROM и USD -> TO
```

**Возможные проблемы:**

* `400 Bad Request` — отсутствуют обязательные параметры или `amount` некорректен;
* `404 Not Found` — невозможно найти прямой, обратный или USD-кросс курс.

</details>

---

<details>
  <summary>Быстрый старт</summary>

## Вариант A — Локальный запуск (windows, linux - чуть иначе)

### Требования

Перед запуском должны быть установлены:

* **JDK 21**
* **Maven**
* **Apache Tomcat 9**
* **SQLite DB file** с подготовленной схемой и начальными данными

> Важно: проект использует `javax.servlet-api`, поэтому нужен **Tomcat 9**.
> Tomcat 10/11 использует уже `jakarta.servlet` и для этого проекта не подходит без миграции imports/dependencies.

---

### 1. Скачать проект

```bash
git clone https://github.com/timk01/currency-exchange.git
cd currency-exchange
```

Либо скачать проект архивом с GitHub и открыть его в IDE.

---

### 2. Настроить путь к SQLite DB

В проекте используется SQLite.

JDBC URL не хранится непосредственно в исходном коде, а передаётся приложению через системное свойство JVM:

```text
db.url
```

При запуске через IntelliJ IDEA необходимо открыть конфигурацию локального Tomcat и добавить в VM options:

-Ddb.url=jdbc:sqlite:C:/projects/currency-exchange/src/main/data/currency_exchange.db
(где томкат - едит конфигурейшенс)

Путь после jdbc:sqlite: должен указывать на существующий файл БД на вашей машине.

---

### 3. Собрать WAR

```bash
mvn clean package
```

После сборки WAR-файл будет находиться в папке:

```text
target/
```

---

### 4. Запустить через локальный Tomcat

Можно запустить двумя способами.

#### Способ 1 — через IntelliJ IDEA

1. Открыть проект в IntelliJ IDEA.
2. Добавить локальный Tomcat 9 в **Run/Debug Configurations**.
3. Добавить WAR artifact.
4. Установить **Application context** в `/`.
5. Добавить в **VM options** путь к локальной БД:

```text
-Ddb.url=jdbc:sqlite:C:/projects/currency-exchange/src/main/data/currency_exchange.db
```

Запустить Tomcat из IDE.

Браузерный интерфейс будет доступен по адресу:

http://localhost:8080/

REST API будет доступен по адресам:

http://localhost:8080/currencies
http://localhost:8080/exchangeRates
http://localhost:8080/exchange?from=USD&to=EUR&amount=100

---

#### Способ 2 — вручную через Tomcat

Скопировать WAR в папку `webapps` локального Tomcat.

Для запуска без context path:

```bash
cp target/currency-exchange.war <TOMCAT_HOME>/webapps/ROOT.war
```

Затем запустить Tomcat.

Перед запуском Tomcat необходимо передать JVM системное свойство `db.url`, например через `CATALINA_OPTS` или файл `setenv`.

Значение свойства должно указывать на существующий файл локальной базы данных:

```text
-Ddb.url=jdbc:sqlite:<ABSOLUTE_PATH_TO_DB>
```

---

## Вариант B — Запуск на сервере

### Требования на сервере

На сервере должны быть установлены:

* **Ubuntu / Linux server**
* **JDK 21**
* **Apache Tomcat 9**
* SQLite DB file, доступный пользователю `tomcat`

Приложение деплоится как `ROOT.war`, чтобы endpoint-ы были доступны без дополнительного context path:

```text
http://<SERVER_IP>:8080/currencies
http://<SERVER_IP>:8080/exchangeRates
http://<SERVER_IP>:8080/exchange
```

---

### 1. Подготовить SQLite DB на сервере

Создать директорию для базы данных:

```bash
mkdir -p /opt/tomcat/data
```

Скопировать файл БД на сервер:

```bash
scp src/main/data/currency_exchange.db root@<SERVER_IP>:/opt/tomcat/data/currency_exchange.db
```

Выдать права пользователю Tomcat:

```bash
chown -R tomcat:tomcat /opt/tomcat/data
```

Путь к серверной БД передаётся Tomcat через системное свойство `db.url`.

Создать systemd override для Tomcat:

```bash
sudo systemctl edit tomcat
```

Добавить в открывшийся файл:

[Service]
Environment="CATALINA_OPTS=-Ddb.url=jdbc:sqlite:/opt/tomcat/data/currency_exchange.db"

Сохранить файл и применить изменения:

systemctl daemon-reload
systemctl restart tomcat
systemctl status tomcat

Проверить, что переменная была добавлена:

systemctl show tomcat --property=Environment

В результате среди переменных окружения должно присутствовать:

CATALINA_OPTS=-Ddb.url=jdbc:sqlite:/opt/tomcat/data/currency_exchange.db

Эта настройка выполняется на сервере один раз и сохраняется при последующих деплоях приложения.


---

### 2. Собрать WAR локально

```bash
mvn clean package
```

---

### 3. Подготовить скрипт деплоя на сервере

Скрипт создаётся один раз и затем используется для повторных деплоев.

Локально можно создать файл `deploy-currency-exchange.sh` со следующим содержимым:

```bash
#!/usr/bin/env bash
set -euo pipefail

WAR_SRC="/tmp/ROOT.war"
WEBAPPS="/opt/tomcat/webapps"

if [ ! -f "$WAR_SRC" ]; then
  echo "ERROR: $WAR_SRC not found"
  echo "First upload WAR: scp target/currency-exchange.war root@<SERVER_IP>:/tmp/ROOT.war"
  exit 1
fi

echo "Stopping tomcat..."
systemctl stop tomcat

echo "Removing old ROOT..."
rm -rf "$WEBAPPS/ROOT"
rm -f "$WEBAPPS/ROOT.war"

echo "Moving new WAR..."
mv "$WAR_SRC" "$WEBAPPS/ROOT.war"
chown tomcat:tomcat "$WEBAPPS/ROOT.war"

echo "Starting tomcat..."
systemctl start tomcat

echo "Status:"
systemctl status tomcat --no-pager
```

Скопировать скрипт на сервер:

```bash
scp deploy-currency-exchange.sh root@<SERVER_IP>:/root/deploy-currency-exchange.sh
```

На сервере один раз выдать права на запуск и убрать Windows-переносы строк, если файл создавался в Windows:

```bash
chmod +x /root/deploy-currency-exchange.sh
sed -i 's/
$//' /root/deploy-currency-exchange.sh
```

---

### 4. Скопировать WAR на сервер

WAR копируется во временную директорию `/tmp`:

```bash
scp target/currency-exchange.war root@<SERVER_IP>:/tmp/ROOT.war
```

---

### 5. Запустить скрипт деплоя

Можно зайти на сервер и выполнить:

```bash
/root/deploy-currency-exchange.sh
```

Или запустить скрипт удалённо с локальной машины:

```bash
ssh root@<SERVER_IP> "/root/deploy-currency-exchange.sh"
```

Скрипт останавливает Tomcat, удаляет старое `ROOT`-приложение, переносит новый WAR в `webapps`, выдаёт права пользователю `tomcat` и снова запускает Tomcat.

---

### 6. Проверить приложение

Проверить REST API на сервере:

```bash
curl -i http://localhost:8080/currencies
```

Браузерный интерфейс снаружи доступен по адресу:

http://<SERVER_IP>:8080/

REST API доступен по адресам:

http://<SERVER_IP>:8080/currencies
http://<SERVER_IP>:8080/exchangeRates
http://<SERVER_IP>:8080/exchange?from=USD&to=EUR&amount=100

---

## Повторный деплой после изменений

Если сервер уже настроен, для повторного деплоя достаточно локально собрать WAR и скопировать его на сервер во временную директорию:

```bash
mvn clean package
scp target/currency-exchange.war root@<SERVER_IP>:/tmp/ROOT.war
```

Затем запустить серверный скрипт деплоя:

```bash
ssh root@<SERVER_IP> "/root/deploy-currency-exchange.sh"
```

Либо зайти на сервер и выполнить:

```bash
/root/deploy-currency-exchange.sh
```

Проверка:

```bash
curl -i http://localhost:8080/currencies
```

Браузерный интерфейс:

```text
http://<SERVER_IP>:8080/
```

</details>

---

<details>
  <summary>Детали реализации</summary>

## Детали реализации для любопытных

### SQLite и тип `REAL` для курса

`REAL` как курс оставлен осознанно (`DECIMAL` / `NUMERIC` в SQLite не дают полноценной decimal-точности).

Все же расчёты выполняются на стороне Java через `BigDecimal`:

```text
rate — округляется до 6 знаков
convertedAmount — округляется до 2 знаков
```

---

### Ограничения на уровне БД

В БД добавлены ограничения:

```text
Currencies.Code — UNIQUE + CHECK(length(Code) = 3)
ExchangeRates(BaseCurrencyId, TargetCurrencyId) — UNIQUE
ExchangeRates.BaseCurrencyId / TargetCurrencyId — FOREIGN KEY
CHECK(BaseCurrencyId <> TargetCurrencyId)
```

Предварительные проверки в коде используются для более понятной обработки ошибок, но финальная защита от дублей остаётся на уровне БД через `UNIQUE`.

---

### HikariCP

Для работы с соединениями используется HikariCP.

DAO по-прежнему получают `Connection` через `DBConnectionFactory`, а соединение - берётся из pool-а.

Для SQLite (связано с особенностями SQLite как файловой БД) используется:

```text
maximumPoolSize = 1
```

При остановке или redeploy приложения pool закрывается через `ServletContextListener`.

---

### Конфигурация БД

Настройки HikariCP находятся в `DBConnectionFactory`.

JDBC URL передаётся приложению через системное свойство JVM:

```text
db.url
```

Это позволяет использовать один и тот же WAR в разных окружениях:

локально — jdbc:sqlite:C:/projects/currency-exchange/src/main/data/currency_exchange.db
на сервере — jdbc:sqlite:/opt/tomcat/data/currency_exchange.db

Отдельный .properties файл не добавлялся: локальное значение передаётся через VM options, а серверное — через CATALINA_OPTS в systemd override.

---

### DTO и converters

Для REST-ответов используются DTO.

Преобразование моделей/проекций в response DTO выполняется вручную через converter-классы.

MapStruct / ModelMapper не добавлялись: преобразования в проекте небольшие, а ручные converter-ы позволяют явно показать, какие данные попадают в REST-ответ.

---

### Ошибки и JSON-ответы

Servlet-ы обрабатывают ошибки и возвращают HTTP status + JSON body.

DAO / Service выбрасывают project exceptions, а servlet-слой преобразует их в REST-ответы.

Формат ошибки:

```json
{
  "message": "error message"
}
```

---

### Filter и BaseApiServlet

Общие настройки request/response вынесены в filter:

```text
request encoding — UTF-8 
response encoding — UTF-8
```
(Content-Type: application/json - не выставленна намеренно, т.к. есть JS для отображения фронта)

Установка JSON content type, сериализация успешных ответов и запись ошибок выполняются в BaseApiServlet

</details>

---

## Важные замечания

* Проект использует **Tomcat 9**, потому что servlet API в проекте основан на `javax.servlet`.
* Приложение деплоится как `ROOT.war`, поэтому браузерный интерфейс доступен по корневому адресу, а REST API — без дополнительного context path.
* JDBC URL должен быть передан JVM через системное свойство `db.url`.
* Пользователь `tomcat` должен иметь права на чтение и запись файла БД и директории, где он расположен.
* Фронтенд обращается к API через относительный адрес `host = "."`, поэтому один и тот же код работает локально и на удалённом сервере.
* `Content-Type: application/json` устанавливается только для API-ответов в `BaseApiServlet`.
* Если используется HikariCP, `Connection.close()` в DAO не закрывает физическое соединение, а возвращает его в connection pool.
* При остановке или redeploy web-приложения Hikari pool закрывается через `ServletContextListener`.

## Контакты

Автор: [@timk01](https://github.com/timk01)
Телеграмм: https://t.me/tim_matv
