package com.appdynamics.jrbronet.projectplan;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

// Program arguments: <controller_URL> <user>@<account> <password> <input_XLS> <output_XLS>
// Program arguments example: https://aviva1.saas.appdynamics.com josebronet@aviva1 myPassword <> <> output.xls
// Program arguments example: https://aviva1.saas.appdynamics.com josebronet@aviva1 myPAss Gi-GW-PRE "18-09-2014 10:00" "18-09-2014 10:30" result.xls


public class SnapshotsDDRMain {

	public static void main(String[] args) {
		
		try{
			System.out.println("Snapshots Deep Analysis by Jose R Bronet");
			if(args.length == 0){
				System.out.println("Usage: java - jar SnaphotsDeepCPUAnalysis.jar <controller_URL> <user@account> <password> Application \"<Strat Date Time>\" \"<End Date Time>\" <output_XLS>");
				System.out.println("Example:java -jar SnaphotsDeepCPUAnalysis.jar http://appdynamics.local:8090 josebronet@customer1 myPassword Gi-GW-PRE \"30-12-2014 10:00\" \"30-12-2014 12:00\" result.xls");
				System.exit(0);
			}			
			AppDynamicsControllerRESTClient a = new AppDynamicsControllerRESTClient();
			System.out.print("Getting application metadata");
			a.builMaps(args[0], args[1], args[2],args[3]);
			System.out.println("");
			System.out.print("Getting snapshots data ");
			a.getAndBuildSnpashotsListReport(args[0],args[3], args[4],args[5],args[1],args[2],args[6]);
			System.out.println("");
			System.out.println("Done!!!");
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	

}
