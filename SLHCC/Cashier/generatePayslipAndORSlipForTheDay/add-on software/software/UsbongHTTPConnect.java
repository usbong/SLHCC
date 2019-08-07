/*
  Copyright 2019 Usbong Social Systems, Inc.

  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You ' may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, ' WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing ' permissions and limitations under the License.

  @author: Michael Syson
  @date created: 20190807
  @date updated: 20190807

  Given:
  1) List with the details of the transactions for the day

  Output:
  1) Automatically connect to the database (DB) and send the details of the transactions to the computer server to store them in the DB
  
  Note:
  1) The details of the transactions to be sent are in the JSON (JavaScript Object Notation) format.
  
  Reference:
  1) Introducing JSON. https://www.json.org/; last accessed: 20190807
  --> ECMA-404 The JSON Data Interchange Standard  
  2) https://stackoverflow.com/questions/7181534/http-post-using-json-in-java; last accessed: 20190807
  --> answer by: Cigano Morrison Mendez on 20131111; edited on 20140819
*/

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

	public static void main(String[] args) {
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