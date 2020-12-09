public class Inode {
   private final static int iNodeSize = 32;       // fix to 32 bytes
   private final static int directSize = 11;      // # direct pointers
   private final static int numberOfiNodes = 16;  // # of nodes per block

   public int length;                             // file size in bytes.
   public short count;                            // # file-table entries pointing to this.
   public short flag;                             // 0 = unused, 1 = used, ...
   public short direct[] = new short[directSize]; // direct pointers.
   public short indirect;                         // a indirect pointer.

   Inode( ) {                                     // a default constructor
      length = 0;
      count = 0;
      flag = 1;
      for ( int i = 0; i < directSize; i++ )
         direct[i] = -1;
      indirect = -1;
   }

   Inode( short iNumber ) {                       // retrieving inode from disk
      // find block id number
      int blockId = 1 + iNumber / numberOfiNodes;
      byte data[] = new byte[Disk.blockSize];

      // read data from the disk
      SysLib.rawread(blockId, data);

      // set values from the disk
      int i = iNodeSize * (iNumber / numberOfiNodes);
      length = SysLib.bytes2int(data, i);
      i += 4;
      count = SysLib.bytes2short(data, i);
      i += 2;
      flag = SysLib.bytes2short(data, i);
      i += 2;

      for(int j = 0; j < directSize; j++){
         direct[j] = SysLib.bytes2short(data, i);
         i += 2;
      }

      indirect = SysLib.bytes2short(data, i);
   }

   int toDisk( short iNumber ) {                  // save to disk as the i-th inode
      // find block id number
      int blockId = 1 + iNumber / numberOfiNodes;
      byte data[] = new byte[Disk.blockSize];
      SysLib.rawread(blockId, data);
      int i = iNodeSize * (iNumber / numberOfiNodes);

      // set values from inode to disk
      SysLib.int2bytes(length, data, i);
      i += 4;
      SysLib.short2bytes(count, data, i);
      i += 2;
      SysLib.short2bytes(flag, data, i);
      i += 2;

      for(int j = 0; j < directSize; j++){
         SysLib.short2bytes(direct[j], data, i);
         i += 2;
      }

      SysLib.short2bytes(indirect, data, i);

      // write it to disk
      SysLib.rawwrite(blockId, data);

      return 1; // im not sure what to return rn
   }

   int getBlockNumPointer( int seekPtr ) {
      // make sure the pointer is in range
      if (seekPtr <= 0 || seekPtr > numberOfiNodes)
         return -1;
      // return appropriate pointer
      return direct[1 + seekPtr / numberOfiNodes];
   }

}

