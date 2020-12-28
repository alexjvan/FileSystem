import java.util.Vector;

public class Cache {

	int bufferSize;
	int size;
	Vector<Block> cache;
	int pointer = 0;
	
	public Cache(int blockSize, int cacheBlocks) {
		bufferSize = blockSize;
		size = cacheBlocks;
		cache = new Vector<Block>(size);
		for(int i = 0; i < size; i++) {
			Block nBlock = new Block(blockSize);
			cache.add(nBlock);
		}
	}
	
	public synchronized boolean read(int blockId, byte[] buffer) {
		// Try and find required block
		for(int i = 0; i < size; i++) {
			Block block = cache.get(i);
			// If block is correct, and manipulated
			if(block.blockNum == blockId) {
				System.arraycopy(block.buffer, 0, buffer, 0, bufferSize);
				block.reference = true;
				return true;
			}
		}
		// Not found? Find next empty
		for(int i = 0; i < size; i++) {
			Block block = cache.get(i);
			if(block.blockNum == -1) {
				SysLib.rawread(blockId, block.buffer);
				System.arraycopy(block.buffer, 0, buffer, 0, bufferSize);
				block.reference = true;
				block.blockNum = blockId;
				return true;
			}
		}
		// Try and find a victim to replace
		int victim = nextVictim();
		Block block = cache.get(victim);
		// If the block needs to be cleared, write to disk
		if(block.dirty) {
			SysLib.rawwrite(block.blockNum, block.buffer);
			block.dirty = false;
		}
		SysLib.rawread(blockId, block.buffer);
		System.arraycopy(block.buffer, 0, buffer, 0, bufferSize);
		block.blockNum = blockId;
		block.reference = true;
		return true;
	}
	
	public synchronized boolean write(int blockId, byte[] buffer) {
		// Try and find required block
		for(int i = 0; i < size; i++) {
			Block block = cache.get(i);
			// If block is correct, and manipulated
			if(block.blockNum == blockId) {
				System.arraycopy(buffer, 0, block.buffer, 0, bufferSize);
				block.reference = true;
				block.dirty = true;
				return true;
			}
		}
		// Not found? Find next empty
		for(int i = 0; i < size; i++) {
			Block block = cache.get(i);
			if(block.blockNum == -1) {
				System.arraycopy(buffer, 0, block.buffer, 0, bufferSize);
				block.reference = true;
				block.blockNum = blockId;
				block.dirty = true;
				return true;
			}
		}
		// Find a victim to replace
		int victim = nextVictim();
		Block block = cache.get(victim);
		// If the block needs to be cleared, write to disk
		if(block.dirty)
			SysLib.rawwrite(block.blockNum, block.buffer);
		System.arraycopy(buffer, 0, block.buffer, 0, bufferSize);
		block.blockNum = blockId;
		block.reference = true;
		block.dirty = true;
		return false;
	}
	
	public synchronized void sync() {
		for(int i = 0; i < size; i++) {
			Block block = cache.get(i);
			// If block has not been touched...
			if(block.blockNum == -1)
				continue;
			// If block has been manipulated
			if(block.dirty) {
				SysLib.rawwrite(block.blockNum, block.buffer);
				block.dirty = false;
			}
		}
		SysLib.sync();
	}
	
	public synchronized void flush() {
		// Flush out all data
		for(int i = 0; i < size; i++) {
			Block block = cache.get(i);
			// If block has not been touched...
			if(block.blockNum == -1)
				continue;
			// If block has been manipulated
			if(block.dirty) {
				SysLib.rawwrite(block.blockNum, block.buffer);
				block.dirty = false;
			}
			block.reference = false;
			block.blockNum = -1;
		}
		SysLib.sync();
	}
	
	private int nextVictim() {
		int loops = 1;
		boolean passed = false;
		while(true) {
			Block test = cache.get(pointer);
			// If it hasn't been referenced yet and is not being accessed
			if(!test.reference && !test.dirty) {
				int retval = pointer;
				pointer = (pointer+1) % cache.size();
				return retval;
			}
			
			// Check if loop has gone through everything
			if(loops == cache.size())
				passed = true;
			if(passed && !test.reference && test.dirty) {
				int retval = pointer;
				pointer = (pointer+1) % cache.size();
				return retval;
			}
			test.reference = false;
			pointer = (pointer + 1) % cache.size();
			loops++;
		}
	}
	
}

class Block {
	byte[] buffer;
	int blockNum = -1;
	boolean reference = false;
	boolean dirty = false;
	
	public Block(int size) {
		// Init variables
		buffer = new byte[size];
	}
}