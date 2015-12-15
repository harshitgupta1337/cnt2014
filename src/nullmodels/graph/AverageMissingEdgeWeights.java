package nullmodels.graph;

import java.util.List;

public class AverageMissingEdgeWeights {

	private ConnectedComponentsFinder connectedComponentsFinder;
	private double mat[][];
	private int noOfVertices;
	private double threshold;
	private List<List<Integer>> connectedComponents;
	private int adjMat[][];
	public AverageMissingEdgeWeights(double mat[][], int noOfVertices, double threshold){
		this.mat = mat;
		this.noOfVertices = noOfVertices;
		this.threshold = threshold;
		adjMat = new int[noOfVertices][noOfVertices];
		for(int i=0;i<noOfVertices;i++){
			for(int j=0;j<noOfVertices;j++){
				if(mat[i][j] < threshold){
					adjMat[i][j] = 0;
				}else{
					adjMat[i][j] = 1;
				}
			}
		}
		this.connectedComponentsFinder = new ConnectedComponentsFinder(this.mat, this.noOfVertices, this.threshold);
		this.connectedComponents = connectedComponentsFinder.findConnectedComponents();
	}
	private int[][] multiply(int[][] a, int[][] b){
		int[][] result = new int[noOfVertices][noOfVertices];
		for(int i=0;i<noOfVertices;i++){
			for(int j=0;j<noOfVertices;j++){
				result[i][j] = 0;
				for(int k=0;k<noOfVertices;k++){
					result[i][j] += a[i][k]*b[k][j];
				}
				if(result[i][j] > 0)
					result[i][j] = 1;
			}
		}
		return result;
	}
	
	private int[][] getKdistantNeighbours(int k, int[][] adjmatComponent){
		int result[][] = new int[noOfVertices][noOfVertices];
		int done[][] = new int[noOfVertices][noOfVertices];
		for(int i=0;i<noOfVertices;i++){
			for(int j=0;j<noOfVertices;j++){
				result[i][j] = adjmatComponent[i][j];
				if(i==j)
					done[i][j] = 1;
				else
					done[i][j] = 0;
			}
		}
		for(int l=1;l<k;l++){
			for(int i=0;i<noOfVertices;i++){
				for(int j=0;j<noOfVertices;j++){
					if(result[i][j]==1)
						done[i][j] = 1;
				}
			}
			
			result = multiply(result, adjmatComponent);
			
		}
		for(int i=0;i<noOfVertices;i++){
			for(int j=0;j<noOfVertices;j++){
				if(done[i][j]==1)
					result[i][j] = 0;
			}
		}
		return result;
	}
	
	public void printAverageMissingEdgeWeights(int k){
		for(List<Integer> component : connectedComponents){
			int[][] compAdjMat = getAdjMatForComponent(component);
			for(int i=1;i<=k-1;i++){
				
			}
		}
	}
	
	private int[][] getAdjMatForComponent(List<Integer> component){
		int[][] matrix = new int[noOfVertices][noOfVertices];
		for(Integer i : component){
			for(Integer j : component){
				if(adjMat[i][j] == 1)
					matrix[i][j] = 1;
				else
					matrix[i][j] = 0;
			}
		}
		return matrix;
	}
	
	public static void main(String args[]){
		/*
		double ad[][] = {{0,1,0,0}, {1,0,1,0}, {0,1,0,0},{0,0,0,0}};
		AverageMissingEdgeWeights averageMissingEdgeWeights = new AverageMissingEdgeWeights(ad, 4, 0.5);
		int[][] result = averageMissingEdgeWeights.getKdistantNeighbours(7);
		for(int i=0;i<4;i++){
			for(int j=0;j<4;j++){
				System.out.print(result[i][j]+"\t");
			}
			System.out.println();
		}
		*/
		double ad[][] = {{0,1,0,0}, {1,0,1,0}, {0,1,0,0},{0,0,0,0}};
		AverageMissingEdgeWeights averageMissingEdgeWeights = new AverageMissingEdgeWeights(ad, 4, 0.5);
		averageMissingEdgeWeights.printAverageMissingEdgeWeights(3);
	}
}
