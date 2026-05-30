package racing.view;

import javafx.scene.Parent;
import javafx.scene.layout.Pane;

// view-teil im MVC
public class Oberflaeche {

    private final Pane wurzel;

    public Oberflaeche() {
        this.wurzel = new Pane();
        this.wurzel.setStyle("-fx-background-color: white;");
    }

    public Parent gibWurzel() {
        return wurzel;
    }

    public void registriereEingabe() {
        // fehlt noch listener und so
    }

    // wird dann vom spiel aufgerufen zum aktualisieren
    public void aktualisiere() {
        // todo zeichnen
    }
}
