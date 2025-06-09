package dev.nabnub;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {

        Chip8 chip8 = new Chip8();
        chip8.loadProgram("tank");
        chip8.startEmulation();
    }
}