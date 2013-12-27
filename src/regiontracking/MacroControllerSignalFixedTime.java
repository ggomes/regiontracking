package regiontracking;

import static regiontracking.Globals.MACROMODEL;
import static regiontracking.Globals.dt;
import static regiontracking.Globals.time;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MacroControllerSignalFixedTime extends AbstractMacroController implements Configurable {

	private float green;		// green time in [sec]
	private float cycle;		// cycle time in [sec]
	private FluxTriangular redFD = new FluxTriangular(0f,1f,1f);
	
	@Override
	public void update() {

		if( Utils.equalsmod(time,cycle) ){
			// assign greenFD to macro models
			for(int i=0;i<MACROMODEL.size();i++)
				MACROMODEL.get(i).setFDat(myLink.xup+myLink.Ltotal,myLink.FD);
		}
		
		if( Utils.equalsmod(time-green,cycle) ){
			// assign redFD to macro models
			for(int i=0;i<MACROMODEL.size();i++)
				MACROMODEL.get(i).setFDat(myLink.xup+myLink.Ltotal,redFD);
		}
		
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
				myLink = Globals.getLinkById(linkid);
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

}
