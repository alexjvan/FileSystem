import java.util.Date;

/**
 * Tests Scheduler.java with 5 child threads: TestThread2b[a],
 * TestThread2b[b], TestThread2b[c], TestThread2b[d], and
 * TestThread2b[e], each running 5, 1, 3, 6, and .5 seconds. Test2b
 * waits for all the child thread termination at the end.
 * <p>
 * Note that TestThread2b prints out its heartbeat messages.
 */
public class Test2b extends Thread {

    /**
     * Is the Test2b.java thread's main body. It launches 5 children.
     * Each child prints out its heartbeat messages.
     */
    public void run( ) {
	long startTime = new Date().getTime();
	String[] args1 = SysLib.stringToArgs( "TestThread2b a 5000" );
	String[] args2 = SysLib.stringToArgs( "TestThread2b b 1000" );
	String[] args3 = SysLib.stringToArgs( "TestThread2b c 3000" );
	String[] args4 = SysLib.stringToArgs( "TestThread2b d 6000" );
	String[] args5 = SysLib.stringToArgs( "TestThread2b e 500" );
	SysLib.exec( args1 );
	SysLib.exec( args2 );
	SysLib.exec( args3 );
	SysLib.exec( args4 );
	SysLib.exec( args5 );
	for (int i = 0; i < 5; i++ )
	    SysLib.join( );
	long endTime = new Date().getTime();
	long totalTime = endTime - startTime;
	SysLib.cout( "Test2b finished; total time = " + totalTime + "\n" );
	SysLib.exit( );
    }
}
