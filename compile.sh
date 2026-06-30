javac --module-path "$FX" --add-modules javafx.controls,javafx.fxml,javafx.graphics -cp "lib/*" -d out $(find src/main/java -name "*.java")
cp -r src/main/resources/* out/
