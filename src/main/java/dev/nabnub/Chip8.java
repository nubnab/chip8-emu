package dev.nabnub;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Chip8 {

    private final int[] V = new int[16];          //V0-VF Registers
    private final int[] stack = new int[16];
    private int I;                                //Index Register
    private int pc;                               //Program counter
    private int sp;                               //Stack pointer
    private int opcode;                           //Stores current instruction
    private int cpuFreq;
    private long cpuCycleTime;
    //private byte delayTimer;

    private Memory memory;
    private Display display;

    public Chip8(int cpuFreq) {
        initialize(cpuFreq);
    }

    private void initialize(int cpuFreq) {
        this.cpuFreq = cpuFreq;
        this.cpuCycleTime = 1_000_000_000 / cpuFreq;
        this.pc = 0x200;
        memory = new Memory();
        loadGUI();
    }

    private void loadGUI() {
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
        memory.loadProgram(romBytes);
    }

    public void startEmulation() {
        while (true) {
            fetch();
            decodeAndExecute();
        }
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
                    case 0x00EE: //Return from subroutine this.PC = pop(); break;
                    default:
                }
                break;
            case 0x1000:
                this.pc = nnn;
                break;
            case 0x6000:
                V[x] = kk;
                break;
            case 0x7000:
                V[x] += kk;
                break;
            case 0xA000:
                I = nnn;
                break;
            case 0xD000:
                //Display
                draw(x, y, n);
                break;
            default:
        }


        //ase 0x1:
        //   //Jump, ensure PC does not go up
        //   //TODO: needs testing
        //   this.PC = (short) secondThirdAndFourthNibble;
        //   break;
        //ase 0x2:
        //   //Call subroutine at nnn
        //   //push(this.PC);
        //   //this.PC = (short) secondThirdAndFourthNibble;
        //   break;
        //ase 0x3:
        //   //Skip next if Vx = nn
        //   //TODO: needs testing
        //   //vAddressX = secondNibble;
        //   //if((V[vAddressX] & 0xFF) == (thirdAndFourthNibble & 0xFF)) {
        //   //    incrementPC();
        //   //}
        //   break;
        //ase 0x4:
        //   //Skip next if Vx != nn
        //   //TODO: needs testing
        //   //vAddressX = secondNibble;
        //   //if((V[vAddressX] & 0xFF) != (thirdAndFourthNibble & 0xFF)) {
        //   //    incrementPC();
        //   //}
        //   break;
        //ase 0x5:
        //   //Skip next if Vx = Vy
        //   //TODO: needs testing
        //   //vAddressX = secondNibble;
        //   //vAddressY = thirdNibble;
        //   //if((V[vAddressX] & 0xFF) == (V[vAddressY] & 0xFF)) {
        //   //    incrementPC();
        //   //}
        //   break;
        //ase 0x6:
        //   //set Vx
        //   vAddressX = secondNibble;
        //   V[vAddressX] = (byte) (currentInstruction & 0xFF);
        //   break;
        //ase 0x7:
        //   //Add to Vx
        //   //TODO: needs testing
        //   vAddressX = secondNibble;
        //   V[vAddressX] = (byte) (V[vAddressX] + (currentInstruction & 0xFF));
        //   break;
        //ase 0x8:
        //   switch(fourthNibble) {
        //       case 0x0:
        //           //Sets Vx = Vy
        //           //vAddressX = secondNibble;
        //           //vAddressY = thirdNibble;
        //           //V[vAddressX] = V[vAddressY];
        //           break;
        //       case 0x1:
        //           //Sets Vx = Vx OR Vy
        //           //vAddressX = secondNibble;
        //           //vAddressY = thirdNibble;
        //           //V[vAddressX] = (byte) (V[vAddressX] | V[vAddressY]);
        //           break;
        //       case 0x2:
        //           //Sets Vx = Vx AND Vy
        //           //vAddressX = secondNibble;
        //           //vAddressY = thirdNibble;
        //           //V[vAddressX] = (byte) (V[vAddressX] & V[vAddressY]);
        //           break;
        //       case 0x3:
        //           //Sets Vx = Vx XOR Vy
        //           //vAddressX = secondNibble;
        //           //vAddressY = thirdNibble;
        //           //V[vAddressX] = (byte) (V[vAddressX] ^ V[vAddressY]);
        //           break;
        //       case 0x4:
        //           //Set Vx = Vx + Vy, set VF = carry ( 1 if sum > 255 / 0xFF )
        //           //vAddressX = secondNibble;
        //           //vAddressY = thirdNibble;
        //           //int sumAddition = (V[vAddressX] & 0xFF) + (V[vAddressY] & 0xFF);
        //           //V[vAddressX] = (byte) (sumAddition & 0xFF);
        //           //V[0xF] = (byte) (sumAddition > 0xFF ? 1 : 0); //Set carry flag
        //           break;
        //       case 0x5:
        //           //Set Vx = Vx - Vy, set VF = NOT borrow ( 0 borrow, 1 otherwise )
        //           //vAddressX = secondNibble;
        //           //vAddressY = thirdNibble;

        //           //if(V[vAddressY] > V[vAddressX]) {
        //           //    V[0xF] = 0;
        //           //    V[vAddressX] += (byte) (0x100 - V[vAddressY]);
        //           //}   else {
        //           //    V[0xF] = 1;
        //           //    V[vAddressX] -= V[vAddressY];
        //           //}

        //          // V[vAddressX] = (byte) ((V[vAddressX] & 0xFF) - (V[vAddressY]) & 0xFF);

        //           break;
        //       case 0x6:
        //           //Set Vx = Vx shift right 1 bit, set VF = 1 if least significant bit = 1
        //           //vAddressX = secondNibble;
        //           //V[0xF] = (byte) (V[vAddressX] & 1);
        //           //V[vAddressX] = (byte) ((V[vAddressX] & 0xFF) >>> 1);
        //           break;
        //       case 0x7:
        //           //Set Vx = Vy - Vx, set VF = NOT borrow ( 1 borrow, 0 otherwise )
        //           //vAddressX = secondNibble;
        //           //vAddressY = thirdNibble;
        //           //int sumSubtractionYX = (V[vAddressY] & 0xFF) - (V[vAddressX] & 0xFF);
        //           //V[vAddressX] = (byte) (sumSubtractionYX);
        //           //V[0xF] = (byte) ((V[vAddressY] & 0xFF) > (V[vAddressX] & 0xFF) ? 1 : 0); //Set borrow flag
        //           break;
        //       case 0xE:
        //           //Set Vx = Vx shift left 1 bit, set VF = 1 if most significant bit = 1
        //           //vAddressX = secondNibble;
        //           //V[0xF] = (byte) ((V[vAddressX] >> 7) & 1);
        //           //V[vAddressX] = (byte) ((V[vAddressX] & 0xFF) << 1);
        //           break;
        //       default:
        //   }
        //   break;
        //ase 0x9:
        //   //Skip next if Vx != Vy
        //   //vAddressX = secondNibble;
        //   //vAddressY = thirdNibble;
        //   //if(V[vAddressX] != V[vAddressY]) {
        //   //    incrementPC();
        //   //}
        //   break;
        //ase 0xA:
        //   //Set I = nnn
        //   //test if lower 12bits are affected by signed/unsigned
        //   I = secondThirdAndFourthNibble;
        //   break;
        //ase 0xD:
        //   //Display
        //   int baseX = V[secondNibble] % 64;
        //   int baseY = V[thirdNibble] % 32;
        //   V[0xF] = 0;

        //   for(int row = 0; row < fourthNibble && (baseY + row) < 32; row++) {
        //       int spriteByte = memory.getMemory()[I + row] & 0xFF;

        //       for(int col = 0; col < 8 && (baseX + col) < 64; col++) {
        //           if((spriteByte & (0x80 >> col)) != 0) {
        //               int pixelX = (baseX + col);
        //               int pixelY = (baseY + row);
        //               boolean wasPixelOn = display.togglePixel(pixelX, pixelY);
        //               if(wasPixelOn) {
        //                   V[0xF] = 1;
        //               }
        //           }
        //       }
        //   }
        //   display.repaint();
        //   break;
        //ase 0xF:
        //   switch (thirdNibble) {
        //       case 0x1:
        //           switch (fourthNibble) {
        //               case 0xE:
        //                   //I +=  (byte) (V[secondNibble] & 0xFF);
        //                   break;
        //               default:
        //           }
        //           break;
        //       case 0x3:
        //           //int vxToDecimal = V[secondNibble] & 0xFF;
        //           //int firstDigit = vxToDecimal / 100;
        //           //int secondDigit = (vxToDecimal / 10) % 10;
        //           //int thirdDigit = vxToDecimal % 10;

        //           //memory.getMemory()[I] = (byte) firstDigit;
        //           //memory.getMemory()[I + 1] = (byte) secondDigit;
        //           //memory.getMemory()[I + 2] = (byte) thirdDigit;
        //           break;
        //       case 0x5:
        //           //short copyToPos = I;
        //           //for (int x = 0; x <= secondNibble; x++ ) {
        //           //    memory.getMemory()[copyToPos++] = V[x];
        //           //}
        //           break;
        //       case 0x6:
        //           //short readFromPos = I;
        //           //for (int x = 0; x <= secondNibble; x++ ) {
        //           //    V[x] = memory.getMemory()[readFromPos++];
        //           //}
        //           break;
        //       default:
        //   }

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

    //public byte getRegister(int register) {
    //    if(register < 0 || register > 0xF) {
    //        throw new IllegalArgumentException("Register out of range");
    //    }
    //    return V[register];
    //}



    private void push(short address) {
        if (sp > 15) {
            throw new RuntimeException("Stack overflow");
        }
        stack[++sp] = address;
    }

    //private short pop() {
    //    if (sp < 0) {
    //        throw new RuntimeException("Stack underflow");
    //    }
    //    return stack[sp--];
    //}

}
