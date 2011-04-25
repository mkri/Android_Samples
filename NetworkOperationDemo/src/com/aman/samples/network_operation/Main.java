/**
Copyright [2011] [Aman Alam]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   **/
   
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


/**
 * This class uses AsyncTask to fetch some data from the web.
 * You can stop the operation in the middle and start it again.
 * The whole (tiny) system is event based, and will react to the button clicks.
 * Read comments to understand what's being done.
 * 
 * This is a VERY tiny example of how you can access the web within your Android app.
 * It may be very less efficient.
 * @author aman
 *
 */
public class Main extends Activity {
    /** Called when the activity is first created. */
	
	TextView result_tv ;
	ProgressBar pv ;
	Button pull_btn;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
    	//	reference to the text view that's supposed to be holding the results
        result_tv = (TextView)findViewById(R.id.result_text);
    	
    	//	reference to the progress bar that is used to show the progress to the user
    	pv = (ProgressBar)findViewById(R.id.progress_bar);

        
        //getting instance of the button that will fire events
        pull_btn = (Button)findViewById(R.id.start_stop_btn);
        pull_btn.setOnClickListener(new OnClickListener(){

        	NetTask netTask;
        	
        	/**
        	 * Handles the different states of the button and the network pull status accordingly
        	 */
			@Override
			public void onClick(View v) {
				Button btn = (Button)v;
				
				//if the text says 'start data pull', then start the AsynTask to fethc the data
				if(btn.getText().toString().trim().equals("Start Data Pull")){
					netTask= new NetTask();
		    		netTask.execute();
					pv.setVisibility(View.VISIBLE);
					result_tv.setText("fething data from http://www.google.com");
					btn.setText("Stop Data Pull");
				}else if(btn.getText().toString().trim().equals("Stop Data Pull")){ //if the text says 'stop data pull' then stop the AsyncTask
					if(null!=netTask){
						netTask.cancel(true);
					}
					btn.setText("Start Data Pull");
				}else if(btn.getText().toString().trim().equals("Clear")){ //If the text says 'clear' then clear the textview that holds the results
					result_tv.setText("");
					btn.setText("Start Data Pull");
				}
			}
		});
    }
    
    
    /**
     * AsyncTask class that handles all the network activity in the background.
     * @author aman
     *
     */
    class NetTask extends AsyncTask<Void,Void,String>{

    	/**
    	 * This method is executed in the background, inside anotehr thread
    	 */
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
    	
		
		/**
    	 * This method is executed on the UI Thread, when the task is canceled
    	 */
		@Override
		protected void onCancelled(){
			pv.setVisibility(View.INVISIBLE);
			result_tv.setText("data fetch aborted");
		}
		
		/**
    	 * This method is executed on the UI Thread, when the AsyncTask finishes working
    	 */
		@Override
		protected void onPostExecute(String dataCaptured){
			pv.setVisibility(View.INVISIBLE);
			result_tv.setText(dataCaptured);
			pull_btn.setText("Clear");
		}
    }
}