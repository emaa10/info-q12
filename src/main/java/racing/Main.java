package racing;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import racing.controller.Kontrolleur;
import racing.model.Spiel;
import racing.view.Oberflaeche;

public class Main extends Application {

    private static final int BREITE = 960;
    private static final int HOEHE = 600;

    private Spiel spiel;
    private Kontrolleur kontrolleur;
    private Thread gameThread;
    private Thread controllerThread;

    @Override
    public void start(Stage buehne) {
        Oberflaeche oberflaeche = new Oberflaeche();

        spiel = new Spiel(oberflaeche);
        kontrolleur = new Kontrolleur(oberflaeche, spiel);

        Scene szene = new Scene(oberflaeche.gibWurzel(), BREITE, HOEHE);
        buehne.setTitle("Racinggame");
        buehne.setScene(szene);
        buehne.show();

        spielThread = new Thread(spiel, "GameThread");
        kontrollThread = new Thread(kontrolleur, "ControllerThread");
        spielThread.setDaemon(true);
        kontrollThread.setDaemon(true);
        spielThread.start();
        kontrollThread.start();
    }

    @Override
    public void stop() {
        if (spiel != null) {
            spiel.stoppe();
        }
        if (kontrolleur != null) {
            kontrolleur.stoppe();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
