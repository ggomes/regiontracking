package regiontracking;

public class Region {

	public AbstractFlux myFD;
	public RegionType type;		// region type
	private float L;			// region length [mile]
	public float xup;			// position of upstream boundary [mile]
	private float rhoup;		// upstream boundary density [veh/mile]
	private float rhodn;		// downstream boundary density [veh/mile]
	public float vup;			// upstream boundary speed [mph]
	public float vdn;			// downstream boundary speed [mph]
	
	//public boolean rhouptied;   // true if this is a fan across an upstream boundary
	//public boolean rhodntied;   // true if this is a fan across an downstream boundary
	
	public Region(RegionType t,float up,float dn,float xxup,float xL,AbstractFlux xFD){
		type = t;
		rhoup = up;
		rhodn = dn;
		xup = xxup;
		L = xL;
		myFD = xFD;
		//rhouptied = false;
		//rhodntied = false;
	}

	public float getL(){
		return L;
	}
	
	public float getRhoup() {
		return rhoup;
		/*
		switch(type){
		case constant:
			return rhoup;
		case rarefaction:
			return sample(0);
		default:
			return Float.NaN;
		}	
		*/
	}

	public void setRhoup(float rhoup) {
		this.rhoup = rhoup;
	}

	public float getRhodn() {
		
		return rhodn;
		
		/*
		switch(type){
		case constant:
			return rhodn;
		case rarefaction:
			return sample(L);
		default:
			return Float.NaN;
		}	
		
		
		*/
	}

	public void setRhodn(float rhodn) {
		this.rhodn = rhodn;
	}

	public void setL(float l) {
		L = l;
	}
	
	public void updateL(float dt){
		L += (vdn-vup)*dt/3600f;
		xup += vup*dt/3600f;
	}
	
	public void updateRhoUp(){
		float partialphi = myFD.partialPhi(0,L,rhoup,rhodn);
		float fprime = myFD.Fprime(rhoup);
		float delta = (vup-fprime)*partialphi/L;
		if(!Float.isNaN(delta)){
			rhoup += Globals.dt*delta/3600f;
			rhoup = Math.min(rhoup,myFD.rhojam);
			rhoup = Math.max(rhoup,0f);
		}
	}
	
	public void updateRhoDn(){
		float partialphi = myFD.partialPhi(L,L,rhoup,rhodn);
		float fprime = myFD.Fprime(rhodn);
		float delta = (vdn-fprime)*partialphi/L;
		if(!Float.isNaN(delta)){
			rhodn += Globals.dt*delta/3600f;
			rhodn = Math.min(rhodn,myFD.rhojam);
			rhodn = Math.max(rhodn,0f);
		}
	}
	
	public float sample(float x){
		if( !Utils.isbetweenclosed(x,0,L) )
			return Float.NaN;
		switch(type){
		case constant:
			return rhoup;
		case rarefaction:
			return myFD.Phi(x,L,rhoup,rhodn);
		default:
			return Float.NaN;
		}
	}

	public boolean LengthIsNotPositive(){
		return Utils.lessorequalthan(L,0);
	}

	@Override
	public String toString() {
		String t="";
		String sep = "\t";
		t+=String.format("xup=%5.0f",xup*5280);
		t+=sep;
		t+=String.format("vup=%3.2f",vup);
		t+=sep;
		t+=String.format("L=%5.0f",L*5280);
		t+=sep;
		t+=String.format("rhoup=%3.2f",rhoup);
		t+=sep;
		t+=String.format("rhodn=%3.2f",rhodn);
		t+=sep;
		t+=String.format("vdn=%3.2f",vdn);
		t+="(";
		switch(type){
		case constant:
			t+="c";
			break;
		case rarefaction:
			t+="r";
			break;
		default:
			t+="x";
			break;
		}
		t+=")";
		
		return t;
	}
	
}
