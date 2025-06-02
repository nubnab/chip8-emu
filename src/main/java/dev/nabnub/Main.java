package dev.nabnub;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {

        Chip8 chip8 = new Chip8(700);
        chip8.loadProgram("ibm");
        chip8.startEmulation();
    }
}