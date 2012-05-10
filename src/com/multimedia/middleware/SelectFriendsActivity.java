package com.multimedia.middleware;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;

public class SelectFriendsActivity extends Activity {
	
	Button btnFinish;
	ListView lstFriends;
	String []list = null;
	Intent intent;
	
	int selected [];
	
	FriendListAdapter adapter = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friendslist);
		
		btnFinish = (Button)this.findViewById(R.id.btnFinish);
		lstFriends = (ListView)this.findViewById(R.id.lstFriends);
		
		intent = getIntent();
		list = intent.getStringArrayExtra("list");
		
		selected = new int[list.length];
		
		adapter = new FriendListAdapter(this, R.layout.friendslist, R.id.lblFriendName, list);
		lstFriends.setAdapter(adapter);
		
		btnFinish.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				ArrayList<String> toReturn = new ArrayList<String>();
				
				for(int i=0; i<selected.length; i++)
				{
					if(selected[i] == 1)
					{
						toReturn.add(list[i]);
					}
				}
				intent.putExtra("selected", toReturn);
				setResult(RESULT_OK, intent);
				finish();
				
			}
		});
		

	}
	
	private class FriendListAdapter extends ArrayAdapter<String> {

		String objects[];
		
		public FriendListAdapter(Context context, int resource, int textViewResourceId, String[] objects) {
			super(context, textViewResourceId, objects);
			this.objects = objects;
		
		}
		
		public View getView(final int position, View convertView, ViewGroup parent)
		{
			View row = convertView;
			
			LayoutInflater inflator = getLayoutInflater();
			row = inflator.inflate(R.layout.friend_row, parent, false);
			
			TextView name = (TextView)row.findViewById(R.id.lblFriendName);
			CheckBox ck = (CheckBox)row.findViewById(R.id.chkSelect);
			
			ck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					
					if(isChecked)
					{
						selected[position] = 1;
						Log.d("better", "checked!");
					}
					else
					{
						selected[position] = 0;
						Log.d("better", "unchecked!");
					}
						
				}
			});
			
			name.setText(objects[position]);
			
			return row;
		}
		
	}

}
