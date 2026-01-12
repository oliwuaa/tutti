# Wzorce projektowe

### **Layer Supertype**
- klasa `BaseEntity`(klasa bazowa dla wszystkich encji w projekcie, takich jak `User`, `Score`, `Part` czy `Orchestra`).
- uzasadnienie: Zamiast w każdej klasie z osobna dopisywać pola takie jak unikalny identyfikator (`id`) czy numer wersji (`version`), definiuje się je raz w jednej klasie nadrzędnej.
- korzyści: Ogromna spójność w całym systemie – każda tabela w bazie danych ma identyczną strukturę podstawową, brak powtórzeń w kodzie, łatwiejsze wprowadzanie zmian.

### **Mapper**
- np. klasy `UserMapper`, `ScoreMapper`, `MembershipMapper`...
- uzasadnienie: Izoluje 2 podsystemy i kontroluje wymianę informacji między nimi tak, że nie wiedzą one o jego istnieniu.
- korzyści : Zapewnia to pełną niezależność – możliwość zmiany  nazwy pól w bazie danych bez wpływania na działanie aplikacji klienckiej. Dodatkowo zwiększa to bezpieczeństwo, ponieważ Mapper pozwala kontrolować, które dane "wychodzą" na zewnątrz.

### **Value Object**
- klasa `PageRange`
- uzasadnienie: To mały obiekt, którego tożsamość opiera się na jego wartościach, a nie na unikalnym ID. `PageRange` (zakres stron) nie ma własnego cyklu życia w bazie danych bez encji `Part`.
- korzyści: Zamiast przesyłać wszędzie dwie luźne liczby (start i koniec), przesyłany jeden bezpieczny obiekt, który sam pilnuje, czy dane są poprawne.

### **Range - specyficzny rodzaj Value Object**
- klasa `PageRange`
- uzasadnienie: Wzorzec ten grupuje dwie logicznie powiązane wartości (`pageStart` i `pageEnd`) w jeden obiekt, zamiast operować na nich oddzielnie. Dzięki temu logika walidacji (np. sprawdzenie, czy koniec nie jest przed początkiem w metodzie `of`) oraz obliczenia (np. `getPageCount`) są zamknięte wewnątrz jednej klasy.
- korzyści: Zwiększa to czytelność kodu, zapobiega błędom polegającym na przekazaniu niepoprawnych wartości do bazy danych i pozwala traktować zakres stron jako spójną całość biznesową.

### **Unit of Work**
- np. klasa: `UserServiceImpl`
- uzasadnienie :  Wzorzec ten grupuje wszystkie operacje wykonywane podczas usuwania użytkownika (walidację, zwalnianie zasobów członkowskich oraz samo usunięcie z bazy) w jedną nierozerwalną całość.
- korzyści: Zapewnia to integralność danych – albo wszystkie kroki usuwania zakończą się sukcesem, albo (w razie błędu) system przywróci stan sprzed rozpoczęcia usuwania.
- 
### **Lazy Load**
- np. klasy  `Part`, `Score`
- uzasadnienie: Wzorzec ten umożliwia wstrzymanie pobierania powiązanych danych z bazy do momentu, gdy zostaną one realnie wywołane w kodzie.
- korzyści: Aplikacja działa znacznie szybciej, ponieważ nie ściąga niepotrzebnie całego drzewa powiązanych danych przy każdym zapytaniu.

### **Identity Field**
- klasa `BaseEntity`
- uzasadnienie: Każda encja dziedziczy po `BaseEntity`, które posiada pole `@Id`. Dzięki temu każdy obiekt w systemie ma swój unikalny klucz główny w bazie danych.
- Zapewnia niezawodne powiązanie między światem obiektowym a relacyjną bazą danych. Dzięki temu unika się błędów polegających na nadpisaniu niewłaściwych danych, a system może łatwo zarządzać relacjami między tysiącami różnych rekordów.

### **Foreign Key Mapping**
- np. klasy `Part`, `Orchestra`...
- uzasadnienie: Obiekt nadrzędny przechowuje identyfikator obiektu powiązanego.
- korzyści: Pozwala to na budowanie powiązań między różnymi typami danych (np. przypisanie konkretnej partii instrumentu do konkretnego utworu muzycznego) w sposób zrozumiały dla relacyjnych baz danych.

### **Repository**
- np. klasy  `PartRepository`, `OrchestraRepository`...
- uzasadnienie: Tworzy warstwę abstrakcji między warstwą dziedziny, a warstwą odwzorowania, zawierającej kod odpowiedzialny za generowanie zapytań.
- korzyści: Eliminuje potrzebę pisania powtarzalnych zapytań SQL w serwisach, co sprawia, że kod jest czytelniejszy i umożliwia łatwe wprowadzanie zmian.

### **Model-View-Controller (MVC)**
- np. klasy  `Orchestra`,  `OrchestraController` oraz aplikacja frontendowa.
- uzasadnienie: Rozdzielona logika danych (Model), logikę sterowania przepływem (Controller) i sposób prezentacji (View). 
- korzyści: pozwala na niezależny rozwój backendu i frontendu oraz ułatwia zarządzanie kodem. 
- 
### **Page Controller**
- np. klasa  `OrchestraController`, `MembershipController`...
- uzasadnienie: Każdy kontroler obsługuje konkretny obszar funkcjonalny (zasób) aplikacji.
- korzyści: Logiczne pogrupowanie akcji (np. dodawanie, usuwanie członkostw) w jednym miejscu, co ułatwia nawigację po kodzie.

### **Data Transfer Object (DTO)**
- Klasy w pakietach  `request` i `response` (np. `ScoreRequest`, `UserResponse`).
- uzasadnienie: Zamiast wysyłać "ciężką" encję bazy danych, wysyłamy tylko te pola, których potrzebuje frontend.
- korzyści: Zmniejsza ilość przesyłanych danych i chroni prywatność

### **Remote Facade**
- kontrolery np. `OrchestraController`, `MembershipController`...
- uzasadnienie: Zamiast kazać frontendowi wywoływać po kolei kilka różnych serwisów, udostępniany jeset jeden czytelny punkt końcowy (endpoint).
- korzyści : Upraszcza komunikację dla klienta (frontendu) i ogranicza liczbę zapytań sieciowych

### **Optimistic Offline Lock**
- Klasa `BaseEntity`
- uzasadnienie: Przy każdej próbie zapisu system sprawdza, czy numer wersji w bazie danych jest taki sam, jak ten, który pobraliśmy na początku. Jeśli ktoś inny zmienił rekord w międzyczasie, wersja się nie zgodzi i system rzuci błąd, zamiast bezmyślnie nadpisać zmiany.
- korzyści: Pozwala wielu użytkownikom pracować na tych samych danych bez blokowania bazy danych na sztywno.

### **Client Session State**
- klasa `JwtService` oraz filtr `JwtAuthenticationFilter`
- uzasadnienie: Serwer nie przechowuje informacji o tym, że dany użytkownik jest zalogowany. Wszystkie niezbędne dane (np. ID użytkownika, jego rola) są zaszyfrowane w tokenie, który klient (frontend) musi dołączyć do każdego zapytania.
- korzyści: Backend staje się "lekki" i łatwo go skalować, ponieważ serwer nie musi marnować pamięci RAM na utrzymywanie tysięcy otwartych sesji.


-----
### **Architektura Projektu**
#### Architektura wielowarstwowa (Layered architecture)
Szkieletem aplikacji jest podział na trzy niezależne poziomy, co zapewnia porządek i separację odpowiedzialności:

-   **Warstwa Prezentacji (Controllers):** Odpowiada wyłącznie za komunikację ze światem zewnętrznym (REST API).
    
-   **Warstwa Logiki Biznesowej (Services):** Miejsce, w którym zaimplementowane są wszystkie zasady działania aplikacji (np. walidacje, procesy usuwania).
    
-   **Warstwa Dostępu do Danych (Repositories):** Całkowicie izoluje system od szczegółów technicznych bazy danych.

Dzięki takiemu podejściu, architektura **Tutti** nie jest „sztywna”. Jest to system modularny, w którym wymiana jednego elementu (np. zmiana sposobu przechowywania plików czy zmiana bazy danych) nie wymaga przebudowy całego kodu, co jest kluczową cechą profesjonalnego oprogramowania
