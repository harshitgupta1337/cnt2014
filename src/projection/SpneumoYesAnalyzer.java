package projection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.impl.NoOpLog;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class SpneumoYesAnalyzer 
{
	private int noOfEntries;
	private int noOfAttributes;
	private List<Entry> entries;
	private Map<String, Integer>attr2intMap;
	private Map<Integer, String>int2attrMap;
	private List<Integer> attrFreq;
	private List<Patient> patients;
	private int[][] projAdjMat;
	private int[][] patientProjAdjMat;
	private FileInputStream file;
	private HSSFWorkbook workbook;
	private HSSFSheet sheet;
	public SpneumoYesAnalyzer() throws IOException
	{
		patientProjAdjMat = new int[151][151];
		patients = new ArrayList<Patient>();
		projAdjMat = new int[100][100];
		noOfEntries = 0;
		entries = new ArrayList<>();
		attr2intMap = new HashMap<>();
		int2attrMap = new HashMap<>();
		file = new FileInputStream(new File("/home/harshit/Desktop/CNT2014/spneumo_YES/spneumo_yes.xls"));
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
	public void plotThreshold(int threshold) throws IOException
	{
		File file = new File("/home/harshit/Desktop/CNT2014/Thresholds/COMP/threshold_"+threshold+".NET");
		if (!file.exists()) {
			file.createNewFile();
		}
		List<Integer> nodes = new ArrayList<>();
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		int temp[][] = new int[86][86];
		int deg[] = new int[86];
		for(int i=0;i<noOfAttributes;i++)
		{
			for(int j=0;j<noOfAttributes;j++)
				temp[i][j]=0;
		}
		for(int i=0;i<noOfAttributes;i++)
		{
			for(int j=0;j<noOfAttributes;j++)
			{
				if(projAdjMat[i][j] > threshold)
				{
					temp[i][j] = 1;
				}
			}
		}
		for(int i=0;i<noOfAttributes;i++)
		{
			deg[i]=0;
		}
		int degree = 0;
		for(int i=0;i<noOfAttributes;i++)
		{
			degree=0;
			for(int j=0;j<noOfAttributes;j++)
			{
				if(temp[i][j]==1)
					degree++;
			}
			deg[degree]++;
		}
		int flag;
		for(int i=0;i<noOfAttributes;i++)
		{
			flag = 0;
			for(int j=0;j<noOfAttributes;j++)
			{
				if(temp[i][j] == 1)
					flag = 1;
			}
			if(flag == 1)
				nodes.add(i);
		}
		bw.write("*Vertices "+nodes.size()+"\n");
		for(int i=0;i<nodes.size();i++)
		{
			bw.write(nodes.get(i)+" \""+int2attrMap.get(nodes.get(i))+"\"\n");
		}
		List<Pair> edges = new ArrayList<>();
		for(int i=0;i<noOfAttributes;i++)
		{
			for(int j=0;j<i;j++)
			{
				if(temp[i][j] == 1)
				{
//					bw.write(i+" "+j+"\n");
					edges.add(new Pair(i, j));
				}
			}			
		}
		bw.write("*Edges "+edges.size()+"\n");
		for(int i=0;i<edges.size();i++)
			bw.write(edges.get(i).x+" "+edges.get(i).y+"\n");
		bw.close();
	}
	public void printPatientsProjection() throws IOException
	{
		File file = new File("/home/harshit/Desktop/CNT2014/patient-projection.net");
		if (!file.exists()) 
		{
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("*Vertices "+entries.size()+"\n");
		System.out.println("*Vertices "+entries.size());
		for(int i=0;i<entries.size();i++)
		{
			bw.write((1+entries.get(i).getId())+" \"Patient_"+entries.get(i).getId()+"\"\n");
			System.out.println((1+entries.get(i).getId())+" \"Patient_"+entries.get(i).getId()+"\"");
		}
		bw.write("*Edges\n");
		System.out.println("*Edges");
		for(int i=0;i<noOfEntries;i++)
		{
			for(int j=0;j<i;j++)
			{
				if(patientProjAdjMat[i][j] > 0)
				{
					bw.write((i+1)+" "+(j+1)+" "+patientProjAdjMat[i][j]+"\n");
					System.out.println((i+1)+" "+(j+1)+" "+patientProjAdjMat[i][j]);
				}
			}
		}
	}
	public List<Pair> listTop(int n)
	{
		List<Pair> list = new ArrayList<>();
		for(int i=0;i<noOfAttributes;i++)
		{
			list.add(new Pair(i, attrFreq.get(i)));
		}
		for(int i=0;i<noOfAttributes;i++)
		{
			int small = list.get(i).y;
			int smallIn = i;
			for(int j=i;j<noOfAttributes;j++)
			{
				if(list.get(j).y > small)
				{
					smallIn = j;
					small = list.get(j).y;
				}
			}
			Pair temp = new Pair(list.get(i).x, list.get(i).y);
			list.get(i).x = list.get(smallIn).x;
			list.get(i).y = list.get(smallIn).y;
			list.get(smallIn).x = temp.x;
			list.get(smallIn).y = temp.y;
		}
		/*
		for(int i=0;i<n;i++)
			System.out.print(list.get(i).y+" ");
		System.out.println();
		*/
		return list.subList(0, n);
	}
	public void printSubGraph(List<Pair> list) throws IOException
	{
		File file = new File("/home/harshit/Desktop/CNT2014/spneumo_YES/dendrogam-top"+list.size()+".txt");
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
		for(int i=0;i<list.size();i++)
		{
			bw.write(int2attrMap.get(list.get(i).x).replace(' ', '_').replace('\t',	'_').replace(',','_').replace(';', '_').replace('|',  '_')+"\t");
		}
		bw.write("\n");
		for(int i=0;i<list.size();i++)
		{
			for(int j=0;j<list.size();j++)
			{
				bw.write(projAdjMat[list.get(i).x][list.get(j).x]+"\t");
			}
			bw.write("\n");
		}
		bw.close();
		
	}
	
	public void printPatientsDendrogram() throws IOException
	{
		File file = new File("/home/harshit/Desktop/CNT2014/patients-projection-dendrogram-nos.txt");
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
		for(int i=0;i<entries.size();i++)
		{
			//bw.write("Patient_"+entries.get(i).getId()+"\t");
			bw.write(entries.get(i).getId()+"\t");
		}
		bw.write("\n");
		for(int i=0;i<entries.size();i++)
		{
			for(int j=0;j<entries.size();j++)
			{
				bw.write(patientProjAdjMat[entries.get(i).getId()][entries.get(j).getId()]+"\t");
			}
			bw.write("\n");
		}
		bw.close();
		
	}
	
	
	public void printSubGraphNet(List<Pair> list) throws IOException
	{
		File file = new File("/home/harshit/Desktop/CNT2014/spneumo_YES/top"+list.size()+".net");
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
		bw.write("*Vertices "+list.size()+"\n");
		for(int i=0;i<list.size();i++)
		{
			bw.write((i+1) +" \""+int2attrMap.get(list.get(i).x)+"\"\n");
		}
		bw.write("*Edges\n");
		for(int i=0;i<list.size();i++)
		{
			for(int j=i+1;j<list.size();j++)
			{
				if(projAdjMat[list.get(i).x][list.get(j).x] > 0)
					bw.write((i+1) + " " + (j+1) + " "+ projAdjMat[list.get(i).x][list.get(j).x] +"\n");
			}
		}
		bw.close();
		
	}
	public List<Pair> generateDennisList()
	{
		List<Pair> list = new ArrayList<>();
		for(int i=0;i<noOfAttributes;i++)
		{
			if((int2attrMap.get(i).contains("hinfluenzae") || int2attrMap.get(i).contains("saureus") || int2attrMap.get(i).contains("spneumo") || int2attrMap.get(i).contains("sviridans") || int2attrMap.get(i).contains("mrsa") || int2attrMap.get(i).contains("cd4_class") || int2attrMap.get(i).contains("hiv_medicines") || int2attrMap.get(i).contains("age") || int2attrMap.get(i).contains("septran_prophylaxis")) && !int2attrMap.get(i).contains("movement"))
				list.add(new Pair(i, attrFreq.get(i)));
		}
		return list;
	}
	public void printBipartiteNet() throws IOException
	{
		File file = new File("/home/harshit/Desktop/CNT2014/bipartite.net");
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
		bw.write("*Vertices "+(noOfAttributes+noOfEntries)+"\n");
		List<Pair> dennis = generateDennisList();
		List<Integer> dennisInteger = new ArrayList<>();
		for(Pair pair : dennis)
		{
			dennisInteger.add(pair.x);
		}
		for(int i=0;i<noOfEntries;i++)
		{
			bw.write((i+1) +" \"Patient"+(i+1)+"\"\n");
		}
		for(int i=0;i<dennisInteger.size();i++)
		{
			bw.write((i+1+noOfEntries) +" \""+int2attrMap.get(dennisInteger.get(i))+"\"\n");
		}
		bw.write("*Edges\n");
		int attribute;
		for(int i=0;i<noOfEntries;i++)
		{
			for(int j=0;j<entries.get(i).getAttributes().size();j++)
			{
				attribute = entries.get(i).getAttributes().get(j);
				if(dennisInteger.contains(attribute))
					bw.write((i+1)+" "+(dennisInteger.indexOf(attribute)+1+noOfEntries)+" 1\n");
			}
		}
		
		bw.close();
	}
	public void printTable()
	{
		System.out.println("\thiv_medicines_YES\thiv_medicines_NO");
		System.out.println("hinfluenzae_YES\t" + projAdjMat[attr2intMap.get("hinfluenzae-YES")][attr2intMap.get("hiv_medicines-YES")]+"\t"+projAdjMat[attr2intMap.get("hinfluenzae-YES")][attr2intMap.get("hiv_medicines-NO")]);
		System.out.println("hinfluenzae_NO\t" + projAdjMat[attr2intMap.get("hinfluenzae-NO")][attr2intMap.get("hiv_medicines-YES")]+"\t"+projAdjMat[attr2intMap.get("hinfluenzae-NO")][attr2intMap.get("hiv_medicines-NO")]);
	}
	public void printCsvForAttributes()
	{
		System.out.println("\n\n\n\n\n\n");
//		System.out.print("patient\tage\thinfluenza\thiv_medicines\tcd4_class\thinfluenza_type_b\tmrsa\tsaureus\tspneumo\tsviridans\tseptran");
		int i=1;
		for(Entry entry : entries)
		{
			System.out.print("[ ");
//			System.out.print("Patient"+i+"\t");
			//System.out.print("[");
			// age
			if(entry.getAttributes().contains(attr2intMap.get("age <=1")))
				System.out.print(-2);
			else if(entry.getAttributes().contains(attr2intMap.get("age (1-2]")))
				System.out.print(-1);
			else if(entry.getAttributes().contains(attr2intMap.get("age (2,5]")))
				System.out.print(1);
			else if(entry.getAttributes().contains(attr2intMap.get("age >5")))
				System.out.print(2);
			else
				System.out.print(0);
			System.out.print(", ");
			
			// hinfluenza
			if(entry.getAttributes().contains(attr2intMap.get("hinfluenzae-NO")))
				System.out.print(-1);
			else if(entry.getAttributes().contains(attr2intMap.get("hinfluenzae-YES")))
				System.out.print(1);
			else
				System.out.print(0);
			System.out.print(", ");
			
			//hiv_medicines
			if(entry.getAttributes().contains(attr2intMap.get("hiv_medicines-NO")))
				System.out.print(-1);
			else if(entry.getAttributes().contains(attr2intMap.get("hiv_medicines-YES")))
				System.out.print(1);
			else
				System.out.print(0);
			System.out.print(", ");
			
			//cd4-class
			if(entry.getAttributes().contains(attr2intMap.get("cd4_class-NORMAL")))
				System.out.print(-2);
			else if(entry.getAttributes().contains(attr2intMap.get("cd4_class-MODERATE")))
				System.out.print(-1);
			else if(entry.getAttributes().contains(attr2intMap.get("cd4_class-SEVERE")))
				System.out.print(1);
			else
				System.out.print(0);
			System.out.print(", ");
			
			//hinfluenza-type-b
			if(entry.getAttributes().contains(attr2intMap.get("hinfluenzae_type_b-NO")))
				System.out.print(-1);
			else if(entry.getAttributes().contains(attr2intMap.get("hinfluenzae_type_b-YES")))
				System.out.print(1);
			else
				System.out.print(000);
			System.out.print(", ");
		 	
			//mrsa-bacteria
			if(entry.getAttributes().contains(attr2intMap.get("mrsa_bacteria-NO")))
				System.out.print(-1);
			else if(entry.getAttributes().contains(attr2intMap.get("mrsa_bacteria-YES")))
				System.out.print(1);
			else
				System.out.print(0);
			System.out.print(", ");
			
			//sauerua
			if(entry.getAttributes().contains(attr2intMap.get("saureus_bacteria-NO")))
				System.out.print(-1);
			else if(entry.getAttributes().contains(attr2intMap.get("saureus_bacteria-YES")))
				System.out.print(1);
			else
				System.out.print(0);
			System.out.print(", ");
			
			//spneumo
			if(entry.getAttributes().contains(attr2intMap.get("spneumo_bacteria-NO")))
				System.out.print(-1);
			else if(entry.getAttributes().contains(attr2intMap.get("spneumo_bacteria-YES")))
				System.out.print(1);
			else
				System.out.print(0);
			System.out.print(", ");
			
			//sviridans
			if(entry.getAttributes().contains(attr2intMap.get("sviridans_bacteria-NO")))
				System.out.print(-1);
			else if(entry.getAttributes().contains(attr2intMap.get("sviridans_bacteria-YES")))
				System.out.print(1);
			else
				System.out.print(0);
			System.out.print(", ");
			
			//septran
			if(entry.getAttributes().contains(attr2intMap.get("septran_prophylaxis-YES")))
				System.out.print(-1);
			else if(entry.getAttributes().contains(attr2intMap.get("septran_prophylaxis-NO")))
				System.out.print(1);
			else
				System.out.print(0);
			System.out.print("],");
			System.out.println();
			i++;
		}
	}
	public static void main(String[] args) throws IOException 
	{
		SpneumoYesAnalyzer projector = new SpneumoYesAnalyzer();
		projector.run();
		System.out.println(projector.noOfAttributes);
		List<Pair> list = projector.listTop(projector.noOfAttributes);
		projector.printSubGraph(list);
		projector.printSubGraphNet(list);
		
		
	}
}
