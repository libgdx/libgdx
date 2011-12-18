
package com.badlogic.chip8;

import java.util.Arrays;
import java.util.Random;

/** Chip-8 emulator based on <a
 * href="http://devernay.free.fr/hacks/chip8/C8TECH10.HTM#Annn">http://devernay.free.fr/hacks/chip8/C8TECH10.HTM#Annn</a>. Get a
 * ROM and load it via the constructor. Call tick at a frequency of 60Hz. Override playBuzzer and waitForKeypress.
 * 
 * @author mzechner */
public class Chip8 {
	/** frame buffer width, height, size in pixels/bytes, row shift factor **/
	public static final int FBUF_WIDTH = 64;
	public static final int FBUF_HEIGHT = 32;
	public static final int FBUF_SIZE = FBUF_WIDTH * FBUF_HEIGHT;
	public static final int FBUF_ROW_SHIFT = 6;

	/** ram, stack, frame buffer **/
	public final byte[] mem = new byte[4096];
	public final int[] stack = new int[16];
	public final byte[] fbuf = new byte[FBUF_WIDTH * FBUF_HEIGHT];
	/** keys **/
	public final boolean[] keys = new boolean[16];
	/** registers **/
	public int[] v = new int[16];
	/** index register **/
	public int idx;
	/** program counter **/
	public int pc = 0x200;
	/** stack pointer **/
	public int sp;
	/** sound register **/
	public int st;
	/** delay register **/
	public int dt;

	/** Random number generator for RND instruction **/
	private Random rand = new Random();
	/** true if instruction LD Vx, K (0xfx0a) was executed **/
	private boolean waitForKeypress;
	/** register the key should be put in **/
	private int keyRegister;

	public Chip8 (byte[] rom) {
		System.arraycopy(rom, 0, mem, 0x200, Math.min(mem.length, rom.length));
		initDefaultSprites();
	}

	public Chip8 () {
	}

	/** Sets the default sprites for '0'-'f' in mem, starting at address 0. */
	private void initDefaultSprites () {
		byte[] sprites = {(byte)0xF0, (byte)0x90, (byte)0x90, (byte)0x90, (byte)0xF0, (byte)0x20, (byte)0x60, (byte)0x20,
			(byte)0x20, (byte)0x70, (byte)0xF0, (byte)0x10, (byte)0xF0, (byte)0x80, (byte)0xF0, (byte)0xF0, (byte)0x10, (byte)0xF0,
			(byte)0x10, (byte)0xF0, (byte)0x90, (byte)0x90, (byte)0xF0, (byte)0x10, (byte)0x10, (byte)0xF0, (byte)0x80, (byte)0xF0,
			(byte)0x10, (byte)0xF0, (byte)0xF0, (byte)0x80, (byte)0xF0, (byte)0x90, (byte)0xF0, (byte)0xF0, (byte)0x10, (byte)0x20,
			(byte)0x40, (byte)0x40, (byte)0xF0, (byte)0x90, (byte)0xF0, (byte)0x90, (byte)0xF0, (byte)0xF0, (byte)0x90, (byte)0xF0,
			(byte)0x10, (byte)0xF0, (byte)0xF0, (byte)0x90, (byte)0xF0, (byte)0x90, (byte)0x90, (byte)0xE0, (byte)0x90, (byte)0xE0,
			(byte)0x90, (byte)0xE0, (byte)0xF0, (byte)0x80, (byte)0x80, (byte)0x80, (byte)0xF0, (byte)0xE0, (byte)0x90, (byte)0x90,
			(byte)0x90, (byte)0xE0, (byte)0xF0, (byte)0x80, (byte)0xF0, (byte)0x80, (byte)0xF0, (byte)0xF0, (byte)0x80, (byte)0xF0,
			(byte)0x80, (byte)0x80,

		};
		System.arraycopy(sprites, 0, mem, 0, sprites.length);
	}

	public void tick (int numInstructions) {
		for (int i = 0; i < numInstructions; i++)
			tick();
	}

	public void tick () {
		// update delay timer
		if (dt > 0) --dt;

		// update sound timer
		if (st > 0) {
			--st;
			// TODO
			// playBuzzer();
		}

		// if we wait for a keypress check if a key was
		// pressed (instruction LD Vx, K)
		if (waitForKeypress) {
			// TODO
// int result = waitForKeypress();
// if(result == -1) return;
// v[keyRegister] = result;
			waitForKeypress = false;
		}

		// fetch next instructin and interpret
		// high and low byte of instruction
		int hb = mem[pc++] & 0xff;
		int lb = mem[pc++] & 0xff;
		// nibbles, from most significant to least significant
		int n1 = (hb >>> 4);
		int n2 = (hb & 0xf);
		int n3 = (lb >>> 4);
		int n4 = (lb & 0xf);

		// interpret highest nibble, then interpret based on lowest nibble/byte
		// if necessary
		switch (n1) {
		case 0x0: {
			// CLS, RET
			if (hb == 0) {
				// CLS
				if (lb == 0xe0) {
					Arrays.fill(fbuf, (byte)0);
				}
				// RET
				if (lb == 0xee) {
					pc = stack[sp];
					--st;
				}
			}
			// SYS
			else {
				pc = (n2 << 8) | lb;
			}
			break;
		}
		case 0x1: {
			// JP
			pc = (n2 << 8) | lb;
			break;
		}
		case 0x2: {
			// CALL
			++st;
			stack[sp] = pc;
			pc = (n2 << 8) | lb;
			break;
		}
		case 0x3: {
			// SE Vx, byte
			if (v[n2] == lb) pc += 2;
			break;
		}
		case 0x4: {
			// SNE Vx, byte
			if (v[n2] != lb) pc += 2;
			break;
		}
		case 0x5: {
			// SE Vx, Vy
			if (v[n2] == v[n3]) pc += 2;
			break;
		}
		case 0x6: {
			// LD Vx, byte
			v[n2] = lb;
			break;
		}
		case 0x7: {
			// ADD Vx, byte
			v[n2] = (v[n2] + lb) & 0xff;
			break;
		}
		case 0x8: {
			switch (n4) {
			case 0x0:
				// LD Vx, Vy
				v[n2] = v[n3];
				break;
			case 0x1:
				// OR Vx, Vy
				v[n2] = v[n2] | v[n3];
				break;
			case 0x2:
				// AND Vx, Vy
				v[n2] = v[n2] & v[n3];
				break;
			case 0x3:
				// XOR Vx, Vy
				v[n2] = v[n2] ^ v[n3];
				break;
			case 0x4:
				// Add Vx, Vy
				int result = v[n2] = v[n2] + v[n3];
				v[0xf] = result > 0xff ? 1 : 0;
				break;
			case 0x5:
				// SUB Vx, Vy
				v[0xf] = v[n2] > v[n3] ? 1 : 0;
				v[n2] = (v[n2] - v[n3]) & 0xff;
				break;
			case 0x6:
				// SHR Vy {, Vy}
				v[0xf] = (v[n2] & 0x1) != 0 ? 1 : 0;
				v[n2] = v[n2] >>> 1;
				break;
			case 0x7:
				// SUBN Vx, Vy
				v[0xf] = v[n3] > v[n2] ? 1 : 0;
				v[n2] = (v[n3] - v[n2]) & 0xff;
				break;
			case 0xe:
				// SHL Vx {, Vy}
				v[0xf] = (v[n2] & 0x80) != 0 ? 1 : 0;
				v[n2] = v[n2] << 1;
				break;
			}
			break;
		}
		case 0x9: {
			// SNE Vx, Vy
			if (v[n2] != v[n3]) pc += 2;
			break;
		}
		case 0xa: {
			// LD I, addr
			idx = (n2 << 8) | lb;
			break;
		}
		case 0xb: {
			// JP V0, addr
			pc = ((n2 << 8) | lb) + v[0];
			break;
		}
		case 0xc: {
			// RND Vx, byte
			v[n2] = rand.nextInt(0xff + 1) & lb;
			break;
		}
		case 0xd: {
			// DRW Vx, Vy, nibble
			displaySprite(v[n2], v[n2], idx, n4, mem, fbuf);
			break;
		}
		case 0xe: {
			switch (lb) {
			case 0x9e:
				// SKP Vx
				if (keys[v[n2]]) pc += 2;
				break;
			case 0xa1:
				// SKNP Vx
				if (!keys[v[n2]]) pc += 2;
				break;
			}
			break;
		}
		case 0xf: {
			switch (lb) {
			case 0x07:
				// LD Vx, DT
				v[n2] = dt;
				break;
			case 0x0a:
				// LD Vx, K
				waitForKeypress = true;
				keyRegister = n2;
				break;
			case 0x15:
				// LD DT, Vx
				dt = v[n2];
				break;
			case 0x18:
				// LD ST, Vx
				st = v[n2];
				break;
			case 0x1e:
				// ADD I, Vx
				idx = (idx + v[n2]) & 0xffff;
				break;
			case 0x29:
				// LD F, Vx
				idx = (5 * v[n2]) & 0xffff;
				break;
			case 0x33:
				// TODO
				// LD B, Vx
				break;
			case 0x55:
				// LD [I], Vx
				System.arraycopy(v, 0, mem, idx, v[n2]);
				break;
			case 0x65:
				// LD Vx, [i]
				System.arraycopy(mem, idx, v, 0, v[n2]);
				break;
			}
			break;
		}
		default:
			throw new RuntimeException("opcode " + Integer.toHexString(hb) + ", " + Integer.toHexString(lb) + " unknown");
		}
	}

	/** Displays a sprite according to the specification at <a
	 * href="http://devernay.free.fr/hacks/chip8/C8TECH10.HTM#Fx0A">http://devernay.free.fr/hacks/chip8/C8TECH10.HTM#Fx0A</a>
	 * @param vx the x-coordinate of the top left corner of the sprite
	 * @param vy the y-coordinate of the top left corner of the sprite
	 * @param addr the address of the sprite in mem
	 * @param numBytes the number of bytes of the sprite
	 * @param mem the memory to fetch the sprite data from
	 * @param fbuf the display buffer to draw to, 64x32 bytes, either 0 (black) or 0xff (white).
	 * @return true if a pixel was erased */
	private void displaySprite (int vx, int vy, int addr, int numBytes, byte[] mem, byte[] fbuf) {
		v[0xF] = 0;
		int spriteWidth = 8;
		int spriteHeight = (numBytes & 0x000F);
		if (spriteHeight == 0) spriteHeight = 16;

		int fbufAddr = vx + vy << FBUF_ROW_SHIFT;
		for (int y = 0; y < spriteHeight; y++, addr++) {
			byte pixelData = mem[addr];
			for (int x = 0; x < spriteWidth; x++, fbufAddr++) {
				if ((pixelData & (0x80 >> x)) != 0) {
					byte pixel = (byte)((fbuf[fbufAddr] ^ 1) & 0xff);
					if (pixel == 0) v[0xf] = pixel;
				}
			}
		}
	}

	/** Prints the frame buffer to std out */
	public void debugPrint () {
		int addr = 0;
		for (int y = 0; y < FBUF_HEIGHT; y++) {
			for (int x = 0; x < FBUF_WIDTH; x++) {
				System.out.print(fbuf[addr++]);
			}
			System.out.println();
		}

		System.out.println("pc: " + pc);
		System.out.println("sp: " + sp);
		System.out.println("dt: " + dt);
		System.out.println("st: " + st);
		System.out.println("v[]: " + Arrays.toString(v));
	}
}
