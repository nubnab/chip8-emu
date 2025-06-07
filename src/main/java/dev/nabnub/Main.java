package dev.nabnub;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {

        Chip8 chip8 = new Chip8(700);
        chip8.loadProgram("1-chip8-logo");
        //chip8.loadProgram("2-ibm");
        //chip8.loadProgram("3-corax+");
        //chip8.loadProgram("4-flags");
        //chip8.loadProgram("5-quirks");
        //chip8.loadProgram("6-keypad");
        //chip8.loadProgram("7-beep");


        chip8.startEmulation();
    }
}