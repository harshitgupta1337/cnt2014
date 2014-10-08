package core;

import java.io.File;
import java.io.FileInputStream;
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

public class Analyzer {

	protected int noOfEntries;
	protected int noOfAttributes;
	protected List<Entry> entries;
	protected Map<String, Integer>attr2intMap;
	public int getNoOfEntries() {
		return noOfEntries;
	}
	public void setNoOfEntries(int noOfEntries) {
		this.noOfEntries = noOfEntries;
	}
	public int getNoOfAttributes() {
		return noOfAttributes;
	}
	public void setNoOfAttributes(int noOfAttributes) {
		this.noOfAttributes = noOfAttributes;
	}
	public List<Entry> getEntries() {
		return entries;
	}
	public void setEntries(List<Entry> entries) {
		this.entries = entries;
	}
	public Map<String, Integer> getAttr2intMap() {
		return attr2intMap;
	}
	public void setAttr2intMap(Map<String, Integer> attr2intMap) {
		this.attr2intMap = attr2intMap;
	}
	public Map<Integer, String> getInt2attrMap() {
		return int2attrMap;
	}
	public void setInt2attrMap(Map<Integer, String> int2attrMap) {
		this.int2attrMap = int2attrMap;
	}
	public List<Integer> getAttrFreq() {
		return attrFreq;
	}
	public void setAttrFreq(List<Integer> attrFreq) {
		this.attrFreq = attrFreq;
	}
	public Map<Integer, List<Integer>> getAttrToPatientsMap() {
		return attrToPatientsMap;
	}
	public void setAttrToPatientsMap(Map<Integer, List<Integer>> attrToPatientsMap) {
		this.attrToPatientsMap = attrToPatientsMap;
	}
	public int[][] getProjAdjMat() {
		return projAdjMat;
	}
	public void setProjAdjMat(int[][] projAdjMat) {
		this.projAdjMat = projAdjMat;
	}
	public int[][] getPatientProjAdjMat() {
		return patientProjAdjMat;
	}
	public void setPatientProjAdjMat(int[][] patientProjAdjMat) {
		this.patientProjAdjMat = patientProjAdjMat;
	}
	public FileInputStream getFile() {
		return file;
	}
	public void setFile(FileInputStream file) {
		this.file = file;
	}
	public HSSFWorkbook getWorkbook() {
		return workbook;
	}
	public void setWorkbook(HSSFWorkbook workbook) {
		this.workbook = workbook;
	}
	public HSSFSheet getSheet() {
		return sheet;
	}
	public void setSheet(HSSFSheet sheet) {
		this.sheet = sheet;
	}
	protected Map<Integer, String>int2attrMap;
	protected List<Integer> attrFreq;
	protected Map<Integer, List<Integer>> attrToPatientsMap;
	protected int[][] projAdjMat;
	protected int[][] patientProjAdjMat;
	protected FileInputStream file;
	protected HSSFWorkbook workbook;
	protected HSSFSheet sheet;
	
	public Analyzer(String filename) throws IOException{
		attrToPatientsMap = new HashMap<Integer, List<Integer>>();
		patientProjAdjMat = new int[151][151];
		projAdjMat = new int[100][100];
		noOfEntries = 0;
		entries = new ArrayList<>();
		attr2intMap = new HashMap<>();
		int2attrMap = new HashMap<>();
		file = new FileInputStream(new File(filename));
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
		fillAttrToPatientsMap();
	}
	protected void fillAttrToPatientsMap(){
		for(int i=0;i<noOfAttributes;i++){
			attrToPatientsMap.put(i, new ArrayList<Integer>());
		}
		for(Entry entry : entries){
			for(Integer attr : entry.getAttributes()){
				attrToPatientsMap.get(attr).add(entry.getId());
			}
		}
	}
	
}
