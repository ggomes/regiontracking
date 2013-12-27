package regiontracking;

import org.w3c.dom.Node;

public class FluxTriangular extends AbstractFlux {

	protected float w;					// [mph] congested wave speed
	protected float vf;					// [mph] freeflow speed
	
	public FluxTriangular(){
		return;
	}
	
	public FluxTriangular(float xfbar,float xrhocrit,float xrhojam){
		super(xfbar,xrhocrit,xrhojam);
		
		vf = fbar/rhocrit;
		if(Float.isNaN(vf))
			vf = Float.POSITIVE_INFINITY;
		w = fbar/(rhojam-rhocrit);
		if(Float.isNaN(w))
			w = Float.POSITIVE_INFINITY;
	}
	
	public float S(float dens) {
		float s = vf*dens;
		s = Math.min( s , fbar );
		s = Math.max( s , 0f);
		return s;
	}

	public float R(float dens) {
		float r = w*(rhojam-dens);
		r = Math.min( r , fbar );
		r = Math.max( r , 0f);
		return r;
	}
	
	public float F(float dens) {
		if(!Utils.isbetweenclosed(dens,0,rhojam))
			return Float.NaN;
		else
			return Math.min( vf*dens , w*(rhojam-dens) );
	}
	
	public float Fprime(float dens){
		if(!Utils.isbetweenclosed(dens,0,rhojam))
			return Float.NaN;
		if(Utils.lessthan(dens,rhocrit))
			return vf;
		if(Utils.equals(dens,rhocrit)) 
			return 0;
		else
			return -w;
	}

	public float Sinv(float f){	
		f = Math.min(f,fbar);
		f = Math.max(f, 0);
		return f/vf;
	}

	public float Rinv(float f){
		f = Math.min(f,fbar);
		f = Math.max(f, 0);
		return rhocrit+(fbar-f)/w;
	}

	public float Phi(float x,float L,float rhoup,float rhodn){
		
		/*
		boolean upfreeflow = Utils.lessthan(rhoup,rhocrit);
		boolean dnfreeflow = Utils.lessthan(rhodn,rhocrit);
		
		// same regime
		if( upfreeflow==dnfreeflow ){
			System.out.println("I think this should not happen");
			return rhoup;
		}
		else
			return rhocrit;
		*/

		return rhocrit;
	}

	public float partialPhi(float x,float L,float rhoup,float rhodn){
		return 0;
	}
	
	@Override
	public boolean initFromDOM(Node p) throws Exception {

		boolean res = true;
		if (p == null)
			return !res;
		try  {

			Node pp;
			
			pp = p.getAttributes().getNamedItem("capacity");
			if(pp!=null)
				fbar = Float.parseFloat(pp.getNodeValue());
			
			pp = p.getAttributes().getNamedItem("rhocrit");
			if(pp!=null)
				rhocrit = Float.parseFloat(pp.getNodeValue());
			
			pp = p.getAttributes().getNamedItem("rhojam");
			if(pp!=null)
				rhojam = Float.parseFloat(pp.getNodeValue());
			
			vf = fbar/rhocrit;
			w = fbar/(rhojam-rhocrit);
				
		}
		catch(Exception e) {
			res = false;
			throw new Exception(e.getMessage());
		}
		return res;
	}
	
	/*
	
	public float integralPhi(float rhoup, float rhodn) {
		return rhodn;
	}

	public float dPhidZeta(float zeta, float rhoup, float rhodn) {
		return 0f;
	}
	
	public float maxspeed(){
		return vf;
	}
	
	public float evalAvgDensity(float rup,float rdn){
		return rdn;
	}
*/	
}
