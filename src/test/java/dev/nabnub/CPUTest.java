package dev.nabnub;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

    private void setUpMemory(int memStart, int opcode) {
        memory.getMemory()[memStart] = (opcode & 0xFF00) >> 8;
        memory.getMemory()[memStart + 1] = opcode & 0x00FF;
    }

    private void runCycles(int n) {
        for (int i = 0; i < n; i++) {
            cpu.cycle();
        }
    }

    @Test
    @DisplayName("00E0 - Display clear")
    void display_shouldBeCleared() {
        display.togglePixel(0, 0);
        assertTrue(display.getPixels()[0][0]);
        display.clear();
        assertArrayEquals(display.getPixels(), new Display().getPixels());
    }

    @Test
    @DisplayName("00EE - Return from subroutine")
    void returnFromSubroutine() {
        setUpMemory(0x200, 0x2300);
        setUpMemory(0x300, 0x00EE);

        cpu.cycle();

        assertEquals(0x300, cpu.getPC());
        assertEquals(1, cpu.getSp());

        cpu.cycle();

        assertEquals(0x202, cpu.getPC());
        assertEquals(0, cpu.getSp());
    }

    @Test
    @DisplayName("1NNN - Unconditional jump to NNN")
    void pc_shouldBe_NNN() {
        setUpMemory(0x200, 0x1234);

        cpu.cycle();

        assertEquals(0x234, cpu.getPC());
    }

    @Test
    @DisplayName("2NNN - Conditional jump to NNN")
    void callSubroutine() {
        setUpMemory(0x200, 0x2300);

        cpu.cycle();

        assertEquals(0x300, cpu.getPC());
        assertEquals(1, cpu.getSp());
    }

    @Test
    @DisplayName("3XKK - Vx != kk")
    void skipIfVxEqualsKK_shouldNotSkip() {
        setUpMemory(0x200, 0x6044);
        setUpMemory(0x202, 0x3033);

        runCycles(2);

        assertEquals(0x204, cpu.getPC());
    }

    @Test
    @DisplayName("3XKK - Vx == kk")
    void skipIfVxEqualsKK_shouldSkip() {
        setUpMemory(0x200, 0x6033);
        setUpMemory(0x202, 0x3033);

        runCycles(2);

        assertEquals(0x206, cpu.getPC());
    }

    @Test
    @DisplayName("4XKK - Vx == kk")
    void skipIfVxNotEqualsKK_shouldNotSkip() {
        setUpMemory(0x200, 0x6033);
        setUpMemory(0x202, 0x4033);

        runCycles(2);

        assertEquals(0x204, cpu.getPC());
    }

    @Test
    @DisplayName("4XKK - Vx != kk")
    void skipIfVxNotEqualsKK_shouldSkip() {
        setUpMemory(0x200, 0x6033);
        setUpMemory(0x202, 0x4044);

        runCycles(2);

        assertEquals(0x206, cpu.getPC());
    }

    @Test
    @DisplayName("5XYO - Vx != Vy")
    void skipIfVxEqualsVy_shouldNotSkip() {
        setUpMemory(0x200, 0x6011);
        setUpMemory(0x202, 0x6122);
        setUpMemory(0x204, 0x5010);

        runCycles(3);

        assertEquals(0x206, cpu.getPC());
    }

    @Test
    @DisplayName("5XYO - Vx == Vy")
    void skipIfVxEqualsVy_shouldSkip() {
        setUpMemory(0x200, 0x6022);
        setUpMemory(0x202, 0x6122);
        setUpMemory(0x204, 0x5010);

        runCycles(3);

        assertEquals(0x208, cpu.getPC());
    }

    @Test
    @DisplayName("6XKK - Vx = kk")
    void setVx_shouldBeKK() {
        setUpMemory(0x200, 0x6044);

        cpu.cycle();

        assertEquals(0x44, cpu.getRegistersCopy()[0]);
    }

    @Test
    @DisplayName("7XNN - Vx += NN")
    void setVx_shouldBeVxPlusNN() {
        setUpMemory(0x200, 0x7064);
        setUpMemory(0x202, 0x7064);

        cpu.cycle();

        assertEquals(0x64, cpu.getRegistersCopy()[0]);

        cpu.cycle();

        assertEquals(0xC8, cpu.getRegistersCopy()[0]);
    }

    @Test
    @DisplayName("8XY0 - Vx = Vy")
    void setVx_shouldBeVy() {
        setUpMemory(0x200, 0x6122);
        setUpMemory(0x202, 0x8010);

        runCycles(2);

        assertEquals(0x22, cpu.getRegistersCopy()[0]);
    }










}
