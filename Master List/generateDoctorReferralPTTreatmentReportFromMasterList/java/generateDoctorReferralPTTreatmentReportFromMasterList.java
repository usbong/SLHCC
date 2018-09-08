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

	public static void main ( String[] args ) throws Exception
	{
		PrintWriter writer = new PrintWriter(inputFilename+"Output.txt", "UTF-8");
		
		File f = new File(inputFilename+".txt");

		Scanner sc = new Scanner(new FileInputStream(f));				

		String s;

		while (sc.hasNextLine()) {

			s=sc.nextLine();
			
			String[] columns = s.split("\t");

			writer.print(columns[2]+"\t"); //transaction number
			writer.println(columns[3]); //patient name
		}			
		
		sc.close();
		writer.close();
	}
}