/*
 * Copyright 2018~2021 SYSON, MICHAEL B.
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
 *
 * @company: USBONG
 * @author: SYSON, MICHAEL B.
 * @date created: 2018
 * @last updated: 20211228
 * @website address: http://www.usbong.ph
 */
import java.util.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.text.NumberFormat;
import java.text.DecimalFormat;

//added by Mike, 20201228
import java.text.ParsePosition;

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
' 1) Auto-generated Annual Year End Summary Report
' --> "Tab delimited" .txt file 
' --> Regardless of the name of the input file or input files, the output file will be "AnnualYearEndSummaryReportOutput.txt".
'
' Notes:
' 1) To execute the add-on software/application simply use the following command:
'   java generateAnnualYearEndSummaryReportOfAllInputFiles input_201801.txt
' 
' where: "input_201801.txt" is the name of the file.
' 
' 2) To execute a set of input files, e.g. input201801.txt, input201802.txt, you can use the following command: 
'   java generateAnnualYearEndSummaryReportOfAllInputFiles input*
' 
' where: "input*" means any file in the directory that starts with "input".
'
' 3) Make sure to include "Consultation" in the input file name.
' --> This is so that the add-on software would be able to properly identify it as a set of "Consultation" transactions, instead of those of "Treatment".
' --> Example: inputConsultation201801.txt
'
' 4) If you use space in your file name, e.g. "input Consultation 201801.txt", you will have to execute the input files as follows.
'   java generateAnnualYearEndSummaryReportOfAllInputFiles *"2018"*.txt
'
' where: * means any set of characters
'
' 5) To compile on Windows' Command Prompt the add-on software with the Apache Commons Text .jar file, i.e. org.apache.commons.text, use the following command:
'   javac -cp .;org.apache.commons.text.jar generateAnnualYearEndSummaryReportOfAllInputFiles.java
'
' 6) To execute on Windows' Command Prompt the add-on software with the Apache Commons Text .jar file, i.e. org.apache.commons.text, use the following command:
'   java -cp .;org.apache.commons.text.jar generateAnnualYearEndSummaryReportOfAllInputFiles *.txt
'
' 7) The Apache Commons Text binaries with the .jar file can be downloaded here:
'   http://commons.apache.org/proper/commons-text/download_text.cgi; last accessed: 20190123
'
' 8) The documentation for the LevenshteinDistance can be viewed here:
'   https://commons.apache.org/proper/commons-text/javadocs/api-release/org/apache/commons/text/similarity/LevenshteinDistance.html; last accessed: 20190123
*/ 

public class generateAnnualYearEndSummaryReportOfAllInputFiles {	
	private static boolean isInDebugMode = false; //true;
	private static String inputFilename = "input201801"; //without extension; default input file
	
	//added by Mike, 20211223
	//note: computer auto-identifies the value
	//note: set this to 1 if input files do not adhere to the Master List format
	private static int INPUT_MASTER_LIST_OFFSET = 0;//1; //added by Mike, 20200101; edited by Mike, 20211223

	private static String startDate = null;
	private static String endDate = null;
	
	//added by Mike, 20190127
	private static final int HMO_CONTAINER_TYPE = 0;
	private static final int NON_HMO_CONTAINER_TYPE = 1;	
	private static final int REFERRING_DOCTOR_CONTAINER_TYPE = 2;	
	private static final int HMO_CLASSIFICATION_CONTAINER_PER_MEDICAL_DOCTOR_CONTAINER_TYPE = 3;	
	private static final int NON_HMO_CLASSIFICATION_CONTAINER_PER_MEDICAL_DOCTOR_CONTAINER_TYPE = 4;	
		
	private static final int INPUT_REFERRING_DOCTOR_COLUMN = 15;
//	private static final int INPUT_NOTES_COLUMN = 0; //removed by Mike, 20201010
	private static final int INPUT_DATE_COLUMN = 1;
	private static final int INPUT_NAME_COLUMN = 3;
	private static final int INPUT_CLASS_COLUMN = 8; //HMO and NON-HMO
//	private static final int INPUT_NET_PF_COLUMN = 10; //removed by Mike, 20200101
	private static final int INPUT_NEW_OLD_COLUMN = 16;
	private static final int INPUT_CONSULTATION_PROCEDURE_COLUMN = 2;
	//added by Mike, 20190107
	private static final int INPUT_CONSULTATION_MEDICAL_CERTIFICATE_COLUMN = 2; //The int value is the same as "INPUT_CONSULTATION_PROCEDURE_COLUMN-INPUT_MASTER_LIST_OFFSET-INPUT_MASTER_LIST_OFFSET".

	//added by Mike, 20181218
	//CONSULTATION
/*	
	private static final int INPUT_CONSULTATION_CLASS_COLUMN = 9;
	private static final int INPUT_CONSULTATION_NET_PF_COLUMN = 11;
	private static final int INPUT_CONSULTATION_NEW_OLD_COLUMN = 17;
*/	
	private static final int INPUT_CONSULTATION_OFFSET = 1;		

/* //removed by Mike, 20211223
	//note: set this to 1 if input files do not adhere to the Master List format
	private static int INPUT_MASTER_LIST_OFFSET = 1; //0;//1; //added by Mike, 20200101; edited by Mike, 20211223
*/
	
/*	private static HashMap<String, double[]> referringDoctorContainer;	
*/

	//added by Mike, 20191230; edited by Mike, 20191231
/*	
	private static final int INPUT_HMO_LIST_CLASSIFICATION_COLUMN = 0;
	private static final int INPUT_HMO_LIST_SUB_CLASSIFICATION_COLUMN = 1;
*/
	private static final int INPUT_LIST_CLASSIFICATION_COLUMN = 0;
	private static final int INPUT_LIST_SUB_CLASSIFICATION_COLUMN = 1;

	private static HashMap<Integer, double[]> dateContainer;	//added by Mike, 201801205
//	private static HashMap<String, double[]> hmoContainer;	//added by Mike, 201801217
	private static HashMap<String, double[]> hmoContainer;	//edited by Mike, 201901230
	private static HashMap<String, double[]> nonHmoContainer;	//added by Mike, 201801217
	private static HashMap<String, double[]> referringDoctorContainer; //added by Mike, 20181218

	private static ArrayList<String[]> hmoContainerArrayList; //edited by Mike, 20191230
	private static HashMap<String, double[]> classifiedHmoContainer; //added by Mike, 20191230

	//added by Mike, 20191231
	private static ArrayList<String[]> medicalDoctorContainerArrayList; 
	private static HashMap<String, double[]> classifiedMedicalDoctorContainer; 

	private static double[] columnValuesArray;
	private static String[] dateValuesArray; //added by Mike, 20180412
	private static int[] dateValuesArrayInt; //added by Mike, 20181206
	//private static ArrayList<int> dateValuesArrayInt; //edited by Mike, 20181221
		
	//the date and the referring doctor are not yet included here
	//this is for both HMO and NON-HMO transactions
	private static final int OUTPUT_TOTAL_COLUMNS = 17; //edited by Mike, 20190107

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
	private static final int OUTPUT_CONSULTATION_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN = 15; //transaction count; added by Mike, 20190107
	private static final int OUTPUT_CONSULTATION_NON_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN = 16; //transaction count; added by Mike, 20190107

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
		PrintWriter writer = new PrintWriter("output/AnnualYearEndSummaryReportOutput.txt", "UTF-8");			
		/*referringDoctorContainer = new HashMap<String, double[]>();
		*/
		
		dateContainer = new HashMap<Integer, double[]>();
		hmoContainer = new HashMap<String, double[]>();
		nonHmoContainer = new HashMap<String, double[]>();
		referringDoctorContainer = new HashMap<String, double[]>();
//		medicalDoctorContainer = new HashMap<String, double[]>();
		classificationContainerPerMedicalDoctor = new HashMap<String, HashMap<String, double[]>>();				
		hmoContainerArrayList = new ArrayList<String[]>(); //edited by Mike, 20191230
		//classifiedHmoContainer = new HashMap<String, Integer>(); //added by Mike, 20191230
		classifiedHmoContainer = new HashMap<String, double[]>(); //added by Mike, 20191230
				
		medicalDoctorContainerArrayList = new ArrayList<String[]>(); //added by Mike, 20191231
		classifiedMedicalDoctorContainer = new HashMap<String, double[]>(); //added by Mike, 20191231
				
		//added by Mike, 20181116; edited by Mike, 20191231
/*		
		startDate = null; //properly set the month and year in the output file of each input file
		dateValuesArray = new String[args.length]; //added by Mike, 20180412
		dateValuesArrayInt = new int[args.length]; //added by Mike, 20180412
		//dateValuesArrayInt = new ArrayList<int>(); //edited by Mike, 20181221
*/
		//System.out.println("args.length: " + args.length);

		//note 24 due to 12 months for Consultation, and another 12 months for PT Treatment
		//TO-DO: -update: instructions to notify Unit member if the total number of input files for each input folder is incorrect
		startDate = null; //properly set the month and year in the output file of each input file
		dateValuesArray = new String[24]; //added by Mike, 20180412
		dateValuesArrayInt = new int[24]; //added by Mike, 20180412
		//dateValuesArrayInt = new ArrayList<int>(); //edited by Mike, 20181221

		myLevenshteinDistance = new LevenshteinDistance(); //added by Mike, 20191231

		//added by Mike, 20191230; edited by Mike, 20191231
		//PART/COMPONENT/MODULE/PHASE 1
//		processHMOInputFile(args);
		processAssetsInputFile(args, "hmo", hmoContainerArrayList);
		processAssetsInputFile(args, "medical", medicalDoctorContainerArrayList);

		//PART/COMPONENT/MODULE/PHASE 2
		processInputFiles(args, true);

		//PART/COMPONENT/MODULE/PHASE 3		
		setClassificationContainerPerMedicalDoctor(classificationContainerPerMedicalDoctor);
		processInputFiles(args, false);

		//PART/COMPONENT/MODULE/PHASE 4
		//added by Mike, 20190125
		processContainers();


//		processMedicalDoctorInputFile(args, ");
//processMedicalDoctorNameWithMedicalDoctorClassification(String inputString, ArrayList<String[]> containerArrayList) {

		//PART/COMPONENT/MODULE/PHASE 5		
		//added by Mike, 20191230
//		processHMOClassification();

		//PART/COMPONENT/MODULE/PHASE 6
		//added by Mike, 20191230
		//We do this after processContainers() and processHMOClassification()
//		consolidateKeysAndTheirValuesInContainerUsingListFromAssetsFolder(classifiedHmoContainer, hmoContainer, HMO_CONTAINER_TYPE);
		
				
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
		//added by Mike, 20181118
		writer.print("Annual Year End Summary Report\n");
		
		//--------------------------------------------------------------------
		//init table header names
		writer.print("\tTREATMENT COUNT:\tCONSULTATION COUNT:\tPROCEDURE COUNT:\tMEDICAL CERTIFICATE COUNT:\n"); 		

		double totalTreatmentCount = 0;
		double totalConsultationCount = 0; //added by Mike, 20181218
		double totalProcedureCount = 0; //added by Mike, 20190105		
		double totalMedicalCertificateCount = 0; //added by Mike, 20190107

		//added by Mike 20200102
		dateValuesArrayInt = autoVerifyDateValuesArrayInt(dateValuesArrayInt);
		
		//Note that there should be an even number of input files and at least two (2) input files, one for PT Treatment and another for Consultation
		for(int i=0; i<dateValuesArrayInt.length; i++) { //divide by 2 because we have the same 
//		for(int i=0; i<dateValuesArrayInt.length/2; i++) { //divide by 2 because we have the same month-year for both PT TREATMENT and CONSULTATION
	
/*	
			//edited by Mike, 20200108
			if (INPUT_MASTER_LIST_OFFSET==0) {
				writer.print(convertDateToMonthYearInWords(dateValuesArrayInt[i])+"\t");
			}
			else {
				writer.print(convertDateToMonthYearInWords(dateValuesArrayInt[i])+"\t");
			}
*/
			writer.print(convertDateToMonthYearInWords(dateValuesArrayInt[i])+"\t");
			
			//added by Mike, 20201228
			if (dateContainer.get(dateValuesArrayInt[i])==null) {
				writer.print(
/*
								treatmentCount+"\t"+						
								consultationCount+"\t"+							
								procedureCount+"\t"+
								medicalCertificateCount+"\n"
*/
								"0"+"\t"+						
								"0"+"\t"+							
								"0"+"\t"+
								"0"+"\n"
							); 				   							

				continue;
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
			
			writer.print(
							treatmentCount+"\t"+						
							consultationCount+"\t"+							
							procedureCount+"\t"+
							medicalCertificateCount+"\n"
						); 				   							
		}
		//TOTAL
		writer.print(
				"TOTAL:\t"+totalTreatmentCount+"\t"+totalConsultationCount+"\t"+totalProcedureCount+"\t"+totalMedicalCertificateCount+"\n"		
				); 				   							

		//--------------------------------------------------------------------
		//init table header names
/*		
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
*/

		writer.print("\n\tTREATMENT COUNT:\tCONSULTATION COUNT:\tPROCEDURE COUNT:\tMEDICAL CERTIFICATE COUNT:\n"); 		

		double totalTreatmentHMOCount = 0;
		double totalConsultationHMOCount = 0; //added by Mike, 20181219		
		double totalProcedureHMOCount = 0; //added by Mike, 20190105		
		double totalMedicalCertificateHMOCount = 0; //added by Mike, 20190107
	
		//added by Mike, 20200124
		double totalSLRTreatmentCount=0;
		double totalSLRConsultationCount=0;
		double totalSLRProcedureCount=0;
		double totalSLRMedicalCertificateCount=0;

		//edited by Mike, 20191230
		SortedSet<String> sortedKeyset = new TreeSet<String>(hmoContainer.keySet());
		//SortedSet<String> sortedKeyset = new TreeSet<String>(classifiedHmoContainer.keySet());

		for (String key : sortedKeyset) {	
			double treatmentCount = hmoContainer.get(key)[OUTPUT_HMO_COUNT_COLUMN];
			double consultationCount = hmoContainer.get(key)[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN];
			double procedureCount = hmoContainer.get(key)[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN]; //added by Mike, 20190105		
			double medicalCertificateCount = hmoContainer.get(key)[OUTPUT_CONSULTATION_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN]; //added by Mike, 20190107

			//added by Mike, 20201231
			key = key.replace(":","");

			//added by Mike, 20200124
			if(key.contains("SLR")) {
				totalSLRTreatmentCount+=treatmentCount;
				totalSLRConsultationCount+=consultationCount;
				totalSLRProcedureCount+=procedureCount;
				totalSLRMedicalCertificateCount+=medicalCertificateCount;
			}
			else{
				totalTreatmentHMOCount += treatmentCount;
				totalConsultationHMOCount += consultationCount;
				totalProcedureHMOCount += procedureCount;
				totalMedicalCertificateHMOCount += medicalCertificateCount;
			}			
			
			writer.print(
							key + "\t" + 
							treatmentCount+"\t"+							
							consultationCount+"\t"+							
							procedureCount+"\t"+							
							medicalCertificateCount+"\n"							
						); 				   							
			
		}
			
		//TOTAL
		writer.print(
				"TOTAL:\t"+totalTreatmentHMOCount+"\t"+totalConsultationHMOCount+"\t"+totalProcedureHMOCount+"\t"+totalMedicalCertificateHMOCount+"\n"
				);					

		//--------------------------------------------------------------------
		//init table header names
/*		
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
*/

		writer.print("\n\tTREATMENT COUNT:\tCONSULTATION COUNT:\tPROCEDURE COUNT:\tMEDICAL CERTIFICATE COUNT:\n"); 		

		double totalTreatmentNONHMOCount = 0;
		double totalConsultationNONHMOCount = 0; //added by Mike, 20181219		
		double totalProcedureNONHMOCount = 0; //added by Mike, 20190105		
		double totalMedicalCertificateNONHMOCount = 0; //added by Mike, 20190107
		
		SortedSet<String> sortedNONHMOKeyset = new TreeSet<String>(nonHmoContainer.keySet());

		for (String key : sortedNONHMOKeyset) {	
			double treatmentNONHMOCount = nonHmoContainer.get(key)[OUTPUT_NON_HMO_COUNT_COLUMN];
			double consultationNONHMOCount = nonHmoContainer.get(key)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN];
			double procedureNONHMOCount = nonHmoContainer.get(key)[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN]; //added by Mike, 20190105		
			double medicalCertificateNONHMOCount = nonHmoContainer.get(key)[OUTPUT_CONSULTATION_NON_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN]; //added by Mike, 20190107

			//added by Mike, 20201231
			key = key.replace(":","");

			//added by Mike, 20200124
			if(key.contains("WI") && key.contains("ROBLES")) {
				totalSLRTreatmentCount+=treatmentNONHMOCount;
				totalSLRConsultationCount+=consultationNONHMOCount;
				totalSLRProcedureCount+=procedureNONHMOCount;
				totalSLRMedicalCertificateCount+=medicalCertificateNONHMOCount;
			}
			else {
				writer.print(
							key + "\t" + 
							treatmentNONHMOCount+"\t"+							
							consultationNONHMOCount+"\t"+							
							procedureNONHMOCount+"\t"+							
							medicalCertificateNONHMOCount+"\n"							
						); 				   							

				totalTreatmentNONHMOCount += treatmentNONHMOCount;
				totalConsultationNONHMOCount += consultationNONHMOCount;
				totalProcedureNONHMOCount += procedureNONHMOCount;
				totalMedicalCertificateNONHMOCount += medicalCertificateNONHMOCount;
			}
			

/*
			writer.print(
							key + "\t" + 
							treatmentNONHMOCount+"\t"+							
							consultationNONHMOCount+"\t"+							
							procedureNONHMOCount+"\t"+							
							medicalCertificateNONHMOCount+"\n"							
						); 				   							
*/						
		}
		
		totalTreatmentNONHMOCount += totalSLRTreatmentCount;
		totalConsultationNONHMOCount += totalSLRConsultationCount;
		totalProcedureNONHMOCount += totalSLRProcedureCount;
		totalMedicalCertificateNONHMOCount += totalSLRMedicalCertificateCount;


		//added by Mike, 20200113; edited by Mike, 20201231
/*		writer.print(
				"HMO:\t"+totalTreatmentHMOCount+"\t"+totalConsultationHMOCount+"\t"+totalProcedureHMOCount+"\t"+totalMedicalCertificateHMOCount+"\n"
				);					
*/				
		writer.print(
				"HMO\t"+totalTreatmentHMOCount+"\t"+totalConsultationHMOCount+"\t"+totalProcedureHMOCount+"\t"+totalMedicalCertificateHMOCount+"\n"
				);					

/*
		//added by Mike, 202000124
		SortedSet<String> sortedReferringMedicalDoctorTransactionCountKeyset = new TreeSet<String>(referringDoctorContainer.keySet());
		double totalSLRTreatmentCount=0;
		double totalSLRConsultationCount=0;
		double totalSLRProcedureCount=0;
		double totalSLRMedicalCertificateCount=0;
		for (String key : sortedReferringMedicalDoctorTransactionCountKeyset) {	
			if (key.contains("SLR")) {			  
				double slrTreatmentCount = referringDoctorContainer.get(key)[OUTPUT_NON_HMO_COUNT_COLUMN]+referringDoctorContainer.get(key)[OUTPUT_HMO_COUNT_COLUMN];
				
				double slrConsultationCount = referringDoctorContainer.get(key)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]+referringDoctorContainer.get(key)[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN];
				
				double slrProcedureCount = referringDoctorContainer.get(key)[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN]+referringDoctorContainer.get(key)[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN];
				
				double slrMedicalCertificateCount = referringDoctorContainer.get(key)[OUTPUT_CONSULTATION_NON_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN]+referringDoctorContainer.get(key)[OUTPUT_CONSULTATION_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN];
				
				totalSLRTreatmentCount+=slrTreatmentCount;
				totalSLRConsultationCount+=slrConsultationCount;
				totalSLRProcedureCount+=slrProcedureCount;
				totalSLRMedicalCertificateCount+=slrMedicalCertificateCount;				
			}
		}
		
		writer.print(
				"SLR:\t"+totalSLRTreatmentCount+"\t"+totalSLRConsultationCount+"\t"+totalSLRProcedureCount+"\t"+totalSLRMedicalCertificateCount+"\n"
				);					
*/

		//SLR
		writer.print(
					"SLR" + "\t" + 
					totalSLRTreatmentCount+"\t"+							
					totalSLRConsultationCount+"\t"+							
					totalSLRProcedureCount+"\t"+							
					totalSLRMedicalCertificateCount+"\n"							
				); 				   							

		double totalTreatmentCountPartTwo = totalTreatmentHMOCount + totalTreatmentNONHMOCount;
		double totalConsultationCountPartTwo = totalConsultationHMOCount + totalConsultationNONHMOCount;
		double totalProcedureCountPartTwo = totalProcedureHMOCount + totalProcedureNONHMOCount;
		double totalMedicalCertificateCountPartTwo = totalMedicalCertificateHMOCount + totalMedicalCertificateNONHMOCount;

		//TOTAL
		writer.print(
				"TOTAL:\t"+totalTreatmentCountPartTwo+"\t"+totalConsultationCountPartTwo+"\t"+totalProcedureCountPartTwo+"\t"+totalMedicalCertificateCountPartTwo+"\n"
				);					
		
		//TOTAL
/*		writer.print(
				"TOTAL:\t"+totalTreatmentNONHMOCount+"\t"+totalConsultationNONHMOCount+"\t"+totalProcedureNONHMOCount+"\t"+totalMedicalCertificateNONHMOCount+"\n"
				);					
*/

		//--------------------------------------------------------------------
		//init table header names
		writer.print("\n\tTREATMENT COUNT:\tCONSULTATION COUNT:\tPROCEDURE COUNT:\tMEDICAL CERTIFICATE COUNT:\tNEW PATIENT REFERRAL COUNT:\n"); 		

		double totalReferringMedicalDoctorTransactionCount = 0;
		double totalNewPatientReferralTransactionCount = 0;
		double totalConsultationPerDoctorCount = 0;
		double totalProcedurePerDoctorCount = 0;
		double totalMedicalCertificatePerDoctorCount = 0;
		
		//removed by Mike, 20200124
		SortedSet<String> sortedReferringMedicalDoctorTransactionCountKeyset = new TreeSet<String>(referringDoctorContainer.keySet());

		for (String key : sortedReferringMedicalDoctorTransactionCountKeyset) {	
			double count = referringDoctorContainer.get(key)[OUTPUT_HMO_COUNT_COLUMN] + referringDoctorContainer.get(key)[OUTPUT_NON_HMO_COUNT_COLUMN];

			double newPatientReferralTransactionCount = referringDoctorContainer.get(key)[OUTPUT_HMO_NEW_OLD_COUNT_COLUMN] + referringDoctorContainer.get(key)[OUTPUT_NON_HMO_NEW_OLD_COUNT_COLUMN];

			//added by Mike, 20181219
			double consultationCount = referringDoctorContainer.get(key)[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN] + referringDoctorContainer.get(key)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN];

			//added by Mike, 20181219
			double procedureCount = referringDoctorContainer.get(key)[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN] + referringDoctorContainer.get(key)[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN];

			//added by Mike, 20190109
			double medicalCertificateCount = referringDoctorContainer.get(key)[OUTPUT_CONSULTATION_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN] + referringDoctorContainer.get(key)[OUTPUT_CONSULTATION_NON_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN];
			
			totalReferringMedicalDoctorTransactionCount += count;
			totalNewPatientReferralTransactionCount += newPatientReferralTransactionCount;
			totalConsultationPerDoctorCount += consultationCount;
			totalProcedurePerDoctorCount += procedureCount;
			totalMedicalCertificatePerDoctorCount += medicalCertificateCount; //procedureCount; //edited by Mike, 20200124
			
			writer.print(
							key + "\t" + 
							count+"\t" +
							consultationCount+"\t"+
							procedureCount+"\t"+		
							medicalCertificateCount+"\t"+		
							newPatientReferralTransactionCount+"\n"						
							); 				   							
		}

		//TOTAL
		writer.print(
				"TOTAL:\t"+totalReferringMedicalDoctorTransactionCount+"\t"+
				totalConsultationPerDoctorCount+"\t"+totalProcedurePerDoctorCount+"\t"+
				totalMedicalCertificatePerDoctorCount+"\t"+totalNewPatientReferralTransactionCount+"\n"							
				); 				   										

/*		
		writer.print("\n\tTREATMENT COUNT:\tNEW PATIENT REFERRAL COUNT:\tCONSULTATION COUNT:\tPROCEDURE COUNT:\n"); 
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
*/
		//--------------------------------------------------------------------
		//edited by Mike, 20200101
		//init table header names
		writer.print("\nCONSULTATION COUNT under each CLASSIFICATION\n");
		writer.print("MEDICAL DOCTOR NAME");

/*		SortedSet<String> sortedReferringMedicalDoctorTransactionCountKeyset = new TreeSet<String>(referringDoctorContainer.keySet());
*/
		SortedSet<String> sortedclassificationContainerPerMedicalDoctorTransactionCountKeyset = new TreeSet<String>(classificationContainerPerMedicalDoctor.keySet());
//		String defaultKey=null;
/*		
		writer.print("\n");
		for (String key : sortedNONHMOKeyset) {	
			writer.print(key+"\t"); 		
		}		
		writer.print("\n");
*/				
//		SortedSet<String> sortedNonHmoContainerTableHeaderKeyset = new TreeSet<String>(classificationContainerPerMedicalDoctor.get(defaultKey).keySet());

/*
		double totalNonHmoCount = 0;
*/
		HashMap<String, Integer> totalCountForEachClassification = new HashMap<String, Integer>(); //added by Mike, 20190102
		boolean hasInitTableHeader=false;		
		SortedSet<String> sortedclassificationKeyset = null;
		
		double hmoClassificationKeyCount;// = 0; //added by Mike, 20200101
		double slrClassificationKeyCount;// = 0; //added by Mike, 20200117				
		double wiClassificationKeyCount;// = 0; //added by Mike, 20200117
		
		for (String key : sortedclassificationContainerPerMedicalDoctorTransactionCountKeyset) {		 
				
			sortedclassificationKeyset = new TreeSet<String>(classificationContainerPerMedicalDoctor.get(key).keySet());

			hmoClassificationKeyCount = 0; //added by Mike, 20200101
			slrClassificationKeyCount = 0; //added by Mike, 20200117
			wiClassificationKeyCount = 0; //added by Mike, 20200117

			if (!hasInitTableHeader) {
				writer.print("\t");
				for (String classificationKey : sortedclassificationKeyset) {	
					//edited by Mike, 20200101
					//writer.print(classificationKey+"\t");			
					if (!classificationKey.contains("HMO")) {
						//added by Mike, 20200117
						if (classificationKey.contains("SLR")) {
							classificationKey = "SLR";
						}
						else if (classificationKey.contains("WI")) {
							classificationKey = "WI";
						}
						//added by Mike, 20200124
						else if (classificationKey.contains("MEDICAL CERTIFICATE")) {
							continue;
						}
						else {
							writer.print(classificationKey+"\t");
						}
						
						//writer.print(classificationKey+"\t");
					}
					else {
						classificationKey = "HMO";
/*						
						if (!totalCountForEachClassification.containsKey(classificationKey)) {
							writer.print("HMO"+"\t");				
						}
*/						
					}						
					
					//added by Mike, 20190102
					totalCountForEachClassification.put(classificationKey, 0);
				}				

				//added by Mike, 20200117
				writer.print("WI"+"\t");				

				//added by Mike, 20200117
				writer.print("SLR"+"\t");				

				//added by Mike, 20200101
				writer.print("HMO"+"\t");				

				writer.print("\n");
				hasInitTableHeader=true;
			}

			writer.print(key+"\t");

			for (String classificationKey : sortedclassificationKeyset) {
				double[] value = classificationContainerPerMedicalDoctor.get(key).get(classificationKey);
				double classificationCount = value[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN] + value[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN];
				
				//added by Mike, 20200101; edited by Mike, 20200117
				if (!classificationKey.contains("HMO")) {
					//added by Mike, 20200117
					if (classificationKey.contains("SLR")) {
						classificationKey = "SLR";
						slrClassificationKeyCount+=classificationCount;
					}
					else if (classificationKey.contains("WI")) {
						classificationKey = "WI";
						wiClassificationKeyCount+=classificationCount;
					}
					//added by Mike, 20200124
					else if (classificationKey.contains("MEDICAL CERTIFICATE")) {
						continue;
					}
					else {
						writer.print(classificationCount+"\t");
					}

					//writer.print(classificationCount+"\t");
				}					
				else {
					classificationKey = "HMO";					
					hmoClassificationKeyCount+=classificationCount;
				}
				
				//added by Mike, 20190102
				totalCountForEachClassification.put(classificationKey, totalCountForEachClassification.get(classificationKey)+(int)classificationCount);
//				System.out.println(">>" +" "+classificationKey+" "+totalCountForEachClassification.get(classificationKey));

				//removed by Mike, 20200101
				//writer.print(classificationCount+"\t");
			}			

			//added by Mike, 20200117
			writer.print(wiClassificationKeyCount+"\t");			

			//added by Mike, 20200117
			writer.print(slrClassificationKeyCount+"\t");			

			//added by Mike, 20200101
			writer.print(hmoClassificationKeyCount+"\t");			
			
			writer.print("\n");
		}
		
		//TOTAL
		writer.print("TOTAL:\t");
				
		//added by Mike, 20190102; edited by Mike, 20200101
		for (String classificationKey : sortedclassificationKeyset) {
			//writer.print(totalCountForEachClassification.get(classificationKey)+"\t");
			
			//added by Mike, 20200101; edited by Mike, 20200117
			//if (!classificationKey.contains("HMO")) {
			if ((!classificationKey.contains("HMO")) && (!classificationKey.contains("SLR")) && (!classificationKey.contains("WI")) && (!classificationKey.contains("MEDICAL CERTIFICATE"))){
				writer.print(totalCountForEachClassification.get(classificationKey)+"\t");
			}					
		}			

		//added by Mike, 20200117
		writer.print(totalCountForEachClassification.get("WI")+"\t");
	
		//added by Mike, 20200117
		writer.print(totalCountForEachClassification.get("SLR")+"\t");
	
		//added by Mike, 20200101
		writer.print(totalCountForEachClassification.get("HMO")+"\t");

		writer.print("\n");		
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

	//added by Mike, 20200102
	private static int[] autoVerifyDateValuesArrayInt(int[] dateValuesArrayInt) {
		int[] output = new int[12]; //12 months
		
		int iYear = dateValuesArrayInt[0]/100;
		int iMonth = 0;
		
		for (int i=0; i<12; i++) {
			//from Month 1 until 12 
			iMonth = i+1;
			output[i] = iYear*100 + iMonth;
		}
		
		boolean hasYearMonth = false;

		for(int k=0; k<dateValuesArrayInt.length/2; k++) { //divide by 2 because we have the same month-year for both PT TREATMENT and CONSULTATION			

			hasYearMonth = false;
			for (int i=0; i<12; i++) {
				if (dateValuesArrayInt[k]==output[i]) {
					hasYearMonth = true;
					break;
				}
			}
			
			if (!hasYearMonth) {
				System.out.println("Wala ang taon at buwan na ito: " + dateValuesArrayInt[k]);
			}
		}	

		return output;
	}	
	
	//added by Mike 20200102
	//1.1) input:
	//Apr-02-19
	//1.2) output:
	//format: yyyymm
	//example: 202001
	//added by Mike 202001228
	//2.1) input:
	//12/25/20
	//2.2) output:
	//format: yyyymm
	//example: 202012
	//TO-DO: -update: instructions for the computer automatically calculate and add "20"
	private static int getYearMonthInInt(String date) {
		//added by Mike, 20201227
		StringBuffer sb = new StringBuffer(date);				
		String output;// = "202012";
		
		System.out.println("date: "+date);

		//added by Mike, 20211228
//		date = date.replace("/","");
			
		//identify if correct input format; based on Month
		//edited by Mike, 20211228
		if (date.contains("/")) {
			output = ("20").concat(sb.substring(sb.length()-2,sb.length()));
			
//			System.out.println("date"+date.split("/")[0]);
			
			if (date.split("/")[0].length()<2) {
				output = output.concat("0" + date.split("/")[0]);
			}
			else {
				output = output.concat(date.split("/")[0]);
			}			
		}
		else if (isNumeric(date.substring(0,3))) {					
//		if (!isNumeric(date.replace("/","").substring(0,3))) {					
			//edited by Mike, 20201227
//			String output = ("20").concat(sb.substring(sb.length()-2,sb.length()));		
			output = ("20").concat(sb.substring(sb.length()-2,sb.length()));		
			output = output.concat(convertMonthToNumericalString(sb.substring(0,3)));			
		}
		else {			
			output = sb.substring(sb.length()-4,sb.length());		
			output = output.concat(sb.substring(0,2));					
		}

		System.out.println("output: "+output);
		
		return Integer.parseInt(output);
	}
	
	//added by Mike, 20200108; edited by Mike, 20211223
	//1) input:
	//format#1: m/d/yyyy
	//example: 8/1/2019
	//format#2: mmm-dd-yy
	//example: Jan-04-21	
	//2) output:
	//format: yyyymm
	//example: 202008
	private static int getYearMonthInIntNotMasterList(String date) {
		StringBuffer sb = new StringBuffer(date);			
		
		System.out.println(">>date: "+date);
		
		//added by Mike, 20211223
		//example input: Jan-04-21
		//example output: 202101
		if (!isNumeric(sb.substring(0,3))) {		
			System.out.println(">>>>>>DITO");
			return getYearMonthInInt(date);
		}	
		
		String[] inputStringArray = date.split("/");			
		
		if (inputStringArray[0].length() == 1) {
			inputStringArray[0] = "0".concat(inputStringArray[0]);
		}		
		String output = inputStringArray[2].concat(inputStringArray[0]);
				
				
		System.out.println(">>>>>>>>>>>output: " + output);
		return Integer.parseInt(output);
	}
	
	//added by Mike, 20200102
	//1) input:
	//Apr
	//format: mmm
	//output: 04	
	private static String convertMonthToNumericalString(String mmm) {
		mmm = mmm.toLowerCase();
		
		switch(mmm) {
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

		return null;//error
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
		//				if (!referringDoctorContainer.containsKey(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN-INPUT_MASTER_LIST_OFFSET])) {
				if (!dateContainer.containsKey(dateValuesArrayInt[i])) {
					columnValuesArray = new double[OUTPUT_TOTAL_COLUMNS];

					//edited by Mike, 20181218
					if (!isConsultation) {											
						//edited by Mike, 20181206
						if ((inputColumns[INPUT_CLASS_COLUMN-INPUT_MASTER_LIST_OFFSET].contains("HMO")) ||
							(inputColumns[INPUT_CLASS_COLUMN-INPUT_MASTER_LIST_OFFSET].contains("SLR"))) {

							columnValuesArray[OUTPUT_HMO_COUNT_COLUMN] = 1;
	
							//removed by Mike, ,20200101
/*							
							columnValuesArray[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
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
							
							//removed by Mike, ,20200101
/*							
							columnValuesArray[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
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
						if ((inputColumns[INPUT_CLASS_COLUMN-INPUT_MASTER_LIST_OFFSET+INPUT_CONSULTATION_OFFSET].contains("HMO")) ||
							(inputColumns[INPUT_CLASS_COLUMN-INPUT_MASTER_LIST_OFFSET+INPUT_CONSULTATION_OFFSET].contains("SLR"))) {

							//edited by Mike, 20190107
							if (inputColumns[INPUT_CONSULTATION_MEDICAL_CERTIFICATE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("mc")) {
								columnValuesArray[OUTPUT_CONSULTATION_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN] = 1;
							}
							else if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("p")) {
								//edited by Mike, 20190108
								if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("/")) {
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
							if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN-INPUT_MASTER_LIST_OFFSET-INPUT_MASTER_LIST_OFFSET].toLowerCase().contains("p")) {
								columnValuesArray[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN] = 1;
							}
*/							
						}
						else {
							//edited by Mike, 20190107
							if (inputColumns[INPUT_CONSULTATION_MEDICAL_CERTIFICATE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("mc")) {
								columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN] = 1;
							}
							else if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("p")) {								
								//edited by Mike, 20190108
								if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("/")) {
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
							if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN-INPUT_MASTER_LIST_OFFSET-INPUT_MASTER_LIST_OFFSET].toLowerCase().contains("p")) {
								columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN] = 1;
							}
*/							
						}						
					}
					
//					referringDoctorContainer.put(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN-INPUT_MASTER_LIST_OFFSET], columnValuesArray);
					dateContainer.put(dateValuesArrayInt[i], columnValuesArray);
				}
				else {
					//edited by Mike, 20181218
					if (!isConsultation) {											
						//edited by Mike, 20181206
						if ((inputColumns[INPUT_CLASS_COLUMN-INPUT_MASTER_LIST_OFFSET].contains("HMO")) ||
							(inputColumns[INPUT_CLASS_COLUMN-INPUT_MASTER_LIST_OFFSET].contains("SLR"))) {								
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
						if ((inputColumns[INPUT_CLASS_COLUMN-INPUT_MASTER_LIST_OFFSET+INPUT_CONSULTATION_OFFSET].contains("HMO")) ||
							(inputColumns[INPUT_CLASS_COLUMN-INPUT_MASTER_LIST_OFFSET+INPUT_CONSULTATION_OFFSET].contains("SLR"))) {
/*							dateContainer.get(dateValuesArrayInt[i])[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN]++;					
*/							
							//edited by Mike, 20190107
							if (inputColumns[INPUT_CONSULTATION_MEDICAL_CERTIFICATE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("mc")) {
								dateContainer.get(dateValuesArrayInt[i])[OUTPUT_CONSULTATION_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN]++;
							}
							else if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("p")) {
								//edited by Mike, 20190108
								if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("/")) {
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
							if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN-INPUT_MASTER_LIST_OFFSET-INPUT_MASTER_LIST_OFFSET].toLowerCase().contains("p")) {
								dateContainer.get(dateValuesArrayInt[i])[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN]++;
							}
*/							
						}
						else {							
/*							dateContainer.get(dateValuesArrayInt[i])[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]++;					
*/
							//edited by Mike, 20190107
							if (inputColumns[INPUT_CONSULTATION_MEDICAL_CERTIFICATE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("mc")) {
								dateContainer.get(dateValuesArrayInt[i])[OUTPUT_CONSULTATION_NON_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN]++;
							}
							else if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("p")) {
								//edited by Mike, 20190108
								if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("/")) {
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
							if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN-INPUT_MASTER_LIST_OFFSET-INPUT_MASTER_LIST_OFFSET].toLowerCase().contains("p")) {
								dateContainer.get(dateValuesArrayInt[i])[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN]++;
							}
*/							
						}
					}
				}					
	}

	//added by Mike, 20181217; edited by Mike,, 20191230
	private static void processHMOCount(HashMap<String, double[]> hmoContainer, String[] inputColumns, boolean isConsultation) {
			//edited by Mike, 20181219
			if (!isConsultation) {											
				//edited by Mike, 20181206
				if ((inputColumns[INPUT_CLASS_COLUMN-INPUT_MASTER_LIST_OFFSET].contains("HMO")) ||
					(inputColumns[INPUT_CLASS_COLUMN-INPUT_MASTER_LIST_OFFSET].contains("SLR"))) {

					String hmoName = inputColumns[INPUT_CLASS_COLUMN-INPUT_MASTER_LIST_OFFSET].trim().toUpperCase();		
					
					//added by Mike, 20200115
					hmoName = hmoName.replace("\"","");
					
					hmoName = processHmoNameWithHmoClassification(hmoName); //added by Mike, 20191230

					//System.out.println(">>>PT Treatment hmoName: " + hmoName);
					
					if (!hmoContainer.containsKey(hmoName)) {
						columnValuesArray = new double[OUTPUT_TOTAL_COLUMNS];
					
						columnValuesArray[OUTPUT_HMO_COUNT_COLUMN] = 1;

						//removed by Mike, 20200101
/*
						columnValuesArray[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
							columnValuesArray[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						}
						else {
							columnValuesArray[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						}
*/						
						hmoContainer.put(hmoName, columnValuesArray);
					}
					else {
						hmoContainer.get(hmoName)[OUTPUT_HMO_COUNT_COLUMN]++;	

						//removed by Mike, 20200101
/*					
						hmoContainer.get(hmoName)[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] 
							+= Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
							
						if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
							hmoContainer.get(hmoName)[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						}
						else {
							hmoContainer.get(hmoName)[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);							
						}		
*/						
					}
				}				
			}
			else {																	
				if ((inputColumns[INPUT_CLASS_COLUMN-INPUT_MASTER_LIST_OFFSET+INPUT_CONSULTATION_OFFSET].contains("HMO")) ||
					(inputColumns[INPUT_CLASS_COLUMN-INPUT_MASTER_LIST_OFFSET+INPUT_CONSULTATION_OFFSET].contains("SLR"))) {

					String hmoName = inputColumns[INPUT_CLASS_COLUMN-INPUT_MASTER_LIST_OFFSET+INPUT_CONSULTATION_OFFSET].trim().toUpperCase();
					
					//added by Mike, 20200115
					hmoName = hmoName.replace("\"","");

					hmoName = processHmoNameWithHmoClassification(hmoName); //added by Mike, 20191230
					
//					System.out.println(">>>Consultation hmoName: " + hmoName);

					if (!hmoContainer.containsKey(hmoName)) {
						columnValuesArray = new double[OUTPUT_TOTAL_COLUMNS];					
						
						//edited by Mike, 20190109
						if (inputColumns[INPUT_CONSULTATION_MEDICAL_CERTIFICATE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("mc")) {
							columnValuesArray[OUTPUT_CONSULTATION_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN] = 1;						
						}
						else if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("p")) {
							//edited by Mike, 20190108
							if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("/")) {
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
						//edited by Mike, 20190109
						if (inputColumns[INPUT_CONSULTATION_MEDICAL_CERTIFICATE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("mc")) {
							hmoContainer.get(hmoName)[OUTPUT_CONSULTATION_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN]++;						
						}
						else if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("p")) {
							//edited by Mike, 20190108
							if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("/")) {
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
	
	//added by Mike, 20181217; edited by Mike, 20200101
	private static void processNONHMOCount(HashMap<String, double[]> nonHmoContainer, String[] inputColumns, boolean isConsultation) {
		//edited by Mike, 20181219
		if (!isConsultation) {											
			//edited by Mike, 20181206
			if ((!inputColumns[INPUT_CLASS_COLUMN-INPUT_MASTER_LIST_OFFSET].contains("HMO")) &&
				(!inputColumns[INPUT_CLASS_COLUMN-INPUT_MASTER_LIST_OFFSET].contains("SLR"))) {

				String classificationName = inputColumns[INPUT_CLASS_COLUMN-INPUT_MASTER_LIST_OFFSET].trim().toUpperCase();
				
				//added by Mike, 20200101
				classificationName = autoCorrectClassification(classificationName);

				if (!nonHmoContainer.containsKey(classificationName)) {
					columnValuesArray = new double[OUTPUT_TOTAL_COLUMNS];
				
					columnValuesArray[OUTPUT_NON_HMO_COUNT_COLUMN] = 1;

					//removed by Mike, 20200101
/*
					columnValuesArray[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
					if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
						columnValuesArray[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
					}
					else {
						columnValuesArray[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
					}
*/					
					nonHmoContainer.put(classificationName, columnValuesArray);
				}
				else {
					nonHmoContainer.get(classificationName)[OUTPUT_NON_HMO_COUNT_COLUMN]++;

					//removed by Mike, 20200101
/*					
				nonHmoContainer.get(classificationName)[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] 
						+= Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						
					if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
						nonHmoContainer.get(classificationName)[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
					}
					else {
						nonHmoContainer.get(classificationName)[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);							
					}
*/					
				}
			}			
		}
		else {			
			if ((!inputColumns[INPUT_CLASS_COLUMN-INPUT_MASTER_LIST_OFFSET+INPUT_CONSULTATION_OFFSET].contains("HMO")) &&
				(!inputColumns[INPUT_CLASS_COLUMN-INPUT_MASTER_LIST_OFFSET+INPUT_CONSULTATION_OFFSET].contains("SLR"))) {

				String classificationName = inputColumns[INPUT_CLASS_COLUMN-INPUT_MASTER_LIST_OFFSET+INPUT_CONSULTATION_OFFSET].trim().toUpperCase();
//				System.out.println("classificationName: "+classificationName); 

				//added by Mike, 20200101
				classificationName = autoCorrectClassification(classificationName);

				
				if (isInDebugMode) {
					if (classificationName.trim().equals("")) {
//						System.out.println(">>> "+inputColumns[INPUT_DATE_COLUMN-INPUT_MASTER_LIST_OFFSET]+"; Name: "+inputColumns[INPUT_NAME_COLUMN-INPUT_MASTER_LIST_OFFSET]);
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
						if (inputColumns[INPUT_CONSULTATION_MEDICAL_CERTIFICATE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("mc")) {
							columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN] = 1;						
						}
						else if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("p")) {
							//edited by Mike, 20190108
							if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("/")) {
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
						if (inputColumns[INPUT_CONSULTATION_MEDICAL_CERTIFICATE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("mc")) {
/*							hmoContainer.get(hmoName)[OUTPUT_CONSULTATION_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN]++;			
*/
							nonHmoContainer.get(classificationName)[OUTPUT_CONSULTATION_NON_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN]++;							
						}
						else if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("p")) {
							//edited by Mike, 20190108
							if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("/")) {
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
	
	//added by Mike, 20181218; edited by Mike, 20191231
	private static void processReferringDoctorTransactionCount(HashMap<String, double[]> referringDoctorContainer, String[] inputColumns, Boolean isConsultation) {		
		//added by Mike, 20190125
		String inputReferringMedicalDoctor = inputColumns[INPUT_REFERRING_DOCTOR_COLUMN-INPUT_MASTER_LIST_OFFSET].trim().toUpperCase();
		
		//added by Mike, 20191231
		inputReferringMedicalDoctor = processMedicalDoctorNameWithMedicalDoctorClassification(inputReferringMedicalDoctor, medicalDoctorContainerArrayList);
	
		//edited by Mike, 20181219
		if (!isConsultation) {	
			if (!referringDoctorContainer.containsKey(inputReferringMedicalDoctor)) {
				columnValuesArray = new double[OUTPUT_TOTAL_COLUMNS];
				
				if (inputColumns[INPUT_CLASS_COLUMN-INPUT_MASTER_LIST_OFFSET].contains("HMO")) {
					columnValuesArray[OUTPUT_HMO_COUNT_COLUMN] = 1;

					//removed by Mike, 20200101
/*
					columnValuesArray[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
					if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
						columnValuesArray[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
					}
					else {
						columnValuesArray[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
					}
*/
					if (inputColumns[INPUT_NEW_OLD_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().contains("new")) {
						//added by Mike, 20181218
						columnValuesArray[OUTPUT_HMO_NEW_OLD_COUNT_COLUMN] = 1;
					}							
				}
				else {
					columnValuesArray[OUTPUT_NON_HMO_COUNT_COLUMN] = 1;

					//removed by Mike, 20200101
/*
					columnValuesArray[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
					
					if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
						columnValuesArray[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
					}
					else {
						columnValuesArray[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] = Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
					}
*/					
					if (inputColumns[INPUT_NEW_OLD_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().contains("new")) {
						//added by Mike, 20181218
						columnValuesArray[OUTPUT_NON_HMO_NEW_OLD_COUNT_COLUMN] = 1;
					}			
				}
				
				referringDoctorContainer.put(inputReferringMedicalDoctor, columnValuesArray);
			}
			else {
				if (inputColumns[INPUT_CLASS_COLUMN-INPUT_MASTER_LIST_OFFSET].contains("HMO")) {
					referringDoctorContainer.get(inputReferringMedicalDoctor)[OUTPUT_HMO_COUNT_COLUMN]++;					
					
					//removed by Mike, 20200101
/*
					referringDoctorContainer.get(inputReferringMedicalDoctor)[OUTPUT_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] 
						+= Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						
					if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
						referringDoctorContainer.get(inputReferringMedicalDoctor)[OUTPUT_HMO_PAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
					}
					else {
						referringDoctorContainer.get(inputReferringMedicalDoctor)[OUTPUT_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
					}
*/					
					if (inputColumns[INPUT_NEW_OLD_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().contains("new")) {
						//added by Mike, 20181218
						referringDoctorContainer.get(inputReferringMedicalDoctor)[OUTPUT_HMO_NEW_OLD_COUNT_COLUMN]++;					
					}							
				}
				else {
					referringDoctorContainer.get(inputReferringMedicalDoctor)[OUTPUT_NON_HMO_COUNT_COLUMN]++;					
					
					//removed by Mike, 20200101
/*					
					referringDoctorContainer.get(inputReferringMedicalDoctor)[OUTPUT_NON_HMO_TOTAL_NET_TREATMENT_FEE_COLUMN] 
						+= Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
						
					if (inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
						referringDoctorContainer.get(inputReferringMedicalDoctor)[OUTPUT_NON_HMO_PAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
					}
					else {
						referringDoctorContainer.get(inputReferringMedicalDoctor)[OUTPUT_NON_HMO_UNPAID_NET_TREATMENT_FEE_COLUMN] += Double.parseDouble(inputColumns[INPUT_NET_PF_COLUMN]);
					}
*/
					
					if (inputColumns[INPUT_NEW_OLD_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().contains("new")) {
						//added by Mike, 20181218
						referringDoctorContainer.get(inputReferringMedicalDoctor)[OUTPUT_NON_HMO_NEW_OLD_COUNT_COLUMN]++;					
					}
				}
			}
		}
		else {
			//added by Mike, 20190125
			inputReferringMedicalDoctor = inputColumns[INPUT_REFERRING_DOCTOR_COLUMN-INPUT_MASTER_LIST_OFFSET+INPUT_CONSULTATION_OFFSET].trim().toUpperCase();
						
			//added by Mike, 20191231
			inputReferringMedicalDoctor = processMedicalDoctorNameWithMedicalDoctorClassification(inputReferringMedicalDoctor, medicalDoctorContainerArrayList);
			
			if (!referringDoctorContainer.containsKey(inputReferringMedicalDoctor)) {
				columnValuesArray = new double[OUTPUT_TOTAL_COLUMNS];
				
				if (inputColumns[INPUT_CLASS_COLUMN-INPUT_MASTER_LIST_OFFSET+INPUT_CONSULTATION_OFFSET].contains("HMO")) {						
					//edited by Mike, 20190109
					if (inputColumns[INPUT_CONSULTATION_MEDICAL_CERTIFICATE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("mc")) {
						columnValuesArray[OUTPUT_CONSULTATION_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN] = 1;						
					}
					else if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN-INPUT_MASTER_LIST_OFFSET-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("p")) {
						//edited by Mike, 20190108
						if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN-INPUT_MASTER_LIST_OFFSET-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("/")) {
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
					if (inputColumns[INPUT_CONSULTATION_MEDICAL_CERTIFICATE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("mc")) {
						columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN] = 1;							
					}
					else if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN-INPUT_MASTER_LIST_OFFSET-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("p")) {
						//edited by Mike, 20190108
						if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN-INPUT_MASTER_LIST_OFFSET-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("/")) {
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
				if (inputColumns[INPUT_CLASS_COLUMN-INPUT_MASTER_LIST_OFFSET+INPUT_CONSULTATION_OFFSET].contains("HMO")) {
					columnValuesArray[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN] = 1;
				}
				else {
					columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN] = 1;
				}
				
				//added by Mike, 20181219
				if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN-INPUT_MASTER_LIST_OFFSET-INPUT_MASTER_LIST_OFFSET].toLowerCase().contains("p")) {
					columnValuesArray[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN] = 1;
				}
				else {
					columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN] = 1;
				}
*/				
				referringDoctorContainer.put(inputReferringMedicalDoctor, columnValuesArray);
			}
			else {													
				if (inputColumns[INPUT_CLASS_COLUMN-INPUT_MASTER_LIST_OFFSET+INPUT_CONSULTATION_OFFSET].contains("HMO")) {
					//edited by Mike, 20190109
					if (inputColumns[INPUT_CONSULTATION_MEDICAL_CERTIFICATE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("mc")) {
/*						columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN]++;							
*/
						referringDoctorContainer.get(inputReferringMedicalDoctor)[OUTPUT_CONSULTATION_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN]++;				
					}
//					else if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN-INPUT_MASTER_LIST_OFFSET-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("p")) {
					else if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("p")) {
						//edited by Mike, 20190108
						if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("/")) {
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

/*												`		referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN-INPUT_MASTER_LIST_OFFSET+INPUT_CONSULTATION_OFFSET])[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN]++;				
					//added by Mike, 20181219
					if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN-INPUT_MASTER_LIST_OFFSET-INPUT_MASTER_LIST_OFFSET].toLowerCase().contains("p")) {
						//edited by Mike, 20181221
						//columnValuesArray[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN]++;
						referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN-INPUT_MASTER_LIST_OFFSET+INPUT_CONSULTATION_OFFSET])[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN]++;				
					}
*/					
				}
				else {
					//edited by Mike, 20190109
					if (inputColumns[INPUT_CONSULTATION_MEDICAL_CERTIFICATE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("mc")) {
/*						columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN]++;							
*/
						referringDoctorContainer.get(inputReferringMedicalDoctor)[OUTPUT_CONSULTATION_NON_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN]++;				
					}
					else if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("p")) {
						//edited by Mike, 20190108
						if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("/")) {
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
					referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN-INPUT_MASTER_LIST_OFFSET+INPUT_CONSULTATION_OFFSET])[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]++;					
					//added by Mike, 20181219
					if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN-INPUT_MASTER_LIST_OFFSET-INPUT_MASTER_LIST_OFFSET].toLowerCase().contains("p")) {
						//edited by Mike, 20181221
						//columnValuesArray[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN]++;						
						referringDoctorContainer.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN-INPUT_MASTER_LIST_OFFSET+INPUT_CONSULTATION_OFFSET])[OUTPUT_CONSULTATION_NON_HMO_PROCEDURE_COUNT_COLUMN]++;				
					}
*/					
				}
			}
		}
	}

	//added by Mike, 20181220; edited by Mike, 20191231
	private static void processMedicalDoctorTransactionPerClassificationCount(HashMap<String, HashMap<String, double[]>> classificationContainerPerMedicalDoctor, String[] inputColumns, Boolean isConsultation) {				

		String medicalDoctorKey = inputColumns[INPUT_REFERRING_DOCTOR_COLUMN-INPUT_MASTER_LIST_OFFSET+INPUT_CONSULTATION_OFFSET].trim().toUpperCase();

		//added by Mike, 20191231
		medicalDoctorKey = processMedicalDoctorNameWithMedicalDoctorClassification(medicalDoctorKey, medicalDoctorContainerArrayList);
	
		if (isConsultation) {			
			String classificationName = inputColumns[INPUT_CLASS_COLUMN-INPUT_MASTER_LIST_OFFSET+INPUT_CONSULTATION_OFFSET].trim().toUpperCase(); //added by Mike, 20181220

			//added by Mike, 20200101
			classificationName = autoCorrectClassification(classificationName);


				if (!classificationName.contains("HMO")) {					
//			System.out.println(">>>"+inputColumns[INPUT_REFERRING_DOCTOR_COLUMN-INPUT_MASTER_LIST_OFFSET+INPUT_CONSULTATION_OFFSET]+" "+classificationName);

/*					classificationContainerPerMedicalDoctor.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN-INPUT_MASTER_LIST_OFFSET+INPUT_CONSULTATION_OFFSET]).get(classificationName)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]++;
*/					
					//edited by Mike, 20190107
					if (inputColumns[INPUT_CONSULTATION_MEDICAL_CERTIFICATE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("mc")) {
						classificationContainerPerMedicalDoctor.get(medicalDoctorKey).get(classificationName)[OUTPUT_CONSULTATION_NON_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN]++;
					}
					else if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("p")) {
						//edited by Mike, 20190108
						if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("/")) {
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

					

//					System.out.println(">>> NON-HMO count: "+classificationContainerPerMedicalDoctor.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN-INPUT_MASTER_LIST_OFFSET+INPUT_CONSULTATION_OFFSET]).get(classificationName)[OUTPUT_CONSULTATION_NON_HMO_COUNT_COLUMN]);
				}
				else {
					
					//added by Mike, 20191231
					classificationName = processHmoNameWithHmoClassification(classificationName); 
				
//				System.out.println(">>>>>"+inputColumns[INPUT_REFERRING_DOCTOR_COLUMN-INPUT_MASTER_LIST_OFFSET+INPUT_CONSULTATION_OFFSET]+" "+classificationName);
/*
					classificationContainerPerMedicalDoctor.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN-INPUT_MASTER_LIST_OFFSET+INPUT_CONSULTATION_OFFSET]).get(classificationName)[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN]++;					
*/

					//edited by Mike, 20190107
					if (inputColumns[INPUT_CONSULTATION_MEDICAL_CERTIFICATE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("mc")) {
						classificationContainerPerMedicalDoctor.get(medicalDoctorKey).get(classificationName)[OUTPUT_CONSULTATION_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN]++;
					}
					else if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("p")) {
						//edited by Mike, 20190108
						if (inputColumns[INPUT_CONSULTATION_PROCEDURE_COLUMN-INPUT_MASTER_LIST_OFFSET].toLowerCase().trim().contains("/")) {
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

//					System.out.println(">>>>> HMO count: "+classificationContainerPerMedicalDoctor.get(inputColumns[INPUT_REFERRING_DOCTOR_COLUMN-INPUT_MASTER_LIST_OFFSET+INPUT_CONSULTATION_OFFSET]).get(classificationName)[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN]);

				}
		}		
	}

	private static void setClassificationContainerPerMedicalDoctor(HashMap<String, HashMap<String, double[]>> classificationContainerPerMedicalDoctor) {
		//edited by Mike, 20191231
//		SortedSet<String> sortedHmoContainerKeyset = new TreeSet<String>(hmoContainer.keySet());
		SortedSet<String> sortedHmoContainerKeyset = new TreeSet<String>(hmoContainer.keySet());

		SortedSet<String> sortedNonHmoContainerKeyset = new TreeSet<String>(nonHmoContainer.keySet());
		SortedSet<String> sortedMedicalDoctorKeyset = new TreeSet<String>(referringDoctorContainer.keySet());
				
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
			//added by Mike, 20181030; edited by Mike, 20201228
/*			inputFilename = args[i].replaceAll(".txt","");			
			File f = new File(inputFilename+".txt");
*/
			File f;
			//identify if file extension uses .txt
			if (args[i].contains(".txt")) {
				inputFilename = args[i].replaceAll(".txt","");			
				f = new File(inputFilename+".txt");
			}
			//.csv
			else {
				inputFilename = args[i].replaceAll(".csv","");			
				f = new File(inputFilename+".csv");				
			}			

			System.out.println("inputFilename:"+inputFilename);

			
			if (inputFilename.toLowerCase().contains("consultation")) {
				isConsultation=true;
			}
			else {
				isConsultation=false;
			}

			if ((inputFilename.toLowerCase().contains("master")) && (inputFilename.toLowerCase().contains("list"))){			
				INPUT_MASTER_LIST_OFFSET = 0;
			}
			else {
				INPUT_MASTER_LIST_OFFSET = 1;
			}
			
			//added by Mike, 20191230
			if (inputFilename.toLowerCase().contains("assets")) {
				continue;
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

/*
				//TO-DO: -update: to auto-identify
				//added by Mike, 20211223
				//auto-verify if tab-delimited, OR comma-delimited
				//NOT tab-delimited
				if (inputColumns.length==1) {
					inputColumns = s.split(",");		
				}
*/
				
				//added by Mike, 20180412
				if (dateValuesArray[i]==null) {
					dateValuesArray[i] = getMonthYear(inputColumns[INPUT_DATE_COLUMN-INPUT_MASTER_LIST_OFFSET]);
				}

				if (dateValuesArrayInt[i]==0) {
					//edited by Mike, 20200102
/*					
					dateValuesArrayInt[i] = Integer.parseInt(args[i].substring(args[i].indexOf("_")+1,args[i].indexOf(".txt")));
*/
					//edited by Mike, 20200108					
					//dateValuesArrayInt[i] = getYearMonthInInt(inputColumns[INPUT_DATE_COLUMN-INPUT_MASTER_LIST_OFFSET]);
					if (INPUT_MASTER_LIST_OFFSET==0) {
						dateValuesArrayInt[i] = getYearMonthInInt(inputColumns[INPUT_DATE_COLUMN-INPUT_MASTER_LIST_OFFSET]);
					}
					else {
						dateValuesArrayInt[i] = getYearMonthInIntNotMasterList(inputColumns[INPUT_DATE_COLUMN-INPUT_MASTER_LIST_OFFSET]);
					}
					
				}
/*
				int dateValueInt = Integer.parseInt(args[i].substring(args[i].indexOf("_")+1,args[i].indexOf(".txt")));
				if (!dateValuesArrayInt.contains(dateValueInt)){
					dateValuesArrayInt.add(dateValueInt);
				}				
*/				
				//edited by Mike, 20181121
				if (startDate==null) {
					startDate = getMonthYear(inputColumns[INPUT_DATE_COLUMN-INPUT_MASTER_LIST_OFFSET]);
					endDate = startDate;
				}
				else {
					//edited by Mike, 20181121
					//add this condition in case the input file does not have a date for each transaction; however, ideally, for input files 2018 onwards, each transaction should have a date
					if (!inputColumns[INPUT_DATE_COLUMN-INPUT_MASTER_LIST_OFFSET].trim().equals("")) {
						endDate = getMonthYear(inputColumns[INPUT_DATE_COLUMN-INPUT_MASTER_LIST_OFFSET]);
					}
				}

				if (isInDebugMode) {
					rowCount++;
					System.out.println("rowCount: "+rowCount);
				}
				
				//added by Mike, 20181121
				//skip transactions that have "RehabSupplies" as its "CLASS" value
				//In Excel logbook/workbook 2018 onwards, such transactions are not included in the Consultation and PT Treatment Excel logbooks/workbooks.
				if (inputColumns[INPUT_CLASS_COLUMN-INPUT_MASTER_LIST_OFFSET].contains("RehabSupplies")) {
					continue;
				}

				if (isPhaseOne) {
					//added by Mike, 20181216
	//				processMonthlyCount(dateContainer, inputColumns, i, false);
					processMonthlyCount(dateContainer, inputColumns, i, isConsultation); //isConsultation = false
					
					//added by Mike, 20181217
					processHMOCount(hmoContainer, inputColumns, isConsultation); //edited by Mike, 20181219
					
					//added by Mike, 20181217
					processNONHMOCount(nonHmoContainer, inputColumns, isConsultation); //edited by Mike, 20181219
					
					//added by Mike, 20181218
					processReferringDoctorTransactionCount(referringDoctorContainer, inputColumns, isConsultation); //edited by Mike, 20181219
			
					//added by Mike, 20181220
	//				processMedicalDoctorTransactionPerClassificationCount(classificationContainerPerMedicalDoctor, inputColumns, isConsultation);
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

				threshold = 3; //Similar with for Referring Medical Doctors, the numerical value should be less than 3.
								
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
						container.get(key).get(classificationKey)[OUTPUT_HMO_NEW_OLD_COUNT_COLUMN] += container.get(keyTwo).get(classificationKey)[OUTPUT_HMO_NEW_OLD_COUNT_COLUMN]; 	

						container.get(key).get(classificationKey)[OUTPUT_NON_HMO_NEW_OLD_COUNT_COLUMN] += container.get(keyTwo).get(classificationKey)[OUTPUT_NON_HMO_NEW_OLD_COUNT_COLUMN]; 	
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
							container.get(key)[OUTPUT_HMO_NEW_OLD_COUNT_COLUMN] += container.get(keyTwo)[OUTPUT_HMO_NEW_OLD_COUNT_COLUMN]; 	

							container.get(key)[OUTPUT_NON_HMO_NEW_OLD_COUNT_COLUMN] += container.get(keyTwo)[OUTPUT_NON_HMO_NEW_OLD_COUNT_COLUMN]; 	
							
							container.remove(keyTwo);
							consolidateKeysAndTheirValuesInContainer(container, containerType);
							return;
					}
				}
			}
		}			
//		return container;
	}

	//added by Mike, 20191230
	//TO-DO: -update: instructons to include Medical Doctor
	private static void consolidateKeysAndTheirValuesInContainerUsingListFromAssetsFolder(HashMap<String, double[]> classifiedContainer, HashMap<String, double[]> container, int containerType) {
		//SortedSet<String> sortedKeyset = new TreeSet<String>(container.keySet());
		//SortedSet<String> sortedKeysetTwo = new TreeSet<String>(container.keySet());
		//SortedSet<String> sortedKeysetTwo = new TreeSet<String>(classifiedHmoContainer.keySet());
		SortedSet<String> sortedKeyset = new TreeSet<String>(classifiedContainer.keySet());
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
								
				System.out.println("key: "+key);
				System.out.println("keyTwo: "+keyTwo);
								
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
							classifiedContainer.get(key)[OUTPUT_HMO_COUNT_COLUMN] += container.get(keyTwo)[OUTPUT_HMO_COUNT_COLUMN];
		/*
							System.out.println("container.get(key)[OUTPUT_HMO_COUNT_COLUMN]: "+container.get(key)[OUTPUT_HMO_COUNT_COLUMN]);
		*/					
							//consultationCount
							classifiedContainer.get(key)[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN] += container.get(keyTwo)[OUTPUT_CONSULTATION_HMO_COUNT_COLUMN];

							//procedureCount
							classifiedContainer.get(key)[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN] += container.get(keyTwo)[OUTPUT_CONSULTATION_HMO_PROCEDURE_COUNT_COLUMN]; 		

							//medicalCertificateCount
							classifiedContainer.get(key)[OUTPUT_CONSULTATION_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN] += container.get(keyTwo)[OUTPUT_CONSULTATION_HMO_MEDICAL_CERTIFICATE_COUNT_COLUMN]; 	

							/*container.remove(keyTwo);
							*/
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
							container.get(key)[OUTPUT_HMO_NEW_OLD_COUNT_COLUMN] += container.get(keyTwo)[OUTPUT_HMO_NEW_OLD_COUNT_COLUMN]; 	

							container.get(key)[OUTPUT_NON_HMO_NEW_OLD_COUNT_COLUMN] += container.get(keyTwo)[OUTPUT_NON_HMO_NEW_OLD_COUNT_COLUMN]; 	
							
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
		//removed by Mike, 20191231
		//myLevenshteinDistance = new LevenshteinDistance();

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
	
	//added by Mike, 20191230; edited by Mike, 20191231
	private static void processAssetsInputFile(String[] args, String fileKeyword, ArrayList<String[]> containerArrayList) throws Exception {
		for (int i=0; i<args.length; i++) {						
			//added by Mike, 20181030; edited by Mike, 20201228
/*			inputFilename = args[i].replaceAll(".txt","");			
			File f = new File(inputFilename+".txt");
*/
			File f;
			//identify if file extension uses .txt
			if (args[i].contains(".txt")) {
				inputFilename = args[i].replaceAll(".txt","");			
				f = new File(inputFilename+".txt");
			}
			//.csv
			else {
				inputFilename = args[i].replaceAll(".csv","");			
				f = new File(inputFilename+".csv");				
			}			

			System.out.println("inputFilename:"+inputFilename);
			
			//added by Mike, 20190207
			if (inputFilename.contains("*")) {
				continue;
			}
			
			if (!inputFilename.toLowerCase().contains("assets")) {
				continue;
			}					

			if (!inputFilename.toLowerCase().contains(fileKeyword.toLowerCase())) {
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

				String[] containerArrayListValue = {
					inputColumns[INPUT_LIST_SUB_CLASSIFICATION_COLUMN].toUpperCase(),
					inputColumns[INPUT_LIST_CLASSIFICATION_COLUMN].toUpperCase()};
				
				containerArrayList.add(containerArrayListValue);

//				if (isInDebugMode) {
					rowCount++;
					System.out.println("rowCount: "+rowCount);
//				}
			}		
		}		
	}

/*
	private static void processHMOInputFile(String[] args) throws Exception {
		for (int i=0; i<args.length; i++) {						
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
				//edited by Mike, 20190430
				String[] hmoContainerArrayListValue = {inputColumns[INPUT_HMO_LIST_SUB_CLASSIFICATION_COLUMN].toUpperCase(),
				inputColumns[INPUT_HMO_LIST_CLASSIFICATION_COLUMN].toUpperCase()};
				hmoContainerArrayList.add(hmoContainerArrayListValue);
//				if (isInDebugMode) {
					rowCount++;
					System.out.println("rowCount: "+rowCount);
//				}
			}		
		}		
	}
*/
	
	//added by Mike, 20191230
	//TO-DO: -update: instructions to process various inputs, e.g. HMO names, Medical Doctor names
	private static String processHmoNameWithHmoClassification(String hmoNameInputString) {
		//SortedSet<String> sortedKeyset = new TreeSet<String>(hmoContainer.keySet());

		//added by Mike, 20191231
		if (hmoNameInputString.contains("SLR")) {
			return hmoNameInputString;
		}
				
		String classificationKey = "";
		String subClassification = ""; 
		String classification = "";
		
		boolean hasHMOKeywords=false;
		
		String[] inputStringArray = hmoNameInputString.replace(" ", "").split(" ");
	
//		System.out.println(">>>>>>> hmoNameInputString: "+inputStringArray[0]);

		int threshold = 2; //3;

//		for (String inputString : sortedKeyset) {					
			//edited by Mike, 20191230
//			String[] inputStringArray = inputString.replace(" ","").split(" "); //delete space
			
			//added by Mike, 20190224
//			String[] inputStringArray = inputString.replace("-"," ").split(" ");				
//			System.out.println(">>>>>>> inputString: "+inputString);
//			System.out.println(">>>>>>> inputStringArray: "+inputStringArray[0]);



			//edited by Mike, 20190430
//			for (String knownDiagnosedCasesKey : sortedKnownDiagnosedCasesKeyset) {	 //the key is the sub-classification
			for (int h=0; h<hmoContainerArrayList.size(); h++) {	 //the key is the sub-classification
			
//				System.out.println("knownDiagnosedCasesKey: "+knownDiagnosedCasesKey);
//				System.out.println("knownDiagnosedCasesKey: "+hmoContainerArrayList.get(h)[0]);
			
				hasHMOKeywords=false;
//				subClassification = knownDiagnosedCasesKey; 
				subClassification = hmoContainerArrayList.get(h)[0]; 
//				classification = knownhmoContainer.get(knownDiagnosedCasesKey);
				classification = hmoContainerArrayList.get(h)[1];
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
					//edited by Mike, 20190430
					for(k=0; k<inputStringArray.length; k++) {		
//					for(k=inputStringArray.length-1; k>=0; k--) {		
//						System.out.println(">> "+inputStringArray[k]);

						String key = inputStringArray[k].trim().toUpperCase();
						String keyTwo = s[i].trim().toUpperCase();
						
						//edited by Mike 20191231
//						if (inputStringArray[k].trim().toUpperCase().equals(s[i].trim().toUpperCase())) {
	
						if (key.equals(keyTwo)) {
							hasHMOKeywords=true;
							break;
						}
						else if (myLevenshteinDistance.apply(key, keyTwo)<threshold) {					
							hasHMOKeywords=true;
							break;
						}
//						else {
//							System.out.println(">> true: "+inputString +" : "+s[i]);
//						}						
					}

					if (k==inputStringArray.length) {
						hasHMOKeywords=false;
						break;
					}
				}			
				if (hasHMOKeywords) {
					break;
				}
			}
			
			//edited by Mike, 20192030
			//classificationKey = inputString;
			classificationKey = inputStringArray[0];
			
			if (hasHMOKeywords) {
				classificationKey = classification;
/*				
				if (inputString.toLowerCase().contains("fx")) {					
					System.out.println(">>> inputString: "+inputString);
					System.out.println(">>> classificationKey: "+classificationKey);
				}
*/
//				break;
			}
			
//			System.out.println("classificationKey: " + classificationKey);

			return classificationKey;
			
//		}
	}	
		
	//added by Mike, 20191231
	private static String processMedicalDoctorNameWithMedicalDoctorClassification(String inputString, ArrayList<String[]> containerArrayList) {
/*
		//added by Mike, 20191231
		if (nameInputString.contains("SLR")) {
			return nameInputString;
		}
*/				
		String classificationKey = "";
		String subClassification = ""; 
		String classification = "";
		
		boolean hasKeywords=false;
	
		//edited by Mike, 20191231
		//String[] inputStringArray = inputString.replace(" ", "").split(" ");

/*
		if (inputString.contains(",")) {
			System.out.println("> inputString: "+inputString);
		}
*/	
		//automatically correct input Medical Doctor name
//		String[] inputStringArray = inputString.replace("\"", "").replace(",", ".").replace(".", ". ").split(" ");
		inputString = inputString.replace("\"", "");
		inputString = inputString.replace(",", ".");
//		inputString = inputString.replace(".", ". ");
		
		String[] inputStringArray  = inputString.split(" ");

/*		if (inputString.contains(",")) {
			System.out.println(">>> inputString: "+inputString);
		}
*/
					
//		System.out.println(">>>>>>> inputString: "+inputStringArray[0]);

		int threshold = 3; //3, instead of 2 for Medical Doctors

		for (int h=0; h<containerArrayList.size(); h++) {
			hasKeywords=false;
			subClassification = containerArrayList.get(h)[0]; 
			classification = containerArrayList.get(h)[1];

			String[] s = subClassification.split(" ");
			
			for(int i=0; i<s.length; i++) {			
				int k;

				for(k=0; k<inputStringArray.length; k++) {		
					String key = inputStringArray[k].trim().toUpperCase();
					String keyTwo = s[i].trim().toUpperCase();
					
					if (key.equals(keyTwo)) {
						hasKeywords=true;
						break;
					}
					else if (myLevenshteinDistance.apply(key, keyTwo)<threshold) {					
						hasKeywords=true;
						break;
					}
				}

				if (k==inputStringArray.length) {
					hasKeywords=false;
					break;
				}
			}			
			if (hasKeywords) {
				break;
			}
		}
		
		//classificationKey = inputString;
		//edited by Mike, 20191231
//		classificationKey = inputStringArray[0];
		StringBuffer inputStringBuffer = new StringBuffer();
		for(int k=0; k<inputStringArray.length; k++) {		
			inputStringBuffer.append(inputStringArray[k]+" ");
		}
		
//		if (inputStringBuffer.toString().contains(",")) {
//			System.out.println("inputStringBuffer.toString(): " + inputStringBuffer.toString());
//		}
		
		classificationKey = inputStringBuffer.toString().trim();
		
		if (hasKeywords) {
//		System.out.println("classification: " + classification);			
			classificationKey = classification;
		}
		
//		System.out.println("classificationKey: " + classificationKey);

		return classificationKey;			
	}	
	
	//added by Mike, 20200101; edited by Mike, 20200114
	private static String autoCorrectClassification(String inputString) {
		inputString = inputString.trim().toUpperCase();
		
		//added by Mike, 20200114
		inputString = inputString.replace("\"","");
		
		if (inputString.equals("NC")) {
			inputString = "NO CHARGE";
		}
		else if (inputString.equals("SENIOR CITIZEN")) {
			inputString = "SC";
		}

		if (inputString.contains("WI")) {
			//inputString = inputString.replace(" ","");

			inputString = inputString.replace("WI(", "WI (");
			
			if (inputString.contains("(")) {
				if (!inputString.contains("C/O")) {
					inputString = inputString.replace("(", "(C/O ");					
				}
				else {
					inputString = inputString.replace("C/O", "");									
					inputString = inputString.replace("(", "(C/O ");									
				}
			}
			
			String[] inputStringArray  = inputString.split(" ");
			
			StringBuffer inputStringBuffer = new StringBuffer();
			for(int k=0; k<inputStringArray.length; k++) {		
				if (!inputStringArray[k].equals("")) { //blank is included after using split(...)
					inputStringBuffer.append(inputStringArray[k]+" ");
				}			
			}
			
			inputString = inputStringBuffer.toString().trim();			
		}
		
		return inputString;
	}

	//added by Mike, 20201228
	//Reference: https://stackoverflow.com/questions/1102891/how-to-check-if-a-string-is-numeric-in-java;
	//last accessed: 20201227
	//answer by: CraigTP, 20090709T0955
	//edited by: Javad Besharati, 20190302T0927
	public static boolean isNumeric(String str) {
	  NumberFormat formatter = NumberFormat.getInstance();
	  ParsePosition pos = new ParsePosition(0);
	  formatter.parse(str, pos);
	  return str.length() == pos.getIndex();
	}
}