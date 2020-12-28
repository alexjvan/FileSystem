import java.util.Date;
import java.util.Random;

class Test4 extends Thread {
    private boolean enabled;
    private int testcase;
    private long startTime;
    private long endTime;
    private byte[] wbytes;
    private byte[] rbytes;
    private Random rand;

    private void getPerformance( String msg ) {
		if ( enabled == true )
		    SysLib.cout( "Test " + msg + "(cache enabled): " 
				 + (endTime - startTime) + "\n" );
		else
		    SysLib.cout( "Test " + msg + "(cache disabled): " 
				 + (endTime - startTime) + "\n" );
    }

    private void read( int blk, byte[] bytes ) {
		if ( enabled == true )
		    SysLib.cread( blk, bytes );
		else
		    SysLib.rawread( blk, bytes );
    }

    private void write( int blk, byte[] bytes ) {
		if ( enabled == true )
		    SysLib.cwrite( blk, bytes );
		else
		    SysLib.rawwrite( blk, bytes );
    }

    private void randomAccess( ) {
		int[] accesses = new int[200];
		for ( int i = 0; i < 200; i++ ) {
		    accesses[i] = Math.abs(rand.nextInt( ) % 512);
		    // SysLib.cout( accesses[i] + " " );
		}
		// SysLib.cout( "\n" );
		for ( int i = 0; i < 200; i++ ) {
		    for ( int j = 0; j < 512; j++ )
			wbytes[j] = (byte)(j);
		    write( accesses[i], wbytes );
		}
		for ( int i = 0; i < 200; i++ ) {
		    read( accesses[i], rbytes );
		    for ( int k = 0; k < 512; k++ ) {
				if ( rbytes[k] != wbytes[k] ) {
				    SysLib.cerr( "ERROR\n" );
				    SysLib.exit( );
				}
		    }
		}
    }

    private void localizedAccess( ) {
		for ( int i = 0; i < 20; i++ ) {
		    for ( int j = 0; j < 512; j++ )
		    	wbytes[j] = (byte)(i + j);
		    for ( int j = 0; j < 1000; j += 100 )
		    	write( j, wbytes );
		    for ( int j = 0; j < 1000; j += 100 ) {
				read( j, rbytes );
				for ( int k = 0; k < 512; k++ ) {
				    if ( rbytes[k] != wbytes[k] ) {
						SysLib.cerr( "ERROR\n" );
						SysLib.exit( );
				    }
				}
		    }
		}
    }

    private void mixedAccess( ) {
		int[] accesses = new int[200];
		for ( int i = 0; i < 200; i++ ) {
		    if ( Math.abs( rand.nextInt( ) % 10 ) > 8 ) {
				// random
				accesses[i] = Math.abs( rand.nextInt( ) % 512 );
		    } else {
				// localized
				accesses[i] = Math.abs( rand.nextInt( ) % 10 );
		    }
		}
		for ( int i = 0; i < 200; i++ ) {
		    for ( int j = 0; j < 512; j++ )
			wbytes[j] = (byte)(j);
		    write( accesses[i], wbytes );
		}
		for ( int i = 0; i < 200; i++ ) {
		    read( accesses[i], rbytes );
		    for ( int k = 0; k < 512; k++ ) {
				if ( rbytes[k] != wbytes[k] ) {
				    SysLib.cerr( "ERROR\n" );
				    SysLib.exit( );
				}
		    }
		}
    }

    private void adversaryAccess( ) {
		int[] accesses = new int[200];
		for ( int i = 0; i < 200; i++ )
		    accesses[i] = ( i % 2 == 0 ) ? i : i + 256;
			
		for ( int i = 0; i < 200; i++ ) {
		    for ( int j = 0; j < 512; j++ )
			wbytes[j] = (byte)(j);
		    write( accesses[i], wbytes );
		}
		for ( int i = 0; i < 200; i++ ) {
		    read( accesses[i], rbytes );
		    for ( int k = 0; k < 512; k++ ) {
				if ( rbytes[k] != wbytes[k] ) {
				    SysLib.cerr( "ERROR\n" );
				    SysLib.exit( );
				}
		    }
		}
    }


    public Test4( String[] args ) {
		enabled = args[0].equals( "enabled" ) ? true : false;
		testcase = Integer.parseInt( args[1] );
		wbytes = new byte[Disk.blockSize];
		rbytes = new byte[Disk.blockSize];
		rand = new Random( );
    }
	
    public void run( ) {
		SysLib.flush( );
		startTime = new Date( ).getTime( );
		switch ( testcase ) {
		case 1: 
		    randomAccess( );
		    endTime = new Date( ).getTime( );
		    getPerformance( "random accesses" );
		    break;
		case 2:
		    localizedAccess( );
		    endTime = new Date( ).getTime( );
		    getPerformance( "localized accesses" );
		    break;
		case 3:
		    mixedAccess( );
		    endTime = new Date( ).getTime( );
		    getPerformance( "mixed accesses" );
		    break;
		case 4:
		    adversaryAccess( );
		    endTime = new Date( ).getTime( );
		    getPerformance( "adversary accesses" );
		    break;
		case 5:
		    randomAccess( );
		    endTime = new Date( ).getTime( );
		    getPerformance( "random accesses" );
	
		    startTime = new Date( ).getTime( );
		    localizedAccess( );
		    endTime = new Date( ).getTime( );
		    getPerformance( "localized accesses" );
	
		    startTime = new Date( ).getTime( );
		    mixedAccess( );
		    endTime = new Date( ).getTime( );
		    getPerformance( "mixed accesses" );
	
		    startTime = new Date( ).getTime( );
		    adversaryAccess( );
		    endTime = new Date( ).getTime( );
		    getPerformance( "adversary accesses" );
		    break;
		}
		SysLib.exit( );
    }
}
