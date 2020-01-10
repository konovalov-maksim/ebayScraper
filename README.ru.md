# ![app_icon](https://user-images.githubusercontent.com/49783652/72156153-ddbdd180-33c5-11ea-8f87-368e733923ea.png) Ebay Scraper

*Read this in another languages: ![en](https://user-images.githubusercontent.com/49783652/69971412-e56d9900-1530-11ea-8516-f9f1f6219147.png) [English](https://github.com/konovalov-maksim/ebay_scraper/blob/master/README.md), ![ru](https://user-images.githubusercontent.com/49783652/69971413-e56d9900-1530-11ea-8937-a7989b8d727d.png) [Русский](https://github.com/konovalov-maksim/ebay_scraper/blob/master/README.ru.md).*

Ebay Scraper - это инструмент, который позволяет массово извлекать данные о товарах с Ebay.com по списку запросов.

![download](https://user-images.githubusercontent.com/49783652/70123296-6b99f480-1683-11ea-8f71-ac9d1e14fd54.png) Скачать релиз (v1.0): [EbayScraper-1.0.exe + JRE](https://github.com/konovalov-maksim/ebay_scraper/releases/download/1.0.0/ebay_scraper.zip) (77.2 Mb)

![screenshot-1](https://user-images.githubusercontent.com/49783652/72157230-6fc6d980-33c8-11ea-9c1d-3ad6371d5879.png)

#### Особенности:
- Извлечение числа активных товаров и числа проданных товаров, найденных по запросу
- Определение средней стоимости найденных активных и проданных товаров
- Возможность многопоточного сбора данных
- Фильтры по категории и состоянию товаров
- Возможность поиска CD по номерам штрих-кодов на основе данных discogs.com

### Запуск
Для запуска приложения необходимо указать Ebay App Id ([регистрация](https://developer.ebay.com/))

Для конвертации UPC также необходимо также указать токен разработчика Discogs ([регистрация](https://www.discogs.com/developers/))

Указанные ключи должны быть размещены в файлах

\app\key.txt

\app\discogs_token.txt

соответственно.
