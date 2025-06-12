package dev.nabnub;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Chip8 {

    private int instructionsPerFrame;

    private boolean running = true;

    private Memory memory;
    private Display display;
    private Keyboard keyboard;
    private CPU cpu;


    public Chip8(int ipf) {
        initialize(ipf);
    }

    private void initialize(int ipf) {
        this.instructionsPerFrame = ipf;

        loadGUI();
    }

    private void loadGUI() {
        JFrame frame = new JFrame("Chip8 Emulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        memory = new Memory();
        display = new Display();
        keyboard = new Keyboard();
        cpu = new CPU(memory, keyboard, display);

        frame.addKeyListener(keyboard);
        frame.add(display);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void loadProgram(String programName) throws IOException {
        File file = new File("roms", programName + ".ch8");
        byte[] romBytes = Files.readAllBytes(file.toPath());
        memory.loadProgram(romBytes);
    }

    public void startEmulation() {
        long lastFrameTime = System.nanoTime();

        while (running) {
            long currentTime = System.nanoTime();
            long elapsedTime = currentTime - lastFrameTime;

            //60Hz refresh rate
            long frameDuration = 16_666_667;
            if(elapsedTime >= frameDuration) {
                cpu.updateTimers();

                for (int i = 0; i < instructionsPerFrame; i++) {
                    cpu.cycle();
                }

                display.repaint();

                long remainingTime = frameDuration - (System.nanoTime() - currentTime);

                if(remainingTime > 0){
                    try {
                        Thread.sleep(remainingTime / 1_000_000, (int) (remainingTime % 1_000_000));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        running = false;
                    }
                }
                lastFrameTime = currentTime;
            }
        }
    }

    public Display getDisplay() {
        return display;
    }
}
