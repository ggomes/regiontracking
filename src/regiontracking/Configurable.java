package regiontracking;

import org.w3c.dom.Node;

public interface Configurable {

	public boolean initFromDOM(Node p) throws Exception;
	
}
