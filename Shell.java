import java.io.*;
import java.util.*;

/**
 * Is a ThreadOS Shell interpreter.
 */
public class Shell extends Thread
{
    /**
     * Is a default constructor.
     */
    public Shell( ) {
    }

    /**
     * Is the Shell.java thread's main body. It repeatedly prints a
     * command prompt "shell[number]% ", reads a line of user
     * commands, interprets each command to launch the corresponding
     * user thread, and check with a command delimitor "&amp;" or ";"
     * whether it should wait for the termination or go onto the next
     * command interpretation.
     */
    public void run( ) {
        for ( int line = 1; ; line++ ) {
            String cmdLine = "";
            do { // print out a command prompt and read one line.
                StringBuffer inputBuf = new StringBuffer( );
                SysLib.cerr( "shell[" + line + "]% " );
                SysLib.cin( inputBuf );
                cmdLine = inputBuf.toString( );
            } while ( cmdLine.length( ) == 0 );
            String[] args = SysLib.stringToArgs( cmdLine );
            // now args[] got "cmd1 arg ... ;or& cmd2 arg ... ;or& ...
            int first = 0;
            for ( int i = 0; i < args.length; i++ ) {
                if ( args[i].equals( ";" ) || args[i].equals( "&" )
                     || i == args.length - 1 ) { // keep scanning till the next delim.
                    String[] command = generateCmd( args, first, 
                                                    ( i==args.length - 1 ) ? 
                                                    i+1 : i );
                	// now command[] got a command and its arguments executing
                	// the last delimiter ';' or '&'
                    if ( command != null ) {
                    	// HW1B:
                    	// Check if command[0] is exit. If so, get terminated
                    	if(command[0].equals("exit")) {
                    		SysLib.exit();
                    		return;
                    	} 
                        // otherwise, pass command to SysLib.exec( ).
                    	SysLib.exec(command);
                        // if args[i] is '&' don't call SysLib.join( ).
                    	// Otherwise (i.e., ';'), keep calling SysLib.join( )
                    	if(args[i].equals(";")) {
                    		SysLib.join();
                    	}
                    	// EASY!!
                    }
                    first = i + 1;
                }
            }
        }
    }

    /**
     * returns a String array of a command and its arguments excluding
     * the delimiter ';' or '&amp;'.
     */
    private String[] generateCmd( String args[], int first, int last ) {
        if ( (args[last-1].equals(";")) || (args[last-1].equals("&")) )
            last = last -1;

        if ( last - first <= 0 )
            return null;
        String[] command = new String[ last - first ];
        for ( int i = first ; i < last; i++ ) 
              command[i - first] = args[i];
        return command;
    }
}
