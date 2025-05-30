package dev.nabnub;

import javax.swing.*;
import java.awt.*;

public class Display extends JPanel {
    private int scale = 10;
    private int width = 64 * scale;
    private int height = 32 * scale;

    public Dimension getPreferredSize() {
        return new Dimension(width, height);
    }

}
