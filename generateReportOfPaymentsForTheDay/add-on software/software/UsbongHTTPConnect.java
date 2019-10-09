/*
  Copyright 2019 Usbong Social Systems, Inc.

  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You ' may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, ' WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing ' permissions and limitations under the License.

  @author: Michael Syson
  @date created: 20190807
  @date updated: 20191010

  Given:
  1) List with the details of the transactions for the day

  Output:
  1) Automatically connect to the database (DB) and send the details of the transactions to the computer server to store them in the DB
  
  Notes:
  1) The details of the transactions to be sent are in the JSON (JavaScript Object Notation) format.
    
  2) To compile on Windows' Command Prompt the add-on software with the libraries, e.g. JSON .jar file, use the following command:
   javac -cp .;org.json.jar;org.apache.httpclient.jar;org.apache.httpcore.jar;org.apache.commons-logging.jar UsbongHTTPConnect.java

  3) To execute on Windows' Command Prompt the add-on software with the JSON .jar file, i.e. json, use the following command:
   java -cp .;org.json.jar;org.apache.httpclient.jar;org.apache.httpcore.jar;org.apache.commons-logging.jar UsbongHTTPConnect

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

import java.io.PrintWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class UsbongHTTPConnect {
	//added by Mike, 20190811
	private static boolean isInDebugMode = true;

	//added by Mike, 20190814; edited by Mike, 20190918
	private static boolean isForUpload = false;

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

		UsbongHTTPConnect main = new UsbongHTTPConnect();

		//added by Mike, 20190918
		serverIpAddress = args[0];

		//edited by Mike, 20190918		
		if (isForUpload) {
			main.processUpload(new String[]{args[1]});
		}
		else {
			main.processDownload(new String[]{args[1]});
		}
	}
	
	private void processUpload(String[] args) throws Exception {
		JSONObject json = processPayslipInputForUpload(args);	
				
//		System.out.println("json: "+json.toString());

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();

		try {
			HttpPost request = new HttpPost(serverIpAddress+STORE_TRANSACTIONS_LIST_FOR_THE_DAY_UPLOAD);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
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
			
			s=sc.nextLine(); 			
			//edited by Mike, 20190917
			json.put("dateTimeStamp", s.trim());    

			s=sc.nextLine(); 
			//edited by Mike, 20190917
			json.put("cashierPerson", s.trim().replace("\"",""));    
	
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
								
//		System.out.println("json: "+json.toString());
		
		return json;
	}	
	
	//added by Mike, 20190812; edited by Mike, 20191010
	//Note: Consultation and PT Treatment payslip inputs are processed separately
	private void processPayslipInputAfterDownload(String s) throws Exception {		
		JSONArray nestedJsonArray = new JSONArray(s);
		
		//edited by Mike, 20190917
		PrintWriter writer = new PrintWriter("output/payslipPTFromCashier.txt", "UTF-8");	
		//PrintWriter writer = new PrintWriter("");
		
		if (nestedJsonArray != null) {
		   for(int j=0;j<nestedJsonArray.length();j++) {
				JSONObject jo_inside = nestedJsonArray.getJSONObject(j);

				//added by Mike, 20190917
				if (jo_inside.getInt("payslip_type_id") == 1) {
					writer = new PrintWriter("output/payslipConsultationFromCashier.txt", "UTF-8");	
				}
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
			
					//write in Tab-delimited .txt file
					writer.write(outputString);
				}
		   }
		   
		   //added by Mike, 20190817
		   writer.close();
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
}

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