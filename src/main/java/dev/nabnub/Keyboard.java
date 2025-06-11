package dev.nabnub;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;

public class Keyboard implements KeyListener {
    private boolean[] keys;
    private final Map<Integer, Integer> keyMap = new HashMap<>();

    public Keyboard() {
        initialize();
    }

    private void initialize() {
        keys = new boolean[16];
        keyMap.clear();

        keyMap.put(KeyEvent.VK_1, 0x1);
        keyMap.put(KeyEvent.VK_2, 0x2);
        keyMap.put(KeyEvent.VK_3, 0x3);
        keyMap.put(KeyEvent.VK_4, 0xC);


        keyMap.put(KeyEvent.VK_Q, 0x4);
        keyMap.put(KeyEvent.VK_W, 0x5);
        keyMap.put(KeyEvent.VK_E, 0x6);
        keyMap.put(KeyEvent.VK_R, 0xD);

        keyMap.put(KeyEvent.VK_A, 0x7);
        keyMap.put(KeyEvent.VK_S, 0x8);
        keyMap.put(KeyEvent.VK_D, 0x9);
        keyMap.put(KeyEvent.VK_F, 0xE);

        keyMap.put(KeyEvent.VK_Z, 0xA);
        keyMap.put(KeyEvent.VK_X, 0x0);
        keyMap.put(KeyEvent.VK_C, 0xB);
        keyMap.put(KeyEvent.VK_V, 0xF);
    }

    public boolean isKeyPressed(int keyCode) {
        if (keyCode < 0 || keyCode > 16) return false;
        return keys[keyCode];
    }

     public boolean[] getKeys() {
        return keys;
     }

    public int getAnyPressedKey() {
        for (int i = 0; i < keys.length; i++) {
            if (keys[i]) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (keyMap.containsKey(e.getKeyCode())) {
            keys[keyMap.get(e.getKeyCode())] = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (keyMap.containsKey(e.getKeyCode())) {
            keys[keyMap.get(e.getKeyCode())] = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}



}
