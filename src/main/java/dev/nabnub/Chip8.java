package dev.nabnub;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;

public class Chip8 {

    private final int[] V = new int[16];          //V0-VF Registers
    private final int[] stack = new int[16];
    private final Random randomNum = new Random();
    private int I;                                //Index Register
    private int pc;                               //Program counter
    private int sp;                               //Stack pointer
    private int opcode;                           //Stores current instruction
    private long cpuCycleTime;
    private int delayTimer;


    private Memory memory;
    private Keyboard keyboard;
    private Display display;

    public Chip8(int cpuFreq) {
        initialize(cpuFreq);
    }

    private void initialize(int cpuFreq) {
        this.cpuCycleTime = 1_000_000_000 / cpuFreq;
        this.pc = 0x200;
        memory = new Memory();
        loadGUI();
    }

    private void loadGUI() {
        JFrame frame = new JFrame("Chip8 Emulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        keyboard = new Keyboard();
        display = new Display();
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
        long lastCycleTime = System.nanoTime();
        long lastTimerTime = lastCycleTime;

        while (true) {
            long currentTime = System.nanoTime();

            if(currentTime - lastCycleTime >= cpuCycleTime) {
                fetch();
                decodeAndExecute();
                lastCycleTime = currentTime;
            }

            if(currentTime - lastTimerTime >= 16_666_667) {
                updateTimers();
                lastTimerTime = currentTime;
            }


        }
    }

    private void updateTimers() {
        if (delayTimer > 0) delayTimer--;
    }

    private void fetch() {
        fetchOpcode();
        incrementPC();
    }

    private void decodeAndExecute() {
        int x = (opcode & 0x0F00) >> 8;
        int y = (opcode & 0x00F0) >> 4;
        int n = opcode & 0x000F;
        int kk = opcode & 0x00FF;
        int nnn = opcode & 0x00FFF;

        switch (opcode & 0xF000) {
            case 0x0000:
                switch (opcode) {
                    case 0x00E0:
                        display.clear();
                        break;
                    case 0x00EE:
                        this.pc = stack[sp--];
                        break;
                    default:
                }
                break;
            case 0x1000:
                this.pc = nnn;
                break;
            case 0x2000:
                stack[++sp] = this.pc;
                this.pc = nnn;
                break;
            case 0x3000:
                if (V[x] == kk) incrementPC();
                break;
            case 0x4000:
                if (V[x] != kk) incrementPC();
                break;
            case 0x5000:
                if (V[x] == V[y]) incrementPC();
                break;
            case 0x6000:
                V[x] = kk;
                break;
            case 0x7000:
                int result = V[x] + kk;
                if (result >= 256) {
                    V[x] = result - 256;
                } else {
                    V[x] = result;
                }
                break;
            case 0x8000:
                switch (opcode & 0xF00F) {
                    case 0x8000:
                        V[x] = V[y];
                        break;
                    case 0x8001:
                        V[x] |= V[y];
                        V[0xF] = 0x0;
                        break;
                    case 0x8002:
                        V[x] &= V[y];
                        V[0xF] = 0x0;
                        break;
                    case 0x8003:
                        V[x] ^= V[y];
                        V[0xF] = 0x0;
                        break;
                    case 0x8004:
                        addVxVy(x, y);
                        break;
                    case 0x8005:
                        subVxVy(x, y);
                        break;
                    case 0x8006:
                        setVxVySHR(x, y);
                        break;
                    case 0x8007:
                        subVyVx(x, y);
                        break;
                    case 0x800E:
                        setVxVySHL(x, y);
                        break;
                    default:
                }
                break;
            case 0x9000:
                if (V[x] != V[y]) incrementPC();
                break;
            case 0xA000:
                I = nnn;
                break;
            case 0xB000:
                this.pc = nnn + V[0];
                break;
            case 0xC000:
                V[x] = (randomNum.nextInt(256) & kk);
                break;
            case 0xD000:
                draw(x, y, n);
                break;
            case 0xE000:
                switch (opcode & 0xF0FF) {
                    case 0xE0A1:
                        if (!keyboard.isKeyPressed(V[x])) incrementPC();
                        break;
                    case 0xE09E:
                        if (keyboard.isKeyPressed(V[x])) incrementPC();
                        break;
                    default:
                }
                break;
            case 0xF000:
                switch (opcode & 0xF0FF) {
                    case 0xF007:
                        //Investigate
                        V[x] = delayTimer & 0xFF;
                        break;
                    case 0xF015:
                        delayTimer = V[x] & 0xFF;
                        break;
                    case 0xF00A:
                        int pressedKey = keyboard.getAnyPressedKey();
                        if (pressedKey != -1) {
                            V[x] = pressedKey;
                        } else {
                            this.pc -= 2;
                        }
                        break;
                    case 0xF01E:
                        I += V[x];
                        break;
                    case 0xF029:
                        //Investigate
                        I = memory.getFONT_START() + ((V[x] & 0xF) * 5);
                        break;
                    case 0xF033:
                        memory.getMemory()[I] = V[x] / 100;
                        memory.getMemory()[I + 1] = (V[x] % 100) / 10;
                        memory.getMemory()[I + 2] = (V[x] % 100) % 10;
                        break;
                    case 0xF055:
                        for (int i = 0; i <= x; i++) {
                            memory.getMemory()[I + i] = (V[i] & 0xFF);
                        }
                        I += x + 1;
                        break;
                    case 0xF065:
                        for(int i = 0; i <= x; i++) {
                            V[i] = (memory.getMemory()[I + i] & 0xFF);
                        }
                        I += x + 1;
                        break;
                    default:
                }
                break;
            default:
                System.out.println("Unknown opcode: " + opcode);
        }
    }

    private void addVxVy(int x, int y) {
        int carry = ((V[x] + V[y]) > 0xFF) ? 1 : 0;
        V[x] = (V[x] + V[y]) & 0xFF;
        V[0xF] = carry;
    }

    private void subVxVy(int x, int y) {
        int carry = (V[y] > V[x]) ? 0 : 1;
        V[x] = (V[x] - V[y]) & 0xFF;
        V[0xF] = carry;
    }

    private void setVxVySHR(int x, int y) {
        int carry = (V[y] & 0x1) == 1 ? 1 : 0;
        V[x] = V[y] >>> 1;
        V[0xF] = carry;
    }

    private void setVxVySHL(int x, int y) {
        int carry = ((V[y] >>> 7) & 0x1) == 1 ? 1 : 0;
        V[x] = (V[y] << 1) & 0xFF;
        V[0xF] = carry;
    }

    private void subVyVx(int x, int y) {
        int carry = (V[x] > V[y]) ? 0 : 1;
        V[x] = (V[y] - V[x]) & 0xFF;
        V[0xF] = carry;
    }

    public void fetchOpcode() {
        opcode = memory.getMemory()[pc] << 8 | memory.getMemory()[pc + 1];
    }

    private void incrementPC() {
        this.pc += 0x2;
    }

    private void draw(int x, int y, int n) {
        V[0xF] = 0;
        for(int row = 0; row < n && ((V[y] % 32) + row) < 32; row++) {
            int spriteByte = memory.getMemory()[I + row];
            for(int col = 0; col < 8 && ((V[x] % 64) + col) < 64; col++) {
                if((spriteByte & (0x80 >> col)) != 0) {
                    int pixelX = ((V[x] % 64) + col);
                    int pixelY = ((V[y] % 32) + row);
                    boolean wasPixelOn = display.togglePixel(pixelX, pixelY);
                    if(wasPixelOn) {
                        V[0xF] = 1;
                    }
                }
            }
        }
        display.repaint();
    }
}
