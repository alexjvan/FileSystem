import java.util.Date;

/**
 * Tests Scheduler.java with 5 child threads: TestThread2[a],
 * TestThread2[b], TestThread2[c], TestThread2[d], and TestThread2[e],
 * each running 5, 1, 3, 6, and .5 seconds. Test2 waits for all the
 * child thread termination at the end.
 */
public class Test2 extends Thread {

    /**
     * Is the Test2.java thread's main body. It launches 5 children.
     */
    public void run( ) {
	long startTime = new Date().getTime();
	String[] args1 = SysLib.stringToArgs( "TestThread2 a 5000 0" );
	String[] args2 = SysLib.stringToArgs( "TestThread2 b 1000 0" );
	String[] args3 = SysLib.stringToArgs( "TestThread2 c 3000 0" );
	String[] args4 = SysLib.stringToArgs( "TestThread2 d 6000 0" );
	String[] args5 = SysLib.stringToArgs( "TestThread2 e 500  0" );
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
