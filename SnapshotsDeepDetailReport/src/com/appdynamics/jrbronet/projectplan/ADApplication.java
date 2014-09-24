package com.appdynamics.jrbronet.projectplan;

import java.util.HashMap;

public class ADApplication {
	//private int Java,Net,PHP;
	private String name;
	private HashMap licenses;

	public ADApplication(String appName) {
		//Java=0;
		//Net=0;
		//PHP=0;
		licenses = new HashMap ();
		this.name=appName;
		System.out.println("New app read: "+appName);
	}		
	
	public String getName(){
		return name;
	}
	
	public HashMap getLicenseCount(){
		return licenses;
	}
	
	public void increaseLicenseCount(String type){
		if (licenses.containsKey(type))
		{
			Integer lc = (Integer)this.licenses.get(type);
			licenses.remove(type);
			licenses.put(type, new Integer(1+lc));
		}else{			
			licenses.put(type, new Integer(1));
		}
	}		
	
	
}
