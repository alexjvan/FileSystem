class Superblock {
   public int totalBlocks; // the number of disk blocks
   public int totalInodes; // the number of inodes
   public int freeList;    // the block number of the free list's head
   
   public Superblock( int diskSize ) {
	   byte[] block = new byte[Disk.blockSize];
	   SysLib.rawread(0, block);
	   this.totalBlocks = SysLib.bytes2int(block, 0);
	   this.totalInodes = SysLib.bytes2int(block, 4);
	   this.freeList = SysLib.bytes2int(block, 8);
	   if(this.totalBlocks != diskSize && this.totalInodes <= 0 && this.freeList < 2) {
		   this.totalBlocks = diskSize;
		   format(64);
	   }
   }
   
   public void sync() {
	   byte[] blockInfo = new byte[Disk.blockSize];
	   // Pass in the int, byte array, and offset
	   SysLib.int2bytes(this.totalBlocks, blockInfo, 0);
	   SysLib.int2bytes(this.totalInodes, blockInfo, 4);
	   SysLib.int2bytes(this.freeList, blockInfo, 8);
	   // Pass in data to write and sync
	   SysLib.rawwrite(0, blockInfo);
   }
   
   public void format(int numOfBlocks) {
	   this.totalInodes = numOfBlocks;
	   // Write empty iNodes to block
	   for(short i = 0; i < this.totalInodes; i++) {
		   Inode temp = new Inode();
		   temp.flag = 0;
		   temp.toDisk(i);
	   }
	   // Find where to jump
	   this.freeList = 2 + (this.totalInodes / 16);
	   // Write index to next available block
	   for(int i = this.freeList; i < this.totalBlocks; i++) {
		   byte[] temp = new byte[Disk.blockSize];
		   SysLib.int2bytes(i + 1, temp, 0);
		   SysLib.rawwrite(i, temp);
	   }
	   // Update super
	   this.sync();
   }
   
   public int findFreeBlock() {
	   int freeBlock = freeList;
	   // Check if the block is still in range
	   if(freeList > 0 && freeList < this.totalBlocks) {
		   byte[] blockInfo = new byte[Disk.blockSize];
		   SysLib.rawread(this.freeList, blockInfo);
		   
		   this.freeList = SysLib.bytes2int(blockInfo, 0);
		   
		   SysLib.int2bytes(0, blockInfo, 0);
		   // Update current block
		   SysLib.rawwrite(freeBlock, blockInfo);
	   }
	   return freeBlock;
   }
   
   public void addFreeBlock(int blockNum) {
	   // check for valid block num
	   if(blockNum < 0)
		   return;
	   
	   byte[] temp = new byte[Disk.blockSize];
	   
	   SysLib.int2bytes(this.freeList, temp, 0);
	   SysLib.rawwrite(blockNum, temp);
	   this.freeList = blockNum;
   }
   
}
