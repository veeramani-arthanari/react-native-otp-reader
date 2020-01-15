
package com.reactlibrary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.auth.api.phone.*;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import android.content.pm.PackageManager;
import java.util.ArrayList;

public class RNOtpReaderModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private OTPSMSBroadcastRetriever otpsmsBroadcastRetriever;
    public static ReactApplicationContext staticContext;

    public RNOtpReaderModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        staticContext = reactContext;
    }

    @Override
    public String getName() {
        return "RNOtpReader";
    }

    @ReactMethod
    public void StartObservingIncomingSMS(final Callback successCallback, final Callback failureCallback) {
        PackageManager pm = this.reactContext.getPackageManager();
        int hasPerm = pm.checkPermission(android.Manifest.permission.READ_SMS, "com.google.android.gms");
        if (hasPerm != PackageManager.PERMISSION_GRANTED) {
            // do stuff
            // Log.d("PackageManager Permission not Granted", "PackageManager Permission not Granted");
            failureCallback.invoke("Unable to observe Incoming SMS");

        } else {
           // Log.d("PackageManager Permission Granted", "PackageManager Permission Granted");
            SmsRetrieverClient client = SmsRetriever.getClient(this.reactContext);
            Task<Void> task = client.startSmsRetriever();
            task.addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    successCallback.invoke("Started Observing Incoming SMS");
                }
            });
            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    failureCallback.invoke("Unable to observe Incoming SMS");
                }
            });
            if (reactContext == null) {
                Log.d("Error", "reactContext is null");
                return;
            } else {
                Log.d("Error", "reactContext is not null");
            }
        }
    }

    @ReactMethod
    public void GenerateHashString(Callback successCallback) {
        AppSignatureHelper appSignatureHelper = new AppSignatureHelper(this.reactContext);
        ArrayList<String> appSignatures = appSignatureHelper.getAppSignatures();
        WritableArray appSignatureList = new WritableNativeArray();
        for (String appSignature : appSignatures) {
            appSignatureList.pushString(appSignature);
        }
        successCallback.invoke(appSignatureList);
    }

    public static class OTPSMSBroadcastRetriever extends BroadcastReceiver {

        public OTPSMSBroadcastRetriever() {
        }

        private void sendMessage(String message) {
            // this.callback.onMessageReceived(message);
            WritableMap payload = Arguments.createMap();
            payload.putString("message", message);
            if (RNOtpReaderModule.staticContext == null) {
                Log.d("Error", "RNOtpReaderModule.staticContext null");
                return;
            }
            staticContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("otpReceived",
                    payload);
        }

        private void sendErrorMessage(String error) {
            WritableMap errorPayload = Arguments.createMap();
            errorPayload.putString("message", "Error");
            staticContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("otpReceived",
                    errorPayload);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("Error", "Recieved message");
            try {
                if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);

                        switch (status.getStatusCode()) {
                        case CommonStatusCodes.SUCCESS:
                            String message = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
                            // Extract one-time code from the message and complete verification
                            // by sending the cOTPSMSBroadcastRetrieverode back to your server.
                            sendMessage(message);
                            break;
                        case CommonStatusCodes.TIMEOUT:
                            sendErrorMessage("Error");
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                Log.d("Error", "OnReceive Exception");
            }
        }
    }
}