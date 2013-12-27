package regiontracking;

import static regiontracking.Globals.dt;
import static regiontracking.Globals.time;

import java.util.Vector;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GeneratorSignalFixedTime extends AbstractEdgeGenerator implements Configurable {

	private boolean begingreen;
	private boolean beginred;
	private float green;		// green time in [sec]
	private float cycle;		// cycle time in [sec]

	@Override
	protected boolean triggercondition() {
		begingreen = Utils.equalsmod(time,cycle);
		beginred = Utils.equalsmod(time-green,cycle);
		return begingreen | beginred;
	}
	
	@Override
	protected void generateregions() {

		float flowconst = 0f;
		if( begingreen )
			flowconst = Ldn.FD.maxflow();
		if( beginred )
			flowconst = 0;
		Region Rup = Lup.RGNS.lastElement();
		Region Rdn = Ldn.RGNS.firstElement();
		Vector<Vector<Region>> RR = RiemannSolver.solve(Rup,Rdn,Lup.FD,Ldn.FD,flowconst);		
		Lup.insertLast(RR.get(0));	
		Ldn.insertFirst(RR.get(1));		
	}
	
	@Override
	public boolean initFromDOM(Node p) throws Exception {

		boolean res = true;
		if (p == null)
			return !res;
		try 
		{
			int i;
			Node pp;

			pp = p.getAttributes().getNamedItem("link");
			if(pp!=null){
				int linkid = Integer.parseInt(pp.getNodeValue());
				Lup = Globals.getLinkById(linkid);
				Ldn = Globals.getNextLink(Lup);
				if(Lup.myDnController!=null)
					return false;		// error multiple controlers on a link
				Lup.myDnController = this;
			}
			
			if(p.hasChildNodes()){
				NodeList c = p.getChildNodes();
				for(i=0;i<c.getLength();i++){
					pp = c.item(i);
					if(pp.getNodeName().equals("greentime")){
						float x = Float.parseFloat(pp.getTextContent().trim());
						green = Math.round(x/dt)*dt;
					}
					if(pp.getNodeName().equals("cycle")){
						float x = Float.parseFloat(pp.getTextContent().trim());
						cycle = Math.round(x/dt)*dt;
					}
				}
			}
		}
		catch(Exception e) {
			res = false;
		}
		return res;	
	}

	@Override
	public float getFlowConstraint() {
		if(time%cycle<green)
			return Float.POSITIVE_INFINITY;
		else
			return 0f;
	}
	
}
