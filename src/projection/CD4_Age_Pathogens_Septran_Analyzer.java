package projection;

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
import java.io.BufferedWriter;
import java.io.FileWriter;

public class CD4_Age_Pathogens_Septran_Analyzer 
{
	private final String complexAttributes[] = {"cd4_class", "age"};
	private double offset = 0;
	private int noOfEntries;
	private int noOfAttributes;
	private List<Entry> entries;
	private Map<String, Integer>attr2intMap;
	private Map<Integer, String>int2attrMap;
	private List<Integer> attrFreq;
	private List<Patient> patients;
	private int[][] projAdjMat;
	private int[][] patientProjAdjMat;
	private double[][] oddsRatio;
	private Map<Integer, String> int2pathogenMap;
	private Map<String, Integer> pathogen2intMap;
	private FileInputStream file;
	private HSSFWorkbook workbook;
	private HSSFSheet sheet;
	private String pathogens[] = {"hinfluenzae", "hinfluenzae_type_b", "mrsa_bacteria", "saureus_bacteria", "spneumo_bacteria", "sviridans_bacteria"};
	private String attributes[] = {"hinfluenzae", "hinfluenzae_type_b", "mrsa_bacteria", "saureus_bacteria", "spneumo_bacteria", "sviridans_bacteria", "cd4_class-NORMAL", "cd4_class-SEVERE", "cd4_class-MODERATE", "septran_prophylaxis", "age <=1", "age (1-2]", "age (2,5]", "age >5"};
	private String smallAttributes[] = {"mrsa_bacteria", "saureus_bacteria", "spneumo_bacteria", "sviridans_bacteria", "cd4_class-NORMAL", "cd4_class-SEVERE", "cd4_class-MODERATE", "septran_prophylaxis", "age <=1", "age (1-2]", "age (2,5]", "age >5"};
	public CD4_Age_Pathogens_Septran_Analyzer() throws IOException
	{
		oddsRatio = new double[pathogens.length][pathogens.length];
		patientProjAdjMat = new int[151][151];
		patients = new ArrayList<Patient>();
		projAdjMat = new int[100][100];
		noOfEntries = 0;
		entries = new ArrayList<>();
		attr2intMap = new HashMap<>();
		int2attrMap = new HashMap<>();
		int2pathogenMap = new HashMap<>();
		pathogen2intMap = new HashMap<>();
		file = new FileInputStream(new File("/home/harshit/Desktop/CNT2014/CD4_Age_OddsRatio/HIV_cd4_age_septran_pathogens.xls"));
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
		for(int i=0;i<pathogens.length;i++)
		{
			pathogen2intMap.put(pathogens[i], i);
			int2pathogenMap.put(i, pathogens[i]);
		}
		String pathogen1 = "", pathogen2 = "", pathogen1_YES = "", pathogen1_NO = "", pathogen2_YES = "", pathogen2_NO = "";
		double ad, bc;
		for(int i=0;i<pathogens.length;i++)
		{
			for(int j=i+1;j<pathogens.length;j++)
			{
				pathogen1 = int2pathogenMap.get(i);
				pathogen2 = int2pathogenMap.get(j);
				pathogen1_YES = pathogen1+"-YES";
				pathogen2_YES = pathogen2+"-YES";
				pathogen1_NO = pathogen1+"-NO";
				pathogen2_NO = pathogen2+"-NO";
				ad = projAdjMat[attr2intMap.get(pathogen1_YES)][attr2intMap.get(pathogen2_YES)] * projAdjMat[attr2intMap.get(pathogen1_NO)][attr2intMap.get(pathogen2_NO)] + offset;
				bc = projAdjMat[attr2intMap.get(pathogen1_YES)][attr2intMap.get(pathogen2_NO)] * projAdjMat[attr2intMap.get(pathogen1_NO)][attr2intMap.get(pathogen2_YES)] + offset;
				oddsRatio[i][j] = ad/((bc > 0)?bc:1);
				oddsRatio[j][i] = ad/((bc > 0)?bc:1);
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
		File file = new File("/home/harshit/Desktop/CNT2014/Thresholds/DendrogramInput/dendrogam-top"+list.size()+".txt");
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
		File file = new File("/home/harshit/Desktop/CNT2014/TOP_NET_FILES_NEW/top"+list.size()+".net");
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
	public void printTable(String pathogen1, String pathogen2)
	{
		String pathogen1_YES = pathogen1+"-YES";
		String pathogen1_NO = pathogen1+"-NO";
		String pathogen2_YES = pathogen2+"-YES";
		String pathogen2_NO = pathogen2+"-NO";
		System.out.println(","+pathogen1_YES+","+pathogen1_NO);
		System.out.println(pathogen2_YES+"," + projAdjMat[attr2intMap.get(pathogen2_YES)][attr2intMap.get(pathogen1_YES)]+","+projAdjMat[attr2intMap.get(pathogen2_YES)][attr2intMap.get(pathogen1_NO)]);
		System.out.println(pathogen2_NO+"," + projAdjMat[attr2intMap.get(pathogen2_NO)][attr2intMap.get(pathogen1_YES)]+","+projAdjMat[attr2intMap.get(pathogen2_NO)][attr2intMap.get(pathogen1_NO)]);
	}
	public void printOddsRatio() throws IOException
	{
		File file = new File("/home/harshit/Desktop/CNT2014/PathogenTables/OddsRatio.net");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("*Vertices "+pathogens.length+"\n");
		for(int i=0;i<pathogens.length;i++)
		{
			bw.write((i+1)+" \""+int2pathogenMap.get(i)+"\"\n");
		}
		bw.write("*Edges\n");
		for(int i=0;i<pathogens.length;i++)
		{
			for(int j=i+1;j<pathogens.length;j++)
			{
				bw.write((i+1)+" "+(j+1)+" "+oddsRatio[i][j]+"\n");
			}
		}
		bw.close();
	}
	/*Prints the table of cd4 class vs. pathogen
	 * Arguments
	 * cd4class : one of {"NORMAL", "MODERATE", "SEVERE"}
	 * pathogen
	 */
	public void printTableForCD4AgainstPathogen(String cd4class, String pathogen)
	{
		String pathogenYes = pathogen+"-YES";
		String pathogenNo = pathogen+"-NO";
		double a = offset,b = offset,c = offset,d = offset;
		String cd4class1 = null, cd4class2 = null; // Strings holding the cd4 class not sent as parameter
		if(cd4class.equals("NORMAL"))
		{
			cd4class1 = "cd4_class-MODERATE";
			cd4class2 = "cd4_class-SEVERE";
		}
		else if(cd4class.equals("MODERATE"))
		{
			cd4class1 = "cd4_class-NORMAL";
			cd4class2 = "cd4_class-SEVERE";
		}
		else if(cd4class.equals("SEVERE"))
		{
			cd4class1 = "cd4_class-NORMAL";
			cd4class2 = "cd4_class-SEVERE";
		}
		else
		{
			System.out.println("\n\nThe argument cd4class is not one of NORMAL, MODERATE, SEVERE. Please check.\n\n");
		}
		cd4class = "cd4_class-"+cd4class;
		// Now calculating the values of a, b, c, d
		
		a += projAdjMat(cd4class, pathogenYes);
		b += projAdjMat(cd4class, pathogenNo);
		c += projAdjMat(cd4class1, pathogenYes)+projAdjMat(cd4class2, pathogenYes);
		d += projAdjMat(cd4class1, pathogenNo)+projAdjMat(cd4class2, pathogenNo);
		System.out.println(","+pathogenYes+","+pathogenNo);
		System.out.println(cd4class+"-YES,"+a+","+b);
		System.out.println(cd4class+"-NO,"+c+","+d);
	}
	public void printTableForAgeAgainstPathogen(String age, String pathogen)
	{
		String pathogenYes = pathogen+"-YES";
		String pathogenNo = pathogen+"-NO";
		double a = offset,b = offset,c = offset,d = offset;
		String age1 = null, age2 = null, age3 = null; 
		if(age.equals("<=1"))
		{
			age1 = "age (1-2]";
			age2 = "age (2,5]";
			age3 = "age >5";
		}
		else if(age.equals("(1-2]"))
		{
			age1 = "age <=1";
			age2 = "age (2,5]";
			age3 = "age >5";
		}
		else if(age.equals("(2,5]"))
		{
			age1 = "age <=1";
			age2 = "age (1-2]";
			age3 = "age >5";
		}
		else if(age.equals(">5"))
		{
			age1 = "age <=1";
			age2 = "age (1-2]";
			age3 = "age (2,5]";
		}
		else
		{
			System.out.println("\n\nThe argument age is not one of <=1, (1-2], (2,5], >5. Please check.\n\n");
		}
		age = "age "+age;
		// Now calculating the values of a, b, c, d
		
		a += projAdjMat(age, pathogenYes);
		b += projAdjMat(age, pathogenNo);
		c += projAdjMat(age1, pathogenYes)+projAdjMat(age2, pathogenYes)+projAdjMat(age3, pathogenYes);
		d += projAdjMat(age1, pathogenNo)+projAdjMat(age2, pathogenNo)+projAdjMat(age3, pathogenNo);
		System.out.println(";"+pathogenYes+";"+pathogenNo);
		System.out.println(age+"-YES;"+a+";"+b);
		System.out.println(age+"-NO;"+c+";"+d);
	}
	public int projAdjMat(String attr1, String attr2)
	{
		return projAdjMat[attr2intMap.get(attr1)][attr2intMap.get(attr2)];
	}
	public boolean isComplexAttribute(String attr)
	{
		if(attr.contains("cd4") || attr.contains("age"))
			return true;
		return false;
	}
	public double getOddsRatioForCD4Attribute(String complex, String simple)
	{
		return 0;
	}
	public double getOddsRatioForAgeAttribute(String complex, String simple)
	{
		return 0;
	}
	public int getTypeOfComplexAttribute(String complex)
	{
		for(int i=0;i<complexAttributes.length;i++)
		{
			if(complex.contains(complexAttributes[i]))
				return i;
		}
		return -1;
	}
	public List<String> getRemainingComplexAttributes(String complex)
	{
		int typeOfComplexAttribute;
		List<String> remaining = new ArrayList<String>();
		if((typeOfComplexAttribute = getTypeOfComplexAttribute(complex))==-1)
		{
			System.out.println("ERROR IN DETERMINING THE TYPE OF COMPLEX ATTRIBUTE FOR THE COMPLEX ATTRIBUTE : "+ complex);
			System.out.println("EXITING");
			System.exit(0);
		}
		for(String attr : attributes)
		{
			if(attr.contains(complexAttributes[typeOfComplexAttribute]) && !(attr.equals(complex)))
				remaining.add(attr);
		}
		return remaining;
	}
	public double getOddsRatioForComplexAttribute(String complex, String simple)
	{
		String simple_YES = simple+"-YES";
		String simple_NO = simple+"-NO";
		List<String> remainingAttributes = getRemainingComplexAttributes(complex);
		double a = projAdjMat(complex, simple_YES) + offset;
		double b = 0 + offset;
		double c = projAdjMat(complex, simple_NO) + offset;
		double d = 0 + offset;
		for(String attr : remainingAttributes)
		{
			b += projAdjMat(attr, simple_YES);
			d += projAdjMat(attr, simple_NO);
		}
		return (a*d)/(((b*c)>0)?(b*c):1);
	}
	public double getOddsRatioForSimpleAttributes(String attr1, String attr2)
	{
		String attr1_YES = attr1+"-YES";
		String attr1_NO = attr1+"-NO";
		String attr2_YES = attr2+"-YES";
		String attr2_NO = attr2+"-NO";
		double ad = projAdjMat[attr2intMap.get(attr1_YES)][attr2intMap.get(attr2_YES)] * projAdjMat[attr2intMap.get(attr1_NO)][attr2intMap.get(attr2_NO)];
		double bc = projAdjMat[attr2intMap.get(attr1_YES)][attr2intMap.get(attr2_NO)] * projAdjMat[attr2intMap.get(attr1_NO)][attr2intMap.get(attr2_YES)];
		return ad/((bc > 0)?bc:1);
	}
	public double getOddsRatio(String attr1, String attr2)
	{
		if(isComplexAttribute(attr1))
		{
			return getOddsRatioForComplexAttribute(attr1, attr2);
		}
		if(isComplexAttribute(attr2))
			return getOddsRatio(attr2, attr1);
		
		return getOddsRatioForSimpleAttributes(attr1, attr2);
	}
	public double getOddsRatio(int i, int j)
	{
		return getOddsRatio(attributes[i], attributes[j]);
	}
	public boolean isEdgeValid(int i, int j)
	{
		if(isComplexAttribute(attributes[i]) && isComplexAttribute(attributes[j]))
			return false;
		if(attributes[i].contains("cd4") && attributes[j].contains("cd4"))
			return false;
		if(attributes[i].contains("hinf") && attributes[j].contains("hinf"))
			return false;
		return true;
	}
	public boolean isEdgeValid(String attributes[], int i, int j)
	{
		if(isComplexAttribute(attributes[i]) && isComplexAttribute(attributes[j]))
			return false;
		if(attributes[i].contains("cd4") && attributes[j].contains("cd4"))
			return false;
		return true;
	}
	public void work()
	{
		System.out.println("*Vertices "+attributes.length);
		for(int i=0;i<attributes.length;i++)
		{
			System.out.println((i+1)+" \""+attributes[i]+"\"");
		}
		System.out.println("Edges");
		for(int i=0;i<attributes.length;i++)
		{
			for(int j=i+1;j<attributes.length;j++)
			{
				if(isEdgeValid(i,j))
					System.out.println((i+1)+" "+(j+1)+" "+getOddsRatio(i,j));
			}
		}

	}
	public void generateColourCodedNetwork(){
		System.out.println("*Vertices "+attributes.length);
		for(int i=0;i<attributes.length;i++){
			System.out.println((i+1)+" \""+attributes[i]+"\"");
		}
		System.out.println("Edges");
		double oddsRatio = 0.0;
		String colour = null;
		for(int i=0;i<attributes.length;i++){
			for(int j=i+1;j<attributes.length;j++){
				if(isEdgeValid(i,j)){
					oddsRatio = getOddsRatio(i,j);
					colour = "Blue";
					if(oddsRatio < 1){
						colour = "Red";
						if(oddsRatio > 0)
							oddsRatio = 1/oddsRatio;
						else
							oddsRatio = 370;
					}
					System.out.println((i+1)+" "+(j+1)+" "+ oddsRatio + " c " + colour);
				}
					
			}
		}
	}
	public void printDendrogramInput() throws IOException
	{
		File file = new File("/home/harshit/Desktop/CNT2014/CD4_Age_Pathogens_Septran/DendrogramInput/dendrogam.txt");
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
		for(int i=0;i<noOfAttributes;i++)
		{
			for(int j=0;j<noOfAttributes;j++)
			{
				bw.write(projAdjMat[i][j]+"\t");
			}
			bw.write("\n");
		}
		bw.close();
	}
	public void printOddsRatioDendrogramInputWithoutHinfluenzae() throws IOException
	{
		File file = new File("/home/harshit/Desktop/CNT2014/CD4_Age_Pathogens_Septran/DendrogramInput/oddsRatioDendrogamWithoutHinfluenzae.txt");
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
		for(int i=0;i<smallAttributes.length;i++)
		{
			bw.write(smallAttributes[i].replace(' ', '_').replace('\t',	'_').replace(',','_').replace(';', '_').replace('|',  '_')+"\t");
		}
		bw.write("\n");
		for(int i=0;i<smallAttributes.length;i++)
		{
			for(int j=0;j<smallAttributes.length;j++)
			{
				if(isEdgeValid(smallAttributes, i, j))
					bw.write(getOddsRatio(smallAttributes[i], smallAttributes[j])+"\t");
				else
					bw.write("0" + "\t");
			}
			bw.write("\n");
		}
		bw.close();
	}
	public void printOddsRatioDendrogramInput() throws IOException
	{
		File file = new File("/home/harshit/Desktop/CNT2014/CD4_Age_Pathogens_Septran/DendrogramInput/oddsRatioDendrogam.txt");
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
		for(int i=0;i<attributes.length;i++)
		{
			bw.write(attributes[i].replace(' ', '_').replace('\t',	'_').replace(',','_').replace(';', '_').replace('|',  '_')+"\t");
		}
		bw.write("\n");
		for(int i=0;i<attributes.length;i++)
		{
			for(int j=0;j<attributes.length;j++)
			{
				if(isEdgeValid(i, j))
					bw.write(getOddsRatio(i, j)+"\t");
				else
					bw.write("0" + "\t");
			}
			bw.write("\n");
		}
		bw.close();
	}
	public void printOddsRatioDendrogramInput(String filename, boolean positiveRisk) throws IOException
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
		for(int i=0;i<attributes.length;i++){
			bw.write(attributes[i].replace(' ', '_').replace('\t',	'_').replace(',','_').replace(';', '_').replace('|',  '_')+"\t");
		}
		bw.write("\n");
		double edgeWt = 0.0;
		for(int i=0;i<attributes.length;i++){
			for(int j=0;j<attributes.length;j++){
				if(isEdgeValid(i, j)){
					if(positiveRisk){
						edgeWt = getOddsRatio(i, j);
					}
					else{
						edgeWt = (getOddsRatio(i, j) > 0)?(1/getOddsRatio(i, j)):370;
					}
					bw.write(edgeWt+"\t");
				}
					
				else
					bw.write("0" + "\t");
			}
			bw.write("\n");
		}
		bw.close();
	}
	public String getGraphMlDeclarations(){
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\nxsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">\n";
	}
	public String getKeyDefinitionForEdges(){
		return "<key id=\"d1\" for=\"edge\" attr.name=\"weight\" attr.type=\"double\"/>"+ "\n" +"<key attr.name=\"r\" attr.type=\"int\" for=\"edge\" id=\"r\"><default>255</default></key>\n<key attr.name=\"g\" attr.type=\"int\" for=\"edge\" id=\"g\"><default>0</default></key>\n<key attr.name=\"b\" attr.type=\"int\" for=\"edge\" id=\"b\"><default>0</default></key>\n";	
	}
	public String getKeyDefinitionForNodes(){
		return "<key attr.name=\"label\" attr.type=\"string\" for=\"node\" id=\"label\" />" + "\n" +"<key attr.name=\"r\" attr.type=\"int\" for=\"node\" id=\"r_node\"><default>0</default></key>\n<key attr.name=\"g\" attr.type=\"int\" for=\"node\" id=\"g_node\"><default>0</default></key>\n<key attr.name=\"b\" attr.type=\"int\" for=\"node\" id=\"b_node\"><default>255</default></key>\n";	
		
	}
	private String getGMLforRedColouredEdge(){
		return "<data key=\"r\">255</data>\n<data key=\"g\">0</data>\n<data key=\"b\">0</data>\n";
	}
	private String getGMLforBlueColouredEdge(){
		return "<data key=\"r\">0</data>\n<data key=\"g\">0</data>\n<data key=\"b\">255</data>\n";
	}
	public String getEdgeDescription(int i, int j, int id){
		StringBuilder sb = new StringBuilder();
		sb.append("<edge id=\"e"+id+"\" source=\""+i+"\" target=\""+j+"\">\n");
		double oddsRatio = getOddsRatio(i,j);
		if(oddsRatio < 1){
			sb.append(getGMLforBlueColouredEdge());
			if(oddsRatio > 0)
				oddsRatio = 1/oddsRatio;
			else
				oddsRatio = 370;
		}
		else
			sb.append(getGMLforRedColouredEdge());
		sb.append("<data key=\"d1\">"+oddsRatio+"</data>\n");
		sb.append("</edge>\n");
		return sb.toString();
	}
	public String getGraphCharacteristicsGraphML(){
		StringBuilder sb = new StringBuilder();
		sb.append(getGraphMlDeclarations());
		
		sb.append("<graph id=\"G\" edgedefault=\"undirected\">\n");
		sb.append(getKeyDefinitionForEdges());
		sb.append(getKeyDefinitionForNodes());
		for(int i=0;i<attributes.length;i++){
			sb.append("<node id=\""+i+"\">\n");
			sb.append("<data key=\"label\">"+attributes[i]+"</data>\n");
			sb.append("</node>\n");
		}
		int id = 0;
		for(int i=0;i<attributes.length;i++){
			for(int j=i+1;j<attributes.length;j++){
				if(isEdgeValid(i,j)){
					sb.append(getEdgeDescription(i, j, id));
					id++;
				}
			}
		}
		sb.append("</graph>\n</graphml>");
		return sb.toString();
	}
	public void printGraphMLtoFile(String fileName) throws IOException{
		File file = new File(fileName);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		
		bw.write(getGraphCharacteristicsGraphML());
		bw.close();
		
	}
	public static void main(String[] args) throws IOException 
	{
		CD4_Age_Pathogens_Septran_Analyzer projector = new CD4_Age_Pathogens_Septran_Analyzer();
		projector.run();
		//projector.printGraphMLtoFile("/home/harshit/Desktop/OR.graphml");
		projector.printOddsRatioDendrogramInput("/home/harshit/Desktop/OR_more_than_one.txt", true);
	}
}