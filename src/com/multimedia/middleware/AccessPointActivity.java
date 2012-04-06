package com.multimedia.middleware;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.middleware.listeners.AddressTable;
import com.middleware.model.AccessPoint;
import com.middleware.model.DataReceived;
import com.middleware.model.Node;
import com.middleware.model.NodeState;
import com.multimedia.middleware.util.MiddlewareUtil;

public class AccessPointActivity extends Activity implements DataReceived, AddressTable{

	//UI Elements
	Button btnCreateTemporaryAccessPoint;
	Button btnChoosePermanet;
	
	AccessPoint accessPoint;
	NodeState state;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.access);
        
		state = new NodeState("10", "1.2", "10");
		state.setStatus(true);
		
        btnCreateTemporaryAccessPoint = (Button)this.findViewById(R.id.btnCreateTemporaryAccessPoint);
        btnChoosePermanet = (Button)this.findViewById(R.id.btnChoosePermanet);
        
        btnCreateTemporaryAccessPoint.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				try
				{
					MiddlewareUtil.createWifiAccessPoint(getApplicationContext(), "sirack", "betterconnect");
					accessPoint = new AccessPoint(state, 4444);
					setListener();
					accessPoint.startReceiverThread();
				}
				catch(Exception e)
				{
					Log.d("better", "Access Point is not created!");
					e.printStackTrace();
				}
			}
		});
        
        btnChoosePermanet.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try
				{
					boolean change = accessPoint.choosePermanetAccessPoint();
					if(change)
						Log.d("better", "Access Point Changed!");
					else
						Log.d("better", "Access Point Remains");
				}
				catch(Exception e)
				{
					e.printStackTrace();
					Log.d("better", "Unable to choose permanet access point");
				}
			}
		});
        
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
    	
    }
    
    public void setListener()
    {
    	accessPoint.setDataReceived(this);
    	accessPoint.setAddressTable(this);
    }

	@Override
	public void nodeReceivedData(byte[] data) {
		
		String receivedString = new String(data);
		Log.d("better", receivedString);
		
	}

	@Override
	public void nodeAdded(Node node) {
		
		Log.d("better", "Node Added!");
		Log.d("better", node.getAddress().toString());
		
	}

	@Override
	public void nodeRemoved(Node node) {
		
		Log.d("better", "Node Removed!");
		Log.d("better", node.getAddress().toString());
		
	}
}
