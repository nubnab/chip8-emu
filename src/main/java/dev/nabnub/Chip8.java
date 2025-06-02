package dev.nabnub;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Chip8 {
    private final byte[] V = new byte[16];          //V0-VF Registers
    private short I;                                //Index Register
    private short PC;                               //Program counter
    private final short[] stack = new short[16];
    private short sp;                               //Stack pointer
    private byte delayTimer;
    private int cpuFreq;
    private long cpuCycleTime;
    private short currentInstruction;

    private Memory memory;
    private Display display;

    public Chip8(int cpuFreq) {
        initialize(cpuFreq);
    }

    private void initialize(int cpuFreq) {
        this.cpuFreq = cpuFreq;
        this.cpuCycleTime = 1_000_000_000 / cpuFreq;
        memory = new Memory();
        this.PC = (short) memory.getProgramStart();
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

    public void loadProgram(String programName) throws IOException {
        File file = new File("roms", programName + ".ch8");
        byte[] romBytes = Files.readAllBytes(file.toPath());
        System.arraycopy(romBytes, 0, memory.getMemory(), memory.getProgramStart(), romBytes.length);
    }

    public void startEmulation() {
        while (true) {
            fetch();
            decodeAndExecute();
        }
    }

    private void fetch() {
        byte mostSignificantByte = memory.getMemory()[this.PC];
        byte leastSignificantByte = memory.getMemory()[this.PC + 1];

        this.currentInstruction = (short) ((mostSignificantByte & 0xFF) << 8 | (leastSignificantByte & 0xFF));

        incrementPC();
    }

    private void decodeAndExecute() {
        int firstNibble = (currentInstruction & 0xFFFF) >>> 12;
        switch (firstNibble) {
            case 0xA:
                System.out.println("works");
                break;
            default:
        }
    }

    private void incrementPC() {
        this.PC += 0x2;
    }

    public byte getRegister(int register) {
        if(register < 0 || register > 0xF) {
            throw new IllegalArgumentException("Register out of range");
        }
        return V[register];
    }

}
