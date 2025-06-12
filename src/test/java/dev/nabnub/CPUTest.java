package dev.nabnub;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CPUTest {
    private Memory memory;
    private Display display;
    private Keyboard keyboard;
    private CPU cpu;

    @BeforeEach
    public void setUp() {
        memory = new Memory();
        display = new Display();
        keyboard = new Keyboard();
        cpu = new CPU(memory, keyboard, display);
    }

    @Test
    void display_shouldBeCleared_00E0() {
        display.togglePixel(0, 0);
        assertTrue(display.getPixels()[0][0]);
        display.clear();
        assertArrayEquals(display.getPixels(), new Display().getPixels());
    }

    @Test
    void callSubroutine_2NNN() {
        memory.getMemory()[0x200] = 0x23;
        memory.getMemory()[0x201] = 0x00;

        cpu.cycle();

        assertEquals(0x300, cpu.getPC());
        assertEquals(1, cpu.getSp());
    }

    @Test
    void returnFromSubroutine_00EE() {
        callSubroutine_2NNN();

        memory.getMemory()[0x300] = 0x00;
        memory.getMemory()[0x301] = 0xEE;

        cpu.cycle();

        assertEquals(0x202, cpu.getPC());
        assertEquals(0, cpu.getSp());


    }

}
