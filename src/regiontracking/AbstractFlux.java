package regiontracking;

public abstract class AbstractFlux implements Configurable {

	protected float fbar = Float.NaN;			// [veh/hr] capacity
	protected float rhocrit = Float.NaN;		// [veh/mile] critical density
	protected float rhojam = Float.NaN;			// [veh/mile]

	public AbstractFlux(){
		return;
	}
	
	public AbstractFlux(float xfbar,float xrhocrit,float xrhojam){
		fbar = xfbar;
		rhocrit = xrhocrit;
		rhojam = xrhojam;
	}
	
	// sending function, returns [vph]
	public float S(float dens){		
		return Float.NaN;
	}

	// receive function, returns [vph]
	public float R(float dens){
		return Float.NaN;
	}
	
	// fundamental diagram, returns [vph]
	public float F(float dens){
		return Math.min(S(dens),R(dens));
	}

	// slope of fundamental diagram, returns [mph]
	public float Fprime(float dens){
		return Float.NaN;
	}

	// inverse fundamental diagram, freeflow, returns [veh/mile]
	public float Sinv(float f){
		return Float.NaN;
	}

	// inverse fundamental diagram, congestion, returns [veh/mile]
	public float Rinv(float f){
		return Float.NaN;
	}
	
	// maximum flow, returns [veh/hr]
	public float maxflow(){
		return fbar;
	}

	// maximum density, returns [veh/mile]
	public float maxdensity(){
		return rhojam;
	}

	// maximum speed, returns [mile/hre]
	public float maxspeed(){
		return Fprime(0);
	}
	
	public float Phi(float x,float L,float rhoup,float rhodn){
		return Float.NaN;
	}

	public float partialPhi(float x,float L,float rhoup,float rhodn){
		return Float.NaN;
	}
	
	/*
	public float integralPhi(float rhoup,float rhodn){
		return Float.NaN;
	}

	public float dPhidZeta(float zeta,float rhoup,float rhodn){
		return Float.NaN;
	}

	*/
	
	public final float chordslope(float rhoA,float rhoB){			// [mph]
		if( !Utils.equals(rhoA,rhoB) ) 	
			return (F(rhoA)-F(rhoB))/(rhoA-rhoB);	
		else{
			return Fprime(rhoA);
		}
	}

}
