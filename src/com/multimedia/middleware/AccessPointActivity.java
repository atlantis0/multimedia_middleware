package com.multimedia.middleware;

import java.net.InetAddress;
import java.util.Random;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.middleware.listeners.AddressTable;
import com.middleware.listeners.TempAPToNew;
import com.middleware.model.AccessPoint;
import com.middleware.model.Constants;
import com.middleware.model.DataReceived;
import com.middleware.model.MiddlewarePacket;
import com.middleware.model.Node;
import com.middleware.model.NodeState;
import com.multimedia.middleware.util.MiddlewareUtil;

public class AccessPointActivity extends Activity implements DataReceived, AddressTable, TempAPToNew{

	//UI Elements
	Button btnCreateTemporaryAccessPoint;
	Button btnChoosePermanet;
	Button btnHelloPacket;
	
	EditText txtAddress;
	
	AccessPoint accessPoint;
	NodeState state;
	
	//if a the access point is to be changed
	Node newNode;
	NodeState newNodeState;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.access);
        
		state = new NodeState("10", "1.2", "90");
		state.setStatus(true);
		
		newNodeState = new NodeState("10", "1.2", "45");
		newNodeState.setStatus(true);
		
		txtAddress = (EditText)this.findViewById(R.id.txtAddress);
		
        btnCreateTemporaryAccessPoint = (Button)this.findViewById(R.id.btnCreateTemporaryAccessPoint);
        btnChoosePermanet = (Button)this.findViewById(R.id.btnChoosePermanet);
        btnHelloPacket = (Button)this.findViewById(R.id.btnHelloPacket);
        
        btnCreateTemporaryAccessPoint.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				try
				{
					MiddlewareUtil.createWifiAccessPoint(getApplicationContext(), "sirack", "betterconnect");
					accessPoint = new AccessPoint(state, Constants.TEMP_AP_PORT);
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
						Log.d("better", "Access Point Changing!");
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
        
        btnHelloPacket.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				String total[] = txtAddress.getText().toString().split(":");
				
				try
				{
					InetAddress address = InetAddress.getByName(total[0]);
					MiddlewarePacket packet = new MiddlewarePacket();
					byte [] header = {(byte)Constants.REQUEST_TABLE};
					packet.setPacketData(header, "some data".getBytes());
					newNode.sendData(packet, address, new Integer(total[1]));
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				
				
			}
		});
        
        
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
    }
    
    private boolean join(Node node, NodeState state, int port) throws Exception
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
    
    public void setListener()
    {
    	accessPoint.setDataReceived(this);
    	accessPoint.setAddressTable(this);
    	accessPoint.setTempApToNewAccessPoint(this);
    }
    
    private void setNewNodeListener()
    {
    	newNode.setDataReceived(this);
    }
	
	@Override
	public void temporaryAccessPointConnectToNewAP(final boolean success, final String cred) {
		
		this.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				
				if(success)
				{	
					String credentials = cred;
					String credInfo[] = credentials.split(":");
					final String username = credInfo[0];
					final String password = credInfo[1];
					
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							try
							{
								boolean temp1 = false;
								while(!temp1)
								{
									temp1 = MiddlewareUtil.disableAP(getApplicationContext(), "sirack", "betterconnect");
									
									temp1 = true;
									
									Log.d("better", "Acccess Point Disabled!");
								}
								
								//time taken to disconnect from the network
								Thread.sleep(1000);
									
								temp1 = false;
								
								while(!temp1)
								{
									temp1 = MiddlewareUtil.connectToNetwork(getApplicationContext(), username, password);
									
									Log.d("better", "Connected to newly created network!");
									
									temp1 = true;
								}
								
								//wait some time until node connects to a network
								Thread.sleep(8000);
							}
							catch(Exception e)
							{
								e.printStackTrace();
							}
							
							Log.d("better", "Changing from access point to client mode...");
							
							boolean bin = false;
							Random randomPort = new Random();
							
							while(!bin)
							{
								try
								{
									newNode = new Node(newNodeState, 1000 + randomPort.nextInt(3000));
									setNewNodeListener();
									newNode.startReceiverThread();
									
									//start by sending profile information!
									join(newNode, newNodeState, Constants.PERMANET_AP_PORT);
									
									Log.d("better", "joining the permanet access point...");
									
									bin = true;
								}
								catch(Exception e)
								{
									e.printStackTrace();
								}
								
							}//end while
						}
					}).start();
					
				}
				
			}
		});
		
		
	}

	@Override
	public void nodeAdded(Node node) {
		
		Log.d("better", "Node Added!");
		Log.d("better", node.getAddress().toString()+":"+node.getPort());
		
	}

	@Override
	public void nodeRemoved(Node node) {
		
		Log.d("better", "Node Removed!");
		Log.d("better", node.getAddress().toString()+":"+node.getPort());
		
	}
	
	@Override
	public void nodeReceivedData(byte[] data) {
		
		String receivedString = new String(data);
		Log.d("better", receivedString);
	}
	
}
