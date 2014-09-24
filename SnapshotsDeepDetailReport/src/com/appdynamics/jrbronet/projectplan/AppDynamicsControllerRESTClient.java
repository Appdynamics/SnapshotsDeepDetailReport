package com.appdynamics.jrbronet.projectplan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;

import com.appdynamics.jrbronet.projectplan.*;


public class AppDynamicsControllerRESTClient {
		
		private HashMap BTsMap = new HashMap();
		private HashMap tiersMap = new HashMap();
		private HashMap nodesMap = new HashMap();	
		public String AppID = new String(); 
		
		public void getAndBuildSnpashotsListReport(String HostURL, String application, String start, String end, String name, String password, String outputFile){
			// https://aviva1.saas.appdynamics.com/controller/rest/applications/Gi-GW-PRE/request-snapshots?time-range-type=BETWEEN_TIMES&start-time=1410187200000&end-time=1410188400000
			try{
				
				Date startDate = new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(start);
				Date endDate = new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(end);
									
				String RESTReqURL = HostURL+"/controller/rest/applications/"+URLEncoder.encode(application, "UTF-8")+"/request-snapshots?time-range-type=BETWEEN_TIMES&start-time="+startDate.getTime()+"&end-time="+endDate.getTime();
								
				//System.out.println(RESTReqURL);
				
				String authString = name + ":" + password;
				//System.out.println("auth string: " + authString);
				byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
				String authStringEnc = new String(authEncBytes);
				//System.out.println("Base64 encoded auth string: " + authStringEnc);	
				
				URL url = new URL(RESTReqURL);
				URLConnection urlConnection = url.openConnection();
				urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
				InputStream is = urlConnection.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				
				int numCharsRead;
				char[] charArray = new char[1024];
				StringBuffer sb = new StringBuffer();
				while ((numCharsRead = isr.read(charArray)) > 0) {
					System.out.print(".");		
					sb.append(charArray, 0, numCharsRead);
				}
				String result = sb.toString();
				
				HSSFWorkbook wb = new HSSFWorkbook();
			    FileOutputStream fileOut = new FileOutputStream(outputFile);
			    wb.write(fileOut);
			    fileOut.close();
				
				FileInputStream file = new FileInputStream(new File(outputFile));	             
				//Get the workbook instance for XLS file 
				HSSFWorkbook workbook = new HSSFWorkbook(file);				 
				//Get first sheet from the workbook
				HSSFSheet sheet = workbook.createSheet("Jose's Report");
				int rowIdx = 0;				
				HSSFRow row = sheet.createRow(rowIdx++);				 				
				HSSFCell cell = row.createCell(0);
				cell.setCellValue("GUID");
				cell = row.createCell(1);				
				cell.setCellValue("Business Transaction");
				cell = row.createCell(2);
				cell.setCellValue("Application");
				cell = row.createCell(3);
				cell.setCellValue("Tier");
				cell = row.createCell(4);
				cell.setCellValue("Node");
				cell = row.createCell(5);
				cell.setCellValue("timeTakenInMilliSecs");
				cell = row.createCell(6);
				cell.setCellValue("cpuTimeTakenInMilliSecs");
				cell = row.createCell(7);
				cell.setCellValue("Tx CPU %");
				cell = row.createCell(8);
				cell.setCellValue("Thread Name");
				cell = row.createCell(9);
				cell.setCellValue("Local Start Time");
				cell = row.createCell(10);
				cell.setCellValue("Async");
				cell = row.createCell(11);
				cell.setCellValue("URL");
				cell = row.createCell(12);
				cell.setCellValue("HTTP Session ID");				
				cell = row.createCell(13);
				cell.setCellValue("Snapshot URL");
															    
				int idx1=0,idx2=0;
				for(idx1 = result.indexOf("<request-segment-data>", idx2);idx1!=-1;idx1 = result.indexOf("<request-segment-data>", idx2)){
					if(idx1 != -1){
						row = sheet.createRow(rowIdx++);	
						idx2 = result.indexOf("</request-segment-data>", idx1);
						//System.out.println("idx1:"+idx1+" idx2:"+idx2);									
						String snapshotString = result.substring(idx1+22, idx2);	
						snapshotString = snapshotString.replaceAll("<applicationId>.*</applicationId>", "<appName>"+application+"</appName>");
						
						Pattern pattern = Pattern.compile("<applicationComponentNodeId>(.+?)</applicationComponentNodeId>");
						Matcher matcher = pattern.matcher(snapshotString);
						matcher.find();
						String nodeID = matcher.group(1);
						snapshotString = snapshotString.replaceAll("<applicationComponentNodeId>.*</applicationComponentNodeId>", "<applicationComponentNode>"+this.nodesMap.get(nodeID)+"</applicationComponentNode>");
						//System.out.println("NodeID extracted: "+nodeID);
						
						pattern = Pattern.compile("<applicationComponentId>(.+?)</applicationComponentId>");
						matcher = pattern.matcher(snapshotString);
						matcher.find();
						String tierID = matcher.group(1);
						snapshotString = snapshotString.replaceAll("<applicationComponentId>.*</applicationComponentId>", "<applicationComponent>"+this.tiersMap.get(tierID)+"</applicationComponent>");
						//System.out.println("tierID extracted: "+nodeID);
						
						pattern = Pattern.compile("<businessTransactionId>(.+?)</businessTransactionId>");
						matcher = pattern.matcher(snapshotString);
						matcher.find();
						String BTID = matcher.group(1);
						snapshotString = snapshotString.replaceAll("<businessTransactionId>.*</businessTransactionId>", "<businessTransaction>"+this.BTsMap.get(BTID)+"</businessTransaction>");
						//System.out.println("BTID extracted: "+BTID+"converted: "+);
																		
						pattern = Pattern.compile("<requestGUID>(.+?)</requestGUID>");
						matcher = pattern.matcher(snapshotString);
						matcher.find();
						String GUID = matcher.group(1);
						
						pattern = Pattern.compile("<URL>(.+?)</URL>");
						matcher = pattern.matcher(snapshotString);
						String URL = "";
						if (matcher.find())
							URL = matcher.group(1);
						
						pattern = Pattern.compile("<httpSessionID>(.+?)</httpSessionID>");
						matcher = pattern.matcher(snapshotString);
						String httpSessionID = "";
						if (matcher.find())
							httpSessionID = matcher.group(1);
						
						pattern = Pattern.compile("<timeTakenInMilliSecs>(.+?)</timeTakenInMilliSecs>");
						matcher = pattern.matcher(snapshotString);
						matcher.find();
						String timeTakenInMilliSecs = matcher.group(1);
						
						pattern = Pattern.compile("<cpuTimeTakenInMilliSecs>(.+?)</cpuTimeTakenInMilliSecs>");
						matcher = pattern.matcher(snapshotString);
						matcher.find();
						String cpuTimeTakenInMilliSecs = matcher.group(1);
						
						pattern = Pattern.compile("<threadName>(.+?)</threadName>");
						matcher = pattern.matcher(snapshotString);
						matcher.find();
						String threadName = matcher.group(1);
						
						pattern = Pattern.compile("<localStartTime>(.+?)</localStartTime>");
						matcher = pattern.matcher(snapshotString);
						matcher.find();
						String localStartTime = matcher.group(1);
						
						pattern = Pattern.compile("<async>(.+?)</async>");
						matcher = pattern.matcher(snapshotString);
						matcher.find();
						String async = matcher.group(1);
						
						pattern = Pattern.compile("<serverStartTime>(.+?)</serverStartTime>");
						matcher = pattern.matcher(snapshotString);
						matcher.find();
						String serverStartTimeStr = matcher.group(1);
						Long serverStartTime= new Long(serverStartTimeStr);
													
						// https://aviva1.saas.appdynamics.com/controller/#/location=APP_SNAPSHOT_VIEWER&timeRange=Custom_Time_Range.BETWEEN_TIMES.1411314044461.1411306844461.0&application=23&rsdTime=Custom_Time_Range.BETWEEN_TIMES.1411314639394.1411312839394.0&requestGUID=642f5fef-eef2-44ee-b80b-017acce2df0b
						// ID Appliucation. this.AppID
						// startDate.getTime()
						// endDate.getTime()
						String SnapURL = HostURL+"/controller/#/location=APP_SNAPSHOT_VIEWER&timeRange=Custom_Time_Range.BETWEEN_TIMES."+(serverStartTime+1000000)+"."+(serverStartTime-1000000)+".0&application="+this.AppID+"&rsdTime=Custom_Time_Range.BETWEEN_TIMES."+(serverStartTime+10000)+"."+(serverStartTime-1000000)+".0&requestGUID="+GUID;				
						//System.out.println();
						//System.out.println(SnapURL);
						//System.out.println();
												
						cell = row.createCell(0);
						cell.setCellValue(GUID);												
						
						cell = row.createCell(1);
						cell.setCellValue((String)this.BTsMap.get(BTID));
						cell = row.createCell(2);
						cell.setCellValue(application);
						cell = row.createCell(3);
						cell.setCellValue((String)this.tiersMap.get(tierID));
						cell = row.createCell(4);
						cell.setCellValue((String)this.nodesMap.get(nodeID));
						cell = row.createCell(5);
						
						//Warning cpu is not in millis is in nanos!!!! hay que dividirlo entre un millon
						long foo1 = Long.parseLong(timeTakenInMilliSecs);				
						long foo = Long.parseLong(cpuTimeTakenInMilliSecs)/1000000;						
						cell.setCellValue(foo1);
						cell = row.createCell(6);
						if(foo1>foo)
							cell.setCellValue(foo);
						else
							cell.setCellValue(foo1);
						
						cell = row.createCell(7);
						if((foo1==-1)||(foo==-1))
							cell.setCellValue(-1);
						else
							if(foo == 0)
								cell.setCellValue(0);
							else
								cell.setCellFormula("G"+rowIdx+"/F"+rowIdx+"*100");						
						
						cell = row.createCell(8);
						cell.setCellValue(threadName);						
						String S = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Long.parseLong(localStartTime));
						cell = row.createCell(9);
						cell.setCellValue(S);						
						cell = row.createCell(10);
						cell.setCellValue(async);						
						cell = row.createCell(11);
						cell.setCellValue(URL);
						cell = row.createCell(12);
						cell.setCellValue(httpSessionID);
						cell = row.createCell(13);
						cell.setCellValue(SnapURL);

						
						System.out.print(".");						
						//System.out.println("Snap: "+snapshotString);				
					}
					
				}				
				
				file.close();
			    FileOutputStream out = 
			        new FileOutputStream(new File(outputFile));
			    workbook.write(out);
			    out.close();
			    
				
				//System.out.println(result);
				
			}catch(Exception e){
				e.printStackTrace();
			}
			
		}
		
		public void getAppID(String HostURL, String name, String password, String App)  {
			String appID = "-1";
			try {
				String AppsURL = HostURL+"/controller/rest/applications/";
				String authString = name + ":" + password;
				////System.out.println("auth string: " + authString);
				byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
				String authStringEnc = new String(authEncBytes);
				////System.out.println("Base64 encoded auth string: " + authStringEnc);				
				
				URL url = new URL(AppsURL);
				URLConnection urlConnection = url.openConnection();
				urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
				InputStream is = urlConnection.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
	
				int numCharsRead;
				char[] charArray = new char[1024];
				StringBuffer sb = new StringBuffer();
				while ((numCharsRead = isr.read(charArray)) > 0) {
					sb.append(charArray, 0, numCharsRead);
				}
				String result = sb.toString();		
				
				int idx1=0,idx2=0;
				//System.out.println("<name>"+App+"</name>");
				idx1 = result.toLowerCase().indexOf("<name>"+App.toLowerCase()+"</name>");	
				//System.out.println(idx1);
			
				String substr = result.substring(0, idx1);
				idx1 = substr.lastIndexOf("<id>");
				idx2 = substr.lastIndexOf("</id>");
				
				//System.out.println(substr);
				//System.out.println(idx1);
				//System.out.println(idx2);
				
				this.AppID = new String(substr.substring(idx1+4, idx2));
				
				//System.out.println(App+" -> "+this.AppID);
				
				
			} catch (Exception e) {
				e.printStackTrace();
			} 
			
			//this.AppID = appID;
			
		}
		
		public void getAppTiersNodes(String HostURL, String name, String password, String IDApp, String IDTier)  {
			try {
				String AppsURL = HostURL+"/controller/rest/applications/"+IDApp+"/tiers/"+IDTier+"/nodes";
				////System.out.println("URL: " + AppsURL);
				
				String authString = name + ":" + password;
				////System.out.println("auth string: " + authString);
				byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
				String authStringEnc = new String(authEncBytes);
				////System.out.println("Base64 encoded auth string: " + authStringEnc);				
				
				URL url = new URL(AppsURL);
				URLConnection urlConnection = url.openConnection();
				urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
				InputStream is = urlConnection.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);

				int numCharsRead;
				char[] charArray = new char[1024];
				StringBuffer sb = new StringBuffer();
				while ((numCharsRead = isr.read(charArray)) > 0) {
					sb.append(charArray, 0, numCharsRead);
				}
				String result = sb.toString();
							
				int idx1=0,idx2=0;
				for(idx1 = result.indexOf("<id>", idx2);idx1!=-1;idx1 = result.indexOf("<id>", idx2)){	
					if(idx1 != -1){
						idx2 = result.indexOf("</id>", idx1);
						String IDx = result.substring(idx1+4, idx2);
						idx1 = result.indexOf("<name>", idx2);
						idx2 = result.indexOf("</name>", idx1);
						String namex = result.substring(idx1+6, idx2);						
						//System.out.println("Node-> "+IDx+":"+namex);						
						nodesMap.put(IDx, namex);						
						System.out.print(".");
					}
				}		
				//System.out.println("*** BEGIN ***");
				//System.out.println(result);
				//System.out.println("*** END ***");								
				
			} catch (Exception e) {
				e.printStackTrace();
			} 
	 }
		
		
		 public void getAppTiers(String HostURL, String name, String password, String ID)  {
				try {
					String AppsURL = HostURL+"/controller/rest/applications/"+ID+"/tiers";
					////System.out.println("URL: " + AppsURL);
					
					String authString = name + ":" + password;
					////System.out.println("auth string: " + authString);
					byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
					String authStringEnc = new String(authEncBytes);
					////System.out.println("Base64 encoded auth string: " + authStringEnc);				
					
					URL url = new URL(AppsURL);
					URLConnection urlConnection = url.openConnection();
					urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
					InputStream is = urlConnection.getInputStream();
					InputStreamReader isr = new InputStreamReader(is);

					int numCharsRead;
					char[] charArray = new char[1024];
					StringBuffer sb = new StringBuffer();
					while ((numCharsRead = isr.read(charArray)) > 0) {
						sb.append(charArray, 0, numCharsRead);
					}
					String result = sb.toString();
								
					int idx1=0,idx2=0;
					for(idx1 = result.indexOf("<id>", idx2);idx1!=-1;idx1 = result.indexOf("<id>", idx2)){	
						if(idx1 != -1){
							idx2 = result.indexOf("</id>", idx1);
							String IDx = result.substring(idx1+4, idx2);
							idx1 = result.indexOf("<name>", idx2);
							idx2 = result.indexOf("</name>", idx1);
							String namex = result.substring(idx1+6, idx2);						
							//System.out.println("Tier-> "+IDx+":"+namex);					
							tiersMap.put(IDx, namex);
							System.out.print(".");
							getAppTiersNodes(HostURL, name, password, ID, IDx);
						}
					}		
					//System.out.println("*** BEGIN ***");
					//System.out.println(result);
					//System.out.println("*** END ***");								
					
				} catch (Exception e) {
					e.printStackTrace();
				} 
		 }
		 
		 public void getAppBTs(String HostURL, String name, String password, String ID)  {
				try {
					String AppsURL = HostURL+"/controller/rest/applications/"+ID+"/business-transactions";
					////System.out.println("URL: " + AppsURL);
					
					String authString = name + ":" + password;
					////System.out.println("auth string: " + authString);
					byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
					String authStringEnc = new String(authEncBytes);
					////System.out.println("Base64 encoded auth string: " + authStringEnc);				
					
					URL url = new URL(AppsURL);
					URLConnection urlConnection = url.openConnection();
					urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
					InputStream is = urlConnection.getInputStream();
					InputStreamReader isr = new InputStreamReader(is);

					int numCharsRead;
					char[] charArray = new char[1024];
					StringBuffer sb = new StringBuffer();
					while ((numCharsRead = isr.read(charArray)) > 0) {
						sb.append(charArray, 0, numCharsRead);
					}
					String result = sb.toString();
								
					int idx1=0,idx2=0;
					for(idx1 = result.indexOf("<id>", idx2);idx1!=-1;idx1 = result.indexOf("<id>", idx2)){	
						if(idx1 != -1){
							idx2 = result.indexOf("</id>", idx1);
							String IDx = result.substring(idx1+4, idx2);
							idx1 = result.indexOf("<name>", idx2);
							idx2 = result.indexOf("</name>", idx1);
							String namex = result.substring(idx1+6, idx2);						
							//System.out.println("BT-> "+IDx+":"+namex);					
							BTsMap.put(IDx, namex);	
							System.out.print(".");
						}
					}		
					//System.out.println("*** BEGIN ***");
					//System.out.println(result);
					//System.out.println("*** END ***");								
					
				} catch (Exception e) {
					e.printStackTrace();
				} 
		 }
	
	    public void builMaps(String HostURL, String name, String password, String appName)  {
	    	// /controller/rest/applications/LIFE-ECOMM-SYS/nodes
	    	// /controller/rest/applications/
	    	// Tiers https://aviva1.saas.appdynamics.com/controller/rest/applications/6/tiers
	    	// Nodes https://aviva1.saas.appdynamics.com/controller/rest/applications/6/tiers/61/nodes
	    	try {	    	
	    		getAppID(HostURL, name, password, URLEncoder.encode(appName, "UTF-8"));
	    		getAppTiers(HostURL, name, password, URLEncoder.encode(appName, "UTF-8"));
				getAppBTs(HostURL, name, password, URLEncoder.encode(appName, "UTF-8"));											
			} catch (Exception e) {
				e.printStackTrace();
			} 
	    }
	    
	  
	}
	  

