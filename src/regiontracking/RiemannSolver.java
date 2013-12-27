package regiontracking;

import java.util.Vector;

public class RiemannSolver {
	
	// discontinuous F, flow constrained
	public static Vector<Vector<Region>> solve(Region rup,Region rdn,AbstractFlux Fup,AbstractFlux Fdn,float fconst){

		float rhoup = rup.getRhodn();
		float rhodn = rdn.getRhoup();
		float pos = Float.NaN;
		Vector<Region> RRup = new Vector<Region>();
		Vector<Region> RRdn = new Vector<Region>();

		float Sup = Fup.S(rhoup);
		float Rdn = Fdn.R(rhodn);
		boolean restricting = Utils.lessthan(fconst,Sup) & Utils.lessthan(fconst,Rdn);
		
		// flow constraint is restricting OR discontinuous
		// then possibly insert multiple fans
		if( restricting | !Fup.equals(Fdn)){
			
			float fstar = Math.min( Math.min(fconst,Sup) , Rdn );
			float rhoupstar = Fup.Rinv(fstar);
			float rhodnstar = Fdn.Sinv(fstar);
			
			// upstream 
			if(Utils.greaterorequalthan(rhoup,rhoupstar)){		// fan
				float Fprimerhoup = Fup.Fprime(rhoup);
				float Fprimerhoupstar = Fup.Fprime(rhoupstar);
				if( Utils.lessorequalthan(Fprimerhoupstar,0) & Utils.lessthan(Fprimerhoup,Fprimerhoupstar) )
					RRup.add(new Region(RegionType.rarefaction,rhoup,rhoupstar,pos,0f,Fup));
				if( Utils.lessthan(Fprimerhoupstar,0) & Utils.lessorequalthan(Fprimerhoup,Fprimerhoupstar) )
					RRup.add(new Region(RegionType.constant,rhoupstar,rhoupstar,pos,0f,Fup));
			}
			
			else{		// shock
				float sigma = Fup.chordslope(rhoup,rhoupstar);
				if(Utils.lessthan(sigma,0))
					RRup.add(new Region(RegionType.constant,rhoupstar,rhoupstar,pos,0f,Fup));
			}

			// downstream 
			if(Utils.greaterorequalthan(rhodnstar,rhodn)){		// fan
				float Fprimerhodnstar = Fdn.Fprime(rhodnstar);
				float Fprimerhodn = Fdn.Fprime(rhodn);
	
				if( Utils.greaterthan(Fprimerhodnstar,0) & Utils.greaterorequalthan(Fprimerhodn,Fprimerhodnstar) )
					RRdn.add(new Region(RegionType.constant,rhodnstar,rhodnstar,pos,0f,Fdn));
			
				if( Utils.greaterorequalthan(Fprimerhodnstar,0) & Utils.greaterthan(Fprimerhodn,Fprimerhodnstar) ){
					Region rnew = new Region(RegionType.rarefaction,rhodnstar,rhodn,pos,0f,Fdn);
					RRdn.add(rnew);
				}
					
			}
			else{		// shock
				float sigma = Fdn.chordslope(rhodnstar,rhodn);
				if(Utils.greaterthan(sigma,0))
					RRdn.add(new Region(RegionType.constant,rhodnstar,rhodnstar,pos,0f,Fdn));
			}
		}
		
		// non-restricting constraint on uniform segment
		// at most insert 1 fan
		else{
			if(Utils.greaterthan(rhoup,rhodn))
				RRup.add(new Region(RegionType.rarefaction,rhoup,rhodn,pos,0f,Fup));
		}
		
		Vector<Vector<Region>> RR = new Vector<Vector<Region>>();
		RR.add(RRup);
		RR.add(RRdn);
		return RR;
		
	}
	
}
