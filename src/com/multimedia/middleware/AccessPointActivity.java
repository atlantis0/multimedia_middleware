package com.multimedia.middleware;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

	public static final int FIND_FRIENDS = 1002;
	
	boolean isAccessPoint = true;
	
	//UI Elements
	TextView lblInfo_1, lblBoard_1;
	EditText txtMessage_1;
	Button btnAdd_1, btnSend_1;
	
	AccessPoint accessPoint;
	NodeState state;
	
	//if a the access point is to be changed
	Node newNode;
	
	Set<String> neighbours = null;
	Set<String> selected = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.access);
        
		state = new NodeState("10", "1.2", "20");
		state.setStatus(true);
		state.setCanCreate(true);
		
		neighbours = new HashSet<String>();
		
		lblBoard_1 = (TextView)this.findViewById(R.id.lblBoard_1);
		lblBoard_1.setText("");
		
		lblInfo_1 = (TextView)this.findViewById(R.id.lblInfo_1);
		txtMessage_1 = (EditText)this.findViewById(R.id.txtMessage_1);
		btnAdd_1 = (Button)this.findViewById(R.id.btnAdd_1);
		btnSend_1 = (Button)this.findViewById(R.id.btnSend_1);
		
		btnAdd_1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Intent intent = new Intent(getApplicationContext(), SelectFriendsActivity.class);
				intent.putExtra("list", getStringArray(neighbours));
				startActivityForResult(intent, FIND_FRIENDS);
				
			}
		});
		
		
		btnSend_1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if(selected != null && selected.size() != 0)
				{
					char command = Constants.DATA;
					
					String message = Secure.getString(getApplicationContext().getContentResolver(),Secure.ANDROID_ID) + " : " + txtMessage_1.getText().toString();
					
					if(message.length() >= 1)
					{
						if(isAccessPoint)
							broadCastData(command, txtMessage_1.getText().toString().getBytes(), accessPoint, selected);
						else
							broadCastData(command, txtMessage_1.getText().toString().getBytes(), newNode, selected);
					}
					else
					{
						Toast.makeText(getApplicationContext(), "Please enter the message!", Toast.LENGTH_LONG).show();
					}
				}
				else
				{
					Toast.makeText(getApplicationContext(), "Select Friends", Toast.LENGTH_LONG).show();
				}
				
			}
		});

    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	if(resultCode == RESULT_OK)
    	{
    		if(requestCode == FIND_FRIENDS)
    		{
    			ArrayList<String> list = data.getStringArrayListExtra("selected");
    			this.selected = arrayListToSet(list);
    			Log.d("better", "selected..." + this.selected.toString());
    		}
    	}
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
    	if(item.getItemId() == R.id.menurequest)
    	{
    		getTableInformation();
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
    
    private boolean requestTable(Node node, int atPort) throws Exception
    {
    	boolean status = false;
    	
    	//request table 
		MiddlewarePacket packet = new MiddlewarePacket(node.getPort());
		byte [] header = {(byte)Constants.REQUEST_TABLE};
		String data = "data";
		packet.setPacketData(header, data.getBytes());
		//InetAddress address = InetAddress.getAllByName("192.168.43.1")[0];
		InetAddress address = InetAddress.getAllByName(MiddlewareUtil.getIPAddress().get(0))[0];
		node.sendData(packet, address, Constants.PERMANET_AP_PORT);
		
		status = true;
    	
    	return status;
    }
    
    private void broadCastData(char command, byte[] data, Node node, Set<String> nodes)
	{
		Iterator<String> iter = nodes.iterator();
		
		while(iter.hasNext())
		{
			try
			{
				final MiddlewarePacket packet = new MiddlewarePacket(node.getPort());
				byte [] header = {(byte)command};
				packet.setPacketData(header, data);
				
				final String address[] = iter.next().split(":");
				final InetAddress nodeAddress = InetAddress.getByName(address[0]);
				
				//node.sendData(packet, nodeAddress, new Integer(address[1]));
				
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						
						Socket outSocket = null;
						try
						{
							outSocket = new Socket(nodeAddress, new Integer(address[1])); 
							OutputStream out = outSocket.getOutputStream();
							out.write(packet.getMiddleWareData());
							out.close();
							outSocket.close();
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
						
						
					}
				}).start();
				
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
    
    private void getTableInformation()
    {
    	try
		{
			if(!isAccessPoint)
			{
				requestTable(newNode, Constants.PERMANET_AP_PORT);
			}
			else
			{
				neighbours = accessPoint.getRoutingTable().getTable().keySet();
				Log.d("better", neighbours.toString());
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
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
    
    private String [] getStringArray(Set<String> neighbours)
    {
    	String [] arrayToReturn;
    	
    	Iterator<String> iter = neighbours.iterator();
    	
    	arrayToReturn = new String[neighbours.size()];
    	
    	int i = 0;
    	while(iter.hasNext())
    	{
    		arrayToReturn[i] = iter.next();
    		i++;
    	}
    	
    	return arrayToReturn;
    }
    
    private Set<String> arrayListToSet(ArrayList<String> list)
    {
    	Set<String> setToReturn = new HashSet<String>();
    	
    	for(int i=0; i<list.size(); i++)
    	{
    		setToReturn.add(list.get(i));
    	}
    	
    	return setToReturn;
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
								Thread.sleep(10000);
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
		final byte body[] = new byte[data.length-1];
		
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
				final String receivedBody = new String(body);
				
				lblBoard_1.post(new Runnable() {
					
					@Override
					public void run() {
						lblBoard_1.setText(lblBoard_1.getText().toString() + "\n" + receivedBody);
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
