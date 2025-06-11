package dev.nabnub;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {

        Chip8 chip8 = new Chip8(11);
        chip8.loadProgram("5-quirks");
        chip8.startEmulation();
    }
}