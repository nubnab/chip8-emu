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
        chip8 = new Chip8();
    }

    @AfterEach
    public void tearDown() {

    }


}