package com.multimedia.middleware;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Random;

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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

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
    
    boolean isAccessPoint = false;
    
    ArrayList<Bitmap> app_screenshots = null;
    
	Node node;
	NodeState state;
	
	//if this node is chosen to become permanent access point
	AccessPoint accessPoint;
	
	//UI elements
	Button btnSendConnectionProfile;
	Button btnInfo;
	TextView lblInfo;
	Gallery g;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client);
        
		state = new NodeState("10", "1.2", "96");
		state.setStatus(true);
		state.setCanCreate(true);
		
		g = (Gallery)this.findViewById(R.id.gallery);
		app_screenshots = new ArrayList<Bitmap>();
		
		
		app_screenshots.add(BitmapFactory.decodeResource(getResources(), R.drawable.one));
		app_screenshots.add(BitmapFactory.decodeResource(getResources(), R.drawable.two));
		app_screenshots.add(BitmapFactory.decodeResource(getResources(), R.drawable.three));
		app_screenshots.add(BitmapFactory.decodeResource(getResources(), R.drawable.four));
		app_screenshots.add(BitmapFactory.decodeResource(getResources(), R.drawable.five));
		app_screenshots.add(BitmapFactory.decodeResource(getResources(), R.drawable.six));
		app_screenshots.add(BitmapFactory.decodeResource(getResources(), R.drawable.seven));
		app_screenshots.add(BitmapFactory.decodeResource(getResources(), R.drawable.eight));
		app_screenshots.add(BitmapFactory.decodeResource(getResources(), R.drawable.nine));
		
		g.setAdapter(new ImageAdapter(this));
		 
		lblInfo = (TextView)this.findViewById(R.id.lblInfo);
		
		btnInfo = (Button)this.findViewById(R.id.btnInfo);
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
					lblInfo.setText(accessPoint.getRoutingTable().getRoutingTable().keySet().toString());
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}

			}
		});
        
    }
    
    public class ImageAdapter extends BaseAdapter {
	    int mGalleryItemBackground;
	    private Context mContext;

	    public ImageAdapter(Context c) {
	        mContext = c;
	        TypedArray a = obtainStyledAttributes(R.styleable.HelloGallery);
	        mGalleryItemBackground = a.getResourceId(
	                R.styleable.HelloGallery_android_galleryItemBackground, 0);
	        a.recycle();
	    }

	    public int getCount() {
	        return app_screenshots.size();
	    }

	    public Object getItem(int position) {
	        return position;
	    }

	    public long getItemId(int position) {
	        return position;
	    }

	    public View getView(int position, View convertView, ViewGroup parent) {
	    	
	    	ImageView i = new ImageView(mContext);
	    	try
	    	{
		        i.setImageBitmap(app_screenshots.get(position));
		        i.setLayoutParams( new Gallery.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT));
		        i.setScaleType(ImageView.ScaleType.FIT_XY);
		        i.setBackgroundResource(mGalleryItemBackground);
	    	}
	    	catch(Exception e)
	    	{
	    		e.printStackTrace();
	    	}

	        return i;
	    }
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

	@Override
	public void accessPointCreated(boolean success, InetAddress address, int port, int number) {
		
		if(success)
		{	
			Log.d("better", String.valueOf(number));
			
			String username = MiddlewareUtil.username;
			String password = MiddlewareUtil.password;
			
			MiddlewarePacket packet = new MiddlewarePacket();
			byte [] header = {(byte)Constants.PERMANENT_AP_CREATED};
			packet.setPacketData(header, (username+":"+password).getBytes());
			
			try
			{
				node.sendData(packet, InetAddress.getAllByName(MiddlewareUtil.getIPAddress().get(0))[0], Constants.TEMP_AP_PORT);
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
						node.stop();
						node = null;
						
						node = new Node(state, randomPort.nextInt(3000));
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
	
	private void registerForBatteryState()
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
	public void nodeReceivedData(byte[] data) {
		
		String receivedData = new String(data);
		Log.d("better", receivedData);
		
	}

}
