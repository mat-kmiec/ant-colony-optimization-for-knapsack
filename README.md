# AntPack Optimizer (ACO Knapsack Solver)

> Zaawansowana wizualizacja Algorytmu Mrówkowego (Ant Colony Optimization) rozwiązującego Problem Plecakowy (Knapsack Problem).

![Java](https://img.shields.io/badge/Java-20%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-UI-4285F4?style=for-the-badge&logo=java&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)

##  O Projekcie

**AntPack Optimizer** to aplikacja desktopowa napisana w Javie, która pozwala na żywo obserwować, jak roje wirtualnych mrówek rozwiązują problemy optymalizacyjne typu NP-trudnego. Aplikacja nie tylko znajduje rozwiązanie, ale przede wszystkim **wizualizuje proces decyzyjny** (ślady feromonowe, zbieżność populacji, eksplorację).

Projekt powstał w celu zbadania wpływu parametrów metaheurystycznych (Alpha, Beta, Rho) na efektywność algorytmów roju.

---

##  Kluczowe Funkcjonalności

* **Silnik ACO (Ant Colony Optimization):** Autorska implementacja metaheurystyki inspirowanej zachowaniem mrówek, zoptymalizowana pod kątem efektywnego przeszukiwania przestrzeni rozwiązań problemów NP-trudnych.
* **Wielowątkowość (Parallel Processing):** Wykorzystanie `Java Streams API (.parallel())` do równoległej symulacji wielu agentów (mrówek), co pozwala na pełne wykorzystanie mocy procesorów wielordzeniowych.
* **Dynamiczne Sterowanie Parametrami:** Interfejs umożliwia modyfikację parametrów symulacji "w locie", bez konieczności restartu algorytmu:
    * **Alpha ($\alpha$):** Kontrola wpływu śladu feromonowego na decyzje.
    * **Beta ($\beta$):** Waga atrakcyjności (heurystyki) przedmiotu.
    * **Evaporation ($\rho$):** Szybkość parowania feromonów, zapobiegająca przedwczesnej zbieżności.
* **Zaawansowana Wizualizacja:**
    * **Convergence Chart:** Wykres zbieżności pokazujący poprawę wyniku globalnego w czasie.
    * **Scatter Chart:** Wizualizacja przedmiotów w przestrzeni waga-wartość.
    * **Pheromone Monitor:** Tabela wyświetlająca aktualne poziomy feromonów dla każdego elementu zestawu danych.
* **Mechanizm Anty-Stagnacyjny:** System wykrywający brak poprawy wyniku przez określoną liczbę iteracji, automatycznie resetujący ślad feromonowy w celu wymuszenia nowej eksploracji.

---

##  Podstawy Algorytmiczne

Prawdopodobieństwo wyboru przedmiotu $i$ przez mrówkę w procesie budowania rozwiązania opisuje wzór:

$$P_{i} = \frac{\tau_i^\alpha \cdot \eta_i^\beta}{\sum_{j \in candidates} \tau_j^\alpha \cdot \eta_j^\beta}$$

Gdzie:
* $\tau_i$ (tau) – poziom skumulowanego feromonu na przedmiocie.
* $\eta_i$ (eta) – wartość heurystyczna, zdefiniowana jako $\frac{value_i}{weight_i}$.



---

## Stos Technologiczny

* **Język:** Java 20+
* **UI Framework:** JavaFX 21 (FXML + CSS)
* **Build Tool:** Maven 3.9+
* **Biblioteki:** JavaFX Controls, JavaFX FXML
* **Architektura:** Podział na moduły `core` (silnik), `model` (dane) oraz `ui` (warstwa prezentacji).

---

##  Struktura Projektu

```text
pl.antpack
├── core/       # Główny silnik ACO (ACOEngine, Ant, Solution)
├── model/      # Modele domenowe (Item, Knapsack)
├── ui/         # Kontrolery JavaFX i zarządzanie widokami
├── utils/      # Narzędzia pomocnicze (BenchmarkGenerator)
└── Main.java   # Punkt wejściowy aplikacji

```
##  Instalacja i Uruchomienie

Aby uruchomić projekt lokalnie, upewnij się, że Twoje środowisko spełnia poniższe wymagania techniczne.

### Wymagania Systemowe
* **Java Development Kit (JDK):** Wersja 20 lub nowsza (zalecane OpenJDK).
* **Apache Maven:** Wersja 3.9+ (do zarządzania zależnościami i budowania projektu).
* **Środowisko graficzne:** Obsługa biblioteki JavaFX (dostępna w większości nowoczesnych dystrybucji JDK).

### Instrukcja Krok po Kroku

1. **Klonowanie repozytorium:**
   Otwórz terminal i pobierz kod źródłowy na swój dysk:
   ```bash
   git clone [https://github.com/mat-kmiec/ant-colony-optimization-for-knapsack](https://github.com/mat-kmiec/ant-colony-optimization-for-knapsack)
   cd antpack-optimizer
   ```

2. **Kompilacja i instalacja zależności:**
    Pobierz biblioteki (JavaFX, Maven Plugins) i zbuduj plik wykonywalny:
   ```bash
   mvn clean install
   ```
3. **Uruchomienie aplikacji:**
    Skorzystaj z dedykowanego pluginu Maven, aby zainicjować interfejs graficzny:
   ````bash
   mvn javafx:run
   ````
## Format pliku wejściowego (.txt)

Aplikacja pozwala na wczytywanie własnych zestawów danych. Plik tekstowy powinien być sformatowany według poniższego schematu:

* **Pierwsza linia:** Całkowita pojemność plecaka (liczba całkowita).
* **Kolejne linie:** Każda linia reprezentuje jeden przedmiot w formacie `WAGA WARTOŚĆ` (rozdzielone spacją).

## Przykład pliku `dane.txt`:
```text
750
70 135
73 139
77 149
80 150
82 156
87 163
90 173
94 184
98 192
106 201
110 210
113 214
115 221
118 229
120 240
