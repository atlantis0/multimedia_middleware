package com.multimedia.middleware;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SelectFriendsActivity extends Activity {
	
	ListView lstFriends;
	String []list = null;
	
	ArrayAdapter<String> adapter = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friendslist);
		
		lstFriends = (ListView)this.findViewById(R.id.lstFriends);
		
		Intent intent = getIntent();
		list = intent.getStringArrayExtra("list");

	}
	
	private class FriendListAdapter extends ArrayAdapter<String> {

		public FriendListAdapter(Context context, int textViewResourceId, String[] objects) {
			super(context, textViewResourceId, objects);
		
		}
		
		public View getView(int position, View convertView, ViewGroup parent)
		{
			
			return null;
		}
		
	}

}
