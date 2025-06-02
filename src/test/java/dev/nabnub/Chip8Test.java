package dev.nabnub;

import org.junit.jupiter.api.*;

class Chip8Test {

    private Chip8 chip8;

    @BeforeAll
    public static void setUpClass() {

    }

    @AfterAll
    static void tearDownClass() {

    }

    @BeforeEach
    public void setUp() {
        chip8 = new Chip8(700);
    }

    @AfterEach
    public void tearDown() {

    }

    @Test
    public void dataRegisters_shouldBeInitializedTo0() {
        Assertions.assertEquals(0x0, chip8.getRegister(0x0));
        Assertions.assertEquals(0x0, chip8.getRegister(0x1));
        Assertions.assertEquals(0x0, chip8.getRegister(0x2));
        Assertions.assertEquals(0x0, chip8.getRegister(0x3));
        Assertions.assertEquals(0x0, chip8.getRegister(0x4));
        Assertions.assertEquals(0x0, chip8.getRegister(0x5));
        Assertions.assertEquals(0x0, chip8.getRegister(0x6));
        Assertions.assertEquals(0x0, chip8.getRegister(0x7));
        Assertions.assertEquals(0x0, chip8.getRegister(0x8));
        Assertions.assertEquals(0x0, chip8.getRegister(0x9));
        Assertions.assertEquals(0x0, chip8.getRegister(0xA));
        Assertions.assertEquals(0x0, chip8.getRegister(0xB));
        Assertions.assertEquals(0x0, chip8.getRegister(0xC));
        Assertions.assertEquals(0x0, chip8.getRegister(0xD));
        Assertions.assertEquals(0x0, chip8.getRegister(0xE));
        Assertions.assertEquals(0x0, chip8.getRegister(0xF));
    }


}