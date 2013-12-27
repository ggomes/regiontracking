package regiontracking;

public abstract class AbstractEdgeGenerator {

	protected Link Lup;
	protected Link Ldn;
	protected boolean upregionremoved;
	protected boolean dnregionremoved;
	protected boolean fangoingtrhough;

	public AbstractEdgeGenerator(){
	}
	
	public AbstractEdgeGenerator(Link up,Link dn) {
		Lup = up;
		Ldn = dn;
		upregionremoved = false;
		dnregionremoved = false;
	}

	public void msg_upregionremoved(){
		upregionremoved = true;
	}

	public void msg_dnregionremoved(){
		dnregionremoved = true;
	}

	protected void resetmessages(){
		upregionremoved = false;
		dnregionremoved = false;
	}
	
	final public void execute(){
		if(triggercondition()){
			generateregions();
			resetmessages();
		}
	}

	protected boolean triggercondition() {
		return upregionremoved | dnregionremoved;
	}
	
	protected void generateregions(){
	}
	
	public float getFlowConstraint(){
		return Float.POSITIVE_INFINITY;
	}
	
}
