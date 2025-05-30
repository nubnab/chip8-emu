package dev.nabnub;

import javax.swing.*;

public class Chip8 {
    private final byte[] V = new byte[16];          //V0-VF Registers
    private short I;                                //Index Register
    private short pc;                               //Program counter
    private final short[] stack = new short[16];
    private short sp;                               //Stack pointer
    private byte delayTimer;

    private Memory memory;
    private Display display;

    public Chip8() {
        memory = new Memory();
        this.pc = (short) memory.getProgramStart();
        loadGUI(memory);
    }

    private void loadGUI(Memory memory) {
        JFrame frame = new JFrame("Chip8 Emulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        display = new Display();
        frame.add(display);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }



}
