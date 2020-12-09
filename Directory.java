public class Directory {
   private static int maxChars = 30; // max characters of each file name

   // Directory entries
   private int fsize[];        // each element stores a different file size.
   private char fnames[][];    // each element stores a different file name.
   private int maxFileNumber;
   private final int SIZEOFINT = 4;
   private final int NAMESIZE = maxChars * 2;

   public Directory( int maxInumber ) { // directory constructor
      fsize = new int[maxInumber];     // maxInumber = max files
      maxFileNumber = maxInumber;
      for ( int i = 0; i < maxInumber; i++ ) 
         fsize[i] = 0;                 // all file size initialized to 0
      fnames = new char[maxInumber][maxChars];
      String root = "/";                // entry(inode) 0 is "/"
      fsize[0] = root.length( );        // fsize[0] is the size of "/".
      root.getChars( 0, fsize[0], fnames[0], 0 ); // fnames[0] includes "/"
   }

   public int bytes2directory( byte data[] ) {
      // assumes data[] received directory information from disk
      // initializes the Directory instance with this data[]

      // makes sure data exists
      if ((data == null) || (data.length == 0))
         return -1;

      // iterator
      int j = 0;

      // get the max number of files that the directory will hold
      maxFileNumber = SysLib.bytes2int(data, j);
      j += SIZEOFINT;

      // get the size of the files
      for (int i = 0; i < fsize.length; i++) {
         fsize[i] = SysLib.bytes2int(data, j);
         j += SIZEOFINT;
      }
      
      // get the name of the files
      for(int i = 0; i < fnames.length; i++) {
         String name = new String(data, j, maxChars);
         name.getChars(0, maxChars, fnames[i], 0);
          j += NAMESIZE;
      }

      return 1;
   }

   public byte[] directory2bytes( ) {
      // converts and return Directory information into a plain byte array
      // this byte array will be written back to disk
      // note: only meaningfull directory information should be converted
      // into bytes.

      byte data[] = new byte[(fsize.length * SIZEOFINT) + (fnames.length * NAMESIZE) + SIZEOFINT];
      int j = 0;

      // save the max number of files
      SysLib.int2bytes(maxFileNumber, data, j);
      j += SIZEOFINT;


      // add the array of file size to the byte array
      for ( int i = 0; i < fsize.length; i++ ) {
         SysLib.int2bytes(fsize[i], data, j);
         j += SIZEOFINT;
      }


      // add the file names to the byte array
      for(int i = 0; i < fnames.length; i++) {
         String in = new String(fnames[i]);
         System.arraycopy(in.getBytes(), 0, data, j, in.length());
         j += NAMESIZE;
      }


      return data;
   }

   public short ialloc( String filename ) {
      // filename is the one of a file to be created.
      // allocates a new inode number for this filename

      // find a free spot and put the file there
      for (short i = 0; i < fsize.length; i++){
         if(fsize[i] == 0) {
        	 int min = Math.min(filename.length(), maxChars);
        	 for(int j = 0; j < min; j++)
        		 fnames[i][j] = filename.charAt(j);
        	 fsize[i] = min;
        	 return (short)i;
         }
      }
      return -1;
   }

   public boolean ifree( short iNumber ) {
      // deallocates this inumber (inode number)
      // the corresponding file will be deleted

      // make sure the number is in range
      if ((iNumber < 0) || (iNumber >= fsize.length))
         return false;

      // find a free
      for (short i = 0; i < fsize[iNumber]; i++){
         fnames[iNumber][i] = 0;
      }

      fsize[iNumber] = 0;

      return true;
   }

   public short namei( String filename ) {
      // returns the inumber corresponding to this filename
      for (short i = 0; i < fsize.length; i++){
         String name = new String(fnames[i], 0, fsize[i]);
         if (name.equals(filename)){
            return i;
         }
      }

      return -1;
   }
}
