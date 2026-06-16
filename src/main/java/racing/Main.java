package racing;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import racing.controller.Kontrolleur;
import racing.model.Spiel;
import racing.view.Oberflaeche;

public class Main extends Application {

    private static final int BREITE = 960;
    private static final int HOEHE  = 600;

    private Spiel spiel;
    private Kontrolleur kontrolleur;
    private Thread gameThread;
    private Thread controllerThread;

    @Override
    public void start(Stage buehne) {
        Oberflaeche oberflaeche = new Oberflaeche();

        spiel      = new Spiel(oberflaeche);
        kontrolleur = new Kontrolleur(oberflaeche, spiel);

        Scene szene = new Scene(oberflaeche.gibWurzel(), BREITE, HOEHE);
        buehne.setTitle("Rennspiel");
        buehne.setScene(szene);
        buehne.setMinHeight(HOEHE);
        buehne.setMinWidth(BREITE);
        buehne.setMaxHeight(HOEHE);
        buehne.setMaxWidth(BREITE);
        buehne.show();

        // Tastatureingabe registrieren (braucht die fertige Scene)
        oberflaeche.registriereEingabe(szene);

        gameThread      = new Thread(spiel,       "GameThread");
        controllerThread = new Thread(kontrolleur, "ControllerThread");
        gameThread.setDaemon(true);
        controllerThread.setDaemon(true);
        gameThread.start();
        controllerThread.start();
    }

    @Override
    public void stop() {
        if (spiel != null)       spiel.stoppe();
        if (kontrolleur != null) kontrolleur.stoppe();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
