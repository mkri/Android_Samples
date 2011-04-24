package com.aman.samples.network_operation;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Main extends Activity {
    /** Called when the activity is first created. */
	
	TextView result_tv ;
	ProgressBar pv ;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
    	//	reference to the text view that's supposed to be holding the results
        result_tv = (TextView)findViewById(R.id.result_text);
    	
    	//	reference to the progress bar that is used to show the progress to the user
    	pv = (ProgressBar)findViewById(R.id.progress_bar);

        
        //getting instance of the button that will fire events
        Button pull_btn = (Button)findViewById(R.id.start_stop_btn);
        pull_btn.setOnClickListener(new OnClickListener(){

        	//Setting different actions on different states of button text
			@Override
			public void onClick(View v) {
				Button btn = (Button)v;
				
				if(btn.getText().toString().trim().equals("Start Data Pull")){
					
					pv.setVisibility(View.VISIBLE);
					result_tv.setText("fething data from http://www.google.com");
					btn.setText("Stop Data Pull");
				}else if(btn.getText().toString().trim().equals("Stop Data Pull")){
					netTask.cancel(true);
					btn.setText("Start Data Pull");
				}else if(btn.getText().toString().trim().equals("Clear")){
					result_tv.setText("");
					btn.setText("Start Data Pull");
				}
			}
		});
    }
    
    void NetTaskHandler(){
    	NetTask netTask = null;
    	netTask= new NetTask();
		netTask.execute();
    }
    
    
    class NetTask extends AsyncTask<Void,Void,String>{

		@Override
		protected String doInBackground(Void... params) {
			String results = "";
			
			try{
			URL url = new URL("http://www.google.com");
			Object content = url.getContent();
			InputStream is = (InputStream) content;
			int temp;
			while((temp = is.read())!=-1){
				if(!isCancelled()){
					results+=(char)temp;
					Log.d("GettingData","Getting data "+temp);
				}else{
					Log.d("Cancelled","Cancelled");
					break;
				}
				
			}
			
			}catch(MalformedURLException murlex){
				Log.e("ERROR","Malformed URL: "+murlex.getMessage());
				results = "Some Error Occurred";
			}catch(IOException ioex){
				Log.e("ERROR","IO Exception: "+ioex.getMessage());
				results = "Some Error Occurred";
			}
			return results;
		}
    	
		@Override
		protected void onCancelled(){
			pv.setVisibility(View.INVISIBLE);
			result_tv.setText("data fetch aborted");
		}
		
		@Override
		protected void onPostExecute(String dataCaptured){
			pv.setVisibility(View.INVISIBLE);
			result_tv.setText(dataCaptured);
		}
    }
}