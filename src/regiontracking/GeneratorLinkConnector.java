package regiontracking;

import java.util.Vector;

public class GeneratorLinkConnector extends AbstractEdgeGenerator {

	public GeneratorLinkConnector(Link up,Link dn) {
		super(up,dn);
	}
	
	@Override
	protected boolean triggercondition() {		
		return super.triggercondition();
	}

	@Override
	protected void generateregions() {
		
		Region rup = Lup.RGNS.lastElement();
		Region rdn = Ldn.RGNS.firstElement();
		
		float fmax = Float.POSITIVE_INFINITY;
		if(Lup.myDnController!=null)
			fmax = Lup.myDnController.getFlowConstraint();

		Vector<Vector<Region>> RR = RiemannSolver.solve(rup,rdn,Lup.FD,Ldn.FD,fmax);
		
		Lup.insertLast(RR.get(0));
		Ldn.insertFirst(RR.get(1));
		
	}

}
