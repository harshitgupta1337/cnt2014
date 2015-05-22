package nullmodels;

public class Graph {
	
	int noOfNodes;
	
	public int getNoofNodes() {
		return noOfNodes;
	}

	public void setNoofNodes(int noofNodes) {
		this.noOfNodes = noofNodes;
	}

	public int[][] getProjAdjMat() {
		return projAdjMat;
	}

	public void setProjAdjMat(int[][] projAdjMat) {
		this.projAdjMat = projAdjMat;
	}

	int projAdjMat[][];
	
	public Graph(int noOfNodes, int[][] projAdjMat) {
		this.noOfNodes = noOfNodes;
		this.projAdjMat = projAdjMat;
	}
	
	
}
