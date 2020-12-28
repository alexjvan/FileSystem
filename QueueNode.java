import java.util.*;

public class QueueNode {
    private Vector<Integer> tidQueue;

    public QueueNode( ) {
    	// initialize tidQueue;
    	tidQueue = new Vector<Integer>();
    }

    public synchronized int sleep( ) {
		// implement the logic
		// return a child tid
    	if(tidQueue.size() == 0) {
    		try {
    			wait();
    		} catch(InterruptedException e) {
    			SysLib.cout("Error in sleep method");
    		}
    		return tidQueue.remove(0);
    	}
    	return -1;
    }

    public synchronized void wakeup( int tid ) {
    	// implement the logic
    	tidQueue.add(tid);
    	notify();
    }
}

