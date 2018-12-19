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
import java.text.DecimalFormat;
//import java.lang.Integer;

/*
' Given:
' 1) Encoding for the Month Input Worksheet
' --> Saved/Exported as "Tab delimited" .txt file from Excel
' --> Example: input201808.txt (where the date format is YYYYMM; based on ISO 8601)
'
' Output:
' 1) Auto-generated PT Treatment Year End Summary Report
' --> "Tab delimited" .txt file 
' --> Regardless of the name of the input file or input files, the output file will be "YearEndSummaryReportOutput.txt".
'
' Notes:
' 1) To execute the add-on software/application simply use the following command:
'   java generatePTTreatmentYearEndSummaryReportOfAllInputFilesFromMasterList input201801.txt
' 
' where: "input201801.txt" is the name of the file.
' 
' 2) To execute a set of input files, e.g. input201801.txt, input201802.txt, you can use the following command: 
'  java generatePTTreatmentYearEndSummaryReportOfAllInputFilesFromMasterList input*
'
' where: "input*" means any file in the directory that starts with "input".
*/ 

public class generatePTTreatmentYearEndSummaryReportOfAllInputFilesFromMasterList {	
	private static boolean inDebugMode = true;
	private static String inputFilename = "input201801"; //without extension; default input file
	
	private static String startDate = null;
	private static String endDate = null;
	
	private static final int INPUT_REFERRING_DOCTOR_COLUMN = 15;
	private static final int INPUT_NOTES_COLUMN = 0;
	private static final int INPUT_DATE_COLUMN = 1;
	private static final int INPUT_CLASS_COLUMN = 8; //HMO and NON-HMO
	private static final int INPUT_NET_PF_COLUMN = 10;
	private static final int INPUT_NEW_OLD_COLUMN = 16;
	private static final int INPUT_CONSULTATION_PROCEDURE_COLUMN = 3;

	//added by Mike, 20181218
	//CONSULTATION
/*	
	private static final int INPUT_CONSULTATION_CLASS_COLUMN = 9;
	private static final int INPUT_CONSULTATION_NET_PF_COLUMN = 11;
	private static final int INPUT_CONSULTATION_NEW_OLD_COLUMN = 17;
*/	
	private static final int INPUT_CONSULTATION_OFFSET = 1;
		
		
/*	private static HashMap<String, double[]> referringDoctorContainer;	
*/
	private static HashMap<Integer, double[]> dateContainer;	//added by Mike, 201801205
	private static HashMap<String, double[]> hmoContainer;	//added by Mike, 201801217
	private static HashMap<String, double[]> nonHmoContainer;	//added by Mike, 201801217
	private static HashMap<String, double[]> referringDoctorContainer; //added by Mike, 20181218

	private static double[] columnValuesArray;
	private static String[] dateValuesArray; //added by Mike, 20180412
	private static int[] dateValuesArrayInt; //added by Mike, 20181206
		
	//the date and the referring doctor are not yet included here
	//this is for both HMO and NON-HMO transactions
	private static final int OUTPUT_TOTAL_COLUMNS = 15; 

	//PT TREATMENT
	private static final int OUTPUT_HMO_COUNT_COLUMN = 0; //transaction count
	private static final int OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN = 1;
	private static final int OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN = 2;
	private static final int OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN = 3;
	private static final int OUTPUT_HMO_NEW_OLD_COUNT_COLUMN = 4;

	private static final int OUTPUT_NON_HMO_COUNT_COLUMN = 5; //transaction count
	private static final int OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN = 6;
	private static final int OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN = 7;
	private static final int OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN = 8;
	private static final int OUTPUT_NON_HMO_NEW_OLD_COUNT_COLUMN = 9;

	private static final int OUTPUT_DATE_ID_COLUMN = 10; //added by Mike, 20181205
	
	//CONSULTATION
	private static final int OUTPUT_CONSULTATION_HMO_COUNT_COLUMN = 11; //transaction count
	private static final int OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN = 12; //transaction count
	private static final int OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN = 13; //transaction count
	private static final int OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN = 14; //transaction count

	private static boolean isConsultation;
	
	private static DecimalFormat df = new DecimalFormat("0.00"); //added by Mike, 20181105
	private static int rowCount; //added by Mike, 20181105
				
	private static int totalCountForAllReferringDoctors;
	private static double totalNetTreatmentFeeForAllReferringDoctors;
	private static double totalPaidNetTreatmentFeeForAllReferringDoctors;
	private static double totalUnpaidNetTreatmentFeeForAllReferringDoctors;
	private static double totalFivePercentShareOfNetPaidForAllReferringDoctors;
				
	public static void main ( String[] args ) throws Exception
	{			
		makeFilePath("output"); //"output" is the folder where I've instructed the add-on software/application to store the output file			
		PrintWriter writer = new PrintWriter("output/YearEndSummaryReportOutput.txt", "UTF-8");			
		/*referringDoctorContainer = new HashMap<String, double[]>();
		*/
		
		dateContainer = new HashMap<Integer, double[]>();
		hmoContainer = new HashMap<String, double[]>();
		nonHmoContainer = new HashMap<String, double[]>();
		referringDoctorContainer = new HashMap<String, double[]>();
		
		//added by Mike, 20181116
		startDate = null; //properly set the month and year in the output file of each input file
		dateValuesArray = new String[args.length]; //added by Mike, 20180412
		dateValuesArrayInt = new int[args.length]; //added by Mike, 20180412
		
		//edited by Mike, 20181030
		for (int i=0; i<args.length; i++) {						
			//added by Mike, 20181030
			inputFilename = args[i].replaceAll(".txt","");			
			File f = new File(inputFilename+".txt");

			System.out.println("inputFilename: " + inputFilename);
			
			if (inputFilename.toLowerCase().contains("consultation")) {
				isConsultation=true;
			}
			else {
				isConsultation=false;
			}
			
			Scanner sc = new Scanner(new FileInputStream(f));				
		
			String s;		
			s=sc.nextLine(); //skip the first row, which is the input file's table headers
	
			if (inDebugMode) {
				rowCount=0;
			}
						
			//count/compute the number-based values of inputColumns 
			while (sc.hasNextLine()) {
				s=sc.nextLine();
				
				//if the row is blank
				if (s.trim().equals("")) {
					continue;
				}
				
				String[] inputColumns = s.split("\t");					
				
				//added by Mike, 20180412
				if (dateValuesArray[i]==null) {
					dateValuesArray[i] = getMonthYear(inputColumns[INPUT_DATE_COLUMN]);
				}

				//added by Mike, 20181206
				if (dateValuesArrayInt[i]==0) {
					dateValuesArrayInt[i] = Integer.parseInt(args[i].substring(args[i].indexOf("_")+1,args[i].indexOf(".txt")));
				}
				
				
				//edited by Mike, 20181121
				if (startDate==null) {
					startDate = getMonthYear(inputColumns[INPUT_DATE_COLUMN]);
					endDate = startDate;
				}
				else {
					//edited by Mike, 20181121
					//add this condition in case the input file does not have a date for each transaction; however, ideally, for input files 2018 onwards, each transaction should have a date
					if (!inputColumns[INPUT_DATE_COLUMN].trim().equals("")) {
						endDate = getMonthYear(inputColumns[INPUT_DATE_COLUMN]);
					}
				}

				if (inDebugMode) {
					rowCount++;
					System.out.println("rowCount: "+rowCount);
				}
				
				//added by Mike, 20181121
				//skip transactions that have "RehabSupplies" as its "CLASS" value
				//In Excel logbook/workbook 2018 onwards, such transactions are not included in the Consultation and PT Treatment Excel logbooks/workbooks.
				if (inputColumns[INPUT_CLASS_COLUMN].contains("RehabSupplies")) {
					continue;
				}
				
				//added by Mike, 20181216
//				processMonthlyCount(dateContainer, inputColumns, i, false);
				processMonthlyCount(dateContainer, inputColumns, i, isConsultation); //isConsultation = false

				//added by Mike, 20181217
				processHMOCount(hmoContainer, inputColumns, isConsultation); //edited by Mike, 20181219
				
				//added by Mike, 20181217
				processNONHMOCount(nonHmoContainer, inputColumns, isConsultation); //edited by Mike, 20181219
				
				//added by Mike, 20181218
				processReferringDoctorTransactionCount(referringDoctorContainer, inputColumns, isConsultation); //edited by Mike, 20181219
				
			}
			//added by Mike, 20181205
			columnValuesArray[OUTPUT_DATE_ID_COLUMN] = i; 			
		}
		
		/*
		 * --------------------------------------------------------------------
		 * OUTPUT
		 * --------------------------------------------------------------------
		*/
		//added by Mike, 20181118
		writer.print("Annual Year End Summary Report\n");
		
		//--------------------------------------------------------------------
		//init table header names
		writer.print("\tTREATMENT COUNT:\tCONSULTATION COUNT:\n"); 		

		double totalTreatmentCount = 0;
		double totalConsultationCount = 0; //added by Mike, 20181218
		
		for(int i=0; i<dateValuesArrayInt.length; i++) {
			writer.print(convertDateToMonthYearInWords(dateValuesArrayInt[i])+"\t");
			
			double treatmentCount = dateContainer.get(dateValuesArrayInt[i])[OUTPUT_HMO_COUNT_COLUMN] + dateContainer.get(dateValuesArrayInt[i])[OUTPUT_NON_HMO_COUNT_COLUMN];

			//added by Mike, 20181218
			double consultationCount = dateContainer.get(dateValuesArrayInt[i])[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN] + dateContainer.get(dateValuesArrayInt[i])[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN];

			
			totalTreatmentCount += treatmentCount;
			totalConsultationCount += consultationCount;
			
			writer.print(
							treatmentCount+"\t"+						
							consultationCount+"\n"							
						); 				   							
		}
		//TOTAL
		writer.print(
				"TOTAL:\t"+totalTreatmentCount+"\t"+totalConsultationCount+"\n"							
				); 				   							


		//--------------------------------------------------------------------
		//init table header names
		writer.print("\n\tTREATMENT COUNT:\tCONSULTATION COUNT:\n"); 		

		double totalTreatmentHMOCount = 0;
		double totalConsultationHMOCount = 0; //added by Mike, 20181219
		
		SortedSet<String> sortedKeyset = new TreeSet<String>(hmoContainer.keySet());

		for (String key : sortedKeyset) {	
			double treatmentCount = hmoContainer.get(key)[OUTPUT_HMO_COUNT_COLUMN];
			double consultationCount = hmoContainer.get(key)[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN];

			totalTreatmentHMOCount += treatmentCount;
			totalConsultationHMOCount += consultationCount;
			
			writer.print(
							key + "\t" + 
							treatmentCount+"\t"+							
							consultationCount+"\n"							
						); 				   							
		}

		//TOTAL
		writer.print(
				"TOTAL:\t"+totalTreatmentHMOCount+"\t"+totalConsultationHMOCount+"\n"							
				); 				   							


		//--------------------------------------------------------------------
		//init table header names
		writer.print("\n\tTREATMENT COUNT:\tCONSULTATION COUNT:\n"); 		

		double totalTreatmentNONHMOCount = 0;
		double totalConsultationNONHMOCount = 0; //added by Mike, 20181219
		
		SortedSet<String> sortedNONHMOKeyset = new TreeSet<String>(nonHmoContainer.keySet());

		for (String key : sortedNONHMOKeyset) {	
			double treatmentCount = nonHmoContainer.get(key)[OUTPUT_NON_HMO_COUNT_COLUMN];
			double consultationCount = nonHmoContainer.get(key)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN];

			totalTreatmentNONHMOCount += treatmentCount;
			totalConsultationNONHMOCount += consultationCount;
			
			writer.print(
							key + "\t" + 
							treatmentCount+"\t"+							
							consultationCount+"\n"							
						); 				   							
		}

		//TOTAL
		writer.print(
				"TOTAL:\t"+totalTreatmentNONHMOCount+"\t"+totalConsultationHMOCount+"\n"							
				); 				   							

		//--------------------------------------------------------------------
		//init table header names
		writer.print("\n\tTREATMENT COUNT:\tNET PATIENT REFERRAL COUNT:\tCONSULTATION COUNT:\tPROCEDURE COUNT:\n"); 

		double totalReferringMedicalDoctorTransactionCount = 0;
		double totalNewPatientReferralTransactionCount = 0;
		double totalConsultationPerDoctorCount = 0;
		double totalProcedurePerDoctorCount = 0;
		
		SortedSet<String> sortedReferringMedicalDoctorTransactionCountKeyset = new TreeSet<String>(referringDoctorContainer.keySet());

		for (String key : sortedReferringMedicalDoctorTransactionCountKeyset) {	
			double count = referringDoctorContainer.get(key)[OUTPUT_HMO_COUNT_COLUMN] + referringDoctorContainer.get(key)[OUTPUT_NON_HMO_COUNT_COLUMN];

			double newPatientReferralTransactionCount = referringDoctorContainer.get(key)[OUTPUT_HMO_NEW_OLD_COUNT_COLUMN] + referringDoctorContainer.get(key)[OUTPUT_NON_HMO_NEW_OLD_COUNT_COLUMN];

			//added by Mike, 20181219
			double consultationCount = referringDoctorContainer.get(key)[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN] + referringDoctorContainer.get(key)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN];

			//added by Mike, 20181219
			double procedureCount = referringDoctorContainer.get(key)[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN] + referringDoctorContainer.get(key)[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN];

			totalReferringMedicalDoctorTransactionCount += count;
			totalNewPatientReferralTransactionCount += newPatientReferralTransactionCount;
			totalConsultationPerDoctorCount += consultationCount;
			totalProcedurePerDoctorCount += procedureCount;
			
			writer.print(
							key + "\t" + 
							count+"\t" +
							newPatientReferralTransactionCount+"\t"+							
							consultationCount+"\t"+
							procedureCount+"\n"		
							); 				   							
		}

		//TOTAL
		writer.print(
				"TOTAL:\t"+totalReferringMedicalDoctorTransactionCount+"\t"+totalNewPatientReferralTransactionCount+"\t"+
				totalConsultationPerDoctorCount+"\t"+totalProcedurePerDoctorCount+"\n"							
				); 				   							
				
		writer.close();
	}
	
	private static String convertDateToMonthYearInWords(int date) {
		StringBuffer sb = new StringBuffer(""+date);	
		String year = sb.substring(0,4); //index 4 is not included
		int month = Integer.parseInt(sb.substring(4,6)); //index 6 is not included
		
		switch(month) {
			case 1:
				return "January" + " " + year;
			case 2:
				return "February" + " " + year;
			case 3:
				return "March" + " " + year;
			case 4:
				return "April" + " " + year;
			case 5:
				return "May" + " " + year;
			case 6:
				return "June" + " " + year;
			case 7:
				return "July" + " " + year;
			case 8:
				return "August" + " " + year;
			case 9:
				return "September" + " " + year;
			case 10:
				return "October" + " " + year;
			case 11:
				return "November" + " " + year;
			case 12:
				return "December" + " " + year;
		}	

		return null;//error
	}
	
	private static String getMonthYear(String date) {
		StringBuffer sb = new StringBuffer(date);				
		return sb.substring(0,3).concat("-").concat(sb.substring(sb.length()-2,sb.length()));
	}
	
	//added by Mike, 20181030
	private static void makeFilePath(String filePath) {
		File directory = new File(filePath);		
		if (!directory.exists() && !directory.mkdirs()) 
    	{
    		System.out.println("File Path to file could not be made.");
    	}    			
	}
	
	//added by Mike, 20181216
	private static void processMonthlyCount(HashMap<Integer, double[]> dateContainer, String[] inputColumns, int i, boolean isConsultation) {
		//				if (!referringDoctorContainer.containsKey(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])) {
				if (!dateContainer.containsKey(dateValuesArrayInt[i])) {
					columnValuesArray = new double[OUTPUT_TOTAL_COLUMNS];

					//edited by Mike, 20181218
					if (!isConsultation) {											
						//edited by Mike, 20181206
						if ((inputColumns[INPUT_CLASS_COLUMN].contains("HMO")) ||
							(inputColumns[INPUT_CLASS_COLUMN].contains("SLR"))) {

							columnValuesArray[OUTPUT_HMO_COUNT_COLUMN] = 1;
							columnValuesArray[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);

							if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
								columnValuesArray[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
							}
							else {
								columnValuesArray[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
							}
						}
						else {
							columnValuesArray[OUTPUT_NON_HMO_COUNT_COLUMN] = 1;
							columnValuesArray[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);

							if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
								columnValuesArray[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
							}
							else {
								columnValuesArray[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
							}
						}
					}
					else {												
						if ((inputColumns[INPUT_CLASS_COLUMN+INPUT_CONSULTATION_OFFSET].contains("HMO")) ||
							(inputColumns[INPUT_CLASS_COLUMN+INPUT_CONSULTATION_OFFSET].contains("SLR"))) {

							columnValuesArray[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN] = 1;
/*							columnValuesArray[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);

							if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
								columnValuesArray[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
							}
							else {
								columnValuesArray[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
							}
*/							
						}
						else {
							columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN] = 1;
/*							columnValuesArray[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);

							if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
								columnValuesArray[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
							}
							else {
								columnValuesArray[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
							}
*/							
						}						
					}
					
//					referringDoctorContainer.put(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN], columnValuesArray);
					dateContainer.put(dateValuesArrayInt[i], columnValuesArray);
				}
				else {
					//edited by Mike, 20181218
					if (!isConsultation) {											
						//edited by Mike, 20181206
						if ((inputColumns[INPUT_CLASS_COLUMN].contains("HMO")) ||
							(inputColumns[INPUT_CLASS_COLUMN].contains("SLR"))) {
							dateContainer.get(dateValuesArrayInt[i])[OUTPUT_HMO_COUNT_COLUMN]++;					
/*							dateContainer.get(dateValuesArrayInt[i])[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] 
								+= Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
								
							if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
								dateContainer.get(dateValuesArrayInt[i])[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
							}
							else {
								dateContainer.get(dateValuesArrayInt[i])[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);							
							}
*/							
						}
						else {
							dateContainer.get(dateValuesArrayInt[i])[OUTPUT_NON_HMO_COUNT_COLUMN]++;					
/*							dateContainer.get(dateValuesArrayInt[i])[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] 
								+= Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
								
							if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
								dateContainer.get(dateValuesArrayInt[i])[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
							}
							else {
								dateContainer.get(dateValuesArrayInt[i])[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);							
							}
*/							
						}
					}
					else {
						if ((inputColumns[INPUT_CLASS_COLUMN+INPUT_CONSULTATION_OFFSET].contains("HMO")) ||
							(inputColumns[INPUT_CLASS_COLUMN+INPUT_CONSULTATION_OFFSET].contains("SLR"))) {
							dateContainer.get(dateValuesArrayInt[i])[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN]++;					
/*							dateContainer.get(dateValuesArrayInt[i])[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] 
								+= Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
								
							if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
								dateContainer.get(dateValuesArrayInt[i])[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
							}
							else {
								dateContainer.get(dateValuesArrayInt[i])[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);							
							}
*/							
						}
						else {
							dateContainer.get(dateValuesArrayInt[i])[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]++;					
/*							dateContainer.get(dateValuesArrayInt[i])[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] 
								+= Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
								
							if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
								dateContainer.get(dateValuesArrayInt[i])[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
							}
							else {
								dateContainer.get(dateValuesArrayInt[i])[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);							
							}
*/							
						}
					}
				}					
	}

	//added by Mike, 20181217
	private static void processHMOCount(HashMap<String, double[]> hmoContainer, String[] inputColumns, boolean isConsultation) {
			//edited by Mike, 20181219
			if (!isConsultation) {											
				//edited by Mike, 20181206
				if ((inputColumns[INPUT_CLASS_COLUMN].contains("HMO")) ||
					(inputColumns[INPUT_CLASS_COLUMN].contains("SLR"))) {

					String hmoName = inputColumns[INPUT_CLASS_COLUMN];
					
					if (!hmoContainer.containsKey(hmoName)) {
						columnValuesArray = new double[OUTPUT_TOTAL_COLUMNS];
					
						columnValuesArray[OUTPUT_HMO_COUNT_COLUMN] = 1;
						columnValuesArray[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);

						if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
							columnValuesArray[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						}
						else {
							columnValuesArray[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						}
						
						hmoContainer.put(hmoName, columnValuesArray);
					}
					else {
						hmoContainer.get(hmoName)[OUTPUT_HMO_COUNT_COLUMN]++;					
						hmoContainer.get(hmoName)[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] 
							+= Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
							
						if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
							hmoContainer.get(hmoName)[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						}
						else {
							hmoContainer.get(hmoName)[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);							
						}		
					}
				}				
			}
			else {																	
				if ((inputColumns[INPUT_CLASS_COLUMN+INPUT_CONSULTATION_OFFSET].contains("HMO")) ||
					(inputColumns[INPUT_CLASS_COLUMN+INPUT_CONSULTATION_OFFSET].contains("SLR"))) {

					String hmoName = inputColumns[INPUT_CLASS_COLUMN+INPUT_CONSULTATION_OFFSET];
					
					if (!hmoContainer.containsKey(hmoName)) {
						columnValuesArray = new double[OUTPUT_TOTAL_COLUMNS];					
						columnValuesArray[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN] = 1;						
						hmoContainer.put(hmoName, columnValuesArray);
					}
					else {
						hmoContainer.get(hmoName)[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN]++;					
					}
				}				
			}
	}	
	
	//added by Mike, 20181217
	private static void processNONHMOCount(HashMap<String, double[]> nonHmoContainer, String[] inputColumns, boolean isConsultation) {
		//edited by Mike, 20181219
		if (!isConsultation) {											
			//edited by Mike, 20181206
			if ((!inputColumns[INPUT_CLASS_COLUMN].contains("HMO")) &&
				(!inputColumns[INPUT_CLASS_COLUMN].contains("SLR"))) {

				String nonHmoName = inputColumns[INPUT_CLASS_COLUMN];
				
				if (!nonHmoContainer.containsKey(nonHmoName)) {
					columnValuesArray = new double[OUTPUT_TOTAL_COLUMNS];
				
					columnValuesArray[OUTPUT_NON_HMO_COUNT_COLUMN] = 1;
					columnValuesArray[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);

					if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
						columnValuesArray[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
					}
					else {
						columnValuesArray[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
					}
					
					nonHmoContainer.put(nonHmoName, columnValuesArray);
				}
				else {
					nonHmoContainer.get(nonHmoName)[OUTPUT_NON_HMO_COUNT_COLUMN]++;					
					nonHmoContainer.get(nonHmoName)[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] 
						+= Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						
					if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
						nonHmoContainer.get(nonHmoName)[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
					}
					else {
						nonHmoContainer.get(nonHmoName)[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);							
					}		
				}
			}			
		}
		else {			
			if ((!inputColumns[INPUT_CLASS_COLUMN+INPUT_CONSULTATION_OFFSET].contains("HMO")) &&
				(!inputColumns[INPUT_CLASS_COLUMN+INPUT_CONSULTATION_OFFSET].contains("SLR"))) {

				String nonHmoName = inputColumns[INPUT_CLASS_COLUMN+INPUT_CONSULTATION_OFFSET];
				
				if (!nonHmoContainer.containsKey(nonHmoName)) {
					columnValuesArray = new double[OUTPUT_TOTAL_COLUMNS];				
					columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN] = 1;					
					nonHmoContainer.put(nonHmoName, columnValuesArray);
				}
				else {
					nonHmoContainer.get(nonHmoName)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]++;
				}
			}
		}
	}	
	
	//added by Mike, 20181218
	private static void processReferringDoctorTransactionCount(HashMap<String, double[]> referringDoctorContainer, String[] inputColumns, Boolean isConsultation) {
		//edited by Mike, 20181219
		if (!isConsultation) {											
			if (!referringDoctorContainer.containsKey(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])) {
				columnValuesArray = new double[OUTPUT_TOTAL_COLUMNS];
				
				if (inputColumns[INPUT_CLASS_COLUMN].contains("HMO")) {
					columnValuesArray[OUTPUT_HMO_COUNT_COLUMN] = 1;
					columnValuesArray[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);

					if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
						columnValuesArray[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
					}
					else {
						columnValuesArray[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
					}

					if (inputColumns[INPUT_NEW_OLD_COLUMN].toLowerCase().contains("new")) {
						//added by Mike, 20181218
						columnValuesArray[OUTPUT_HMO_NEW_OLD_COUNT_COLUMN] = 1;
					}							
				}
				else {
					columnValuesArray[OUTPUT_NON_HMO_COUNT_COLUMN] = 1;
					columnValuesArray[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);

					if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
						columnValuesArray[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
					}
					else {
						columnValuesArray[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
					}
					
					if (inputColumns[INPUT_NEW_OLD_COLUMN].toLowerCase().contains("new")) {
						//added by Mike, 20181218
						columnValuesArray[OUTPUT_NON_HMO_NEW_OLD_COUNT_COLUMN] = 1;
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
						referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
					}
					else {
						referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
					}
					
					if (inputColumns[INPUT_NEW_OLD_COLUMN].toLowerCase().contains("new")) {
						//added by Mike, 20181218
						referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])[OUTPUT_HMO_NEW_OLD_COUNT_COLUMN]++;					
					}							
				}
				else {
					referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])[OUTPUT_NON_HMO_COUNT_COLUMN]++;					
					referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] 
						+= Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						
					if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
						referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
					}
					else {
						referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
					}
					
					if (inputColumns[INPUT_NEW_OLD_COLUMN].toLowerCase().contains("new")) {
						//added by Mike, 20181218
						referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])[OUTPUT_NON_HMO_NEW_OLD_COUNT_COLUMN]++;					
					}
				}
			}
		}
		else {
			if (!referringDoctorContainer.containsKey(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN+INPUT_CONSULTATION_OFFSET])) {
				columnValuesArray = new double[OUTPUT_TOTAL_COLUMNS];
				
				if (inputColumns[INPUT_CLASS_COLUMN+INPUT_CONSULTATION_OFFSET].contains("HMO")) {
					columnValuesArray[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN] = 1;
				}
				else {
					columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN] = 1;
				}
				
				//added by Mike, 20181219
				if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().contains("p")) {
					columnValuesArray[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN] = 1;
				}
				else {
					columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN] = 1;
				}
				
				referringDoctorContainer.put(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN+INPUT_CONSULTATION_OFFSET], columnValuesArray);
			}
			else {													
				if (inputColumns[INPUT_CLASS_COLUMN+INPUT_CONSULTATION_OFFSET].contains("HMO")) {
					referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN+INPUT_CONSULTATION_OFFSET])[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN]++;				
/*
					if (inputColumns[INPUT_NEW_OLD_COLUMN+INPUT_CONSULTATION_OFFSET].toLowerCase().contains("new")) {
						//added by Mike, 20181218
						referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN+INPUT_CONSULTATION_OFFSET])[OUTPUT_CONSULTATION_HMO_NEW_OLD_COUNT_COLUMN]++;					
					}												
*/					
					//added by Mike, 20181219
					if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().contains("p")) {
						columnValuesArray[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN]++;
					}
				}
				else {
					referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN+INPUT_CONSULTATION_OFFSET])[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]++;					
/*					
					if (inputColumns[INPUT_NEW_OLD_COLUMN+INPUT_CONSULTATION_OFFSET].toLowerCase().contains("new")) {
						//added by Mike, 20181218
						referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN+INPUT_CONSULTATION_OFFSET])[OUTPUT_CONSULTATION_NON_HMO_NEW_OLD_COUNT_COLUMN]++;					
					}
*/					
					//added by Mike, 20181219
					if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().contains("p")) {
						columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN]++;
					}
				}
			}
		}
	}

}