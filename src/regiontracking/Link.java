package regiontracking;

import static regiontracking.Globals.dt;

import java.util.Vector;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Link implements Configurable {
	
	public int id;
	public float xup;
	public float Ltotal;
	public AbstractFlux FD;
	public Vector<Region> RGNS = new Vector<Region>();
	
	public AbstractEdgeGenerator myUpEdgeGenerator = null;
	public AbstractEdgeGenerator myDnEdgeGenerator = null;
	public AbstractEdgeGenerator myDnController = null;
	
	public Link(float x){
		xup = x;
	}
	
	public void setEndpointGenerators(GeneratorLinkConnector up,GeneratorLinkConnector dn){
		myUpEdgeGenerator = up;
		myDnEdgeGenerator = dn;
	}
	
	public void removeRegion(int index){
		
		int numR = RGNS.size();
		
		if(index<0 | index>numR-1 )
			return;
		if(numR<=1)		// don't remove last region
			return;

		Region prevR,nextR;
		
		Region thisR = RGNS.get(index);

		if(index==0){									// remove first region
			nextR = RGNS.get(index+1);
			nextR.vup = 0f;
			myUpEdgeGenerator.msg_dnregionremoved(); 	// notify upstream generator
		}
		
		else if(index==numR-1){							// remove last region
			prevR = RGNS.get(index-1);
			prevR.vdn = 0f; 	
			myDnEdgeGenerator.msg_upregionremoved();	// notify downstream generator
		}

		else {			// remove internal region
			
			prevR = RGNS.get(index-1);
			nextR = RGNS.get(index+1);
			
			float v = FD.chordslope(prevR.getRhodn(),nextR.getRhoup());
			prevR.vdn = v;
			nextR.vup = v;
			
			float newboundary = (nextR.xup+thisR.xup)/2;
			prevR.setL(newboundary - prevR.xup);
			nextR.xup = newboundary;
		}	
			
		RGNS.remove(index);
	}
	
	// insert region at position x
	public void insert(Region r){
		Vector<Region> V = new Vector<Region>();
		V.add(r);
		insert(V);
//		untie();
	}
	
	// insert vector of regions at position x
	public void insert(Vector<Region> V){
		
		int i,j;
		int insertat = -1;
		
		if(V.isEmpty())
			return;
		
		// insert position
		float x = V.get(0).xup;
		
		// should be the same for all inserted regions
		for(i=1;i<V.size();i++)
			if(V.get(i).xup!=x)
				return;
		
		if(Utils.greaterthan(x,Ltotal))
			return;
		
		
		// convert flat rarefaction waves to constants
		for(i=0;i<V.size();i++){
			Region R = V.get(i);
			if(R.type==RegionType.rarefaction){
				float rhoup = R.myFD.Phi(0,1,R.getRhoup(),R.getRhodn());
				float rhodn = R.myFD.Phi(1,1,R.getRhoup(),R.getRhodn());
				if( Utils.equals(rhoup,rhodn)){
					R.type = RegionType.constant;
					R.setRhoup(rhoup);
					R.setRhodn(rhoup);
				}
			}
		}
			
		for(i=0;i<RGNS.size();i++){
			Region thisR = RGNS.get(i);
			float nextxup;
			if(i<RGNS.size()-1)
				nextxup = RGNS.get(i+1).xup;
			else
				nextxup = Ltotal;

			if( Utils.equals(x,thisR.xup) ){
				insertat = i;
				break;
			}
			
			if( Utils.lessthan(x,nextxup) ){
				float cutval = thisR.sample(x-thisR.xup);
				Region newDnR = new Region(thisR.type,cutval,thisR.getRhodn(),x,nextxup-x,FD);
				thisR.setRhodn(cutval);
				thisR.setL(x-thisR.xup);
				V.add(newDnR);
				insertat = i+1;
				break;
			}
		}
		
		if(insertat==-1 & Utils.equals(x,Ltotal) )
			insertat=RGNS.size();
		
		if(insertat>=0){
			for(j=V.size()-1;j>=0;j--)
				RGNS.insertElementAt(V.get(j),insertat);
		}

//		untie();
	}
	
	// insert at x=0
	public void insertFirst(Vector<Region> V){
		for(int i=0;i<V.size();i++)
			if(V.get(i)!=null)
				V.get(i).xup = 0f;
		insert(V);
	}
	
	// insert at x=0
	public void insertLast(Vector<Region> V){
		for(int i=0;i<V.size();i++)
			if(V.get(i)!=null)
				V.get(i).xup = Ltotal;
		insert(V);
	}
	
	// return the left and right hand limits of density
	public Vector<Float> sampleBeforeAndAfter(float x){

		Vector<Float> FF = new Vector<Float>();
		FF.add(Float.NaN);
		FF.add(Float.NaN);
		
		if(!Utils.isbetweenclosed(x,0,Ltotal))
			return FF;
		
		if(Utils.equals(x,0)){
			FF.set(1,RGNS.firstElement().getRhoup());
			return FF;
		}

		if(Utils.equals(x,Ltotal)){
			FF.set(0,RGNS.lastElement().getRhodn());
			return FF;
		}
			
		Vector<Region> RR = getRegionsBedoreAndAfter(x);
		Region thisR;
		
		thisR = RR.get(0);
		if(thisR!=null)
			FF.set(0,thisR.sample(x-thisR.xup));

		thisR = RR.get(1);
		if(thisR!=null)
			FF.set(1,thisR.sample(x-thisR.xup));
		
		return FF;
	}
	
	// returns a vector with 2 elements.
	// r[0] is immediately upstream of x, r[1] immediately downstream.
	// r[0]=null if x=0. r[1]=null if x=length
	// r[0] and r[1]=null if x not in [0,L]
	// r[0]=r[1] if x is within a region
	// r[0]!=r[1] if x is on a boundary
	public Vector<Region> getRegionsBedoreAndAfter(float x){
		
		Vector<Region> RR = new Vector<Region>();
		RR.add(null);
		RR.add(null);
		
		Region thisR;
		float nextRxup;
		
		// x outside of the domain
		if( !Utils.isbetweenclosed(x,0,Ltotal) )
			return RR;

		// x on upstream boundary
		if(Utils.equals(x,0f)){
			//r.add(null);
			//r.add(RGNS.firstElement());
			RR.set(1,RGNS.firstElement());
			return RR;
		}
	
		for(int i=0;i<RGNS.size();i++){
			
			thisR = RGNS.get(i);
			if(i<RGNS.size()-1)
				nextRxup = RGNS.get(i+1).xup;
			else
				nextRxup = Ltotal;
			
			// within this region
			if(Utils.lessthan(x,nextRxup)){
				RR.set(0,thisR);
				RR.set(1,thisR);
				return RR;
			}

			// on downstream boundary
			if( Utils.equals(x,nextRxup) ){
				RR.set(0,thisR);				
				if(i<RGNS.size()-1)
					RR.set(1,RGNS.get(i+1));
				return RR;
			}
			
		}
		
		return null;	// should never get here
	}
	
	public void updateboundarydensities(){
		for(int i=0;i<RGNS.size();i++){
			Region R = RGNS.get(i);
			//if(!R.rhouptied)
				R.updateRhoUp();
			//if(!R.rhodntied)
				R.updateRhoDn();
		}
	}
	
	public void updatespeeds(){

		int i;
		float sigma;
		Region thisR,prevR;
		
		thisR = RGNS.firstElement();
		thisR.vup = 0f;
		
		for(i=1;i<RGNS.size();i++){
			thisR = RGNS.get(i);
			prevR = RGNS.get(i-1);
			sigma = FD.chordslope(prevR.getRhodn(),thisR.getRhoup());
			thisR.vup = sigma;
			prevR.vdn = sigma;
		}

		thisR = RGNS.lastElement();
		thisR.vdn = 0f;
		
	}

	public void updatelengths(){
		for(int i=0;i<RGNS.size();i++)
			RGNS.get(i).updateL(dt);
		
		Region r;
		r = RGNS.firstElement();
		r.xup = 0f;			// GCG make this more robust.
		r = RGNS.lastElement();
		r.setL(Ltotal-r.xup);
		
	}	
	
	public void removeextraregions(){

		int i;
		
		// remove extra regions ........................
		for(i=0;i<RGNS.size();i++){
			if(RGNS.get(i).LengthIsNotPositive())
				removeRegion(i);
		}
	}

	/*
	private void untie(){
		int i;
		for(i=1;i<RGNS.size();i++)
			RGNS.get(i).rhouptied = false;
		for(i=0;i<RGNS.size()-1;i++)
			RGNS.get(i).rhodntied = false;
	}
	*/
	
	@Override
	public boolean initFromDOM(Node p) throws Exception {
		boolean res = true;
		if (p == null)
			return !res;
		try  {
			
			float length,dens;
			myUpEdgeGenerator = null;
			myDnEdgeGenerator = null;
			float filledto = 0f;
			
			Node pp;
			
			pp = p.getAttributes().getNamedItem("id");
			if(pp!=null)
				id = Integer.parseInt(pp.getNodeValue());
			
			pp = p.getAttributes().getNamedItem("length");
			if(pp!=null)
				Ltotal = Float.parseFloat(pp.getNodeValue());

			if(p.hasChildNodes()){

				NodeList c = p.getChildNodes();
			
				for(int i=0;i<c.getLength();i++){
					pp = c.item(i);
					
					if(pp.getNodeName().equals("fd")){
						
						Node fdtype = pp.getAttributes().getNamedItem("type");
						
						if(fdtype!=null){
							
							if(fdtype.getTextContent().trim().equalsIgnoreCase("triangular")){			// GCG FIX THIS
								FD = new FluxTriangular();
								res &= FD.initFromDOM(pp);
							}
							if(fdtype.getTextContent().trim().equalsIgnoreCase("parabolic")){
								FD = new FluxParabolic(); 
								res &= FD.initFromDOM(pp);
							}
						}
					}
				}

				for(int i=0;i<c.getLength();i++){
					pp = c.item(i);
					if(pp.getNodeName().equals("initialcondition")){
						PiecewiseConstant ic = new PiecewiseConstant();
						ic.initFromDOM(pp);				
						for(int j=1;j<ic.x.size();j++){
							length = ic.x.get(j) - filledto;
							dens = (Float) ic.y.get(j-1);
							if(Utils.greaterthan(length,0f)){
								RGNS.add(new Region(RegionType.constant,dens,dens,filledto,length,FD));
								filledto += length;
							}
						}
						
						
						length = Ltotal-filledto;
						dens = (Float) ic.y.lastElement();
						if(Utils.greaterthan(length,0f))
							RGNS.add(new Region(RegionType.constant,dens,dens,filledto,length,FD));
						else
							return false;
								
						
					}						
				}
			}
			
		}
		catch(Exception e) {
			res = false;
			throw new Exception(e.getMessage());
		}
		return res;
	}
		
}
