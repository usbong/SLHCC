/*
 * Copyright 2018~2020 Usbong Social Systems, Inc.
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

//added by Mike, 20200918
//TO-DO: -delete: excess notes and instructions 
 
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
' 1) Auto-generated Monthly PT Treatment Report
' --> "Tab delimited" .txt file 
' --> Regardless of the name of the input file or input files, the output file will be "MonthlySummaryReportOutput.txt".
' --> The Report includes: 
' --> a) NON-HMO (net)
' --> b) HMO & Sta. Lucia Reality/SLR (net)
' --> c) TOTAL (net)
' --> d) HMO & Sta. Lucia Reality/SLR (net) : PAID (Php)
' --> e) HMO & Sta. Lucia Reality/SLR (net) : UNPAID (Php)
' --> f) HMO & Sta. Lucia Reality/SLR (net) : TOTAL (Php)
'
' Notes:
' 1) To execute the add-on software/application simply use the following command:
'   java generateMonthlyTreatmentSummaryReportOfAllInputFilesFromMasterList input201801.txt
' 
' where: "input201801.txt" is the name of the file.
' 
' 2) To execute a set of input files, e.g. input201801.txt, input201802.txt, you can use the following command: 
'  java generateMonthlyTreatmentSummaryReportOfAllInputFilesFromMasterList input*
'
' where: "input*" means any file in the directory that starts with "input".
*/ 

public class generateMonthlyPaymentSummaryReportOfAllInputFilesFromMasterList {	
	private static boolean isInDebugMode = true;
	private static String inputFilename = "input201801"; //without extension; default input file
	
	private static String startDate = null;
	private static String endDate = null;
	
	private static final int offset = 1;
	
	private static final int INPUT_REFERRING_DOCTOR_COLUMN = 15;
	private static final int INPUT_MEDICAL_DOCTOR_COLUMN = 15+offset; //added by Mike, 20190603
	private static final int INPUT_NOTES_COLUMN = 0;
	private static final int INPUT_DATE_COLUMN = 1;
	private static final int INPUT_CLASS_COLUMN = 8; //HMO and NON-HMO
	private static final int INPUT_NET_PF_COLUMN = 10;

/*	private static HashMap<String, double[]> referringDoctorContainer;	
*/
	private static HashMap<Integer, double[]> dateContainer;	//added by Mike, 201801205

	//added by Mike, 20190531
	private static HashMap<Integer, Integer[]> treatmentMonthlyPaymentSummaryContainer; 
	private static HashMap<Integer, Integer[]> consultationMonthlyPaymentSummaryContainer; 
	private static HashMap<Integer, Integer[]> procedureMonthlyPaymentSummaryContainer; 
	
//	private static ArrayList<Integer> yearsContainerArrayList; //added by Mike, 20190531

	private static double[] columnValuesArray;
	private static String[] dateValuesArray; //added by Mike, 20180412
	private static int[] dateValuesArrayInt; //added by Mike, 20181206
		
	//the date and the referring doctor are not yet included here
	//this is for both HMO and NON-HMO transactions
	private static final int OUTPUT_TOTAL_COLUMNS = 16; //9; //edited by Mike, 20190603 
	
	private static final int OUTPUT_HMO_TREATMENT_COUNT_COLUMN = 0; //COUNT
	private static final int OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN = 1;
	private static final int OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN = 2;
	private static final int OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN = 3;

	private static final int OUTPUT_NON_HMO_TREATMENT_COUNT_COLUMN = 4; //COUNT
	private static final int OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN = 5;
	private static final int OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN = 6;
	private static final int OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN = 7;

	//edited by Mike, 20190603
	//private static final int OUTPUT_DATE_ID_COLUMN = 8; //added by Mike, 20181205

	//added by Mike, 20190603
	private static final int OUTPUT_CONSULTATION_NOT_TREATMENT_OFFSET = 8;
	
	//added by Mike, 20190603
	private static final int OUTPUT_HMO_CONSULTATION_COUNT_COLUMN = 8; //COUNT
	private static final int OUTPUT_HMO_TOTAL_NET_CONSULTATION_FEE_COLUMN = 9;
	private static final int OUTPUT_HMO_PAID_NET_CONSULTATION_FEE_COLUMN = 10;
	private static final int OUTPUT_HMO_UNPAID_NET_CONSULTATION_FEE_COLUMN = 11;

	private static final int OUTPUT_NON_HMO_CONSULTATION_COUNT_COLUMN = 12; //COUNT
	private static final int OUTPUT_NON_HMO_TOTAL_NET_CONSULTATION_FEE_COLUMN = 13;
	private static final int OUTPUT_NON_HMO_PAID_NET_CONSULTATION_FEE_COLUMN = 14;
	private static final int OUTPUT_NON_HMO_UNPAID_NET_CONSULTATION_FEE_COLUMN = 15;

	
	private static boolean isConsultation; //added by Mike, 20190106

	//To include the zero (0) in the hundredths place, e.g. 432.10, we use "0.00", instead of "#.##"
	private static DecimalFormat df = new DecimalFormat("0.00");//("#.##"); //added by Mike, 20181105; edited by Mike, 20190602
	private static int rowCount; //added by Mike, 20181105
				
	private static int totalCountForAllReferringDoctors;
	private static double totalNetTreatmentFeeForAllReferringDoctors;
	private static double totalPaidNetTreatmentFeeForAllReferringDoctors;
	private static double totalUnpaidNetTreatmentFeeForAllReferringDoctors;
	private static double totalFivePercentShareOfNetPaidForAllReferringDoctors;
				
	//added by Mike, 20190531
	//Note that I have to use double backslash, i.e. "\\", to use "\" in the filename
	//without extension; default input file 
//	private static String inputOutputTemplateFilenameMonthlyPaymentSummary = "assets\\templates\\generateMonthlyPaymentSummaryReportOutputTemplate";
  //Linux Machine
	private static String inputOutputTemplateFilenameMonthlyPaymentSummary = "./assets/templates/generateMonthlyPaymentSummaryReportOutputTemplate";

	//added by Mike, 20190504
	private static final int TREATMENT_FILE_TYPE = 0;
	private static final int CONSULTATION_FILE_TYPE = 1;
	private static final int PROCEDURE_FILE_TYPE = 2;
	
	//added by Mike, 20190531
	private static boolean isConsultationInputFileEmpty=true;
	private static boolean isTreatmentInputFileEmpty=true;
	
	public static void main ( String[] args ) throws Exception
	{			
		makeFilePath("output"); //"output" is the folder where I've instructed the add-on software/application to store the output file			
/*		//edited by Mike, 20190604		
		PrintWriter writer = new PrintWriter("output/TreatmentMonthlyPaymentSummaryReportOutput.txt", "UTF-8");
*/
		//added by Mike, 20190531
		PrintWriter MonthlyPaymentSummaryTreatmentWriter = new PrintWriter("output/MonthlyPaymentSummaryTreatment.html", "UTF-8");	

		//added by Mike, 20190603
		PrintWriter MonthlyPaymentSummaryConsultationWriter = new PrintWriter("output/MonthlyPaymentSummaryConsultation.html", "UTF-8");	
		
		
		/*referringDoctorContainer = new HashMap<String, double[]>();
		*/
		
		dateContainer = new HashMap<Integer, double[]>();
		
		//added by Mike, 20181116
		startDate = null; //properly set the month and year in the output file of each input file
		dateValuesArray = new String[args.length]; //added by Mike, 20180412
		dateValuesArrayInt = new int[args.length]; //added by Mike, 20180412
				
		//added by Mike, 20190531
		treatmentMonthlyPaymentSummaryContainer = new HashMap<Integer, Integer[]>(); 
		consultationMonthlyPaymentSummaryContainer = new HashMap<Integer, Integer[]>();
		procedureMonthlyPaymentSummaryContainer  = new HashMap<Integer, Integer[]>();

		//edited by Mike, 20181030
		for (int i=0; i<args.length; i++) {						
			//added by Mike, 20181030
			inputFilename = args[i].replaceAll(".txt","");			
			File f = new File(inputFilename+".txt");

			System.out.println("inputFilename: " + inputFilename);
			
			//added by Mike, 20181206
			//edited by Mike, 20190106
			if (dateValuesArrayInt[i]==0) {
				dateValuesArrayInt[i] = Integer.parseInt(args[i].substring(args[i].indexOf("_")+1,args[i].indexOf(".txt")));
			}
			
			if (inputFilename.toLowerCase().contains("consultation")) {
				isConsultation=true;
//				continue; //edited by Mike, 20190603
			}
			else {
				isConsultation=false;
			}

			
			Scanner sc = new Scanner(new FileInputStream(f));				
		
			String s;
			
			//added by Mike, 20200916
			if (!sc.hasNext()) {
				columnValuesArray = new double[OUTPUT_TOTAL_COLUMNS];			
			
				//added by Mike, 20200918
				//blank file
				System.out.println(">>>"+dateValuesArrayInt[i]);
				dateContainer.put(dateValuesArrayInt[i], columnValuesArray);
			
				continue;
			}
					
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

				//added by Mike, 20190426
				if (inputFilename.toLowerCase().contains("consultation")) {
					isConsultationInputFileEmpty=false;
				}
				else if (inputFilename.toLowerCase().contains("treatment")) {
//					System.out.println(">>>dateValuesArray[i]: "+dateValuesArray[i]);
					isTreatmentInputFileEmpty=false;
				}

								
				if (isInDebugMode) {
					rowCount++;
					System.out.println("rowCount: "+rowCount);
				}
				
				//added by Mike, 20181121
				//skip transactions that have "RehabSupplies" as its "CLASS" value
				//In Excel logbook/workbook 2018 onwards, such transactions are not included in the Consultation and PT Treatment Excel logbooks/workbooks.
				if (inputColumns[INPUT_CLASS_COLUMN].contains("RehabSupplies")) {
					continue;
				}

				if (isConsultation) {
					//added by Mike, 20190603
					processConsultationTransaction(inputColumns, i);						
				}
				else {
					//edited by Mike, 20190603
					processTreatmentTransaction(inputColumns, i);						
				}				
			}			
/*			
			//added by Mike, 20181205
			columnValuesArray[OUTPUT_DATE_ID_COLUMN] = i; 			
*/			
		}
		
		/*
		 * --------------------------------------------------------------------
		 * OUTPUT
		 * --------------------------------------------------------------------
		*/
		//added by Mike, 20190531
/*		if (!isConsultationInputFileEmpty) {
			processWriteOutputFileConsultation(consultationWriter);
		}
		else {
			System.out.println("\nThere is no Tab-delimited .txt input file in the \"input\\consultation\" folder.\n");
		}
*/
		
		processWriteOutputFileMonthlyPaymentSummary(MonthlyPaymentSummaryTreatmentWriter, TREATMENT_FILE_TYPE);
		
		processWriteOutputFileMonthlyPaymentSummary(MonthlyPaymentSummaryConsultationWriter, CONSULTATION_FILE_TYPE);
		
		if ((isTreatmentInputFileEmpty) && (isConsultationInputFileEmpty)) {
			System.out.println("\nThere is no Tab-delimited .txt input file in the \"input\\treatment\" folder.\n");
			return;
		}
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
	
	//added by Mike, 20190531
	private static void processWriteOutputFileMonthlyPaymentSummary(PrintWriter writer, int fileType) throws Exception {		
//		File inputDataFile = new File(inputDataFilenameTreatmentMonthlyStatistics+".txt");	
		File f = new File(inputOutputTemplateFilenameMonthlyPaymentSummary+".html");

//		System.out.println("inputOutputTemplateFilenameMonthlyStatistics: " + inputOutputTemplateFilenameMonthlyStatistics);
		
		Scanner sc = new Scanner(new FileInputStream(f), "UTF-8");				
	
		String s;		
		//removed by Mike, 20200918
//			s=sc.nextLine(); //skip the first row, which is the input file's table headers

		if (isInDebugMode) {
			rowCount=0;
		}

		//edited by Mike, 20190603
		//boolean hasWrittenAutoCalculatedValue=false;
		
		//added by Mike, 20190603
		int offset = 0;


		//count/compute the number-based values of inputColumns 
		while (sc.hasNextLine()) {
			s=sc.nextLine();
/*			
			//if the row is blank
			if (s.trim().equals("")) {
				continue;
			}
*/			
			if (isInDebugMode) {
				rowCount++;
//				System.out.println("rowCount: "+rowCount);
			}
			
//			s = s.replace("<?php echo $data['date'];?>", "" + dateValue.toUpperCase());

			//added by Mike, 20190504
			if (s.contains("<!-- FILE TYPE  -->")) {
				String fileTypeString = "";
				switch (fileType) {
					case TREATMENT_FILE_TYPE:
						fileTypeString = "TREATMENT";
						break;
					case CONSULTATION_FILE_TYPE:
						fileTypeString = "CONSULTATION";
						offset = OUTPUT_CONSULTATION_NOT_TREATMENT_OFFSET;
						
//						System.out.println(">>>> CONSULTATION" + offset);
						break;
					default:// PROCEDURE_FILE_TYPE:
						fileTypeString = "PROCEDURE";
						break;
				}			
				s = s.concat("\n");
				s = s.concat(fileTypeString+"\n");
			}			
						
						
//			System.out.println(">>>>" + offset);
						
			if (s.contains("<!-- DATE VALUE Column -->")) {
				s = s.concat("\n");
				s = s.concat("\t\t\t<!-- DATE: Column 1 -->\n");
				s = s.concat("\t\t\t<td colspan=\"1\">\n");
				s = s.concat("\t\t\t\t<div class=\"date\"><b><span class=\"transaction_type_column_header\">DATE:</span></b></div>\n");
				s = s.concat("\t\t\t</td>\n");
				
				//edited by Mike, 20190603; edited again by Mike, 20200916
				for(int i=0; i<dateValuesArrayInt.length; i++) {
				//TO-DO: -update: this
//				for(int i=0; i<dateValuesArrayInt.length/2; i++) {
					int dateKey = dateValuesArrayInt[i];
					s = s.concat("\n");
					s = s.concat("\t\t\t<!-- DATE "+dateKey+": Column 1 -->\n");
					s = s.concat("\t\t\t<td colspan=\"1\">\n");
					s = s.concat("\t\t\t\t<div class=\"date\"><b><span>"+dateKey+"</span></b></div>\n");
					s = s.concat("\t\t\t</td>\n");
//						System.out.println("yearKey: "+yearKey);
//						System.out.println(i+": "+inputMonthRowYearColumns[i+1]);					
				}
				//s = s.concat("\n");			
				
				s = s.concat("\n");				
				s = s.concat("\t\t\t<!-- TOTAL: Column 1 -->\n");
				s = s.concat("\t\t\t<td colspan=\"1\">\n");
				s = s.concat("\t\t\t\t<b><span>TOTAL</span></b>\n");
				s = s.concat("\t\t\t</td>\n");
			}

			if (s.contains("<!-- TRANSACTION TYPE AND VALUE Rows -->")) {		
				s = s.concat("\n");
				
				//--------------------------------------------------------------------
				//CASH payment transactions
				s = s.concat("\t\t\t<tr>\n");
				s = s.concat("\t\t\t<td>\n");				
				s = s.concat("\t\t\t\t<div class=\"transaction_type_column\"><b><span>Cash (net) : TOTAL (PHP)</span></b></div>\n"); 		
				s = s.concat("\t\t\t</td>\n");

				s = autoWriteValuesInRowForAllDateColumns(s, writer, OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN+offset);
				s = s.concat("\t\t\t</tr>\n");
				//--------------------------------------------------------------------
				
				s = s.concat("\t\t\t<tr>\n");
				s = s.concat("\t\t\t<td>\n");				
				s = s.concat("\t\t\t\t<b><div class=\"transaction_type_column\"><span>Cash (net) : PAID (PHP)</span></b></div>"); 		
				s = s.concat("\t\t\t</td>\n");

				s = autoWriteValuesInRowForAllDateColumns(s, writer, OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN+offset);
				s = s.concat("\t\t\t</tr>\n");
				//--------------------------------------------------------------------
				
				s = s.concat("\t\t\t<tr>\n");
				s = s.concat("\t\t\t<td>\n");				
				s = s.concat("\t\t\t\t<div class=\"transaction_type_column\"><b><span>Cash (net) : UNPAID (PHP)</span></b></div>"); 		
				s = s.concat("\t\t\t</td>\n");

				s = autoWriteValuesInRowForAllDateColumns(s, writer, OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN+offset);
				s = s.concat("\t\t\t</tr>\n");
				//--------------------------------------------------------------------
				
				s = s.concat("\t\t\t<tr>\n");
				s = s.concat("\t\t\t<td>\n");				
				s = s.concat("\t\t\t\t<div class=\"transaction_type_column\"><b><span>Cash (net) : COUNT</span></b></div>"); 		
				s = s.concat("\t\t\t</td>\n");

				s = autoWriteValuesInRowForAllDateColumns(s, writer, OUTPUT_NON_HMO_TREATMENT_COUNT_COLUMN+offset);
				s = s.concat("\t\t\t</tr>\n");
				//--------------------------------------------------------------------
				//space
				s = s.concat("\t\t\t<tr>\n");
				s = s.concat("\t\t\t<td>\n");				
				s = s.concat("\t\t\t<div><br /></div>\n");
				s = s.concat("\t\t\t</td>\n");				
				s = s.concat("\t\t\t</tr>\n");
				//--------------------------------------------------------------------
				//HMO payment transactions
				s = s.concat("\t\t\t<tr>\n");
				s = s.concat("\t\t\t<td>\n");				
				s = s.concat("\t\t\t\t<div class=\"transaction_type_column\"><b><span>HMO (net) : TOTAL (PHP)</span></b></div>"); 		
				s = s.concat("\t\t\t</td>\n");

				s = autoWriteValuesInRowForAllDateColumns(s, writer, OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN+offset);
				s = s.concat("\t\t\t</tr>\n");
				//--------------------------------------------------------------------

				s = s.concat("\t\t\t<tr>\n");
				s = s.concat("\t\t\t<td>\n");				
				s = s.concat("\t\t\t\t<div class=\"transaction_type_column\"><b><span>HMO (net) : PAID (PHP)</span></b></div>"); 		
				s = s.concat("\t\t\t</td>\n");

				s = autoWriteValuesInRowForAllDateColumns(s, writer, OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN+offset);
				s = s.concat("\t\t\t</tr>\n");
				//--------------------------------------------------------------------

				s = s.concat("\t\t\t<tr>\n");
				s = s.concat("\t\t\t<td>\n");				
				s = s.concat("\t\t\t\t<div class=\"transaction_type_column\"><b><span>HMO (net) : UNPAID (PHP)</span></b></div>"); 		
				s = s.concat("\t\t\t</td>\n");

				s = autoWriteValuesInRowForAllDateColumns(s, writer, OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN+offset);
				s = s.concat("\t\t\t</tr>\n");
				//--------------------------------------------------------------------

				s = s.concat("\t\t\t<tr>\n");
				s = s.concat("\t\t\t<td>\n");				
				s = s.concat("\t\t\t\t<div class=\"transaction_type_column\"><b><span>HMO (net) : COUNT</span></b></div>"); 		
				s = s.concat("\t\t\t</td>\n");

				s = autoWriteValuesInRowForAllDateColumns(s, writer, OUTPUT_HMO_TREATMENT_COUNT_COLUMN+offset);
				s = s.concat("\t\t\t</tr>\n");
				
				//added by Mike, 20190602
				//--------------------------------------------------------------------
				//space
				s = s.concat("\t\t\t<tr>\n");
				s = s.concat("\t\t\t<td>\n");				
				s = s.concat("\t\t\t<div><br /></div>\n");
				s = s.concat("\t\t\t</td>\n");				
				s = s.concat("\t\t\t</tr>\n");
				//--------------------------------------------------------------------
				//CASH and HMO payment transactions
				s = s.concat("\t\t\t<tr>\n");
				s = s.concat("\t\t\t<td>\n");				
				s = s.concat("\t\t\t\t<div class=\"transaction_type_column\"><b><span>CASH and HMO (net) : TOTAL (PHP)</span></b></div>"); 		
				s = s.concat("\t\t\t</td>\n");
									
				s = autoWriteValuesInRowForAllDateColumns(s, writer, OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN+offset, OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN+offset);
				s = s.concat("\t\t\t</tr>\n");
				//--------------------------------------------------------------------

				s = s.concat("\t\t\t<tr>\n");
				s = s.concat("\t\t\t<td>\n");				
				s = s.concat("\t\t\t\t<div class=\"transaction_type_column\"><b><span>CASH and HMO (net) : PAID (PHP)</span></b></div>"); 		
				s = s.concat("\t\t\t</td>\n");

				s = autoWriteValuesInRowForAllDateColumns(s, writer, OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN+offset, OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN+offset);
				s = s.concat("\t\t\t</tr>\n");
				//--------------------------------------------------------------------

				s = s.concat("\t\t\t<tr>\n");
				s = s.concat("\t\t\t<td>\n");				
				s = s.concat("\t\t\t\t<div class=\"transaction_type_column\"><b><span>CASH and HMO (net) : UNPAID (PHP)</span></b></div>"); 		
				s = s.concat("\t\t\t</td>\n");

				s = autoWriteValuesInRowForAllDateColumns(s, writer, OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN+offset, OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN+offset);
				s = s.concat("\t\t\t</tr>\n");
				//--------------------------------------------------------------------

				s = s.concat("\t\t\t<tr>\n");
				s = s.concat("\t\t\t<td>\n");				
				s = s.concat("\t\t\t\t<div class=\"transaction_type_column\"><b><span>CASH and HMO (net) : COUNT</span></b></div>"); 		
				s = s.concat("\t\t\t</td>\n");

				s = autoWriteValuesInRowForAllDateColumns(s, writer, OUTPUT_HMO_TREATMENT_COUNT_COLUMN+offset, OUTPUT_NON_HMO_TREATMENT_COUNT_COLUMN+offset);
				s = s.concat("\t\t\t</tr>\n");
			}
			writer.print(s + "\n");		
			
		}
		
		writer.close();
	}

	//added by Mike, 20190604
	//Note that the output .txt file is Tab-delimited.
	//At present, we do not use this method. 
	//TO-DO: -update: this to also be for Consultation transactions
	private static void processWriteOutputFileMonthlyPaymentSummaryAsTxtFile(PrintWriter writer, int fileType) throws Exception {		
		//added by Mike, 20181118
		writer.print("Cash and HMO PT TREATMENT Monthly Summary Report\n");

		//init table header names
		writer.print("DATE:\t"); //"PT TREATMENT:" column

		//do not include input files that are Consultation transactions 
		for(int i=0; i<dateValuesArrayInt.length; i++) { //edited by Mike, 20190106
			writer.print(dateValuesArrayInt[i]+"\t"); //"PT TREATMENT:" column
		}

		//added by Mike, 20190521
		writer.print("TOTAL"+"\t"); 
		
		//--------------------------------------------------------------------
		writer.print("\nCash (net) : TOTAL (Php)"); 		

		SortedSet<Integer> sortedKeyset = new TreeSet<Integer>(dateContainer.keySet());

		//added by Mike, 20190521
		double rowTotal = 0;
		
		for (Integer key : sortedKeyset) {	
			writer.print( 
							"\t" + dateContainer.get(key)[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN]
							); 				   							
		
			//added by Mike, 20190521
			rowTotal += dateContainer.get(key)[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN];
		}

		//added by Mike, 20190521
		writer.print("\t" + rowTotal); 		

		writer.print("\nCash (net) : PAID (Php)"); 		

		//added by Mike, 20190521
		rowTotal = 0;
		
		for (Integer key : sortedKeyset) {	
			writer.print( 
							"\t" + dateContainer.get(key)[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN]
							); 				   							

			//added by Mike, 20190521
			rowTotal += dateContainer.get(key)[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN];
		}
		//added by Mike, 20190521
		writer.print("\t" + rowTotal); 		

		writer.print("\nCash (net) : UNPAID (Php)"); 		

		//added by Mike, 20190521
		rowTotal = 0;
		
		for (Integer key : sortedKeyset) {	
			writer.print( 
							"\t" + dateContainer.get(key)[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN]
							); 				   							

			//added by Mike, 20190521
			rowTotal += dateContainer.get(key)[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN];
		}
		//added by Mike, 20190521
		writer.print("\t" + rowTotal); 		
		
		writer.print("\nCash (net) : COUNT"); 		

		//added by Mike, 20190521
		rowTotal = 0;
		
		for (Integer key : sortedKeyset) {	
			writer.print( 
							"\t" + dateContainer.get(key)[OUTPUT_NON_HMO_TREATMENT_COUNT_COLUMN]
							); 				   							

			//added by Mike, 20190521
			rowTotal += dateContainer.get(key)[OUTPUT_NON_HMO_TREATMENT_COUNT_COLUMN];
		}
		//added by Mike, 20190521
		writer.print("\t" + rowTotal); 		

		writer.print("\n"); //blank row 				
		writer.print("\nHMO (net) : TOTAL (Php)"); 		

		//added by Mike, 20190521
		rowTotal = 0;
		
		for (Integer key : sortedKeyset) {	
			writer.print( 
							"\t" + dateContainer.get(key)[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN]
							); 				   							

			//added by Mike, 20190521
			rowTotal += dateContainer.get(key)[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN];
		}
		//added by Mike, 20190521
		writer.print("\t" + rowTotal); 		

		writer.print("\nHMO (net) : PAID (Php)"); 		

		//added by Mike, 20190521
		rowTotal = 0;
		
		for (Integer key : sortedKeyset) {	
			writer.print( 
							"\t" + dateContainer.get(key)[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN]
							); 				   							

			//added by Mike, 20190521
			rowTotal += dateContainer.get(key)[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN];
		}
		//added by Mike, 20190521
		writer.print("\t" + rowTotal); 		

		writer.print("\nHMO (net) : UNPAID (Php)"); 		

		//added by Mike, 20190521
		rowTotal = 0;
		
		for (Integer key : sortedKeyset) {	
			writer.print( 
							"\t" + dateContainer.get(key)[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN]
							); 				   							
			
			//added by Mike, 20190521
			rowTotal += dateContainer.get(key)[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN];
		}
		//added by Mike, 20190521
		writer.print("\t" + rowTotal); 		
		
		writer.print("\nHMO (net) : COUNT"); 		

		//added by Mike, 20190521
		rowTotal = 0;
		
		for (Integer key : sortedKeyset) {	
			writer.print( 
							"\t" + dateContainer.get(key)[OUTPUT_HMO_TREATMENT_COUNT_COLUMN]
							); 				   							
			
			//added by Mike, 20190521
			rowTotal += dateContainer.get(key)[OUTPUT_HMO_TREATMENT_COUNT_COLUMN];
		}
		//added by Mike, 20190521
		writer.print("\t" + rowTotal); 		

		writer.print("\n"); //blank row
		writer.print("\nCash and HMO (net) : TOTAL (Php)"); 		

		//added by Mike, 20190521
		rowTotal = 0;

		for (Integer key : sortedKeyset) {	
			double count = dateContainer.get(key)[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] + dateContainer.get(key)[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN];
		
			writer.print( 
							"\t" + count
							); 				   							

			//added by Mike, 20190521
			rowTotal += (dateContainer.get(key)[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] + dateContainer.get(key)[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN]);
		}
		//added by Mike, 20190521
		writer.print("\t" + rowTotal); 		

		writer.print("\nCash and HMO (net) : PAID (Php)"); 		

		//added by Mike, 20190521
		rowTotal = 0;

		for (Integer key : sortedKeyset) {	
			double count = dateContainer.get(key)[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN] + dateContainer.get(key)[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN];
		
			writer.print( 
							"\t" + count
							); 				   							
							
			//added by Mike, 20190521
			rowTotal += (dateContainer.get(key)[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN] + dateContainer.get(key)[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN]);		
		}
		//added by Mike, 20190521
		writer.print("\t" + rowTotal); 		

		writer.print("\nCash and HMO (net) : UNPAID (Php)"); 		

		//added by Mike, 20190521
		rowTotal = 0;

		for (Integer key : sortedKeyset) {	
			double count = dateContainer.get(key)[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] + dateContainer.get(key)[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN];
		
			writer.print( 
							"\t" + count
							); 				   							

			//added by Mike, 20190521
			rowTotal += (dateContainer.get(key)[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] + dateContainer.get(key)[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN]);		
		}
		//added by Mike, 20190521
		writer.print("\t" + rowTotal); 		

		writer.print("\nCash and HMO (net) : COUNT"); 		

		//added by Mike, 20190521
		rowTotal = 0;

		for (Integer key : sortedKeyset) {	
			double count = dateContainer.get(key)[OUTPUT_HMO_TREATMENT_COUNT_COLUMN] + dateContainer.get(key)[OUTPUT_NON_HMO_TREATMENT_COUNT_COLUMN];
		
			writer.print( 
							"\t" + count
							); 				   							

			//added by Mike, 20190521
			rowTotal += (dateContainer.get(key)[OUTPUT_HMO_TREATMENT_COUNT_COLUMN] + dateContainer.get(key)[OUTPUT_NON_HMO_TREATMENT_COUNT_COLUMN]);		
		}

		//added by Mike, 20190521
		writer.print("\t" + rowTotal); 		
		
		writer.close();
	}
	
	//added by Mike, 20190601; edited by Mike, 20190602
	private static String autoWriteValuesInRowForAllDateColumns(String s, PrintWriter writer, int columnIndexOne, int columnIndexTwo) {
		SortedSet<Integer> sortedKeyset = new TreeSet<Integer>(dateContainer.keySet());

		double rowTotal = 0;				
		for (Integer key : sortedKeyset) {			
			s = s.concat("\t\t\t<td>\n");				
			s = s.concat( 
							"\t\t\t\t<b><span>" + autoAddCommaToNumberStringValue(df.format(dateContainer.get(key)[columnIndexOne] + dateContainer.get(key)[columnIndexTwo])) + "</span></b>"
							); 				   														
			s = s.concat("\t\t\t</td>\n");
		
			rowTotal += (dateContainer.get(key)[columnIndexOne] + dateContainer.get(key)[columnIndexTwo]);
		}
		
		s = s.concat("\t\t\t<td>\n");				
		s = s.concat("\t\t\t\t<b><span>" + autoAddCommaToNumberStringValue(df.format(rowTotal)) + "</b></span>"); 
		s = s.concat("\t\t\t</td>\n");
		
		return s;
	}
	
	//added by Mike, 20190601; edited by Mike, 20190602
	private static String autoWriteValuesInRowForAllDateColumns(String s, PrintWriter writer, int columnIndex) {
		SortedSet<Integer> sortedKeyset = new TreeSet<Integer>(dateContainer.keySet());

		double rowTotal = 0;				
		for (Integer key : sortedKeyset) {
			s = s.concat("\t\t\t<td>\n");				
			s = s.concat( 
							"\t\t\t\t<b><span>" + autoAddCommaToNumberStringValue(df.format(dateContainer.get(key)[columnIndex])) + "</span></b>"
							); 				   							
			s = s.concat("\t\t\t</td>\n");
		
			rowTotal += dateContainer.get(key)[columnIndex];
		}
		
		s = s.concat("\t\t\t<td>\n");				
		s = s.concat("\t\t\t\t<b><span>" + autoAddCommaToNumberStringValue(df.format(rowTotal)) + "</b></span>"); 
		s = s.concat("\t\t\t</td>\n");
		
		return s;
	}
	
	//added by Mike, 20190602; edited by Mike, 20190603
	private static String autoAddCommaToNumberStringValue(String number) {
		StringBuffer sb = new StringBuffer(number);		
		int placeValueCount=0;
		int sbLength = sb.length();
		
		for (int i=sbLength-3; i>0; i--) { //do not include tenths, hundredths, and the dot 		
			if (placeValueCount<3) {
				placeValueCount++;
			}
			else {
				sb.insert(i, ",");
				placeValueCount=1;
			}	

//			System.out.println(">>>>" + sb.toString());
		}
		
		return sb.toString();
	}
	
	//added by Mike, 20190603
	private static void processTreatmentTransaction(String[] inputColumns, int i) {
//				if (!referringDoctorContainer.containsKey(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])) {
		if (!dateContainer.containsKey(dateValuesArrayInt[i])) {
			columnValuesArray = new double[OUTPUT_TOTAL_COLUMNS];			
			
			//added by Mike, 20200918
			//blank file
			//TO-DO: -reverify this
			System.out.println(">>"+inputColumns[INPUT_DATE_COLUMN]+">>>");
			if ((inputColumns[INPUT_DATE_COLUMN]).equals("")) {			
			System.out.println("dito:");
				dateContainer.put(dateValuesArrayInt[i], columnValuesArray);
				return;
			}
			
			//edited by Mike, 20181206
			if ((inputColumns[INPUT_CLASS_COLUMN].contains("HMO")) ||
				(inputColumns[INPUT_CLASS_COLUMN].contains("SLR"))) {

				columnValuesArray[OUTPUT_HMO_TREATMENT_COUNT_COLUMN] = 1;
				columnValuesArray[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);

				if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
					columnValuesArray[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
				}
				else {
					columnValuesArray[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
				}
			}
			else {
				columnValuesArray[OUTPUT_NON_HMO_TREATMENT_COUNT_COLUMN] = 1;
				columnValuesArray[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);

				if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
					columnValuesArray[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
				}
				else {
					columnValuesArray[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
				}
			}

//					referringDoctorContainer.put(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN], columnValuesArray);
			dateContainer.put(dateValuesArrayInt[i], columnValuesArray);
		}
		else {
			//edited by Mike, 20181206
			if ((inputColumns[INPUT_CLASS_COLUMN].contains("HMO")) ||
				(inputColumns[INPUT_CLASS_COLUMN].contains("SLR"))) {
/*							
				referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])[OUTPUT_HMO_TREATMENT_COUNT_COLUMN]++;					
				referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] 
					+= Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
*/
				dateContainer.get(dateValuesArrayInt[i])[OUTPUT_HMO_TREATMENT_COUNT_COLUMN]++;					
				dateContainer.get(dateValuesArrayInt[i])[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] 
					+= Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
					
				if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
/*							referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
*/
					dateContainer.get(dateValuesArrayInt[i])[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
				}
				else {
/*							
					referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
*/
					dateContainer.get(dateValuesArrayInt[i])[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
					
				}
			}
			else {
/*						
				referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])[OUTPUT_NON_HMO_TREATMENT_COUNT_COLUMN]++;					
				referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] 
					+= Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
*/
				dateContainer.get(dateValuesArrayInt[i])[OUTPUT_NON_HMO_TREATMENT_COUNT_COLUMN]++;					
				dateContainer.get(dateValuesArrayInt[i])[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] 
					+= Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
					
				if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
/*							referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
*/
					dateContainer.get(dateValuesArrayInt[i])[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
				}
				else {
/*							
					referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
*/
					dateContainer.get(dateValuesArrayInt[i])[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);							
				}

			}

/*			
			//added by Mike, 20200918
			System.out.println(i);
			System.out.println(dateContainer.get(dateValuesArrayInt[i]).toString());
*/
		}
	}	
	
	//added by Mike, 20190603
	private static void processConsultationTransaction(String[] inputColumns, int i) {//added by Mike, 20190110
		if (!inputColumns[INPUT_MEDICAL_DOCTOR_COLUMN].toLowerCase().trim().contains("syson")) { //TODO: update this if there are two (2) Medical Doctors with the keyword Syson
			return;
		}
		
		//added by Mike, 20190110
		if (inputColumns[INPUT_CLASS_COLUMN+offset].toLowerCase().trim().contains("nc")) {
			return;
		}		

//				if (!referringDoctorContainer.containsKey(inputColumns[INPUT_MEDICAL_DOCTOR_COLUMN])) {
		if (!dateContainer.containsKey(dateValuesArrayInt[i])) {
			columnValuesArray = new double[OUTPUT_TOTAL_COLUMNS];
			
			//edited by Mike, 20181206
			if ((inputColumns[INPUT_CLASS_COLUMN+offset].contains("HMO")) ||
				(inputColumns[INPUT_CLASS_COLUMN+offset].contains("SLR"))) {

				columnValuesArray[OUTPUT_HMO_CONSULTATION_COUNT_COLUMN] = 1;
				
				//added by Mike, 20190603
				if (inputColumns[INPUT_NET_PF_COLUMN+offset].toLowerCase().equals("no charge")) {
					return;
				}

				columnValuesArray[OUTPUT_HMO_TOTAL_NET_CONSULTATION_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN+offset]);

				if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
					columnValuesArray[OUTPUT_HMO_PAID_NET_CONSULTATION_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN+offset]);
				}
				else {
					columnValuesArray[OUTPUT_HMO_UNPAID_NET_CONSULTATION_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN+offset]);
				}
			}
			else {
				columnValuesArray[OUTPUT_NON_HMO_CONSULTATION_COUNT_COLUMN] = 1;
				
				//added by Mike, 20190603
				if (inputColumns[INPUT_NET_PF_COLUMN+offset].toLowerCase().equals("no charge")) {
					return;
				}

				columnValuesArray[OUTPUT_NON_HMO_TOTAL_NET_CONSULTATION_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN+offset]);

				if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
					columnValuesArray[OUTPUT_NON_HMO_PAID_NET_CONSULTATION_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN+offset]);
				}
				else {
					columnValuesArray[OUTPUT_NON_HMO_UNPAID_NET_CONSULTATION_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN+offset]);
				}
			}

//					referringDoctorContainer.put(inputColumns[INPUT_MEDICAL_DOCTOR_COLUMN], columnValuesArray);
			dateContainer.put(dateValuesArrayInt[i], columnValuesArray);
		}
		else {
			//edited by Mike, 20181206
			if ((inputColumns[INPUT_CLASS_COLUMN+offset].contains("HMO")) ||
				(inputColumns[INPUT_CLASS_COLUMN+offset].contains("SLR"))) {
/*							
				referringDoctorContainer.get(inputColumns[INPUT_MEDICAL_DOCTOR_COLUMN])[OUTPUT_HMO_CONSULTATION_COUNT_COLUMN]++;					
				referringDoctorContainer.get(inputColumns[INPUT_MEDICAL_DOCTOR_COLUMN])[OUTPUT_HMO_TOTAL_NET_CONSULTATION_FEE_COLUMN] 
					+= Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
*/
				dateContainer.get(dateValuesArrayInt[i])[OUTPUT_HMO_CONSULTATION_COUNT_COLUMN]++;					
				//added by Mike, 20190603
				if (inputColumns[INPUT_NET_PF_COLUMN+offset].toLowerCase().equals("no charge")) {
					return;
				}

				dateContainer.get(dateValuesArrayInt[i])[OUTPUT_HMO_TOTAL_NET_CONSULTATION_FEE_COLUMN] 
					+= Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN+offset]);
					
				if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
/*							referringDoctorContainer.get(inputColumns[INPUT_MEDICAL_DOCTOR_COLUMN])[OUTPUT_HMO_PAID_NET_CONSULTATION_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
*/
					dateContainer.get(dateValuesArrayInt[i])[OUTPUT_HMO_PAID_NET_CONSULTATION_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN+offset]);
				}
				else {
/*							
					referringDoctorContainer.get(inputColumns[INPUT_MEDICAL_DOCTOR_COLUMN])[OUTPUT_HMO_UNPAID_NET_CONSULTATION_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
*/
					dateContainer.get(dateValuesArrayInt[i])[OUTPUT_HMO_UNPAID_NET_CONSULTATION_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN+offset]);
					
				}
			}
			else {
/*						
				referringDoctorContainer.get(inputColumns[INPUT_MEDICAL_DOCTOR_COLUMN])[OUTPUT_NON_HMO_CONSULTATION_COUNT_COLUMN]++;					
				referringDoctorContainer.get(inputColumns[INPUT_MEDICAL_DOCTOR_COLUMN])[OUTPUT_NON_HMO_TOTAL_NET_CONSULTATION_FEE_COLUMN] 
					+= Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
*/
				dateContainer.get(dateValuesArrayInt[i])[OUTPUT_NON_HMO_CONSULTATION_COUNT_COLUMN]++;					
				//added by Mike, 20190603
				if (inputColumns[INPUT_NET_PF_COLUMN+offset].toLowerCase().equals("no charge")) {
					return;
				}
				
				dateContainer.get(dateValuesArrayInt[i])[OUTPUT_NON_HMO_TOTAL_NET_CONSULTATION_FEE_COLUMN] 
					+= Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN+offset]);
					
				if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
/*							referringDoctorContainer.get(inputColumns[INPUT_MEDICAL_DOCTOR_COLUMN])[OUTPUT_NON_HMO_PAID_NET_CONSULTATION_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
*/
					dateContainer.get(dateValuesArrayInt[i])[OUTPUT_NON_HMO_PAID_NET_CONSULTATION_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN+offset]);
				}
				else {
/*							
					referringDoctorContainer.get(inputColumns[INPUT_MEDICAL_DOCTOR_COLUMN])[OUTPUT_NON_HMO_UNPAID_NET_CONSULTATION_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
*/
					dateContainer.get(dateValuesArrayInt[i])[OUTPUT_NON_HMO_UNPAID_NET_CONSULTATION_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN+offset]);							
				}
			}
		}					
	}			
}
