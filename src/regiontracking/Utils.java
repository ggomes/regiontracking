package regiontracking;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class Utils {

	static public boolean equals(float a,float b){
		return Math.abs(a-b)<Globals.epsilon;
	}
	
	static public boolean lessorequalthan(float a,float b){
		return a<b | equals(a,b);
	}
	
	static public boolean lessthan(float a,float b){
		return a<b & !equals(a,b);
	}
	
	static public boolean greaterorequalthan(float a,float b){
		return a>b | equals(a,b);
	}
	
	static public boolean greaterthan(float a,float b){
		return a>b & !equals(a,b);
	}
	
	static public boolean isbetweenopen(float x,float a,float b){
		return greaterthan(x,a) & lessthan(x,b);
	}
	
	static public boolean isbetweenhalfopen(float x,float a,float b){
		return greaterorequalthan(x,a) & lessthan(x,b);
	}
	
	static public boolean isbetweenclosed(float x,float a,float b){
		return greaterorequalthan(x,a) & lessorequalthan(x,b);
	}
	
	static public boolean equalsmod(float x,float y){
		return equals(x%y,0);
	}

	static public long getCpuTime( ) {
	    ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
	    return bean.isCurrentThreadCpuTimeSupported( ) ?
	        bean.getCurrentThreadCpuTime( ) : 0L;
	}
	 
}
