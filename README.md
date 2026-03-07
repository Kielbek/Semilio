# 🌿 Semilio - Sustainable Fashion Marketplace

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4+-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Angular](https://img.shields.io/badge/Angular-19-DD0031?style=for-the-badge&logo=angular&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)

**Semilio** to nowoczesna platforma e-commerce typu C2C (Customer-to-Customer), zainspirowana rozwiązaniami takimi jak Vinted czy OLX. Aplikacja koncentruje się na rynku mody cyrkularnej (second-hand), oferując bezpieczne środowisko do sprzedaży, kupna i wymiany odzieży.

Projekt wyróżnia się zastosowaniem najnowszych standardów technologicznych (Java 21, Angular Signals) oraz architektury Cloud-Ready (AWS S3).

## 🛠️ Stos Technologiczny (Tech Stack)

### Backend (Java 21 / Spring Boot 3.4)
* **Spring Security 6 & JJWT** – Bezpieczna, bezstanowa autoryzacja oparta na tokenach JWT.
* **AWS SDK (S3)** – Przechowywanie zdjęć produktów w chmurze Amazon (skalowalność i wydajność).
* **Spring WebSocket (STOMP)** – Obsługa czatu w czasie rzeczywistym między kupującym a sprzedającym.
* **MapStruct & Lombok** – Profesjonalne mapowanie DTO i redukcja kodu boilerplate.
* **Twilio SDK** – Integracja z usługami SMS/Głosowymi.
* **Spring Data JPA & PostgreSQL** – Wydajna warstwa persystencji danych.
* **Spring Mail** – System powiadomień e-mail.

### Frontend (Angular 19)
* **Angular Signals** – Nowoczesne zarządzanie stanem aplikacji (State Management).
* **Standalone Components** – Architektura bezmodułowa dla lepszej wydajności.
* **Tailwind CSS** – Utility-first CSS framework (Mobile First).
* **Lucide Icons** – Nowoczesna biblioteka ikon SVG.

## 🚀 Kluczowe Funkcje

* **Real-time Chat**: Natychmiastowa komunikacja (WebSockets) umożliwiająca negocjację ceny.
* **Cloud Image Processing**: Zdjęcia są przesyłane bezpośrednio do AWS S3.
* **Advanced Filtering**: Wyszukiwanie po rozmiarach, markach i lokalizacji.
* **Secure Auth**: Pełny proces rejestracji, logowania i resetowania haseł.

## 🔐 Konfiguracja i Zabezpieczenia (Ważne!)

Aplikacja integruje się z zewnętrznymi serwisami, co wymaga podania kluczy API. **Z przyczyn bezpieczeństwa dane te nie są śledzone w repozytorium.** Aby uruchomić aplikację lokalnie, musisz uzupełnić plik `application.properties` (lub ustawić zmienne środowiskowe) o następujące dane:

* **AWS S3**: Wymagane są `aws.accessKeyId` oraz `aws.secretKey` do obsługi zdjęć.
* **Spring Mail**: Wymagane są dane serwera SMTP (`spring.mail.username`, `spring.mail.password` – w przypadku Gmaila użyj *App Password*).
* **Twilio**: Wymagane są `twilio.accountSid`, `twilio.authToken` oraz zweryfikowany numer nadawcy do obsługi SMS.

## 🏗️ Architektura
Projekt realizuje zasady **Clean Architecture** i **SOLID**:
1. **Backend**: Warstwy Controller -> Service -> Repository.
2. **Frontend**: Feature-based structure (Features, Core, Shared).

## 🏁 Uruchomienie lokalne

### Wymagania wstępne (Prerequisites)
Przed uruchomieniem upewnij się, że masz zainstalowane na swoim komputerze:
* **Java 21** oraz Maven
* **Node.js** (wersja 18+) oraz Angular CLI
* **PostgreSQL** (uruchomiony lokalnie na porcie 5432 z utworzoną bazą danych)

### 1. Uruchomienie Backendu (Spring Boot)
Pamiętaj, aby przed uruchomieniem uzupełnić plik `application.properties` o hasła i klucze API.

```bash
cd semilio-core
mvn clean install
mvn spring-boot:run
```

Serwer uruchomi się domyślnie na porcie 8080.

### 2. Uruchomienie Frontendu (Angular 19)
Przejdź do folderu z kodem źródłowym frontendu i zainstaluj zależności.

```bash
cd semilio-frontend
npm install
ng serve
```
