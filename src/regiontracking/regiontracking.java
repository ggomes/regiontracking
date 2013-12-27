package regiontracking;

import static regiontracking.Globals.BCdn;
import static regiontracking.Globals.BCup;
import static regiontracking.Globals.GEN;
import static regiontracking.Globals.LINKS;
import static regiontracking.Globals.MACROCONTROL;
import static regiontracking.Globals.MACROMODEL;
import static regiontracking.Globals.Nbar;
import static regiontracking.Globals.Tfinal;
import static regiontracking.Globals.dt;
import static regiontracking.Globals.epsilon;
import static regiontracking.Globals.outputdx;
import static regiontracking.Globals.time;

import java.io.File;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class regiontracking implements Configurable {

	private OutputWriter out;

	//////////////////////////////////////////////////////////////////////////////////////

	public static void main(String[] args) {
		
		long startTime = Utils.getCpuTime();

		boolean dooutput;
		
		String inputfilename;
		
		if(args.length>0)
			inputfilename = args[0];
		else
			inputfilename = "C:\\Users\\gomes\\workspace\\regiontracking\\ex2.xml";
		
		if(args.length>1)
			dooutput = Boolean.parseBoolean(args[1]);
		else
			dooutput = true;

		regiontracking RT = new regiontracking(dooutput);
		try {
			File config = new File(inputfilename);
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("file:" + config.getAbsolutePath());
			RT.initFromDOM(doc.getChildNodes().item(0));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		try {
			RT.initialize();
		} catch (Exception e) {
			System.out.println("Initialization error: " + e.getMessage());
			return;
		}

		// run it
		RT.run();

		float totalTime = (Utils.getCpuTime()-startTime)/1000000000f;

		System.out.println("done in " +totalTime + " seconds");
	}

	//////////////////////////////////////////////////////////////////////////////////////

	public regiontracking(boolean xdowrite){
		out = new OutputWriter(xdowrite);
	}	

	private void initialize() throws Exception{
		int i,j,k;
		float Ltotal = 0;
		Region rup,rdn;
		Link Lup,Ldn;
		float posup,posdn;
		
		// create link connecting generators
		for(i=0;i<LINKS.size()-1;i++){
			Lup = LINKS.get(i);
			Ldn = LINKS.get(i+1);
			GeneratorLinkConnector G = new GeneratorLinkConnector(Lup,Ldn);
			Lup.myDnEdgeGenerator = G;
			Ldn.myUpEdgeGenerator = G; 
			GEN.add(G);
			Ltotal += Lup.Ltotal;
		}

		Ltotal += LINKS.lastElement().Ltotal;

		// solve Riemann at all region boundaries of initial condition
		Vector<Region> toinsertinLup = new Vector<Region>();
		Vector<Region> toinsertinLdn = new Vector<Region>();
		Link thevoid = new Link(0);
		thevoid.FD = new FluxTriangular(Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY);
		thevoid.RGNS.add(new Region(RegionType.constant,0,0,0,1,thevoid.FD));

		for(i=0;i<LINKS.size();i++){
			Lup = LINKS.get(i);
			if(i<LINKS.size()-1)
				Ldn = LINKS.get(i+1);
			else
				Ldn = thevoid;
			toinsertinLup.clear();
			toinsertinLdn.clear();
			
			for(j=0;j<Lup.RGNS.size();j++){
				
				// skip upstream and downstream boundaries
				//if( i==LINKS.size()-1 & j==Lup.RGNS.size()-1 )
				//	continue;
				
				// skip recently inserted regions
				if( Utils.lessorequalthan(Lup.RGNS.get(j).getL(),0)  )
					continue;

				// assign rup and rdn
				Vector<Vector<Region>> RR;
				if(j<Lup.RGNS.size()-1){				// within a link
					rup = Lup.RGNS.get(j);
					rdn = Lup.RGNS.get(j+1);
					posup = rdn.xup;
					posdn = Float.NaN;
					RR = RiemannSolver.solve(rup,rdn,Lup.FD,Lup.FD,Float.POSITIVE_INFINITY); //RR.get(1) will be empty.
				}
				else{
					rup = Lup.RGNS.lastElement();		// accross link boundaries
					rdn = Ldn.RGNS.firstElement();
					posup = Lup.Ltotal;
					posdn = 0;	
					RR = RiemannSolver.solve(rup,rdn,Lup.FD,Ldn.FD,Float.POSITIVE_INFINITY);
				}
				
				for(k=0;k<RR.get(0).size();k++){
					RR.get(0).get(k).xup = posup;
					toinsertinLup.add(RR.get(0).get(k));
				}
				for(k=0;k<RR.get(1).size();k++){
					RR.get(1).get(k).xup = posdn;
					toinsertinLdn.add(RR.get(1).get(k));
				}
			}
			Lup.insert(toinsertinLup);
			Ldn.insert(toinsertinLdn);
		}
		
		//Globals.printregions();
		
		// initialize macro models
		for(i=0;i<MACROMODEL.size();i++)
			MACROMODEL.get(i).initialize(Ltotal);
		
		// test CFL condition
		float maxspeed = Float.NEGATIVE_INFINITY;
		for(i=0;i<LINKS.size();i++)
			maxspeed = Math.max( maxspeed , LINKS.get(i).FD.maxspeed() );
		boolean cflpassed = true;
		for(i=0;i<MACROMODEL.size();i++)
			cflpassed &= MACROMODEL.get(i).checkCFL(maxspeed);

		if(!cflpassed)
			throw new Exception("CFL DID NOT PASS!!!!");
	}

	private void run(){
		
		int i;
		
		time=0f;
		out.writeparameters();
		out.open();
		out.write();
		while(time<Tfinal){									

			for(i=0;i<GEN.size();i++)
				GEN.get(i).execute();
			
			// compute boundary speeds .....................
			for(i=0;i<LINKS.size();i++)
				LINKS.get(i).updatespeeds();		
			
			// compute boundary densities .....................
			for(i=0;i<LINKS.size();i++)
				LINKS.get(i).updateboundarydensities();
			
			/*
			for(i=0;i<LINKS.size();i++){
				Link L = LINKS.get(i);
				if(L.RGNS.firstElement().rhouptied){
					float rho = LINKS.get(i-1).RGNS.lastElement().getRhodn();
					L.RGNS.firstElement().setRhoup(rho);
				}
				if(L.RGNS.lastElement().rhodntied){
					float rho = LINKS.get(i+1).RGNS.firstElement().getRhoup();
					L.RGNS.lastElement().setRhodn(rho);
				}
			}
			*/
			
			// move region boundaries ......................
			for(i=0;i<LINKS.size();i++)
				LINKS.get(i).updatelengths();
			
			// remove extra regions
			for(i=0;i<LINKS.size();i++)
				LINKS.get(i).removeextraregions();

			// update macro controllers
			if(MACROMODEL.size()>0)
				for(i=0;i<MACROCONTROL.size();i++)
					MACROCONTROL.get(i).update();
			
			// update the macro models
			for(i=0;i<MACROMODEL.size();i++)
				MACROMODEL.get(i).update();
			
			time += dt;
			
			// write to files ........................
			out.write();
			
		}
		out.close();
	}

	@Override
	public boolean initFromDOM(Node p) throws Exception {
		boolean res = true;
		if (p == null)
			return !res;
		try  {
			int i;
			
			if(p.hasChildNodes()){

				NodeList c = p.getChildNodes();
			
				for(i=0;i<c.getLength();i++){
					Node pp = c.item(i);
					String nodename = pp.getNodeName();
					String text = pp.getTextContent().trim();
					
					if(nodename.equals("Nbar"))
						Nbar = Integer.parseInt(text);

					if(nodename.equals("dt"))
						dt = Float.parseFloat(text);

					if(nodename.equals("Tfinal"))
						Tfinal = Integer.parseInt(text);
					
					if(nodename.equals("outputdx"))
						outputdx = Float.parseFloat(text);
					
					if(nodename.equals("epsilon"))
						epsilon = Float.parseFloat(text);
				
					if (nodename.equals("LinkList"))
						initFromDOMLinkList(pp);
				}				

				for(i=0;i<c.getLength();i++){
					Node pp = c.item(i);
					String nodename = pp.getNodeName();

					if (nodename.equals("BoundaryConditions"))
						initFromDOMBoundaryConditions(pp);

					if (nodename.equals("TrafficSignals")) 
						initFromDOMTrafficSignals(pp);	

					if (nodename.equals("MacroscopicModels")) 
						initFromDOMMacroscopicModels(pp);
				}
			}
		}
		catch(Exception e) {
			res = false;
		}
		return res;
	}

	private boolean initFromDOMLinkList(Node p) {
		boolean res = true;
		if (p == null)
			return !res;
		try 
		{
			if(p.hasChildNodes()){
				float xup=0;
				NodeList c = p.getChildNodes();
				for(int i=0;i<c.getLength();i++){
					Node pp = c.item(i);
					if(pp.getNodeName().equals("link")){
						Link L = new Link(xup);
						res &= L.initFromDOM(pp);
						LINKS.add(L);
						xup+=L.Ltotal;
					}
				}
			}
		}
		catch(Exception e) {
			res = false;
		}
		return res;
	}

	private boolean initFromDOMBoundaryConditions(Node p){
		
		boolean res = true;
		if (p == null)
			return !res;
		try 
		{
			Link thisLink;
			if(p.hasChildNodes()){
				NodeList c = p.getChildNodes();
				for(int i=0;i<c.getLength();i++){
					Node pp = c.item(i);
					if(pp.getNodeName().equals("upstream")){
						PiecewiseConstant xBCup = new PiecewiseConstant();
						xBCup.initFromDOM(pp);
						Globals.BCup = xBCup;
						thisLink = LINKS.firstElement();
						GeneratorBoundaryCondition G = new GeneratorBoundaryCondition(thisLink,true,BCup);	
						thisLink.myUpEdgeGenerator = G;	
						GEN.add(G);
					}
					if(pp.getNodeName().equals("downstream")){
						PiecewiseConstant xBCdn = new PiecewiseConstant();
						xBCdn.initFromDOM(pp);
						Globals.BCdn = xBCdn;
						thisLink = LINKS.lastElement();
						GeneratorBoundaryCondition G = new GeneratorBoundaryCondition(thisLink,false,BCdn);	
						thisLink.myDnEdgeGenerator = G;
						GEN.add(G);
					}
				}
			}
		}
		catch(Exception e) {
			res = false;
		}
		return res;	
	}

	private boolean initFromDOMTrafficSignals(Node p){

		boolean res = true;
		if (p == null)
			return !res;
		try 
		{			
			if(p.hasChildNodes()){
				NodeList c = p.getChildNodes();
				for(int i=0;i<c.getLength();i++){
					Node pp = c.item(i);
					if(pp.getNodeName().equals("signal")){
						
						Node ppp = pp.getAttributes().getNamedItem("algorithm");
						if(ppp!=null){
							String alg = ppp.getNodeValue();
							
							if(alg.equalsIgnoreCase("pretimed")){
								GeneratorSignalFixedTime S = new GeneratorSignalFixedTime();
								S.initFromDOM(pp);
								GEN.add(S);
								
								MacroControllerSignalFixedTime MS = new MacroControllerSignalFixedTime();
								MS.initFromDOM(pp);
								MACROCONTROL.add(MS);
							}
						}
					}

				}
			}
		}
		catch(Exception e) {
			res = false;
		}
		return res;		
	}

	private boolean initFromDOMMacroscopicModels(Node p){

		boolean res = true;
		if (p == null)
			return !res;
		try 
		{			
			if(p.hasChildNodes()){
				NodeList c = p.getChildNodes();
				for(int i=0;i<c.getLength();i++){
					Node pp = c.item(i);
					if(pp.getNodeName().equals("model")){
						Node ppp = pp.getAttributes().getNamedItem("modelname");
						if(ppp!=null){
							String name = ppp.getNodeValue();
							if(name.equalsIgnoreCase("ctm")){
								MacroModelCTM M = new MacroModelCTM();
								M.initFromDOM(pp);
								MACROMODEL.add(M);
							}
						}
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
