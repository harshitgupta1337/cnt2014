package nullmodels;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import projection.Entry;
import projection.Projector;

public class Pathogens {

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
	private Map<String, List<Integer>> pathogenToPatientsMap;
	
	public Pathogens() throws IOException
	{
		pathogenToPatientsMap = new HashMap<String, List<Integer>>();
		patientProjAdjMat = new int[151][151];
		projAdjMat = new int[100][100];
		noOfEntries = 0;
		entries = new ArrayList<>();
		attr2intMap = new HashMap<>();
		int2attrMap = new HashMap<>();
		file = new FileInputStream(new File("/home/harshit/workspace/CNT2014/pathogens.xls"));
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
	}
	private void fillAttrToPatientsMap(){
		for(int i=0;i<noOfAttributes;i++){
			if(int2attrMap.get(i).contains("YES"))
				pathogenToPatientsMap.put(int2attrMap.get(i), new ArrayList<Integer>());
		}
		for(Entry entry : entries){
			for(Integer pathogenId : entry.getAttributes()){
				if(int2attrMap.get(pathogenId).contains("YES"))
					pathogenToPatientsMap.get(int2attrMap.get(pathogenId)).add(entry.getId());
			}
		}
	}
	public void printInputForEcoSim(String filename) throws IOException{
		FileWriter fw = new FileWriter(new File(filename).getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("Pathogens ");
		for(int i=0;i<entries.size();i++)
			bw.write("patient"+(i+1)+" ");
		bw.write("\n");
		for(String pathogen : pathogenToPatientsMap.keySet()){
			bw.write(pathogen.substring(0, pathogen.lastIndexOf("YES")-1)+" ");
			for(Entry entry : entries){
				if(pathogenToPatientsMap.get(pathogen).contains(entry.getId()))
					bw.write("1 ");
				else
					bw.write("0 ");
			}
			bw.write("\n");
		}
		bw.close();
	}
	public static void main(String[] args) throws IOException {
		Pathogens pathogens = new Pathogens();
		pathogens.run();
		System.out.println(pathogens.noOfAttributes);
		pathogens.fillAttrToPatientsMap();
		pathogens.printInputForEcoSim("/home/harshit/CNT2014/Null Models/ecosim7/HIV Data/Specific Exploration/pathogens.txt");
	}

}
