package dev.nabnub;

import java.util.Arrays;
import java.util.Random;

public class CPU {

    private int[] v;                              //V0-VF Registers
    private int[] stack;
    private final Random random;
    private int index;                            //Index Register
    private int pc;                               //Program counter
    private int sp;                               //Stack pointer
    private int opcode;                           //Stores current instruction
    private int delay;

    private final Memory memory;
    private final Display display;
    private final Keyboard keyboard;

    public CPU(Memory memory, Keyboard keyboard, Display display) {
        this.random = new Random();
        this.memory = memory;
        this.display = display;
        this.keyboard = keyboard;

        reset();
    }

    public void reset() {
        v = new int[16];
        stack = new int[16];
        index = 0;
        pc = 0x200;
        sp = 0;
        delay = 0;
    }

    public void cycle() {
        fetch();
        execute(opcode);
    }

    public void updateTimers() {
        if (delay > 0) {
            delay--;
        }
    }

    private void fetch() {
        opcode = memory.getMemory()[pc] << 8 | memory.getMemory()[pc + 1];
        incrementPC();
    }

    private void execute(int opcode) {
        int x = (opcode & 0x0F00) >> 8;
        int y = (opcode & 0x00F0) >> 4;
        int n = opcode & 0x000F;
        int kk = opcode & 0x00FF;
        int nnn = opcode & 0x00FFF;

        switch (opcode & 0xF000) {
            case 0x0000:
                decode0000(opcode);
                break;
            case 0x1000:
                jumpToNNN(nnn);
                break;
            case 0x2000:
                callSubroutine(nnn);
                break;
            case 0x3000:
                skipIfVxKK(x, kk);
                break;
            case 0x4000:
                skipIfVxNotKK(x, kk);
                break;
            case 0x5000:
                skipIfVxVy(x, y);
                break;
            case 0x6000:
                setVxKK(x, kk);
                break;
            case 0x7000:
                setVxPlusKK(x, kk);
                break;
            case 0x8000:
                decode8000(opcode, x, y);
                break;
            case 0x9000:
                skipIfVxNotVy(x, y);
                break;
            case 0xA000:
                setINNN(nnn);
                break;
            case 0xB000:
                skipToNNNPlusV0(nnn);
                break;
            case 0xC000:
                setVxRandomAndKK(x, kk);
                break;
            case 0xD000:
                draw(x, y, n);
                break;
            case 0xE000:
                decodeE000(opcode, x);
                break;
            case 0xF000:
                decodeF000(opcode, x);
                break;
            default:
                System.out.println("Unknown opcode: " + opcode);
        }
    }

    private void decode0000(int opcode) {
        switch (opcode) {
            //No op
            case 0x0000:
                break;
            case 0x00E0:
                display.clear();
                break;
            case 0x00EE:
                returnFromSubroutine();
                break;
            default:
        }
    }

    private void jumpToNNN(int nnn) {
        this.pc = nnn;
    }

    private void callSubroutine(int nnn) {
        stack[++sp] = this.pc;
        this.pc = nnn;
    }

    private void skipIfVxKK(int x, int kk) {
        if (v[x] == kk) {
            incrementPC();
        }
    }

    private void skipIfVxNotKK(int x, int kk) {
        if (v[x] != kk) {
            incrementPC();
        }
    }

    private void skipIfVxVy(int x, int y) {
        if (v[x] == v[y]) {
            incrementPC();
        }
    }

    private void setVxKK(int x, int kk) {
        v[x] = kk;
    }

    private void setVxPlusKK(int x, int kk) {
        int result = v[x] + kk;
        if (result >= 256) {
            v[x] = result - 256;
        } else {
            v[x] = result;
        }
    }

    private void decode8000(int opcode, int x, int y) {
        switch (opcode & 0xF00F) {
            case 0x8000:
                setVxVy(x, y);
                break;
            case 0x8001:
                setVxOrVy(x, y);
                break;
            case 0x8002:
                setVxAndVy(x, y);
                break;
            case 0x8003:
                setVxXorVy(x, y);
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
    }

    private void skipIfVxNotVy(int x, int y) {
        if (v[x] != v[y]) {
            incrementPC();
        }
    }

    private void setINNN(int nnn) {
        index = nnn;
    }

    private void skipToNNNPlusV0(int nnn) {
        this.pc = v[0] + nnn;
    }

    private void setVxRandomAndKK(int x, int kk) {
        v[x] = (random.nextInt(256) & kk);
    }

    private void draw(int x, int y, int n) {
        v[0xF] = 0;

        for(int yline = 0; yline < n; yline++) {
            int spriteByte = memory.getMemory()[index + yline];
            for(int xline = 0; xline < 8; xline++) {
                if((spriteByte & (0x80 >> xline)) != 0) {
                    int pixelX = ((v[x] + xline) % 64);
                    int pixelY = ((v[y] + yline) % 32);
                    boolean wasPixelOn = display.togglePixel(pixelX, pixelY);
                    if(!wasPixelOn) {
                        v[0xF] = 1;
                    }
                }
            }
        }
    }

    private void decodeE000(int opcode, int x) {
        switch (opcode & 0xF0FF) {
            case 0xE09E:
                skipIfKeyPressed(x);
                break;
            case 0xE0A1:
                skipIfKeyNotPressed(x);
                break;
            default:
        }
    }

    private void decodeF000(int opcode, int x) {
        switch (opcode & 0xF0FF) {
            case 0xF007:
                setVxDt(x);
                break;
            case 0xF015:
                setDtVx(x);
                break;
            case 0xF00A:
                waitForKeyPressAndRelease(x);
                break;
            case 0xF01E:
                setIPlusVx(x);
                break;
            case 0xF029:
                setISprite(x);
                break;
            case 0xF033:
                setIVxBCD(x);
                break;
            case 0xF055:
                setIV0Vx(x);
                break;
            case 0xF065:
                readV0VxI(x);
                break;
            default:
        }
    }

    private void returnFromSubroutine() {
        this.pc = stack[sp--];
    }

    private void setVxVy(int x, int y) {
        v[x] = v[y];
    }

    private void setVxOrVy(int x, int y) {
        v[x] |= v[y];
        v[0xF] = 0x0;
    }

    private void setVxAndVy(int x, int y) {
        v[x] &= v[y];
        v[0xF] = 0x0;
    }

    private void setVxXorVy(int x, int y) {
        v[x] ^= v[y];
        v[0xF] = 0x0;
    }

    private void addVxVy(int x, int y) {
        int carry = ((v[x] + v[y]) > 0xFF) ? 1 : 0;
        v[x] = (v[x] + v[y]) & 0xFF;
        v[0xF] = carry;
    }

    private void subVxVy(int x, int y) {
        int carry = (v[y] > v[x]) ? 0 : 1;
        v[x] = (v[x] - v[y]) & 0xFF;
        v[0xF] = carry;
    }

    private void setVxVySHR(int x, int y) {
        int carry = (v[y] & 0x1) == 1 ? 1 : 0;
        v[x] = v[y] >>> 1;
        v[0xF] = carry;
    }

    private void subVyVx(int x, int y) {
        int carry = (v[x] > v[y]) ? 0 : 1;
        v[x] = (v[y] - v[x]) & 0xFF;
        v[0xF] = carry;
    }

    private void setVxVySHL(int x, int y) {
        int carry = ((v[y] >>> 7) & 0x1) == 1 ? 1 : 0;
        v[x] = (v[y] << 1) & 0xFF;
        v[0xF] = carry;
    }

    private void skipIfKeyPressed(int x) {
        if (keyboard.isKeyPressed(v[x])) {
            incrementPC();
        }
    }

    private void skipIfKeyNotPressed(int x) {
        if (!keyboard.isKeyPressed(v[x])) {
            incrementPC();
        }
    }

    private void setVxDt(int x) {
        v[x] = delay;
    }

    private void setDtVx(int x) {
        delay = v[x];
    }

    private void waitForKeyPressAndRelease(int x) {
        int pressedKey = keyboard.getAnyPressedKey();
        if (pressedKey != -1) {
            v[x] = pressedKey;
        } else {
            this.pc -= 2;
        }
    }

    private void setIPlusVx(int x) {
        index += v[x];
    }

    private void setISprite(int x) {
        index = memory.getFONT_START() + (v[x] * 5);
    }

    private void setIVxBCD(int x) {
        memory.getMemory()[index] = v[x] / 100;
        memory.getMemory()[index + 1] = (v[x] / 10) % 10;
        memory.getMemory()[index + 2] = (v[x] % 10);
    }

    private void setIV0Vx(int x) {
        for (int i = 0; i <= x; i++) {
            memory.getMemory()[index + i] = (v[i] & 0xFF);
        }
        index += x + 1;
    }

    private void readV0VxI(int x) {
        for(int i = 0; i <= x; i++) {
            v[i] = (memory.getMemory()[index + i] & 0xFF);
        }
        index += x + 1;
    }

    private void incrementPC() {
        this.pc += 0x2;
    }

    protected int getPC() {
        return pc;
    }

    protected int getSp() {
        return sp;
    }

    protected int[] getStackCopy() {
        return Arrays.copyOf(this.stack, this.stack.length);
    }

    protected int[] getRegistersCopy() {
        return Arrays.copyOf(this.v, this.v.length);
    }

}
