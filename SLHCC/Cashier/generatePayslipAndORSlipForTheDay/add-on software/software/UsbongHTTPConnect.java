/*
  Copyright 2019 Usbong Social Systems, Inc.

  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You ' may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, ' WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing ' permissions and limitations under the License.

  @author: Michael Syson
  @date created: 20190807
  @date updated: 20190810

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

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class UsbongHTTPConnect {

	private static String TAG = "usbong.HTTPConnect.storeTransactionsListForTheDay";
/*
	private String filePath = "";
	private String columnName = "";
	private String action = "";
*/	
	private URL url;
	private HttpURLConnection conn;

	public static void main(String[] args) throws IOException {
		JSONObject json = new JSONObject();
		json.put("myKey", "myValue");    

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();

		try {
			HttpPost request = new HttpPost("http://localhost/usbong_kms/server/storetransactionslistfortheday.php");
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			httpClient.execute(request);
		} catch (Exception ex) {
			
		} finally {
			httpClient.close();
		}
	}
}