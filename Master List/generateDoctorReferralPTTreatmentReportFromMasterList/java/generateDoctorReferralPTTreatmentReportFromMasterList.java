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
import java.text.NumberFormat;

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
	
	private static String[] inputColumns;
	
	private static final int INPUT_REFERRING_DOCTOR_COLUMN = 15;
	private static final int INPUT_NOTES_COLUMN = 0;
	private static final int INPUT_DATE_COLUMN = 1;
	private static final int INPUT_CLASS_COLUMN = 8; //HMO and NON-HMO
	private static final int INPUT_NET_PF_COLUMN = 10;

	private static HashMap<String, double[]> referringDoctorContainer;	
	private static double[] columnValuesArray;
	
	//the date and the referring doctor are not yet included here
	//this is for both HMO and NON-HMO transactions
	private static final int OUTPUT_TOTAL_COLUMNS = 8; 
	
	private static final int OUTPUT_HMO_COUNT_COLUMN = 0; //transaction count
	private static final int OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN = 1;
	private static final int OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN = 2;
	private static final int OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN = 3;

	private static final int OUTPUT_NON_HMO_COUNT_COLUMN = 4; //transaction count
	private static final int OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN = 5;
	private static final int OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN = 6;
	private static final int OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN = 7;
		
	public static void main ( String[] args ) throws Exception
	{
		PrintWriter writer = new PrintWriter(inputFilename+"Output.txt", "UTF-8");
		
		File f = new File(inputFilename+".txt");

		Scanner sc = new Scanner(new FileInputStream(f));				

		referringDoctorContainer = new HashMap<String, double[]>();
		
		//init table header names
		writer.print("DATE:\t"); //"DATE:" column
		writer.print("REFERRING DOCTOR:\t"); //"REFERRING DOCTOR:" column
		writer.print("COUNT:\t"); //"COUNT:" column
		writer.print("TOTAL NET TREATMENT FEE:\t"); //"TOTAL NET TREATMENT FEE:" column
		writer.print("PAID NET TREATMENT FEE:\t"); //"PAID NET TREATMENT FEE:" column
		writer.println("UNPAID NET TREATMENT FEE:"); //"UNPAID NET TREATMENT FEE:" column
	
		String s;		
		s=sc.nextLine(); //skip the first row, which is the input file's table headers

		//count/compute the values for number-based inputColumns 
		while (sc.hasNextLine()) {
			s=sc.nextLine();
			
			inputColumns = s.split("\t");

			if (!referringDoctorContainer.containsKey(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])) {
				columnValuesArray = new double[OUTPUT_TOTAL_COLUMNS];
				
				if (inputColumns[INPUT_CLASS_COLUMN].contains("HMO")) {
					columnValuesArray[OUTPUT_HMO_COUNT_COLUMN] = 1;
					columnValuesArray[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);

					if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
						columnValuesArray[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
					}
				}
				else {
					columnValuesArray[OUTPUT_NON_HMO_COUNT_COLUMN] = 1;
					columnValuesArray[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);

					if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
						columnValuesArray[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
					}
				}

				referringDoctorContainer.put(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN], columnValuesArray);
			}
			else {
				if (inputColumns[INPUT_CLASS_COLUMN].contains("HMO")) {
					referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])[OUTPUT_HMO_COUNT_COLUMN]++;					
					referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] 
						+= Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						
					if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
						referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
					}
				}
				else {
					referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])[OUTPUT_NON_HMO_COUNT_COLUMN]++;					
					referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] 
						+= Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						
					if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
						referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
					}
				}
			}
		}			

		for (String key  : referringDoctorContainer.keySet()) {
			writer.println( getMonthYear(inputColumns[INPUT_DATE_COLUMN]) + 
							"\t" + key +
							"\t" + (int) referringDoctorContainer.get(key)[OUTPUT_HMO_COUNT_COLUMN] +
							"\t" + referringDoctorContainer.get(key)[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] +
							"\t" + referringDoctorContainer.get(key)[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN]); 				   							
		}
		
		sc.close();
		writer.close();
	}
	
	private static String getMonthYear(String date) {
		StringBuffer sb = new StringBuffer(date);				
		return sb.substring(0,3).concat("-").concat(sb.substring(sb.length()-2,sb.length()));
	}
}