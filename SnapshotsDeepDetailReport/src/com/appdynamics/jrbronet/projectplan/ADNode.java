package com.appdynamics.jrbronet.projectplan;

public class ADNode {
		private String name;
		private String type;
		private String hostName;
		private String appName;
		private String tierName;
		
		public ADNode(String name, String type, String hostName, String appName, String tierName){
			this.hostName = hostName;
			this.type = type;
			this.appName = appName;
			this.name = name;
			this.tierName = tierName;					
			System.out.println("New node with activity detected: "+type+", "+hostName+", "+appName+", "+tierName+", "+name);
		}
		
		public String getType(){
			return type;
		}
		
		public String getHostName(){
			return hostName;
		}
		
		public String getAppName(){
			return appName;
		}

		
}
