package dev.nabnub;

import javax.swing.*;
import java.awt.*;

public class Display extends JPanel {
    private boolean[][] pixels;
    private int scale = 10;
    private int width = 64;
    private int height = 32;

    public Display() {
        pixels = new boolean[width][height];
        setPreferredSize(new Dimension(width * scale, height * scale));
        setBackground(Color.BLACK);
    }

    public boolean togglePixel(int x, int y) {
        x = x % width;
        y = y % height;

        pixels[x][y] ^= true;
        return pixels[x][y];
    }

    public void clear() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pixels[x][y] = false;
            }
        }
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
         g.setColor(Color.WHITE);

        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                if(pixels[x][y]) {
                    g.fillRect(x * scale, y * scale, scale, scale);
                }
            }
        }
    }
}
