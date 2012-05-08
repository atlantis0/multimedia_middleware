package com.multimedia.middleware;

import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MiddlewareMultimediaActivity extends Activity {
	
	Button btnAccessPointMode;
	Button btnClient;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        btnAccessPointMode = (Button)this.findViewById(R.id.btnAccessPointMode);
        btnClient = (Button)this.findViewById(R.id.btnClientMode);
        
        
        btnAccessPointMode.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			
				Intent intent = new Intent(getApplicationContext(), AccessPointActivity.class);
				startActivity(intent);
				finish();
				
				
			}
		});
        
        btnClient.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), ClientActivity.class);
				startActivity(intent);
				finish();
				
			}
		});
        
        String sClassName = "com.android.settings.vpn.KeyStore"; 
        
        try
        {
        	Class classToInvestigate = Class.forName(sClassName); 
        	
        	Method[] aClassMethods = classToInvestigate.getDeclaredMethods();  
        	
        	for(Method m : aClassMethods)  
        	{  
        	    Log.d("better",m.toString());
        	}  
        	
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        
        
    }
}