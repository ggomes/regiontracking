package regiontracking;

import org.w3c.dom.Node;

public class FluxParabolic extends AbstractFlux {

	public float S(float dens) {
		
		if( Utils.lessthan(dens,0)){
			return Float.NaN;
		} 
		else if( Utils.lessorequalthan(dens,rhocrit)){
			return fbar*dens*(2*rhocrit-dens)/rhocrit/rhocrit;
		}
		else{
			return fbar;	
		}
	}

	public float R(float dens) {

		if( Utils.lessorequalthan(dens,rhocrit)){
			return fbar;
		} 
		else if( Utils.lessorequalthan(dens,2*rhocrit)){
			return fbar*dens*(2*rhocrit-dens)/rhocrit/rhocrit;
		} else
			return 0f;
		
	}
	
	public float F(float dens) {
		if( !Utils.isbetweenclosed(dens,0,2*rhocrit) )
			return Float.NaN;
		else
			return fbar*dens*(2*rhocrit-dens)/rhocrit/rhocrit;
	}
	
	public float Fprime(float dens){
		if(!Utils.isbetweenclosed(dens,0,2*rhocrit))
			return Float.NaN;
		return 2*fbar*(rhocrit-dens)/rhocrit/rhocrit;
	}
	
	public float Sinv(float f){
		f = Math.min(f,fbar);
		f = Math.max(f, 0);
		return rhocrit*(1.0f-(float)Math.sqrt(1.0f-f/fbar));
	}

	public float Rinv(float f){
		f = Math.min(f,fbar);
		f = Math.max(f, 0);
		return rhocrit*(1.0f+(float)Math.sqrt(1.0f-f/fbar));
	}

	public float Phi(float x,float L,float rhoup,float rhodn){
		if(Utils.lessorequalthan(L,0))
			return rhoup;		// for no good reason
		float zeta = x/L;
		return (1-zeta)*rhoup+zeta*rhodn;
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
			
			if( !Utils.equals(2*rhocrit,rhojam) )
				throw new Exception("Rhojam must equal 2*rhocrit in a parabolic fd.");
				
		}
		catch(Exception e) {
			res = false;
			throw new Exception(e.getMessage());
		}
		return res;
		
	}
	
	public float partialPhi(float x,float L,float rhoup,float rhodn){
		return rhodn-rhoup;
	}
	
	/*

	public float integralPhi(float rhoup, float rhodn) {
		return 0.5f*(rhodn+rhoup);
	}
	
	public float maxspeed(){
		return 2*fbar/rhocrit;
	}
	
	public float evalAvgDensity(float rup,float rdn){
		return (rup+rdn)/2.0f;
	}
	*/
	
}
