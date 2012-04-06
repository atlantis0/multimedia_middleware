package com.multimedia.middleware;

import java.net.InetAddress;
import java.util.Random;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.middleware.model.Constants;
import com.middleware.model.DataReceived;
import com.middleware.model.MiddlewarePacket;
import com.middleware.model.Node;
import com.middleware.model.NodeState;
import com.multimedia.middleware.util.MiddlewareUtil;

public class ClientActivity extends Activity implements DataReceived {
	
	NodeState state;
	Node node;
	
	//UI elements
	Button btnSendConnectionProfile;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client);
        
		state = new NodeState("10", "1.2", "10");
		state.setStatus(true);
		
        btnSendConnectionProfile = (Button)this.findViewById(R.id.btnSendConnectionProfile);

        btnSendConnectionProfile.setOnClickListener(new OnClickListener() {
        	
			@Override
			public void onClick(View v) {
				
				boolean success = false;
				btnSendConnectionProfile.setEnabled(false);
				Random randomPort = new Random();
				
				while(!success)
				{
					try
					{
	    				node = new Node(state, randomPort.nextInt(3000));
	    				setListener();
	    				node.startReceiverThread();
	    				
	    				//start by sending profile information!
	    				join(state, Constants.TEMP_AP_PORT);
	    				
	    				success = true;
					}
					catch(Exception e)
					{
						e.printStackTrace();
						Log.d("better", "Unable to create node!");
					}
				}
				
				btnSendConnectionProfile.setEnabled(true);
			}
		});
        
    }
    
    private boolean join(NodeState state, int port) throws Exception
    {
    	boolean status = false;
    	
    	MiddlewarePacket packet = new MiddlewarePacket();
		byte [] header = {(byte)Constants.CONNECTION_PROFILE};
		String nodeProfile = state.toString();
		packet.setPacketData(header, nodeProfile.getBytes());
		InetAddress address = InetAddress.getAllByName(MiddlewareUtil.getIPAddress().get(0))[0];
		node.sendData(packet, address, port);
		status = true;
    	
    	return status;
    	
    }
    
    private void setListener()
    {
    	node.setDataReceived(this);
    }

	@Override
	public void nodeReceivedData(byte[] data) {
		
		String receivedData = new String(data);
		Log.d("better", receivedData);
		
	}

}
