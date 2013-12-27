package regiontracking;

import java.util.Vector;

public class Globals {

	// parameters and default values
	public static float dt = 1f;					// time step in [sec]
	public static float time;						// current time in [sec]
	public static float Tfinal = 2000;				// simulation time [sec]
	public static int Nbar = 200;					// maximum number of regions
	public static float epsilon = 20f/5280f;		// spatial precision [mile]
	public static float outputdx = 200f/5280f; 		// spatial grid for contour sampling [mile]
	
	// ordered vector of links
	public static Vector<Link> LINKS = new Vector<Link>();
	
	// vector of region generators
	public static Vector<AbstractEdgeGenerator> GEN = new Vector<AbstractEdgeGenerator>();
	
	// vector of macroscopic controllers
	public static Vector<AbstractMacroController> MACROCONTROL = new Vector<AbstractMacroController>();
	
	// vector of macroscopic models
	public static Vector<AbstractMacroModel> MACROMODEL = new Vector<AbstractMacroModel>();	

	public static PiecewiseConstant BCup = null;
	public static PiecewiseConstant BCdn = null;
	public static float currentBCup;
	public static float currentBCdn;
	
	public static Link getLinkById(int id){
		for(int i=0;i<LINKS.size();i++){
			if(LINKS.get(i).id==id)
				return LINKS.get(i);
		}
		return null;
	}
	
	public static Link getNextLink(Link Lup){
		int index = LINKS.indexOf(Lup);
		if(index==-1 | index==LINKS.size()-1)
			return null;
		else
			return LINKS.get(index+1);
	}
	
	public static Link getPreviousLink(Link Ldn){
		int index = LINKS.indexOf(Ldn);
		if(index==-1 | index==0)
			return null;
		else
			return LINKS.get(index-1);
	}
	
	public static Link getLinkByPosition(float x){
		float linkup = 0;
		float linkdn;
		for(int i=0;i<LINKS.size();i++){
			Link L = LINKS.get(i);
			linkdn = linkup + L.Ltotal;
			if( Utils.isbetweenclosed(x,linkup,linkdn))
				return L;
			linkup = linkdn;
		}
		return null;	
	}
	
	/*
	public static AbstractFlux getFluxByPosition(float x){
		Link L = getLinkByPosition(x);
		if(L!=null)
			return L.FD;
		else
			return null;
	}*/
	
	public static void printregions(){
		for(int i=0;i<LINKS.size();i++){
			for(int j=0;j<LINKS.get(i).RGNS.size();j++){
				Region R = LINKS.get(i).RGNS.get(j);
				System.out.println(i + "\t" + j + "\t" + R.toString());
			}
		}
	}
	
			
	
}
