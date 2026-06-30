package racing;

import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class AudioWiedergabe implements Runnable {

    private final String datei;
    private Clip clip;

    public AudioWiedergabe(String datei) {
        this.datei = datei;
    }

    @Override
    public void run() {
        try {
            URL url = getClass().getResource(datei);
            if (url == null) {
                System.err.println("Audio-Datei nicht gefunden: " + datei);
                return;
            }
            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            clip = AudioSystem.getClip();
            clip.open(ais);
            ais.close();
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            Thread.sleep(Long.MAX_VALUE);
        } catch (Exception e) {
            System.err.println("Audio-Fehler: " + e.getMessage());
        }
    }

    public void stoppe() {
        if (clip != null) {
            clip.stop();
            clip.close();
        }
    }
}
