package com.multimedia.middleware.presentation;

import java.io.ByteArrayOutputStream;

import android.R;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

public class PresentationActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_lock_idle_alarm);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos); //bm is the bitmap object   
		byte[] imageBytes = baos.toByteArray(); 
	}

}
