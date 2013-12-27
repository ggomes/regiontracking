package regiontracking;
import java.util.Vector;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PiecewiseConstant implements Configurable {
	
	public Vector<Float> x = new Vector<Float>();		// x value
	public Vector<Object> y = new Vector<Object>();		// y value

	public PiecewiseConstant(){}
	
	public PiecewiseConstant(float [] tx,Object [] ty){
		int i;
		if(tx.length!=ty.length){
			System.out.println("ERROR!");
			return;
		}
		if(tx.length==0){
			System.out.println("EMPTY!");
			return;
		}
		if(tx[0]!=0f){
			System.out.println("x must start with 0!");
			return;
		}
		
		for(i=0;i<tx.length;i++){
			x.add(tx[i]);
			y.add(ty[i]);
		}
	}
	
	public Object eval(float xx){
		if(x.isEmpty())
			return null;
		if(x.size()==1 | Utils.lessthan(xx,x.firstElement()))
			return y.firstElement();
		if(Utils.greaterorequalthan(xx,x.lastElement()))
			return y.lastElement();
		
		while(x.size()>1){
			if(Utils.greaterorequalthan(xx,x.get(1))){
				x.remove(0);
				y.remove(0);
			}
			else
				break;
		}
		return y.get(0);
			
}
	
	public boolean switchtimewithin(float t1,float t2){
		for(int i=0;i<x.size();i++){
			if( Utils.isbetweenhalfopen(x.get(i),t1,t2) )
				return true;
		}
		return false;
	}
	
	public class XYItriplet {
		public float x;
		public Object y;
		public int index;
		public XYItriplet(int ii,float xx,Object yy){
			index = ii;
			x = xx;
			y = yy;
		}
	}

	@Override
	public boolean initFromDOM(Node p) throws Exception {

		boolean res = true;
		if (p == null)
			return !res;
		try 
		{
			int i,j;
			if(p.hasChildNodes()){
				NodeList c = p.getChildNodes();
				for(i=0;i<c.getLength();i++){
					Node pp = c.item(i);
					if(pp.getNodeName().equals("x")){
						String [] str = pp.getTextContent().split(",");
						for(j=0;j<str.length;j++)
							x.add(Float.parseFloat(str[j].trim()));
					}
					
					// ASSUMES Y IS A FLOAT
					if(pp.getNodeName().equals("y")){
						String [] str = pp.getTextContent().split(",");
						for(j=0;j<str.length;j++)
							y.add(Float.parseFloat(str[j].trim()));				
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
