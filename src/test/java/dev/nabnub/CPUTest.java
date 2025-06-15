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

    @Test
    @DisplayName("8XY1 - Vx |= Vy, VF = 0")
    void setVx_shouldBeVxOrVy_and_resetVf() {
        setUpMemory(0x200, 0x6F22);
        setUpMemory(0x202, 0x6002);
        setUpMemory(0x204, 0x6104);
        setUpMemory(0x206, 0x8011);

        runCycles(4);

        assertEquals(0x2 | 0x4, cpu.getRegistersCopy()[0]);
        assertEquals(0x0, cpu.getRegistersCopy()[0xF]);
    }

    @Test
    @DisplayName("8XY2 - Vx & Vy, VF = 0")
    void setVx_shouldBeVxAndVy_andResetVf() {
        setUpMemory(0x200, 0x6F22);
        setUpMemory(0x202, 0x6002);
        setUpMemory(0x204, 0x6104);
        setUpMemory(0x206, 0x8012);

        runCycles(4);

        assertEquals(0x2 & 0x4, cpu.getRegistersCopy()[0]);
        assertEquals(0x0, cpu.getRegistersCopy()[0xF]);
    }

    @Test
    @DisplayName("8XY3 - Vx ^ Vy, VF = 0")
    void setVx_shouldBeVxXorVy_andResetVf() {
        setUpMemory(0x200, 0x6F22);
        setUpMemory(0x202, 0x6002);
        setUpMemory(0x204, 0x6104);
        setUpMemory(0x206, 0x8013);

        runCycles(4);

        assertEquals(0x2 ^ 0x4, cpu.getRegistersCopy()[0]);
        assertEquals(0x0, cpu.getRegistersCopy()[0xF]);
    }

    @Test
    @DisplayName("8XY4 - Vx += Vy, VF = 0")
    void setVx_shouldBeVxPlusVy_noCarry() {
        setUpMemory(0x200, 0x6022);
        setUpMemory(0x202, 0x6155);
        setUpMemory(0x204, 0x8014);

        runCycles(3);

        assertEquals(0x22 + 0x55, cpu.getRegistersCopy()[0]);
        assertEquals(0x0, cpu.getRegistersCopy()[0xF]);
    }


    @Test
    @DisplayName("8XY4 - Vx += Vy, VF = 1")
    void setVx_shouldBeVxPlusVy_carry() {
        setUpMemory(0x200, 0x60BB);
        setUpMemory(0x202, 0x61CC);
        setUpMemory(0x204, 0x8014);

        runCycles(3);

        assertEquals((0xBB + 0xCC) & 0xFF, cpu.getRegistersCopy()[0]);
        assertEquals(0x1, cpu.getRegistersCopy()[0xF]);
    }

    @Test
    @DisplayName("8XY4 - Vx += Vy, VF = first param, VF = 0")
    void setVx_shouldBeVxPlusVy_firstParamVf_noCarry() {
        setUpMemory(0x200, 0x6F22);
        setUpMemory(0x202, 0x6155);
        setUpMemory(0x204, 0x8F14);

        runCycles(3);

        assertEquals(0x0, cpu.getRegistersCopy()[0xF]);
    }

    @Test
    @DisplayName("8XY4 - Vx += Vy, VF = first param, VF = 1")
    void setVx_shouldBeVxPlusVy_firstParamVf_carry() {
        setUpMemory(0x200, 0x6FBB);
        setUpMemory(0x202, 0x61CC);
        setUpMemory(0x204, 0x8F14);

        runCycles(3);

        assertEquals(0x1, cpu.getRegistersCopy()[0xF]);
    }


    @Test
    @DisplayName("8XY4 - Vx += Vy, Zero addition, VF = 0")
    void setVx_shouldBeVxPlusVy_zeroAddition_noCarry() {
        setUpMemory(0x200, 0x6000);
        setUpMemory(0x202, 0x6100);
        setUpMemory(0x204, 0x8014);

        runCycles(3);

        assertEquals(0x0, cpu.getRegistersCopy()[0]);
        assertEquals(0x0, cpu.getRegistersCopy()[0xF]);
    }

    @Test
    @DisplayName("8XY4 - Vx += Vy, Max addition, VF = 1")
    void setVx_shouldBeVxPlusVy_maxAddition_carry() {
        setUpMemory(0x200, 0x60FF);
        setUpMemory(0x202, 0x61FF);
        setUpMemory(0x204, 0x8014);

        runCycles(3);

        assertEquals((0xFF * 2) & 0xFF, cpu.getRegistersCopy()[0]);
        assertEquals(0x1, cpu.getRegistersCopy()[0xF]);
    }

    @Test
    @DisplayName("8XY5 - Vx -= Vy, VF = 1")
    void setVx_shouldBeVxMinusVy_noUnderflow() {
        setUpMemory(0x200, 0x6044);
        setUpMemory(0x202, 0x6133);
        setUpMemory(0x204, 0x8015);

        runCycles(3);

        assertEquals(0x44 - 0x33, cpu.getRegistersCopy()[0]);
        assertEquals(0x1, cpu.getRegistersCopy()[0xF]);
    }

    @Test
    @DisplayName("8XY5 - Vx -= Vy, VF = 0")
    void setVx_shouldBeVxMinusVy_underflow() {
        setUpMemory(0x200, 0x6044);
        setUpMemory(0x202, 0x6155);
        setUpMemory(0x204, 0x8015);

        runCycles(3);

        assertEquals((0x44 - 0x55) & 0xFF, cpu.getRegistersCopy()[0]);
        assertEquals(0x0, cpu.getRegistersCopy()[0xF]);
    }

    @Test
    @DisplayName("8XY5 - Vx -= Vy, VF = param, VF = 1")
    void setVx_shouldBeVxMinusVy_VFParam_noUnderflow() {
        setUpMemory(0x200, 0x6F44);
        setUpMemory(0x202, 0x6133);
        setUpMemory(0x204, 0x8F15);

        runCycles(3);

        assertEquals(0x1, cpu.getRegistersCopy()[0xF]);
    }

    @Test
    @DisplayName("8XY5 - Vx -= Vy, VF = param, VF = 0")
    void setVx_shouldBeVxMinusVy_VFParam_underflow() {
        setUpMemory(0x200, 0x6F44);
        setUpMemory(0x202, 0x6155);
        setUpMemory(0x204, 0x8F15);

        runCycles(3);

        assertEquals(0x0, cpu.getRegistersCopy()[0xF]);
    }

    @Test
    @DisplayName("8XY5 - Vx -= Vy, Zero subtraction")
    void setVx_shouldBeVxMinusVy_zeroSubtraction() {
        setUpMemory(0x200, 0x6000);
        setUpMemory(0x202, 0x6100);
        setUpMemory(0x204, 0x8015);

        runCycles(3);

        assertEquals(0x0, cpu.getRegistersCopy()[0]);
        assertEquals(0x1, cpu.getRegistersCopy()[0xF]);
    }

    @Test
    @DisplayName("8XY5 - Vx -= Vy, Max subtraction")
    void setVx_shouldBeVxMinusVy_maxSubtraction() {
        setUpMemory(0x200, 0x60FF);
        setUpMemory(0x202, 0x61FF);
        setUpMemory(0x204, 0x8015);

        runCycles(3);

        assertEquals(0x0, cpu.getRegistersCopy()[0]);
        assertEquals(0x1, cpu.getRegistersCopy()[0xF]);
    }

    @Test
    @DisplayName("8XY6 - Vx = Vy >> 1, VF = 1")
    void setVx_shouldBeVyShiftRight_shiftedIsVf() {
        setUpMemory(0x200, 0x6125);
        setUpMemory(0x202, 0x8016);

        runCycles(2);

        assertEquals((0x25) >> 1, cpu.getRegistersCopy()[0]);
        assertEquals(0x1, cpu.getRegistersCopy()[0xF]);
    }

    @Test
    @DisplayName("8XY6 - Vx = Vy >> 1, VF First Param, VF = 1")
    void setVx_shouldBeVyShiftRight_shiftedIsVf_VFFirstParam() {
        setUpMemory(0x200, 0x6125);
        setUpMemory(0x202, 0x8F16);

        runCycles(2);

        assertEquals(0x1, cpu.getRegistersCopy()[0xF]);
    }

    @Test
    @DisplayName("8XY6 - Vx = Vy >> 1, VF Second Param, VF = 1")
    void setVx_shouldBeVyShiftRight_shiftedIsVf_VFSecondParam() {
        setUpMemory(0x200, 0x6F25);
        setUpMemory(0x202, 0x80F6);

        runCycles(2);

        assertEquals((0x25) >> 1, cpu.getRegistersCopy()[0]);
        assertEquals(0x1, cpu.getRegistersCopy()[0xF]);
    }


    @Test
    @DisplayName("8XY7 - Vx = Vy - Vx, VF = 1")
    void setVx_shouldBeVyMinusVx_noUnderflow() {
        setUpMemory(0x200, 0x6022);
        setUpMemory(0x202, 0x6155);
        setUpMemory(0x204, 0x8017);

        runCycles(3);

        assertEquals(0x55 - 0x22, cpu.getRegistersCopy()[0]);
        assertEquals(0x1, cpu.getRegistersCopy()[0xF]);
    }

    @Test
    @DisplayName("8XY7 - Vx = Vy - Vx, VF = 0")
    void setVx_shouldBeVyMinusVx_underflow() {
        setUpMemory(0x200, 0x6066);
        setUpMemory(0x202, 0x6155);
        setUpMemory(0x204, 0x8017);

        runCycles(3);

        assertEquals((0x55 - 0x66) & 0xFF, cpu.getRegistersCopy()[0]);
        assertEquals(0x0, cpu.getRegistersCopy()[0xF]);
    };
    @Test
    @DisplayName("8XY7 - Vx = Vy - Vx, VF = param VF = 1")
    void setVx_shouldBeVyMinusVx_VFParam_noUnderflow() {
        setUpMemory(0x200, 0x6022);
        setUpMemory(0x202, 0x6F55);
        setUpMemory(0x204, 0x80F7);

        runCycles(3);

        assertEquals((0x55 - 0x22) & 0xFF, cpu.getRegistersCopy()[0]);
        assertEquals(0x1, cpu.getRegistersCopy()[0xF]);
    }

    @Test
    @DisplayName("8XY7 - Vx = Vy - Vx, VF = param VF = 0")
    void setVx_shouldBeVyMinusVx_VFParam_underflow() {
        setUpMemory(0x200, 0x6066);
        setUpMemory(0x202, 0x6F55);
        setUpMemory(0x204, 0x80F7);

        runCycles(3);

        assertEquals((0x55 - 0x66) & 0xFF, cpu.getRegistersCopy()[0]);
        assertEquals(0x0, cpu.getRegistersCopy()[0xF]);
    }

    @Test
    @DisplayName("8XY7 - Vx = Vy - Vx, Zero subtraction")
    void setVx_shouldBeVyMinusVx_zeroSubtraction() {
        setUpMemory(0x200, 0x6000);
        setUpMemory(0x202, 0x6100);
        setUpMemory(0x204, 0x8017);

        runCycles(3);

        assertEquals(0x0, cpu.getRegistersCopy()[0]);
        assertEquals(0x1, cpu.getRegistersCopy()[0xF]);
    }

    @Test
    @DisplayName("8XY7 - Vx = Vy - Vx, Max subtraction")
    void setVx_shouldBeVyMinusVx_maxSubtraction() {
        setUpMemory(0x200, 0x60FF);
        setUpMemory(0x202, 0x61FF);
        setUpMemory(0x204, 0x8017);

        runCycles(3);

        assertEquals(0x0, cpu.getRegistersCopy()[0]);
        assertEquals(0x1, cpu.getRegistersCopy()[0xF]);
    }

    @Test
    @DisplayName("8XYE - Vx = Vy << 1, VF = 0")
    void setVx_shouldBeVyShiftLeft_shiftedIsVf_VfZero() {
        setUpMemory(0x200, 0x6125);
        setUpMemory(0x202, 0x801E);

        runCycles(2);

        assertEquals((0x25 << 1) & 0xFF, cpu.getRegistersCopy()[0]);
        assertEquals(0x0, cpu.getRegistersCopy()[0xF]);
    }

    @Test
    @DisplayName("8XYE - Vx = Vy << 1, VF = 1")
    void setVx_shouldBeVyShiftLeft_shiftedIsVf_VfOne() {
        setUpMemory(0x200, 0x61C1);
        setUpMemory(0x202, 0x801E);

        runCycles(2);

        assertEquals((0xC1 << 1) & 0xFF, cpu.getRegistersCopy()[0]);
        assertEquals(0x1, cpu.getRegistersCopy()[0xF]);
    }

    @Test
    @DisplayName("8XYE - Vx = Vy << 1, VF First Param, VF = 0")
    void setVx_shouldBeVyShiftLeft_shiftedIsVf_VFFirstParam() {
        setUpMemory(0x200, 0x6125);
        setUpMemory(0x202, 0x8F1E);

        runCycles(2);

        assertEquals(0x0, cpu.getRegistersCopy()[0xF]);
    }

    @Test
    @DisplayName("8XYE - Vx = Vy << 1, VF Second Param, VF = 0")
    void setVx_shouldBeVyShiftLeft_shiftedIsVf_VFSecondParam() {
        setUpMemory(0x200, 0x6F25);
        setUpMemory(0x202, 0x80FE);

        runCycles(2);

        assertEquals(0x0, cpu.getRegistersCopy()[0xF]);
    }

    @Test
    @DisplayName("9XY0 - Skip if Vx != Vy")
    void skipIfVxNotEqualsVy_shouldSkip() {
        setUpMemory(0x200, 0x6025);
        setUpMemory(0x202, 0x6130);
        setUpMemory(0x204, 0x901E);

        runCycles(3);

        assertEquals(0x208, cpu.getPC());
    }

    @Test
    @DisplayName("9XY0 - Skip if Vx != Vy")
    void skipIfVxNotEqualsVy_shouldNotSkip() {
        setUpMemory(0x200, 0x6025);
        setUpMemory(0x202, 0x6125);
        setUpMemory(0x204, 0x901E);

        runCycles(3);

        assertEquals(0x206, cpu.getPC());
    }




}
