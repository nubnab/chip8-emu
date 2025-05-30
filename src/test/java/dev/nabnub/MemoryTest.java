package dev.nabnub;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class MemoryTest {

    private Memory mem;

    @BeforeAll
    public static void setUpClass() {

    }

    @AfterAll static void tearDownClass() {

    }

    @BeforeEach
    public void setUp() {
        this.mem = new Memory();
    }

    @AfterEach
    public void tearDown() {

    }

    @Test
    public void pcInitialized_ShouldBe0x200() {
        Assertions.assertEquals(0x200, mem.getPc());
    }



}