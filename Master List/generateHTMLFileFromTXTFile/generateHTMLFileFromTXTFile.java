/*
 * Copyright 2019 Usbong Social Systems, Inc.
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
' 1) Report for the Day (with the Observation Notes) Input 
' --> .txt file
' --> Example: input201808.txt (where the date format is YYYYMM; based on ISO 8601)
'
' Output:
' 1) Auto-generated Observation Notes For the Day Report
' --> .html file format
' --> Regardless of the name of the input file or input files, the output file will be "ObservationNotesForTheDaySummaryReportOutput.html".
' --> The output file will be stored inside the "output" folder, in the same directory where the "generateHTMLFileFromTXTFile.class" is located.
'
' Notes:
' 1) To execute the add-on software/application simply use the following command:
'   java generateHTMLFileFromTXTFile input201801.txt
' 
' where: "input201801.txt" is the name of the file.
' 
*/ 

public class generateHTMLFileFromTXTFile {	
	private static boolean isInDebugMode = true;
	private static String inputFilename = "input201801"; //without extension; default input file

	//This replaces the "\t" in each row of the input file
	private static final String myHtmlTab = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"; //added by Mike, 20190219
	
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
	private static HashMap<Integer, ArrayList<String>> outputContainer; //added by Mike, 20190215; the key is a numerical value for the set of transactions
	private static HashMap<Integer, ArrayList<Integer>> setOfTransactionsContainer; //added by Mike, 20190215

	//added by Mike, 20190219
	private static ArrayList<String> outputContainerArrayList; 
	
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
		//TO-DO: -add: transaction date in the output filename
		PrintWriter writer = new PrintWriter("output/ObservationNotesForTheDaySummaryTreatmentReportOutput.html", "UTF-8");			
		/*referringDoctorContainer = new HashMap<String, double[]>();
		*/
		
		dateContainer = new HashMap<Integer, double[]>();
		notesContainer = new HashMap<String, ArrayList<Integer>>();
		transactionsContainer = new HashMap<Integer, String[]>();
		outputContainer = new HashMap<Integer, ArrayList<String>>(); //added by Mike, 20190215
		setOfTransactionsContainer = new HashMap<Integer, ArrayList<Integer>>(); //added by Mike, 20190215
 	
		//added by Mike, 20190219
		outputContainerArrayList = new ArrayList<String>(); 
	
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
/*				
				//if the row is blank
				if (s.trim().equals("")) {
					continue;
				}
				String[] inputColumns = s.split("\t");					
*/				

				if (isInDebugMode) {
					rowCount++;
					System.out.println("rowCount: "+rowCount);
				}

				outputContainerArrayList.add(s);
			}
		}
		
		writer.println("<html>");	
		writer.println("<body>");
		int notesCount=1;
				
		for (int i=0; i<outputContainerArrayList.size(); i++) {		
			writer.println(outputContainerArrayList.get(i).replace("\t",myHtmlTab)+"</br>");
		}
		writer.println("</body>");
		writer.println("</html>");					
		
		writer.close();
	}
	
	//input: Feb-14-19
	//output: 14/02/2019
	private static String updateDateFormat(String date) { 
		String[] inputDateParts = date.split("-");					
		String month = "";
		
		switch(inputDateParts[0].toLowerCase()) { //month
			case "jan":
				month = "01";
				break;
			case "feb":
				month = "02";
				break;
			case "mar":
				month = "03";
				break;
			case "apr":
				month = "04";
				break;
			case "may":
				month = "05";
				break;
			case "jun":
				month = "06";
				break;
			case "jul":
				month = "07";
				break;
			case "aug":
				month = "08";
				break;
			case "sep":
				month = "09";
				break;
			case "oct":
				month = "10";
				break;
			case "nov":
				month = "11";
				break;
			case "dec":
				month = "12";
				break;
		}	

		//TO-DO: -update: this to not hardcode "20" for the year
		return inputDateParts[1]+"/"+month+"/"+"20"+inputDateParts[2];
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