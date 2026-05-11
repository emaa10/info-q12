package com.pixeldrift.audio;

import javax.sound.sampled.Clip;
import java.util.Map;

public class AudioManager {
    private Map<SoundEffect, Clip> clips;
    private Clip currentMusic;
    private float volume;

    public void playSound(SoundEffect s) {}

    public void playMusic(String name) {}

    public void stopMusic() {}

    public void setVolume(float v) {}
}
