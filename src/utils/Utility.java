package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import projection.Cluster;
import projection.Entry;

public class Utility 
{
	public static void main(String args[]) throws IOException
	{
		String data = "5, 2, 2, 3, 7, 5, 0, 2, 6, 3, 5, 4, 3, 5, 5, 8, 9, 5, 5, 2, 1, 8, 2, 5, 1, 8, 1, 5, 2, 0, 5, 3, 4, 7, 8, 9, 5, 0, 4, 5, 5, 3, 3, 8, 7, 5, 2, 9, 1, 5, 2, 0, 5, 2, 1, 9, 5, 4, 5, 5, 5, 9, 5, 3, 5, 1, 6, 5, 5, 6, 1, 1, 2, 5, 9, 2, 9, 6, 3, 6, 3, 4, 6, 9, 8, 2, 3, 7, 1, 0, 6, 5, 0, 6, 9, 7, 1, 4, 4, 6, 5, 0, 4, 0, 5, 0, 0, 2, 2, 0, 4, 5, 5, 9, 1, 9, 5, 4, 2, 4, 0, 5, 9, 2, 8, 6, 6, 2, 5, 5, 9, 0, 5, 1, 2, 2, 7, 1, 5, 1, 3, 6, 5, 6, 6, 5, 5, 5, 7, 5, 7";
		data = data.replace(", ",  "\n");
		//System.out.println(data);

		List<Cluster> list = new ArrayList<Cluster>();
		for(int i=0;i<10;i++)
			list.add(new Cluster());
		BufferedReader br = new BufferedReader(new FileReader("/home/harshit/Desktop/CNT_k_means/clusters.txt"));
		String line;
		int i=1;
		while ((line = br.readLine()) != null) 
		{
		   int cluster = Integer.parseInt(line);
		   list.get(cluster).addPatient(i);
		   i++;
		}
		br.close();
		FileInputStream file = new FileInputStream(new File("/home/harshit/Desktop/CNT2014/HIV_data.xls"));
		Workbook workbook = new HSSFWorkbook(file);
		org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
		Iterator<Row> rowIterator = sheet.iterator();
		Row row;
		i=0;
		String cellVal;
		int patientNo = 1;
		int clusterNo = 9;
		Cell cell;
		while(rowIterator.hasNext())
		{
			//System.out.println("Patient"+patientNo);
			row = rowIterator.next();
			if(list.get(clusterNo).getPatients().contains(patientNo))
			{
				for(int x=0;x<33;x++)
				{
					cell = row.getCell(x);
					if(cell != null)
						System.out.print(cell);
					System.out.print(";");
				}
				/*Iterator<Cell> cellIterator = row.cellIterator();
				while(cellIterator.hasNext())
				{
					System.out.println("Cell value : "+row.getCell(0));
					cellVal = cellIterator.next().getStringCellValue();
					//System.out.print(cellVal+";");
				}*/
				System.out.println();
			}
			//System.out.println("Cell value : "+row.getCell(0));
			patientNo++;
		}
	}
}
