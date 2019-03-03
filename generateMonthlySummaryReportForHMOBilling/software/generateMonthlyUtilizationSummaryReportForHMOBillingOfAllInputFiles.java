/*
 * Copyright 2018~2019 Usbong Social Systems, Inc.
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
//import commons-lang3-3.8.1;
//import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;

/*
' Given:
' 1) Encoding for the Month Input Worksheet
' --> Saved/Exported as "Tab delimited" .txt file from Excel
' --> Example: input_201808.txt (where the date format is YYYYMM; based on ISO 8601)
'
' Output:
' 1) Auto-generated Monthly Summary Report For HMO Billing using Sta. Lucia Health Care Centre's Billing Template
' --> "Tab delimited" .txt file for each HMO
'
' Notes:
' 1) To execute the add-on software/application simply use the following command:
'   java generateMonthlyUtilizationSummaryReportForHMOBillingOfAllInputFiles input_201801.txt
' 
' where: "input_201801.txt" is the name of the file.
' 
' 2) To execute a set of input files, e.g. input201801.txt, input201802.txt, you can use the following command: 
'   java generateMonthlyUtilizationSummaryReportForHMOBillingOfAllInputFiles input*
' 
' where: "input*" means any file in the directory that starts with "input".
'
' 3) Make sure to include "Consultation" in the input file name.
' --> This is so that the add-on software would be able to properly identify it as a set of "Consultation" transactions, instead of those of "Treatment".
' --> Example: inputConsultation201801.txt
'
' 4) If you use space in your file name, e.g. "input Consultation 201801.txt", you will have to execute the input files as follows.
'   java generateMonthlyUtilizationSummaryReportForHMOBillingOfAllInputFiles *"2018"*.txt
'
' where: * means any set of characters
'
' 5) To compile on Windows' Command Prompt the add-on software with the Apache Commons Text .jar file, i.e. org.apache.commons.text, use the following command:
'   javac -cp .;org.apache.commons.text.jar generateMonthlyUtilizationSummaryReportForHMOBillingOfAllInputFiles.java
'
' 6) To execute on Windows' Command Prompt the add-on software with the Apache Commons Text .jar file, i.e. org.apache.commons.text, use the following command:
'   java -cp .;org.apache.commons.text.jar generateMonthlyUtilizationSummaryReportForHMOBillingOfAllInputFiles *.txt
'
' 7) The Apache Commons Text binaries with the .jar file can be downloaded here:
'   http://commons.apache.org/proper/commons-text/download_text.cgi; last accessed: 20190123
'
' 8) The documentation for the LevenshteinDistance can be viewed here:
'   https://commons.apache.org/proper/commons-text/javadocs/api-release/org/apache/commons/text/similarity/LevenshteinDistance.html; last accessed: 20190123
*/ 

public class generateMonthlyUtilizationSummaryReportForHMOBillingOfAllInputFiles {	
	private static boolean isInDebugMode = true; //edited by Mike, 20190131
	private static boolean isNetPFComputed = false; //added by Mike, 20190131

	private static String inputFilename = "input201801"; //without extension; default input file
	
	private static String startDate = null;
	private static String endDate = null;
	
	//added by Mike, 20190127
	private static final int HMO_CONTAINER_TYPE = 0;
	private static final int NON_HMO_CONTAINER_TYPE = 1;	
	private static final int REFERRING_DOCTOR_CONTAINER_TYPE = 2;	
	private static final int HMO_CLASSIFICATION_CONTAINER_PER_MEDICAL_DOCTOR_CONTAINER_TYPE = 3;	
	private static final int NON_HMO_CLASSIFICATION_CONTAINER_PER_MEDICAL_DOCTOR_CONTAINER_TYPE = 4;	
	
	//added by Mike, 20190131
	private static final int INPUT_NON_MASTER_LIST_OFFSET = 1; 
	
	private static final int INPUT_REFERRING_DOCTOR_COLUMN = 15-INPUT_NON_MASTER_LIST_OFFSET;
	private static final int INPUT_NOTES_COLUMN = 0; //This column is not included in the INPUT_NON_MASTER_LIST_OFFSET
	private static final int INPUT_DATE_COLUMN = 1-INPUT_NON_MASTER_LIST_OFFSET;
	private static final int INPUT_PATIENT_NAME_COLUMN = 3-INPUT_NON_MASTER_LIST_OFFSET;
	private static final int INPUT_CLASS_COLUMN = 8-INPUT_NON_MASTER_LIST_OFFSET; //HMO and NON-HMO
	private static final int INPUT_NET_PF_COLUMN = 10-INPUT_NON_MASTER_LIST_OFFSET;
	private static final int INPUT_NEW_OLD_COLUMN = 16-INPUT_NON_MASTER_LIST_OFFSET;
	private static final int INPUT_NEW_OLD_PATIENT_COLUMN = 16-INPUT_NON_MASTER_LIST_OFFSET; //added by Mike, 20190102
	private static final int INPUT_CHARGE_SLIP_NUMBER_COLUMN = 11-INPUT_NON_MASTER_LIST_OFFSET; //added by Mike, 20190226
	private static final int INPUT_APPROVAL_CODE_COLUMN = 12-INPUT_NON_MASTER_LIST_OFFSET; //added by Mike, 20190226
	private static final int INPUT_FEE_COLUMN = 7-INPUT_NON_MASTER_LIST_OFFSET; //added by Mike, 20190226
	private static final int INPUT_DIAGNOSIS_COLUMN = 6-INPUT_NON_MASTER_LIST_OFFSET; //added by Mike, 20190303

	//added by Mike, 20190226
	private static final String OUTPUT_HMO_BILLING_DEPARTMENT_PT_VALUE = "PT";
	private static final String OUTPUT_HMO_BILLING_DEPARTMENT_ORTHO_VALUE = "ORTHO";
	private static final String OUTPUT_HMO_BILLING_DEPARTMENT_REHAB_VALUE = "REHAB";

	private static final String OUTPUT_HMO_BILLING_PROCEDURE_PT_VALUE = "PT TREATMENT";
	private static final String OUTPUT_HMO_BILLING_PROCEDURE_ORTHO_VALUE = "ORTHO CONSULT";
	private static final String OUTPUT_HMO_BILLING_PROCEDURE_REHAB_VALUE = "REHAB CONSULT";
	private static final String OUTPUT_HMO_BILLING_PROCEDURE_ORTHO_PROCEDURE_VALUE = "ORTHO-PROCEDURE";
	private static final String OUTPUT_HMO_BILLING_PROCEDURE_REHAB_PROCEDURE_VALUE = "REHAB-PROCEDURE";

	private static final String OUTPUT_HMO_BILLING_PAYEE_VALUE = "STA. LUCIA HEALTH CARE CENTRE, INC.";
	
	//edited by Mike, 20190202
	private static final int INPUT_CONSULTATION_PROCEDURE_COLUMN = 2-INPUT_NON_MASTER_LIST_OFFSET;
	private static final int INPUT_CONSULTATION_MEDICAL_DOCTOR_COLUMN = 16-INPUT_NON_MASTER_LIST_OFFSET;

	//added by Mike, 20190107
	private static final int INPUT_CONSULTATION_MEDICAL_CERTIFICATE_COLUMN = 2-INPUT_NON_MASTER_LIST_OFFSET; //The int value is the same as "INPUT_CONSULTATION_PROCEDURE_COLUMN".

	//added by Mike, 20181218
	//CONSULTATION
	private static final int INPUT_CONSULTATION_PROCEDURE_DETAILS_COLUMN = 12-INPUT_NON_MASTER_LIST_OFFSET;
	private static final int INPUT_CONSULTATION_APPROVAL_CODE_COLUMN = 15-INPUT_NON_MASTER_LIST_OFFSET;
	private static final int INPUT_CONSULTATION_DIAGNOSIS_COLUMN = 7-INPUT_NON_MASTER_LIST_OFFSET; //added by Mike, 20190303
	
/*	
	private static final int INPUT_CONSULTATION_CLASS_COLUMN = 9;
	private static final int INPUT_CONSULTATION_NET_PF_COLUMN = 11;
	private static final int INPUT_CONSULTATION_NEW_OLD_COLUMN = 17;
*/	
	private static final int INPUT_CONSULTATION_OFFSET = 1;
	
	private static int hmoBillingContainerTransactionCount = 0; //added by Mike, 20190227	
	
	//TO-DO: -update: the keyword accordingly
	private static final String HMO_BILLING_REHAB_CLASSIFICATION_KEYWORD_VALUE = "REYES"; //added by Mike, 20190228	
						
/*	private static HashMap<String, double[]> referringDoctorContainer;	
*/
	private static HashMap<Integer, double[]> dateContainer;	//added by Mike, 201801205
	private static HashMap<String, double[]> hmoContainer;	//added by Mike, 201801217
	private static HashMap<Integer, String[]> hmoBillingContainer;	//added by Mike, 20190226
	private static HashMap<String, double[]> nonHmoContainer;	//added by Mike, 201801217
	private static HashMap<String, double[]> referringDoctorContainer; //added by Mike, 20181218
	private static HashMap<String, double[]> medicalDoctorContainer; //added by Mike, 20190202
	private static HashMap<String, PrintWriter> hmoPrintWriterContainer; //added by Mike, 20190228
	private static ArrayList<String> hmoBillingTableHeaderArrayList; //added by Mike, 20190301
	private static ArrayList<String> hmoUtilizationTableHeaderArrayList; //added by Mike, 20190303
	
	private static double[] columnValuesArray;
	private static String[] hmoBillingColumnValuesArray;
	private static String[] dateValuesArray; //added by Mike, 20180412
	private static int[] dateValuesArrayInt; //added by Mike, 20181206
	//private static ArrayList<int> dateValuesArrayInt; //edited by Mike, 20181221
		
	//the date and the referring doctor are not yet included here
	//this is for both HMO and NON-HMO transactions
	private static final int OUTPUT_TOTAL_COLUMNS = 25; //edited by Mike, 20190202

	private static final int OUTPUT_HMO_BILLING_TOTAL_COLUMNS = 17; //added by Mike, 20190227

	private static final int OUTPUT_HMO_UTILIZATION_SUMMARY_TOTAL_COLUMNS = 6; //added by Mike, 20190303	

	//added by Mike, 20190303
	private static final int OUTPUT_HMO_UTILIZATION_DATE_COLUMN = 0;
	private static final int OUTPUT_HMO_UTILIZATION_PATIENT_NAME_COLUMN = 1;
	private static final int OUTPUT_HMO_UTILIZATION_HMO_NAME_COLUMN = 2;
	private static final int OUTPUT_HMO_UTILIZATION_DIAGNOSIS_COLUMN = 3;
	private static final int OUTPUT_HMO_UTILIZATION_MD_SIGNATURE_COLUMN = 4;
	private static final int OUTPUT_HMO_UTILIZATION_RECEIVED_BY_DATE_COLUMN = 5;
	
	//PT TREATMENT
	//added by Mike, 20190226
	private static final int OUTPUT_HMO_BILLING_DATE_COLUMN = 0;
	private static final int OUTPUT_HMO_BILLING_HMO_NAME_COLUMN = 1;
	private static final int OUTPUT_HMO_BILLING_NUMBER_COLUMN = 2; //for use as table header only
	private static final int OUTPUT_HMO_BILLING_FORM_COLUMN = 3; //for use as table header only
	private static final int OUTPUT_HMO_BILLING_CHARGE_SLIP_NUMBER_COLUMN = 4;
	private static final int OUTPUT_HMO_BILLING_PATIENT_NAME_COLUMN = 5;
	private static final int OUTPUT_HMO_BILLING_APPROVAL_CODE_COLUMN = 6; //approval code/account number
	private static final int OUTPUT_HMO_BILLING_MEDICAL_DOCTOR_NAME_COLUMN = 7; 
	private static final int OUTPUT_HMO_BILLING_PAYEE_COLUMN = 8; 
	private static final int OUTPUT_HMO_BILLING_DIAGNOSIS_COLUMN = 9; //for use as table header only
	private static final int OUTPUT_HMO_BILLING_DEPARTMENT_COLUMN = 10; 
	private static final int OUTPUT_HMO_BILLING_PROCEDURE_COLUMN = 11; 
	private static final int OUTPUT_HMO_BILLING_DOCTORS_COLUMN = 12; //for use as table header only
	private static final int OUTPUT_HMO_BILLING_VAT_EXEMPT_COLUMN = 13; //for use as table header only
	private static final int OUTPUT_HMO_BILLING_VATABLE_SALES_COLUMN = 14; //for use as table header only
	private static final int OUTPUT_HMO_BILLING_VAT_COLUMN = 15; //for use as table header only
	private static final int OUTPUT_HMO_BILLING_TOTAL_AMOUNT_COLUMN = 16; 
			
	private static final int OUTPUT_HMO_COUNT_COLUMN = 0; //transaction count
	private static final int OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN = 1;
	private static final int OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN = 2;
	private static final int OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN = 3;
	private static final int OUTPUT_HMO_NEW_PATIENT_COUNT_COLUMN = 4;
	private static final int OUTPUT_HMO_OLD_PATIENT_COUNT_COLUMN = 5; //added by Mike, 20190102

	private static final int OUTPUT_NON_HMO_COUNT_COLUMN = 6; //transaction count
	private static final int OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN = 7;
	private static final int OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN = 8;
	private static final int OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN = 9;
	private static final int OUTPUT_NON_HMO_NEW_PATIENT_COUNT_COLUMN = 10;	
	private static final int OUTPUT_NON_HMO_OLD_PATIENT_COUNT_COLUMN = 11; //added by Mike, 20190102

	private static final int OUTPUT_DATE_ID_COLUMN = 12; //added by Mike, 20181205
	
	//CONSULTATION
	private static final int OUTPUT_CONSULTATION_HMO_COUNT_COLUMN = 13; //transaction count
	private static final int OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN = 14; //transaction count
	private static final int OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN = 15; //transaction count
	private static final int OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN = 16; //transaction count
	private static final int OUTPUT_CONSULTATION_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN = 17; //transaction count; added by Mike, 20190107
	private static final int OUTPUT_CONSULTATION_NON_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN = 18; //transaction count; added by Mike, 20190107
	private static final int OUTPUT_CONSULTATION_HMO_NEW_PATIENT_COUNT_COLUMN = 19; //transaction count; added by Mike, 20190107
	private static final int OUTPUT_CONSULTATION_NON_HMO_NEW_PATIENT_COUNT_COLUMN = 20; //transaction count; added by Mike, 20190107
	private static final int OUTPUT_CONSULTATION_HMO_OLD_PATIENT_COUNT_COLUMN = 21; //added by Mike, 20190202
	private static final int OUTPUT_CONSULTATION_NON_HMO_OLD_PATIENT_COUNT_COLUMN = 22; //added by Mike, 20190202
	private static final int OUTPUT_CONSULTATION_HMO_FOLLOW_UP_COUNT_COLUMN = 23; //added by Mike, 20190202
	private static final int OUTPUT_CONSULTATION_NON_HMO_FOLLOW_UP_COUNT_COLUMN = 24; //added by Mike, 20190202

	private static boolean isConsultation;
	
	private static DecimalFormat df = new DecimalFormat("0.00"); //added by Mike, 20181105
	private static int rowCount; //added by Mike, 20181105
				
	private static int totalCountForAllReferringDoctors;
	private static double totalNetTreatmentFeeForAllReferringDoctors;
	private static double totalPaidNetTreatmentFeeForAllReferringDoctors;
	private static double totalUnpaidNetTreatmentFeeForAllReferringDoctors;
	private static double totalFivePercentShareOfNetPaidForAllReferringDoctors;
				
	//added by Mike, 20181220
	private static HashMap<String, HashMap<String, double[]>> classificationContainerPerMedicalDoctor = new HashMap<String, HashMap<String, double[]>>();
	private static HashMap<String, double[]> classificationContainerHashmap = new HashMap<String, double[]>();
	private static double[] classificationContainerColumnValuesArray = new double[OUTPUT_TOTAL_COLUMNS];
	private static boolean hasSetClassificationContainerPerMedicalDoctor=false;
	
	//added by Mike, 20190126
	private static LevenshteinDistance myLevenshteinDistance;
	
	public static void main ( String[] args ) throws Exception
	{			
		makeFilePath("output"); //"output" is the folder where I've instructed the add-on software/application to store the output file			
/*		PrintWriter writer = new PrintWriter("output/MonthlySummaryReportOutput.txt", "UTF-8");			
*/
		/*referringDoctorContainer = new HashMap<String, double[]>();
		*/
		
		dateContainer = new HashMap<Integer, double[]>();
		hmoContainer = new HashMap<String, double[]>();
		hmoBillingContainer = new HashMap<Integer, String[]>(); //added by Mike, 20190226
		nonHmoContainer = new HashMap<String, double[]>();
		referringDoctorContainer = new HashMap<String, double[]>();
//		medicalDoctorContainer = new HashMap<String, double[]>();
		classificationContainerPerMedicalDoctor = new HashMap<String, HashMap<String, double[]>>();				
		medicalDoctorContainer = new HashMap<String, double[]>(); //added by Mike, 20190202				
		hmoPrintWriterContainer = new HashMap<String, PrintWriter>(); //added by Mike, 20190228				
/*		
		hmoBillingTableHeaderArrayList = new ArrayList<String>();
		hmoBillingTableHeaderArrayList.add("DATE"); //OUTPUT_HMO_BILLING_DATE_COLUMN		
		hmoBillingTableHeaderArrayList.add("HMO NAME"); //OUTPUT_HMO_BILLING_HMO_NAME_COLUMN		
		hmoBillingTableHeaderArrayList.add("NO."); //OUTPUT_HMO_BILLING_NUMBER_COLUMN		
		hmoBillingTableHeaderArrayList.add("FORM"); //OUTPUT_HMO_BILLING_FORM_COLUMN		
		hmoBillingTableHeaderArrayList.add("CHARGE SLIP NO."); //OUTPUT_HMO_BILLING_CHARGE_SLIP_NUMBER_COLUMN		
		hmoBillingTableHeaderArrayList.add("PATIENT'S NAME"); //OUTPUT_HMO_BILLING_PATIENT_NAME_COLUMN		
		hmoBillingTableHeaderArrayList.add("ACCOUNT#/APPROVAL CODE"); //OUTPUT_HMO_BILLING_APPROVAL_CODE_COLUMN		
		hmoBillingTableHeaderArrayList.add("DOCTOR'S NAME"); //OUTPUT_HMO_BILLING_MEDICAL_DOCTOR_NAME_COLUMN		
		hmoBillingTableHeaderArrayList.add("PAYEE"); //OUTPUT_HMO_BILLING_PAYEE_COLUMN
		hmoBillingTableHeaderArrayList.add("DIAGNOSIS"); //OUTPUT_HMO_BILLING_DIAGNOSIS_COLUMN
		hmoBillingTableHeaderArrayList.add("DEPT"); //OUTPUT_HMO_BILLING_DEPARTMENT_COLUMN
		hmoBillingTableHeaderArrayList.add("PROCEDURE"); //OUTPUT_HMO_BILLING_PROCEDURE_COLUMN
		hmoBillingTableHeaderArrayList.add("DOCTORS"); //OUTPUT_HMO_BILLING_DOCTORS_COLUMN
		hmoBillingTableHeaderArrayList.add("VAT EXEMPT"); //OUTPUT_HMO_BILLING_VAT_EXEMPT_COLUMN
		hmoBillingTableHeaderArrayList.add("VATABLE SALES"); //OUTPUT_HMO_BILLING_VATABLE_SALES_COLUMN
		hmoBillingTableHeaderArrayList.add("VAT"); //OUTPUT_HMO_BILLING_VAT_COLUMN
		hmoBillingTableHeaderArrayList.add("TOTAL AMOUNT"); //OUTPUT_HMO_BILLING_TOTAL_AMOUNT_COLUMN
*/
		
		hmoUtilizationTableHeaderArrayList = new ArrayList<String>();
		hmoUtilizationTableHeaderArrayList.add("DATE"); 
		hmoUtilizationTableHeaderArrayList.add("PATIENT'S NAME");
		hmoUtilizationTableHeaderArrayList.add("HMO NAME");
		hmoUtilizationTableHeaderArrayList.add("DIAGNOSIS");
		hmoUtilizationTableHeaderArrayList.add("MD'S SIGNATURE");		
		hmoUtilizationTableHeaderArrayList.add("RECEIVED BY/DATE");		
				
		//added by Mike, 20181116
		startDate = null; //properly set the month and year in the output file of each input file
		dateValuesArray = new String[args.length]; //added by Mike, 20180412
		dateValuesArrayInt = new int[args.length]; //added by Mike, 20180412
		//dateValuesArrayInt = new ArrayList<int>(); //edited by Mike, 20181221

		//PART/COMPONENT/MODULE/PHASE 1
		processInputFiles(args, true);

		//PART/COMPONENT/MODULE/PHASE 2		
/*		setClassificationContainerPerMedicalDoctor(classificationContainerPerMedicalDoctor);
		processInputFiles(args, false);
*/				
				
		//added by Mike, 20190125
/*		processContainers();
*/
		
/*		
		//TODO: -apply: this properly in the add-on software to consolidate similar Strings, e.g. Medical Doctor, whose difference may only be an excess space between characters, etc
		//added by Mike, 20190123
		LevenshteinDistance myLevenshteinDistance = new LevenshteinDistance();
		
		System.out.println(">>> Compare the Difference between Strings!");		
		System.out.println(myLevenshteinDistance.apply("1234567890", "1")); //answer: 9
		System.out.println(myLevenshteinDistance.apply("123", "123")); //answer: 0
		System.out.println(myLevenshteinDistance.apply("123", "132")); //answer: 2
		System.out.println(myLevenshteinDistance.apply("132", "1 32")); //answer: 1
*/		
		/*
		 * --------------------------------------------------------------------
		 * OUTPUT
		 * --------------------------------------------------------------------
		*/
		//--------------------------------------------------------------------
		//init table header names
//		writer.print("\n\tTREATMENT COUNT:\tCONSULTATION COUNT:\tPROCEDURE COUNT:\tMEDICAL CERTIFICATE COUNT:\tTREATMENT NEW PATIENT COUNT:\tCONSULTATION NEW PATIENT COUNT:\n"); 		
		
		//added by Mike, 20190227
		SortedSet<Integer> sortedKeyset = new TreeSet<Integer>(hmoBillingContainer.keySet());		
		SortedSet<String> sortedHmoPrintWriterKeyset = new TreeSet<String>(hmoPrintWriterContainer.keySet());		

		String hmoNameKey = "";
		
		//added by Mike, 20190301
		for (String key : sortedHmoPrintWriterKeyset) {					
			//added by Mike, 20190301
			//write the top portion above the table header			
			//write an additional Tab, i.e. "\t", at the end of each row
			//this is so that the present add-on software as MS Excel Macro can properly copy and paste all the columns with written data
			hmoPrintWriterContainer.get(key).println(
											addTabUntilColumn(OUTPUT_HMO_UTILIZATION_DIAGNOSIS_COLUMN)+
											"STA. LUCIA HEALTH CARE CENTRE, INC."+"\t"
									   ); 				   											
			hmoPrintWriterContainer.get(key).println(
											addTabUntilColumn(OUTPUT_HMO_UTILIZATION_DIAGNOSIS_COLUMN)+
											"HMO UTILIZATION SUMMARY"+"\t"
									   ); 				   											
			
			for(int k=0; k<hmoUtilizationTableHeaderArrayList.size(); k++) {
				hmoPrintWriterContainer.get(key).print(								
											hmoUtilizationTableHeaderArrayList.get(k)+"\t"
									   ); 				   											
			}
				
			hmoPrintWriterContainer.get(key).println(); 				   											
		}

		for (Integer key : sortedKeyset) {			
			hmoNameKey = hmoBillingContainer.get(key)[OUTPUT_HMO_BILLING_HMO_NAME_COLUMN];

			//edited by Mike, 20190303	
			if (hmoPrintWriterContainer.containsKey(hmoNameKey)) {				
//				for (int i=0; i<OUTPUT_HMO_UTILIZATION_SUMMARY_TOTAL_COLUMNS; i++) {
					hmoPrintWriterContainer.get(hmoNameKey).print(								
													getDateAsHMOBillingFormat(
														hmoBillingContainer.get(key)[OUTPUT_HMO_BILLING_DATE_COLUMN]
														)+"\t"
												); 				   											

					hmoPrintWriterContainer.get(hmoNameKey).print(								
													hmoBillingContainer.get(key)[OUTPUT_HMO_BILLING_PATIENT_NAME_COLUMN]+"\t"
												); 				   											

					hmoPrintWriterContainer.get(hmoNameKey).print(								
													hmoBillingContainer.get(key)[OUTPUT_HMO_BILLING_HMO_NAME_COLUMN]+"\t"
												); 				   											

					hmoPrintWriterContainer.get(hmoNameKey).print(								
													hmoBillingContainer.get(key)[OUTPUT_HMO_BILLING_DIAGNOSIS_COLUMN]+"\t"
												); 				   											

					//MD'S SIGNATURE
					hmoPrintWriterContainer.get(hmoNameKey).print(								
													"\t"
												); 				   											

					//RECEIVED BY/DATE
					hmoPrintWriterContainer.get(hmoNameKey).print(								
													"\t"
												); 				   											
												
/*
					if (outputValue==null) {
						outputValue="";
					}
*/


/*					if (i==OUTPUT_HMO_BILLING_TOTAL_AMOUNT_COLUMN) {
*/	
/*						outputValue = outputValue.replace("\"","");
*/
/*					}
*/					
//				}
				hmoPrintWriterContainer.get(hmoNameKey).println();
			}
		}
				
		for (Integer key : sortedKeyset) {			
			hmoNameKey = hmoBillingContainer.get(key)[OUTPUT_HMO_BILLING_HMO_NAME_COLUMN];
			hmoPrintWriterContainer.get(hmoNameKey).close();
		}
/*	
		writer.print("\n");		
		writer.close();
*/		
	}
	
	private static String addTabUntilColumn(int column) {
		StringBuffer output = new StringBuffer("");
		
		for (int i=0; i<column; i++) {
			output.append("\t");
		}
		
		return output.toString();
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

	//input: Jan
	//output: 1
	private static String convertMonthToNumber(String month) {
		switch(month) {
			case "jan":
				return "01";
			case "feb":
				return "02"; 
			case "mar":
				return "03";
			case "apr":
				return "04";
			case "may":
				return "05";
			case "jun":
				return "06";
			case "jul":
				return "07";
			case "aug":
				return "08";
			case "sep":
				return "09";
			case "oct":
				return "10";
			case "nov":
				return "11";
			case "dec":
				return "12";
		}	
		return null;
	}
	
	//input: Jan-19
	//output: 201901
	private static int getYearMonthAsInt(String date) {
		StringBuffer sb = new StringBuffer(""+date);	
		String month = sb.substring(0,sb.indexOf("-")).toLowerCase(); //index "-" is not included
		month = ""+convertMonthToNumber(month);

		String year = sb.substring(sb.indexOf("-")).substring(sb.indexOf("-")+1);

//		System.out.println("year: "+year);

		//if the year is only 2 digits, e.g. "19", instead of of "2019"
		if (year.length() < 4) {
			year = "20" + year;
		}
		

		System.out.println("Integer.parseInt(year.concat(month)): "+Integer.parseInt(year.concat(month)));
		return Integer.parseInt(year.concat(month));
	}
	
	//added by Mike, 20190227
	//input: JAN-05-19
	//output: 05/01/2019
	private static String getDateAsHMOBillingFormat(String date) {
		StringBuffer sb = new StringBuffer(""+date);	
		String month = sb.substring(0,sb.indexOf("-")).toLowerCase(); //index "-" is not included
		month = ""+convertMonthToNumber(month);

		String day = date.split("-")[1];
		String year = sb.substring(sb.indexOf("-")).substring(sb.indexOf("-")+1);

//		System.out.println("year: "+year);

		//if the year is only 2 digits, e.g. "19", instead of of "2019"
		if (year.length() < 4) {
			year = "20" + year;
		}
		

		return day.concat("/").concat(month).concat("/").concat(year);
	}
	
	//added by Mike, 20181030
	private static void makeFilePath(String filePath) {
		File directory = new File(filePath);		
		if (!directory.exists() && !directory.mkdirs()) 
    	{
    		System.out.println("File Path to file could not be made.");
    	}    			
	}
	
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
/*
							if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
								columnValuesArray[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
							}
							else {
								columnValuesArray[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
							}
*/							
						}
						else {
							columnValuesArray[OUTPUT_NON_HMO_COUNT_COLUMN] = 1;
							columnValuesArray[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
/*
							if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
								columnValuesArray[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
							}
							else {
								columnValuesArray[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
							}
*/							
						}
					}
					else {												
						if ((inputColumns[INPUT_CLASS_COLUMN+INPUT_CONSULTATION_OFFSET].contains("HMO")) ||
							(inputColumns[INPUT_CLASS_COLUMN+INPUT_CONSULTATION_OFFSET].contains("SLR"))) {

							//edited by Mike, 20190107
							if (inputColumns[INPUT_CONSULTATION_MEDICAL_CERTIFICATE_COLUMN].toLowerCase().trim().contains("mc")) {
								columnValuesArray[OUTPUT_CONSULTATION_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN] = 1;
							}
							else if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().trim().contains("p")) {
								//edited by Mike, 20190108
								if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().trim().contains("/")) {
									columnValuesArray[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN] = 1;
									columnValuesArray[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN] = 1;
								}
								else {
									columnValuesArray[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN] = 1;
								}
							}	
							else {
								columnValuesArray[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN] = 1;
							}
/*
							//added by Mike, 20190105
							if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().contains("p")) {
								columnValuesArray[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN] = 1;
							}
*/							
						}
						else {
							//edited by Mike, 20190107
							if (inputColumns[INPUT_CONSULTATION_MEDICAL_CERTIFICATE_COLUMN].toLowerCase().trim().contains("mc")) {
								columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN] = 1;
							}
							else if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().trim().contains("p")) {								
								//edited by Mike, 20190108
								if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().trim().contains("/")) {
									columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN] = 1;
									columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN] = 1;
								}
								else {
									columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN] = 1;
								}
							}	
							else {
								columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN] = 1;
							}

/*							columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN] = 1;
*/
/*
							//added by Mike, 20190105
							if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().contains("p")) {
								columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN] = 1;
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
/*							dateContainer.get(dateValuesArrayInt[i])[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN]++;					
*/							
							//edited by Mike, 20190107
							if (inputColumns[INPUT_CONSULTATION_MEDICAL_CERTIFICATE_COLUMN].toLowerCase().trim().contains("mc")) {
								dateContainer.get(dateValuesArrayInt[i])[OUTPUT_CONSULTATION_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN]++;
							}
							else if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().trim().contains("p")) {
								//edited by Mike, 20190108
								if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().trim().contains("/")) {
									dateContainer.get(dateValuesArrayInt[i])[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN]++;
									dateContainer.get(dateValuesArrayInt[i])[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN]++;
								}
								else {
									dateContainer.get(dateValuesArrayInt[i])[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN]++;
								}
							}	
							else {
								dateContainer.get(dateValuesArrayInt[i])[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN]++;								
							}
/*
							//added by Mike, 20190105
							if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().contains("p")) {
								dateContainer.get(dateValuesArrayInt[i])[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN]++;
							}
*/							
						}
						else {							
/*							dateContainer.get(dateValuesArrayInt[i])[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]++;					
*/
							//edited by Mike, 20190107
							if (inputColumns[INPUT_CONSULTATION_MEDICAL_CERTIFICATE_COLUMN].toLowerCase().trim().contains("mc")) {
								dateContainer.get(dateValuesArrayInt[i])[OUTPUT_CONSULTATION_NON_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN]++;
							}
							else if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().trim().contains("p")) {
								//edited by Mike, 20190108
								if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().trim().contains("/")) {
									dateContainer.get(dateValuesArrayInt[i])[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN]++;
									dateContainer.get(dateValuesArrayInt[i])[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]++;
								}
								else {
									dateContainer.get(dateValuesArrayInt[i])[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN]++;
								}
							}	
							else {
								dateContainer.get(dateValuesArrayInt[i])[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]++;								
							}
/*
							//added by Mike, 20190105
							if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().contains("p")) {
								dateContainer.get(dateValuesArrayInt[i])[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN]++;
							}
*/							
						}
					}
				}					
	}

	//added by Mike, 20190226
	private static void processHMOBilling(HashMap<Integer, String[]> hmoBillingContainer, String[] inputColumns, boolean isConsultation) throws Exception {
			if (!isConsultation) {											
				if ((inputColumns[INPUT_CLASS_COLUMN].contains("HMO")) ||
					(inputColumns[INPUT_CLASS_COLUMN].contains("SLR"))) {

					String hmoName = inputColumns[INPUT_CLASS_COLUMN].trim().toUpperCase().replace("HMO/","");
					int chargeSlipNumber = Integer.parseInt(inputColumns[INPUT_CHARGE_SLIP_NUMBER_COLUMN].trim().toUpperCase());
					
					//added by Mike, 20190228
					if (!hmoPrintWriterContainer.containsKey(hmoName)) {
						hmoPrintWriterContainer.put(hmoName, new PrintWriter("output/"+hmoName+".txt","UTF-8"));
					}
					
					if (!hmoBillingContainer.containsKey(hmoBillingContainerTransactionCount)){//chargeSlipNumber)) {
						hmoBillingColumnValuesArray = new String[OUTPUT_TOTAL_COLUMNS];
					
//						columnValuesArray[OUTPUT_HMO_COUNT_COLUMN] = 1;
						hmoBillingColumnValuesArray[OUTPUT_HMO_BILLING_DATE_COLUMN] 
							= inputColumns[INPUT_DATE_COLUMN].trim().toUpperCase();
							
						hmoBillingColumnValuesArray[OUTPUT_HMO_BILLING_HMO_NAME_COLUMN] 
							= hmoName;//.replace("HMO/","")		
							
						hmoBillingColumnValuesArray[OUTPUT_HMO_BILLING_CHARGE_SLIP_NUMBER_COLUMN] 
							= ""+chargeSlipNumber;
							
						hmoBillingColumnValuesArray[OUTPUT_HMO_BILLING_PATIENT_NAME_COLUMN] 
							= inputColumns[INPUT_PATIENT_NAME_COLUMN].trim().toUpperCase();
							
						hmoBillingColumnValuesArray[OUTPUT_HMO_BILLING_APPROVAL_CODE_COLUMN] 
							= inputColumns[INPUT_APPROVAL_CODE_COLUMN].trim().toUpperCase();
							
						hmoBillingColumnValuesArray[OUTPUT_HMO_BILLING_MEDICAL_DOCTOR_NAME_COLUMN] 
							= inputColumns[INPUT_REFERRING_DOCTOR_COLUMN].trim().toUpperCase();
							
						hmoBillingColumnValuesArray[OUTPUT_HMO_BILLING_PAYEE_COLUMN] 
							= OUTPUT_HMO_BILLING_PAYEE_VALUE;

						//added by Mike, 20190303
						hmoBillingColumnValuesArray[OUTPUT_HMO_BILLING_DIAGNOSIS_COLUMN] 
							= inputColumns[INPUT_DIAGNOSIS_COLUMN].trim().toUpperCase();
							
						hmoBillingColumnValuesArray[OUTPUT_HMO_BILLING_DEPARTMENT_COLUMN] 
							= OUTPUT_HMO_BILLING_DEPARTMENT_PT_VALUE;
							
						hmoBillingColumnValuesArray[OUTPUT_HMO_BILLING_PROCEDURE_COLUMN] 
							= OUTPUT_HMO_BILLING_PROCEDURE_PT_VALUE;
							
						hmoBillingColumnValuesArray[OUTPUT_HMO_BILLING_TOTAL_AMOUNT_COLUMN] 
							= inputColumns[INPUT_FEE_COLUMN].trim().toUpperCase();						
						
						//added by Mike, 20190303
						//remove unnecessary quotaion mark, i.e. "
						for(int h=0; h<hmoBillingColumnValuesArray.length; h++) {
							if (hmoBillingColumnValuesArray[h]!=null) {
								hmoBillingColumnValuesArray[h] = hmoBillingColumnValuesArray[h].replace("\"","");
							}
						}
						
						hmoBillingContainer.put(hmoBillingContainerTransactionCount, hmoBillingColumnValuesArray);
						hmoBillingContainerTransactionCount++;
					}
					else {
						System.out.println("Error: Duplicate Charge Slip Number: "+chargeSlipNumber);
											
/*						
						hmoBillingContainer.get(hmoName)[OUTPUT_HMO_COUNT_COLUMN]++;	

						//added by Mike, 20190102
						if (inputColumns[INPUT_NEW_OLD_PATIENT_COLUMN].trim().toLowerCase().contains("new")) {
							hmoBillingContainer.get(hmoName)[OUTPUT_HMO_NEW_PATIENT_COUNT_COLUMN]++;
						}

						if (isNetPFComputed) {							
							hmoBillingContainer.get(hmoName)[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] 
								+= Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
								
							if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
								hmoBillingContainer.get(hmoName)[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
							}
							else {
								hmoBillingContainer.get(hmoName)[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);							
							}		
						}
*/						
					}
				}				
			}
			else {		
				if ((inputColumns[INPUT_CLASS_COLUMN+INPUT_CONSULTATION_OFFSET].contains("HMO")) ||
					(inputColumns[INPUT_CLASS_COLUMN+INPUT_CONSULTATION_OFFSET].contains("SLR"))) {
						
					String hmoName = inputColumns[INPUT_CLASS_COLUMN+INPUT_CONSULTATION_OFFSET].trim().toUpperCase().replace("HMO/","");
					//int chargeSlipNumber = ""; //no charge slip number for HMO Consultation transactions
					//Integer.parseInt(inputColumns[INPUT_CHARGE_SLIP_NUMBER_COLUMN].trim().toUpperCase());

					String consultationProcedureDetails = inputColumns[INPUT_CONSULTATION_PROCEDURE_DETAILS_COLUMN].trim();
					
					//added by Mike, 20190228
					if (!hmoPrintWriterContainer.containsKey(hmoName)) {
						hmoPrintWriterContainer.put(hmoName, new PrintWriter("output/"+hmoName+".txt","UTF-8"));
					}
					
					if (!hmoBillingContainer.containsKey(hmoBillingContainerTransactionCount)) {
						hmoBillingColumnValuesArray = new String[OUTPUT_TOTAL_COLUMNS];
					
	//						columnValuesArray[OUTPUT_HMO_COUNT_COLUMN] = 1;
						hmoBillingColumnValuesArray[OUTPUT_HMO_BILLING_DATE_COLUMN] 
							= inputColumns[INPUT_DATE_COLUMN].trim().toUpperCase();
							
						hmoBillingColumnValuesArray[OUTPUT_HMO_BILLING_HMO_NAME_COLUMN] 
							= hmoName;//.replace("HMO/","");				
							
						hmoBillingColumnValuesArray[OUTPUT_HMO_BILLING_CHARGE_SLIP_NUMBER_COLUMN] 
							= "";//+chargeSlipNumber;
							
						hmoBillingColumnValuesArray[OUTPUT_HMO_BILLING_PATIENT_NAME_COLUMN] 
							= inputColumns[INPUT_PATIENT_NAME_COLUMN].trim().toUpperCase();
							
						hmoBillingColumnValuesArray[OUTPUT_HMO_BILLING_APPROVAL_CODE_COLUMN] 
							= inputColumns[INPUT_APPROVAL_CODE_COLUMN+INPUT_CONSULTATION_OFFSET].trim().toUpperCase();
							
						hmoBillingColumnValuesArray[OUTPUT_HMO_BILLING_MEDICAL_DOCTOR_NAME_COLUMN] 
							= inputColumns[INPUT_REFERRING_DOCTOR_COLUMN+INPUT_CONSULTATION_OFFSET].trim().toUpperCase();
							
						hmoBillingColumnValuesArray[OUTPUT_HMO_BILLING_PAYEE_COLUMN] 
							= OUTPUT_HMO_BILLING_PAYEE_VALUE;
							
						//added by Mike, 20190303
						hmoBillingColumnValuesArray[OUTPUT_HMO_BILLING_DIAGNOSIS_COLUMN] 
							= inputColumns[INPUT_CONSULTATION_DIAGNOSIS_COLUMN].trim().toUpperCase();

						
/*						hmoBillingColumnValuesArray[OUTPUT_HMO_BILLING_DEPARTMENT_COLUMN] 
							= OUTPUT_HMO_BILLING_DEPARTMENT_ORTHO_VALUE;
*/
						//edited by Mike, 20190228
						if (inputColumns[INPUT_REFERRING_DOCTOR_COLUMN+INPUT_CONSULTATION_OFFSET].trim().toUpperCase().contains(HMO_BILLING_REHAB_CLASSIFICATION_KEYWORD_VALUE)) {
							hmoBillingColumnValuesArray[OUTPUT_HMO_BILLING_DEPARTMENT_COLUMN] 
								= OUTPUT_HMO_BILLING_DEPARTMENT_REHAB_VALUE;
																
							if (!consultationProcedureDetails.trim().equals("")) {
								hmoBillingColumnValuesArray[OUTPUT_HMO_BILLING_PROCEDURE_COLUMN] 
									= OUTPUT_HMO_BILLING_PROCEDURE_REHAB_PROCEDURE_VALUE + " ("+consultationProcedureDetails+")";
							}
							else {
								hmoBillingColumnValuesArray[OUTPUT_HMO_BILLING_PROCEDURE_COLUMN] 
									= OUTPUT_HMO_BILLING_PROCEDURE_REHAB_VALUE;
							}
						}
						else {
							hmoBillingColumnValuesArray[OUTPUT_HMO_BILLING_DEPARTMENT_COLUMN] 
								= OUTPUT_HMO_BILLING_DEPARTMENT_ORTHO_VALUE;
								
							if (!consultationProcedureDetails.trim().equals("")) {
								hmoBillingColumnValuesArray[OUTPUT_HMO_BILLING_PROCEDURE_COLUMN] 
									= OUTPUT_HMO_BILLING_PROCEDURE_ORTHO_PROCEDURE_VALUE + " ("+consultationProcedureDetails+")";
							}
							else {
								hmoBillingColumnValuesArray[OUTPUT_HMO_BILLING_PROCEDURE_COLUMN] 
									= OUTPUT_HMO_BILLING_PROCEDURE_ORTHO_VALUE;
							}
						}

/*						if (!consultationProcedureDetails.trim().equals("")) {
							hmoBillingColumnValuesArray[OUTPUT_HMO_BILLING_PROCEDURE_COLUMN] 
								= OUTPUT_HMO_BILLING_PROCEDURE_ORTHO_PROCEDURE_VALUE + " ("+consultationProcedureDetails+")";
						}
						else {
							hmoBillingColumnValuesArray[OUTPUT_HMO_BILLING_PROCEDURE_COLUMN] 
								= OUTPUT_HMO_BILLING_PROCEDURE_ORTHO_VALUE;
						}
*/											
						hmoBillingColumnValuesArray[OUTPUT_HMO_BILLING_TOTAL_AMOUNT_COLUMN] 
							= inputColumns[INPUT_FEE_COLUMN+INPUT_CONSULTATION_OFFSET].trim().toUpperCase();	

						//added by Mike, 20190303
						//remove unnecessary quotaion mark, i.e. "
						for(int h=0; h<hmoBillingColumnValuesArray.length; h++) {
							if (hmoBillingColumnValuesArray[h]!=null) {
								hmoBillingColumnValuesArray[h] = hmoBillingColumnValuesArray[h].replace("\"","");
							}
						}
						
						hmoBillingContainer.put(hmoBillingContainerTransactionCount, hmoBillingColumnValuesArray);
						hmoBillingContainerTransactionCount++;
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

					String hmoName = inputColumns[INPUT_CLASS_COLUMN].trim().toUpperCase();
					
					if (!hmoContainer.containsKey(hmoName)) {
						columnValuesArray = new double[OUTPUT_TOTAL_COLUMNS];
					
						columnValuesArray[OUTPUT_HMO_COUNT_COLUMN] = 1;
						
						//added by Mike, 20190102						
						if (inputColumns[INPUT_NEW_OLD_PATIENT_COLUMN].trim().toLowerCase().contains("new")) {
							columnValuesArray[OUTPUT_HMO_NEW_PATIENT_COUNT_COLUMN] = 1;
						}
						
						if (isNetPFComputed) {							
							columnValuesArray[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);

							if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
								columnValuesArray[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
							}
							else {
								columnValuesArray[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
							}
						}
						hmoContainer.put(hmoName, columnValuesArray);
					}
					else {
						hmoContainer.get(hmoName)[OUTPUT_HMO_COUNT_COLUMN]++;	

						//added by Mike, 20190102
						if (inputColumns[INPUT_NEW_OLD_PATIENT_COLUMN].trim().toLowerCase().contains("new")) {
							hmoContainer.get(hmoName)[OUTPUT_HMO_NEW_PATIENT_COUNT_COLUMN]++;
						}

						if (isNetPFComputed) {							
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
			}
			else {																	
				if ((inputColumns[INPUT_CLASS_COLUMN+INPUT_CONSULTATION_OFFSET].contains("HMO")) ||
					(inputColumns[INPUT_CLASS_COLUMN+INPUT_CONSULTATION_OFFSET].contains("SLR"))) {

					String hmoName = inputColumns[INPUT_CLASS_COLUMN+INPUT_CONSULTATION_OFFSET].trim().toUpperCase();
					
					if (!hmoContainer.containsKey(hmoName)) {
						columnValuesArray = new double[OUTPUT_TOTAL_COLUMNS];		

						//added by Mike, 20190102
						if (inputColumns[INPUT_NEW_OLD_PATIENT_COLUMN+INPUT_CONSULTATION_OFFSET].trim().toLowerCase().contains("new")) {
							columnValuesArray[OUTPUT_CONSULTATION_HMO_NEW_PATIENT_COUNT_COLUMN] = 1;
						}						
						
						//edited by Mike, 20190109
						if (inputColumns[INPUT_CONSULTATION_MEDICAL_CERTIFICATE_COLUMN].toLowerCase().trim().contains("mc")) {
							columnValuesArray[OUTPUT_CONSULTATION_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN] = 1;						
						}
						else if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().trim().contains("p")) {
							//edited by Mike, 20190108
							if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().trim().contains("/")) {
								//do not include in count; only for NON-HMO/Cash payments
/*								columnValuesArray[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN] = 1;						
								columnValuesArray[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN] = 1;						
*/								
							}
							else {
								columnValuesArray[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN] = 1;						
							}
						}	
						else {
							columnValuesArray[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN] = 1;						
						}
						
/*						columnValuesArray[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN] = 1;						
*/
						hmoContainer.put(hmoName, columnValuesArray);
					}
					else {
						//added by Mike, 20190102
						if (inputColumns[INPUT_NEW_OLD_PATIENT_COLUMN+INPUT_CONSULTATION_OFFSET].trim().toLowerCase().contains("new")) {
							hmoContainer.get(hmoName)[OUTPUT_CONSULTATION_HMO_NEW_PATIENT_COUNT_COLUMN]++;
						}						
						
						//edited by Mike, 20190109
						if (inputColumns[INPUT_CONSULTATION_MEDICAL_CERTIFICATE_COLUMN].toLowerCase().trim().contains("mc")) {
							hmoContainer.get(hmoName)[OUTPUT_CONSULTATION_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN]++;						
						}
						else if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().trim().contains("p")) {
							//edited by Mike, 20190108
							if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().trim().contains("/")) {
								//do not include in count; only for NON-HMO/Cash payments
/*								columnValuesArray[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN]++;						
								columnValuesArray[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN]++;						
*/								
							}
							else {
								hmoContainer.get(hmoName)[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN]++;						
							}
						}	
						else {
							hmoContainer.get(hmoName)[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN]++;						
						}
					
/*						hmoContainer.get(hmoName)[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN]++;					
*/
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

				String classificationName = inputColumns[INPUT_CLASS_COLUMN].trim().toUpperCase();
				
				if (!nonHmoContainer.containsKey(classificationName)) {
					columnValuesArray = new double[OUTPUT_TOTAL_COLUMNS];
				
					columnValuesArray[OUTPUT_NON_HMO_COUNT_COLUMN] = 1;
					
					if (isNetPFComputed) {							
						columnValuesArray[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);

						if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
							columnValuesArray[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						}
						else {
							columnValuesArray[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						}
					}
					
					nonHmoContainer.put(classificationName, columnValuesArray);
				}
				else {
					nonHmoContainer.get(classificationName)[OUTPUT_NON_HMO_COUNT_COLUMN]++;					

					if (isNetPFComputed) {							
						nonHmoContainer.get(classificationName)[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] 
							+= Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);

						if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
							nonHmoContainer.get(classificationName)[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						}
						else {
							nonHmoContainer.get(classificationName)[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);							
						}		
					}					
				}
			}			
		}
		else {			
			if ((!inputColumns[INPUT_CLASS_COLUMN+INPUT_CONSULTATION_OFFSET].contains("HMO")) &&
				(!inputColumns[INPUT_CLASS_COLUMN+INPUT_CONSULTATION_OFFSET].contains("SLR"))) {

				String classificationName = inputColumns[INPUT_CLASS_COLUMN+INPUT_CONSULTATION_OFFSET].trim().toUpperCase();
				System.out.println("classificationName: "+classificationName); 
				
				if (isInDebugMode) {
					if (classificationName.trim().equals("")) {
						System.out.println(">>> "+inputColumns[INPUT_DATE_COLUMN]+"; Name: "+inputColumns[INPUT_PATIENT_NAME_COLUMN]);
					}
				}
/*								
				if (!nonHmoContainer.containsKey(classificationName)) {
					columnValuesArray = new double[OUTPUT_TOTAL_COLUMNS];				
					columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN] = 1;					
					nonHmoContainer.put(classificationName, columnValuesArray);
				}
				else {
					nonHmoContainer.get(classificationName)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]++;
				}
*/				
				
				if (!nonHmoContainer.containsKey(classificationName)) {
						columnValuesArray = new double[OUTPUT_TOTAL_COLUMNS];					
						
						//edited by Mike, 20190109
						if (inputColumns[INPUT_CONSULTATION_MEDICAL_CERTIFICATE_COLUMN].toLowerCase().trim().contains("mc")) {
							columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN] = 1;						
						}
						else if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().trim().contains("p")) {
							//edited by Mike, 20190108
							if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().trim().contains("/")) {
								//include in count; only for NON-HMO/Cash payments
								columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN] = 1;						
								columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN] = 1;														
							}
							else {
								columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN] = 1;						
							}
						}	
						else {
							columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN] = 1;						
						}
						
/*						columnValuesArray[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN] = 1;						
*/
						nonHmoContainer.put(classificationName, columnValuesArray);
					}
					else {
						//edited by Mike, 20190109
						if (inputColumns[INPUT_CONSULTATION_MEDICAL_CERTIFICATE_COLUMN].toLowerCase().trim().contains("mc")) {
/*							hmoContainer.get(hmoName)[OUTPUT_CONSULTATION_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN]++;			
*/
							nonHmoContainer.get(classificationName)[OUTPUT_CONSULTATION_NON_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN]++;							
						}
						else if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().trim().contains("p")) {
							//edited by Mike, 20190108
							if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().trim().contains("/")) {
								//include in count; only for NON-HMO/Cash payments
								nonHmoContainer.get(classificationName)[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN]++;						
								nonHmoContainer.get(classificationName)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]++;									
							}
							else {
								nonHmoContainer.get(classificationName)[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN]++;						
							}
						}	
						else {
							nonHmoContainer.get(classificationName)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]++;						
						}
					
/*						hmoContainer.get(hmoName)[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN]++;					
*/
					}
			}
		}
	}	
	
	//added by Mike, 20181218
	private static void processReferringDoctorTransactionCount(HashMap<String, double[]> referringDoctorContainer, String[] inputColumns, Boolean isConsultation) {		
		//added by Mike, 20190125
		String inputReferringMedicalDoctor = inputColumns[INPUT_REFERRING_DOCTOR_COLUMN].trim().toUpperCase();
	
		//edited by Mike, 20181219
		if (!isConsultation) {	
			if (!referringDoctorContainer.containsKey(inputReferringMedicalDoctor)) {
				columnValuesArray = new double[OUTPUT_TOTAL_COLUMNS];
				
				if (inputColumns[INPUT_CLASS_COLUMN].contains("HMO")) {
					columnValuesArray[OUTPUT_HMO_COUNT_COLUMN] = 1;
					
					if (isNetPFComputed) {
						columnValuesArray[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);

						if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
							columnValuesArray[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						}
						else {
							columnValuesArray[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						}
					}

					if (inputColumns[INPUT_NEW_OLD_COLUMN].toLowerCase().contains("new")) {
						//added by Mike, 20181218
						columnValuesArray[OUTPUT_HMO_NEW_PATIENT_COUNT_COLUMN] = 1;
					}							
				}
				else {
					columnValuesArray[OUTPUT_NON_HMO_COUNT_COLUMN] = 1;
					
					if (isNetPFComputed) {
						columnValuesArray[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);

						if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
							columnValuesArray[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						}
						else {
							columnValuesArray[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						}
					}
					
					if (inputColumns[INPUT_NEW_OLD_COLUMN].toLowerCase().contains("new")) {
						//added by Mike, 20181218
						columnValuesArray[OUTPUT_NON_HMO_NEW_PATIENT_COUNT_COLUMN] = 1;
					}			
				}
				
				referringDoctorContainer.put(inputReferringMedicalDoctor, columnValuesArray);
			}
			else {
				if (inputColumns[INPUT_CLASS_COLUMN].contains("HMO")) {
					referringDoctorContainer.get(inputReferringMedicalDoctor)[OUTPUT_HMO_COUNT_COLUMN]++;		

					if (isNetPFComputed) {					
						referringDoctorContainer.get(inputReferringMedicalDoctor)[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] 
							+= Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
							
						if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
							referringDoctorContainer.get(inputReferringMedicalDoctor)[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						}
						else {
							referringDoctorContainer.get(inputReferringMedicalDoctor)[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						}
					}
					
					if (inputColumns[INPUT_NEW_OLD_COLUMN].toLowerCase().contains("new")) {
						//added by Mike, 20181218
						referringDoctorContainer.get(inputReferringMedicalDoctor)[OUTPUT_HMO_NEW_PATIENT_COUNT_COLUMN]++;					
					}							
				}
				else {
					referringDoctorContainer.get(inputReferringMedicalDoctor)[OUTPUT_NON_HMO_COUNT_COLUMN]++;	

					if (isNetPFComputed) {					
						referringDoctorContainer.get(inputReferringMedicalDoctor)[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] 
							+= Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
							
						if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
							referringDoctorContainer.get(inputReferringMedicalDoctor)[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						}
						else {
							referringDoctorContainer.get(inputReferringMedicalDoctor)[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						}
					}
					
					if (inputColumns[INPUT_NEW_OLD_COLUMN].toLowerCase().contains("new")) {
						//added by Mike, 20181218
						referringDoctorContainer.get(inputReferringMedicalDoctor)[OUTPUT_NON_HMO_NEW_PATIENT_COUNT_COLUMN]++;					
					}
				}
			}
		}
		else {
			//added by Mike, 20190125
			inputReferringMedicalDoctor = inputColumns[INPUT_REFERRING_DOCTOR_COLUMN+INPUT_CONSULTATION_OFFSET].trim().toUpperCase();
			
			if (!referringDoctorContainer.containsKey(inputReferringMedicalDoctor)) {
				columnValuesArray = new double[OUTPUT_TOTAL_COLUMNS];
				
				if (inputColumns[INPUT_CLASS_COLUMN+INPUT_CONSULTATION_OFFSET].contains("HMO")) {						
					//edited by Mike, 20190109
					if (inputColumns[INPUT_CONSULTATION_MEDICAL_CERTIFICATE_COLUMN].toLowerCase().trim().contains("mc")) {
						columnValuesArray[OUTPUT_CONSULTATION_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN] = 1;						
					}
					else if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().trim().contains("p")) {
						//edited by Mike, 20190108
						if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().trim().contains("/")) {
							//do not include in count; only for NON-HMO/Cash payments
/*							columnValuesArray[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN] = 1;						
							columnValuesArray[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN] = 1;														
*/							
						}
						else {
							columnValuesArray[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN] = 1;						
						}
					}	
					else {
						columnValuesArray[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN] = 1;						
					}
					
					referringDoctorContainer.put(inputReferringMedicalDoctor, columnValuesArray);
				}
				else {
					//edited by Mike, 20190109
					if (inputColumns[INPUT_CONSULTATION_MEDICAL_CERTIFICATE_COLUMN].toLowerCase().trim().contains("mc")) {
						columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN] = 1;							
					}
					else if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().trim().contains("p")) {
						//edited by Mike, 20190108
						if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().trim().contains("/")) {
							//include in count; only for NON-HMO/Cash payments
							columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN] = 1;						
							columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN] = 1;									
						}
						else {
							columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN] = 1;						
						}
					}	
					else {
						columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN] = 1;					
					}
				}				
/*				
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
*/				
				referringDoctorContainer.put(inputReferringMedicalDoctor, columnValuesArray);
			}
			else {													
				if (inputColumns[INPUT_CLASS_COLUMN+INPUT_CONSULTATION_OFFSET].contains("HMO")) {
					//edited by Mike, 20190109
					if (inputColumns[INPUT_CONSULTATION_MEDICAL_CERTIFICATE_COLUMN].toLowerCase().trim().contains("mc")) {
/*						columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN]++;							
*/
						referringDoctorContainer.get(inputReferringMedicalDoctor)[OUTPUT_CONSULTATION_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN]++;				
					}
					else if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().trim().contains("p")) {
						//edited by Mike, 20190108
						if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().trim().contains("/")) {
/*							//include in count; only for NON-HMO/Cash payments
							columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN]++;						
							columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]++;									
*/							
							referringDoctorContainer.get(inputReferringMedicalDoctor)[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN]++;				
							referringDoctorContainer.get(inputReferringMedicalDoctor)[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN]++;				
						}
						else {
							referringDoctorContainer.get(inputReferringMedicalDoctor)[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN]++;				
/*
							columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN]++;						
*/							
						}
					}	
					else {
						referringDoctorContainer.get(inputReferringMedicalDoctor)[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN]++;				
/*
						columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]++;						
*/						
					}

/*												`		referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN+INPUT_CONSULTATION_OFFSET])[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN]++;				
					//added by Mike, 20181219
					if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().contains("p")) {
						//edited by Mike, 20181221
						//columnValuesArray[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN]++;
						referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN+INPUT_CONSULTATION_OFFSET])[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN]++;				
					}
*/					
				}
				else {
					//edited by Mike, 20190109
					if (inputColumns[INPUT_CONSULTATION_MEDICAL_CERTIFICATE_COLUMN].toLowerCase().trim().contains("mc")) {
/*						columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN]++;							
*/
						referringDoctorContainer.get(inputReferringMedicalDoctor)[OUTPUT_CONSULTATION_NON_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN]++;				
					}
					else if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().trim().contains("p")) {
						//edited by Mike, 20190108
						if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().trim().contains("/")) {
/*							//include in count; only for NON-HMO/Cash payments
							columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN]++;						
							columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]++;									
*/							
							referringDoctorContainer.get(inputReferringMedicalDoctor)[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN]++;				
							referringDoctorContainer.get(inputReferringMedicalDoctor)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]++;				
						}
						else {
							referringDoctorContainer.get(inputReferringMedicalDoctor)[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN]++;				
/*
							columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN]++;						
*/							
						}
					}	
					else {
						referringDoctorContainer.get(inputReferringMedicalDoctor)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]++;				
/*
						columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]++;						
*/						
					}

					
/*					
					referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN+INPUT_CONSULTATION_OFFSET])[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]++;					
					//added by Mike, 20181219
					if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().contains("p")) {
						//edited by Mike, 20181221
						//columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN]++;						
						referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN+INPUT_CONSULTATION_OFFSET])[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN]++;				
					}
*/					
				}
			}
		}
	}

	//added by Mike, 20181218
	private static void processMedicalDoctorTransactionCount(HashMap<String, double[]> medicalDoctorContainer, String[] inputColumns, Boolean isConsultation) {		
		String inputMedicalDoctor = inputColumns[INPUT_REFERRING_DOCTOR_COLUMN].trim().toUpperCase();
	
		//edited by Mike, 20181219
		if (!isConsultation) {	//only process follow-up count for Consultation transactions			
			if (!medicalDoctorContainer.containsKey(inputMedicalDoctor)) {
				columnValuesArray = new double[OUTPUT_TOTAL_COLUMNS];
				
				if (inputColumns[INPUT_CLASS_COLUMN].contains("HMO")) {
					columnValuesArray[OUTPUT_HMO_COUNT_COLUMN] = 1;
					
					if (isNetPFComputed) {
						columnValuesArray[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);

						if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
							columnValuesArray[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						}
						else {
							columnValuesArray[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						}
					}

					if (inputColumns[INPUT_NEW_OLD_COLUMN].toLowerCase().contains("new")) {
						//added by Mike, 20181218
						columnValuesArray[OUTPUT_HMO_NEW_PATIENT_COUNT_COLUMN] = 1;
					}							
				}
				else {
					columnValuesArray[OUTPUT_NON_HMO_COUNT_COLUMN] = 1;
					
					if (isNetPFComputed) {
						columnValuesArray[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);

						if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
							columnValuesArray[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						}
						else {
							columnValuesArray[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						}
					}
					
					if (inputColumns[INPUT_NEW_OLD_COLUMN].toLowerCase().contains("new")) {
						//added by Mike, 20181218
						columnValuesArray[OUTPUT_NON_HMO_NEW_PATIENT_COUNT_COLUMN] = 1;
					}			
				}
				
				medicalDoctorContainer.put(inputMedicalDoctor, columnValuesArray);
			}
			else {
				if (inputColumns[INPUT_CLASS_COLUMN].contains("HMO")) {
					medicalDoctorContainer.get(inputMedicalDoctor)[OUTPUT_HMO_COUNT_COLUMN]++;		

					if (isNetPFComputed) {					
						medicalDoctorContainer.get(inputMedicalDoctor)[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] 
							+= Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
							
						if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
							medicalDoctorContainer.get(inputMedicalDoctor)[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						}
						else {
							medicalDoctorContainer.get(inputMedicalDoctor)[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						}
					}
					
					if (inputColumns[INPUT_NEW_OLD_COLUMN].toLowerCase().contains("new")) {
						//added by Mike, 20181218
						medicalDoctorContainer.get(inputMedicalDoctor)[OUTPUT_HMO_NEW_PATIENT_COUNT_COLUMN]++;					
					}							
				}
				else {
					medicalDoctorContainer.get(inputMedicalDoctor)[OUTPUT_NON_HMO_COUNT_COLUMN]++;	

					if (isNetPFComputed) {					
						medicalDoctorContainer.get(inputMedicalDoctor)[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] 
							+= Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
							
						if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
							medicalDoctorContainer.get(inputMedicalDoctor)[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						}
						else {
							medicalDoctorContainer.get(inputMedicalDoctor)[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						}
					}
					
					if (inputColumns[INPUT_NEW_OLD_COLUMN].toLowerCase().contains("new")) {
						//added by Mike, 20181218
						medicalDoctorContainer.get(inputMedicalDoctor)[OUTPUT_NON_HMO_NEW_PATIENT_COUNT_COLUMN]++;					
					}
				}
			}			
		}
		else {
			//added by Mike, 20190125
			inputMedicalDoctor = inputColumns[INPUT_CONSULTATION_MEDICAL_DOCTOR_COLUMN].trim().toUpperCase();
				
			if (!medicalDoctorContainer.containsKey(inputMedicalDoctor)) {
				columnValuesArray = new double[OUTPUT_TOTAL_COLUMNS];
								
				if (inputColumns[INPUT_CLASS_COLUMN+INPUT_CONSULTATION_OFFSET].contains("HMO")) {						
					if (inputColumns[INPUT_NEW_OLD_COLUMN+INPUT_CONSULTATION_OFFSET].toLowerCase().contains("new")) {
						//added by Mike, 20181218
						columnValuesArray[OUTPUT_CONSULTATION_HMO_NEW_PATIENT_COUNT_COLUMN] = 1;
					}	
					//added by Mike, 20190202
					else if (inputColumns[INPUT_NEW_OLD_COLUMN+INPUT_CONSULTATION_OFFSET].toLowerCase().contains("old")) {
						columnValuesArray[OUTPUT_CONSULTATION_HMO_OLD_PATIENT_COUNT_COLUMN] = 1;
					}	
					//added by Mike, 20190202
					else if ((inputColumns[INPUT_NEW_OLD_COLUMN+INPUT_CONSULTATION_OFFSET].toLowerCase().contains("follow up")) ||
							(inputColumns[INPUT_NEW_OLD_COLUMN+INPUT_CONSULTATION_OFFSET].toLowerCase().contains("follow-up"))) {
						columnValuesArray[OUTPUT_CONSULTATION_HMO_FOLLOW_UP_COUNT_COLUMN] = 1;
					}	
				
					//edited by Mike, 20190109
					if (inputColumns[INPUT_CONSULTATION_MEDICAL_CERTIFICATE_COLUMN].toLowerCase().trim().contains("mc")) {
						columnValuesArray[OUTPUT_CONSULTATION_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN] = 1;						
					}
					else if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().trim().contains("p")) {
						//edited by Mike, 20190108
						if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().trim().contains("/")) {
							//do not include in count; only for NON-HMO/Cash payments
/*							columnValuesArray[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN] = 1;						
							columnValuesArray[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN] = 1;														
*/							
						}
						else {
							columnValuesArray[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN] = 1;						
						}
					}	
					else {
						columnValuesArray[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN] = 1;						
					}
					
					medicalDoctorContainer.put(inputMedicalDoctor, columnValuesArray);
				}
				else {
					if (inputColumns[INPUT_NEW_OLD_COLUMN+INPUT_CONSULTATION_OFFSET].toLowerCase().contains("new")) {
						//added by Mike, 20181218
						columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_NEW_PATIENT_COUNT_COLUMN] = 1;
					}	
					//added by Mike, 20190202
					else if (inputColumns[INPUT_NEW_OLD_COLUMN+INPUT_CONSULTATION_OFFSET].toLowerCase().contains("old")) {
						columnValuesArray[OUTPUT_CONSULTATION_HMO_OLD_PATIENT_COUNT_COLUMN] = 1;
					}	
					//added by Mike, 20190202
					else if ((inputColumns[INPUT_NEW_OLD_COLUMN+INPUT_CONSULTATION_OFFSET].toLowerCase().contains("follow up")) ||
							(inputColumns[INPUT_NEW_OLD_COLUMN+INPUT_CONSULTATION_OFFSET].toLowerCase().contains("follow-up"))) {
						columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_FOLLOW_UP_COUNT_COLUMN] = 1;
					}	

					//edited by Mike, 20190109
					if (inputColumns[INPUT_CONSULTATION_MEDICAL_CERTIFICATE_COLUMN].toLowerCase().trim().contains("mc")) {
						columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN] = 1;							
					}
					else if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().trim().contains("p")) {
						//edited by Mike, 20190108
						if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().trim().contains("/")) {
							//include in count; only for NON-HMO/Cash payments
							columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN] = 1;						
							columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN] = 1;									
						}
						else {
							columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN] = 1;						
						}
					}	
					else {
						columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN] = 1;					
					}
				}				
				medicalDoctorContainer.put(inputMedicalDoctor, columnValuesArray);
			}
			else {													
				if (inputColumns[INPUT_CLASS_COLUMN+INPUT_CONSULTATION_OFFSET].contains("HMO")) {
					if (inputColumns[INPUT_NEW_OLD_COLUMN+INPUT_CONSULTATION_OFFSET].toLowerCase().contains("new")) {
						medicalDoctorContainer.get(inputMedicalDoctor)[OUTPUT_CONSULTATION_HMO_NEW_PATIENT_COUNT_COLUMN]++;				
					}	
					//added by Mike, 20190202
					else if (inputColumns[INPUT_NEW_OLD_COLUMN+INPUT_CONSULTATION_OFFSET].toLowerCase().contains("old")) {
						medicalDoctorContainer.get(inputMedicalDoctor)[OUTPUT_CONSULTATION_HMO_OLD_PATIENT_COUNT_COLUMN]++;
					}	
					//added by Mike, 20190202
					else if ((inputColumns[INPUT_NEW_OLD_COLUMN+INPUT_CONSULTATION_OFFSET].toLowerCase().contains("follow up")) ||
							(inputColumns[INPUT_NEW_OLD_COLUMN+INPUT_CONSULTATION_OFFSET].toLowerCase().contains("follow-up"))) {
						medicalDoctorContainer.get(inputMedicalDoctor)[OUTPUT_CONSULTATION_HMO_FOLLOW_UP_COUNT_COLUMN]++;				
					}	

					//edited by Mike, 20190109
					if (inputColumns[INPUT_CONSULTATION_MEDICAL_CERTIFICATE_COLUMN].toLowerCase().trim().contains("mc")) {
/*						columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN]++;							
*/
						medicalDoctorContainer.get(inputMedicalDoctor)[OUTPUT_CONSULTATION_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN]++;				
					}
					else if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().trim().contains("p")) {
						//edited by Mike, 20190108
						if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().trim().contains("/")) {
/*							//include in count; only for NON-HMO/Cash payments
							columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN]++;						
							columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]++;									
*/							
							medicalDoctorContainer.get(inputMedicalDoctor)[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN]++;				
							medicalDoctorContainer.get(inputMedicalDoctor)[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN]++;				
						}
						else {
							medicalDoctorContainer.get(inputMedicalDoctor)[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN]++;				
/*
							columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN]++;						
*/							
						}
					}	
					else {
						medicalDoctorContainer.get(inputMedicalDoctor)[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN]++;				
/*
						columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]++;						
*/						
					}

/*												`		referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN+INPUT_CONSULTATION_OFFSET])[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN]++;				
					//added by Mike, 20181219
					if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().contains("p")) {
						//edited by Mike, 20181221
						//columnValuesArray[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN]++;
						referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN+INPUT_CONSULTATION_OFFSET])[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN]++;				
					}
*/					
				}
				else {
					if (inputColumns[INPUT_NEW_OLD_COLUMN+INPUT_CONSULTATION_OFFSET].toLowerCase().contains("new")) {
						medicalDoctorContainer.get(inputMedicalDoctor)[OUTPUT_CONSULTATION_NON_HMO_NEW_PATIENT_COUNT_COLUMN]++;				
					}	
					//added by Mike, 20190202
					else if (inputColumns[INPUT_NEW_OLD_COLUMN+INPUT_CONSULTATION_OFFSET].toLowerCase().contains("old")) {
						medicalDoctorContainer.get(inputMedicalDoctor)[OUTPUT_CONSULTATION_NON_HMO_OLD_PATIENT_COUNT_COLUMN]++;
					}	
					//added by Mike, 20190202
					else if ((inputColumns[INPUT_NEW_OLD_COLUMN+INPUT_CONSULTATION_OFFSET].toLowerCase().contains("follow up")) ||
							(inputColumns[INPUT_NEW_OLD_COLUMN+INPUT_CONSULTATION_OFFSET].toLowerCase().contains("follow-up"))) {
						medicalDoctorContainer.get(inputMedicalDoctor)[OUTPUT_CONSULTATION_NON_HMO_FOLLOW_UP_COUNT_COLUMN]++;				
					}	

					//edited by Mike, 20190109
					if (inputColumns[INPUT_CONSULTATION_MEDICAL_CERTIFICATE_COLUMN].toLowerCase().trim().contains("mc")) {
/*						columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN]++;							
*/
						medicalDoctorContainer.get(inputMedicalDoctor)[OUTPUT_CONSULTATION_NON_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN]++;				
					}
					else if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().trim().contains("p")) {
						//edited by Mike, 20190108
						if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().trim().contains("/")) {
/*							//include in count; only for NON-HMO/Cash payments
							columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN]++;						
							columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]++;									
*/							
							medicalDoctorContainer.get(inputMedicalDoctor)[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN]++;				
							medicalDoctorContainer.get(inputMedicalDoctor)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]++;				
						}
						else {
							medicalDoctorContainer.get(inputMedicalDoctor)[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN]++;				
/*
							columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN]++;						
*/							
						}
					}	
					else {
						medicalDoctorContainer.get(inputMedicalDoctor)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]++;				
/*
						columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]++;						
*/						
					}

					
/*					
					referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN+INPUT_CONSULTATION_OFFSET])[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]++;					
					//added by Mike, 20181219
					if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().contains("p")) {
						//edited by Mike, 20181221
						//columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN]++;						
						referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN+INPUT_CONSULTATION_OFFSET])[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN]++;				
					}
*/					
				}
			}
		}
	}
	
	//added by Mike, 20181220
	private static void processMedicalDoctorTransactionPerClassificationCount(HashMap<String, HashMap<String, double[]>> classificationContainerPerMedicalDoctor, String[] inputColumns, Boolean isConsultation) {				

		String medicalDoctorKey = inputColumns[INPUT_REFERRING_DOCTOR_COLUMN+INPUT_CONSULTATION_OFFSET].trim().toUpperCase();
	
		if (isConsultation) {			
			String classificationName = inputColumns[INPUT_CLASS_COLUMN+INPUT_CONSULTATION_OFFSET].trim().toUpperCase(); //added by Mike, 20181220
//			System.out.println(">"+" "+classificationName);
//			System.out.println(">>> "+inputColumns[INPUT_REFERRING_DOCTOR_COLUMN+INPUT_CONSULTATION_OFFSET]);

				if (!classificationName.contains("HMO")) {					
//			System.out.println(">>>"+inputColumns[INPUT_REFERRING_DOCTOR_COLUMN+INPUT_CONSULTATION_OFFSET]+" "+classificationName);

/*					classificationContainerPerMedicalDoctor.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN+INPUT_CONSULTATION_OFFSET]).get(classificationName)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]++;
*/					
					//edited by Mike, 20190107
					if (inputColumns[INPUT_CONSULTATION_MEDICAL_CERTIFICATE_COLUMN].toLowerCase().trim().contains("mc")) {
						classificationContainerPerMedicalDoctor.get(medicalDoctorKey).get(classificationName)[OUTPUT_CONSULTATION_NON_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN]++;
					}
					else if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().trim().contains("p")) {
						//edited by Mike, 20190108
						if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().trim().contains("/")) {
							classificationContainerPerMedicalDoctor.get(medicalDoctorKey).get(classificationName)[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN]++;
							classificationContainerPerMedicalDoctor.get(medicalDoctorKey).get(classificationName)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]++;
						}
						else {
							classificationContainerPerMedicalDoctor.get(medicalDoctorKey).get(classificationName)[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN]++;
						}
					}	
					else {
						classificationContainerPerMedicalDoctor.get(medicalDoctorKey).get(classificationName)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]++;
					}

					

//					System.out.println(">>> NON-HMO count: "+classificationContainerPerMedicalDoctor.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN+INPUT_CONSULTATION_OFFSET]).get(classificationName)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]);
				}
				else {
//				System.out.println(">>>>>"+inputColumns[INPUT_REFERRING_DOCTOR_COLUMN+INPUT_CONSULTATION_OFFSET]+" "+classificationName);
/*
					classificationContainerPerMedicalDoctor.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN+INPUT_CONSULTATION_OFFSET]).get(classificationName)[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN]++;					
*/

					//edited by Mike, 20190107
					if (inputColumns[INPUT_CONSULTATION_MEDICAL_CERTIFICATE_COLUMN].toLowerCase().trim().contains("mc")) {
						classificationContainerPerMedicalDoctor.get(medicalDoctorKey).get(classificationName)[OUTPUT_CONSULTATION_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN]++;
					}
					else if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().trim().contains("p")) {
						//edited by Mike, 20190108
						if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().trim().contains("/")) {
							classificationContainerPerMedicalDoctor.get(medicalDoctorKey).get(classificationName)[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN]++;
							classificationContainerPerMedicalDoctor.get(medicalDoctorKey).get(classificationName)[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN]++;
						}
						else {
							classificationContainerPerMedicalDoctor.get(medicalDoctorKey).get(classificationName)[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN]++;
						}
					}	
					else {
						classificationContainerPerMedicalDoctor.get(medicalDoctorKey).get(classificationName)[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN]++;
					}

//					System.out.println(">>>>> HMO count: "+classificationContainerPerMedicalDoctor.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN+INPUT_CONSULTATION_OFFSET]).get(classificationName)[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN]);

				}
		}		
	}

	private static void setClassificationContainerPerMedicalDoctor(HashMap<String, HashMap<String, double[]>> classificationContainerPerMedicalDoctor) {
		SortedSet<String> sortedHmoContainerKeyset = new TreeSet<String>(hmoContainer.keySet());
		SortedSet<String> sortedNonHmoContainerKeyset = new TreeSet<String>(nonHmoContainer.keySet());
		SortedSet<String> sortedMedicalDoctorKeyset = new TreeSet<String>(medicalDoctorContainer.keySet());
				
		for (String medicalDoctorKey : sortedMedicalDoctorKeyset) {						
//System.out.println("medical doctor: "+key);		
			classificationContainerHashmap = new HashMap<String, double[]>();

			for (String key : sortedHmoContainerKeyset) {						
//	System.out.println("hmoKey: "+key);		
//	System.out.println("classificationContainerColumnValuesArray: "+classificationContainerColumnValuesArray[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN]);		
				classificationContainerColumnValuesArray = new double[OUTPUT_TOTAL_COLUMNS];				
				classificationContainerHashmap.put(key, classificationContainerColumnValuesArray);			
			}

			for (String key : sortedNonHmoContainerKeyset) {								
//	System.out.println("nonHmoKey: "+key);		
//	System.out.println("classificationContainerColumnValuesArray: "+classificationContainerColumnValuesArray[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]);		
				classificationContainerColumnValuesArray = new double[OUTPUT_TOTAL_COLUMNS];				
				classificationContainerHashmap.put(key, classificationContainerColumnValuesArray);
			}

			classificationContainerPerMedicalDoctor.put(medicalDoctorKey, classificationContainerHashmap);
		}					
/*		
		for (String key : sortedMedicalDoctorKeyset) {						
			for (String nonHmoKey : sortedNonHmoContainerKeyset) {						
				System.out.println(classificationContainerPerMedicalDoctor.get(key).get(nonHmoKey)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]);
			}
		}														
*/		
	}

	private static void processInputFiles(String[] args, boolean isPhaseOne) throws Exception {
		//edited by Mike, 20181030
		for (int i=0; i<args.length; i++) {						
			//added by Mike, 20181030
			inputFilename = args[i].replaceAll(".txt","");			
			File f = new File(inputFilename+".txt");

			System.out.println("inputFilename: " + inputFilename);
			
			//added by Mike, 20190207
			if (inputFilename.contains("*")) {
				continue;
			}
			
			if (inputFilename.toLowerCase().contains("consultation")) {
				isConsultation=true;
			}
			else {
				isConsultation=false;
			}
						
			Scanner sc = new Scanner(new FileInputStream(f));				
		
			String s;		
			s=sc.nextLine(); //skip the first row, which is the input file's table headers
	
			if (isInDebugMode) {
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

				//edited by Mike, 20190207
				if (dateValuesArrayInt[i]==0) {
					dateValuesArrayInt[i] = getYearMonthAsInt(inputColumns[INPUT_DATE_COLUMN]);					
/*					
					dateValuesArrayInt[i] = Integer.parseInt(args[i].substring(args[i].indexOf("_")+1,args[i].indexOf(".txt")));
*/					
				}

				
/*
				int dateValueInt = Integer.parseInt(args[i].substring(args[i].indexOf("_")+1,args[i].indexOf(".txt")));
				if (!dateValuesArrayInt.contains(dateValueInt)){
					dateValuesArrayInt.add(dateValueInt);
				}				
*/				
/*				//edited by Mike, 20181121
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
*/
				if (isInDebugMode) {
					rowCount++;
					System.out.println("rowCount: "+rowCount);
				}
/*				
				//added by Mike, 20181121
				//skip transactions that have "RehabSupplies" as its "CLASS" value
				//In Excel logbook/workbook 2018 onwards, such transactions are not included in the Consultation and PT Treatment Excel logbooks/workbooks.
				if (inputColumns[INPUT_CLASS_COLUMN].contains("RehabSupplies")) {
					continue;
				}
*/
				if (isPhaseOne) {
					//added by Mike, 20181216
	//				processMonthlyCount(dateContainer, inputColumns, i, false);
/*					processMonthlyCount(dateContainer, inputColumns, i, isConsultation); //isConsultation = false
*/					
/*					//added by Mike, 20181217
					processHMOCount(hmoContainer, inputColumns, isConsultation); //edited by Mike, 20181219
*/					
					//added by Mike, 20181217
/*					processNONHMOCount(nonHmoContainer, inputColumns, isConsultation); //edited by Mike, 20181219
*/

/*					
					//added by Mike, 20181218
					processReferringDoctorTransactionCount(referringDoctorContainer, inputColumns, isConsultation); //edited by Mike, 20181219
*/			
					//added by Mike, 20181220
	//				processMedicalDoctorTransactionPerClassificationCount(classificationContainerPerMedicalDoctor, inputColumns, isConsultation);
	
					//added by Mike, 20190202
/*					processMedicalDoctorTransactionCount(medicalDoctorContainer, inputColumns, isConsultation);
*/
					//added by Mike, 20190226
					processHMOBilling(hmoBillingContainer, inputColumns, isConsultation);															
				}
				else {
/*					
					//added by Mike, 20181220
					processMedicalDoctorTransactionPerClassificationCount(classificationContainerPerMedicalDoctor, inputColumns, isConsultation);
*/					
				}
			}		
/*			
			//added by Mike, 20181205
			columnValuesArray[OUTPUT_DATE_ID_COLUMN] = i; 			
*/			
		}		

	}
	
/*	
	private static void resetNonHMOContainerCount() {
		//added by Mike, 20181220
		SortedSet<String> sortedNONHMOKeyset = new TreeSet<String>(nonHmoContainer.keySet());
		for (String key : sortedNONHMOKeyset) {	
			nonHmoContainer.get(key)[OUTPUT_NON_HMO_COUNT_COLUMN] = 0;
			nonHmoContainer.get(key)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN] = 0;
		}
	}
*/	

	//added by Mike, 20190128
	private static void consolidateKeysAndTheirHashMapValuesInContainer(HashMap<String, HashMap<String, double[]>> container) {
		SortedSet<String> sortedKeyset = new TreeSet<String>(container.keySet());
		SortedSet<String> sortedKeysetTwo = new TreeSet<String>(container.keySet());
						
		int threshold; //added by Mike, 20190127
	
		//At present, the key is the name of the Medical Doctor
		for (String key : sortedKeyset) {	
			for (String keyTwo : sortedKeysetTwo) {				
//				System.out.println(">>> Compare the Difference between Strings!");		
/*				System.out.println(myLevenshteinDistance.apply(key, keyTwo));
				System.out.println("key: "+key+" : keyTwo: "+keyTwo);
*/
				if (key.equals(keyTwo)) {
					continue;
				}

				threshold = 3; //Similar with the for Referring Medical Doctors, the numerical value should be less than 3.
								
				if (myLevenshteinDistance.apply(key, keyTwo)<threshold) {					
					SortedSet<String> sortedclassificationContainerPerMedicalDoctorTransactionCountKeyset = new TreeSet<String>(container.get(key).keySet());
		
					for (String classificationKey : sortedclassificationContainerPerMedicalDoctorTransactionCountKeyset) {
						//treatmentCount 
						container.get(key).get(classificationKey)[OUTPUT_HMO_COUNT_COLUMN] += container.get(keyTwo).get(classificationKey)[OUTPUT_HMO_COUNT_COLUMN];

						container.get(key).get(classificationKey)[OUTPUT_NON_HMO_COUNT_COLUMN] += container.get(keyTwo).get(classificationKey)[OUTPUT_NON_HMO_COUNT_COLUMN];
						
						//consultationCount
						container.get(key).get(classificationKey)[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN] += container.get(keyTwo).get(classificationKey)[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN];

						container.get(key).get(classificationKey)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN] += container.get(keyTwo).get(classificationKey)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN];

						//procedureCount
						container.get(key).get(classificationKey)[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN] += container.get(keyTwo).get(classificationKey)[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN]; 		

						container.get(key).get(classificationKey)[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN] += container.get(keyTwo).get(classificationKey)[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN]; 		

						//medicalCertificateCount
						container.get(key).get(classificationKey)[OUTPUT_CONSULTATION_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN] += container.get(keyTwo).get(classificationKey)[OUTPUT_CONSULTATION_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN]; 	

						container.get(key).get(classificationKey)[OUTPUT_CONSULTATION_NON_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN] += container.get(keyTwo).get(classificationKey)[OUTPUT_CONSULTATION_NON_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN]; 	

						//newPatientReferralTransactionCount
						container.get(key).get(classificationKey)[OUTPUT_HMO_NEW_PATIENT_COUNT_COLUMN] += container.get(keyTwo).get(classificationKey)[OUTPUT_HMO_NEW_PATIENT_COUNT_COLUMN]; 	

						container.get(key).get(classificationKey)[OUTPUT_NON_HMO_NEW_PATIENT_COUNT_COLUMN] += container.get(keyTwo).get(classificationKey)[OUTPUT_NON_HMO_NEW_PATIENT_COUNT_COLUMN]; 	
					}
					
					container.remove(keyTwo);
					consolidateKeysAndTheirHashMapValuesInContainer(container);
					return;
				}
			}
		}
	}

	//added by Mike, 20190126
	private static void consolidateKeysAndTheirValuesInContainer(HashMap<String, double[]> container, int containerType) {
		SortedSet<String> sortedKeyset = new TreeSet<String>(container.keySet());
		SortedSet<String> sortedKeysetTwo = new TreeSet<String>(container.keySet());

		int threshold; //added by Mike, 20190127
		
		for (String key : sortedKeyset) {	
			//added by Mike, 20190127
			if (containerType==HMO_CLASSIFICATION_CONTAINER_PER_MEDICAL_DOCTOR_CONTAINER_TYPE) {				
				if (!key.contains("HMO")) {
/*					System.out.println("Not HMO");
					System.out.println("key: "+key);
*/					
					continue;
				}
			}

			for (String keyTwo : sortedKeysetTwo) {				
//				System.out.println(">>> Compare the Difference between Strings!");		
/*				System.out.println(myLevenshteinDistance.apply(key, keyTwo));
				System.out.println("key: "+key+" : keyTwo: "+keyTwo);
*/
				if (key.equals(keyTwo)) {
					continue;
				}

				//compare the two key strings; if the result is a numerical value that is less than 2, combine the two 
				//Note: We use less than 2, so that "MEDOCARE", with the "MEDO", and MEDICARE, with the "MEDI", are recognized by the add-on software as distinct.
				threshold = 2; //default value
				if (containerType==REFERRING_DOCTOR_CONTAINER_TYPE) { //In this case, the numerical value should be less than 3.
					threshold = 3;
				}
								
				if (myLevenshteinDistance.apply(key, keyTwo)<threshold) {
					switch (containerType) {
						case HMO_CONTAINER_TYPE:
						case HMO_CLASSIFICATION_CONTAINER_PER_MEDICAL_DOCTOR_CONTAINER_TYPE:
		/*					
							System.out.println(myLevenshteinDistance.apply(key, keyTwo));
							System.out.println("key: "+key+" : keyTwo: "+keyTwo);
							System.out.println("container.get(key)[OUTPUT_HMO_COUNT_COLUMN]: "+container.get(key)[OUTPUT_HMO_COUNT_COLUMN]);
							System.out.println("container.get(keyTwo)[OUTPUT_HMO_COUNT_COLUMN]: "+container.get(keyTwo)[OUTPUT_HMO_COUNT_COLUMN]);
		*/					
							//treatmentCount 
							container.get(key)[OUTPUT_HMO_COUNT_COLUMN] += container.get(keyTwo)[OUTPUT_HMO_COUNT_COLUMN];
		/*
							System.out.println("container.get(key)[OUTPUT_HMO_COUNT_COLUMN]: "+container.get(key)[OUTPUT_HMO_COUNT_COLUMN]);
		*/					
							//consultationCount
							container.get(key)[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN] += container.get(keyTwo)[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN];

							//procedureCount
							container.get(key)[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN] += container.get(keyTwo)[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN]; 		

							//medicalCertificateCount
							container.get(key)[OUTPUT_CONSULTATION_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN] += container.get(keyTwo)[OUTPUT_CONSULTATION_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN]; 	

							container.remove(keyTwo);
							consolidateKeysAndTheirValuesInContainer(container, containerType);
							return;
						case NON_HMO_CONTAINER_TYPE:
						case NON_HMO_CLASSIFICATION_CONTAINER_PER_MEDICAL_DOCTOR_CONTAINER_TYPE:
		/*					
							System.out.println(myLevenshteinDistance.apply(key, keyTwo));
							System.out.println("key: "+key+" : keyTwo: "+keyTwo);
							System.out.println("container.get(key)[OUTPUT_HMO_COUNT_COLUMN]: "+container.get(key)[OUTPUT_HMO_COUNT_COLUMN]);
							System.out.println("container.get(keyTwo)[OUTPUT_HMO_COUNT_COLUMN]: "+container.get(keyTwo)[OUTPUT_HMO_COUNT_COLUMN]);
		*/					
							//treatmentCount 
							container.get(key)[OUTPUT_NON_HMO_COUNT_COLUMN] += container.get(keyTwo)[OUTPUT_NON_HMO_COUNT_COLUMN];
		/*
							System.out.println("container.get(key)[OUTPUT_HMO_COUNT_COLUMN]: "+container.get(key)[OUTPUT_HMO_COUNT_COLUMN]);
		*/					
							//consultationCount
							container.get(key)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN] += container.get(keyTwo)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN];

							//procedureCount
							container.get(key)[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN] += container.get(keyTwo)[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN]; 		

							//medicalCertificateCount
							container.get(key)[OUTPUT_CONSULTATION_NON_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN] += container.get(keyTwo)[OUTPUT_CONSULTATION_NON_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN]; 	

							container.remove(keyTwo);
							consolidateKeysAndTheirValuesInContainer(container, containerType);
							return;
						case REFERRING_DOCTOR_CONTAINER_TYPE:
							//treatmentCount 
							container.get(key)[OUTPUT_HMO_COUNT_COLUMN] += container.get(keyTwo)[OUTPUT_HMO_COUNT_COLUMN];

							container.get(key)[OUTPUT_NON_HMO_COUNT_COLUMN] += container.get(keyTwo)[OUTPUT_NON_HMO_COUNT_COLUMN];
							
							//consultationCount
							container.get(key)[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN] += container.get(keyTwo)[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN];

							container.get(key)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN] += container.get(keyTwo)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN];

							//procedureCount
							container.get(key)[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN] += container.get(keyTwo)[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN]; 		

							container.get(key)[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN] += container.get(keyTwo)[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN]; 		

							//medicalCertificateCount
							container.get(key)[OUTPUT_CONSULTATION_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN] += container.get(keyTwo)[OUTPUT_CONSULTATION_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN]; 	

							container.get(key)[OUTPUT_CONSULTATION_NON_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN] += container.get(keyTwo)[OUTPUT_CONSULTATION_NON_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN]; 	

							//newPatientReferralTransactionCount
							container.get(key)[OUTPUT_HMO_NEW_PATIENT_COUNT_COLUMN] += container.get(keyTwo)[OUTPUT_HMO_NEW_PATIENT_COUNT_COLUMN]; 	

							container.get(key)[OUTPUT_NON_HMO_NEW_PATIENT_COUNT_COLUMN] += container.get(keyTwo)[OUTPUT_NON_HMO_NEW_PATIENT_COUNT_COLUMN]; 	
							
							container.remove(keyTwo);
							consolidateKeysAndTheirValuesInContainer(container, containerType);
							return;
					}
				}
			}
		}			
//		return container;
	}

	private static void processContainers() {
		myLevenshteinDistance = new LevenshteinDistance();
		consolidateKeysAndTheirValuesInContainer(hmoContainer, HMO_CONTAINER_TYPE);
		
		//This method below is at present not useful given that there are NON-HMO names whose length is only 2 characters.
		//Thus, NON-HMO's that shouldn't be combined, e.g. "SC" and "NC" (No Charge), are combined.
		//As a workaround, we can, however, use NON-HMO names whose length is longer than 2 characters
/*		consolidateKeysAndTheirValuesInContainer(nonHmoContainer, NON_HMO_CONTAINER_TYPE);
*/
		//added by Mike, 20190127
		consolidateKeysAndTheirValuesInContainer(referringDoctorContainer, REFERRING_DOCTOR_CONTAINER_TYPE);

		//added by Mike, 20190127
		SortedSet<String> sortedclassificationContainerPerMedicalDoctorTransactionCountKeyset = new TreeSet<String>(classificationContainerPerMedicalDoctor.keySet());
		
		for (String key : sortedclassificationContainerPerMedicalDoctorTransactionCountKeyset) {	
			System.out.println(">>>> key: "+key);
			consolidateKeysAndTheirValuesInContainer(classificationContainerPerMedicalDoctor.get(key), HMO_CLASSIFICATION_CONTAINER_PER_MEDICAL_DOCTOR_CONTAINER_TYPE);
/*			consolidateKeysAndTheirValuesInContainer(classificationContainerPerMedicalDoctor.get(key), NON_HMO_CLASSIFICATION_CONTAINER_PER_MEDICAL_DOCTOR_CONTAINER_TYPE);
*/
		}

		consolidateKeysAndTheirHashMapValuesInContainer(classificationContainerPerMedicalDoctor);
		
//		System.out.println(">>> Compare the Difference between Strings!");		
//		System.out.println(myLevenshteinDistance.apply("1234567890", "1")); //answer: 9
	
//		hmoContainer = new HashMap<String, double[]>();
//		nonHmoContainer = new HashMap<String, double[]>();
//		referringDoctorContainer = new HashMap<String, double[]>();
////		medicalDoctorContainer = new HashMap<String, double[]>();
//		classificationContainerPerMedicalDoctor = new HashMap<String, HashMap<String, double[]>>();								
	}
}