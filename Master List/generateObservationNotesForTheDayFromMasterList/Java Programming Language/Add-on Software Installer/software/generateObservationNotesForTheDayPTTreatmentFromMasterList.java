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
	private static HashMap<String, ArrayList<Integer>> notesContainer;	//added by Mike, 20190213
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
		notesContainer = new HashMap<String, ArrayList<Integer>>();
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
			//removed the instruction below given that the input file does not include the table headers
//			s=sc.nextLine(); //skip the first row, which is the input file's table headers
	
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
				
//				System.out.println(">> s: "+s);

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

					
				System.out.println(">> transactionORNumber: "+transactionORNumber);				
				System.out.println(">> input: "+inputColumns[INPUT_NOTES_COLUMN]);

				for (int k=0; k<notesInputColumns.length; k++) {					
					//remove the excess quotation marks due to the input file being exported from MS Excel as Tab-delimited
					notesInputColumns[k] = notesInputColumns[k].replace("\"\"","'").replace("\"","").replace("'","\"");
					
					System.out.println(">> "+notesInputColumns[k]);

					if (!notesContainer.containsKey(notesInputColumns[k])) {
						System.out.println(">>>> new");
						notesContainer.put(notesInputColumns[k], new ArrayList<Integer>());
						notesContainer.get(notesInputColumns[k]).add(transactionORNumber); //add the transaction OR number 
					}	
					else {
						notesContainer.get(notesInputColumns[k]).add(transactionORNumber); //add the transaction OR number in the list with the same notes
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
		SortedSet<Integer> sortedTransactionsKeyset = new TreeSet<Integer>(transactionsContainer.keySet());
		SortedSet<String> sortedNotesKeyset = new TreeSet<String>(notesContainer.keySet());

		if (isInDebugMode) {
			System.out.println("//--------------------------");			
			System.out.println("  Transactions List:");			
			System.out.println("//--------------------------");			

			for (Integer key : sortedTransactionsKeyset) {	
				System.out.println(key+"\n"+
								   transactionsContainer.get(key)[OUTPUT_DATE_COLUMN]+"\n"+
								   transactionsContainer.get(key)[OUTPUT_PATIENT_NAME_COLUMN]+"\n"+
								   transactionsContainer.get(key)[OUTPUT_PAYMENT_CLASSIFICATION_COLUMN]+"\n--");
			}

			System.out.println("//--------------------------");			
			System.out.println("  Notes List:");			
			System.out.println("//--------------------------");			

			for (String key : sortedNotesKeyset) {	
				System.out.println(key+"\n--");
				System.out.println("length: "+notesContainer.get(key).size()+"\n");


				for (Integer officialReceiptNumberValue : notesContainer.get(key)) {
					System.out.println(key+"\n"+
								       officialReceiptNumberValue+"\n--");
				}
				System.out.println("-----");
				
			}

		}
		
/*		
		SortedSet<String> sortedNotesKeyset = new TreeSet<String>(notesContainer.keySet());

		for (String key : sortedNotesKeyset) {	
			System.out.println(""+dateContainer.get(key)[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN]);
		}
*/		
	
		writer.print("Notes:\n");
		int notesCount=1;
		
		for (String notesKey : sortedNotesKeyset) {	
		
			if (notesContainer.get(notesKey).size()>1) {
				writer.print(notesCount+") These are for the following transactions.\n\n");
			}
			else {
				writer.print(notesCount+") These are for the following transaction.\n\n");
			}
			
			notesCount++;
			
//			for (Integer transactionsKey : sortedTransactionsKeyset) {	
/*			System.out.println("transactionsKey: "+transactionsKey+"; "+"notesContainer.get(notesKey): "+notesContainer.get(notesKey));
*/

				for (Integer officialReceiptNumberValue : notesContainer.get(notesKey)) {
/*					System.out.println(key+"\n"+
								       officialReceiptNumberValue+"\n--");
*/									   
//					if (transactionsKey.intValue()==officialReceiptNumberValue.intValue()) { //OR NUMBER
/*					System.out.println("here");
*/				
					writer.println(transactionsContainer.get(officialReceiptNumberValue)[OUTPUT_DATE_COLUMN]+"\t"+
								   officialReceiptNumberValue+"\t"+
								   transactionsContainer.get(officialReceiptNumberValue)[OUTPUT_PATIENT_NAME_COLUMN]+"\t"+
								   transactionsContainer.get(officialReceiptNumberValue)[OUTPUT_PAYMENT_CLASSIFICATION_COLUMN]);		
//					}

				}
//			}
								   
			writer.println("\n--> "+notesKey+"\n");							   
		}
		
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