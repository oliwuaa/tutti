# tutti


## ✨ Funkcjonalności aplikacji

### 🎼 Zarządzanie Nutami (Biblioteka)
- **Panel Zarządu:** Pełne CRUD (dodawanie, edycja, usuwanie) zasobów nutowych.
- **Dostęp dla Muzyków:** Wygodne przeglądanie i pobieranie materiałów do ćwiczeń.
- **Personalizacja głosów:** Funkcja przypisywania konkretnych stron lub partytur do wybranych muzyków (podział na głosy).
- **Wyszukiwarka:** Zaawansowane filtrowanie nut według tytułu, autora oraz instrumentu.

### 📅 Wydarzenia i Koncerty
- **Planowanie:** Narzędzia dla Zarządu do tworzenia nowych wydarzeń i koncertów.
- **Szczegóły logistyczne:** Możliwość określenia miejsca, daty, planu organizacji oraz listy utworów (setlisty).
- **Harmonogram:** Przejrzysty widok nadchodzących wydarzeń dla wszystkich członków orkiestry.

### 🎺 Zasoby
- **Instrumentarium:** Spis instrumentów orkiestrowych wraz z systemem przypisywania ich do konkretnych muzyków.
- **Ewidencja materiałów:** Zarządzanie spisem marszówek, strojów oraz innych zasobów wspólnych.

## 🎭 Role i Uprawnienia
| Rola | Opis | 
| :--- | :--- | 
| **OWNER** | Właściciel orkiestry |
| **ADMIN** | Członek Zarządu | 
| **CONDUCTOR** | Dyrygent |
| **LIBRARIAN** | Bibliotekarz | 
| **MUSICIAN** | Muzyk |

#### 👑 Właściciel orkiestry, Członek Zarządu, Dyrygent - posiadają uprawnienia bibliotekarza
* **Zarządzanie orkiestrą** - członkami, zasobami, nutami orkiestry

#### 📚 Bibliotekarz - posiada uprawnienia muzyka
* **Repozytorium nut:** Dodawanie, edytowanie i usuwanie nut, podział na głosy.

#### 🎺 Muzyk
* **Moje Nuty:** Szybki dostęp do przypisanych głosów lub całych partytur.
* **Kalendarz:** Dostęp do wydarzeń, możliwość podejrzenia szczegółów.

## 🚀 Szybki Start (Docker)

Najszybszym sposobem na uruchomienie aplikacji jest użycie Dockera. Dzięki temu nie musisz konfigurować środowiska Java czy Node.js lokalnie.

### Wymagania
* Zainstalowany i uruchomiony **Docker Desktop**.

### Instrukcja uruchomienia
1. Sklonuj repozytorium:
   ```bash
   git clone [https://github.com/oliwuaa/tutti.git](https://github.com/oliwuaa/tutti.git)
   cd tutti
2. Zbuduj i uruchom kontenery:
   ```bash
    docker-compose up --build

- Aplikacja backendowa będzie dostępna pod adresem: [http://localhost:8080](http://localhost:8080)
- Aplikacja frontendowa (React) będzie dostępna pod adresem: [http://localhost:3000](http://localhost:3000)
- Po uruchomieniu aplikacji, dokumentacja Swaggera dostępna jest pod adresem:
[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)


## 🛠 Technologie

Projekt został zbudowany przy użyciu:

- **Java 21**
- **Spring Boot**
- **Spring Data JPA**
- **Lombok**
- **Swagger**
- **Gradle**
- **Baza danych H2**
- **React**
- **Axios**
