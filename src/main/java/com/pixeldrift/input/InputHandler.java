package com.pixeldrift.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Set;

public class InputHandler implements KeyListener {
    private Set<Integer> pressedKeys;

    public boolean isPressed(int keyCode) {
        return false;
    }

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}
}
