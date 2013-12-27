package regiontracking;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import static regiontracking.Globals.*;

public class OutputWriter {

	private BufferedWriter out_t;
	private BufferedWriter out_density;
	private boolean dowrite;
	
	public OutputWriter(boolean xdowrite){
		dowrite = xdowrite;
	}
	
	public void open(){
		try {
			out_t = new BufferedWriter(new FileWriter("out_t.txt"));
			out_density = new BufferedWriter(new FileWriter("out_density.txt"));
			
			for(int i=0;i<MACROMODEL.size();i++){
				AbstractMacroModel M = MACROMODEL.get(i);
				M.out_density = new BufferedWriter(new FileWriter(M.prefix+"_density.txt"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeparameters() {
		if(!dowrite)
			return;
		try {
			float maxrho = Float.NEGATIVE_INFINITY;
			float L = 0f;
			for(int i=0;i<LINKS.size();i++){
				maxrho = Math.max(maxrho,LINKS.get(i).FD.maxdensity());
				L += LINKS.get(i).Ltotal;
			}
			BufferedWriter z = new BufferedWriter(new FileWriter("out_parameters.m"));
			z.write("dt=" + dt + ";\n");
			z.write("dx=" + Globals.outputdx + ";\n");
			z.write("T=" + Tfinal + ";\n");
			z.write("L=" + L + ";\n");
			z.write("Nbar=" + Nbar + ";\n");
			z.write("maxdensity =" + maxrho + ";\n");
			for(int i=0;i<MACROMODEL.size();i++)
				z.write("prefix{" + (i+1) + "} = '" + MACROMODEL.get(i).prefix + "';");
			
			
			z.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void write(){
		if(!dowrite)
			return;
		
		int i,j;
		float Ltotal;
		float currx = 0;
		float rho;
		float tt = Math.round(time/dt)*dt;
		Vector<Float> rhovec = new Vector<Float>();
	
		for(i=0;i<LINKS.size();i++){

			Link L = LINKS.get(i);
			currx = 0;
			Ltotal = L.Ltotal;
			for(j=0;j<L.RGNS.size();j++){
				Region thisR = L.RGNS.get(j);
				
				float maxx;
				if(j<L.RGNS.size()-1)
					maxx = L.RGNS.get(j+1).xup;
				else
					maxx = Ltotal;
				maxx = Math.min(maxx,Ltotal);
				while(  Utils.lessthan(currx,maxx) ){ 		// (lessthan to not get points <0
					rho = thisR.sample(currx-thisR.xup);
					if(Float.isNaN(rho))
						System.out.println("Region sample returned NaN");
					rhovec.add(rho);
					currx += Globals.outputdx;
				}
				
				if( Utils.greaterorequalthan(currx,Ltotal) )
					break;
			}	
		}

		try {
			out_t.write( tt + "\n");
			out_density.write( format(rhovec) + "\n");

			for(i=0;i<MACROMODEL.size();i++)
				MACROMODEL.get(i).write();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		if(!dowrite)
			return;
		try {
			out_t.close();
			out_density.close();
			for(int i=0;i<MACROMODEL.size();i++)
				MACROMODEL.get(i).close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String format(Vector<Float> V){
		String s = "";
		if(V.isEmpty())
			return s;
		for(int i=0;i<V.size()-1;i++){
			s += V.get(i).toString() + "\t";
		}
		s+= V.lastElement().toString();
		return s;
	}
	
}
