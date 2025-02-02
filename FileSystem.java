public class FileSystem {
   private Superblock superblock;
   private Directory directory;
   private FileTable filetable;
   
   public FileSystem( int diskBlocks ) {
      // create superblock, and format disk wth 64 inodes in default
	   this.superblock = new Superblock( diskBlocks);

      // create directory, and register "/" in direstory ectry 0
      directory = new Directory( this.superblock.totalInodes );

      // file table is created, and store directory in the file table
      filetable = new FileTable( directory );

      // directory reconstruction
      int dirEnt = open( "/",  "r" );
      int dirSize = fsize( dirEnt );
      if ( dirSize > 0 ) {
         byte[] dirData = new byte[dirSize];
         read( dirEnt, dirData );
         directory.bytes2directory( dirData );
      }
      close( dirEnt );
   }
   
   void sync() {
	   int entryNum = open("/", "w");
	   byte[] temp = this.directory.directory2bytes();
	   
	   write(entryNum, temp);
	   close(entryNum);
	   
	   this.superblock.sync();
   }
   
   int format( int files) {
	   this.superblock.format(files);
	   // Create new instance of Directory and FileTable
	   this.directory = new Directory(this.superblock.totalInodes);
	   this.filetable = new FileTable(this.directory);
	   
	   return 0;
   }
   
   int open( String filename, String mode ) {
	   FileTableEntry returnMe = this.filetable.falloc(filename, mode);
	   return this.filetable.getNum(returnMe);
   }
   
   int close( int ftNum ) {
	   FileTableEntry ftEnt = this.filetable.getEntry(ftNum);
	   // outliers
	   if(ftEnt == null)
		   return -1;
	   synchronized(ftEnt) {
		   ftEnt.count--;
		   if(ftEnt.count <= 0)
			   return this.filetable.ffree(ftEnt);
		   return 0;
	   }
   }
   
   int fsize( int ftNum ){
	   FileTableEntry ftEnt = this.filetable.getEntry(ftNum);
	   if(ftEnt == null)
		   return -1;
	   synchronized(ftEnt) {
		   return ftEnt.inode.length;
	   }
   }
   
   int read( int ftNum, byte[] buffer ) {
	   FileTableEntry ftEnt = this.filetable.getEntry(ftNum);
	   int leftToRead = 0;
	   int dataRead = 0;
	   int size = buffer.length;
	   
	   if(ftEnt.mode.equals("w") || ftEnt.mode.equals("a"))
		   return -1;
	   if(buffer == null)
		   return -1;
	   
	   synchronized(ftEnt) {
		   // stop when ptr is in range and buffer can still read
		   while(ftEnt.seekPtr < fsize(ftNum) && buffer.length > 0) {
			   int block = 0;
			   
			   int oset = ftEnt.seekPtr / Disk.blockSize;
			   if(oset < 11)
				   block = ftEnt.inode.direct[oset];
			   else if(ftEnt.inode.indirect == -1)
				   block = -1;
			   else {
				   byte[] temp = new byte[Disk.blockSize];
				   SysLib.rawread(ftEnt.inode.indirect, temp);
				   int difference = oset - 11;
				   block = SysLib.bytes2short(temp, 2*difference);
			   }
			   
			   if(block != -1) {
				   byte[] temp = new byte[Disk.blockSize];
				   // now we know where the block is...
				   SysLib.rawread(block, temp);
				   
				   int data = ftEnt.seekPtr % Disk.blockSize;
				   int blocksLeft = Disk.blockSize - data;
				   
				   int remaining = fsize(ftNum) - ftEnt.seekPtr;
				   
				   int smallerOne = Math.min(blocksLeft, size);
				   leftToRead = Math.min(smallerOne, remaining);
				   
				   System.arraycopy(temp, data, buffer, dataRead, leftToRead);
				   // Update vars to get new sizes and locs
				   dataRead += leftToRead;
				   ftEnt.seekPtr += leftToRead;
				   size -= leftToRead;
				   
				   if(leftToRead == 0)
					   break;
			   }
			   // invalid loc
			   else
				   break;
		   }
		   return dataRead;
	   }
   }
   
   int write (int ftNum, byte[] buffer ) {
	   FileTableEntry ftEnt = this.filetable.getEntry(ftNum);
	   // outliers
	   if(ftEnt == null || buffer == null)
		   return -1;
	   
	   synchronized(ftEnt) {
		   int offset = 0;
		   int size = buffer.length;
		   
		   // continue writing until we are done
		   while(size > 0) {
			   int block = 0;
		   
			   int oset = ftEnt.seekPtr / Disk.blockSize;
			   if(oset < 11)
				   block = ftEnt.inode.direct[oset];
			   else if(ftEnt.inode.indirect == -1)
				   block = -1;
			   else {
				   byte[] temp = new byte[Disk.blockSize];
				   SysLib.rawread(ftEnt.inode.indirect, temp);
				   int difference = oset - 11;
				   block = SysLib.bytes2short(temp, 2*difference);
			   }
			   
			   if(block == -1) {
				   int freeBlock = this.superblock.findFreeBlock();
				   int res = updateBlock(ftEnt, ftEnt.seekPtr, (short)freeBlock);
				   // indirect block not available
				   if(res == -3) {
					   int nextFree = this.superblock.findFreeBlock();
					   if(!updateFree(ftEnt, (short)nextFree))
						   return -1;
					   if(updateBlock(ftEnt, ftEnt.seekPtr, (short)freeBlock) != 0)
						   return -1;
					   res = 0;
				   }
				   // found correct pos
				   if(res == 0)
					   block = freeBlock;
				   // direct pointer bad
				   if(res == -2 || res == -1)
					   return -1;
			   }
			   
			   // now grab data...
			   byte[] temp = new byte[Disk.blockSize];
			   SysLib.rawread(block, temp);
			   
			   int pos = ftEnt.seekPtr % Disk.blockSize;
			   int remaining = Disk.blockSize - pos;
			   int open = Math.min(remaining, size);
			   
			   System.arraycopy(buffer, offset, temp, pos, open);
			   SysLib.rawwrite(block, temp);
			   
			   ftEnt.seekPtr += open;
			   offset += open;
			   size -= open;
			   
			   if(ftEnt.seekPtr > ftEnt.inode.length)
				   ftEnt.inode.length = ftEnt.seekPtr;
			   
			   if(remaining == 0)
				   break;
		   }
		   ftEnt.inode.toDisk(ftEnt.iNumber);
		   return offset;
	   }
   }

   private int updateBlock(FileTableEntry ftEnt, int pos, short val) {
	   int pointer = pos / Disk.blockSize;
	   if(pointer < 11) {
		   if(ftEnt.inode.direct[pointer] >= 0)
			   return -1;
		   else if(pointer > 0 && ftEnt.inode.direct[pointer - 1] == -1)
			   return -2;
		   else {
			   ftEnt.inode.direct[pointer] = val;
			   return 0;
		   }
	   }
	   // in indir loc - not a real one tho
	   if(ftEnt.inode.indirect < 0)
		   return -3;
	   // real indir loc
	   byte[] temp = new byte[Disk.blockSize];
	   SysLib.rawread(ftEnt.inode.indirect, temp);
	   
	   int oset = pointer - 11;
	   if(SysLib.bytes2short(temp, oset * 2) > 0)
		   return -1;
	   
	   SysLib.short2bytes(val, temp, oset * 2);
	   SysLib.rawwrite(ftEnt.inode.indirect, temp);
	   return 0;
   }

   private boolean updateFree(FileTableEntry ftEnt, short val) {
	   // if a direct block is invalid...
	   for(int i = 0; i < 11; i++) {
		   if(ftEnt.inode.direct[i] == -1)
			   return false;
	   }
	   // no need to update if in use
	   if(ftEnt.inode.indirect != -1)
		   return false;
	   
	   ftEnt.inode.indirect = val;
	   byte[] temp = new byte[Disk.blockSize];
	   
	   for(int writePos = 0; writePos < Disk.blockSize / 2; ++writePos)
		   SysLib.short2bytes((short)-1, temp, writePos * 2);
	   SysLib.rawwrite(val, temp);
	   return true;
   }
   
   int delete( String filename) {
	   int find = open(filename, "w");
	   FileTableEntry ftEnt = this.filetable.getEntry(find);
	   if(find == -1)
		   return -1;
	   short num = ftEnt.iNumber;
	   int closed = close(find);
	   boolean freed = this.directory.ifree(num);
	   if(closed == 0 && freed)
		   return 0;
	   return -1;
   }
   
   private final int SEEK_SET = 0;
   private final int SEEK_CUR = 1;
   private final int SEEK_END = 2;
   
   int seek( int ftNum, int offset, int whence ) {
	   FileTableEntry ftEnt = this.filetable.getEntry(ftNum);
	   if(whence != 0 && whence != 1 && whence != 2)
		   return -1;
	   
	   synchronized(ftEnt) {
		   if(ftEnt == null)
			   return -1;
		   if(whence == SEEK_SET) {
			   if(offset <= fsize(ftNum) && offset >= 0)
				   ftEnt.seekPtr = offset;
		   } else if(whence == SEEK_CUR) {
			   // make sure the file size is greater than 0
			   if(ftEnt.seekPtr + offset <= fsize(ftNum) && (ftEnt.seekPtr + offset) >= 0)
				   ftEnt.seekPtr += offset;
		   } else if(whence == SEEK_END) {
			   // make sure its still in position...
			   if(fsize(ftNum) + offset >= 0 && fsize(ftNum) + offset <= fsize(ftNum))
				   ftEnt.seekPtr = fsize(ftNum) + offset;
			   else
				   return -1;
		   }
		   return ftEnt.seekPtr;
	   }
   }

}