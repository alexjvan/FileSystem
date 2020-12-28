/**
 * Is a test program for HW1B. It repeats 100 times of printing out a
 * given word and sleeping for a given millisecond,
 *
 */
public class PingPong extends Thread {
    private String word; // a word to print out
    private int msec;    // a millisecond to wait
    public PingPong( String[] args ) {
	word = args[0];
	msec = Integer.parseInt( args[1] );
    }
    public void run( ) {
	for ( int j = 0; j < 100; j++ ) {
	    // substituting SysLib.cout() for System.out.println()
	    // System.out.print( word + " " );
	    SysLib.cout( word + " " );
	    // substituting a busy wait with SysLib.sleep( )
	    // for ( int i = 0; i < time; i++ ) ;
	    SysLib.sleep( msec );
	}
	SysLib.cout( "\n" );
	SysLib.exit( );
    }
}
