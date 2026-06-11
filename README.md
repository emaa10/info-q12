<!-- TOC -->
- [Informatik Projekt](#informatik-projekt)
- [Documentation](#documentation)
- [Email an Herr Stark](#email-an-herr-stark)
- [Projekt](#projekt)
  - [Konzept](#konzept)
  - [Technologien](#technologien)
  - [Build & Start](#build-start)
- [kompilieren](#kompilieren)
- [starten](#starten)
<!-- /TOC -->

# Informatik Projekt
Repository for the project in the IT class in grade 12

# Documentation
[![Project Board](https://img.shields.io/badge/Project-Board-blue?logo=github)](https://github.com/users/emaa10/projects/2)

# Email an Herr Stark
**an** starkrobert@gym-indersdorf.de 
**Betreff**: Berger_1.Woche z.b.

# Projekt

## Konzept
Wir bauen ein 2d-racing game. Vorerst soll es sich auf den Einzelspielermodus beschränken.

## Technologien
Wir haben uns für das UI-Framework JavaFX entschieden, da es im Vergleich zu Swing für die Spieleentwicklung optimiert ist: so nutzt es z.B. die Grafikprozessoren des Computers, was Swing nicht macht. Zudem haben wir die von KI generierte Struktur (siehe Stunde am Dienstag) gelöscht und selbst überarbeitet.

## Build & Start

Voraussetzungen: **JDK 17** und das **JavaFX 17 SDK** ([Download](https://gluonhq.com/products/javafx/)). Pfad zum SDK-`lib`-Ordner setzen:

```bash
export FX=/pfad/zu/javafx-sdk-17/lib
# MAC setup
https://gluonhq.com/products/javafx/
export FX=/Users/emanuel/javafx-sdk-17.0.19/lib

# kompilieren
javac --module-path "$FX" --add-modules javafx.controls -cp lib/sqlite-jdbc-3.46.0.0.jar -d out $(find src/main/java -name "*.java")

# starten
java --module-path "$FX" --add-modules javafx.controls -cp out:lib/sqlite-jdbc-3.46.0.0.jar racing.Main
```

> **Windows:** Trennzeichen im `-cp`-Flag ist `;` statt `:`, also `out;lib\sqlite-jdbc-3.46.0.0.jar`
