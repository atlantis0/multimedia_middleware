package com.multimedia.middleware;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.middleware.listeners.AddressTable;
import com.middleware.listeners.CreatePermanetAccessPoint;
import com.middleware.listeners.NewAccessPoint;
import com.middleware.model.AccessPoint;
import com.middleware.model.Constants;
import com.middleware.model.DataReceived;
import com.middleware.model.MiddlewarePacket;
import com.middleware.model.Node;
import com.middleware.model.NodeState;
import com.multimedia.middleware.util.MiddlewareUtil;

public class ClientActivity extends Activity implements DataReceived, CreatePermanetAccessPoint, NewAccessPoint, AddressTable {
	
    int scale = -1;
    int level = -1;
    int voltage = -1;
    int temp = -1;
    
    int slides [] = {
    		R.drawable.ic_launcher,
    		R.drawable.one,
    		R.drawable.two,
    		R.drawable.three,
    		R.drawable.four,
    		R.drawable.five,
    		R.drawable.six,
    		
	};
    
    int slide_raw [] = {
    		R.raw.ic_launcher,
    		R.raw.one,
    		R.raw.two,
    		R.raw.three,
    		R.raw.four,
    		R.raw.five,
    		R.raw.six,
    };
    
    boolean isAccessPoint = false;
    
	Node node;
	NodeState state;
	
    Set<String> neighbours = null;
	
	//if this node is chosen to become permanent access point
	AccessPoint accessPoint;
	
	//UI elements
	Button btnSendConnectionProfile;
	Button btnInfo;
	TextView lblInfo;
	Gallery g;
	ImageView imgPresenter;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client);
        
		state = new NodeState("10", "1.2", "96");
		state.setStatus(true);
		state.setCanCreate(true);
		
		neighbours = new HashSet<String>();
		
		g = (Gallery)this.findViewById(R.id.gallery);
		g.setAdapter(new ImageAdapter(this));
		
		imgPresenter = (ImageView)this.findViewById(R.id.imgPresenter);
		
		lblInfo = (TextView)this.findViewById(R.id.lblInfo);
		
		btnInfo = (Button)this.findViewById(R.id.btnInfo);
        btnSendConnectionProfile = (Button)this.findViewById(R.id.btnSendConnectionProfile);

        g.setOnItemClickListener(new OnItemClickListener() {
        	
        	@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
        	{
        		byte[] imageBytes = null;
        		
        		try
        		{
        			InputStream is = getApplicationContext().getResources().openRawResource(slide_raw[arg2]);
            		imageBytes = org.apache.commons.io.IOUtils.toByteArray(is);
        		}
        		catch(Exception e)
        		{
        			Log.d("better", "unable to encode the image");
        			e.printStackTrace();
        		}
				
        		//Bitmap bm = BitmapFactory.decodeResource(getResources(), slides[arg2]);
        		//ByteArrayOutputStream baos = new ByteArrayOutputStream();
				//bm.compress(Bitmap.CompressFormat.PNG, 100, baos);  
				//imageBytes = baos.toByteArray();

				Log.d("better", "sending ..." + imageBytes.toString());
        		
        		if(!isAccessPoint)
        		{
            		broadCastData(Constants.DATA, imageBytes, node, neighbours);
        		}
        		else
        		{
        			broadCastData(Constants.DATA, imageBytes, accessPoint, neighbours);
        		}
        		
        		Toast.makeText(getApplicationContext(), "Selected", Toast.LENGTH_SHORT).show();
        		imgPresenter.setImageResource(slides[arg2]);
        		
        		
        	}
		});
        
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
	    				node = new Node(state, 1000 + randomPort.nextInt(3000));
	    				setListener();
	    				node.startReceiverThread();
	    				
	    				//start by sending profile information!
	    				join(node, state, Constants.TEMP_AP_PORT);
	    				
	    				success = true;
	    				
	    				Log.d("better", "Node Created!");
					}
					catch(Exception e)
					{
						e.printStackTrace();
						Log.d("better", "Retrying...");
					}
				}
				
				//btnSendConnectionProfile.setEnabled(true);
			}
		});
        
        
        btnInfo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				try
				{
					if(!isAccessPoint)
					{
						requestTable(node, Constants.PERMANET_AP_PORT);
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
		});
        
    }
    
    private void setListener()
    {
    	node.setDataReceived(this);
    	node.setCreatePermanetAccessPoint(this);
    	node.setNewAccessPointCreated(this);
    }
    
    private void setAccessPointListener()
    {
    	accessPoint.setDataReceived(this);
    	accessPoint.setAddressTable(this);
    }
    
    private boolean join(Node node, NodeState state, int port) throws Exception
    {
    	boolean status = false;
    	
    	MiddlewarePacket packet = new MiddlewarePacket(node.getPort());
		byte [] header = {(byte)Constants.CONNECTION_PROFILE};
		String nodeProfile = state.toString();
		//new byte[] {-119, 80, 13, 10, 0, 32, 23}
		packet.setPacketData(header, nodeProfile.getBytes());
		//InetAddress address = InetAddress.getAllByName(MiddlewareUtil.getIPAddress().get(0))[0];
		InetAddress address = InetAddress.getAllByName(MiddlewareUtil.KNOWN_HOST)[0];
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
		InetAddress address = InetAddress.getAllByName(MiddlewareUtil.KNOWN_HOST)[0];
		//InetAddress address = InetAddress.getAllByName(MiddlewareUtil.getIPAddress().get(0))[0];
		node.sendData(packet, address, Constants.PERMANET_AP_PORT);
		
		status = true;
    	
    	return status;
    }

    @Override
	public void nodeReceivedData(final byte[] data) {
		
		String receivedData = new String(data);
		Log.d("better", receivedData);
		
		final byte[] header = new byte[1];
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
			neighbours.add(MiddlewareUtil.KNOWN_HOST + ":" + Constants.PERMANET_AP_PORT);
			
			Log.d("better", "TABLE_DATA --> neighbours" + neighbours.toString());
		}
		
		else if(receivedHeader.equals(String.valueOf(Constants.DATA)))
		{
			try
			{
				Log.d("better", "receiving..." + body.toString());
				final Bitmap bmp=BitmapFactory.decodeByteArray(body,0,body.length);
				
				imgPresenter.post(new Runnable() {
					
					@Override
					public void run() {
						Log.d("better", "setting bitmap...");
						imgPresenter.setImageBitmap(bmp);
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
	
    @Override
	public void nodeAdded(Node node) {
		
		Log.d("better", "Node Added!");
		Log.d("better", node.getAddress().toString()+":"+node.getPort());
	}

	@Override
	public void nodeRemoved(Node node) {
		
		Log.d("better", "Node Added!");
		Log.d("better", node.getAddress().toString()+":"+node.getPort());
	}

	@Override
	public void accessPointCreated(boolean success, InetAddress address, int port, int number) {
		
		if(success)
		{	
			Log.d("better", String.valueOf(number));
			
			String username = MiddlewareUtil.username;
			String password = MiddlewareUtil.password;
			
			MiddlewarePacket packet = new MiddlewarePacket(port);
			byte [] header = {(byte)Constants.PERMANENT_AP_CREATED};
			packet.setPacketData(header, (username+":"+password).getBytes());
			
			try
			{
				node.sendData(packet, InetAddress.getAllByName(MiddlewareUtil.KNOWN_HOST)[0], Constants.TEMP_AP_PORT);
				//node.sendData(packet, InetAddress.getAllByName(MiddlewareUtil.getIPAddress().get(0))[0], Constants.TEMP_AP_PORT);
				MiddlewareUtil.createWifiAccessPoint(getApplicationContext(), username, password);
				
				accessPoint = new AccessPoint(state, Constants.PERMANET_AP_PORT);
				setAccessPointListener();
				accessPoint.startReceiverThread();
				
				isAccessPoint = true;
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

		}
		
	}
	
	@Override
	public void newAccessPointCreated(boolean success, String username, String password) {
		
		if(success)
		{	
			try
			{
				boolean status = false;
				while(!status)
				{
					status = MiddlewareUtil.connectToNetwork(getApplicationContext(), username, password);
				}
				
				Log.d("better", "joining.....");
				Thread.sleep(8000);
				
				status = false;
				Random randomPort = new Random();
				
				while(!status)
				{
					try
					{
						//stop the previous node
						try
						{
							node.stop();
							node = null;
						}
						catch(Exception e)
						{
							Log.d("better", "Unable to stop previous node!");
							e.printStackTrace();
						}
						
						node = new Node(state, 1000 + randomPort.nextInt(3000));
						setListener();
	    				node.startReceiverThread();
	    				
	    				//start by sending profile information!
	    				join(node, state, Constants.PERMANET_AP_PORT);
	    				
	    				isAccessPoint = false;
	    				
	    				status = true;
	    				
	    				Log.d("better", "New Node Created!");
					}
					catch(Exception e)
					{
						e.printStackTrace();
						Log.d("better", "Retrying...");
					}
					
					Log.d("better", "joined!");
				}
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
	}
	
	public void registerForBatteryState()
	{
		BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	            level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
	            scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
	            temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
	            voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
	            
	            state.setBatteryLife(new Integer(level).toString());
	            
	            Log.d("better", "level is "+level+"/"+scale+", temp is "+temp+", voltage is "+voltage);
	        }
	    };
	    IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
	    registerReceiver(batteryReceiver, filter);
	    
	}
	
	public class ImageAdapter extends BaseAdapter {
		
	    int mGalleryItemBackground;
	    private Context mContext;

	    public ImageAdapter(Context c) {
	        setmContext(c);
	        TypedArray a = obtainStyledAttributes(R.styleable.HelloGallery);
	        mGalleryItemBackground = a.getResourceId(
	                R.styleable.HelloGallery_android_galleryItemBackground, 0);
	        a.recycle();
	    }

	    public int getCount() {
	        return slides.length;
	    }

	    public Object getItem(int position) {
	        return position;
	    }

	    public long getItemId(int position) {
	        return position;
	    }

	    public View getView(int position, View convertView, ViewGroup parent) {
	    	
	    	ImageView iv = new ImageView(getApplicationContext());
	        iv.setImageResource(slides[position]);
	        iv.setScaleType(ImageView.ScaleType.FIT_XY);
	        iv.setLayoutParams(new Gallery.LayoutParams(150,120));
	        iv.setBackgroundResource(mGalleryItemBackground);
	        
	        return iv;
	    }

		public Context getmContext() {
			return mContext;
		}

		public void setmContext(Context mContext) {
			this.mContext = mContext;
		}
	}
	

	

}
