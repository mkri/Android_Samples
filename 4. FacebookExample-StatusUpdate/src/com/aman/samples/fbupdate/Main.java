package com.aman.samples.fbupdate;

import java.io.IOException;
import java.net.MalformedURLException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;


/**
 * Class to demonstrate how you update your Facebook status using the Facebook Android SDK
 * Before using this example, create an app at Facebook and get its App ID
 * Then import the Facebook SDK into eclipse and mark it as a library to this project
 * else this program will keep showing errors around the usage of Facebook class.
 * 
 * @author Aman Alam (http://www.sheikhaman.com)
 */
public class Main extends Activity {

	
    Facebook facebook = new Facebook("123456789789456"); // Application ID of your app at facebook
    boolean isLoggedIn = false;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
        //Implementing SSO
        facebook.authorize(this, new String[]{"publish_stream"}, new DialogListener(){

			@Override
			public void onComplete(Bundle values) {
				//control comes here if the login was successful
//				Facebook.TOKEN is the key by which the value of access token is stored in the Bundle called 'values'
				Log.d("COMPLETE","AUTH COMPLETE. VALUES: "+values.size());
				Log.d("AUTH TOKEN","== "+values.getString(Facebook.TOKEN));
				updateStatus(values.getString(Facebook.TOKEN));
			}

			@Override
			public void onFacebookError(FacebookError e) {
				Log.d("FACEBOOK ERROR","FB ERROR. MSG: "+e.getMessage()+", CAUSE: "+e.getCause());
			}

			@Override
			public void onError(DialogError e) {
				Log.e("ERROR","AUTH ERROR. MSG: "+e.getMessage()+", CAUSE: "+e.getCause());
			}

			@Override
			public void onCancel() {
				Log.d("CANCELLED","AUTH CANCELLED");
			}
		});
    }
    
    //updating Status
    public void updateStatus(String accessToken){
    	try {
			Bundle bundle = new Bundle();
			bundle.putString("message", "test update"); //'message' tells facebook that you're updating your status
			bundle.putString(Facebook.TOKEN,accessToken);
			//tells facebook that you're performing this action on the authenticated users wall, thus 
//			it becomes an update. POST tells that the method being used is POST
			String response = facebook.request("me/feed",bundle,"POST");
			Log.d("UPDATE RESPONSE",""+response);
		} catch (MalformedURLException e) {
			Log.e("MALFORMED URL",""+e.getMessage());
		} catch (IOException e) {
			Log.e("IOEX",""+e.getMessage());
		}
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("onActivityResult","onActivityResult");
        facebook.authorizeCallback(requestCode, resultCode, data);
    }

}