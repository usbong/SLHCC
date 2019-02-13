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

/*
' Given:
' 1) Encoding for the Updated Report for the Day (with the Observation Notes) Input Worksheet
' --> Saved/Exported as "Tab delimited" .txt file from Excel
' --> Example: input201808.txt (where the date format is YYYYMM; based on ISO 8601)
'
' Output:
' 1) Auto-generated Observation Notes For the Day PT Treatment Report
' --> "Tab delimited" .txt file 
' --> Regardless of the name of the input file or input files, the output file will be "ObservationNotesForTheDaySummaryReportOutput.txt".
' --> The Report includes: 
' --> a) NON-HMO (net)
' --> b) HMO & Sta. Lucia Reality/SLR (net)
' --> c) TOTAL (net)
' --> d) HMO & Sta. Lucia Reality/SLR (net) : PAID
' --> e) HMO & Sta. Lucia Reality/SLR (net) : UNPAID
' --> f) HMO & Sta. Lucia Reality/SLR (net) : TOTAL
'
' Notes:
' 1) To execute the add-on software/application simply use the following command:
'   java generateObservationNotesForTheDayPTTreatmentFromMasterList input201801.txt
' 
' where: "input201801.txt" is the name of the file.
' 
*/ 

public class generateObservationNotesForTheDayPTTreatmentFromMasterList {	
	private static boolean isInDebugMode = true;
	private static String inputFilename = "input201801"; //without extension; default input file
	
	private static String startDate = null;
	private static String endDate = null;
	
	private static final int INPUT_REFERRING_DOCTOR_COLUMN = 15;
	private static final int INPUT_NOTES_COLUMN = 0;
	private static final int INPUT_DATE_COLUMN = 1;
	private static final int INPUT_CLASS_COLUMN = 8; //HMO and NON-HMO
	private static final int INPUT_NET_PF_COLUMN = 10;
	private static final int INPUT_OR_NUMBER_COLUMN = 14;
	private static final int INPUT_PATIENT_NAME_COLUMN = 3;

/*	private static HashMap<String, double[]> referringDoctorContainer;	
*/
	private static HashMap<Integer, double[]> dateContainer;	//added by Mike, 20181205
	private static HashMap<String, Integer> notesContainer;	//added by Mike, 20190213
	private static HashMap<Integer, String[]> transactionsContainer; //added by Mike, 20190213; the int key is the OR number

	private static String[] notesValuesArray; //added by Mike, 20190213
	private static String[] transactionsValuesArray; //added by Mike, 20190213

	private static double[] columnValuesArray;
	private static String[] dateValuesArray; //added by Mike, 20180412
	private static int[] dateValuesArrayInt; //added by Mike, 20181206

	private static final int OUTPUT_TRANSACTION_TOTAL_COLUMNS = 3; //added by Mike, 20190213

	private static final int OUTPUT_DATE_COLUMN = 0; //transaction count
	private static final int OUTPUT_PATIENT_NAME_COLUMN = 1;
	private static final int OUTPUT_PAYMENT_CLASSIFICATION_COLUMN = 2;
	
	//the date and the referring doctor are not yet included here
	//this is for both HMO and NON-HMO transactions
	private static final int OUTPUT_TOTAL_COLUMNS = 9; 
	
	private static final int OUTPUT_HMO_COUNT_COLUMN = 0; //transaction count
	private static final int OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN = 1;
	private static final int OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN = 2;
	private static final int OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN = 3;

	private static final int OUTPUT_NON_HMO_COUNT_COLUMN = 4; //transaction count
	private static final int OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN = 5;
	private static final int OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN = 6;
	private static final int OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN = 7;

	private static final int OUTPUT_DATE_ID_COLUMN = 8; //added by Mike, 20181205
	
	private static boolean isConsultation; //added by Mike, 20190106

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
		PrintWriter writer = new PrintWriter("output/ObservationNotesForTheDaySummaryReportOutput.txt", "UTF-8");			
		/*referringDoctorContainer = new HashMap<String, double[]>();
		*/
		
		dateContainer = new HashMap<Integer, double[]>();
		notesContainer = new HashMap<String, Integer>();
		transactionsContainer = new HashMap<Integer, String[]>();
		
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
/*			
			//added by Mike, 20181206
			//edited by Mike, 20190106
			if (dateValuesArrayInt[i]==0) {
				dateValuesArrayInt[i] = Integer.parseInt(args[i].substring(args[i].indexOf("_")+1,args[i].indexOf(".txt")));
			}
*/			
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

				//do not include HMO and SLR transactions
				if ((inputColumns[INPUT_CLASS_COLUMN].contains("HMO")) ||
					(inputColumns[INPUT_CLASS_COLUMN].contains("SLR"))) {
					continue;
				}
				
				//phase/part/component 1
				int transactionORNumber = Integer.parseInt(inputColumns[INPUT_OR_NUMBER_COLUMN]);
				if (!transactionsContainer.containsKey(transactionORNumber)) {
					transactionsValuesArray = new String[OUTPUT_TRANSACTION_TOTAL_COLUMNS];
					
					transactionsValuesArray[OUTPUT_DATE_COLUMN] = inputColumns[INPUT_DATE_COLUMN];
					transactionsValuesArray[OUTPUT_PATIENT_NAME_COLUMN] = inputColumns[INPUT_PATIENT_NAME_COLUMN];
					transactionsValuesArray[OUTPUT_PAYMENT_CLASSIFICATION_COLUMN] = inputColumns[INPUT_CLASS_COLUMN];	
					
					transactionsContainer.put(transactionORNumber, transactionsValuesArray);
				}				
				
				//phase/part/component 2
				String[] notesInputColumns = inputColumns[INPUT_NOTES_COLUMN].split(";");					
				for (int k=0; k<notesInputColumns.length; k++) {
					if (!notesContainer.containsKey(notesInputColumns[k])) {
						notesContainer.put(notesInputColumns[k], transactionORNumber);						
					}	
					else {
						notesContainer.put(notesInputColumns[k], transactionORNumber); //add the transaction OR number in the list with the same notes
					}
				}
		
			}			
		}
		
		//TO-DO: -update: instructions to auto-write output
		/*
		 * --------------------------------------------------------------------
		 * OUTPUT
		 * --------------------------------------------------------------------
		*/
		if (isInDebugMode) {
			System.out.println("//--------------------------");			
			System.out.println("  Transactions List:");			
			System.out.println("//--------------------------");			

			SortedSet<Integer> sortedTransactionsKeyset = new TreeSet<Integer>(transactionsContainer.keySet());

			for (Integer key : sortedTransactionsKeyset) {	
				System.out.println(key+"\n"+
								   transactionsContainer.get(key)[OUTPUT_DATE_COLUMN]+"\n"+
								   transactionsContainer.get(key)[OUTPUT_PATIENT_NAME_COLUMN]+"\n"+
								   transactionsContainer.get(key)[OUTPUT_PAYMENT_CLASSIFICATION_COLUMN]+"\n--");
			}

			System.out.println("//--------------------------");			
			System.out.println("  Notes List:");			
			System.out.println("//--------------------------");			
			SortedSet<String> sortedNotesKeyset = new TreeSet<String>(notesContainer.keySet());

			for (String key : sortedNotesKeyset) {	
				System.out.println(key+"\n"+
								   notesContainer.get(key)+"\n--");
			}

		}
		
/*		
		SortedSet<String> sortedNotesKeyset = new TreeSet<String>(notesContainer.keySet());

		for (String key : sortedNotesKeyset) {	
			System.out.println(""+dateContainer.get(key)[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN]);
		}
*/		
/*		
		writer.print("Notes:\n");
		writer.print("1) These are for the following transactions.\n\n");

		SortedSet<String> sortedNotesKeyset = new TreeSet<String>(notesContainer.keySet());

		for (String key : sortedNotesKeyset) {	
			writer.print( 
//							"\t" + entry.getValue()[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN]
							"\t" + dateContainer.get(key)[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN]
							); 				   							
		}
*/
		
/*		
		SortedSet<Integer> sortedKeyset = new TreeSet<Integer>(dateContainer.keySet());

		for (Integer key : sortedKeyset) {	
			writer.print( 
//							"\t" + entry.getValue()[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN]
							"\t" + dateContainer.get(key)[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN]
							); 				   							
		}
*/		
		
		
		
		
		//init table header names
		writer.print("DATE:\t"); //"PT TREATMENT:" column

		//do not include input files that are Consultation transactions 
		for(int i=0; i<dateValuesArrayInt.length; i++) { //edited by Mike, 20190106
			writer.print(dateValuesArrayInt[i]+"\t"); //"PT TREATMENT:" column
		}
		
		//--------------------------------------------------------------------
		writer.print("\nNON-HMO/Cash (net) : TOTAL"); 		
/*
       for (Map.Entry<String, double[]> entry : dateContainer.entrySet())
       {
*/		
		
		SortedSet<Integer> sortedKeyset = new TreeSet<Integer>(dateContainer.keySet());

		for (Integer key : sortedKeyset) {	
/*		for(int k=0; k<dateContainer.length; k++) {
*/	
/*
			int totalCount = (int) dateContainer.get(key)[OUTPUT_HMO_COUNT_COLUMN] + (int) dateContainer.get(key)[OUTPUT_NON_HMO_COUNT_COLUMN];
			double totalNetTreatmentFee = dateContainer.get(key)[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] + dateContainer.get(key)[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN];
			double totalPaidNetTreatmentFee = dateContainer.get(key)[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN] + dateContainer.get(key)[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN];
			double totalUnpaidNetTreatmentFee = dateContainer.get(key)[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] + dateContainer.get(key)[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN];
			double totalFivePercentShareOfNetPaid = dateContainer.get(key)[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN]*0.05 + dateContainer.get(key)[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN]*0.05;

			//added by Mike, 20181123
			totalCountForAllReferringDoctors += totalCount;
			totalNetTreatmentFeeForAllReferringDoctors += totalNetTreatmentFee;
			totalPaidNetTreatmentFeeForAllReferringDoctors += totalPaidNetTreatmentFee;
			totalUnpaidNetTreatmentFeeForAllReferringDoctors += totalUnpaidNetTreatmentFee;
			totalFivePercentShareOfNetPaidForAllReferringDoctors += totalFivePercentShareOfNetPaid;
*/			
			writer.print( 
//							"\t" + entry.getValue()[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN]
							"\t" + dateContainer.get(key)[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN]
/*							"\n" + key			

							"\t" + totalCount +
							"\t" + df.format(totalNetTreatmentFee) +
							"\t" + df.format(totalPaidNetTreatmentFee) +
							"\t" + df.format(totalUnpaidNetTreatmentFee) +
							"\t" + df.format(totalFivePercentShareOfNetPaid)
*/							
							); 				   							
		}

		writer.print("\nNON-HMO/Cash (net) : PAID"); 		
		
		for (Integer key : sortedKeyset) {	
			writer.print( 
							"\t" + dateContainer.get(key)[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN]
							); 				   							
		}

		writer.print("\nNON-HMO/Cash (net) : UNPAID"); 		
		
		for (Integer key : sortedKeyset) {	
			writer.print( 
							"\t" + dateContainer.get(key)[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN]
							); 				   							
		}
		
		writer.print("\nNON-HMO/Cash (net) : TRANSACTION COUNT"); 		
		
		for (Integer key : sortedKeyset) {	
			writer.print( 
							"\t" + dateContainer.get(key)[OUTPUT_NON_HMO_COUNT_COLUMN]
							); 				   							
		}

		writer.print("\n"); //blank row 				
		writer.print("\nHMO (net) : TOTAL"); 		
		
		for (Integer key : sortedKeyset) {	
			writer.print( 
							"\t" + dateContainer.get(key)[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN]
							); 				   							
		}

		writer.print("\nHMO (net) : PAID"); 		
		
		for (Integer key : sortedKeyset) {	
			writer.print( 
							"\t" + dateContainer.get(key)[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN]
							); 				   							
		}

		writer.print("\nHMO (net) : UNPAID"); 		
		
		for (Integer key : sortedKeyset) {	
			writer.print( 
							"\t" + dateContainer.get(key)[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN]
							); 				   							
		}
		
		writer.print("\nHMO (net) : TRANSACTION COUNT"); 		
		
		for (Integer key : sortedKeyset) {	
			writer.print( 
							"\t" + dateContainer.get(key)[OUTPUT_HMO_COUNT_COLUMN]
							); 				   							
		}

		writer.print("\n"); //blank row
		writer.print("\nHMO and NON-HMO/Cash (net) : TOTAL"); 		

		for (Integer key : sortedKeyset) {	
			double count = dateContainer.get(key)[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] + dateContainer.get(key)[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN];
		
			writer.print( 
							"\t" + count
							); 				   							
		}

		writer.print("\nHMO and NON-HMO/Cash (net) : PAID"); 		

		for (Integer key : sortedKeyset) {	
			double count = dateContainer.get(key)[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN] + dateContainer.get(key)[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN];
		
			writer.print( 
							"\t" + count
							); 				   							
		}

		writer.print("\nHMO and NON-HMO/Cash (net) : UNPAID"); 		

		for (Integer key : sortedKeyset) {	
			double count = dateContainer.get(key)[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] + dateContainer.get(key)[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN];
		
			writer.print( 
							"\t" + count
							); 				   							
		}

		writer.print("\nHMO and NON-HMO/Cash (net) : TRANSACTION COUNT"); 		

		for (Integer key : sortedKeyset) {	
			double count = dateContainer.get(key)[OUTPUT_HMO_COUNT_COLUMN] + dateContainer.get(key)[OUTPUT_NON_HMO_COUNT_COLUMN];
		
			writer.print( 
							"\t" + count
							); 				   							
		}

		
/*		
		writer.print("REFERRING DOCTOR:\t"); //"REFERRING DOCTOR:" column
		writer.print("COUNT:\t"); //"COUNT:" column
		writer.print("TOTAL NET TREATMENT FEE:\t"); //"TOTAL NET TREATMENT FEE:" column
		writer.print("PAID NET TREATMENT FEE:\t"); //"PAID NET TREATMENT FEE:" column
		writer.print("UNPAID NET TREATMENT FEE:\t"); //"UNPAID NET TREATMENT FEE:" column		
		writer.println("5% SHARE OF NET PAID:"); //"5% SHARE OF NET PAID:" column		

		SortedSet<String> sortedKeyset = new TreeSet<String>(referringDoctorContainer.keySet());


		for (String key : sortedKeyset) {
			int totalCount = (int) referringDoctorContainer.get(key)[OUTPUT_HMO_COUNT_COLUMN] + (int) referringDoctorContainer.get(key)[OUTPUT_NON_HMO_COUNT_COLUMN];
			double totalNetTreatmentFee = referringDoctorContainer.get(key)[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] + referringDoctorContainer.get(key)[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN];
			double totalPaidNetTreatmentFee = referringDoctorContainer.get(key)[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN] + referringDoctorContainer.get(key)[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN];
			double totalUnpaidNetTreatmentFee = referringDoctorContainer.get(key)[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] +referringDoctorContainer.get(key)[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN];
			double totalFivePercentShareOfNetPaid = referringDoctorContainer.get(key)[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN]*0.05 + referringDoctorContainer.get(key)[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN]*0.05;

			//added by Mike, 20181123
			totalCountForAllReferringDoctors += totalCount;
			totalNetTreatmentFeeForAllReferringDoctors += totalNetTreatmentFee;
			totalPaidNetTreatmentFeeForAllReferringDoctors += totalPaidNetTreatmentFee;
			totalUnpaidNetTreatmentFeeForAllReferringDoctors += totalUnpaidNetTreatmentFee;
			totalFivePercentShareOfNetPaidForAllReferringDoctors += totalFivePercentShareOfNetPaid;
			
			writer.println( 
							startDate + " to " + endDate +
							"\t" + key +
							"\t" + totalCount +
							"\t" + df.format(totalNetTreatmentFee) +
							"\t" + df.format(totalPaidNetTreatmentFee) +
							"\t" + df.format(totalUnpaidNetTreatmentFee) +
							"\t" + df.format(totalFivePercentShareOfNetPaid)
							); 				   							
							
		}

		writer.println( startDate + " to " + endDate +
						"\t" + "All Referring Doctors" +
						"\t" + totalCountForAllReferringDoctors +
						"\t" + df.format(totalNetTreatmentFeeForAllReferringDoctors) +
						"\t" + df.format(totalPaidNetTreatmentFeeForAllReferringDoctors) +
						"\t" + df.format(totalUnpaidNetTreatmentFeeForAllReferringDoctors) +
						"\t" + df.format(totalFivePercentShareOfNetPaidForAllReferringDoctors)
						); 				   							
*/		

/*		
		//init table header names
		writer.print("DATE:\t"); //"DATE:" column
		writer.print("REFERRING DOCTOR:\t"); //"REFERRING DOCTOR:" column
		writer.print("COUNT:\t"); //"COUNT:" column
		writer.print("TOTAL NET TREATMENT FEE:\t"); //"TOTAL NET TREATMENT FEE:" column
		writer.print("PAID NET TREATMENT FEE:\t"); //"PAID NET TREATMENT FEE:" column
		writer.print("UNPAID NET TREATMENT FEE:\t"); //"UNPAID NET TREATMENT FEE:" column		
		writer.println("5% SHARE OF NET PAID:"); //"5% SHARE OF NET PAID:" column		

		SortedSet<String> sortedKeyset = new TreeSet<String>(referringDoctorContainer.keySet());

//		int dateCount=0; //added by Mike, 20180412
//		dateValuesArray[dateCount];

		for (String key : sortedKeyset) {
			int totalCount = (int) referringDoctorContainer.get(key)[OUTPUT_HMO_COUNT_COLUMN] + (int) referringDoctorContainer.get(key)[OUTPUT_NON_HMO_COUNT_COLUMN];
			double totalNetTreatmentFee = referringDoctorContainer.get(key)[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] + referringDoctorContainer.get(key)[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN];
			double totalPaidNetTreatmentFee = referringDoctorContainer.get(key)[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN] + referringDoctorContainer.get(key)[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN];
			double totalUnpaidNetTreatmentFee = referringDoctorContainer.get(key)[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] +referringDoctorContainer.get(key)[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN];
			double totalFivePercentShareOfNetPaid = referringDoctorContainer.get(key)[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN]*0.05 + referringDoctorContainer.get(key)[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN]*0.05;

			//added by Mike, 20181123
			totalCountForAllReferringDoctors += totalCount;
			totalNetTreatmentFeeForAllReferringDoctors += totalNetTreatmentFee;
			totalPaidNetTreatmentFeeForAllReferringDoctors += totalPaidNetTreatmentFee;
			totalUnpaidNetTreatmentFeeForAllReferringDoctors += totalUnpaidNetTreatmentFee;
			totalFivePercentShareOfNetPaidForAllReferringDoctors += totalFivePercentShareOfNetPaid;
			
			writer.println( 
							startDate + " to " + endDate +
							"\t" + key +
							"\t" + totalCount +
							"\t" + df.format(totalNetTreatmentFee) +
							"\t" + df.format(totalPaidNetTreatmentFee) +
							"\t" + df.format(totalUnpaidNetTreatmentFee) +
							"\t" + df.format(totalFivePercentShareOfNetPaid)
							); 				   							
							
		}

		writer.println( startDate + " to " + endDate +
						"\t" + "All Referring Doctors" +
						"\t" + totalCountForAllReferringDoctors +
						"\t" + df.format(totalNetTreatmentFeeForAllReferringDoctors) +
						"\t" + df.format(totalPaidNetTreatmentFeeForAllReferringDoctors) +
						"\t" + df.format(totalUnpaidNetTreatmentFeeForAllReferringDoctors) +
						"\t" + df.format(totalFivePercentShareOfNetPaidForAllReferringDoctors)
						); 				   							
		
		//----------------------------------------------------------------------------------------------------------------------------		
		writer.print("\nHMO Report\n");
		 
		//init table header names
		writer.print("DATE:\t"); //"DATE:" column
		writer.print("REFERRING DOCTOR:\t"); //"REFERRING DOCTOR:" column
		writer.print("COUNT:\t"); //"COUNT:" column
		writer.print("TOTAL NET TREATMENT FEE:\t"); //"TOTAL NET TREATMENT FEE:" column
		writer.print("PAID NET TREATMENT FEE:\t"); //"PAID NET TREATMENT FEE:" column
		writer.print("UNPAID NET TREATMENT FEE:\t"); //"UNPAID NET TREATMENT FEE:" column		
		writer.println("5% SHARE OF NET PAID:"); //"5% SHARE OF NET PAID:" column		

//		SortedSet<String> sortedKeyset = new TreeSet<String>(referringDoctorContainer.keySet());
		
		//added by Mike, 20181123
		totalCountForAllReferringDoctors = 0;
		totalNetTreatmentFeeForAllReferringDoctors = 0;
		totalPaidNetTreatmentFeeForAllReferringDoctors = 0;
		totalUnpaidNetTreatmentFeeForAllReferringDoctors = 0;
		totalFivePercentShareOfNetPaidForAllReferringDoctors = 0;
		
		for (String key : sortedKeyset) {			
			//added by Mike, 20181123
			totalCountForAllReferringDoctors += (int) referringDoctorContainer.get(key)[OUTPUT_HMO_COUNT_COLUMN];
			totalNetTreatmentFeeForAllReferringDoctors += referringDoctorContainer.get(key)[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN];
			totalPaidNetTreatmentFeeForAllReferringDoctors += referringDoctorContainer.get(key)[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN];
			totalUnpaidNetTreatmentFeeForAllReferringDoctors += referringDoctorContainer.get(key)[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN];
			totalFivePercentShareOfNetPaidForAllReferringDoctors += referringDoctorContainer.get(key)[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN];
			
			writer.println( startDate + " to " + endDate +
							"\t" + key +
							"\t" + (int) referringDoctorContainer.get(key)[OUTPUT_HMO_COUNT_COLUMN] +
							"\t" + df.format(referringDoctorContainer.get(key)[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN]) +
							"\t" + df.format(referringDoctorContainer.get(key)[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN]) +
							"\t" + df.format(referringDoctorContainer.get(key)[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN]) +
							"\t" + df.format(referringDoctorContainer.get(key)[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN]*0.05)
							); 				   							
		}
		
		writer.println( startDate + " to " + endDate +
				"\t" + "All Referring Doctors" +
				"\t" + totalCountForAllReferringDoctors +
				"\t" + df.format(totalNetTreatmentFeeForAllReferringDoctors) +
				"\t" + df.format(totalPaidNetTreatmentFeeForAllReferringDoctors) +
				"\t" + df.format(totalUnpaidNetTreatmentFeeForAllReferringDoctors) +
				"\t" + df.format(totalFivePercentShareOfNetPaidForAllReferringDoctors)
				); 				   							

		
		//----------------------------------------------------------------------------------------------------------------------------
		writer.print("\nNON-HMO Report\n");
		
		//init table header names
		writer.print("DATE:\t"); //"DATE:" column
		writer.print("REFERRING DOCTOR:\t"); //"REFERRING DOCTOR:" column
		writer.print("COUNT:\t"); //"COUNT:" column
		writer.print("TOTAL NET TREATMENT FEE:\t"); //"TOTAL NET TREATMENT FEE:" column
		writer.print("PAID NET TREATMENT FEE:\t"); //"PAID NET TREATMENT FEE:" column
		writer.print("UNPAID NET TREATMENT FEE:\t"); //"UNPAID NET TREATMENT FEE:" column
		writer.println("5% SHARE OF NET PAID:"); //"5% SHARE OF NET PAID:" column		
								
		//added by Mike, 20181123
		totalCountForAllReferringDoctors = 0;
		totalNetTreatmentFeeForAllReferringDoctors = 0;
		totalPaidNetTreatmentFeeForAllReferringDoctors = 0;
		totalUnpaidNetTreatmentFeeForAllReferringDoctors = 0;
		totalFivePercentShareOfNetPaidForAllReferringDoctors = 0;

		for (String key : sortedKeyset) {
			//added by Mike, 20181123
			totalCountForAllReferringDoctors += (int) referringDoctorContainer.get(key)[OUTPUT_NON_HMO_COUNT_COLUMN];
			totalNetTreatmentFeeForAllReferringDoctors += referringDoctorContainer.get(key)[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN];
			totalPaidNetTreatmentFeeForAllReferringDoctors += referringDoctorContainer.get(key)[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN];
			totalUnpaidNetTreatmentFeeForAllReferringDoctors += referringDoctorContainer.get(key)[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN];
			totalFivePercentShareOfNetPaidForAllReferringDoctors += referringDoctorContainer.get(key)[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN];

			writer.println( startDate + " to " + endDate +
							"\t" + key +
							"\t" + (int) referringDoctorContainer.get(key)[OUTPUT_NON_HMO_COUNT_COLUMN] +
							"\t" + df.format(referringDoctorContainer.get(key)[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN]) +
							"\t" + df.format(referringDoctorContainer.get(key)[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN]) +
							"\t" + df.format(referringDoctorContainer.get(key)[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN]) +
							"\t" + df.format(referringDoctorContainer.get(key)[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN]*0.05)
							); 				   							
		}
		
		writer.println( startDate + " to " + endDate +
		"\t" + "All Referring Doctors" +
		"\t" + totalCountForAllReferringDoctors +
		"\t" + df.format(totalNetTreatmentFeeForAllReferringDoctors) +
		"\t" + df.format(totalPaidNetTreatmentFeeForAllReferringDoctors) +
		"\t" + df.format(totalUnpaidNetTreatmentFeeForAllReferringDoctors) +
		"\t" + df.format(totalFivePercentShareOfNetPaidForAllReferringDoctors)
		); 				   							
*/
		//----------------------------------------------------------------------------------------------------------------------------
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
}