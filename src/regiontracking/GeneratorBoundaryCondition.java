package regiontracking;

import static regiontracking.Globals.*;

public class GeneratorBoundaryCondition extends AbstractEdgeGenerator {

	public PiecewiseConstant V;
	public boolean isupstream;
	
	public GeneratorBoundaryCondition(Link L,boolean isup,PiecewiseConstant v){
		super(L,L);
		isupstream = isup;
		V = v;
	}

	@Override
	protected boolean triggercondition() {
		boolean regionremoved = super.triggercondition();
		return regionremoved | V.switchtimewithin(time-dt/2,time+dt/2);
	}
		
	@Override
	protected void generateregions() {
	
		float bcf = (Float) V.eval(time);
		float rho;
		float bcrhoMinus;
		float bcrhoPlus;
		Link myLink;
		
		if(isupstream){
			rho = Ldn.RGNS.firstElement().getRhoup();
			myLink = Ldn;
		}
		else{
			rho = Lup.RGNS.lastElement().getRhodn();
			myLink = Lup;
		}
		
		bcrhoMinus = myLink.FD.Sinv(bcf);
		bcrhoPlus = myLink.FD.Rinv(bcf);

		// copy to global scope for macro models
		if(isupstream)
			Globals.currentBCup = bcf;
		else
			Globals.currentBCdn = bcf;

		// generate new region if it will move ...
		if( !Utils.equals(bcrhoMinus,rho) &  !Utils.equals(bcrhoPlus,rho) ){

			// ... in the right direction
			if( isupstream & myLink.FD.chordslope(bcrhoMinus,rho)>0 )
				myLink.insert(new Region(RegionType.constant,bcrhoMinus,bcrhoMinus,0f,0f,myLink.FD));

			if( !isupstream & myLink.FD.chordslope(bcrhoPlus,rho)<0 )
				myLink.insert( new Region(RegionType.constant,bcrhoPlus,bcrhoPlus,myLink.Ltotal,0f,myLink.FD) );
		}

	}

}
