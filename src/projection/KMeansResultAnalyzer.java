package projection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class KMeansResultAnalyzer 
{
	private int noOfEntries;
	private int noOfAttributes;
	private List<Entry> entries;
	private Map<String, Integer>attr2intMap;
	private Map<Integer, String>int2attrMap;
	private List<Integer> attrFreq;
	private List<Patient> patients;
	private Map<Integer, List<Integer>> attrToPatientsMap;
	private int[][] projAdjMat;
	private int[][] patientProjAdjMat;
	private FileInputStream file;
	private HSSFWorkbook workbook;
	private HSSFSheet sheet;
	private List<Map<Integer, List<Integer>>> clusterToAttributeIdsMaps;
	private int[][] kmeansWeights;
	public KMeansResultAnalyzer() throws IOException
	{
		clusterToAttributeIdsMaps = new ArrayList<Map<Integer, List<Integer>>>();
		attrToPatientsMap = new HashMap<Integer, List<Integer>>();
		patientProjAdjMat = new int[151][151];
		patients = new ArrayList<Patient>();
		projAdjMat = new int[100][100];
		noOfEntries = 0;
		entries = new ArrayList<>();
		attr2intMap = new HashMap<>();
		int2attrMap = new HashMap<>();
		file = new FileInputStream(new File("/home/harshit/workspace/CNT2014/HIV_data.xls"));
		workbook = new HSSFWorkbook(file);
		sheet = workbook.getSheetAt(0);
		Iterator<Row> rowIterator = sheet.iterator();
		Row row;
		int i=0;
		String cellVal;
		Entry entry;
		while(rowIterator.hasNext())
		{
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
		kmeansWeights = new int[noOfAttributes][noOfAttributes];
	}
	public void run() throws IOException
	{
		for(int i=0;i<noOfAttributes;i++)
		{
			for(int j=0;j<noOfAttributes;j++)
			{
				projAdjMat[i][j] = 0;
				kmeansWeights[i][j] = 0;
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
		fillAttrToPatientsMap();
	}
	private void fillAttrToPatientsMap(){
		for(int i=0;i<noOfAttributes;i++){
			attrToPatientsMap.put(i, new ArrayList<Integer>());
		}
		for(Entry entry : entries){
			for(Integer attr : entry.getAttributes()){
				attrToPatientsMap.get(attr).add(entry.getId());
			}
		}
	}
	public void preProcessOutputofKmeans(String outputOfKmeansPath, String delimiterBwIterations) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(outputOfKmeansPath));
		String line;
		int attributeId=0;
		Map<Integer, List<Integer>> clusterToAttrIdsMap = new HashMap<Integer, List<Integer>>();
		for(int i=0;i<10;i++){
			clusterToAttrIdsMap.put(i, new ArrayList<Integer>());
		}
		while ((line = br.readLine()) != null) {
			if(line.equals(delimiterBwIterations)){
				// add the current result to the list of results
				clusterToAttributeIdsMaps.add(clusterToAttrIdsMap);
				
				//reset the state of computation to that before the computation began
				clusterToAttrIdsMap = new HashMap<Integer, List<Integer>>();
				for(int i=0;i<10;i++){
					clusterToAttrIdsMap.put(i, new ArrayList<Integer>());
				}
				System.out.println(attributeId);
				attributeId = 0;
			}else{
				// add the present attribute id to the list of the present cluster
				int cluster = Integer.parseInt(line);
				clusterToAttrIdsMap.get(cluster).add(attributeId);
				attributeId++;
			}
		}
		br.close();
		if(validatePreProcessing())
			System.out.println("Preprocessing is consistent !!");
		generateCoClusterPresenceMatrix();
	}
	public boolean validatePreProcessing(){
		
		for(Map<Integer, List<Integer>> result : clusterToAttributeIdsMaps){
			int attributesCovered = 0;
			for(Integer cluster : result.keySet()){
				attributesCovered += result.get(cluster).size();
			}
			if(attributesCovered != noOfAttributes)
				return false;
		}
		return true;
	}
	public void generateCoClusterPresenceMatrix(){
		for(Map<Integer, List<Integer>> result : clusterToAttributeIdsMaps){
			for(Integer cluster : result.keySet()){
				for(Integer attr1 : result.get(cluster)){
					for(Integer attr2 : result.get(cluster)){
						kmeansWeights[attr1][attr2]++;
					}
				}
			}
		}
	}
	public void printCoClusterPresenceMatrix(String outputFileName) throws FileNotFoundException, UnsupportedEncodingException{
		PrintWriter writer = new PrintWriter(outputFileName, "UTF-8");
		for(int i=0;i<noOfAttributes;i++){
			writer.print(int2attrMap.get(i)+"\t");
		}
		writer.println();
		for(int i=0;i<noOfAttributes;i++){
			writer.print(int2attrMap.get(i)+"\t");
			for(int j=0;j<noOfAttributes;j++){
				writer.print(kmeansWeights[i][j]/2+"\t");
			}
			writer.println();
		}
		writer.close();
	}
	public void printDendrogramInputForCoClusterOccurence(String filename) throws IOException
	{
		File file = new File(filename);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		for(int i=0;i<noOfAttributes;i++){
			bw.write(int2attrMap.get(i).replace(' ', '_').replace('\t',	'_').replace(',','_').replace(';', '_').replace('|',  '_')+"\t");
		}
		bw.write("\n");
		for(int i=0;i<noOfAttributes;i++){
			for(int j=0;j<noOfAttributes;j++){
				bw.write(kmeansWeights[i][j]/2+"\t");
			}
			bw.write("\n");
		}
		bw.close();
		
	}
	public static void main(String[] args) throws IOException 
	{
		KMeansResultAnalyzer projector = new KMeansResultAnalyzer();
		projector.run();
		projector.preProcessOutputofKmeans("/home/harshit/CNT2014/KMeansOnAttributes/kmeans_output.txt", "X");
		projector.generateCoClusterPresenceMatrix();
		projector.printCoClusterPresenceMatrix("/home/harshit/CNT2014/KMeansOnAttributes/kmeans_cocluster_occurence_2.txt");
		projector.printDendrogramInputForCoClusterOccurence("/home/harshit/CNT2014/KMeansOnAttributes/kmeans_dendrogram_input.txt");
	}
}