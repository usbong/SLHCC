package utils;
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

public class UsbongUtils {
	//added by Mike, 20190622
	public void copyContentsOfSourceFileToDestinationFile(String sourceFileName, String destinationFileName) throws Exception {
		File f = new File(sourceFileName);
		
		PrintWriter writer = new PrintWriter(destinationFileName, "UTF-8");	
		
		Scanner sc = new Scanner(new FileInputStream(f), "UTF-8");				
	
		String s;		
		//count/compute the number-based values of inputColumns 
		while (sc.hasNextLine()) {
			s=sc.nextLine();
System.out.println(">>>>> "+s);			
			s.concat(s);
			
			writer.print(s+"\n");
		}
		
		writer.close();
	}
}