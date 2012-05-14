package com.multimedia.middleware.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

public class MiddlewareUtil {

	public static final String username = "mine";
	public static final String password = "mineminemine";
	public static final String KNOWN_HOST = "192.168.43.1";
	
	public static ArrayList<String> getIPAddress()
	{   
		ArrayList<String> address_list = new ArrayList<String>();
        
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
            	
            	if(line.startsWith("192"))
            	{
            		//there exists an ip address
            	    String [] deviceNetworkInfo = line.split(" ");
            	    String necessaryInfo [] = new String [6];
            	    int j=0;
            	    for(int i=0; i<deviceNetworkInfo.length; i++)
            	    {
            	    	if(deviceNetworkInfo[i].length()!=0)
            	    	{
            	    		necessaryInfo[j] = deviceNetworkInfo[i];
            	    		j++;
            	    	}
            	    }
            	    
            	    address_list.add(necessaryInfo[0]);
            	   
            	}
         
            }
        } 
        catch (Exception e) {
            e.printStackTrace();
        } 
        finally {
            try {
                br.close();
            } 
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        //return the value
        return address_list;
	}
	
	@SuppressWarnings("static-access")
	public static void createWifiAccessPoint(Context context, String ntId, String password) {
	    	try
	    	{
	    		WifiManager wifiManager = (WifiManager)context.getSystemService(context.WIFI_SERVICE);
	    		
	    		 if(wifiManager.isWifiEnabled())
	    	        {
	    	            wifiManager.setWifiEnabled(false);          
	    	        }       
	    	        
	    	        Method[] wmMethods = wifiManager.getClass().getDeclaredMethods();   //Get all declared methods in WifiManager class     
	    	        for(Method method: wmMethods){
	    	            if(method.getName().equals("setWifiApEnabled")){
	    	                WifiConfiguration netConfig = new WifiConfiguration();
	    	                netConfig.SSID = ntId;
	    	                netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
	    	                netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
	    	                netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
	    	                netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
	    	                netConfig.preSharedKey = password;
	    	                netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
	    	                netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
	    	                netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
	    	                netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
	    	                try {
	    	                    boolean apstatus=(Boolean) method.invoke(wifiManager, netConfig,true);          
	    	                    
	    	                    for (Method isWifiApEnabledmethod: wmMethods)
	    	                    {
	    	                        if(isWifiApEnabledmethod.getName().equals("isWifiApEnabled")){
	    	                            while(!(Boolean)isWifiApEnabledmethod.invoke(wifiManager)){
	    	                            };
	    	                            for(Method method1: wmMethods){
	    	                                if(method1.getName().equals("getWifiApState")){
	    	                                    @SuppressWarnings("unused")
												int apstate;
	    	                                    apstate=(Integer)method1.invoke(wifiManager);
	    	                                }
	    	                            }
	    	                        }
	    	                    }
	    	                    if(apstatus)
	    	                    {
	    	                        Log.d("better", "Access Point Created");  
	    	                    }
	    	                    else
	    	                    {
	    	                    	Log.d("better", "Failed to create Access Point!");  
	    	                    }

	    	                } catch (IllegalArgumentException e) {
	    	                    e.printStackTrace();
	    	                } catch (IllegalAccessException e) {
	    	                    e.printStackTrace();
	    	                } catch (InvocationTargetException e) {
	    	                    e.printStackTrace();
	    	                }
	    	            }      
	    	        }
	    	}
	    	catch(Exception e)
	    	{
	    		e.printStackTrace();
	    	}
	       
	    }
	
	@SuppressWarnings("static-access")
	public static boolean connectToNetwork(Context context, String username, String password)
	{
   	 	boolean status = false;
   	 	try
   	 	{
   	 		WifiManager wifi = (WifiManager)context.getSystemService(context.WIFI_SERVICE);
		 
   	 		if(!wifi.isWifiEnabled())
   	 		{
   	 			wifi.setWifiEnabled(true);
   	 			Thread.sleep(3000);
   	 		}
			
   	 		WifiConfiguration netConfig = new WifiConfiguration();
   	 		netConfig.SSID = "\"" + username + "\"";
   	 		netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
   	 		netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
   	 		netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
   	 		netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
   	 		netConfig.preSharedKey = "\"" + password + "\"";
   	 		netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
   	 		netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
   	 		netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
   	 		netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
       
   	 		int netId = wifi.addNetwork(netConfig);
   	 		status  = wifi.enableNetwork(netId, true);
   	 	}
   	 	catch (Exception e) {
   	 		status = false;
			e.printStackTrace();
		}
   	 	
		return status;
	 }
	
	 @SuppressWarnings("static-access")
	public static boolean disableAP(Context context, String ntId, String password) throws Exception
	 {
		 boolean apstatus = false;
		 
		 WifiManager wifiManager = (WifiManager)context.getSystemService(context.WIFI_SERVICE);
		 
		 Method[] wmMethods = wifiManager.getClass().getDeclaredMethods();   //Get all declared methods in WifiManager class     
	        for(Method method: wmMethods){
	            if(method.getName().equals("setWifiApEnabled")){
	            
	                WifiConfiguration netConfig = new WifiConfiguration();
	                netConfig.SSID = ntId;
	                netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
	                netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
	                netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
	                netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
	                netConfig.preSharedKey = password;
	                netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
	                netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
	                netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
	                netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
	            	
	                apstatus=(Boolean)method.invoke(wifiManager, netConfig,false);    
	            }
	        }
	        
	     return apstatus;
	 }
}
