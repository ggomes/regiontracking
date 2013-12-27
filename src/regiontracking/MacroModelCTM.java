package regiontracking;

import java.util.Vector;

public class MacroModelCTM extends AbstractMacroModel {

	Vector<Float> S = new Vector<Float>();		// [vph]
	Vector<Float> R = new Vector<Float>();		// [vph]
	Vector<Float> bF = new Vector<Float>();		// [vph]
	
	@Override
	public void update() {

		int i;
		float rho;
		AbstractFlux F;
		
		for(i=0;i<numcells;i++){
			F = FDgrid.get(i);
			rho = dty.get(i);
			S.set(i,F.S(rho));
			R.set(i,F.R(rho));
		}

		// boundary flows
		bF.clear();
		bF.add(Math.min(R.firstElement(),Globals.currentBCup));
		for(i=0;i<numcells-1;i++)
			bF.add(Math.min(S.get(i),R.get(i+1)));
		bF.add(Math.min(S.lastElement(),Globals.currentBCdn));

		// densities
		float newdty;
		for(i=0;i<numcells;i++){
			newdty = dty.get(i) + (bF.get(i)-bF.get(i+1))*dtoverdx;
			dty.set(i, newdty);
		}

	}

	@Override
	public void initialize(float Ltotal) {
		super.initialize(Ltotal);
		
		for(int i=0;i<numcells;i++){
			S.add(null);
			R.add(null);
		}
	}

	@Override
	public boolean checkCFL(float maxspeed) {
		return Utils.greaterorequalthan(3600*dx/Globals.dt,maxspeed);
	}

}
