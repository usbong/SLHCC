/*
  Copyright 2019~2020 Usbong Social Systems, Inc.
  
  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You ' may obtain a copy of the License at
  http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, ' WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing ' permissions and limitations under the License.
  
  @author: Michael Syson
  @date created: 20190807
  @date updated: 20201008
  
  Given:
  1) List with the details of the transactions for the day
  Output:
  1) Automatically process the transactions and send the details to a mobile telephone at the headquarters using Short Messaging Service (SMS)
  --> Example SMS Output (auto-generated from JSON format): 
  SLHCC,2020-10-8,PT,Total:1,CashTotalFee:0,CashTotalNetFee:0,HMOTotalFee:1100,HMOTotalNetFee:606.38
  
  --> JSON format: 
{"dHMOTotalFee":1100,"dCashTotalFee":0,"dCashTotalNetFee":0,"iTotal":1,"payslip_type_id":2,"dHMOTotalNetFee":606.375}

  Notes:
  1) The details of the transactions to be sent are in the JSON (JavaScript Object Notation) format.
    
  2) To compile on Linux Terminal the add-on software with the libraries, e.g. JSON .jar file, use the following command:
   javac -cp .:org.json.jar:org.apache.httpclient.jar:org.apache.httpcore.jar:org.apache.commons-logging.jar UsbongSMSReportMain.java
   
   NOTE: We use ":" in Linux Terminal, and ";" in Windows Command Prompt.
   javac -cp .;org.json.jar;org.apache.httpclient.jar;org.apache.httpcore.jar;org.apache.commons-logging.jar UsbongSMSReportMain.java
   
  3) To execute on Linux Terminal the add-on software with the JSON .jar file, i.e. json, use the following command:
   java -cp .:org.json.jar:org.apache.httpclient.jar:org.apache.httpcore.jar:org.apache.commons-logging.jar UsbongSMSReportMain
   NOTE: We use ":" in Linux Terminal, and ";" in Windows Command Prompt.  
  4) The JSON .jar file can be downloaded here:
   https://github.com/stleary/JSON-java; last accessed: 20190808
   
  5) The two (2) Apache HttpComponents, i.e. 1) HttpClient and 2) HttpCore .jar files (not beta) can be downloaded here:
   http://hc.apache.org/downloads.cgi; last accessed: 20190810
  6) The Apache commons-logging .jar is also necessary to execute the add-on software. The .jar file is present in the set of .jar files inside the "lib", i.e. library, folder of the zipped httpcomponents-client-<version>-bin folder. It is in this same library folder that you can find the Apache HttpComponent, HttpClient, .jar file.
     
  References:
  1) Introducing JSON. https://www.json.org/; last accessed: 20190807
  --> ECMA-404 The JSON Data Interchange Standard
  
  2) https://stackoverflow.com/questions/7181534/http-post-using-json-in-java; last accessed: 20190807
  --> answer by: Cigano Morrison Mendez on 20131111; edited on 20140819
  
  3) The Apache Software Foundation. (2019). The Official Apache HttpComponents Homepage. https://hc.apache.org/index.html; last accessed: 20190810
*/

import org.json.JSONObject;
import org.json.JSONArray;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.StringEntity;

import java.util.Scanner;
import java.nio.charset.StandardCharsets;

import java.io.PrintWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

//added by Mike, 20200916
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat; //added by Mike, 20200926

public class UsbongSMSReportMain {
	//added by Mike, 20190811
	private static boolean isInDebugMode = true;

	//added by Mike, 20190814; edited by Mike, 20190917
	private static boolean isForUpload = true;

	//edited by Mike, 20190918
	private static String serverIpAddress = "";//"http://localhost/";
	private static final String STORE_TRANSACTIONS_LIST_FOR_THE_DAY_UPLOAD = "usbong_kms/server/storetransactionslistfortheday.php";
	
	private static final String GET_TRANSACTIONS_LIST_FOR_THE_DAY_DOWNLOAD = "usbong_kms/server/gettransactionslistfortheday.php";

	//added by Mike, 20190812
	private static String inputFilename;
	private static int rowCount;

	//added by Mike, 20190811
	private static final int INPUT_OR_NUMBER_COLUMN = 0; //Official Receipt Number
	private static final int INPUT_PATIENT_NAME_COLUMN = 1;
	private static final int INPUT_CLASSIFICATION_COLUMN = 2;
	private static final int INPUT_AMOUNT_PAID_COLUMN = 3;
	private static final int INPUT_NET_PF_COLUMN = 4;

	//added by Mike, 20190916
	private static final int INPUT_WORKBOOK_DATE_COLUMN = 0;
	private static final int INPUT_WORKBOOK_FEE_COLUMN = 5;
	private static final int INPUT_WORKBOOK_CLASSIFICATION_COLUMN = 7;
	private static final int INPUT_WORKBOOK_AMOUNT_PAID_COLUMN = 8;
	private static final int INPUT_WORKBOOK_NET_PF_COLUMN = 9;
	
/*	
	private static String TAG = "usbong.HTTPConnect.storeTransactionsListForTheDay";	
	private static String TAG = "usbong.HTTPConnect.getTransactionsListForTheDay";	
*/

/*
	private String filePath = "";
	private String columnName = "";
	private String action = "";
*/	
	private URL url;
	private HttpURLConnection conn;

	public static void main(String[] args) throws Exception {
//		JSONObject json = new JSONObject();
//		json.put("myKey", "myValue");    

/*
		UsbongHTTPConnect main = new UsbongHTTPConnect();
*/
		UsbongSMSReportMain main = new UsbongSMSReportMain();

/*
		//added by Mike, 20190918
		serverIpAddress = args[0];
*/

		main.processSendSMS(new String[]{args[1]});

/* 		//removed by Mike, 20200916
		//edited by Mike, 20190918		
		if (isForUpload) {
			main.processUpload(new String[]{args[1]});
		}
		else {
			main.processDownload(new String[]{args[1]});
		}
*/		
	}

	//added by Mike, 20200916
	private void processSendSMS(String[] args) throws Exception {
		JSONObject json = processPayslipInputForSendSMS(args);	
			
		//added by Mike, 20200917
		if (json==null) {
			 return;
		}
			
			
		 //added by Mike, 20200917
		 //write output file
		 PrintWriter writer = new PrintWriter("output/smsReport"+getDateTodayISOFormat()+".txt", "UTF-8");
		 //PrintWriter writer = new PrintWriter("");
			
		 //edited by Mike, 20200926
//		 writer.print(json.toString());			
		
		 //edited by Mike, 20200929
		 //TO-DO: -reverify: rounding method
		 DecimalFormat df = new DecimalFormat("#.##");

		//edited by Mike, 20201008
//		 writer.print("SLHCC,");		
		 writer.print("SLHCC,"+getDateTodayISOFormat()+",");		
		 
		 //note: for HTC Wildfire (year 2012) Android, SMS body value does not accept as input the length of select string of characters, e.g. "PT Treatment"
		 //in its stead, we use "PT"
		 if (json.getInt("payslip_type_id") == 1) {
			//writer.print("Consultation,");		
			writer.print("CON,");	
		 }
		 else {
			//writer.print("PT Treatment,");
			writer.print("PT,");
		 }

		 writer.print("Total:"+json.getInt("iTotal")+",");		
		 writer.print("CashTotalFee:"+df.format(json.getDouble("dCashTotalFee"))+",");		
		 writer.print("CashTotalNetFee:"+df.format(json.getDouble("dCashTotalNetFee"))+",");		
				
		 writer.print("HMOTotalFee:"+df.format(json.getDouble("dHMOTotalFee"))+",");
		 writer.print("HMOTotalNetFee:"+df.format(json.getDouble("dHMOTotalNetFee"))+"");
					
		 writer.close();
	}	
	
	private void processUpload(String[] args) throws Exception {
		JSONObject json = processPayslipInputForUpload(args);	
				
//		System.out.println("json: "+json.toString());

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();

		try {
			HttpPost request = new HttpPost(serverIpAddress+STORE_TRANSACTIONS_LIST_FOR_THE_DAY_UPLOAD);
			StringEntity params = new StringEntity(json.toString(), "UTF-8"); //edited by Mike, 20191012
			request.addHeader("content-type", "application/json; charset=utf-8'"); //edited by Mike, 20191012
			request.setEntity(params);
			httpClient.execute(request);
		} catch (Exception ex) {
			
		} finally {
			httpClient.close();
		}
	}
	
	//added by Mike, 20190814; edited by Mike, 20190815
	//Reference: https://hc.apache.org/httpcomponents-client-4.5.x/httpclient/examples/org/apache/http/examples/client/ClientWithResponseHandler.java; last accessed: 20190814
	private void processDownload(String[] args) throws Exception {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();

		 try {
            HttpGet httpget = new HttpGet(serverIpAddress+GET_TRANSACTIONS_LIST_FOR_THE_DAY_DOWNLOAD);

            System.out.println("Executing request " + httpget.getRequestLine());

            //Create a custom response handler
            ResponseHandler<String> responseHandler = new MyResponseHandler();
			
            String responseBody = httpClient.execute(httpget, responseHandler);
            System.out.println("----------------------------------------");
            System.out.println(responseBody); 
			
			//edited by Mike, 20190820
			if (!responseBody.contains("No payslips")) {
				System.out.println("JSON Array----------------------------------------");			
				processPayslipInputAfterDownload(responseBody);
			}			
        } finally {
            httpClient.close();
        }
	}

	//added by Mike, 20200916
	private String getDateToday() {
//      DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
      DateFormat dateFormat = new SimpleDateFormat("M/d/yyyy");

      Date date = new Date();
  
  		//removed by Mike, 20200917    
//      System.out.println(dateFormat.format(date));	
      
      return dateFormat.format(date);
	}	

	//added by Mike, 20200917
	private String getDateTodayISOFormat() {
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 //     DateFormat dateFormat = new SimpleDateFormat("M/d/yyyy");

      Date date = new Date();
 //     System.out.println(dateFormat.format(date));	
      
      return dateFormat.format(date);
	}	

	
	//added by Mike, 20200916	
	//Note: Consultation and PT Treatment payslip inputs are processed separately
	private JSONObject processPayslipInputForSendSMS(String[] args) throws Exception {
		JSONObject json = new JSONObject();
//		json.put("myKey", "myValue");    

		String sDateToday = getDateToday();

//		System.out.println("sDateToday: " + sDateToday);

		//added by Mike, 20200930
		DecimalFormat df = new DecimalFormat("#.##");

		//added by Mike, 20190812
		int transactionCount = 0; //start from zero

		double dHMOTotalFee = 0.0;
		double dHMOTotalNetFee = 0.0;

		double dCashTotalFee = 0.0;
		double dCashTotalNetFee = 0.0;

		//added by Mike, 20200921
		System.out.println("args.length: "+args.length);

		for (int i=0; i<args.length; i++) {									
			inputFilename = args[i].replaceAll(".txt","");			
			File f = new File(inputFilename+".txt");

			//added by Mike, 20200921
			System.out.println("inputFilename: " + inputFilename);

			//added by Mike, 20200917
			if(!f.exists()) { 
    			System.out.println("File does not exist: "+f.toString());
    			return null;
			}			

			//added by Mike, 20190917
			//note that the default payslip_type_id is 2, i.e. "PT Treatment"
			if (inputFilename.contains("CONSULT")) {
				json.put("payslip_type_id", 1);    				
			}			
			else {
				json.put("payslip_type_id", 2);    				
			}
			
			Scanner sc = new Scanner(new FileInputStream(f));				
		
			String s;		
			
			//edited by Mike, 20191012; removed by Mike, 20200919
			//s=sc.nextLine(); 			
			//s = new String(sc.nextLine().getBytes(), StandardCharsets.UTF_8);

			//removed by Mike, 20200916
			//edited by Mike, 20190917
//			json.put("dateTimeStamp", s.trim());

			//edited by Mike, 20191012
			//s=sc.nextLine();
			//removed by Mike, 20200919
			//s = new String(sc.nextLine().getBytes(), StandardCharsets.UTF_8);

/*		//removed by Mike, 20200916			
			//edited by Mike, 20190917
			json.put("cashierPerson", s.trim().replace("\"",""));    
*/
	
			if (isInDebugMode) {
				rowCount=0;
			}
						
			//count/compute the number-based values of inputColumns 
			while (sc.hasNextLine()) {				
			  //edited by Mike, 20191012
				//s=sc.nextLine();
				
				//noted by Mike, 20200919
				//note: skip table header row				
				s = new String(sc.nextLine().getBytes(), StandardCharsets.UTF_8);

			  System.out.println(s);

				//if the row is blank
				if (s.trim().equals("")) {
					continue;
				}
				
				//added by Mike, 20200916
				if (rowCount==0) { //skip table header row
					rowCount++;
					continue;
				}
				
				String[] inputColumns = s.split("\t");					

				//System.out.println(s);
				//json.put("myKey", "myValue");    

				//removed by Mike, 20200916
/*
				//added by Mike, 20190812; edited by Mike, 20190816
				JSONObject transactionInJSONFormat = new JSONObject();
				transactionInJSONFormat.put(""+INPUT_OR_NUMBER_COLUMN, Integer.parseInt(inputColumns[INPUT_OR_NUMBER_COLUMN]));
				transactionInJSONFormat.put(""+INPUT_PATIENT_NAME_COLUMN, inputColumns[INPUT_PATIENT_NAME_COLUMN].replace("\"",""));
				transactionInJSONFormat.put(""+INPUT_CLASSIFICATION_COLUMN, inputColumns[INPUT_CLASSIFICATION_COLUMN]);
				transactionInJSONFormat.put(""+INPUT_AMOUNT_PAID_COLUMN, inputColumns[INPUT_AMOUNT_PAID_COLUMN]);
				transactionInJSONFormat.put(""+INPUT_NET_PF_COLUMN, inputColumns[INPUT_NET_PF_COLUMN]);
				//edited by Mike, 20190813
				json.put("i"+transactionCount, transactionInJSONFormat);    				
*/

			  
				//added by Mike, 20200916
//				System.out.println(inputColumns[INPUT_WORKBOOK_DATE_COLUMN]);
				if (!inputColumns[INPUT_WORKBOOK_DATE_COLUMN].equals(sDateToday)) {
					rowCount++;
					continue;
				}

				
				if (inputColumns[INPUT_WORKBOOK_CLASSIFICATION_COLUMN].contains("HMO")) {
					//edited by Mike, 20200930
/*					dHMOTotalFee = dHMOTotalFee + Double.parseDouble(inputColumns[INPUT_WORKBOOK_AMOUNT_PAID_COLUMN]);
					dHMOTotalNetFee = dHMOTotalNetFee + Double.parseDouble(inputColumns[INPUT_WORKBOOK_NET_PF_COLUMN]);	
*/
					dHMOTotalFee = dHMOTotalFee + UsbongUtilsRound(Double.parseDouble(inputColumns[INPUT_WORKBOOK_AMOUNT_PAID_COLUMN]),2);
					dHMOTotalNetFee = dHMOTotalNetFee + UsbongUtilsRound(Double.parseDouble(inputColumns[INPUT_WORKBOOK_NET_PF_COLUMN]),2);	
				}
				else {
					//edited by Mike, 20200930
/*					dCashTotalFee = dCashTotalFee + Double.parseDouble(inputColumns[INPUT_WORKBOOK_AMOUNT_PAID_COLUMN]);
					dCashTotalNetFee = dCashTotalNetFee + Double.parseDouble(inputColumns[INPUT_WORKBOOK_NET_PF_COLUMN]);
*/
					dCashTotalFee = dCashTotalFee + UsbongUtilsRound(Double.parseDouble(inputColumns[INPUT_WORKBOOK_AMOUNT_PAID_COLUMN]),2);
					dCashTotalNetFee = dCashTotalNetFee + UsbongUtilsRound(Double.parseDouble(inputColumns[INPUT_WORKBOOK_NET_PF_COLUMN]),2);
				}
	
				transactionCount++;

				if (isInDebugMode) {
					rowCount++;
//					System.out.println("rowCount: "+rowCount);
				}
			}				
		}
		
		//added by Mike, 20200916
		json.put("dHMOTotalFee", dHMOTotalFee);
		json.put("dHMOTotalNetFee", dHMOTotalNetFee);			
		json.put("dCashTotalFee", dCashTotalFee);
		json.put("dCashTotalNetFee", dCashTotalNetFee);
		
		//added by Mike, 20190812; edited by Mike, 20190815
		json.put("iTotal", transactionCount);    				
								
		System.out.println("json: "+json.toString());
		
		return json;
	}
		
	//added by Mike, 20190811; edited by Mike, 20190812
	//Note: Consultation and PT Treatment payslip inputs are processed separately
	private JSONObject processPayslipInputForUpload(String[] args) throws Exception {
		JSONObject json = new JSONObject();
//		json.put("myKey", "myValue");    

		//added by Mike, 20190812
		int transactionCount = 0; //start from zero

		for (int i=0; i<args.length; i++) {									
			inputFilename = args[i].replaceAll(".txt","");			
			File f = new File(inputFilename+".txt");

			//added by Mike, 20190917
			//note that the default payslip_type_id is 2, i.e. "PT Treatment"
			if (inputFilename.contains("CONSULT")) {
				json.put("payslip_type_id", 1);    				
			}			
			else {
				json.put("payslip_type_id", 2);    				
			}
			
			Scanner sc = new Scanner(new FileInputStream(f));				
		
			String s;		
			
			//edited by Mike, 20191012
			//s=sc.nextLine(); 			
			s = new String(sc.nextLine().getBytes(), StandardCharsets.UTF_8);

			//edited by Mike, 20190917
			json.put("dateTimeStamp", s.trim());

			//edited by Mike, 20191012
			//s=sc.nextLine(); 			
			s = new String(sc.nextLine().getBytes(), StandardCharsets.UTF_8);
			
			//edited by Mike, 20190917
			json.put("cashierPerson", s.trim().replace("\"",""));    
	
			if (isInDebugMode) {
				rowCount=0;
			}
						
			//count/compute the number-based values of inputColumns 
			while (sc.hasNextLine()) {				
			    //edited by Mike, 20191012
				//s=sc.nextLine();
				s = new String(sc.nextLine().getBytes(), StandardCharsets.UTF_8);

				//if the row is blank
				if (s.trim().equals("")) {
					continue;
				}
				
				String[] inputColumns = s.split("\t");					

				//System.out.println(s);
				//json.put("myKey", "myValue");    

				//added by Mike, 20190812; edited by Mike, 20190816
				JSONObject transactionInJSONFormat = new JSONObject();
				transactionInJSONFormat.put(""+INPUT_OR_NUMBER_COLUMN, Integer.parseInt(inputColumns[INPUT_OR_NUMBER_COLUMN]));
				transactionInJSONFormat.put(""+INPUT_PATIENT_NAME_COLUMN, inputColumns[INPUT_PATIENT_NAME_COLUMN].replace("\"",""));
				transactionInJSONFormat.put(""+INPUT_CLASSIFICATION_COLUMN, inputColumns[INPUT_CLASSIFICATION_COLUMN]);
				transactionInJSONFormat.put(""+INPUT_AMOUNT_PAID_COLUMN, inputColumns[INPUT_AMOUNT_PAID_COLUMN]);
				transactionInJSONFormat.put(""+INPUT_NET_PF_COLUMN, inputColumns[INPUT_NET_PF_COLUMN]);

				//edited by Mike, 20190813
				json.put("i"+transactionCount, transactionInJSONFormat);    				
				transactionCount++;

				if (isInDebugMode) {
					rowCount++;
					System.out.println("rowCount: "+rowCount);
				}
			}				
		}
		
		//added by Mike, 20190812; edited by Mike, 20190815
		json.put("iTotal", transactionCount);    				
								
		System.out.println("json: "+json.toString());
		
		return json;
	}	
	
	//added by Mike, 20190812; edited by Mike, 20191026
	//Note: Consultation and PT Treatment payslip inputs are automatically identified
	private void processPayslipInputAfterDownload(String s) throws Exception {		
		JSONArray nestedJsonArray = new JSONArray(s);
		
		//edited by Mike, 20190917
		PrintWriter writer = new PrintWriter("output/payslipPTFromCashier.txt", "UTF-8");	
		//PrintWriter writer = new PrintWriter("");
		
		//added by Mike, 20191026
		PrintWriter consultationWriter = new PrintWriter("output/payslipConsultationFromCashier.txt", "UTF-8");	
		
		if (nestedJsonArray != null) {
		   for(int j=0;j<nestedJsonArray.length();j++) {
				JSONObject jo_inside = nestedJsonArray.getJSONObject(j);

/*				//removed by Mike, 20191026				
				//added by Mike, 20190917
				if (jo_inside.getInt("payslip_type_id") == 1) {
					writer = new PrintWriter("output/payslipConsultationFromCashier.txt", "UTF-8");	
				}
*/				
/*				else {
					writer = new PrintWriter("output/payslipPTFromCashier.txt", "UTF-8");	
				}
*/				
				System.out.println(""+jo_inside.getString("payslip_description"));				
				
				JSONObject payslipInJSONFormat = new JSONObject(jo_inside.getString("payslip_description"));

				int totalTransactionCount = payslipInJSONFormat.getInt("iTotal");
				System.out.println("totalTransactionCount: "+totalTransactionCount);
				
				//added by Mike, 20190821
				int count;
				
				for (int i=0; i<totalTransactionCount; i++) {
					JSONArray transactionInJSONArray = payslipInJSONFormat.getJSONArray("i"+i);
					
//					System.out.println(""+transactionInJSONArray.getInt(0)); //Official Receipt Number
//					System.out.println(""+transactionInJSONArray.getString(1)); //Patient Name

					//edited by Mike, 20190821
					count = i+1;
					
					String outputString = 	this.getDate(payslipInJSONFormat.getString("dateTimeStamp")) + "\t" +
							   count + "\t" +
							   transactionInJSONArray.getInt(INPUT_OR_NUMBER_COLUMN) + "\t" +
							   transactionInJSONArray.getString(INPUT_PATIENT_NAME_COLUMN) + "\t" +
							   "\t" + //FEE COLUMN
							   transactionInJSONArray.getString(INPUT_CLASSIFICATION_COLUMN) + "\t" +
							   transactionInJSONArray.getString(INPUT_AMOUNT_PAID_COLUMN) + "\t" +
							   //edited by Mike, 20191010
							   transactionInJSONArray.getString(INPUT_NET_PF_COLUMN) + "\t"; //"\n";

					//added by Mike, 20191010
					outputString = outputString + jo_inside.getString("added_datetime_stamp") + "\t" +
												  payslipInJSONFormat.getString("cashierPerson") + "\n";
			
					//added by Mike, 20191012
//					outputString = outputString.replace("u00d1", "Ñ");

					//edited by Mike, 20191026
					//write in Tab-delimited .txt file
/*					writer.write(outputString);
*/
					if (jo_inside.getInt("payslip_type_id") == 1) {
						consultationWriter.write(outputString);
					}
					else {
						writer.write(outputString);
					}
				}
		   }
		   
		   //added by Mike, 20190817; edited by Mike, 20191026
		   writer.close();
		   consultationWriter.close();
		}
	}
	
	//added by Mike, 20190820
	//input: 2019-08-11T14:12:16
	//output: 08/16/2019
	//note: when the date is imported to MS EXCEL the format becomes the intended 16/08/2019
	private String getDate(String dateTimeStamp) {
		String[] dateStringPart1 = dateTimeStamp.split("T");		
		String[] dateStringPart2 = dateStringPart1[0].split("-");		
		
		return dateStringPart2[1] + "/" + dateStringPart2[2] + "/" + dateStringPart2[0];
	}	

	//added by Mike, 20200930
	//Reference: https://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places;
	//last accessed: 20200930
	//answer by luke, 20101111T0643
	//edited by confile, 20160208T1448
	
	//Use Banker's Rounding due to used by Macro at SLHCC's Cashier Unit 
	//SLHCC ORTHO & PT Unit verifies payslip report from Cashier Unit 
	//only until the second to the last digit;
	//if equal, computer writes the value received from the Cashier Unit
	//Banker's Rounding: if even and digitToVerify <= 5, round down, else round up
	//Examples: 
	//535.716 : 535.71; digitToVerify = 1
	//714.286 : 714.29; digitToVerify = 8
	//Reference: https://stackoverflow.com/questions/44310679/bankers-rounding-formula-in-excel;
	//last accessed: 20201008
	//answer by: Scott Craner, 20170601T15:06
	private double UsbongUtilsRound(double inputValue, int numOfDecimalPlacesAfterDot) {
		//edited by Mike, 20201007
/*		
		int iNum=1; //10
		//edited by Mike, 20201007
		for(int iCount=0; iCount<numOfDecimalPlacesAfterDot; iCount++) {
		//for(int iCount=1; iCount<numOfDecimalPlacesAfterDot; iCount++) {
			iNum=iNum*10;
		}
		
//		inputValue = inputValue*100;
		inputValue = inputValue*iNum;
		inputValue = (double) Math.round(inputValue);				
		inputValue = inputValue/iNum;
*/

		//truncate, last digit: third number right of the dot, i.e. decimal point
		//Example: 535.71675 -> 535.716
		int iNum=10;
		for(int iCount=0; iCount<numOfDecimalPlacesAfterDot; iCount++) {
		//for(int iCount=1; iCount<numOfDecimalPlacesAfterDot; iCount++) {
			iNum=iNum*10;
		}
		inputValue = inputValue*iNum;
		inputValue = (int) inputValue;				
		inputValue = inputValue/(iNum);		
		
		System.out.println("inputValue: " +inputValue);
		
		//added by Mike, 20201008
		//banker's rounding: if even and <= 0.5, round down, else round up
		//Examples: 
		//535.716 : 535.71
		//714.286 : 714.29

		//get number of digits
		int numOfDigits = (""+inputValue).length()-1;//do not include dot
		
//		System.out.println("numOfDigits: " + numOfDigits);

//		int tempInputValue = Integer.parseInt((""+inputValue).substring(numOfDigits-1)); //get the second to the last digit, start at 0, dot not included
		
		int tempInputValue = 0;
		//get the second to the last digit, start at 0, dot not included
		String sLastDigit = (""+inputValue).substring(numOfDigits-1,numOfDigits);
		
		if (sLastDigit.equals(".")) {			
		}
		else {
			tempInputValue = Integer.parseInt(sLastDigit);
		}
		
//		System.out.println("tempInputValue: " + tempInputValue);

		//note: 
		//numOfDecimalPlacesAfterDot : 2 ; iUpdatedNum : 100
		int iUpdatedNum = iNum/10;
		
		if ((tempInputValue%2) == 0) { //even
			if (tempInputValue <= 5) { //0.5) {
				//round down
				//truncate
				inputValue = inputValue*iUpdatedNum;
				inputValue = (int) inputValue;
				inputValue = inputValue/(iUpdatedNum);
			}
			else {
				//round up
				inputValue = (double) Math.round(inputValue*iUpdatedNum)/iUpdatedNum;
			}
		}
		else {
				//round down
				//truncate
				inputValue = inputValue*iUpdatedNum; //100;
				inputValue = (int) inputValue;				
				inputValue = inputValue/(iUpdatedNum);//100);
		}

//		System.out.println("after inputValue: " +inputValue);
		
		return inputValue;
	}	
}

/* //removed by Mike, 20200916
//added by Mike, 20190814; edited by Mike, 20190815
//Create a custom response handler
//Reference: https://hc.apache.org/httpcomponents-client-4.5.x/httpclient/examples/org/apache/http/examples/client/ClientWithResponseHandler.java; last accessed: 20190814
class MyResponseHandler implements ResponseHandler<String> {
	@Override
	public String handleResponse(
			final HttpResponse response) throws ClientProtocolException, IOException {
		int status = response.getStatusLine().getStatusCode();
		if (status >= 200 && status < 300) {
			HttpEntity entity = response.getEntity();
			return entity != null ? EntityUtils.toString(entity) : null;
		} else {
			throw new ClientProtocolException("Unexpected response status: " + status);
		}
	}		
}
*/