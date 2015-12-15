package org.hiv.nullmodels.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import nullmodels.graph.ConnectedComponentsFinder;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import projection.Entry;

public class Runner {
	private int NO_OF_RANDOM_GRAPHS = 5000;
	private static int NO_OF_COLUMNS;
	
	/**
	 * A list of complementary group of attributes
	 */
	private List<List<Integer>> complementaryGroups;
	
	/**
	 * A list denoting whether a complementary group pertains to a social parameter
	 */
	private List<Boolean> isComplementaryGroupSocial;
	
	
	/**
	 * A list of the cumulative frequencies of attributes in the order in which they are present in complementaryGroups
	 */
	private List<List<Double>> complementaryGroupsIntervals;
	
	private int noOfEntries;
	private int noOfAttributes;
	private List<Entry> entries;
	private Map<String, Integer>attr2intMap;
	private Map<Integer, String>int2attrMap;
	private List<Integer> attrFreq;
	private int[][] projAdjMat;
	private int[][] patientProjAdjMat;
	private FileInputStream file;
	private HSSFWorkbook workbook;
	private HSSFSheet sheet;
	
	/**
	 * noOfCommonData[i][j] stores the number of patients having entries in both complementary groups i and j
	 */
	private int[][] noOfCommonData;
	
	/**
	 * Maps an attribute to the complementary group it lies in
	 */
	Map<Integer, Integer> attr2ComplGrp;
	
	public Runner() throws IOException
	{ 	
		attr2ComplGrp = new HashMap<Integer, Integer>();
		noOfCommonData = new int[500][500];
		complementaryGroupsIntervals = new ArrayList<List<Double>>();
		complementaryGroups = new ArrayList<List<Integer>>();
		isComplementaryGroupSocial = new ArrayList<Boolean>();
		patientProjAdjMat = new int[500][500];
		projAdjMat = new int[500][500];
		noOfEntries = 0;
		entries = new ArrayList<>();
		attr2intMap = new HashMap<>();
		int2attrMap = new HashMap<>();
		file = new FileInputStream(new File("new.xls"));
		workbook = new HSSFWorkbook(file);
		sheet = workbook.getSheetAt(0);
		Iterator<Row> rowIterator = sheet.iterator();
		Row row;
		int i=0;
		String cellVal;
		Entry entry;
		while(rowIterator.hasNext())
		{
			int col = 0;
			List<String> complGroup = new ArrayList<String>();
			entry = new Entry(noOfEntries);
			row = rowIterator.next();
			Iterator<Cell> cellIterator = row.cellIterator();
			int colCount=0;
			while(cellIterator.hasNext())
			{
				colCount++;
				cellVal = cellIterator.next().getStringCellValue();
				if(!cellVal.equals("")){
					
					if(!(attr2intMap.containsKey(cellVal)))
					{
						attr2intMap.put(cellVal, i);
						int2attrMap.put(i, cellVal);
						i++;
					}
					entry.addAttribute(attr2intMap.get(cellVal));
				}
			}
			NO_OF_COLUMNS = Math.max(NO_OF_COLUMNS, colCount);
			entries.add(entry);
			noOfEntries++;
		}
		noOfAttributes = i;
		attrFreq = new ArrayList<>();
		for(int j=0;j<noOfAttributes;j++)
			attrFreq.add(0);
	}
	public void run() throws IOException
	{
		for(int i=0;i<noOfAttributes;i++)
		{
			for(int j=0;j<noOfAttributes;j++)
			{
				projAdjMat[i][j] = 0;
			}
		}
		for(int i=0;i<noOfEntries;i++)
		{
			for(int j=0;j<noOfEntries;j++)
			{
				patientProjAdjMat[i][j] = 0;
			}
		}
		for(int i=0;i<entries.size();i++)
		{
			for(int j=0;j<entries.get(i).getAttributes().size();j++)
			{
				attrFreq.set(entries.get(i).getAttributes().get(j), attrFreq.get(entries.get(i).getAttributes().get(j))+1);
				for(int k=0;k<j;k++)
				{
					projAdjMat[entries.get(i).getAttributes().get(j)][entries.get(i).getAttributes().get(k)]++;
					projAdjMat[entries.get(i).getAttributes().get(k)][entries.get(i).getAttributes().get(j)]++;
				}
			}
		}
		for(Entry entry1 : entries)
		{
			for(Entry entry2 : entries)
			{
				for(Integer attr1 : entry1.getAttributes())
				{
					for(Integer attr2 : entry2.getAttributes())
					{
						if(attr1 == attr2)
						{
							patientProjAdjMat[entry1.getId()][entry2.getId()]++;
						}
					}
				}
			}
		}
		discoverComplementaryGroups();
	}
	
	public Graph generateRandomGraph(){
		int adjMat[][] = new int[noOfAttributes][noOfAttributes];
		
		List<Entry> entries = constructRandomEntries();
		
		for(int i=0;i<noOfAttributes;i++){
			for(int j=0;j<noOfAttributes;j++){
				adjMat[i][j] = 0;
			}
		}
		for(int i=0;i<entries.size();i++)
		{
			for(int j=0;j<entries.get(i).getAttributes().size();j++)
			{
				for(int k=0;k<j;k++)
				{
					adjMat[entries.get(i).getAttributes().get(j)][entries.get(i).getAttributes().get(k)]++;
					adjMat[entries.get(i).getAttributes().get(k)][entries.get(i).getAttributes().get(j)]++;
				}
			}
		}
		
		return new Graph(noOfAttributes, adjMat);
	}
	
	/**
	 * Creates a list of 150 patients with randomized attributes.
	 * @return a list of entries (patients)
	 */
	public List<Entry> constructRandomEntries(){
		List<Entry> entries = new ArrayList<Entry>();
		List<Random> randoms = new ArrayList<Random>();
		for(int i=0;i<NO_OF_COLUMNS;i++)
			randoms.add(new Random());
		for(int i=0;i<noOfEntries;i++){
			Entry entry = new Entry(i);
			addAttributesToEntry(entry, randoms, i);
			entries.add(entry);
		}
		return entries;
	}
	
	/**
	 * Adds attributes to a patient.
	 * The attributes are assigned based on its probability of occurrence.
	 * @param entry a patient without any attributes. Attributes are added to this patient
	 * @param randoms a list of random number generators - each one for a particular complementary group of attributes
	 */
	public void addAttributesToEntry(Entry entry, List<Random> randoms, int entryIndex){
		int index=0;
		//System.out.println(complementaryGroupsIntervals.get(attr2intMap.get("mother_hiv-NO")));

		for(List<Integer> complGrp : complementaryGroups){
			if(isComplementaryGroupSocial.get(index)){
				// ADD THE SOCIAL ATTRIBUTE AS IN OBSERVED DATA
				Entry observedEntry = entries.get(entryIndex);
				for(int attr : complGrp){
					if(observedEntry.getAttributes().contains(attr)){
						entry.addAttribute(attr);
						break;
					}
				}
			}else{
				double random = randoms.get(index).nextDouble();
				int i;
				for(i=0;i<complementaryGroupsIntervals.get(index).size();i++){
					if(random <= complementaryGroupsIntervals.get(index).get(i)){
						//System.out.println("CHECK\t"+random+"\t"+complementaryGroupsIntervals.get(index).get(i));
						break;
					}
				}
				if(complGrp.get(i) != -1){
					//System.out.println("AAAAAAAAAa");
					entry.addAttribute(complGrp.get(i));
				}else{
					//System.out.println("AAAAAAAAAAAABBBBBBBBBBB");
				}
			}
			index++;
		}
	}
	
	/**
	 * 	Returns whether the attribute attrib is a social attribute or not
	 * @param attrib
	 * @return
	 */
	public boolean isSocialAttribute(String attrib){
		/*for(String prefix : Metadata.NON_SOCIAL_ATTRIBUTES){
			if(attrib.startsWith(prefix))
				return false;
		}
		return true;
		*/
		return false;
	}
	
	/**
	 * A pre-processing step. It contains all the logic of identifying which attributes belong to one complementary group.
	 * @throws IOException
	 */
	public void discoverComplementaryGroups() throws IOException{
		for(int j=0;j<NO_OF_COLUMNS;j++){
        	for(int k=0;k<NO_OF_COLUMNS;k++){
        		noOfCommonData[j][k]=0;
        	}
        }
		complementaryGroups = new ArrayList<List<Integer>>();
		complementaryGroupsIntervals = new ArrayList<List<Double>>();
		
		for(int i=0;i<NO_OF_COLUMNS;i++){
			complementaryGroups.add(new ArrayList<Integer>());
			complementaryGroupsIntervals.add(new ArrayList<Double>());
			isComplementaryGroupSocial.add(false);
		}
		for(int i=0;i<NO_OF_COLUMNS;i++){
			complementaryGroups.get(i).add(-1);
		}
		for (int i=0; i< sheet.getLastRowNum() + 1; i++) {
	        Row row = sheet.getRow(i);
	        for(int j=0;j<NO_OF_COLUMNS;j++){
	        	Cell cell = row.getCell(j);
	        	try{
		        	String attrib = cell.getStringCellValue();
		        	if(!attrib.equals("")){
		        		
			        	if(!complementaryGroups.get(j).contains(attr2intMap.get(attrib))){
			        		complementaryGroups.get(j).add(attr2intMap.get(attrib));
			        		attr2ComplGrp.put(attr2intMap.get(attrib), j);
			        		if(isSocialAttribute(attrib)){
			        			isComplementaryGroupSocial.set(j, true);
			        		}
			        	}
		        	}
	        	}catch(Exception e){
	        		;
	        	}
	        }
	        for(int j=0;j<NO_OF_COLUMNS;j++){
	        	for(int k=0;k<NO_OF_COLUMNS;k++){
	        		try{
		        		Cell cell = row.getCell(j);
		        		String attrib = cell.getStringCellValue();
		        		cell = row.getCell(k);
		        		attrib = cell.getStringCellValue();
		        		noOfCommonData[j][k]++;
	        		}catch(Exception e){
	        			;
	        		}
	        	}
	        }
	    }
		int index=0;
		for(List<Integer> complGrp : complementaryGroups){
			//System.out.println("-------------");
			double tot = 0;
			for(int i=0;i<complGrp.size();i++){
				if(complGrp.get(i) != -1){
					tot += attrFreq.get(complGrp.get(i));
					//System.out.println("COUNT\t"+int2attrMap.get(complGrp.get(i))+"\t"+attrFreq.get(complGrp.get(i)));
					//System.out.println(int2attrMap.get(complGrp.get(i))+ "\t" + attrFreq.get(complGrp.get(i)));
				}
			}
			for(int i=0;i<complGrp.size();i++){
				if(i==0){
					if(tot > noOfEntries){
						//System.out.println("XXXX" + int2attrMap.get(complGrp.get(1)));
						//System.out.println("XXXX" + complGrp);
						//System.exit(0);
					}
					complementaryGroupsIntervals.get(index).add(noOfEntries - tot);
				}else{
					complementaryGroupsIntervals.get(index).add((double)(attrFreq.get(complGrp.get(i))+complementaryGroupsIntervals.get(index).get(i-1)));
				}
			}
			for(int i=0;i<complGrp.size();i++){
				complementaryGroupsIntervals.get(index).set(i, complementaryGroupsIntervals.get(index).get(i)/noOfEntries);
			}
			index++;
		}
		/*for(List<Integer> complGrp : complementaryGroups){
			for(int attr : complGrp){
				System.out.print(int2attrMap.get(attr)+"\t");
			}
			System.out.println();
		}
		System.exit(0);*/
	}
	
	/**
	 * Performs the major calculation step related to null models.
	 * @throws IOException
	 */
	public void calculate() throws IOException{
		double less[][] = new double[noOfAttributes][noOfAttributes];
		double greater[][] = new double[noOfAttributes][noOfAttributes];
		for(int i=0;i<noOfAttributes;i++){
			for(int j=0;j<noOfAttributes;j++){
				less[i][j] = 0;
				greater[i][j]=0;
			}
		}
		for(int i=0;i<NO_OF_RANDOM_GRAPHS;i++){
			Graph graph = generateRandomGraph();
			//System.out.println(graph.getProjAdjMat()[attr2intMap.get("tb-0")][attr2intMap.get("tbhs-0")]);
			for(int j=0;j<noOfAttributes;j++){
				for(int k=0;k<noOfAttributes;k++){
					
					//if(graph.getProjAdjMat()[j][k] < this.projAdjMat[j][k]*(1.0*noOfEntries)/noOfCommonData[attr2ComplGrp.get(j)][attr2ComplGrp.get(k)]){
					if(graph.getProjAdjMat()[j][k] < this.projAdjMat[j][k]){//*(1.0*noOfEntries)/noOfCommonData[attr2ComplGrp.get(j)][attr2ComplGrp.get(k)]){
						less[j][k]++;
					}
					else if(graph.getProjAdjMat()[j][k] > this.projAdjMat[j][k]){
						greater[j][k]++;
					}
				}
			}
		}
		for(int i=0;i<noOfAttributes;i++){
			for(int j=0;j<noOfAttributes;j++){
				less[i][j]/=NO_OF_RANDOM_GRAPHS;
				greater[i][j]/=NO_OF_RANDOM_GRAPHS;
				//System.out.print(greater[i][j]+"\t");
			}
		}
		
		//System.exit(0);
		generateCorrelationNetwork(less, greater, 0.99);
		//generatePositiveCorrelationNetworkR(less, greater, 0.99);
		ConnectedComponentsFinder connectedComponentsFinder = new ConnectedComponentsFinder(less, noOfAttributes, 0.9);
		List<List<Integer>> connectedComps = connectedComponentsFinder.findConnectedComponents();
		/*for(List<Integer> component : connectedComps){
			System.out.println("----------------Component---------------------");
			for(Integer vertex : component){
				System.out.println(int2attrMap.get(vertex));
			}
			System.out.println("----------------Component---------------------");
		}*/
	}
	private void displayComplGroup(List<Integer> complGrp){
		for(int i : complGrp){
			System.out.print(int2attrMap.get(i)+"\t");
		}
		System.out.println();
	}
	/**
	 * Generates a network as a .net file depicting both the positive and negative correlations.
	 * This function does not omit the vertices which don't have an edge under the given threshold.
	 * Because of this, the output .net file works in mapequation.org but not in Pajek.
	 * @param less
	 * @param greater
	 * @param threshold
	 */
	public void generateCorrelationNetwork(double less[][], double greater[][], double threshold){
		System.out.println("*Vertices "+noOfAttributes);
		for(int i=0;i<noOfAttributes;i++){
			for(int j=0;j<noOfAttributes;j++){
				if(less[i][j] > threshold || greater[i][j] > threshold){
					System.out.println((i+1)+" \""+int2attrMap.get(i)+"\"");	
					break;
				}
			}
		}
		System.out.println("*Edges");
		String colour = null;
		for(int i=0;i<noOfAttributes;i++){
			for(int j=i+1;j<noOfAttributes;j++){
				if(isEdgeValid(i,j)){
					double result=less[i][j];
					colour = "Blue";
					if(less[i][j] < 0.5){
						result = greater[i][j];
						colour = "Red";
					}
					
					if(result > threshold)
						System.out.println((i+1)+" "+(j+1)+" "+ result + " c " + colour);
				}
			}
		}
	}
	/**
	 * 
	 * @param less
	 * @param greater
	 * @param threshold
	 */
	public void generateNegativeCorrelationNetwork(double less[][], double greater[][], double threshold){
		int count = 0;
		for(int i=0;i<noOfAttributes;i++){
			for(int j=0;j<noOfAttributes;j++){
				if(greater[i][j] > threshold){
					count++;
					break;
				}
			}
		}
		System.out.println("*Vertices "+count);
		count=1;
		Map<Integer, Integer> vertexIdToPajekIdMap = new HashMap<Integer, Integer>();
		for(int i=0;i<noOfAttributes;i++){
			for(int j=0;j<noOfAttributes;j++){
				if(greater[i][j] > threshold){
					vertexIdToPajekIdMap.put(i, count);
					
					//System.out.println((i+1)+" \""+int2attrMap.get(i)+"\"");
					System.out.println((count)+" \""+int2attrMap.get(i)+"\"");
					count++;
					break;
				}
			}
		}
		System.out.println("*Edges");
		String colour = null;
		for(int i=0;i<noOfAttributes;i++){
			for(int j=i+1;j<noOfAttributes;j++){
				if(isEdgeValid(i,j)){
					double result=greater[i][j];
					colour = "Red";
					
					if(result > threshold)
						System.out.println(vertexIdToPajekIdMap.get(i)+" "+vertexIdToPajekIdMap.get(j)+" "+ result + " c " + colour);
				}
			}
		}
	}
	
	public void generatePositiveCorrelationNetwork(double less[][], double greater[][], double threshold){
		int count = 0;
		for(int i=0;i<noOfAttributes;i++){
			for(int j=0;j<noOfAttributes;j++){
				if(less[i][j] > threshold){
					count++;
					break;
				}
			}
		}
		System.out.println("*Vertices "+count);
		count=1;
		Map<Integer, Integer> vertexIdToPajekIdMap = new HashMap<Integer, Integer>();
		for(int i=0;i<noOfAttributes;i++){
			for(int j=0;j<noOfAttributes;j++){
				if(less[i][j] > threshold){
					vertexIdToPajekIdMap.put(i, count);
					
					//System.out.println((i+1)+" \""+int2attrMap.get(i)+"\"");
					System.out.println((count)+" \""+int2attrMap.get(i)+"\"");
					count++;
					break;
				}
			}
		}
		System.out.println("*Edges");
		String colour = null;
		for(int i=0;i<noOfAttributes;i++){
			for(int j=i+1;j<noOfAttributes;j++){
				if(isEdgeValid(i,j)){
					double result=less[i][j];
					colour = "Blue";
					
					if(result > threshold)
						System.out.println(vertexIdToPajekIdMap.get(i)+" "+vertexIdToPajekIdMap.get(j)+" "+ result + " c " + colour);
				}
			}
		}
	}
	
	public void generatePositiveCorrelationNetworkR(double less[][], double greater[][], double threshold){
		int count=1;
		Map<Integer, Integer> vertexIdToPajekIdMap = new HashMap<Integer, Integer>();
		for(int i=0;i<noOfAttributes;i++){
			for(int j=0;j<noOfAttributes;j++){
				if(less[i][j] > threshold){
					vertexIdToPajekIdMap.put(i, count);
					
					//System.out.println((i+1)+" \""+int2attrMap.get(i)+"\"");
					count++;
					break;
				}
			}
		}
		System.out.println("first\tsecond\tgrade\tspec");
		String colour = null;
		for(int i=0;i<noOfAttributes;i++){
			for(int j=i+1;j<noOfAttributes;j++){
				if(isEdgeValid(i,j)){
					double result=less[i][j];
					colour = "blue";
					
					if(result > threshold)
						System.out.println(int2attrMap.get(i)+"\t"+int2attrMap.get(j)+"\t"+ result + "\t" + colour);
				}
			}
		}
	}

	public void generateNegativeCorrelationNetworkR(double less[][], double greater[][], double threshold){
		int count=1;
		Map<Integer, Integer> vertexIdToPajekIdMap = new HashMap<Integer, Integer>();
		for(int i=0;i<noOfAttributes;i++){
			for(int j=0;j<noOfAttributes;j++){
				if(less[i][j] > threshold){
					vertexIdToPajekIdMap.put(i, count);
					
					//System.out.println((i+1)+" \""+int2attrMap.get(i)+"\"");
					count++;
					break;
				}
			}
		}
		System.out.println("first\tsecond\tgrade\tspec");
		String colour = null;
		for(int i=0;i<noOfAttributes;i++){
			for(int j=i+1;j<noOfAttributes;j++){
				if(isEdgeValid(i,j)){
					double result=greater[i][j];
					colour = "red";
					
					if(result > threshold)
						System.out.println(int2attrMap.get(i)+"\t"+int2attrMap.get(j)+"\t"+ result + "\t" + colour);
				}
			}
		}
	}
	
	public void generateCorrelationNetworkR(double less[][], double greater[][], double threshold){
		int count=1;
		Map<Integer, Integer> vertexIdToPajekIdMap = new HashMap<Integer, Integer>();
		for(int i=0;i<noOfAttributes;i++){
			for(int j=0;j<noOfAttributes;j++){
				if(less[i][j] > threshold){
					vertexIdToPajekIdMap.put(i, count);
					
					//System.out.println((i+1)+" \""+int2attrMap.get(i)+"\"");
					count++;
					break;
				}
			}
		}
		System.out.println("first\tsecond\tgrade\tspec");
		String colour = null;
		for(int i=0;i<noOfAttributes;i++){
			for(int j=i+1;j<noOfAttributes;j++){
				if(isEdgeValid(i,j)){
					
					double result=less[i][j];
					colour = "blue";
					if(less[i][j] < 0.5){
						result = greater[i][j];
						colour = "red";
					}
					if(result > threshold)
						System.out.println(int2attrMap.get(i)+"\t"+int2attrMap.get(j)+"\t"+ result + "\t" + colour);
				}
			}
		}
	}
	
	public void generateColourCodedNetworkNonRedundant(double less[][], double greater[][], double threshold){
		int count = 0;
		for(int i=0;i<noOfAttributes;i++){
			for(int j=0;j<noOfAttributes;j++){
				if(less[i][j] > threshold || greater[i][j] > threshold){
					count++;
					break;
				}
			}
		}
		System.out.println("*Vertices "+count);
		count=1;
		Map<Integer, Integer> vertexIdToPajekIdMap = new HashMap<Integer, Integer>();
		for(int i=0;i<noOfAttributes;i++){
			for(int j=0;j<noOfAttributes;j++){
				if(less[i][j] > threshold || greater[i][j] > threshold){
					vertexIdToPajekIdMap.put(i, count);
					System.out.println((count)+" \""+int2attrMap.get(i)+"\"");
					count++;
					break;
				}
			}
		}
		System.out.println("*Edges");
		String colour = null;
		for(int i=0;i<noOfAttributes;i++){
			for(int j=i+1;j<noOfAttributes;j++){
				if(isEdgeValid(i,j)){
					double result=less[i][j];
					colour = "Blue";
					if(less[i][j] < 0.5){
						result = greater[i][j];
						colour = "Red";
					}
					
					if(result > threshold)
						System.out.println(vertexIdToPajekIdMap.get(i)+" "+vertexIdToPajekIdMap.get(j)+" "+ result + " c " + colour);
				}
			}
		}
	}
	
	
	private boolean isEdgeValid(int i, int j) {
		for(List<Integer> complGrp : complementaryGroups){
			if(complGrp.contains(i)){
				if(complGrp.contains(j))
					return false;
				return true;
			}
		}
		return true;
	}
	public static void main(String args[]) throws IOException{
		Runner dr = new Runner();
		dr.run();
		dr.calculate();
		
	}

}
