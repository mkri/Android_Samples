/*
 * A very simple example to demonstrate how Twitter4J can be used to make the users
 * sign-in with Twitter.
 * Whenever it is started, control goes to twitter.com for authentication.
 * if app is already authenticated, control comes back in your application.
 * And finally it displays the first tweet of the timeline. 
 * 
 * 
 */



package com.aman.samples.t4jsignin;

import java.util.List;

import com.aman.t4j.activities.R;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.http.AccessToken;
import twitter4j.http.RequestToken;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.Toast;

public class Main extends Activity {
	/** Called when the activity is first created. */

	Twitter twitter;
	RequestToken requestToken;
//Please put the values of consumerKy and consumerSecret of your app 
	public final static String consumerKey = "A8xBfgdqrDV3yGJIowY8iQ"; // "your key here";
	public final static String consumerSecret = "hzY5WZKnkJSB08YeFCgYNpyv0GH1RvSRiMjEKsCPg"; // "your secret key here";
	private final String CALLBACKURL = "T4J_OAuth://callback_main";  //Callback URL that tells the WebView to load this activity when it finishes with twitter.com. (see manifest)


	/*
	 * Calls the OAuth login method as soon as its started
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		OAuthLogin();
	}

	/*
	 * - Creates object of Twitter and sets consumerKey and consumerSecret
	 * - Prepares the URL accordingly and opens the WebView for the user to provide sign-in details
	 * - When user finishes signing-in, WebView opens your activity back
	 */
	void OAuthLogin() {
		try {
			twitter = new TwitterFactory().getInstance();
			twitter.setOAuthConsumer(consumerKey, consumerSecret);
			requestToken = twitter.getOAuthRequestToken(CALLBACKURL);
			String authUrl = requestToken.getAuthenticationURL();
			this.startActivity(new Intent(Intent.ACTION_VIEW, Uri
					.parse(authUrl)));
		} catch (TwitterException ex) {
			Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
			Log.e("in Main.OAuthLogin", ex.getMessage());
		}
	}

	
	/*
	 * - Called when WebView calls your activity back.(This happens when the user has finished signing in)
	 * - Extracts the verifier from the URI received
	 * - Extracts the token and secret from the URL 
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Uri uri = intent.getData();
		try {
			String verifier = uri.getQueryParameter("oauth_verifier");
			AccessToken accessToken = twitter.getOAuthAccessToken(requestToken,
					verifier);
			String token = accessToken.getToken(), secret = accessToken
					.getTokenSecret();
			displayTimeLine(token, secret); //after everything, display the first tweet 

		} catch (TwitterException ex) {
			Log.e("Main.onNewIntent", "" + ex.getMessage());
		}

	}
	
	/*
	 * Displays the timeline's first tweet in a Toast
	 */

	@SuppressWarnings("deprecation")
	void displayTimeLine(String token, String secret) {
		if (null != token && null != secret) {
			List<Status> statuses = null;
			try {
				twitter.setOAuthAccessToken(token, secret);
				statuses = twitter.getFriendsTimeline();
				Toast.makeText(this, statuses.get(0).getText(), Toast.LENGTH_LONG)
					.show();
			} catch (Exception ex) {
				Toast.makeText(this, "Error:" + ex.getMessage(),
						Toast.LENGTH_LONG).show();
				Log.d("Main.displayTimeline",""+ex.getMessage());
			}
			
		} else {
			Toast.makeText(this, "Not Verified", Toast.LENGTH_LONG).show();
		}
	}
}