import java.util.Date;

/**
 * Is spawned from Test2.java and receives time to run in milliseconds
 * and to sleep every 100 milliseconds.
 */
public class TestThread2 extends Thread {
    private String name;
    private int cpuBurst;

    private long submissionTime;
    private long responseTime;
    private long completionTime;

    /**
     * Is the constructor to receive a thread name (a, b, c, d, or e)
     * and time to run in milliseconds.
     */
    public TestThread2 ( String args[] ) {
	name = args[0];
	cpuBurst = Integer.parseInt( args[1] );
	submissionTime = new Date( ).getTime( );
    }

    /**
     * Keeps sleeping every 100 millieseconds for the time to run. At
     * the end, it computes response time, turnaround time, and
     * execution time.
     */
    public void run( ) {
	responseTime = new Date( ).getTime( );

	for ( int burst = cpuBurst; burst > 0; burst -= 100 ) {
	    SysLib.sleep( 100 );
	}

	completionTime = new Date( ).getTime( );
	SysLib.cout( "Thread[" + name + "]:" +
		     " response time = " + (responseTime - submissionTime) +
		     " turnaround time = " + (completionTime - submissionTime)+
		     " execution time = " + (completionTime - responseTime)+
		     "\n");
	SysLib.exit( );
    }
}
