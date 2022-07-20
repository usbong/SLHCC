/*
 * Copyright 2018~2022 SYSON, MICHAEL B.
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
 * @last updated: 20220720; from 20220719
 * @website address: http://www.usbong.ph
 *
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

//added by Mike, 20210716
import java.util.Date; 
import java.text.DateFormat; 
import java.text.SimpleDateFormat;

/*
' Given:
' 1) Encoding for the Month Input Worksheet
' --> Saved/Exported as "Tab delimited" .txt file from Excel
' --> Example: input201808.txt (where the date format is YYYYMM; based on ISO 8601)
'
' Output:
' 1) Auto-generated Unpaid HMO Summary Report
' --> "Tab delimited" .txt file 
' --> Regardless of the name of the input file or input files, the output file will be "UnpaidHMOSummaryReportOutput.txt".
'
' Notes:
' 1) To execute the add-on software/application simply use the following command:
'   java generateUnpaidHMOSummaryReportOfAllInputFilesFromMasterList input201801.txt
' 
' where: "input201801.txt" is the name of the file.
' 
' 2) To execute a set of input files, e.g. input201801.txt, input201802.txt, you can use the following command: 
'   java generateUnpaidHMOSummaryReportOfAllInputFilesFromMasterList input*
' 
' where: "input*" means any file in the directory that starts with "input".
'
' 3) Make sure to include "Consultation" in the input file name.
' --> This is so that the add-on software would be able to properly identify it as a set of "Consultation" transactions, instead of those of "Treatment".
' --> Example: inputConsultation201801.txt
'
' 4) If you use space in your file name, e.g. "input Consultation 201801.txt", you will have to execute the input files as follows.
'   java generateUnpaidHMOSummaryReportOfAllInputFilesFromMasterList *"2018"*.txt
'
' where: * means any set of characters
*/ 

public class generateUnpaidHMOSummaryReportOfAllInputFilesFromMasterList {	
	private static boolean inDebugMode = true;
	private static String inputFilename = "input201801"; //without extension; default input file
	
	private static String medicalDoctorInput = ""; //added by Mike, 20200216
	private static String medicalDoctorCompleteNameInput = ""; //added by Mike, 20210309
	private static PrintWriter consultationWriter; //added by Mike, 20200217
	
	private static boolean hasProcessedPTTreatment = false; //added by Mike, 20200217
	
	private static String startDate = null;
	private static String endDate = null;
	
	private static final int INPUT_REFERRING_DOCTOR_COLUMN = 15;
	private static final int INPUT_NOTES_COLUMN = 0;
	private static final int INPUT_DATE_COLUMN = 1;
	private static final int INPUT_NAME_COLUMN = 3;
	private static final int INPUT_CLASS_COLUMN = 8; //HMO and NON-HMO
	private static final int INPUT_FEE_COLUMN = 7; //added by Mike, 20190119
	private static final int INPUT_CONSULTATION_FEE_COLUMN = 8; //added by Mike, 20190119
	private static final int INPUT_NET_PF_COLUMN = 10;
	private static final int INPUT_NEW_OLD_COLUMN = 16;
	private static final int INPUT_APPROVAL_CODE_COLUMN = 12; //added by Mike, 20190119

	private static final int INPUT_CONSULTATION_PROCEDURE_COLUMN = 2;
	//added by Mike, 20190107
	private static final int INPUT_CONSULTATION_MEDICAL_CERTIFICATE_COLUMN = 2; //The int value is the same as "INPUT_CONSULTATION_PROCEDURE_COLUMN".
	private static final int INPUT_CONSULTATION_MEDICAL_DOCTOR_COLUMN = 16; //added by Mike, 20190122

	//added by Mike, 20181218
	//CONSULTATION
/*	
	private static final int INPUT_CONSULTATION_CLASS_COLUMN = 9;
	private static final int INPUT_CONSULTATION_NET_PF_COLUMN = 11;
	private static final int INPUT_CONSULTATION_NEW_OLD_COLUMN = 17;
*/	
	private static final int INPUT_CONSULTATION_OFFSET = 1;

	//added by Mike, 20210716
	private static HashMap<String, Double> medicalDoctorContainer;	
	
		
/*	private static HashMap<String, double[]> referringDoctorContainer;	
*/

	private static ArrayList<String[]> transactionDateContainer;	//added by Mike, 20190119
/*  //removed by Mike, 20210121
	private static HashMap<Integer, double[]> dateContainer;	//added by Mike, 201801205
	private static HashMap<String, double[]> hmoContainer;	//added by Mike, 201801217
	private static HashMap<String, double[]> nonHmoContainer;	//added by Mike, 201801217
	private static HashMap<String, double[]> referringDoctorContainer; //added by Mike, 20181218
*/
	
	private static String[] columnValuesStringArray; //added by Mike, 20190119
	
	private static double[] columnValuesArray;
	private static String[] dateValuesArray; //added by Mike, 20180412
	private static int[] dateValuesArrayInt; //added by Mike, 20181206
	//private static ArrayList<int> dateValuesArrayInt; //edited by Mike, 20181221
		
	//the date and the referring doctor are not yet included here
	//this is for both HMO and NON-HMO transactions
	private static final int OUTPUT_TOTAL_COLUMNS = 23; //edited by Mike, 20190107

	//PT TREATMENT
	//edited by Mike, 20220719
	//private static final int OUTPUT_HMO_COUNT_COLUMN = 0; //transaction count
	private static final int OUTPUT_HMO_NOTES_COLUMN = 0; //transaction notes
	
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

	//added by Mike, 20190119
	private static final int OUTPUT_HMO_FEE_COLUMN = 17;
	private static final int OUTPUT_HMO_APPROVAL_CODE_COLUMN = 18;
	private static final int OUTPUT_HMO_NAME_COLUMN = 19;
	private static final int OUTPUT_HMO_CLASS_COLUMN = 20;
	private static final int OUTPUT_HMO_FILE_TYPE_COLUMN = 21; //Consultation or PT Treatment?
	private static final int OUTPUT_HMO_DATE_COLUMN = 22;

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
	
	//added by Mike, 20210716
	private static double totalUnpaidHMOFeeConsultation;
	private static double totalUnpaidSLRFeeConsultation;
	private static double totalUnpaidHMOFeeTreatment;		
	private static double totalUnpaidSLRFeeTreatment;	
	private static double grandTotalFeeConsultationTreatment; //added by Mike, 20211019
	
	//added by Mike, 20220720
	//private static double dTotalUnderpaymentAmount;	
	private static double dTotalFeePaidWithUnderpayment;
	
					
	public static void main ( String[] args ) throws Exception
	{					
		makeFilePath("output"); //"output" is the folder where I've instructed the add-on software/application to store the output file			
		
		//removed by Mike, 20200217
/*		
		PrintWriter consultationWriter = new PrintWriter("output/UnpaidHMOSummaryReportOutputConsultation.txt", "UTF-8");			
*/		
		PrintWriter treatmentWriter = new PrintWriter("output/UnpaidHMOSummaryReportOutputTreatment.txt", "UTF-8");			

		/*referringDoctorContainer = new HashMap<String, double[]>();
		*/

/*		
		transactionDateContainer = new ArrayList<String[]>(); //added by Mike, 20190119
		
		dateContainer = new HashMap<Integer, double[]>();
		hmoContainer = new HashMap<String, double[]>();
		nonHmoContainer = new HashMap<String, double[]>();
		referringDoctorContainer = new HashMap<String, double[]>();
//		medicalDoctorContainer = new HashMap<String, double[]>();
		classificationContainerPerMedicalDoctor = new HashMap<String, HashMap<String, double[]>>();							
*/		
		//added by Mike, 20210716
		medicalDoctorContainer = new HashMap<String, Double>();
		
		//added by Mike, 20181116
		startDate = null; //properly set the month and year in the output file of each input file
		dateValuesArray = new String[args.length]; //added by Mike, 20180412
		dateValuesArrayInt = new int[args.length]; //added by Mike, 20180412
		//dateValuesArrayInt = new ArrayList<int>(); //edited by Mike, 20181221

		//added by Mike, 20200217		
		//medicalDoctorInput = "CIELO"; //added by Mike, 20200216

		File medicalDoctorInputFile = new File("assets/MedicalDoctorList.txt");

		//added by Mike, 20210716
		totalUnpaidHMOFeeTreatment=0;
		
		Scanner sc = new Scanner(new FileInputStream(medicalDoctorInputFile));				
	
		//medicalDoctorInput=sc.nextLine(); //skip the first row, which is the input file's table headers

		//count/compute the number-based values of inputColumns 
		while (sc.hasNextLine()) {
			//edited by Mike, 20210309
			medicalDoctorInput=sc.nextLine();			
			medicalDoctorCompleteNameInput=medicalDoctorInput.split("\t")[1]; //added by Mike, 20210309
			medicalDoctorInput=medicalDoctorInput.split("\t")[0]; //added by Mike, 20210309			
			
			consultationWriter = new PrintWriter("output/UnpaidHMOSummaryReportOutputConsultation" + medicalDoctorInput+".txt", "UTF-8");			
			
			transactionDateContainer = new ArrayList<String[]>();

			//added by Mike, 20210121
			ArrayList<String[]> slrTransactionContainer = new ArrayList<String[]>();
			
			//PART/COMPONENT/MODULE/PHASE 1
			processInputFiles(args, true);	
			
			//OUTPUT
			//added by Mike, 20181118; edited by Mike, 20210309
//			consultationWriter.print("Unpaid HMO Summary Report (CONSULTATION)\n");
			consultationWriter.print("Unpaid HMO Summary Report (CONSULTATION): "+medicalDoctorCompleteNameInput+"\n");

			//--------------------------------------------------------------------
			//added by Mike, 20190122; edited by Mike, 20210715
			totalUnpaidHMOFeeConsultation = 0;
//			double totalUnpaidHMOFeeTreatment = 0;
			totalUnpaidSLRFeeConsultation = 0; //added by Mike, 20210121

			//added by Mike, 20210415; edited by Mike, 20210715
			totalUnpaidSLRFeeTreatment = 0;
			
			//added by Mike, 20220720
			dTotalFeePaidWithUnderpayment=0.0;
						
			//init table header names
	//		writer.print("CONSULTATION\n");
			consultationWriter.print("DATE:\tPATIENT NAME:\tFEE:\tCLASSIFICATION:\tAPPROVAL CODE:\tUNPAID REASON:\n"); 		
			for(int i=0; i<transactionDateContainer.size(); i++) {
				if (transactionDateContainer.get(i)[OUTPUT_HMO_FILE_TYPE_COLUMN].toLowerCase().trim().equals("consultation")){
					//edited by Mike, 20210121
/*					
					consultationWriter.print(
									transactionDateContainer.get(i)[OUTPUT_HMO_DATE_COLUMN]+"\t"+
									transactionDateContainer.get(i)[OUTPUT_HMO_NAME_COLUMN]+"\t"+
									transactionDateContainer.get(i)[OUTPUT_HMO_FEE_COLUMN]+"\t"+
									transactionDateContainer.get(i)[OUTPUT_HMO_CLASS_COLUMN]+"\t"+
	//								transactionDateContainer.get(i)[OUTPUT_HMO_APPROVAL_CODE_COLUMN]+"\n"
									"\t\n"
								); 				   											
*/
					if (transactionDateContainer.get(i)[OUTPUT_HMO_CLASS_COLUMN].toLowerCase().trim().contains("slr")){												
						slrTransactionContainer.add(transactionDateContainer.get(i));
					}
					else {
						consultationWriter.print(
										transactionDateContainer.get(i)[OUTPUT_HMO_DATE_COLUMN]+"\t"+
										transactionDateContainer.get(i)[OUTPUT_HMO_NAME_COLUMN]+"\t"+
										autoAddCommaToNumber(Double.parseDouble(transactionDateContainer.get(i)[OUTPUT_HMO_FEE_COLUMN]))+"\t"+
										transactionDateContainer.get(i)[OUTPUT_HMO_CLASS_COLUMN]+"\t"+
		//								transactionDateContainer.get(i)[OUTPUT_HMO_APPROVAL_CODE_COLUMN]+"\n"
										"\t\n"
									);
						
						
						//added by Mike, 20210714
						totalUnpaidHMOFeeConsultation += Double.parseDouble(transactionDateContainer.get(i)[OUTPUT_HMO_FEE_COLUMN].replace("\"","").replace(",",""));						
					}

					//added by Mike, 20190122; removed by Mike, 20210714
					//totalUnpaidHMOFeeConsultation += Double.parseDouble(transactionDateContainer.get(i)[OUTPUT_HMO_FEE_COLUMN].replace("\"","").replace(",",""));
				}
			}
			//edited by Mike, 20210121
//			consultationWriter.print("TOTAL:\t\t"+totalUnpaidHMOFeeConsultation+"\n"); 					
			consultationWriter.print("TOTAL:\t\t"+autoAddCommaToNumber(totalUnpaidHMOFeeConsultation)+"\n"); 	
			
			//added by Mike, 20211019
			grandTotalFeeConsultationTreatment+=totalUnpaidHMOFeeConsultation;
			
			//added by Mike, 20210121
			if (slrTransactionContainer.size()>0) {
				consultationWriter.print("\nUnpaid SLR\n"); 	

				for(int i=0; i<slrTransactionContainer.size(); i++) {
					consultationWriter.print(
									slrTransactionContainer.get(i)[OUTPUT_HMO_DATE_COLUMN]+"\t"+
									slrTransactionContainer.get(i)[OUTPUT_HMO_NAME_COLUMN]+"\t"+
									autoAddCommaToNumber(Double.parseDouble(slrTransactionContainer.get(i)[OUTPUT_HMO_FEE_COLUMN]))+"\t"+
									slrTransactionContainer.get(i)[OUTPUT_HMO_CLASS_COLUMN]+"\t"+
	//								slrTransactionContainer.get(i)[OUTPUT_HMO_APPROVAL_CODE_COLUMN]+"\n"
									"\t\n"
								);			
					
					 totalUnpaidSLRFeeConsultation += Double.parseDouble(slrTransactionContainer.get(i)[OUTPUT_HMO_FEE_COLUMN].replace("\"","").replace(",",""));						 
				}
				
				//added by Mike, 20210415
				//includes SLR transactions in the total count
				consultationWriter.print("TOTAL:\t\t"+autoAddCommaToNumber(totalUnpaidSLRFeeConsultation)+"\n"); 	
				
				//added by Mike, 20211019
				grandTotalFeeConsultationTreatment+=totalUnpaidSLRFeeConsultation;
			}
						
			consultationWriter.close();		
			
			//added by Mike, 20210415
			slrTransactionContainer.clear();
			
			//added by Mike, 20210716
			double dTotalUnpaidFeeInput = totalUnpaidHMOFeeConsultation+totalUnpaidSLRFeeConsultation;
			medicalDoctorContainer.put(medicalDoctorCompleteNameInput,dTotalUnpaidFeeInput);
			
			//added by Mike, 20200217
			if (medicalDoctorInput.equals("PEDRO")) {
				//edited by Mike, 20210716
				//double totalUnpaidHMOFeeTreatment = 0;
				totalUnpaidHMOFeeTreatment = 0;

				treatmentWriter.print("Unpaid HMO Summary Report (PT TREATMENT)\n");

		//		treatmentWriter.print("\nPT TREATMENT\n");
				
				//edited by Mike, 20220720
				//treatmentWriter.print("DATE:\tPATIENT NAME:\tFEE:\tCLASSIFICATION:\tAPPROVAL CODE:\tUNPAID REASON:\n"); 		
				treatmentWriter.print("DATE:\tPATIENT NAME:\tFEE:\tCLASSIFICATION:\tAPPROVAL CODE:\tUNPAID REASON:\tUNDERPAYMENT\tPAID\n"); 
				
				for(int i=0; i<transactionDateContainer.size(); i++) {
					if (transactionDateContainer.get(i)[OUTPUT_HMO_FILE_TYPE_COLUMN].toLowerCase().trim().equals("treatment")){
						//edited by Mike, 20210415
						if (transactionDateContainer.get(i)[OUTPUT_HMO_CLASS_COLUMN].toLowerCase().trim().contains("slr")){												
							slrTransactionContainer.add(transactionDateContainer.get(i));
						}
						else {
						
/* //edited by Mike, 20220719		
							treatmentWriter.print(
											transactionDateContainer.get(i)[OUTPUT_HMO_DATE_COLUMN]+"\t"+
											transactionDateContainer.get(i)[OUTPUT_HMO_NAME_COLUMN]+"\t"+
											autoAddCommaToNumber(Double.parseDouble(transactionDateContainer.get(i)[OUTPUT_HMO_FEE_COLUMN]))+"\t"+
											transactionDateContainer.get(i)[OUTPUT_HMO_CLASS_COLUMN]+"\t"+
											transactionDateContainer.get(i)[OUTPUT_HMO_APPROVAL_CODE_COLUMN]+"\n"
										); 				   
*/


System.out.println(">>>>" +transactionDateContainer.get(i)[OUTPUT_HMO_NOTES_COLUMN]);

String sNoteUnderpayment="";
Double dNoteUnderpaymentAmount=0.0;
Double dFeePaidWithUnderpayment=0.0;

String sInputNote = transactionDateContainer.get(i)[OUTPUT_HMO_NOTES_COLUMN].toLowerCase().trim();

//edited by Mike, 20220720
//if (transactionDateContainer.get(i)[OUTPUT_HMO_NOTES_COLUMN].toLowerCase().trim().contains("underpayment")) {
if (sInputNote.contains("underpayment")) {

/*	
	treatmentWriter.print("underpayment");
	treatmentWriter.close();
	
	System.out.println("underpayment!");
	System.exit(0);	
*/	

//	System.out.println("PT workbook: underpayment! "+transactionDateContainer.get(i)[OUTPUT_HMO_DATE_COLUMN]+" "+transactionDateContainer.get(i)[OUTPUT_HMO_NAME_COLUMN]);

	//edited by Mike, 20220720
	sNoteUnderpayment="underpayment";
	
	String[] sInputNoteUnderpaymentArrayPartOne = sInputNote.split("underpayment");		
	
	if (sInputNoteUnderpaymentArrayPartOne[1]!=null) {
		
		//note: output error if empty string
		dNoteUnderpaymentAmount=Double.parseDouble(sInputNoteUnderpaymentArrayPartOne[1].trim().replace("\"","").split(" ")[0]);
		
		dFeePaidWithUnderpayment= (Double.parseDouble(transactionDateContainer.get(i)[OUTPUT_HMO_FEE_COLUMN])-dNoteUnderpaymentAmount);

		//edited by Mike, 20220720
		//sNoteUnderpayment="underpayment\t"+dNoteUnderpaymentAmount;
		//sNoteUnderpayment="underpayment\t"+dNoteUnderpaymentAmount+"\t"+;		
		sNoteUnderpayment="underpayment\t"+dNoteUnderpaymentAmount+"\t"+dFeePaidWithUnderpayment+"\t";

	System.out.println("PT workbook: underpayment! "+transactionDateContainer.get(i)[OUTPUT_HMO_DATE_COLUMN]+" "+transactionDateContainer.get(i)[OUTPUT_HMO_NAME_COLUMN]);

		dTotalFeePaidWithUnderpayment+=dFeePaidWithUnderpayment;

	}	
	else {
		sNoteUnderpayment="REVERIFY AMOUNT!\t";
	}
}

							treatmentWriter.print(
											transactionDateContainer.get(i)[OUTPUT_HMO_DATE_COLUMN]+"\t"+
											transactionDateContainer.get(i)[OUTPUT_HMO_NAME_COLUMN]+"\t"+
											autoAddCommaToNumber(Double.parseDouble(transactionDateContainer.get(i)[OUTPUT_HMO_FEE_COLUMN]))+"\t"+
											transactionDateContainer.get(i)[OUTPUT_HMO_CLASS_COLUMN]+"\t"+
																						transactionDateContainer.get(i)[OUTPUT_HMO_APPROVAL_CODE_COLUMN]+"\t"+
																						
sNoteUnderpayment+"\n"															
										); 				   										
																					
						}

						
						//added by Mike, 20190122; added by Mike, 20210415
						//includes SLR transactions in the total count						
						totalUnpaidHMOFeeTreatment += Double.parseDouble(transactionDateContainer.get(i)[OUTPUT_HMO_FEE_COLUMN].replace("\"","").replace(",",""));
					}
				}

				//edited by Mike, 20220720
				//treatmentWriter.print("TOTAL:\t\t"+autoAddCommaToNumber(totalUnpaidHMOFeeTreatment)+"\n"); 		

double dTotalUnpaidHMOFeeTreatmentWithUnderpaymentAmount=totalUnpaidHMOFeeTreatment-dTotalFeePaidWithUnderpayment;

					treatmentWriter.print("TOTAL:\t\t"+autoAddCommaToNumber(totalUnpaidHMOFeeTreatment)+"\t"+
						"-"+autoAddCommaToNumber(dTotalFeePaidWithUnderpayment)+"\t"+
						autoAddCommaToNumber(dTotalUnpaidHMOFeeTreatmentWithUnderpaymentAmount)+"\t"+
						"\n"); 	

				//added by Mike, 20211019; edited by Mike, 20220720
				//grandTotalFeeConsultationTreatment+=totalUnpaidHMOFeeTreatment;
				grandTotalFeeConsultationTreatment+=dTotalUnpaidHMOFeeTreatmentWithUnderpaymentAmount;

				
				//added by Mike, 20210415
				if (slrTransactionContainer.size()>0) {
					treatmentWriter.print("\nUnpaid SLR\n"); 	

					for(int i=0; i<slrTransactionContainer.size(); i++) {
						treatmentWriter.print(
										slrTransactionContainer.get(i)[OUTPUT_HMO_DATE_COLUMN]+"\t"+
										slrTransactionContainer.get(i)[OUTPUT_HMO_NAME_COLUMN]+"\t"+
										autoAddCommaToNumber(Double.parseDouble(slrTransactionContainer.get(i)[OUTPUT_HMO_FEE_COLUMN]))+"\t"+
										slrTransactionContainer.get(i)[OUTPUT_HMO_CLASS_COLUMN]+"\t"+
		//								slrTransactionContainer.get(i)[OUTPUT_HMO_APPROVAL_CODE_COLUMN]+"\n"
										"\t\n"
									);			

						 totalUnpaidSLRFeeTreatment += Double.parseDouble(slrTransactionContainer.get(i)[OUTPUT_HMO_FEE_COLUMN].replace("\"","").replace(",",""));						 
					}

					treatmentWriter.print("TOTAL:\t\t"+autoAddCommaToNumber(totalUnpaidSLRFeeTreatment)+"\n"); 	

					//added by Mike, 20211019
					grandTotalFeeConsultationTreatment+=totalUnpaidSLRFeeTreatment;
				}				
				
		//		consultationWriter.close();		//removed by Mike, 20200217
				treatmentWriter.close();
			}
		}
		
		//added by Mike, 20210716
		autoGenerateUnpaidHMOSummaryReportOutputHTML(args);
		
/*
		//PART/COMPONENT/MODULE/PHASE 1
		processInputFiles(args, true);
*/
		//PART/COMPONENT/MODULE/PHASE 2		
/*		setClassificationContainerPerMedicalDoctor(classificationContainerPerMedicalDoctor);
		processInputFiles(args, false);
*/				
		
		/*
		 * --------------------------------------------------------------------
		 * OUTPUT
		 * --------------------------------------------------------------------
		*/
/*		
		//added by Mike, 20181118
		consultationWriter.print("Unpaid HMO Summary Report (CONSULTATION)\n");
		
		//--------------------------------------------------------------------
		//added by Mike, 20190122
		double totalUnpaidHMOFeeConsultation = 0;
		double totalUnpaidHMOFeeTreatment = 0;
		
		//init table header names
//		writer.print("CONSULTATION\n");
		consultationWriter.print("DATE:\tPATIENT NAME:\tFEE:\tCLASSIFICATION:\tAPPROVAL CODE:\tUNPAID REASON:\n"); 		
		for(int i=0; i<transactionDateContainer.size(); i++) {
			if (transactionDateContainer.get(i)[OUTPUT_HMO_FILE_TYPE_COLUMN].toLowerCase().trim().equals("consultation")){
				consultationWriter.print(
								transactionDateContainer.get(i)[OUTPUT_HMO_DATE_COLUMN]+"\t"+
								transactionDateContainer.get(i)[OUTPUT_HMO_NAME_COLUMN]+"\t"+
								transactionDateContainer.get(i)[OUTPUT_HMO_FEE_COLUMN]+"\t"+
								transactionDateContainer.get(i)[OUTPUT_HMO_CLASS_COLUMN]+"\t"+
//								transactionDateContainer.get(i)[OUTPUT_HMO_APPROVAL_CODE_COLUMN]+"\n"
								"\t\n"
							); 				   											
							
				//added by Mike, 20190122
				totalUnpaidHMOFeeConsultation += Double.parseDouble(transactionDateContainer.get(i)[OUTPUT_HMO_FEE_COLUMN].replace("\"","").replace(",",""));
			}
		}
		consultationWriter.print("TOTAL:\t\t"+totalUnpaidHMOFeeConsultation+"\n"); 		
*/
/*
		double totalUnpaidHMOFeeTreatment = 0;

		treatmentWriter.print("Unpaid HMO Summary Report (PT TREATMENT)\n");

//		treatmentWriter.print("\nPT TREATMENT\n");
		treatmentWriter.print("DATE:\tPATIENT NAME:\tFEE:\tCLASSIFICATION:\tAPPROVAL CODE:\tUNPAID REASON:\n"); 		
		for(int i=0; i<transactionDateContainer.size(); i++) {
			if (transactionDateContainer.get(i)[OUTPUT_HMO_FILE_TYPE_COLUMN].toLowerCase().trim().equals("treatment")){
				treatmentWriter.print(
								transactionDateContainer.get(i)[OUTPUT_HMO_DATE_COLUMN]+"\t"+
								transactionDateContainer.get(i)[OUTPUT_HMO_NAME_COLUMN]+"\t"+
								transactionDateContainer.get(i)[OUTPUT_HMO_FEE_COLUMN]+"\t"+
								transactionDateContainer.get(i)[OUTPUT_HMO_CLASS_COLUMN]+"\t"+
								transactionDateContainer.get(i)[OUTPUT_HMO_APPROVAL_CODE_COLUMN]+"\n"
							); 				   											
							
				//added by Mike, 20190122
				totalUnpaidHMOFeeTreatment += Double.parseDouble(transactionDateContainer.get(i)[OUTPUT_HMO_FEE_COLUMN].replace("\"","").replace(",",""));
			}
		}
		treatmentWriter.print("TOTAL:\t\t"+totalUnpaidHMOFeeTreatment+"\n"); 		
		
//		consultationWriter.close();		//removed by Mike, 20200217
		treatmentWriter.close();
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

	//added by Mike, 20210121
	//note: comma removed when importing to LibreOfficeCalc as number, not text
	//TO-DO: -add: second digit after the dot from left if no digit exists in that position
	private static String autoAddCommaToNumber(Double dNumberInput) {
		StringBuffer sbInput = new StringBuffer(""+dNumberInput);
		StringBuffer sbOutput = new StringBuffer("");

//		System.out.println("dNumberInput:"+dNumberInput);

		boolean bIsPositionBeforeDotFromRight=false;
		int iCountDigit=0;
		String sValueAtPosition;
		int iDotPosition=0;

//		System.out.println("sbInput.length():"+sbInput.length());

		for (int iCount=sbInput.length(); iCount>0; iCount--) {
//			System.out.println("value:"+sbInput.substring(iCount-1,iCount));
			
			sValueAtPosition=sbInput.substring(iCount-1,iCount);

			if (sValueAtPosition.equals(".")) {
				bIsPositionBeforeDotFromRight=true;
				iDotPosition=iCount-1;
			}
						
			if (bIsPositionBeforeDotFromRight) {
				if (iCountDigit==3) {
//					sValueAtPosition=sbInput.substring(iCount-1,iCount).replace(sValueAtPosition,","+sValueAtPosition);
					sValueAtPosition=","+sValueAtPosition;
					iCountDigit=0;
				}				
				iCountDigit=iCountDigit+1;
			}
			
//			System.out.println("sValueAtPosition:"+sValueAtPosition);
			
			sbOutput.insert(0,sValueAtPosition);
		}

		//delete excess comma if exists in position 0 from left
		String sOutput=sbOutput.substring(0,1).replace(",","").concat(sbOutput.substring(1));

		//added by Mike, 20210121
		//verify that there are two digits after the dot from the left
		sbOutput=new StringBuffer(sOutput);
		
		//removed by Mike, 20210714
		//System.out.println("sbOutput:"+sbOutput.substring(iDotPosition+1));
			
		//example: 1,200.0
		if (sbOutput.substring(iDotPosition+1).length()<2) {
			sOutput=sOutput+"0";
			//output: 1,200.00
		}	
/*	TO-DO: -update: this due to multiple dots
		//example: 1,200.67
		else if (sbOutput.substring(iDotPosition+1).length()==2) {
			sOutput=sbOutput.substring(0,iDotPosition).concat("."+sbOutput.substring(iDotPosition+1));
			//output: 1,200.67
		}
		//example: 1,200.678		
		else {
			System.out.println("sbOutputLength:"+sbOutput.substring(iDotPosition+1).length());

			sOutput=sbOutput.substring(0,iDotPosition).concat("."+sbOutput.substring(iDotPosition+1));
			//output: 1,200.67
		}
*/
		
//		System.out.println(sOutput);
		
		return sOutput; 
//		return sbOutput.toString(); 
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
	private static void processUnpaidHMOCount(ArrayList<String[]> transactionDateContainer, String[] inputColumns, boolean isConsultation) {
		columnValuesStringArray = new String[OUTPUT_TOTAL_COLUMNS];

		columnValuesStringArray[OUTPUT_HMO_DATE_COLUMN] = inputColumns[INPUT_DATE_COLUMN];
		columnValuesStringArray[OUTPUT_HMO_NAME_COLUMN] = inputColumns[INPUT_NAME_COLUMN];
		columnValuesStringArray[OUTPUT_HMO_APPROVAL_CODE_COLUMN] = inputColumns[INPUT_APPROVAL_CODE_COLUMN];

		//added by Mike, 20220719
		columnValuesStringArray[OUTPUT_HMO_NOTES_COLUMN] = inputColumns[INPUT_NOTES_COLUMN];
		
	
		//edited by Mike, 20181218
		if (!isConsultation) {											
			columnValuesStringArray[OUTPUT_HMO_FILE_TYPE_COLUMN] = "TREATMENT";
			columnValuesStringArray[OUTPUT_HMO_FEE_COLUMN] = inputColumns[INPUT_FEE_COLUMN];
			columnValuesStringArray[OUTPUT_HMO_CLASS_COLUMN] = inputColumns[INPUT_CLASS_COLUMN];

			//edited by Mike, 20181206
			if ((inputColumns[INPUT_CLASS_COLUMN].contains("HMO")) ||
				(inputColumns[INPUT_CLASS_COLUMN].contains("SLR"))) {
				if (!inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
					transactionDateContainer.add(columnValuesStringArray);
				}
				//added by Mike, 20220719
				else {
					if (inputColumns[INPUT_NOTES_COLUMN].contains("underpayment")) {
						transactionDateContainer.add(columnValuesStringArray);
					}
				}
			}
		}
		else {												
			//added by Mike, 20190122
			//do the set of instructions if the MEDICAL DOCTOR has the keywords "syson" and "pedro"
/*			if ((inputColumns[INPUT_CONSULTATION_MEDICAL_DOCTOR_COLUMN].toLowerCase().trim().contains("syson")) &&
						(inputColumns[INPUT_CONSULTATION_MEDICAL_DOCTOR_COLUMN].toLowerCase().trim().contains("pedro"))) {
*/
if ((inputColumns[INPUT_CONSULTATION_MEDICAL_DOCTOR_COLUMN].toUpperCase().trim().contains(medicalDoctorInput))) {/* &&
						(inputColumns[INPUT_CONSULTATION_MEDICAL_DOCTOR_COLUMN].toLowerCase().trim().contains(medicalDoctorInput))) {
*/
				columnValuesStringArray[OUTPUT_HMO_FILE_TYPE_COLUMN] = "CONSULTATION";
				columnValuesStringArray[OUTPUT_HMO_FEE_COLUMN] = inputColumns[INPUT_CONSULTATION_FEE_COLUMN];
				columnValuesStringArray[OUTPUT_HMO_CLASS_COLUMN] = inputColumns[INPUT_CLASS_COLUMN+INPUT_CONSULTATION_OFFSET];

				if ((inputColumns[INPUT_CLASS_COLUMN+INPUT_CONSULTATION_OFFSET].contains("HMO")) ||
					(inputColumns[INPUT_CLASS_COLUMN+INPUT_CONSULTATION_OFFSET].contains("SLR"))) {
					if (!inputColumns[INPUT_NOTES_COLUMN].contains("paid:")) {
						transactionDateContainer.add(columnValuesStringArray);
					}
					//added by Mike, 20220719
					else {
						if (inputColumns[INPUT_NOTES_COLUMN].contains("underpayment")) {
							transactionDateContainer.add(columnValuesStringArray);
						}
					}
					
				}							
			}					
		}
	}

	private static void processInputFiles(String[] args, boolean isPhaseOne) throws Exception {
		//added by Mike, 20201106
		String sFileExtension = ".txt";

		//edited by Mike, 20181030
		for (int i=0; i<args.length; i++) {						
			//added by Mike, 20181030; edited by Mike, 20201106
/*			inputFilename = args[i].replaceAll(".txt","");			
			File f = new File(inputFilename+".txt");
*/
			//added by Mike, 20201106
/*			if (args[i].contains(".txt")) {
				inputFilename = args[i].replaceAll(".txt","");
			}
			else */if (args[i].contains(".csv")) {
//				inputFilename = args[i].replaceAll(".csv","");
				sFileExtension = ".csv";
			}			
			//added by Mike, 20210716
			else if (args[i].contains(".html")) {
				continue;
			}
			
			inputFilename = args[i].replaceAll(sFileExtension,"");			
			File f = new File(inputFilename+sFileExtension);
			
			System.out.println("inputFilename: " + inputFilename);
			
			if (inputFilename.toLowerCase().contains("consultation")) {
				isConsultation=true;
			}
			else {
				isConsultation=false;				

				//added by Mike, 20200217
				if (!medicalDoctorInput.equals("PEDRO")) {					
					continue;
				}
				else {
					System.out.println(">>> " + medicalDoctorInput);
				}
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

				if (dateValuesArrayInt[i]==0) {
					//edited by Mike, 20201106
//					dateValuesArrayInt[i] = Integer.parseInt(args[i].substring(args[i].indexOf("_")+1,args[i].indexOf(".txt")));
					dateValuesArrayInt[i] = Integer.parseInt(args[i].substring(args[i].indexOf("_")+1,args[i].indexOf(sFileExtension)));
				}
/*
				int dateValueInt = Integer.parseInt(args[i].substring(args[i].indexOf("_")+1,args[i].indexOf(".txt")));
				if (!dateValuesArrayInt.contains(dateValueInt)){
					dateValuesArrayInt.add(dateValueInt);
				}				
*/				
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

				if (isPhaseOne) {
					//added by Mike, 20190119
					processUnpaidHMOCount(transactionDateContainer, inputColumns, isConsultation); //isConsultation = false
				}
			}		
			
			//added by Mike, 20210716
			if (inDebugMode) {
				rowCount++;
				System.out.println("rowCount: "+rowCount);
			}			
		}		
	}
	
	//added by Mike, 20210716; edited by Mike, 20211018
	//TO-DO: -add: previous Total Unpaid HMO Fee
	private static void autoGenerateUnpaidHMOSummaryReportOutputHTML(String[] args) throws Exception {
		PrintWriter unpaidHMOSummaryReportHTMLWriter = new PrintWriter("output/UnpaidHMOSummaryReportOutput.html", "UTF-8");			
				
		String sFileExtension = ".html";

		for (int i=0; i<args.length; i++) {
			if (args[i].contains(".html")) {
				sFileExtension = ".html";
			}
			else {
				continue;
			}
			
			inputFilename = args[i].replaceAll(sFileExtension,"");			
			File f = new File(inputFilename+sFileExtension);
							
			System.out.println("inputFilename: " + inputFilename);
						
			Scanner sc = new Scanner(new FileInputStream(f), "UTF-8");				
		
			String s;		

//			s=sc.nextLine(); //skip the first row, which is the input file's table headers
	
			if (inDebugMode) {
				rowCount=0;
			}
						
			while (sc.hasNextLine()) {
				s=sc.nextLine();
				
				//if the row is blank
				if (s.trim().equals("")) {
					continue;
				}
				
				//added by Mike, 20211018
				//TO-DO: -add: start and end of year-month based on input files
				//Example: CONSULTATION2020OnlyVerifiedMacroEnabledMasterListV59_202001.txt

				//TO-DO: -update: this				
/* //edited by Mike, 20211019				
				if (s.contains("<?php echo $data['date'];?>")) {					
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    				Date myDate = new Date();
					
					s=s.replace("<?php echo $data['date'];?>",dateFormat.format(myDate));
					
    				System.out.println(dateFormat.format(myDate));
				}
*/
				//get year based on first input file
				//note: startDate value obtained from processInputFiles(...) Command
				//example: startDate value = "01/-20", where 20 is year
				//TO-DO: -update: this
				s=s.replace("<?php echo $data['date'];?>","20"+startDate.substring(startDate.length()-2));
				
				if (s.contains("<!-- Table Values Row 2 -->")) {
					SortedSet<String> sortedMedicalDoctorKeyset = new TreeSet<String>(medicalDoctorContainer.keySet());
					StringBuffer sb = new StringBuffer(s);
					DecimalFormat df = new DecimalFormat("#,###.00"); //"0.00"
					int iMedicalDoctorRowCount = 0;
					
					for (String medicalDoctorKey : sortedMedicalDoctorKeyset) {													
//						System.out.println(medicalDoctorKey+": "+medicalDoctorContainer.get(medicalDoctorKey)+"\n");

						//edited by Mike, 20201716
						//sb.append("<tr>\n");

  						if (iMedicalDoctorRowCount % 2 == 0) { //even number
						  //edited by Mike, 20211020
//						  sb.append("<tr class=\"rowEvenNumber\">");
						  sb.append("\n<tr class=\"rowEvenNumber\">\n");
						}
						else {
						  //edited by Mike, 20211020
//						  sb.append("<tr>");
						  sb.append("<tr>\n");
						}				   										
						iMedicalDoctorRowCount=iMedicalDoctorRowCount+1;
						
						sb.append("<!-- Column 1 -->\n");
						sb.append("<td>\n");
						sb.append("<b>"+medicalDoctorKey+"</b>\n");
						sb.append("</td>\n");
						sb.append("<!-- Column 2 -->\n");
						sb.append("<td class='tdUnpaidHMOTotal'>\n");
						
						if (medicalDoctorContainer.get(medicalDoctorKey)==0) {
							sb.append("<b>0.00</b>\n");
						}
						else {
							sb.append("<b>"+df.format(medicalDoctorContainer.get(medicalDoctorKey))+"</b>\n");
						}
						
						sb.append("</td>\n");
						sb.append("</tr>\n");
					}		
					
					//PT Treatment
					sb.append("<!-- PT TREATMENT -->\n");
					sb.append("<!-- Column 1 -->\n");
					sb.append("<td>\n");
					sb.append("<b>PT TREATMENT</b>\n");
					sb.append("</td>\n");
					sb.append("<!-- Column 2 -->\n");
					sb.append("<td class='tdUnpaidHMOTotal'>\n");

					Double dTotalUnpaidHMOAndSLRTreatment=totalUnpaidHMOFeeTreatment+totalUnpaidSLRFeeTreatment;
										
					if (dTotalUnpaidHMOAndSLRTreatment==0) {
						sb.append("<b>0.00</b>\n");
					}
					else {
						sb.append("<b>"+df.format(dTotalUnpaidHMOAndSLRTreatment)+"</b>\n");
					}

					sb.append("</td>\n");
					sb.append("</tr>\n");
					
					s=sb.toString();
					
					//added by Mike, 20211019
					//GRAND TOTAL
					sb.append("<!-- GRAND TOTAL -->\n");
					sb.append("<!-- Column 1 -->\n");
					sb.append("<td class='tdUnpaidHMOGrandTotal'>\n");
					sb.append("<b>GRAND TOTAL</b>\n");
					sb.append("</td>\n");
					sb.append("<!-- Column 2 -->\n");
					sb.append("<td class='tdUnpaidHMOGrandTotal'>\n");

//					Double dTotalUnpaidHMOAndSLRTreatment=totalUnpaidHMOFeeTreatment+totalUnpaidSLRFeeTreatment;
										
					if (grandTotalFeeConsultationTreatment==0) {
						sb.append("<b>0.00</b>\n");
					}
					else {
						sb.append("<b>"+df.format(grandTotalFeeConsultationTreatment)+"</b>\n");
					}

					sb.append("</td>\n");
					sb.append("</tr>\n");
					
					s=sb.toString();
				}
				
				unpaidHMOSummaryReportHTMLWriter.print(s+"\n");
					
				if (inDebugMode) {
					rowCount++;
					System.out.println("rowCount: "+rowCount);
				}
			}	
			
			unpaidHMOSummaryReportHTMLWriter.close();
		}		
	}			
}
