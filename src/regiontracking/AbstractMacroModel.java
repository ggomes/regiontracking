package regiontracking;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import org.w3c.dom.Node;

public class AbstractMacroModel implements Configurable {

	protected int numcells;
	protected float dx;
	protected float dtoverdx;
	public String prefix;
	public BufferedWriter out_density;

	protected Vector<Float> dty = new Vector<Float>();
	protected Vector<Float> flw = new Vector<Float>();
	protected Vector<AbstractFlux> FDgrid = new Vector<AbstractFlux>();
	
	public void initialize(float Ltotal){
		
		numcells = (int) Math.floor(Ltotal/dx);
		dtoverdx = Globals.dt/dx/3600;
		
		// initial condition
		Link L;
		float dens;
		float pos = 0;
		for(int i=0;i<numcells;i++){
			pos += dx;
			pos = Utils.greaterorequalthan(pos,Ltotal) ? Ltotal : pos;
			L = Globals.getLinkByPosition(pos);
			if(L==null)
				System.out.println("Error: getLinkByPosition returned null");
			FDgrid.add(L.FD);
			dens = L.sampleBeforeAndAfter(pos-L.xup).get(0);		
			dty.add(dens);
			flw.add(L.FD.F(dens));
		}
		
		// write parameters
		try {
			BufferedWriter z = new BufferedWriter(new FileWriter(prefix + "_parameters.m"));
			z.write("dx=" + dx + ";\n");
			z.write("numcells=" + numcells + ";\n");
			z.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	};
	
	public void update(){
		return;
	}

	public void write(){
		try {
			String str = dty.toString();
			out_density.write(str.substring(1, str.length()-1) + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public void close(){
		try {
			out_density.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	@Override
	public boolean initFromDOM(Node p) throws Exception {
		boolean res = true;
		if (p == null)
			return !res;
		try 
		{	
			Node pp;
			pp = p.getAttributes().getNamedItem("prefix");
			if(pp!=null)
				prefix = pp.getNodeValue();
			
			pp = p.getAttributes().getNamedItem("dx");
			if(pp!=null)
				dx = Float.parseFloat(pp.getNodeValue());
		}
		catch(Exception e) {
			res = false;
		}
		return res;		
	}

	public boolean checkCFL(float maxspeed) {
		return true;
	}	

	public void setFDat(float x,AbstractFlux newFD){
		FDgrid.set(Math.round(x/dx),newFD);
	}
	
}
