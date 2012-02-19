package com.aman.samples.twitter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TwitterSignIn extends Activity{

	WebView webView;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.twitter_sign_in_layout);
		Bundle receivedData = getIntent().getExtras();
		String urlToLoad = receivedData.getString("urlToOAuth");
		
		Log.d("INSIDE WEBVIEW","INSIDE");
		
		webView = (WebView)findViewById(R.id.twitter_sign_in_webview);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new CustomWebViewClient());
		webView.loadUrl(urlToLoad);
		
		
	}
	
	private class CustomWebViewClient extends WebViewClient {
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	        Log.d("LOADING..",""+""+url);
	    	view.loadUrl(url);
	        return true;
	    }
	    
	    public void onPageStarted(WebView view, String url, Bitmap favicon){
	    	Log.d("LOADING URL:",""+url);
    		Uri uri = Uri.parse(url);
    		String verifier = uri.getQueryParameter("oauth_verifier");
	    	if(null != verifier){
	    		view.stopLoading();
	    		Log.d("VERIFIER FOUND","FINISHING SIGN-IN ACTIVITY");
	    		finishActivity(verifier);
	    	}else{
	    		String isDenied = uri.getQueryParameter("denied");
	    		if(null != isDenied){
	    			view.stopLoading();
		    		Log.d("Denied","FINISHING SIGN-IN ACTIVITY");
	    			finishActivity("denied");
	    		}
	    	}
	    }
	}
	
	

	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
	        webView.goBack();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	public void finishActivity(String verifier){
    		Log.d("VERIFIER",""+verifier);
	    	Intent resultIntent = new Intent();
	    	resultIntent.putExtra("verifier",verifier);
	    	setResult(RESULT_OK,resultIntent);
	    	finish();
	}
}
