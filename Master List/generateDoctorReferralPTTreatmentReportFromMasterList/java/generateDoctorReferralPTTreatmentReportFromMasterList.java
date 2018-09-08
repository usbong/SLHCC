/*
 * Copyright 2018 Usbong Social Systems, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.util.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/*

' Given:
' 1) Encoding for the Month Input Worksheet
' --> Saved/Exported as "Tab delimited" .txt file from Excel
' --> Example: input201808.txt (where the date format is YYYYMM; based on ISO 8601)
'
' Output:
' 1) Auto-generated Medical Doctor Referral PT Treatment Report
' --> "Tab delimited" .txt file 
*/ 

public class generateDoctorReferralPTTreatmentReportFromMasterList {	
	private static boolean inDebugMode = false;
	private static String inputFilename = "input201808"; //without extension
	
	private static Vector<String> referringDoctorContainer;
	private static double columnValuesArray[];
	private static final int TOTAL_COLUMNS = 5; //the actual first column in the output file, i.e. the date, is not included here
	
	private static final int INPUT_REFERRING_DOCTOR_COLUMN = 15;
	private static final int INPUT_DATE_COLUMN = 1;
	
	public static void main ( String[] args ) throws Exception
	{
		PrintWriter writer = new PrintWriter(inputFilename+"Output.txt", "UTF-8");
		
		File f = new File(inputFilename+".txt");

		Scanner sc = new Scanner(new FileInputStream(f));				

		referringDoctorContainer = new Vector<String>();
		columnValuesArray = new double[TOTAL_COLUMNS];		
		
		//init table header names
		writer.print("DATE:\t"); //"DATE:" column
		writer.print("REFERRING DOCTOR:\t"); //"REFERRING DOCTOR:" column
		writer.print("COUNT:\t"); //"COUNT:" column
		writer.print("TOTAL NET TREATMENT FEE:\t"); //"TOTAL NET TREATMENT FEE:" column
		writer.print("PAID NET TREATMENT FEE:\t"); //"PAID NET TREATMENT FEE:" column
		writer.println("UNPAID NET TREATMENT FEE:"); //"UNPAID NET TREATMENT FEE:" column
	
		String s;		
		s=sc.nextLine(); //skip the first row, which is the input file's table headers

		//count/compute the values for number-based columns 
		while (sc.hasNextLine()) {
			s=sc.nextLine();
			
			String[] columns = s.split("\t");
			
			if (!referringDoctorContainer.contains(columns[INPUT_REFERRING_DOCTOR_COLUMN])) {
				//referringDoctorContainer.add(columns[INPUT_DATE_COLUMN]);
				
				referringDoctorContainer.add(columns[INPUT_REFERRING_DOCTOR_COLUMN]);
				
//				writer.print(columns[INPUT_REFERRING_DOCTOR_COLUMN]+"\n"); //referring Medical Doctor name
			}
			
/*
			writer.print(columns[2]+"\t"); //transaction number
			writer.println(columns[3]); //patient name
*/			
		}			
		
		sc.close();
		writer.close();
	}
}