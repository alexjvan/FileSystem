public class SyncQueue {
    private QueueNode queue[] = null;
    // add private data members if needed

    public SyncQueue( ) {
        // instantiate queue[0] ~ queue[9] 
    	this(10);
    }

    public SyncQueue( int condMax ) {
        // instantiate queue[0] ~ queue[condMax - 1]
    	queue = new QueueNode[condMax];
    	for(int i = 0; i < condMax; i++)
    		queue[i] = new QueueNode();
    }

    int enqueueAndSleep( int condition ) {
    	// check the validity of condition: 0 ~ queue.length
    	//   return -1 if it's invalid
	    if(condition < 0 || condition > queue.length)
	    	return -1;
	    // implement the logic
    	return queue[condition].sleep();
    }

    void dequeueAndWakeup( int condition, int tid ) {
    	// check the validity of condition 
    	// implemen the logic
    	queue[condition].wakeup(tid);
    }

    void dequeueAndWakeup( int condition ) {
        dequeueAndWakeup( condition, 0 );
    }
}

