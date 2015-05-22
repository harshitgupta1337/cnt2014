package nullmodels;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import com.sun.org.apache.xpath.internal.operations.Gte;

import projection.Entry;
import projection.Pair;

public class Dendrogram {
	private int NO_OF_RANDOM_GRAPHS = 5000;
	private static int NO_OF_COLUMNS = 33;
	
	/**
	 * A list of complementary group of attributes
	 */
	private List<List<Integer>> complementaryGroups;
	
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
	
	public Dendrogram() throws IOException
	{ 	
		attr2ComplGrp = new HashMap<Integer, Integer>();
		noOfCommonData = new int[100][100];
		complementaryGroupsIntervals = new ArrayList<List<Double>>();
		complementaryGroups = new ArrayList<List<Integer>>();
		patientProjAdjMat = new int[151][151];
		projAdjMat = new int[100][100];
		noOfEntries = 0;
		entries = new ArrayList<>();
		attr2intMap = new HashMap<>();
		int2attrMap = new HashMap<>();
		file = new FileInputStream(new File("HIV_data.xls"));
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
			while(cellIterator.hasNext())
			{
				cellVal = cellIterator.next().getStringCellValue();
				if(!(attr2intMap.containsKey(cellVal)))
				{
					attr2intMap.put(cellVal, i);
					int2attrMap.put(i, cellVal);
					i++;
				}
				entry.addAttribute(attr2intMap.get(cellVal));
			}
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
		//System.out.println(attrFreq.get(attr2intMap.get("tb_child-YES")));
		//System.out.println(attrFreq.get(attr2intMap.get("tb_child-NO")));
		//System.out.println(attrFreq.get(attr2intMap.get("waz_cat- 1")));
		//System.out.println(attrFreq.get(attr2intMap.get("mother_hiv-NO")));
		//System.out.println("----------------------");
		//System.out.println(projAdjMat[attr2intMap.get("tb_child-YES")][attr2intMap.get("waz_cat- 1")] + "\t" + adjMat[attr2intMap.get("tb_child-YES")][attr2intMap.get("waz_cat- 1")]);
		
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
			addAttributesToEntry(entry, randoms);
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
	public void addAttributesToEntry(Entry entry, List<Random> randoms){
		int index=0;
		//System.out.println(complementaryGroupsIntervals.get(attr2intMap.get("mother_hiv-NO")));

		for(List<Integer> complGrp : complementaryGroups){
			double random = randoms.get(index).nextDouble();
			int i;
			for(i=0;i<complementaryGroupsIntervals.get(index).size();i++){
				if(random <= complementaryGroupsIntervals.get(index).get(i)){
					break;
				}
			}
			if(complGrp.get(i) != -1)
				entry.addAttribute(complGrp.get(i));
			index++;
		}
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
		        	if(!complementaryGroups.get(j).contains(attr2intMap.get(attrib))){
		        		complementaryGroups.get(j).add(attr2intMap.get(attrib));
		        		attr2ComplGrp.put(attr2intMap.get(attrib), j);
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
			double tot = 0;
			for(int i=0;i<complGrp.size();i++){
				if(complGrp.get(i) != -1)
					tot += attrFreq.get(complGrp.get(i));
			}
			for(int i=0;i<complGrp.size();i++){
				if(i==0){
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
		
		/*for(List<Integer> complGrp : complGrps){
			for(Integer i : complGrp){
				System.out.print(int2attrMap.get(i)+"\t");
			}
			System.out.println();
		}*/
		
	}
	
	public void calculate() throws IOException{
		//System.out.println("xxx"+noOfCommonData[attr2ComplGrp.get(attr2intMap.get("father_tb_death-YES"))][attr2ComplGrp.get(attr2intMap.get("tb_child-NO"))]);

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
			
			//System.out.println(projAdjMat[attr2intMap.get("tb_child-YES")][attr2intMap.get("waz_cat- 1")]*150.0/(projAdjMat[attr2intMap.get("tb_child-YES")][attr2intMap.get("waz_cat- 1")] +
				//	projAdjMat[attr2intMap.get("tb_child-YES")][attr2intMap.get("mother_hiv-NO")] +
					//projAdjMat[attr2intMap.get("tb_child-NO")][attr2intMap.get("waz_cat- 1")]+
					//projAdjMat[attr2intMap.get("tb_child-NO")][attr2intMap.get("mother_hiv-NO")])
					//+"\t"+ graph.getProjAdjMat()[attr2intMap.get("tb_child-YES")][attr2intMap.get("waz_cat- 1")]);
			//if(graph.projAdjMat[attr2intMap.get("tb_child-YES")][attr2intMap.get("waz_cat- 1")] > this.projAdjMat[attr2intMap.get("tb_child-YES")][attr2intMap.get("waz_cat- 1")]*(1.0*noOfEntries)/noOfCommonData[attr2ComplGrp.get(attr2intMap.get("tb_child-YES"))][attr2ComplGrp.get(attr2intMap.get("waz_cat- 1"))])
				//System.out.println(graph.projAdjMat[attr2intMap.get("tb_child-YES")][attr2intMap.get("waz_cat- 1")] +"\t"+ this.projAdjMat[attr2intMap.get("tb_child-YES")][attr2intMap.get("waz_cat- 1")]*(1.0*noOfEntries)/noOfCommonData[attr2ComplGrp.get(attr2intMap.get("tb_child-YES"))][attr2ComplGrp.get(attr2intMap.get("waz_cat- 1"))]);
			
			for(int j=0;j<noOfAttributes;j++){
				for(int k=0;k<noOfAttributes;k++){
					
					//if(graph.getProjAdjMat()[j][k] < this.projAdjMat[j][k]*(1.0*noOfEntries)/noOfCommonData[attr2ComplGrp.get(j)][attr2ComplGrp.get(k)]){
					if(graph.getProjAdjMat()[j][k] < this.projAdjMat[j][k]){//*(1.0*noOfEntries)/noOfCommonData[attr2ComplGrp.get(j)][attr2ComplGrp.get(k)]){
						less[j][k]++;
					}
					else{
						
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
			//System.out.println();
		}
		//System.out.println(less[attr2intMap.get("waz_cat- 1")][attr2intMap.get("tb_child-YES")]);
		/*System.out.println(complementaryGroupsIntervals.get(2));
		System.out.println(complementaryGroups.get(2));
		System.out.println(int2attrMap.get(2));
		System.out.println(int2attrMap.get(50));
		System.out.println(int2attrMap.get(63));
		System.out.println(int2attrMap.get(79));*/
		generateDendrogramInput("dend", less, greater);
		generateColourCodedNetwork(less, greater);
	}
	
	public void generateColourCodedNetwork(double less[][], double greater[][]){
		System.out.println("*Vertices "+noOfAttributes);
		for(int i=0;i<noOfAttributes;i++){
			System.out.println((i+1)+" \""+int2attrMap.get(i)+"\"");
		}
		System.out.println("*Edges");
		String colour = null;
		for(int i=0;i<noOfAttributes;i++){
			for(int j=i+1;j<noOfAttributes;j++){
				if(isEdgeValid(i,j)){
					double result=less[i][j];
					/*if(less[i][j] < greater[i][j]){
						if(less[i][j] > 0)
							result = 1/less[i][j];
						colour = "Red";
					}else{
						if(greater[i][j] > 0)
							result = 1/greater[i][j];
						colour = "Blue";
					}*/
					colour = "Blue";
					System.out.println((i+1)+" "+(j+1)+" "+ result + " c " + colour);
				}
			}
		}
	}
	
	public void generateDendrogramInput(String filename, double[][] less, double[][] greater) throws IOException
	{
		File file = new File("output/DendrogramInput/"+filename+".txt");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		for(int i=0;i<noOfAttributes;i++)
		{
			bw.write(int2attrMap.get(i).replace(' ', '_').replace('\t',	'_').replace(',','_').replace(';', '_').replace('|',  '_')+"\t");
		}
		bw.write("\n");
		//System.out.println(less[attr2intMap.get("tb_child-YES")][attr2intMap.get("waz_cat- 1")]);
		
		for(int i=0;i<noOfAttributes;i++)
		{
			for(int j=0;j<noOfAttributes;j++)
			{
				bw.write(less[i][j]+"\t");
			}
			bw.write("\n");
		}
		bw.close();
		
	}
	
	private boolean isEdgeValid(int i, int j) {
		// TODO Auto-generated method stub
		return true;
	}
	public static void main(String args[]) throws IOException{
		Dendrogram dr = new Dendrogram();
		dr.run();
		dr.calculate();
	}

}
