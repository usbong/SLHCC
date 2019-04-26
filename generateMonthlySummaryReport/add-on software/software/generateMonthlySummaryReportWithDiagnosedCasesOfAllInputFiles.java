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
import java.util.Map.Entry; //added by Mike, 20190417
import utils.IncidenceNumberComparator; //added by Mike, 20190418
/*
' Given:
' 1) Encoding for the Month Input Worksheet
' --> Saved/Exported as "Tab delimited" .txt file from Excel
' --> Example: input_201808.txt (where the date format is YYYYMM; based on ISO 8601)
'
' Output:
' 1) Auto-generated Monthly Summary Report
' --> "Tab delimited" .txt file 
' --> Regardless of the name of the input file or input files, the output file will be "MonthlySummaryReportOutput.txt", which is located inside an "output" folder that is in the same directory as the add-on software
'
' Notes:
' 1) To execute the add-on software/application simply use the following command:
'   java generateMonthlySummaryReportWithDiagnosedCasesOfAllInputFiles input_201801.txt
' 
' where: "input_201801.txt" is the name of the file.
' 
' 2) To execute a set of input files, e.g. input201801.txt, input201802.txt, you can use the following command: 
'   java generateMonthlySummaryReportWithDiagnosedCasesOfAllInputFiles input*
' 
' where: "input*" means any file in the directory that starts with "input".
'
' 3) Make sure to include "Consultation" in the input file name.
' --> This is so that the add-on software would be able to properly identify it as a set of "Consultation" transactions, instead of those of "Treatment".
' --> Example: inputConsultation201801.txt
'
' 4) If you use space in your file name, e.g. "input Consultation 201801.txt", you will have to execute the input files as follows.
'   java generateMonthlySummaryReportWithDiagnosedCasesOfAllInputFiles *"2018"*.txt
'
' where: * means any set of characters
'
' 5) To compile on Windows' Command Prompt the add-on software with the Apache Commons Text .jar file, i.e. org.apache.commons.text, use the following command:
'   javac -cp .;org.apache.commons.text.jar generateMonthlySummaryReportWithDiagnosedCasesOfAllInputFiles.java
'
' 6) To execute on Windows' Command Prompt the add-on software with the Apache Commons Text .jar file, i.e. org.apache.commons.text, use the following command:
'   java -cp .;org.apache.commons.text.jar generateMonthlySummaryReportWithDiagnosedCasesOfAllInputFiles *.txt
'
' 7) The Apache Commons Text binaries with the .jar file can be downloaded here:
'   http://commons.apache.org/proper/commons-text/download_text.cgi; last accessed: 20190123
'
' 8) The documentation for the LevenshteinDistance can be viewed here:
'   https://commons.apache.org/proper/commons-text/javadocs/api-release/org/apache/commons/text/similarity/LevenshteinDistance.html; last accessed: 20190123
*/ 

public class generateMonthlySummaryReportWithDiagnosedCasesOfAllInputFiles {	
	private static boolean isInDebugMode = true; //edited by Mike, 20190131
	private static boolean isNetPFComputed = false; //added by Mike, 20190131

	private static String inputFilename = "input201801"; //without extension; default input file
	//added by Mike, 20190413
	private static String diagnosedCasesListInputFilename = "diagnosedCasesList"; //without extension; default input file 

	//added by Mike, 20190414
	private static String inputOutputTemplateFilenameTreatment = "assets\\templates\\generateMonthlySummaryReportOutputTemplateTreatment";//without extension; default input file 
	//Note that I have to use double backslash, i.e. "\\", to use "\" in the filename

	//added by Mike, 20190422
	private static String inputOutputTemplateFilenameConsultation = "assets\\templates\\generateMonthlySummaryReportOutputTemplateConsultation";//without extension; default input file 
	//Note that I have to use double backslash, i.e. "\\", to use "\" in the filename
	
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
	
	//edited by Mike, 20190424
	private static final int INPUT_REFERRING_DOCTOR_COLUMN = 15-INPUT_NON_MASTER_LIST_OFFSET;
	private static final int INPUT_NOTES_COLUMN = 0; //This column is not included in the INPUT_NON_MASTER_LIST_OFFSET
	private static final int INPUT_DATE_COLUMN = 1-INPUT_NON_MASTER_LIST_OFFSET;
	private static final int INPUT_NAME_COLUMN = 3-INPUT_NON_MASTER_LIST_OFFSET;
	private static final int INPUT_CLASS_COLUMN = 8-INPUT_NON_MASTER_LIST_OFFSET; //HMO and NON-HMO
	private static final int INPUT_NET_PF_COLUMN = 10-INPUT_NON_MASTER_LIST_OFFSET;
	private static final int INPUT_NEW_OLD_COLUMN = 16-INPUT_NON_MASTER_LIST_OFFSET;
	private static final int INPUT_NEW_OLD_PATIENT_COLUMN = 16-INPUT_NON_MASTER_LIST_OFFSET; //added by Mike, 20190102

	private static final int INPUT_CONSULTATION_MEDICAL_DOCTOR_COLUMN = 16-INPUT_NON_MASTER_LIST_OFFSET;	
	private static final int INPUT_CONSULTATION_CLASS_COLUMN = 9-INPUT_NON_MASTER_LIST_OFFSET; //HMO and NON-HMO
	private static final int INPUT_CONSULTATION_NET_PF_COLUMN = 11-INPUT_NON_MASTER_LIST_OFFSET;
	private static final int INPUT_CONSULTATION_NEW_OLD_COLUMN = 17-INPUT_NON_MASTER_LIST_OFFSET;
	private static final int INPUT_CONSULTATION_NEW_OLD_PATIENT_COLUMN = 17-INPUT_NON_MASTER_LIST_OFFSET; //added by Mike, 
/*	
	private static final int INPUT_REFERRING_DOCTOR_COLUMN = 15-INPUT_NON_MASTER_LIST_OFFSET;
	private static final int INPUT_NOTES_COLUMN = 0; //This column is not included in the INPUT_NON_MASTER_LIST_OFFSET
	private static final int INPUT_DATE_COLUMN = 1-INPUT_NON_MASTER_LIST_OFFSET;
	private static final int INPUT_NAME_COLUMN = 3-INPUT_NON_MASTER_LIST_OFFSET;
	private static final int INPUT_CLASS_COLUMN = 8-INPUT_NON_MASTER_LIST_OFFSET; //HMO and NON-HMO
	private static final int INPUT_NET_PF_COLUMN = 10-INPUT_NON_MASTER_LIST_OFFSET;
	private static final int INPUT_NEW_OLD_COLUMN = 16-INPUT_NON_MASTER_LIST_OFFSET;
	private static final int INPUT_NEW_OLD_PATIENT_COLUMN = 16-INPUT_NON_MASTER_LIST_OFFSET; //added by Mike, 20190102
*/	
	//TO-DO: -add: column for Consultation transactions, which have both Chief Complaint and Diagnosis
	private static final int INPUT_DIAGNOSIS_COLUMN = 6-INPUT_NON_MASTER_LIST_OFFSET; //added by Mike, 20190413

	//edited by Mike, 20190425
	private static final int INPUT_CONSULTATION_PROCEDURE_COLUMN = 2-INPUT_NON_MASTER_LIST_OFFSET;
//	private static final int INPUT_CONSULTATION_MEDICAL_DOCTOR_COLUMN = 16-INPUT_NON_MASTER_LIST_OFFSET;
	
	//added by Mike, 20190107
	private static final int INPUT_CONSULTATION_MEDICAL_CERTIFICATE_COLUMN = 2-INPUT_NON_MASTER_LIST_OFFSET; //The int value is the same as "INPUT_CONSULTATION_PROCEDURE_COLUMN".

	//added by Mike, 20181218
	//CONSULTATION
/*	
	private static final int INPUT_CONSULTATION_CLASS_COLUMN = 9;
	private static final int INPUT_CONSULTATION_NET_PF_COLUMN = 11;
	private static final int INPUT_CONSULTATION_NEW_OLD_COLUMN = 17;
*/	
	//edited by Mike, 20190423
//	private static final int INPUT_CONSULTATION_OFFSET = 1;

	//added by Mike, 20190412
	private static final int INPUT_KNOWN_DIAGNOSED_CASES_LIST_CLASSIFICATION_COLUMN = 0;
	private static final int INPUT_KNOWN_DIAGNOSED_CASES_LIST_SUB_CLASSIFICATION_COLUMN = 1;

	
/*	private static HashMap<String, double[]> referringDoctorContainer;	
*/
	private static HashMap<Integer, double[]> dateContainer;	//added by Mike, 201801205
	private static HashMap<String, double[]> hmoContainer;	//added by Mike, 201801217
	private static HashMap<String, double[]> nonHmoContainer;	//added by Mike, 201801217
	private static HashMap<String, double[]> referringDoctorContainer; //added by Mike, 20181218
	private static HashMap<String, double[]> medicalDoctorContainer; //added by Mike, 20190202
	private static HashMap<String, Integer> diagnosedCasesContainer; //added by Mike, 20190412
	private static HashMap<String, String> knownDiagnosedCasesContainer; //added by Mike, 20190412
	private static HashMap<String, Integer> classifiedDiagnosedCasesContainer; //added by Mike, 20190412

	private static double[] columnValuesArray;
	private static String[] dateValuesArray; //added by Mike, 20180412
	private static int[] dateValuesArrayInt; //added by Mike, 20181206
	//private static ArrayList<int> dateValuesArrayInt; //edited by Mike, 20181221
		
	//the date and the referring doctor are not yet included here
	//this is for both HMO and NON-HMO transactions
	private static final int OUTPUT_TOTAL_COLUMNS = 25; //edited by Mike, 20190202

	//PT TREATMENT
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
	
	//added by Mike, 20190415
	private static int totalTreatmentCount = 0;
	private static int totalConsultationCount = 0;
	private static int totalProcedureCount = 0;		
	private static int totalMedicalCertificateCount = 0;
	
	//added by Mike, 20190416
	private static int totalReferringMedicalDoctorTransactionCount = 0;
	private static int totalNewPatientReferralTransactionCount = 0;
	private static int totalConsultationPerDoctorCount = 0;
	private static int totalProcedurePerDoctorCount = 0;
	private static int totalMedicalCertificatePerDoctorCount = 0;
	private static int totalNewPatientPerDoctorCount = 0;
	private static int totalFollowUpPerDoctorCount = 0;
	private static int totalOldPatientPerDoctorCount = 0;
	
	//added by Mike, 20190416
	private static final String classificationWI = "WI";
	private static final String classificationWI_MCDO = "WI (C/O MCDO)"; //added by Mike, 20190424
	private static final String classificationSLC = "SLC";
	private static final String classificationSC = "SC";
	private static final String classificationPWD = "PWD";
	private static final String classificationNO_CHARGE = "NO CHARGE"; //added by Mike, 20190425
	private static final String classificationSLR = "SLR"; //added by Mike, 20190425
		
	//added by Mike, 20190417
	private static double totalTreatmentHMOCount = 0;
	private static double totalConsultationHMOCount = 0;
	private static double totalProcedureHMOCount = 0;		
	private static double totalMedicalCertificateHMOCount = 0;
	private static double totalNewPatientTreatmentHMOCount = 0;
	private static double totalNewPatientConsultationHMOCount = 0;
	
	//added by Mike, 20190425
	private static int totalWiValue = 0;
	private static int noChargeValue = 0;
	private static int slcValue = 0;
	private static int slrValue = 0;
	private static int scValue = 0;
	private static int pwdValue = 0;
			
	public static void main ( String[] args ) throws Exception
	{			
		makeFilePath("output"); //"output" is the folder where I've instructed the add-on software/application to store the output file			
		PrintWriter treatmentWriter = new PrintWriter("output/MonthlySummaryReportOutputTreatment.html", "UTF-8");			
		PrintWriter consultationWriter = new PrintWriter("output/MonthlySummaryReportOutputConsultation.html", "UTF-8");			

/*		
		//added by Mike, 20190413
		PrintWriter diagnosedCasesWriter = new PrintWriter("output/MonthlySummaryReportOfDiagnosedCasesOutput.txt", "UTF-8");			
		PrintWriter diagnosedCasesClassifiedWriter = new PrintWriter("output/MonthlySummaryReportOfDiagnosedCasesClassifiedOutput.txt", "UTF-8");			
*/
		
		dateContainer = new HashMap<Integer, double[]>();
		hmoContainer = new HashMap<String, double[]>();
		nonHmoContainer = new HashMap<String, double[]>();
		referringDoctorContainer = new HashMap<String, double[]>();
//		medicalDoctorContainer = new HashMap<String, double[]>();
		classificationContainerPerMedicalDoctor = new HashMap<String, HashMap<String, double[]>>();				
		medicalDoctorContainer = new HashMap<String, double[]>(); //added by Mike, 20190202
				
		diagnosedCasesContainer = new HashMap<String, Integer>(); //added by Mike, 20190412
		knownDiagnosedCasesContainer = new HashMap<String, String>(); //added by Mike, 20190412
		classifiedDiagnosedCasesContainer = new HashMap<String, Integer>(); //added by Mike, 20190412
		
		//added by Mike, 20181116
		startDate = null; //properly set the month and year in the output file of each input file
		dateValuesArray = new String[args.length]; //added by Mike, 20180412
		dateValuesArrayInt = new int[args.length]; //added by Mike, 20180412
		//dateValuesArrayInt = new ArrayList<int>(); //edited by Mike, 20181221

		//added by Mike, 20190412
		//PART/COMPONENT/MODULE/PHASE 1			
		processKnownDiagnosedCasesInputFile(args);

		//PART/COMPONENT/MODULE/PHASE 2
		processInputFiles(args, true);

		//PART/COMPONENT/MODULE/PHASE 3		
		setClassificationContainerPerMedicalDoctor(classificationContainerPerMedicalDoctor);
		processInputFiles(args, false);
						
		//PART/COMPONENT/MODULE/PHASE 4
		processDiagnosisClassification();						
				
		//added by Mike, 20190125		
		processContainers();
				
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

		//added by Mike, 20190415
		processAutoCalculate();

		/*
		 * --------------------------------------------------------------------
		 * OUTPUT
		 * --------------------------------------------------------------------		 
		*/
//		System.out.println("args[0]: " + args[0]);

		//edited by Mike, 20190131
		/*writer.print("Monthly Summary Report\n");
		*/
		//edited by Mike, 20190422
		processWriteOutputFileTreatment(treatmentWriter);
		processWriteOutputFileConsultation(consultationWriter);
		
		/*writer.close();
		*/
		//--------------------------------------------------------------------------------------
		// OUTPUT FOR DIAGNOSED CASES
		//--------------------------------------------------------------------------------------
/*		
		//edited by Mike, 20190223
		SortedSet<String> sortedDiagnosedCasesKeyset = new TreeSet<String>(diagnosedCasesContainer.keySet());
		SortedSet<String> sortedDiagnosedCasesClassifiedKeyset = new TreeSet<String>(classifiedDiagnosedCasesContainer.keySet());
	
		int total = 0;
		
		diagnosedCasesWriter.print("Monthly Summary Report of Diagnosed Cases\n");

		for (String key : sortedDiagnosedCasesKeyset) {	
			int diagnosedCaseCount = diagnosedCasesContainer.get(key);
			total+=diagnosedCaseCount;
			
			diagnosedCasesWriter.println(
							key + "\t" + 
							diagnosedCaseCount+"\n"							
						); 				   							
		}
		
		diagnosedCasesWriter.println(
							"TOTAL:\t" + 
							total+"\n"							
						); 				   							

		//added by Mike, 20190223
		diagnosedCasesClassifiedWriter.print("Monthly Summary Report of Classified Diagnosed Cases\n");
				
		total = 0;
		
		for (String key : sortedDiagnosedCasesClassifiedKeyset) {	
			int diagnosedCaseCount = classifiedDiagnosedCasesContainer.get(key);
			total+=diagnosedCaseCount;
			
			diagnosedCasesClassifiedWriter.println(
							key + "\t" + 
							diagnosedCaseCount+"\n"							
						); 				   							
		}
		
		diagnosedCasesClassifiedWriter.println(
							"TOTAL:\t" + 
							total+"\n"							
						); 				   		
						
						
		
		diagnosedCasesWriter.close();
		diagnosedCasesClassifiedWriter.close();
*/		
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
		

//		System.out.println("Integer.parseInt(year.concat(month)): "+Integer.parseInt(year.concat(month)));
		return Integer.parseInt(year.concat(month));
	}
	
	//added by Mike, 20181030
	private static void makeFilePath(String filePath) {
		File directory = new File(filePath);		
		if (!directory.exists() && !directory.mkdirs()) 
    	{
    		System.out.println("File Path to file could not be made.");
    	}    			
	}
	
	//added by Mike, 20190415
	private static void processAutoCalculate() {		

		System.out.println("dateValuesArrayInt.length: "+dateValuesArrayInt.length);		
		System.out.println("dateValuesArrayInt.length/2: "+dateValuesArrayInt.length/2);
		
		//Note that there should be an even number of input files and at least two (2) input files, one for PT Treatment and another for Consultation
		for(int i=0; i<dateValuesArrayInt.length/2; i++) { //divide by 2 because we have the same month-year for both PT TREATMENT and CONSULTATION
		System.out.println("dateValuesArrayInt[i]: "+dateValuesArrayInt[i]);
		
			//added by Mike, 20190207
			if (dateValuesArrayInt[i]==0) { //if there is no .txt input file
				System.out.println("\nThere is no Tab-delimited .txt input file in either the \"input\\consultation\" folder or the \"input\\treatment\" folder.");
				return;
			}
					
			double treatmentCount = dateContainer.get(dateValuesArrayInt[i])[OUTPUT_HMO_COUNT_COLUMN] + dateContainer.get(dateValuesArrayInt[i])[OUTPUT_NON_HMO_COUNT_COLUMN];

			//added by Mike, 20181218
			double consultationCount = dateContainer.get(dateValuesArrayInt[i])[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN] + dateContainer.get(dateValuesArrayInt[i])[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN];

			//added by Mike, 20190105
			double procedureCount = dateContainer.get(dateValuesArrayInt[i])[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN] + dateContainer.get(dateValuesArrayInt[i])[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN];

			//added by Mike, 20190105
			double medicalCertificateCount = dateContainer.get(dateValuesArrayInt[i])[OUTPUT_CONSULTATION_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN] + dateContainer.get(dateValuesArrayInt[i])[OUTPUT_CONSULTATION_NON_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN];
			
			totalTreatmentCount += treatmentCount;
			totalConsultationCount += consultationCount;
			totalProcedureCount += procedureCount;
			totalMedicalCertificateCount += medicalCertificateCount;		
		}
		
		//--------------------------------------------------------------------
		SortedSet<String> sortedMedicalDoctorTransactionCountKeyset = new TreeSet<String>(medicalDoctorContainer.keySet());

		for (String key : sortedMedicalDoctorTransactionCountKeyset) {	
			double count = medicalDoctorContainer.get(key)[OUTPUT_HMO_COUNT_COLUMN] + medicalDoctorContainer.get(key)[OUTPUT_NON_HMO_COUNT_COLUMN];

			double newPatientReferralTransactionCount = medicalDoctorContainer.get(key)[OUTPUT_HMO_NEW_PATIENT_COUNT_COLUMN] + medicalDoctorContainer.get(key)[OUTPUT_NON_HMO_NEW_PATIENT_COUNT_COLUMN];

			//added by Mike, 20181219
			double consultationCount = medicalDoctorContainer.get(key)[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN] + medicalDoctorContainer.get(key)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN];

			//added by Mike, 20181219
			double procedureCount = medicalDoctorContainer.get(key)[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN] + medicalDoctorContainer.get(key)[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN];

			//added by Mike, 20190109
			double medicalCertificateCount = medicalDoctorContainer.get(key)[OUTPUT_CONSULTATION_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN] + medicalDoctorContainer.get(key)[OUTPUT_CONSULTATION_NON_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN];

			//added by Mike, 20190202
			double newPatientCount = medicalDoctorContainer.get(key)[OUTPUT_CONSULTATION_HMO_NEW_PATIENT_COUNT_COLUMN] + medicalDoctorContainer.get(key)[OUTPUT_CONSULTATION_NON_HMO_NEW_PATIENT_COUNT_COLUMN];

			//added by Mike, 20190202
			double followUpCount = medicalDoctorContainer.get(key)[OUTPUT_CONSULTATION_HMO_FOLLOW_UP_COUNT_COLUMN] + medicalDoctorContainer.get(key)[OUTPUT_CONSULTATION_NON_HMO_FOLLOW_UP_COUNT_COLUMN];

			//added by Mike, 20190202
			double oldPatientCount = medicalDoctorContainer.get(key)[OUTPUT_CONSULTATION_HMO_OLD_PATIENT_COUNT_COLUMN] + medicalDoctorContainer.get(key)[OUTPUT_CONSULTATION_NON_HMO_OLD_PATIENT_COUNT_COLUMN];
			
			totalReferringMedicalDoctorTransactionCount += count;
			totalNewPatientReferralTransactionCount += newPatientReferralTransactionCount;
			totalConsultationPerDoctorCount += consultationCount;
			totalProcedurePerDoctorCount += procedureCount;
			totalMedicalCertificatePerDoctorCount += procedureCount;
			totalFollowUpPerDoctorCount += followUpCount;
			totalNewPatientPerDoctorCount += newPatientCount;
			totalOldPatientPerDoctorCount += oldPatientCount;
		}
		
		//added by Mike, 20190417
		//--------------------------------------------------------------------		
		SortedSet<String> sortedKeyset = new TreeSet<String>(hmoContainer.keySet());
		
		for (String key : sortedKeyset) {	
			double treatmentCount = hmoContainer.get(key)[OUTPUT_HMO_COUNT_COLUMN];
			double consultationCount = hmoContainer.get(key)[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN];
			double procedureCount = hmoContainer.get(key)[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN]; //added by Mike, 20190105		
			double medicalCertificateCount = hmoContainer.get(key)[OUTPUT_CONSULTATION_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN]; //added by Mike, 20190107
			double newPatientTreatmentCount = hmoContainer.get(key)[OUTPUT_HMO_NEW_PATIENT_COUNT_COLUMN]; //added by Mike, 20190102
			double newPatientConsultationCount = hmoContainer.get(key)[OUTPUT_CONSULTATION_HMO_NEW_PATIENT_COUNT_COLUMN]; //added by Mike, 20190102

			totalTreatmentHMOCount += treatmentCount;
			totalConsultationHMOCount += consultationCount;
			totalProcedureHMOCount += procedureCount;
			totalMedicalCertificateHMOCount += medicalCertificateCount;
			totalNewPatientTreatmentHMOCount += newPatientTreatmentCount;
			totalNewPatientConsultationHMOCount += newPatientConsultationCount;			
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
/*							
							columnValuesArray[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
*/							
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
/*							
							columnValuesArray[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
*/
							
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
						if ((inputColumns[INPUT_CONSULTATION_CLASS_COLUMN].contains("HMO")) ||
							(inputColumns[INPUT_CONSULTATION_CLASS_COLUMN].contains("SLR"))) {

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
						if ((inputColumns[INPUT_CONSULTATION_CLASS_COLUMN].contains("HMO")) ||
							(inputColumns[INPUT_CONSULTATION_CLASS_COLUMN].contains("SLR"))) {
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
				if ((inputColumns[INPUT_CONSULTATION_CLASS_COLUMN].contains("HMO")) ||
					(inputColumns[INPUT_CONSULTATION_CLASS_COLUMN].contains("SLR"))) {

					String hmoName = inputColumns[INPUT_CONSULTATION_CLASS_COLUMN].trim().toUpperCase();
					
					if (!hmoContainer.containsKey(hmoName)) {
						columnValuesArray = new double[OUTPUT_TOTAL_COLUMNS];		

						//added by Mike, 20190102
						if (inputColumns[INPUT_CONSULTATION_NEW_OLD_PATIENT_COLUMN].trim().toLowerCase().contains("new")) {
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
						if (inputColumns[INPUT_CONSULTATION_NEW_OLD_PATIENT_COLUMN].trim().toLowerCase().contains("new")) {
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
			if ((!inputColumns[INPUT_CONSULTATION_CLASS_COLUMN].contains("HMO")) &&
				(!inputColumns[INPUT_CONSULTATION_CLASS_COLUMN].contains("SLR"))) {

				String classificationName = inputColumns[INPUT_CONSULTATION_CLASS_COLUMN].trim().toUpperCase();
/*				System.out.println("classificationName: "+classificationName); 

				if (classificationName.contains("MCDO")) {
					System.out.println("May MCDO"); 	
				}
*/				
				if (isInDebugMode) {
					if (classificationName.trim().equals("")) {
//						System.out.println(">>> "+inputColumns[INPUT_DATE_COLUMN]+"; Name: "+inputColumns[INPUT_NAME_COLUMN]);
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
			//added by Mike, 20190125; edited by Mike, 20190424
//			inputReferringMedicalDoctor = inputColumns[INPUT_CONSULTATION_MEDICAL_DOCTOR_COLUMN].trim().toUpperCase();
			inputReferringMedicalDoctor = inputColumns[INPUT_CONSULTATION_MEDICAL_DOCTOR_COLUMN].trim().toUpperCase();
	
			if (!referringDoctorContainer.containsKey(inputReferringMedicalDoctor)) {
				columnValuesArray = new double[OUTPUT_TOTAL_COLUMNS];
				
				if (inputColumns[INPUT_CONSULTATION_CLASS_COLUMN].contains("HMO")) {						
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
				if (inputColumns[INPUT_CONSULTATION_CLASS_COLUMN].contains("HMO")) {
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
				if (inputColumns[INPUT_CONSULTATION_CLASS_COLUMN].contains("HMO")) {
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

/*												`		referringDoctorContainer.get(inputColumns[INPUT_CONSULTATION_MEDICAL_DOCTOR_COLUMN])[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN]++;				
					//added by Mike, 20181219
					if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().contains("p")) {
						//edited by Mike, 20181221
						//columnValuesArray[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN]++;
						referringDoctorContainer.get(inputColumns[INPUT_CONSULTATION_MEDICAL_DOCTOR_COLUMN])[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN]++;				
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
					referringDoctorContainer.get(inputColumns[INPUT_CONSULTATION_MEDICAL_DOCTOR_COLUMN])[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]++;					
					//added by Mike, 20181219
					if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().contains("p")) {
						//edited by Mike, 20181221
						//columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN]++;						
						referringDoctorContainer.get(inputColumns[INPUT_CONSULTATION_MEDICAL_DOCTOR_COLUMN])[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN]++;				
					}
*/					
				}
			}
		}
	}

	//added by Mike, 20181218
	private static void processMedicalDoctorTransactionCount(HashMap<String, double[]> medicalDoctorContainer, String[] inputColumns, Boolean isConsultation) {		
		//edited by Mike, 20181219
		if (!isConsultation) {	//only process follow-up count for Consultation transactions	
			//edited by Mike, 20190424
			String inputMedicalDoctor = inputColumns[INPUT_REFERRING_DOCTOR_COLUMN].trim().toUpperCase();
	
			System.out.println("inputMedicalDoctor: "+inputMedicalDoctor);
	
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
			//added by Mike, 20190125; edited by Mike, 20190424
			String inputMedicalDoctor = inputColumns[INPUT_CONSULTATION_MEDICAL_DOCTOR_COLUMN].trim().toUpperCase();
				
			System.out.println("CONSULTATION inputMedicalDoctor: "+inputMedicalDoctor);
				
			if (!medicalDoctorContainer.containsKey(inputMedicalDoctor)) {
				columnValuesArray = new double[OUTPUT_TOTAL_COLUMNS];
								
				if (inputColumns[INPUT_CONSULTATION_CLASS_COLUMN].contains("HMO")) {						
					if (inputColumns[INPUT_CONSULTATION_NEW_OLD_COLUMN].toLowerCase().contains("new")) {
						//added by Mike, 20181218
						columnValuesArray[OUTPUT_CONSULTATION_HMO_NEW_PATIENT_COUNT_COLUMN] = 1;
					}	
					//added by Mike, 20190202
					else if (inputColumns[INPUT_CONSULTATION_NEW_OLD_COLUMN].toLowerCase().contains("old")) {
						columnValuesArray[OUTPUT_CONSULTATION_HMO_OLD_PATIENT_COUNT_COLUMN] = 1;
					}	
					//added by Mike, 20190202
					else if ((inputColumns[INPUT_CONSULTATION_NEW_OLD_COLUMN].toLowerCase().contains("follow up")) ||
							(inputColumns[INPUT_CONSULTATION_NEW_OLD_COLUMN].toLowerCase().contains("follow-up"))) {
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
					if (inputColumns[INPUT_CONSULTATION_NEW_OLD_COLUMN].toLowerCase().contains("new")) {
						//added by Mike, 20181218
						columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_NEW_PATIENT_COUNT_COLUMN] = 1;
					}	
					//added by Mike, 20190202
					else if (inputColumns[INPUT_CONSULTATION_NEW_OLD_COLUMN].toLowerCase().contains("old")) {
						columnValuesArray[OUTPUT_CONSULTATION_HMO_OLD_PATIENT_COUNT_COLUMN] = 1;
					}	
					//added by Mike, 20190202
					else if ((inputColumns[INPUT_CONSULTATION_NEW_OLD_COLUMN].toLowerCase().contains("follow up")) ||
							(inputColumns[INPUT_CONSULTATION_NEW_OLD_COLUMN].toLowerCase().contains("follow-up"))) {
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
				if (inputColumns[INPUT_CONSULTATION_CLASS_COLUMN].contains("HMO")) {
					if (inputColumns[INPUT_CONSULTATION_NEW_OLD_COLUMN].toLowerCase().contains("new")) {
						medicalDoctorContainer.get(inputMedicalDoctor)[OUTPUT_CONSULTATION_HMO_NEW_PATIENT_COUNT_COLUMN]++;				
					}	
					//added by Mike, 20190202
					else if (inputColumns[INPUT_CONSULTATION_NEW_OLD_COLUMN].toLowerCase().contains("old")) {
						medicalDoctorContainer.get(inputMedicalDoctor)[OUTPUT_CONSULTATION_HMO_OLD_PATIENT_COUNT_COLUMN]++;
					}	
					//added by Mike, 20190202
					else if ((inputColumns[INPUT_CONSULTATION_NEW_OLD_COLUMN].toLowerCase().contains("follow up")) ||
							(inputColumns[INPUT_CONSULTATION_NEW_OLD_COLUMN].toLowerCase().contains("follow-up"))) {
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

/*												`		referringDoctorContainer.get(inputColumns[INPUT_CONSULTATION_MEDICAL_DOCTOR_COLUMN])[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN]++;				
					//added by Mike, 20181219
					if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().contains("p")) {
						//edited by Mike, 20181221
						//columnValuesArray[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN]++;
						referringDoctorContainer.get(inputColumns[INPUT_CONSULTATION_MEDICAL_DOCTOR_COLUMN])[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN]++;				
					}
*/					
				}
				else {
					if (inputColumns[INPUT_CONSULTATION_NEW_OLD_COLUMN].toLowerCase().contains("new")) {
						medicalDoctorContainer.get(inputMedicalDoctor)[OUTPUT_CONSULTATION_NON_HMO_NEW_PATIENT_COUNT_COLUMN]++;				
					}	
					//added by Mike, 20190202
					else if (inputColumns[INPUT_CONSULTATION_NEW_OLD_COLUMN].toLowerCase().contains("old")) {
						medicalDoctorContainer.get(inputMedicalDoctor)[OUTPUT_CONSULTATION_NON_HMO_OLD_PATIENT_COUNT_COLUMN]++;
					}	
					//added by Mike, 20190202
					else if ((inputColumns[INPUT_CONSULTATION_NEW_OLD_COLUMN].toLowerCase().contains("follow up")) ||
							(inputColumns[INPUT_CONSULTATION_NEW_OLD_COLUMN].toLowerCase().contains("follow-up"))) {
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
					referringDoctorContainer.get(inputColumns[INPUT_CONSULTATION_MEDICAL_DOCTOR_COLUMN])[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]++;					
					//added by Mike, 20181219
					if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN].toLowerCase().contains("p")) {
						//edited by Mike, 20181221
						//columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN]++;						
						referringDoctorContainer.get(inputColumns[INPUT_CONSULTATION_MEDICAL_DOCTOR_COLUMN])[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN]++;				
					}
*/					
				}
			}
		}
	}
	
	//added by Mike, 20181220
	private static void processMedicalDoctorTransactionPerClassificationCount(HashMap<String, HashMap<String, double[]>> classificationContainerPerMedicalDoctor, String[] inputColumns, Boolean isConsultation) {				

		String medicalDoctorKey = inputColumns[INPUT_CONSULTATION_MEDICAL_DOCTOR_COLUMN].trim().toUpperCase();
	
		if (isConsultation) {			
			String classificationName = inputColumns[INPUT_CONSULTATION_CLASS_COLUMN].trim().toUpperCase(); //added by Mike, 20181220
//			System.out.println(">"+" "+classificationName);
//			System.out.println(">>> "+inputColumns[INPUT_CONSULTATION_MEDICAL_DOCTOR_COLUMN]);

				if (!classificationName.contains("HMO")) {					
//			System.out.println(">>>"+inputColumns[INPUT_CONSULTATION_MEDICAL_DOCTOR_COLUMN]+" "+classificationName);

/*					classificationContainerPerMedicalDoctor.get(inputColumns[INPUT_CONSULTATION_MEDICAL_DOCTOR_COLUMN]).get(classificationName)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]++;
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

					

//					System.out.println(">>> NON-HMO count: "+classificationContainerPerMedicalDoctor.get(inputColumns[INPUT_CONSULTATION_MEDICAL_DOCTOR_COLUMN]).get(classificationName)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]);
				}
				else {
//				System.out.println(">>>>>"+inputColumns[INPUT_CONSULTATION_MEDICAL_DOCTOR_COLUMN]+" "+classificationName);
/*
					classificationContainerPerMedicalDoctor.get(inputColumns[INPUT_CONSULTATION_MEDICAL_DOCTOR_COLUMN]).get(classificationName)[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN]++;					
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

//					System.out.println(">>>>> HMO count: "+classificationContainerPerMedicalDoctor.get(inputColumns[INPUT_CONSULTATION_MEDICAL_DOCTOR_COLUMN]).get(classificationName)[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN]);

				}
		}		
	}

	private static void setClassificationContainerPerMedicalDoctor(HashMap<String, HashMap<String, double[]>> classificationContainerPerMedicalDoctor) {
		SortedSet<String> sortedHmoContainerKeyset = new TreeSet<String>(hmoContainer.keySet());
		SortedSet<String> sortedNonHmoContainerKeyset = new TreeSet<String>(nonHmoContainer.keySet());
		SortedSet<String> sortedMedicalDoctorKeyset = new TreeSet<String>(medicalDoctorContainer.keySet());
				
		for (String medicalDoctorKey : sortedMedicalDoctorKeyset) {			
//TO-DO: resolve issue with OR# (Consultation) also being added into medicalDoctorContainer		
System.out.println("medical doctor: "+medicalDoctorKey);		
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

	//added by Mike, 20190412
	private static void processKnownDiagnosedCasesInputFile(String[] args) throws Exception {
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
			
			if (!inputFilename.toLowerCase().contains("assets")) {
				continue;
			}					
									
			Scanner sc = new Scanner(new FileInputStream(f));				
		
			String s;		
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

//				System.out.println(s);
				
				knownDiagnosedCasesContainer.put(inputColumns[INPUT_KNOWN_DIAGNOSED_CASES_LIST_SUB_CLASSIFICATION_COLUMN].toUpperCase(),
												 inputColumns[INPUT_KNOWN_DIAGNOSED_CASES_LIST_CLASSIFICATION_COLUMN].toUpperCase());
				
/*				
				SortedSet<String> sortedKnownDiagnosedCasesKeyset = new TreeSet<String>(knownDiagnosedCasesContainer.keySet());
				for (String key : sortedKnownDiagnosedCasesKeyset) {	
					System.out.println(key + " : " + knownDiagnosedCasesContainer.get(key));
				}
*/
				
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
/*
				if (isPhaseOne) {
					//TO-DO: -add: handle consultation transactions
					processDiagnosedCasesCount(diagnosedCasesContainer, inputColumns, isConsultation); //edited by Mike, 20181219
				}
				else {
					//added by Mike, 20181220
					processMedicalDoctorTransactionPerClassificationCount(classificationContainerPerMedicalDoctor, inputColumns, isConsultation);
				}
*/				
			}		
		}		

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
			
			//added by Mike, 20190413
			if (inputFilename.toLowerCase().contains("assets")) {
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
			
			//edited by Mike, 20190415
			//Note that there is no table header in the input file.
/*			s=sc.nextLine(); //skip the first row, which is the input file's table headers
*/	
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
					processMonthlyCount(dateContainer, inputColumns, i, isConsultation); //isConsultation = false
					
					//added by Mike, 20181217
					processHMOCount(hmoContainer, inputColumns, isConsultation); //edited by Mike, 20181219
					
					//added by Mike, 20181217
					processNONHMOCount(nonHmoContainer, inputColumns, isConsultation); //edited by Mike, 20181219
/*					
					//added by Mike, 20181218
					processReferringDoctorTransactionCount(referringDoctorContainer, inputColumns, isConsultation); //edited by Mike, 20181219
*/			
					//added by Mike, 20181220
	//				processMedicalDoctorTransactionPerClassificationCount(classificationContainerPerMedicalDoctor, inputColumns, isConsultation);
	
					//edited by Mike, 20190426
					if (isConsultation) {
						//added by Mike, 20190202
						processMedicalDoctorTransactionCount(medicalDoctorContainer, inputColumns, isConsultation);						
					}
					else {
						processMedicalDoctorTransactionCount(referringDoctorContainer, inputColumns, isConsultation);						

						//added by Mike, 20190413
						processDiagnosedCasesCount(diagnosedCasesContainer, inputColumns, isConsultation); 
					}
				}
				else {
					//added by Mike, 20181220
					processMedicalDoctorTransactionPerClassificationCount(classificationContainerPerMedicalDoctor, inputColumns, isConsultation);					
				}
			}		
			//added by Mike, 20181205
			columnValuesArray[OUTPUT_DATE_ID_COLUMN] = i; 			
		}		

	}
	
	private static void processWriteOutputFileTreatment(PrintWriter writer) throws Exception {
		File f = new File(inputOutputTemplateFilenameTreatment+".html");

		System.out.println("inputOutputTemplateFilenameTreatment: " + inputOutputTemplateFilenameTreatment);
								
		Scanner sc = new Scanner(new FileInputStream(f), "UTF-8");				
	
		String s;		
//			s=sc.nextLine(); //skip the first row, which is the input file's table headers

		if (isInDebugMode) {
			rowCount=0;
		}

		//added by Mike, 20190417
		SortedSet<String> sortedClassifiedKeyset = new TreeSet<String>(classifiedDiagnosedCasesContainer.keySet());
	
		//added by Mike, 20190417
		Set<Entry<String, Integer>> classifiedDiagnosedCasesContainerSet = classifiedDiagnosedCasesContainer.entrySet();
        List<Entry<String, Integer>> sortedClassifiedDiagnosedCasesContainerList = new ArrayList<Entry<String, Integer>>(classifiedDiagnosedCasesContainerSet);
		
		//edited by Mike, 20190418
		//Removed inner class "Comparator" so that there will be no "generateMonthlySummaryReportWithDiagnosedCasesOfAllInputFiles$1.class" after compiling the "generateMonthlySummaryReportWithDiagnosedCasesOfAllInputFiles.java"
		/*Collections.sort(sortedClassifiedDiagnosedCasesContainerList, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1,
                    Map.Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });*/
		
        Collections.sort(sortedClassifiedDiagnosedCasesContainerList, new IncidenceNumberComparator());

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
			
			//added by Mike, 20190414
			//This is to resolve the following character-encoding issue.
			//This is not anymore necessary due to setting the scanner to use UTF-8
//			s = s.replace("Â", "");

//			System.out.println("s: "+s);
//			System.out.println("totalTreatmentCount: "+totalTreatmentCount);
			
			//added by Mike, 20190417
			s = s.replace("<?php echo $data['date'];?>", "" + dateValuesArray[0].toUpperCase());
			
			s = s.replace("<?php echo $data['total_treatment_count'];?>", "" + totalTreatmentCount);

			//added by Mike, 20190416
			s = s.replace("<?php echo $data['total_new_patients_count'];?>", "" + totalNewPatientReferralTransactionCount);

			//added by Mike, 20190416; edited by Mike, 20190425
			totalWiValue = 0;
			if (nonHmoContainer.containsKey(classificationWI)) {
				totalWiValue = (int) nonHmoContainer.get(classificationWI)[OUTPUT_NON_HMO_COUNT_COLUMN];
			}			
			s = s.replace("<?php echo $data['total_wi_count'];?>", "" + totalWiValue);

			//added by Mike, 20190416; edited by Mike, 20190425
			slcValue = 0;
			if (nonHmoContainer.containsKey(classificationSLC)) {
				slcValue = (int) nonHmoContainer.get(classificationSLC)[OUTPUT_NON_HMO_COUNT_COLUMN];
			}			
			s = s.replace("<?php echo $data['total_slc_count'];?>", "" + slcValue);

			//edited by Mike, 20190425
			pwdValue = 0;
			if (nonHmoContainer.containsKey(classificationPWD)) {
				pwdValue = (int) nonHmoContainer.get(classificationPWD)[OUTPUT_NON_HMO_COUNT_COLUMN];
			}			
			s = s.replace("<?php echo $data['total_pwd_count'];?>", "" + pwdValue);

			//edited by Mike, 20190425
			scValue = 0;
			if (nonHmoContainer.containsKey(classificationSC)) {
				scValue = (int) nonHmoContainer.get(classificationSC)[OUTPUT_NON_HMO_COUNT_COLUMN];
			}			
			s = s.replace("<?php echo $data['total_sc_count'];?>", "" + scValue);
			
			//added by Mike, 20190417
			s = s.replace("<?php echo $data['total_hmo_count'];?>", "" + (int) totalTreatmentHMOCount);			
									
			//added by Mike, 20190417
			if (s.contains("<!-- Table Values: NEW CASES -->")) {						  
				for (String key : sortedClassifiedKeyset) {	
					s = s.concat("\n");
					s = s.concat("\t\t\t\t\t  <tr>\n");
					s = s.concat("\t\t\t\t\t	  <!-- Column 1 -->\n");
					s = s.concat("\t\t\t\t\t     <td>\n");
					s = s.concat("\t\t\t\t\t		  <b><span>" + key + "</span></b>\n");
					s = s.concat("\t\t\t\t\t	  </td>\n");
					s = s.concat("\t\t\t\t\t	  <!-- Column 2 -->\n");
					s = s.concat("\t\t\t\t\t	  <td>\n");
					s = s.concat("\t\t\t\t\t	  <b><span>" + classifiedDiagnosedCasesContainer.get(key) + "</span></b>\n");
					s = s.concat("\t\t\t\t\t     </td>\n");
					s = s.concat("\t\t\t\t\t  </tr>");
				}
			}
			
			//added by Mike, 20190417
			if (s.contains("<b><span><?php echo $value['name'];?>;&nbsp;</span></b>")) {
				s = s.replace("<b><span><?php echo $value['name'];?>;&nbsp;</span></b>", "<b><span>");

				//Write the names of the top 3 cases, separated by a semicolon and a blank space				
				int rankCount = 0;				
				for (Entry<String, Integer> entry : sortedClassifiedDiagnosedCasesContainerList) {
					if (rankCount < 2) {
						s = s.concat(entry.getKey()+"; ");					
						rankCount++;
					}
					else {
						s = s.concat(entry.getKey());					
						break;
					}
				}
				s = s.concat("</span></b>");			
			}
			
			writer.print(s + "\n");
		}
		
		writer.close();
	}

	//added by Mike, 20190422
	private static void processWriteOutputFileConsultation(PrintWriter writer) throws Exception {
		File f = new File(inputOutputTemplateFilenameConsultation+".html");

		System.out.println("inputOutputTemplateFilenameConsultation: " + inputOutputTemplateFilenameConsultation);
								
		Scanner sc = new Scanner(new FileInputStream(f), "UTF-8");				
	
		String s;		
//			s=sc.nextLine(); //skip the first row, which is the input file's table headers

		if (isInDebugMode) {
			rowCount=0;
		}

		//added by Mike, 20190417
		SortedSet<String> sortedClassifiedKeyset = new TreeSet<String>(classifiedDiagnosedCasesContainer.keySet());
		//added by Mike, 20190424
		SortedSet<String> sortedMedicalDoctorTransactionCountKeyset = new TreeSet<String>(medicalDoctorContainer.keySet());
			
		//added by Mike, 20190417
		Set<Entry<String, Integer>> classifiedDiagnosedCasesContainerSet = classifiedDiagnosedCasesContainer.entrySet();
        List<Entry<String, Integer>> sortedClassifiedDiagnosedCasesContainerList = new ArrayList<Entry<String, Integer>>(classifiedDiagnosedCasesContainerSet);
		
/*		//added by Mike, 20190424
		Set<Entry<String, Integer>> medicalDoctorContainerSet = medicalDoctorContainer.entrySet();
        List<Entry<String, Integer>> sortedMedicalDoctorContainerList = new ArrayList<Entry<String, Integer>>(medicalDoctorContainerSet);
*/		
		
		//edited by Mike, 20190418
		//Removed inner class "Comparator" so that there will be no "generateMonthlySummaryReportWithDiagnosedCasesOfAllInputFiles$1.class" after compiling the "generateMonthlySummaryReportWithDiagnosedCasesOfAllInputFiles.java"
		/*Collections.sort(sortedClassifiedDiagnosedCasesContainerList, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1,
                    Map.Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });*/
		
        Collections.sort(sortedClassifiedDiagnosedCasesContainerList, new IncidenceNumberComparator());
        /*Collections.sort(sortedMedicalDoctorContainerList, new IncidenceNumberComparator()); //added by Mike, 20190424
*/

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
			
			//added by Mike, 20190414
			//This is to resolve the following character-encoding issue.
			//This is not anymore necessary due to setting the scanner to use UTF-8
//			s = s.replace("Â", "");

//			System.out.println("s: "+s);
//			System.out.println("totalTreatmentCount: "+totalTreatmentCount);
			
			//added by Mike, 20190417
			s = s.replace("<?php echo $data['date'];?>", "" + dateValuesArray[0].toUpperCase());
			
			s = s.replace("<?php echo $data['total_consultation_count'];?>", "" + totalConsultationCount);

			//added by Mike, 20190416; edited by Mike, 20190423
			s = s.replace("<?php echo $data['total_new_patients_count'];?>", "" + (int) totalNewPatientPerDoctorCount);

			//added by Mike, 20190423
			s = s.replace("<?php echo $data['total_follow_up_patients_count'];?>", "" + (int) totalFollowUpPerDoctorCount);

			//added by Mike, 20190423
			s = s.replace("<?php echo $data['total_old_patients_count'];?>", "" + (int) totalOldPatientPerDoctorCount);
			
			//added by Mike, 20190423
			s = s.replace("<?php echo $data['total_procedure_count'];?>", "" + (int) totalProcedurePerDoctorCount);
			
			//added by Mike, 20190416; edited by Mike 20190425
			totalWiValue = 0;			
			if (nonHmoContainer.containsKey(classificationWI)) {
				totalWiValue = (int) nonHmoContainer.get(classificationWI)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN];
			}
					
			//added by Mike, 20190424; edited by Mike, 20190425
			if (nonHmoContainer.containsKey(classificationWI_MCDO)) {
				totalWiValue += (int) nonHmoContainer.get(classificationWI_MCDO)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN];
	
				s = s.replace("<?php echo $data['total_wi_mcdo_count'];?>", "" + (int) nonHmoContainer.get(classificationWI_MCDO)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]);				
			}			
			s = s.replace("<?php echo $data['total_wi_count'];?>", "" + totalWiValue);			
	
			//added by Mike, 20190425			
			noChargeValue = 0;
			if (nonHmoContainer.containsKey(classificationNO_CHARGE)) {
				noChargeValue = (int) nonHmoContainer.get(classificationNO_CHARGE)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN];
			}
			s = s.replace("<?php echo $data['total_no_charge_count'];?>", "" + noChargeValue);
			
			//added by Mike, 20190416; edited by Mike, 20190425
			slcValue = 0;
			if (nonHmoContainer.containsKey(classificationSLC)) {
				slcValue = (int) nonHmoContainer.get(classificationSLC)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN];
			}			
			s = s.replace("<?php echo $data['total_slc_count'];?>", "" + slcValue);

			//added by Mike, 20190425
			slrValue = 0;
			if (nonHmoContainer.containsKey(classificationSLR)) {
				slrValue = (int) nonHmoContainer.get(classificationSLR)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN];
			}			
			s = s.replace("<?php echo $data['total_slr_count'];?>", "" + slrValue);

			//added by Mike, 20190425
			scValue = 0;
			if (nonHmoContainer.containsKey(classificationSC)) {
				scValue = (int) nonHmoContainer.get(classificationSC)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN];
			}			
			s = s.replace("<?php echo $data['total_sc_count'];?>", "" + scValue);

			//added by Mike, 20190425
			pwdValue = 0;
			if (nonHmoContainer.containsKey(classificationPWD)) {
				pwdValue = (int) nonHmoContainer.get(classificationPWD)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN];
			}			
			s = s.replace("<?php echo $data['total_pwd_count'];?>", "" + pwdValue);
			
			//added by Mike, 20190417
			s = s.replace("<?php echo $data['total_hmo_count'];?>", "" + (int) totalConsultationHMOCount);


/* //The following set of instructions are my guide.			
			//added by Mike, 20190424
			for (String key : sortedMedicalDoctorTransactionCountKeyset) {	
			double count = medicalDoctorContainer.get(key)[OUTPUT_HMO_COUNT_COLUMN] + medicalDoctorContainer.get(key)[OUTPUT_NON_HMO_COUNT_COLUMN];

			double newPatientReferralTransactionCount = medicalDoctorContainer.get(key)[OUTPUT_HMO_NEW_PATIENT_COUNT_COLUMN] + medicalDoctorContainer.get(key)[OUTPUT_NON_HMO_NEW_PATIENT_COUNT_COLUMN];

			//added by Mike, 20181219
			double consultationCount = medicalDoctorContainer.get(key)[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN] + medicalDoctorContainer.get(key)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN];

			//added by Mike, 20181219
			double procedureCount = medicalDoctorContainer.get(key)[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN] + medicalDoctorContainer.get(key)[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN];

			//added by Mike, 20190109
			double medicalCertificateCount = medicalDoctorContainer.get(key)[OUTPUT_CONSULTATION_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN] + medicalDoctorContainer.get(key)[OUTPUT_CONSULTATION_NON_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN];

			//added by Mike, 20190202
			double newPatientCount = medicalDoctorContainer.get(key)[OUTPUT_CONSULTATION_HMO_NEW_PATIENT_COUNT_COLUMN] + medicalDoctorContainer.get(key)[OUTPUT_CONSULTATION_NON_HMO_NEW_PATIENT_COUNT_COLUMN];

			//added by Mike, 20190202
			double followUpCount = medicalDoctorContainer.get(key)[OUTPUT_CONSULTATION_HMO_FOLLOW_UP_COUNT_COLUMN] + medicalDoctorContainer.get(key)[OUTPUT_CONSULTATION_NON_HMO_FOLLOW_UP_COUNT_COLUMN];

			//added by Mike, 20190202
			double oldPatientCount = medicalDoctorContainer.get(key)[OUTPUT_CONSULTATION_HMO_OLD_PATIENT_COUNT_COLUMN] + medicalDoctorContainer.get(key)[OUTPUT_CONSULTATION_NON_HMO_OLD_PATIENT_COUNT_COLUMN];
*/						
			//added by Mike, 20190417; edited by Mike, 20190424
			if (s.contains("<!-- Table Values: MONTHLY SUMMARY FOR EACH MEDICAL DOCTOR PRESENT -->")) {			
				double consultationCount = 0;
				for (String key : sortedMedicalDoctorTransactionCountKeyset) {					
					//edited by Mike, 20190426
					consultationCount = medicalDoctorContainer.get(key)[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN] + medicalDoctorContainer.get(key)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN];
				
					if (consultationCount == 0) {
						continue;
					}
				
					s = s.concat("\n");
					s = s.concat("\t\t\t\t\t  <table width=\"50%\" class=\"consultation_census\">\n");
					s = s.concat("\t\t\t\t\t  <!-- Table Values Row 1 -->\n");

					//This portion is for the Medical Doctor's name
					s = s.concat("\t\t\t\t\t  <tr>\n");
					s = s.concat("\t\t\t\t\t	  <!-- Column 1 -->\n");
					s = s.concat("\t\t\t\t\t      <td>\n");
					s = s.concat("\t\t\t\t\t		  <b><span>" + key + "</span></b>\n");
					s = s.concat("\t\t\t\t\t	  </td>\n");
					s = s.concat("\t\t\t\t\t	  <!-- Column 2: Blank -->\n");
					s = s.concat("\t\t\t\t\t	  <td>\n");
					s = s.concat("\t\t\t\t\t	  <!-- This is blank -->\n");
					s = s.concat("\t\t\t\t\t	  <br />\n");
					s = s.concat("\t\t\t\t\t	  </td>\n");
					s = s.concat("\t\t\t\t\t  </tr>\n");

					//This portion is for the Medical Doctor's total consult count
					//edited by Mike, 20190426
					s = s.concat("\t\t\t\t\t  <tr>\n");
					s = s.concat("\t\t\t\t\t	  <!-- Column 1 -->\n");
					s = s.concat("\t\t\t\t\t      <td>\n");
					s = s.concat("\t\t\t\t\t		  <b><span>CONSULT:</span></b>\n");
					s = s.concat("\t\t\t\t\t	  </td>\n");
					s = s.concat("\t\t\t\t\t	  <!-- Column 2: Blank -->\n");
					s = s.concat("\t\t\t\t\t	  <td>\n");
					s = s.concat("\t\t\t\t\t		  <b><span>" + (int) consultationCount + "</span></b>\n");
					s = s.concat("\t\t\t\t\t	  <br />\n");
					s = s.concat("\t\t\t\t\t	  </td>\n");
					s = s.concat("\t\t\t\t\t  </tr>\n");

					//This portion is for the Medical Doctor's total count of new patient transactions
					double newPatientReferralTransactionCount = medicalDoctorContainer.get(key)[OUTPUT_HMO_NEW_PATIENT_COUNT_COLUMN] + medicalDoctorContainer.get(key)[OUTPUT_NON_HMO_NEW_PATIENT_COUNT_COLUMN];
					s = s.concat("\t\t\t\t\t  <tr>\n");
					s = s.concat("\t\t\t\t\t	  <!-- Column 1 -->\n");
					s = s.concat("\t\t\t\t\t      <td>\n");
					s = s.concat("\t\t\t\t\t		  <b><span>NEW:</span></b>\n");
					s = s.concat("\t\t\t\t\t	  </td>\n");
					s = s.concat("\t\t\t\t\t	  <!-- Column 2: Blank -->\n");
					s = s.concat("\t\t\t\t\t	  <td>\n");
					s = s.concat("\t\t\t\t\t		  <b><span>" + (int) newPatientReferralTransactionCount + "</span></b>\n");
					s = s.concat("\t\t\t\t\t	  <br />\n");
					s = s.concat("\t\t\t\t\t	  </td>\n");
					s = s.concat("\t\t\t\t\t  </tr>\n");

					//This portion is for the Medical Doctor's total count of follow-up patient transactions
					double followUpCount = medicalDoctorContainer.get(key)[OUTPUT_CONSULTATION_HMO_FOLLOW_UP_COUNT_COLUMN] + medicalDoctorContainer.get(key)[OUTPUT_CONSULTATION_NON_HMO_FOLLOW_UP_COUNT_COLUMN];
					s = s.concat("\t\t\t\t\t  <tr>\n");
					s = s.concat("\t\t\t\t\t	  <!-- Column 1 -->\n");
					s = s.concat("\t\t\t\t\t      <td>\n");
					s = s.concat("\t\t\t\t\t		  <b><span>FOLLOW-UP:</span></b>\n");
					s = s.concat("\t\t\t\t\t	  </td>\n");
					s = s.concat("\t\t\t\t\t	  <!-- Column 2: Blank -->\n");
					s = s.concat("\t\t\t\t\t	  <td>\n");
					s = s.concat("\t\t\t\t\t		  <b><span>" + (int) followUpCount + "</span></b>\n");
					s = s.concat("\t\t\t\t\t	  <br />\n");
					s = s.concat("\t\t\t\t\t	  </td>\n");
					s = s.concat("\t\t\t\t\t  </tr>\n");

					//This portion is for the Medical Doctor's total count of old patient transactions
					double oldPatientCount = medicalDoctorContainer.get(key)[OUTPUT_CONSULTATION_HMO_OLD_PATIENT_COUNT_COLUMN] + medicalDoctorContainer.get(key)[OUTPUT_CONSULTATION_NON_HMO_OLD_PATIENT_COUNT_COLUMN];
					s = s.concat("\t\t\t\t\t  <tr>\n");
					s = s.concat("\t\t\t\t\t	  <!-- Column 1 -->\n");
					s = s.concat("\t\t\t\t\t      <td>\n");
					s = s.concat("\t\t\t\t\t		  <b><span>OLD:</span></b>\n");
					s = s.concat("\t\t\t\t\t	  </td>\n");
					s = s.concat("\t\t\t\t\t	  <!-- Column 2: Blank -->\n");
					s = s.concat("\t\t\t\t\t	  <td>\n");
					s = s.concat("\t\t\t\t\t		  <b><span>" + (int) oldPatientCount + "</span></b>\n");
					s = s.concat("\t\t\t\t\t	  <br />\n");
					s = s.concat("\t\t\t\t\t	  </td>\n");
					s = s.concat("\t\t\t\t\t  </tr>\n");

					//This portion is for the Medical Doctor's total count of procedure transactions
					double procedureCount = medicalDoctorContainer.get(key)[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN] + medicalDoctorContainer.get(key)[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN];
					s = s.concat("\t\t\t\t\t  <tr>\n");
					s = s.concat("\t\t\t\t\t	  <!-- Column 1 -->\n");
					s = s.concat("\t\t\t\t\t      <td>\n");
					s = s.concat("\t\t\t\t\t		  <b><span>PROCEDURE/S:</span></b>\n");
					s = s.concat("\t\t\t\t\t	  </td>\n");
					s = s.concat("\t\t\t\t\t	  <!-- Column 2: Blank -->\n");
					s = s.concat("\t\t\t\t\t	  <td>\n");
					s = s.concat("\t\t\t\t\t		  <b><span>" + (int) procedureCount + "</span></b>\n");
					s = s.concat("\t\t\t\t\t	  <br />\n");
					s = s.concat("\t\t\t\t\t	  </td>\n");
					s = s.concat("\t\t\t\t\t  </tr>\n");
					
					
					s = s.concat("\t\t\t\t\t  </table>\n");
					s = s.concat("\t\t\t\t\t  <br />");
					
					
/*					
					s = s.concat("\t\t\t\t\t	  <b><span>" + classifiedDiagnosedCasesContainer.get(key) + "</span></b>\n");
					s = s.concat("\t\t\t\t\t     </td>\n");
					s = s.concat("\t\t\t\t\t  </tr>");
*/					
				}
			}			  
/*	//The following set of instructions are my guide.		
			<table width="50%" class="consultation_census">
					<?php
						foreach ($result as $value) {
					?>
					  <!-- Table Values Row 1 -->
					  <tr>
						<!-- Column 1 -->
						<td>
							<!-- Replace this with the actual value -->
							<b><span>LAST NAME OF MEDICAL DOCTOR</span></b>
						</td>
						<!-- Column 2: Blank -->
						<td>
							<!-- This is blank -->
							<br />
						</td>
					  </tr>
					  <!-- Table Values Row 2 -->
					  <tr>
						<!-- Column 1 -->
						<td>
						  <b><span>CONSULT:</span></b>
						</td>
						<!-- Column 2 -->
						<td>
						  <!-- The value here is for each Medical Doctor present -->
						  <b><span><?php echo $value['total_consultation_count'];?></span></b>
						</td>
					  </tr>
					  <!-- Table Values Row 3 -->
					  <tr>
						<!-- Column 1 -->
						<td>
						  <b><span>NEW:</span></b>
						</td>
						<!-- Column 2 -->
						<td>
						  <!-- The value here is for each Medical Doctor present -->
						  <b><span><?php echo $value['total_new_patients_count'];?></span></b>
						</td>
					  </tr>
					  <!-- Table Values Row 4 -->
					  <tr>
						<!-- Column 1 -->
						<td>
						  <b><span>FOLLOW-UP:</span></b>
						</td>
						<!-- Column 2 -->
						<td>
						  <!-- The value here is for each Medical Doctor present -->
						  <b><span><?php echo $value['total_follow_up_patients_count'];?></span></b>
						</td>
					  </tr>
					  <!-- Table Values Row 5 -->
					  <tr>
						<!-- Column 1 -->
						<td>
						  <b><span>OLD:</span></b>
						</td>
						<!-- Column 2 -->
						<td>
						  <b><span><?php echo $value['total_old_patients_count'];?></span></b>
						</td>
					  </tr>
					  <!-- Table Values Row 6 -->
					  <tr>
						<!-- Column 1 -->
						<td>
						  <b><span>PROCEDURE/S:</span></b>
						</td>
						<!-- Column 2 -->
						<td>
						  <b><span><?php echo $value['total_procedure_count'];?></span></b>
						</td>
					  </tr>
					<?php
						}
					?>		
*/
					
/*			
			if (s.contains("<!-- Table Values: NEW CASES -->")) {						  
				for (String key : sortedClassifiedKeyset) {	
					s = s.concat("\n");
					s = s.concat("\t\t\t\t\t  <tr>\n");
					s = s.concat("\t\t\t\t\t	  <!-- Column 1 -->\n");
					s = s.concat("\t\t\t\t\t     <td>\n");
					s = s.concat("\t\t\t\t\t		  <b><span>" + key + "</span></b>\n");
					s = s.concat("\t\t\t\t\t	  </td>\n");
					s = s.concat("\t\t\t\t\t	  <!-- Column 2 -->\n");
					s = s.concat("\t\t\t\t\t	  <td>\n");
					s = s.concat("\t\t\t\t\t	  <b><span>" + classifiedDiagnosedCasesContainer.get(key) + "</span></b>\n");
					s = s.concat("\t\t\t\t\t     </td>\n");
					s = s.concat("\t\t\t\t\t  </tr>");
				}
			}
			
			//added by Mike, 20190417
			if (s.contains("<b><span><?php echo $value['name'];?>;&nbsp;</span></b>")) {
				s = s.replace("<b><span><?php echo $value['name'];?>;&nbsp;</span></b>", "<b><span>");

				//Write the names of the top 3 cases, separated by a semicolon and a blank space				
				int rankCount = 0;				
				for (Entry<String, Integer> entry : sortedClassifiedDiagnosedCasesContainerList) {
					if (rankCount < 2) {
						s = s.concat(entry.getKey()+"; ");					
						rankCount++;
					}
					else {
						s = s.concat(entry.getKey());					
						break;
					}
				}
				s = s.concat("</span></b>");			
			}
*/			
			writer.print(s + "\n");
		}
		
		writer.close();
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

	//added by Mike, 20190413
	private static void processDiagnosedCasesCount(HashMap<String, Integer> diagnosedCasesContainer, String[] inputColumns, boolean isConsultation) {
			String diagnosedCaseName = inputColumns[INPUT_DIAGNOSIS_COLUMN].trim().toUpperCase();
			
			if (!isConsultation) {											
				if (inputColumns[INPUT_NEW_OLD_PATIENT_COLUMN].trim().toLowerCase().contains("new")) {
					
					//added by Mike, 20190426
					String[] diagnosedCaseNameArray = diagnosedCaseName.split(",");

					for (int i=0; i< diagnosedCaseNameArray.length; i++) {										
						diagnosedCaseName = diagnosedCaseNameArray[i].replace("\"", "").trim();
					
						if (diagnosedCaseName.equals("")){
							System.out.println("STOP!");
						}
						
						if (!diagnosedCasesContainer.containsKey(diagnosedCaseName)) {
							diagnosedCasesContainer.put(diagnosedCaseName, 1);
						}					
						else {
							int currentValue = diagnosedCasesContainer.get(diagnosedCaseName);
							diagnosedCasesContainer.put(diagnosedCaseName, currentValue++); //the existing value of the key is replaced
						}	
					}
/*					
					if (diagnosedCaseName.equals("")){
						System.out.println("STOP!");
					}
					
					if (!diagnosedCasesContainer.containsKey(diagnosedCaseName)) {
						diagnosedCasesContainer.put(diagnosedCaseName, 1);
					}					
					else {
						int currentValue = diagnosedCasesContainer.get(diagnosedCaseName);
						diagnosedCasesContainer.put(diagnosedCaseName, currentValue++); //the existing value of the key is replaced
					}
*/					
				}
			}
			else {	//TO-DO: -add: handle Consultation transactions
			}
	}	
	
	//added by Mike, 20190412
	private static void processDiagnosisClassification() {
		SortedSet<String> sortedKeyset = new TreeSet<String>(diagnosedCasesContainer.keySet());
		SortedSet<String> sortedKnownDiagnosedCasesKeyset = new TreeSet<String>(knownDiagnosedCasesContainer.keySet());
		
		String classificationKey = "";
		String subClassification = ""; 
		String classification = "";
		
		boolean hasKnownDiagnosedCaseKeywords=false;
		
		for (String inputString : sortedKeyset) {			
			//added by Mike, 20190224
			String[] inputStringArray = inputString.replace("-"," ").split(" ");				
//			System.out.println(">>>>>>> inputString: "+inputString);

			for (String knownDiagnosedCasesKey : sortedKnownDiagnosedCasesKeyset) {	 //the key is the sub-classification
				hasKnownDiagnosedCaseKeywords=false;
				subClassification = knownDiagnosedCasesKey; 
				classification = knownDiagnosedCasesContainer.get(knownDiagnosedCasesKey);
/*				
				if (inputString.toLowerCase().contains("trigger")) {					
					System.out.println(">>>>>>> inputString: "+inputString);
				}
				
				if (subClassification.toLowerCase().contains("trigger")) {
				System.out.println(">>> subClassification: "+subClassification);
				System.out.println(">>> classification: "+classification);
				}
*/

				String[] s = subClassification.split(" ");
				
//				System.out.println(">>> subClassification: "+subClassification);
				
				for(int i=0; i<s.length; i++) {			
//					System.out.println(">>>> : "+s[i]);

					int k;
					for(k=0; k<inputStringArray.length; k++) {		
//						System.out.println(">> "+inputStringArray[k]);
						
						if (inputStringArray[k].trim().toUpperCase().equals(s[i].trim().toUpperCase())) {
							hasKnownDiagnosedCaseKeywords=true;
							break;
						}
//						else {
//							System.out.println(">> true: "+inputString +" : "+s[i]);
//						}						
					}

					if (k==inputStringArray.length) {
						hasKnownDiagnosedCaseKeywords=false;
						break;
					}
				}			
				if (hasKnownDiagnosedCaseKeywords) {
					break;
				}
/*				
				for(int i=0; i<s.length; i++) {					
					if (!inputString.contains(s[i])) {
						hasKnownDiagnosedCaseKeywords=false;
						break;
					}
					else {
												System.out.println(">> true: "+inputString +" : "+s[i]);

					}
					hasKnownDiagnosedCaseKeywords=true;
				}
*/
/*
				classificationKey = inputString;
				if (hasKnownDiagnosedCaseKeywords) {
					classificationKey = classification;
					
					if (inputString.toLowerCase().contains("trigger")) {					
						System.out.println(">>> inputString: "+inputString);
						System.out.println(">>> classificationKey: "+classificationKey);
					}

					break;
				}
*/				
//				System.out.println(knownDiagnosedCasesKey + " : " + knownDiagnosedCasesContainer.get(key));
			}
			
			classificationKey = inputString;
			if (hasKnownDiagnosedCaseKeywords) {
				classificationKey = classification;
				
//				if (inputString.toLowerCase().contains("trigger")) {					
//					System.out.println(">>> inputString: "+inputString);
//					System.out.println(">>> classificationKey: "+classificationKey);
//				}

//				break;
			}

			if (!classifiedDiagnosedCasesContainer.containsKey(classificationKey)) {
				classifiedDiagnosedCasesContainer.put(classificationKey,1);
			}
			else {
				int currentCount = classifiedDiagnosedCasesContainer.get(classificationKey);
//				System.out.println(">> classificationKey: "+classificationKey+" : "+currentCount);
				classifiedDiagnosedCasesContainer.put(classificationKey,currentCount+1);
			}

			
/*			
			double diagnosedCaseCount = diagnosedCasesContainer.get(key);
			
			writer.println(
							key + "\t" + 
							diagnosedCaseCount+"\n"							
						); 				   						
*/						
		}

//		SortedSet<String> sortedClassifiedDiagnosedCasesKeyset = new TreeSet<String>(classifiedDiagnosedCasesContainer.keySet());	
		
//		for (String key : sortedClassifiedDiagnosedCasesKeyset) {			
//			int diagnosedCaseCount = classifiedDiagnosedCasesContainer.get(key);
//			
//			System.out.print(
//							key + "\t" + 
//							diagnosedCaseCount+"\n"							
//						); 				   						
//		}

	}
}