package com.aman.samples.ksoap;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class Main extends Activity {
	
	private static final String SOAP_ACTION = "http://footballpool.dataaccess.eu";
	private static final String OPERATION_NAME = "TopGoalScorers";
	private static final String WSDL_TARGET_NAMESPACE = "http://footballpool.dataaccess.eu";
	private static final String SOAP_ADDRESS = "http://footballpool.dataaccess.eu/data/info.wso?WSDL";
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        SoapObject soapRequest = new SoapObject(WSDL_TARGET_NAMESPACE, OPERATION_NAME) ;
        soapRequest.addProperty("appName","7-Laugh" );
        soapRequest.addProperty("sEmail","test@example.com" );
        soapRequest.addProperty("sPassword","test" );
        SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        soapEnvelope.dotNet  = true;
        soapEnvelope.setOutputSoapObject(soapRequest);
        
        HttpTransportSE httpTransport = new HttpTransportSE(SOAP_ADDRESS);
//        httpTransport.debug = true;
        
        try
        
        {
         
        httpTransport.call(SOAP_ACTION, soapEnvelope);
         
        Object response = soapEnvelope.getResponse();
         
        Log.d("-----RESPONSE",""+response);
         
        }
         
        catch (Exception exception)
         
        {
        Log.d("RESPONSE",""+exception.toString());
         
        }
        
    }
}