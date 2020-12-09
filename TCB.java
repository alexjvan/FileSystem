public class TCB {
   private Thread thread = null;
   private int tid = 0;
   private int pid = 0;
   private boolean terminate = false;

   // User file descriptor table: 
   // each entry pointing to a file (structure) table entry
   public FileTableEntry[] ftEnt = null;

   public TCB( Thread newThread, int myTid, int parentTid ) {
      thread = newThread;
      tid = myTid;
      pid = parentTid;
      terminate = false;

      // The following code is added for the file system
      ftEnt = new FileTableEntry[32];
      for ( int i = 0; i < 32; i++ )
         ftEnt[i] = null;         // all entries initialized to null
         // fd[0], fd[1], and fd[2] are kept null.
   }

	public Thread getThread() {
		return this.thread;
	}

	public int getTid() {
		return this.tid;
	}

	public boolean setTerminated() {
		this.terminate = true;
		return true;
	}

	public boolean getTerminated() {
		return this.terminate;
	}

	public int getPid() {
		return this.pid;
	}

	public FileTableEntry getFtEnt(int param) {
		return ftEnt[param];
	}

	public FileTableEntry returnFd(int param) {
		if(param < 3 && param >= 32)
			return null;
		
		FileTableEntry e = ftEnt[param];
		ftEnt[param] = null;
		return e;
	}

	public int getFd(FileTableEntry open) {
		if(open == null)
			return -1;
		for(int i = 3; i < 32; i++) {
			if(ftEnt[i] == null) {
				ftEnt[i] = open;
				return i;
			}
		}
		return -1;
	}
}
