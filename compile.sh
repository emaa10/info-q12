#!/usr/bin/env bash
set -e

# out/ vorher leeren, damit keine alten .class-Dateien liegen bleiben
rm -rf out
mkdir -p out

# Java-Quellen kompilieren
javac --module-path "$FX" --add-modules javafx.controls,javafx.fxml,javafx.graphics -cp "lib/*" -d out $(find src/main/java -name "*.java")

# Ressourcen (Bilder, Audio, ...) nach out/ kopieren.
# WICHTIG: ohne diesen Schritt findet getResourceAsStream() die Bilder nicht
# -> NullPointerException "Input stream must not be null" beim Start.
cp -r src/main/resources/* out/
