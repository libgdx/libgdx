package com.badlogic.chip8;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple "assembler" that helps write small Chip8 programs. For each instruction of the
 * Chip-8 there is a method that will write that instruction to the memory of a {@link Chip8}
 * instance. You can set named jump labels as well to make calculating jump addresses easier.
 * @author mzechner
 *
 */
public class Chip8Assembler {
	/** the chip8 instance to work with **/
	private final Chip8 chip8;
	/** the address to write the next instruction to **/
	private int addr = 0x200;
	/** map of lable name -> address **/
	private Map<String, Integer> labels = new HashMap<String, Integer>();
	
	/**
	 * Creates a new Chip8Assembler that writes instructions
	 * to the given {@link Chip8} instance's memory.
	 * @param chip8
	 */
	public Chip8Assembler(Chip8 chip8) {
		this.chip8 = chip8;
	}
	
	/**
	 * Helper, writes the two bytes to memory
	 * @param opcode
	 */
	private void inst(int highByte, int lowByte) {
		chip8.mem[addr++] = (byte)(highByte & 0xff);
		chip8.mem[addr++] = (byte)(lowByte & 0xff);
	}
	
	/**
	 * Helper, writes an instruction of the form xNNN to memory, 
	 * where x is the opcode and NNN is the address.
	 * @param opcode
	 * @param addr
	 */
	private void instAddr(int opcode, int addr) {
		chip8.mem[this.addr++] = (byte)(((opcode & 0xf) << 4) | ((addr & 0xf00) >>> 8));
		chip8.mem[this.addr++] = (byte)((addr & 0xff));
	}
	
	/**
	 * Helper, writes an instruction of the form xVxVyz to memory,
	 * where x is the opcode, Vx is the first register, Vy is the
	 * second register and z is the opcode suffix
	 * @param opcode
	 * @param vx
	 * @param vy
	 * @param opcodeSuffix
	 */
	private void instRegReg(int opcode, int vx, int vy, int opcodeSuffix) {
		chip8.mem[addr++] = (byte)(((opcode & 0xf) << 4) | (vx & 0xf));
		chip8.mem[addr++] = (byte)(((vy & 0xf) << 4) | (opcodeSuffix & 0xf));
	}
	
	/**
	 * Helper, writes an instruction of the form xVxkk to memory,
	 * where x is the opcode, Vx is the first register, kk is a value
	 * 
	 * @param opcode
	 * @param vx
	 * @param vy
	 */
	private void instRegVal(int opcode, int vx, int kk) {
		chip8.mem[addr++] = (byte)(((opcode & 0xf) << 4) | (vx & 0xf));
		chip8.mem[addr++] = (byte)(kk & 0xf);
	}
	
	/**
	 * Registers a label for the current write address 
	 * @param label
	 */
	public void label(String label) {
		if(labels.containsKey(label)) throw new RuntimeException("label '" + label + "' already defined");
		labels.put(label, addr);
	}
	
	/**
	 * 0nnn - SYS addr
	 * 
	 * Jump to a machine code routine at addr.
	 * @param addr the address to jump to
	 */
	public void sys(int addr) {
		instAddr(0, addr);
	}
	
	/**
	 * 00E0 - CLS
	 * 
	 * Clear the display.
	 */
	public void cls() {
		inst(0, 0xe0);
	}
	
	/**
	 * 00EE - RET
	 * 
	 * Return from a subroutine.
	 */
	public void ret() {
		inst(0, 0xee);
	}
	
	/**
	 * 1nnn - JP addr
	 * 
	 * Jump to location addr
	 * @param addr
	 */
	public void jp(int addr) {
		instAddr(1, addr);
	}
	
	/**
	 * 2nnn - CALL addr
	 * 
	 * Call subroutine at addr.
	 * @param addr
	 */
	public void call(int addr) {
		inst(2, addr);
	}
	
	/**
	 * 3xkk - SE Vx, byte
	 * 
	 * Skip next instruction if Vx = kk.
	 * @param vx
	 * @param kk
	 */
	public void seRegVal(int vx, int kk) {
		instRegVal(3, vx, kk);
	}
	
	/**
	 * 4xkk - SNE Vx, byte
	 * 
	 * Skip next instruction if Vx != kk.
	 * @param vx
	 * @param kk
	 */
	public void sneRegVal(int vx, int kk) {
		instRegVal(3, vx, kk);
	}

	/**
	 * 5xy0 - SE Vx, Vy
	 * 
	 * Skip next instruction if Vx = Vy.
	 * @param vx
	 * @param kk
	 */
	public void seRegReg(int vx, int vy) {
		instRegReg(5, vx, vy, 0);
	}
	
	/**
	 * 6xkk - LD Vx, byte
	 * 
	 * Set Vx = kk
	 * @param vx
	 * @param kk
	 */
	public void ldRegVal(int vx, int kk) {
		instRegVal(6, vx, kk);
	}
	
	/**
	 * 7xkk - ADD Vx, byte
	 * 
	 * Set Vx = Vx + kk.
	 * @param vx
	 * @param kk
	 */
	public void addRegVal(int vx, int kk) {
		instRegVal(7, vx, kk);
	}
	
	/**
	 * 8xy0 - LD Vx, Vy
	 * 
	 * Set Vx = Vy.
	 * @param vx
	 * @param vy
	 */
	public void ldRegReg(int vx, int vy) {
		instRegReg(8, vx, vy, 0);
	}
	
	/**
	 * 8xy1 - OR Vx, Vy
	 * 
	 * Set Vx = Vx OR Vy.
	 * @param vx
	 * @param vy
	 */
	public void orRegReg(int vx, int vy) {
		instRegReg(8, vx, vy, 1);
	}
	
	/**
	 * 8xy2 - AND Vx, Vy
	 * 
	 * Set Vx = Vx AND Vy.
	 * @param vx
	 * @param vy
	 */
	public void andRegReg(int vx, int vy) {
		instRegReg(8, vx, vy, 2);
	}
	
	/**
	 * 8xy3 - XOR Vx, Vy
	 * 
	 * Set Vx = Vx XOR Vy.
	 * @param vx
	 * @param vy
	 */
	public void xorRegReg(int vx, int vy) {
		instRegReg(8, vx, vy, 3);
	}
	
	/**
	 * 8xy4 - ADD Vx, Vy
	 * 
	 * Set Vx = Vx + Vy, set VF = carry.
	 * @param vx
	 * @param vy
	 */
	public void addRegReg(int vx, int vy) {
		instRegReg(8, vx, vy, 4);
	}
	
	/**
	 * 8xy5 - SUB Vx, Vy
	 * 
	 * Set Vx = Vx - Vy, set VF = NOT borrow.
	 * @param vx
	 * @param vy
	 */
	public void subRegReg(int vx, int vy) {
		instRegReg(8, vx, vy, 5);
	}
	
	/**
	 * 8xy6 - SHR Vx {, Vy}
	 * 
	 * Set Vx = Vx SHR 1.
	 * @param vx
	 */
	public void shrReg(int vx) {
		instRegReg(8, vx, 0, 6);
	}
	
	/**
	 * 8xy7 - SUBN Vx, Vy
	 * 
	 * Set Vx = Vy - Vx, set VF = NOT borrow.
	 * @param vx
	 * @param vy
	 */
	public void subnRegReg(int vx, int vy) {
		instRegReg(8, vx, vy, 7);
	}
	
	/**
	 * 8xye - SHL Vx {, Vy}
	 * 
	 * Set Vx = Vx SHL 1.
	 * @param vx
	 */
	public void shlReg(int vx) {
		instRegReg(8, vx, 0, 0xe);
	}
	
	/**
	 * 9xy0 - SNE Vx, Vy
	 * Skip next instruction if Vx != Vy.
	 * 
	 * @param vx
	 * @param vy
	 */
	public void sneRegReg(int vx, int vy) {
		instRegReg(9, vx, vy, 0);
	}
	
	/**
	 * Annn - LD I, addr
	 * 
	 * Set I = nnn.
	 * @param addr
	 */
	public void ldIMem(int addr) {
		instAddr(0xa, addr);
	}
	
	/**
	 * Bnnn - JP V0, addr
	 * 
	 * Jump to location nnn + V0.
	 * @param addr
	 */
	public void jpV0Mem(int addr) {
		instAddr(0xb, addr);
	}
	
	/**
	 * Cxkk - RND Vx, byte
	 * 
	 * Set Vx = random byte AND kk.
	 * @param vx
	 * @param kk
	 */
	public void rndRegVal(int vx, int kk) {
		instRegVal(0xc, vx, kk);
	}
	
	/**
	 * Dxyn - DRW Vx, Vy, nibble
	 * 
	 * Display n-byte sprite starting at memory location I at (Vx, Vy), set VF = collision.
	 * @param vx
	 * @param vy
	 * @param nibble
	 */
	public void drw(int vx, int vy, int nibble) {
		instRegReg(0xd, vx, vy, nibble);
	}
	
	/**
	 * ExA1 - SKNP Vx
	 * 
	 * Skip next instruction if key with the value of Vx is not pressed.
	 * @param vx
	 */
	public void skpReg(int vx) {
		instRegReg(0xe, vx, 0xa, 0x1);
	}
	
	/**
	 * Fx07 - LD Vx, DT
	 * 
	 * Set Vx = delay timer value.
	 * @param vx
	 */
	public void ldRegDt(int vx) {
		instRegReg(0xf, vx, 0, 7);
	}
	
	/**
	 * Fx0A - LD Vx, K
	 * 
	 * Wait for a key press, store the value of the key in Vx.
	 * @param vx
	 */
	public void ldRegK(int vx) {
		instRegReg(0xf, vx, 0, 0xa);
	}
	
	/**
	 * Fx15 - LD DT, Vx
	 * 
	 * Set delay timer = Vx.
	 * @param vx
	 */
	public void ldDtReg(int vx) {
		instRegReg(0xf, vx, 1, 5);
	}
	
	/**
	 * Fx18 - LD ST, Vx
	 * 
	 * Set sound timer = Vx.
	 * @param vx
	 */
	public void ldStReg(int vx) {
		instRegReg(0xf, vx, 1, 8);
	}
	
	/**
	 * Fx1E - ADD I, Vx
	 * 
	 * Set I = I + Vx.
	 * @param vx
	 */
	public void addIReg(int vx) {
		instRegReg(0xf, vx, 1, 0xe);
	}
	
	/**
	 * Fx29 - LD F, Vx
	 * 
	 * Set I = location of sprite for digit Vx.
	 * @param vx
	 */
	public void ldFReg(int vx) {
		instRegReg(0xf, vx, 2, 9);
	}
	
	/**
	 * Fx33 - LD B, Vx
	 * 
	 * Store BCD representation of Vx in memory locations I, I+1, and I+2.
	 * @param vx
	 */
	public void ldBReg(int vx) {
		instRegReg(0xf, vx, 3, 3);
	}
	
	/**
	 * Fx55 - LD [I], Vx
	 * 
	 * Store registers V0 through Vx in memory starting at location I.
	 * @param vx
	 */
	public void ldMemReg(int vx) {
		instRegReg(0xf, vx, 5, 5);
	}
	
	/**
	 * Fx65 - LD Vx, [I]
	 * 
	 * Read registers V0 through Vx from memory starting at location I.
	 * @param vx
	 */
	public void ldRegMem(int vx) {
		instRegReg(0xf, vx, 6, 5);
	}
	
	public static void main (String[] args) {
		Chip8 chip = new Chip8();
		Chip8Assembler asm = new Chip8Assembler(chip);
		
		asm.ldRegVal(0, 1);
		asm.ldRegVal(1, 2);
		asm.addRegReg(0, 1);		
		chip.tick(3);
		chip.debugPrint();
		
		asm.ldRegVal(0, 13);
		asm.shlReg(0);
		chip.tick(2);
		chip.debugPrint();
		
		asm.shrReg(0);
		chip.tick(1);
		chip.debugPrint();
	}
}
