package nullmodels;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import projection.Entry;

import core.Analyzer;

public class InformedExploration extends Analyzer{

	public InformedExploration() throws IOException {
		super("/home/harshit/workspace/CNT2014/HIV_data.xls");
	}

	public void printPresenceAbsenceMatrix(String filename, List<String> attributes) throws IOException{
		FileWriter fw = new FileWriter(new File(filename).getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("Attributes ");
		for(int i=0;i<entries.size();i++)
			bw.write("patient"+(i+1)+" ");
		bw.write("\n");
		for(String attr : attributes){
			bw.write(attr.replace(' ', '_') + " ");
			for(Entry entry : entries){
				if(attrToPatientsMap.get(attr2intMap.get(attr)).contains(entry.getId()))
					bw.write("1 ");
				else
					bw.write("0 ");
			}
			bw.write("\n");
		}
		bw.close();
	}
	
	public static void main(String[] args) throws IOException {
		InformedExploration informedExploration = new InformedExploration();
		informedExploration.run();
		
		List<String> attributes = Arrays.asList("mother_alive-YES", "septran_prophylaxis-YES");
		//List<String> attributes = Arrays.asList("mother_hiv-YES", "immunisation_completed-YES", "full_immunisation_completed-YES", "septran_prophylaxis-YES", "num_children_house <=2");
		//List<String> attributes = Arrays.asList("spneumo_bacteria-NO", "saureus_bacteria-NO", "mrsa_bacteria-NO");
		informedExploration.printPresenceAbsenceMatrix("/home/harshit/CNT2014/Null Models/Presence Absence Matrices/Naive Exploration/top 10/cluster2.txt", attributes);
	}

}
