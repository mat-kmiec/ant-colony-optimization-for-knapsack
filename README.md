# 🐜 AntPack Optimizer (ACO Knapsack Solver)

> Zaawansowana wizualizacja Algorytmu Mrówkowego (Ant Colony Optimization) rozwiązującego Problem Plecakowy (Knapsack Problem).

![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-UI-4285F4?style=for-the-badge&logo=java&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)

## 📋 O Projekcie

**AntPack Optimizer** to aplikacja desktopowa napisana w Javie, która pozwala na żywo obserwować, jak roje wirtualnych mrówek rozwiązują problemy optymalizacyjne typu NP-trudnego. Aplikacja nie tylko znajduje rozwiązanie, ale przede wszystkim **wizualizuje proces decyzyjny** (ślady feromonowe, zbieżność populacji, eksplorację).

Projekt powstał w celu zbadania wpływu parametrów metaheurystycznych (Alpha, Beta, Rho) na efektywność algorytmów roju.

---

## ✨ Główne Funkcjonalności

* **📈 Wizualizacja w Czasie Rzeczywistym:**
    * Wykres zbieżności (Convergence Graph): Porównanie średniej populacji z najlepszym znalezionym rozwiązaniem.
    * Wykres rozrzutu (Scatter Plot): Wizualizacja przestrzeni rozwiązań (Waga vs Wartość).
* **🧪 Laboratorium Feromonowe:**
    * Dynamiczna tabela ("Heatmapa"), która podświetla na zielono przedmioty wybierane przez mrówki.
    * Możliwość obserwacji procesu "parowania" i wzmacniania ścieżek.
* **🎛️ Pełna Kontrola Parametrów:**
    * **Alpha (α):** Wpływ feromonów (doświadczenie historyczne).
    * **Beta (β):** Wpływ heurystyki (lokalna opłacalność przedmiotu).
    * **Rho (ρ):** Współczynnik parowania śladów.
* **📂 Obsługa Danych:**
    * Wbudowany generator "Trudnych Problemów" (Strongly Correlated Instances).
    * **Import własnych danych** z plików `.txt`.

---

## 📸 Zrzuty Ekranu

| Główny Panel | Tabela Feromonów |
|:---:|:---:|
| <img src="screenshots/main_view.png" width="400" alt="Widok Główny"> | <img src="screenshots/table_view.png" width="400" alt="Tabela"> |

---

## 🚀 Instalacja i Uruchomienie

### Wymagania
* JDK 17 lub nowsze.
* Maven.

### Krok 1: Klonowanie
```bash
git clone [https://github.com/mat-kmiec/ant-colony-optimization-for-knapsack)
cd antpack-optimizer
