import java.util.Vector;

public class FileTable {

   private Vector<FileTableEntry> table;         // the actual entity of this file table
   private Directory dir;        // the root directory 

   public FileTable( Directory directory ) { // constructor
      this.table = new Vector<FileTableEntry>( );     // instantiate a file (structure) table
      this.dir = directory;           // receive a reference to the Director
   }                             // from the file system

   public FileTableEntry getEntry(int num) {
	   return table.get(num);
   }
   
   public int getNum(FileTableEntry ent) {
	   for(int i = 0; i < table.capacity(); i++) {
		   if(table.get(i) == ent)
			   return i;
	   }
	   return -1;
   }
   
   // major public methods
   public synchronized FileTableEntry falloc( String filename, String mode ) {
      // allocate a new file (structure) table entry for this file name
      // allocate/retrieve and register the corresponding inode using dir
      // increment this inode's count
      // immediately write back this inode to the disk
      // return a reference to this file (structure) table entry
	  Inode node = null;
	  short iNum = -1;
	  while(true) {
		  // if already in dir
		  if(filename.equals("/")) {
			  iNum = 0;
		  } else {
			  iNum = this.dir.namei(filename);
		  }
		  // cant find file...
		  if(iNum < 0) {
			  if(mode.equals("r")) {
				  return null;
			  } else {
				  iNum = dir.ialloc(filename);
				  node = new Inode();
				  node.flag = 2;
				  break;
			  }
		  } else {
			  node = new Inode(iNum);
			  if(mode.equals("r")) {
				  if(node.flag != 0 && node.flag != 1) {
					  try {
						  wait();
					  } catch(InterruptedException e) {}
					  continue;
				  }
				  node.flag = 1;
				  break;
			  }
			  if(node.flag != 0 && node.flag != 3) {
				  if(node.flag == 1 || node.flag == 2) {
					  node.flag = (short)4;
					  node.toDisk(iNum);
				  }
			  }
			  node.flag = 2;
			  break;
		  }
	  }
	  node.count++;
	  // write new to disc & update
	  node.toDisk(iNum);
	  FileTableEntry entry = new FileTableEntry(node, iNum, mode);
	  this.table.addElement(entry);
	  return entry;
   }

   public synchronized int ffree( FileTableEntry e ) {
      // receive a file table entry reference
      // save the corresponding inode to the disk
      // free this file table entry.
      // return true if this file table entry found in my table
	  
	  if(this.table.removeElement(e)) {
		  Inode cur = e.inode;
		  cur.count--;
		  
		  int flag = e.inode.flag;
		  // if node is being read or nothing important
		  if(flag == 1 || flag == 2)
			  e.inode.flag = 0;
		  
		  // reset node to special condition
		  if(flag == 4 || flag == 5)
			  e.inode.flag = 3;
		  
		  cur.toDisk(e.iNumber);
		  
		  notify();
		  return 0;
	  }
	  return -1;
   }

   public synchronized boolean fempty( ) {
      return table.isEmpty( );  // return if table is empty 
   }                            // should be called before starting a format

}
