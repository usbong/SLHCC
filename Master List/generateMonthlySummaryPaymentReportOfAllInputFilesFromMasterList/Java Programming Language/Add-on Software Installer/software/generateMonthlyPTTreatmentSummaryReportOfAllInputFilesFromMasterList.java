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
'   java generateMonthlyPTTreatmentSummaryReportOfAllInputFilesFromMasterList input201801.txt
' 
' where: "input201801.txt" is the name of the file.
' 
' 2) To execute a set of input files, e.g. input201801.txt, input201802.txt, you can use the following command: 
'  java generateMonthlyPTTreatmentSummaryReportOfAllInputFilesFromMasterList input*
'
' where: "input*" means any file in the directory that starts with "input".
*/ 

public class generateMonthlyPTTreatmentSummaryReportOfAllInputFilesFromMasterList {	
	private static boolean isInDebugMode = true;
	private static String inputFilename = "input201801"; //without extension; default input file
	
	private static String startDate = null;
	private static String endDate = null;
	
	private static final int INPUT_REFERRING_DOCTOR_COLUMN = 15;
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
	private static final int OUTPUT_TOTAL_COLUMNS = 9; 
	
	private static final int OUTPUT_HMO_COUNT_COLUMN = 0; //COUNT
	private static final int OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN = 1;
	private static final int OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN = 2;
	private static final int OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN = 3;

	private static final int OUTPUT_NON_HMO_COUNT_COLUMN = 4; //COUNT
	private static final int OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN = 5;
	private static final int OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN = 6;
	private static final int OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN = 7;

	private static final int OUTPUT_DATE_ID_COLUMN = 8; //added by Mike, 20181205
	
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
	private static String inputOutputTemplateFilenameMonthlyPaymentSummary = "assets\\templates\\generateMonthlyPaymentSummaryReportOutputTemplate";

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
		PrintWriter writer = new PrintWriter("output/TreatmentMonthlyPaymentSummaryReportOutput.txt", "UTF-8");

		//added by Mike, 20190531
		PrintWriter MonthlyPaymentSummaryWriter = new PrintWriter("output/MonthlyPaymentSummaryTreatment.html", "UTF-8");	
				
		
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
				continue;
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

//				if (!referringDoctorContainer.containsKey(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])) {
				if (!dateContainer.containsKey(dateValuesArrayInt[i])) {
					columnValuesArray = new double[OUTPUT_TOTAL_COLUMNS];
					
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

//					referringDoctorContainer.put(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN], columnValuesArray);
					dateContainer.put(dateValuesArrayInt[i], columnValuesArray);
				}
				else {
					//edited by Mike, 20181206
					if ((inputColumns[INPUT_CLASS_COLUMN].contains("HMO")) ||
						(inputColumns[INPUT_CLASS_COLUMN].contains("SLR"))) {
/*							
						referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])[OUTPUT_HMO_COUNT_COLUMN]++;					
						referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] 
							+= Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
*/
						dateContainer.get(dateValuesArrayInt[i])[OUTPUT_HMO_COUNT_COLUMN]++;					
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
						referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])[OUTPUT_NON_HMO_COUNT_COLUMN]++;					
						referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN])[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] 
							+= Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
*/
						dateContainer.get(dateValuesArrayInt[i])[OUTPUT_NON_HMO_COUNT_COLUMN]++;					
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
				}					
			}			
			
			//added by Mike, 20181205
			columnValuesArray[OUTPUT_DATE_ID_COLUMN] = i; 			
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
		
		if (!isTreatmentInputFileEmpty) {			
			processWriteOutputFileMonthlyPaymentSummary(MonthlyPaymentSummaryWriter, TREATMENT_FILE_TYPE);
/*			
			processWriteOutputFileTreatment(treatmentWriter);
			//added by Mike, 20190426
			processWriteOutputFileTreatmentUnclassifiedDiagnosedCases(treatmentUnclassifiedDiagnosedCasesWriter);
			
			//added by Mike, 20190503; edited by Mike, 20190504
			processWriteOutputFileMonthlyStatistics(treatmentCountMonthlyStatisticsWriter, TREATMENT_FILE_TYPE);		
			processWriteOutputFileMonthlyStatistics(consultationCountMonthlyStatisticsWriter, CONSULTATION_FILE_TYPE);		
			processWriteOutputFileMonthlyStatistics(procedureCountMonthlyStatisticsWriter, PROCEDURE_FILE_TYPE);		
*/
		}
		else {
			System.out.println("\nThere is no Tab-delimited .txt input file in the \"input\\treatment\" folder.\n");
		}

		//TO-DO: -remove: these
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
							"\t" + dateContainer.get(key)[OUTPUT_NON_HMO_COUNT_COLUMN]
							); 				   							

			//added by Mike, 20190521
			rowTotal += dateContainer.get(key)[OUTPUT_NON_HMO_COUNT_COLUMN];
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
							"\t" + dateContainer.get(key)[OUTPUT_HMO_COUNT_COLUMN]
							); 				   							
			
			//added by Mike, 20190521
			rowTotal += dateContainer.get(key)[OUTPUT_HMO_COUNT_COLUMN];
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
			double count = dateContainer.get(key)[OUTPUT_HMO_COUNT_COLUMN] + dateContainer.get(key)[OUTPUT_NON_HMO_COUNT_COLUMN];
		
			writer.print( 
							"\t" + count
							); 				   							

			//added by Mike, 20190521
			rowTotal += (dateContainer.get(key)[OUTPUT_HMO_COUNT_COLUMN] + dateContainer.get(key)[OUTPUT_NON_HMO_COUNT_COLUMN]);		
		}

		//added by Mike, 20190521
		writer.print("\t" + rowTotal); 		
		
		writer.close();
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
//			s=sc.nextLine(); //skip the first row, which is the input file's table headers

		if (isInDebugMode) {
			rowCount=0;
		}

		boolean hasWrittenAutoCalculatedValue=false;

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
						break;
					default:// PROCEDURE_FILE_TYPE:
						fileTypeString = "PROCEDURE";
						break;
				}			
				s = s.concat("\n");
				s = s.concat(fileTypeString+"\n");
			}			
						
			if (s.contains("<!-- DATE VALUE Column -->")) {
				s = s.concat("\n");
				s = s.concat("\t\t\t<!-- DATE: Column 1 -->\n");
				s = s.concat("\t\t\t<td colspan=\"1\">\n");
				s = s.concat("\t\t\t\t<div class=\"date\"><b><span class=\"transaction_type_column_header\">DATE:</span></b></div>\n");
				s = s.concat("\t\t\t</td>\n");
				
				for(int i=0; i<dateValuesArrayInt.length; i++) {
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

				s = autoWriteValuesInRowForAllDateColumns(s, writer, OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN);
				s = s.concat("\t\t\t</tr>\n");
				//--------------------------------------------------------------------
				
				s = s.concat("\t\t\t<tr>\n");
				s = s.concat("\t\t\t<td>\n");				
				s = s.concat("\t\t\t\t<b><div class=\"transaction_type_column\"><span>Cash (net) : PAID (PHP)</span></b></div>"); 		
				s = s.concat("\t\t\t</td>\n");

				s = autoWriteValuesInRowForAllDateColumns(s, writer, OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN);
				s = s.concat("\t\t\t</tr>\n");
				//--------------------------------------------------------------------
				
				s = s.concat("\t\t\t<tr>\n");
				s = s.concat("\t\t\t<td>\n");				
				s = s.concat("\t\t\t\t<div class=\"transaction_type_column\"><b><span>Cash (net) : UNPAID (PHP)</span></b></div>"); 		
				s = s.concat("\t\t\t</td>\n");

				s = autoWriteValuesInRowForAllDateColumns(s, writer, OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN);
				s = s.concat("\t\t\t</tr>\n");
				//--------------------------------------------------------------------
				
				s = s.concat("\t\t\t<tr>\n");
				s = s.concat("\t\t\t<td>\n");				
				s = s.concat("\t\t\t\t<div class=\"transaction_type_column\"><b><span>Cash (net) : COUNT</span></b></div>"); 		
				s = s.concat("\t\t\t</td>\n");

				s = autoWriteValuesInRowForAllDateColumns(s, writer, OUTPUT_NON_HMO_COUNT_COLUMN);
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

				s = autoWriteValuesInRowForAllDateColumns(s, writer, OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN);
				s = s.concat("\t\t\t</tr>\n");
				//--------------------------------------------------------------------

				s = s.concat("\t\t\t<tr>\n");
				s = s.concat("\t\t\t<td>\n");				
				s = s.concat("\t\t\t\t<div class=\"transaction_type_column\"><b><span>HMO (net) : PAID (PHP)</span></b></div>"); 		
				s = s.concat("\t\t\t</td>\n");

				s = autoWriteValuesInRowForAllDateColumns(s, writer, OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN);
				s = s.concat("\t\t\t</tr>\n");
				//--------------------------------------------------------------------

				s = s.concat("\t\t\t<tr>\n");
				s = s.concat("\t\t\t<td>\n");				
				s = s.concat("\t\t\t\t<div class=\"transaction_type_column\"><b><span>HMO (net) : UNPAID (PHP)</span></b></div>"); 		
				s = s.concat("\t\t\t</td>\n");

				s = autoWriteValuesInRowForAllDateColumns(s, writer, OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN);
				s = s.concat("\t\t\t</tr>\n");
				//--------------------------------------------------------------------

				s = s.concat("\t\t\t<tr>\n");
				s = s.concat("\t\t\t<td>\n");				
				s = s.concat("\t\t\t\t<div class=\"transaction_type_column\"><b><span>HMO (net) : COUNT</span></b></div>"); 		
				s = s.concat("\t\t\t</td>\n");

				s = autoWriteValuesInRowForAllDateColumns(s, writer, OUTPUT_HMO_COUNT_COLUMN);
				s = s.concat("\t\t\t</tr>\n");
				
/*								
			
				
				writer.print("\nHMO (net) : COUNT"); 		

				//added by Mike, 20190521
				rowTotal = 0;
				
				for (Integer key : sortedKeyset) {	
					writer.print( 
									"\t" + dateContainer.get(key)[OUTPUT_HMO_COUNT_COLUMN]
									); 				   							
					
					//added by Mike, 20190521
					rowTotal += dateContainer.get(key)[OUTPUT_HMO_COUNT_COLUMN];
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
					double count = dateContainer.get(key)[OUTPUT_HMO_COUNT_COLUMN] + dateContainer.get(key)[OUTPUT_NON_HMO_COUNT_COLUMN];
				
					writer.print( 
									"\t" + count
									); 				   							

					//added by Mike, 20190521
					rowTotal += (dateContainer.get(key)[OUTPUT_HMO_COUNT_COLUMN] + dateContainer.get(key)[OUTPUT_NON_HMO_COUNT_COLUMN]);		
				}

			//added by Mike, 20190521
			writer.print("\t" + rowTotal); 		
*/
				
			}
			writer.print(s + "\n");		
			
		}
		
		writer.close();
	}

	//added by Mike, 20190601
	private static String autoWriteValuesInRowForAllDateColumns(String s, PrintWriter writer, int columnIndex) {
		SortedSet<Integer> sortedKeyset = new TreeSet<Integer>(dateContainer.keySet());

		double rowTotal = 0;				
		for (Integer key : sortedKeyset) {
			s = s.concat("\t\t\t<td>\n");				
			s = s.concat( 
							"\t\t\t\t<b><span>" + df.format(dateContainer.get(key)[columnIndex]) + "</span></b>"
							); 				   							
			s = s.concat("\t\t\t</td>\n");
		
			rowTotal += dateContainer.get(key)[columnIndex];
		}
		
		s = s.concat("\t\t\t<td>\n");				
		s = s.concat("\t\t\t\t<b><span>" + df.format(rowTotal) + "</b></span>"); 
		s = s.concat("\t\t\t</td>\n");
		
		return s;
	}
}