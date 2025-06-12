package dev.nabnub;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class Opcode0xxxTest {
    private Chip8 chip8;

    @BeforeEach
    public void setUp() {
        chip8 = new Chip8(11);
    }

    @Test
    void test00E0() {
        chip8.getDisplay().togglePixel(0, 0);
        chip8.getDisplay().clear();
        assertArrayEquals(chip8.getDisplay().getPixels(), new Display().getPixels());
    }



}
