package com.multimedia.middleware;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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

	boolean isAccessPoint = true;
	
	//UI Elements
	TextView lblInfo_1, lblBoard_1;
	EditText txtMessage_1;
	Button btnAdd_1, btnSend_1, btnInfo_1;
	
	AccessPoint accessPoint;
	NodeState state;
	
	//if a the access point is to be changed
	Node newNode;
	
	Set<String> neighbours = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.access);
        
		state = new NodeState("10", "1.2", "90");
		state.setStatus(true);
		state.setCanCreate(true);
		
		neighbours = new HashSet<String>();
		
		lblInfo_1 = (TextView)this.findViewById(R.id.lblInfo_1);
		txtMessage_1 = (EditText)this.findViewById(R.id.txtMessage_1);
		btnAdd_1 = (Button)this.findViewById(R.id.btnAdd_1);
		btnSend_1 = (Button)this.findViewById(R.id.btnSend_1);
		btnInfo_1 = (Button)this.findViewById(R.id.btnInfo_1);
		
    }
    
    private void createTemporaryAcessPoint()
    {
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
    
    private void choosePermanetAccessPoint()
    {
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
    
    @Override
    public void onResume()
    {
    	super.onResume();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.setup, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	if(item.getItemId() == R.id.menucreateap)
    	{
    		createTemporaryAcessPoint();
    		return true;
    	}
    	if(item.getItemId() == R.id.menuchoose)
    	{
    		choosePermanetAccessPoint();
    		return true;
    	}
        
        return super.onOptionsItemSelected(item);
    }
    
    
    private boolean join(Node node, NodeState state, int port) throws Exception
    {
    	boolean status = false;
    	
    	MiddlewarePacket packet = new MiddlewarePacket(node.getPort());
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
									newNode = new Node(state, 1000 + randomPort.nextInt(3000));
									setNewNodeListener();
									newNode.startReceiverThread();
									
									//start by sending profile information!
									join(newNode, state, Constants.PERMANET_AP_PORT);
									
									isAccessPoint = false;
									
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
		
		String receivedData = new String(data);
		Log.d("better", receivedData);
		
		byte[] header = new byte[1];
		header[0] = data[0];
		byte body[] = new byte[data.length-1];
		
		for(int i=0; i<data.length-1; i++)
		{
			body[i] = data[i+1];
		}
		
        String receivedHeader = new String(header);
        
		if(receivedHeader.equals(String.valueOf(Constants.NEW_NODE)))
		{
			final String receivedBody = new String(body);
			String [] nodes = receivedBody.split(",");
			
			for(int i=0; i<nodes.length; i++)
			{
				if(nodes[i].length() > 1)
				{
					neighbours.add(nodes[i]);
				}
			}
			
			Log.d("better", "updated table -->" + neighbours.toString());
		}
		
		else if(receivedHeader.equals(String.valueOf(Constants.DISCONNECTED)))
		{
			final String receivedBody = new String(body);
			String [] nodes = receivedBody.split(",");
			
			for(int i=0; i<nodes.length; i++)
			{
				if(nodes[i].length() > 1)
				{
					neighbours.remove(nodes[i]);
					Log.d("better", "removing " + nodes[i] + " ...");
				}
			}
			
			Log.d("better", "updated table -->" + neighbours.toString());
		}
		
		else if(receivedHeader.equals(String.valueOf(Constants.TABLE_DATA)))
		{
			//clear the neighbors set and fill it with TABLE_DATA
			String all = new String(body);
			all = all.replace("[", "");
			all = all.replace("]", "");
			
			String [] nodes = all.split(",");
			
			neighbours.clear();
			
			for(int i=0; i<nodes.length; i++)
			{
				if(nodes[i].length() > 1)
				{
					neighbours.add(nodes[i]);
				}
			}
			
			//finally add the access point itself
			neighbours.add(MiddlewareUtil.getIPAddress().get(0) + ":" + Constants.PERMANET_AP_PORT);
			
			Log.d("better", "TABLE_DATA --> neighbours" + neighbours.toString());
		}
		
		else if(receivedHeader.equals(String.valueOf(Constants.DATA)))
		{
			try
			{
				Log.d("better", "receiving..." + body.toString());
				final Bitmap bmp=BitmapFactory.decodeByteArray(body,0,body.length);
				
				imgSlide.post(new Runnable() {
					
					@Override
					public void run() {
						Log.d("better", "setting bitmap....");
						imgSlide.setImageBitmap(bmp);
					}
				});
			}
			catch (Exception e) {
				Log.d("better", "failed to decode bitmap");
				e.printStackTrace();
			}
			
		}
		else
		{
			
		}
	}
	
}
