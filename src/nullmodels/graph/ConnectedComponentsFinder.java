package nullmodels.graph;

import java.util.ArrayList;
import java.util.List;

public class ConnectedComponentsFinder {

	private double[][] mat;
	private int noOfVertices;
	private double threshold;
	private int[][] adjMat;
	private List<Boolean> visited;
	private List<List<Integer>> connectedComponents;
	
	public ConnectedComponentsFinder(double[][] mat, int noOfVertices, double threshold){
		this.setMat(mat);
		this.noOfVertices = noOfVertices;
		this.setThreshold(threshold);
		this.adjMat = new int[noOfVertices][noOfVertices];
		this.connectedComponents = new ArrayList<List<Integer>>();
		for(int i=0;i<noOfVertices;i++){
			for(int j=0;j<noOfVertices;j++){
				if(mat[i][j] < threshold){
					adjMat[i][j] = 0;
				}else{
					adjMat[i][j] = 1;
				}
			}
		}
	}
	
	public List<List<Integer>> findConnectedComponents(){
		int componentIndex=-1;
		visited = new ArrayList<Boolean>();
		for(int i=0;i<noOfVertices;i++)
			visited.add(false);
		for(int i=0;i<visited.size();i++){
			if(!visited.get(i)){
				visited.set(i, true);
				connectedComponents.add(new ArrayList<Integer>());
				connectedComponents.get(++componentIndex).add(i);
				dfs(i, componentIndex);
			}
		}
		List<List<Integer>> nonTrivialConnectedComponents = new ArrayList<List<Integer>>();
		for(List<Integer> component : connectedComponents){
			if(component.size()>1)
				nonTrivialConnectedComponents.add(component);
		}
		return nonTrivialConnectedComponents;
	}
	
	private void dfs(int vertexId, int compId){
		for(int i=0;i<visited.size();i++){
			if(adjMat[vertexId][i] == 1){
				if(!visited.get(i)){
					visited.set(i, true);
					connectedComponents.get(compId).add(i);
					dfs(i, compId);
				}
			}
		}
	}
	
	public double getThreshold() {
		return threshold;
	}
	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}
	public double[][] getMat() {
		return mat;
	}
	public void setMat(double[][] mat) {
		this.mat = mat;
	}


}
