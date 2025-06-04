package dev.nabnub;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Chip8 {
    private final byte[] V = new byte[16];          //V0-VF Registers
    private short I;                                //Index Register
    private short PC;                               //Program counter
    private short[] stack = new short[16];
    private byte sp;                               //Stack pointer
    private byte delayTimer;
    private int cpuFreq;
    private long cpuCycleTime;
    private short currentInstruction;
    private int vAddressX;
    private int vAddressY;

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
        //TODO: Refactor
        int firstNibble = (currentInstruction & 0xFFFF) >>> 12;
        int secondNibble = (currentInstruction & 0x0F00) >> 8;
        int thirdNibble = (currentInstruction & 0x00F0) >> 4;
        int fourthNibble = currentInstruction & 0x000F;
        int thirdAndFourthNibble = currentInstruction & 0x00FF;
        int secondThirdAndFourthNibble = currentInstruction & 0x0FFF;

        switch (firstNibble) {
            case 0x0:
                switch (fourthNibble) {
                    case 0x0:
                        display.clear();
                        break;
                    case 0xE:
                        //Return from subroutine
                        this.PC = pop();
                        break;
                    default:
                }
                break;
            case 0x1:
                //Jump, ensure PC does not go up
                //TODO: needs testing
                this.PC = (short) secondThirdAndFourthNibble;
                break;
            case 0x2:
                //Call subroutine at nnn
                push(this.PC);
                this.PC = (short) secondThirdAndFourthNibble;
                break;
            case 0x3:
                //Skip next if Vx = nn
                //TODO: needs testing
                vAddressX = secondNibble;
                if(V[vAddressX] == (byte)(thirdAndFourthNibble)) {
                    incrementPC();
                }
                break;
            case 0x4:
                //Skip next if Vx != nn
                //TODO: needs testing
                vAddressX = secondNibble;
                if(V[vAddressX] != (byte)(thirdAndFourthNibble)) {
                    incrementPC();
                }
                break;
            case 0x5:
                //Skip next if Vx = Vy
                //TODO: needs testing
                vAddressX = secondNibble;
                vAddressY = thirdNibble;
                if(V[vAddressX] == V[vAddressY]) {
                    incrementPC();
                }
                break;
            case 0x6:
                //set Vx
                vAddressX = secondNibble;
                V[vAddressX] = (byte) (currentInstruction & 0xFF);
                break;
            case 0x7:
                //Add to Vx
                //TODO: needs testing
                vAddressX = secondNibble;
                V[vAddressX] = (byte) (V[vAddressX] + (currentInstruction & 0xFF));
                break;
            case 0x8:
                switch(fourthNibble) {
                    case 0x0:
                        //Sets Vx = Vy
                        vAddressX = secondNibble;
                        vAddressY = thirdNibble;
                        V[vAddressX] = V[vAddressY];
                        break;
                    case 0x1:
                        // Sets Vx = Vx OR Vy
                        vAddressX = secondNibble;
                        vAddressY = thirdNibble;
                        V[vAddressX] = (byte) (V[vAddressX] | V[vAddressY]);
                        break;
                    case 0x2:
                        vAddressX = secondNibble;
                        vAddressY = thirdNibble;
                        V[vAddressX] = (byte) (V[vAddressX] & V[vAddressY]);
                        break;
                    case 0x3:
                        break;
                    case 0x4:
                        break;
                    case 0x5:
                        break;
                    case 0x6:
                        break;
                    case 0x7:
                        break;
                    case 0xE:
                    default:
                }
                break;
            case 0x9:
                //Skip next if Vx != Vy
                vAddressX = secondNibble;
                vAddressY = thirdNibble;
                if(V[vAddressX] != V[vAddressY]) {
                    incrementPC();
                }
                break;
            case 0xA:
                //Set I = nnn
                //test if lower 12bits are affected by signed/unsigned
                I = (short) secondThirdAndFourthNibble;
                break;
            case 0xD:
                //Display
                int baseX = V[secondNibble] % 64;
                int baseY = V[thirdNibble] % 32;
                V[0xF] = 0;

                for(int row = 0; row < fourthNibble && (baseY + row) < 32; row++) {
                    int spriteByte = memory.getMemory()[I + row] & 0xFF;

                    for(int col = 0; col < 8 && (baseX + col) < 64; col++) {
                        if((spriteByte & (0x80 >> col)) != 0) {
                            int pixelX = (baseX + col);
                            int pixelY = (baseY + row);
                            boolean wasPixelOn = display.togglePixel(pixelX, pixelY);
                            if(wasPixelOn) {
                                V[0xF] = 1;
                            }
                        }
                    }
                }
                display.repaint();
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

    private void push(short address) {
        if (sp > 15) {
            throw new RuntimeException("Stack overflow");
        }
        stack[++sp] = address;
    }

    private short pop() {
        if (sp < 0) {
            throw new RuntimeException("Stack underflow");
        }
        return stack[sp--];
    }

}
