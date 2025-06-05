package dev.nabnub;

public class Memory {
    private final byte[] memory;                    //Chip 8 memory
    private final int MEMORY_SIZE = 4096;
    private final int PROGRAM_START = 0x200;
    private final int FONT_START = 0x050;

    private static final byte[] FONT = {
            (byte) 0xF0, (byte) 0x90, (byte) 0x90, (byte) 0x90, (byte) 0xF0, // 0
            (byte) 0x20, (byte) 0x60, (byte) 0x20, (byte) 0x20, (byte) 0x70, // 1
            (byte) 0xF0, (byte) 0x10, (byte) 0xF0, (byte) 0x80, (byte) 0xF0, // 2
            (byte) 0xF0, (byte) 0x10, (byte) 0xF0, (byte) 0x10, (byte) 0xF0, // 3
            (byte) 0x90, (byte) 0x90, (byte) 0xF0, (byte) 0x10, (byte) 0x10, // 4
            (byte) 0xF0, (byte) 0x80, (byte) 0xF0, (byte) 0x10, (byte) 0xF0, // 5
            (byte) 0xF0, (byte) 0x80, (byte) 0xF0, (byte) 0x90, (byte) 0xF0, // 6
            (byte) 0xF0, (byte) 0x10, (byte) 0x20, (byte) 0x40, (byte) 0x40, // 7
            (byte) 0xF0, (byte) 0x90, (byte) 0xF0, (byte) 0x90, (byte) 0xF0, // 8
            (byte) 0xF0, (byte) 0x90, (byte) 0xF0, (byte) 0x10, (byte) 0xF0, // 9
            (byte) 0xF0, (byte) 0x90, (byte) 0xF0, (byte) 0x90, (byte) 0x90, // A
            (byte) 0xE0, (byte) 0x90, (byte) 0xE0, (byte) 0x90, (byte) 0xE0, // B
            (byte) 0xF0, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0xF0, // C
            (byte) 0xE0, (byte) 0x90, (byte) 0x90, (byte) 0x90, (byte) 0xE0, // D
            (byte) 0xF0, (byte) 0x80, (byte) 0xF0, (byte) 0x80, (byte) 0xF0, // E
            (byte) 0xF0, (byte) 0x80, (byte) 0xF0, (byte) 0x80, (byte) 0x80  // F
    };

    public Memory() {
        memory = new byte[MEMORY_SIZE];
        loadFonts();
    }

    public int getProgramStart() {
        return PROGRAM_START;
    }

    private void loadFonts() {
        System.arraycopy(FONT, 0, memory, FONT_START, FONT.length);
    }

    public byte[] getMemory() {
        return memory;
    }
    

}
