package com.aman.samples.camera;

import java.io.FileOutputStream;

import com.aman.sample.camera.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class Main extends Activity{
	Bitmap takenImage;
	int CODE_CAM = 123, i =1;
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Button openCam = (Button)findViewById(R.id.take_photo);
		openCam.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Intent camIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(camIntent,CODE_CAM);
			}});
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
		if (requestCode == CODE_CAM && resultCode == RESULT_OK) {
			takenImage = (Bitmap)data.getExtras().get("data");
			/*try{
				FileOutputStream stream = super.openFileOutput("picture" + i++ + ".png", MODE_PRIVATE);
				takenImage.compress(CompressFormat.PNG, 100, stream);
				stream.flush();
				stream.close();
			}catch(Exception e){
				Log.e("Exception in saving",""+e.getMessage());
			}*/
			
			Log.d("IMAGE DETAILS","Density: "+takenImage.getDensity()+", Height: "+takenImage.getHeight()
					+" Width:"+takenImage.getWidth());
			ImageView camImg = (ImageView)findViewById(R.id.img_camera);
			camImg.setImageBitmap(takenImage);
		}
		  
	} 

}
