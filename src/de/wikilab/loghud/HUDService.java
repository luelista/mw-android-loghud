package de.wikilab.loghud;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Toast;

public class HUDService extends Service {

	// These are the actions for the service (name are descriptive enough)
	public static final String		ACTION_START = "LogHud.Forwarder.START";
	public static final String		ACTION_STOP = "LogHud.Forwarder.STOP";
	
//HUD
    HUDView mView;
    HUDView2 mView2;

    
// LogCat	
	private int mType;
	private boolean mStatus  = false, threadKill=false;

    boolean scrollmode;
	
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();


		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        scrollmode = prefs.getBoolean("scrollmode", false);
        
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        
        
        //Toast.makeText(getBaseContext(),"onCreate", Toast.LENGTH_LONG).show();
        mView = new HUDView(this);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                //0,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                //| WindowManager.LayoutParams.FLAG_
	           |   WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
	           
                
/*
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
*/
                
//	                      | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.setTitle("Load Average");
        
        wm.addView(mView,  params);
        

        if (scrollmode) {
	        mView2 = new HUDView2(this);
	        mView2.viewRef = mView;
	        WindowManager.LayoutParams params2 = new WindowManager.LayoutParams(
	                60, 220, 0, 0,
	                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
	                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
	                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
	                        PixelFormat.TRANSLUCENT);
	        params2.gravity = Gravity.LEFT | Gravity.TOP;
	        params2.setTitle("Load Average");
	        wm.addView(mView2, params2);
        }
        
        
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(getBaseContext(),"Log HUD Service Destroyed", Toast.LENGTH_LONG).show();
        if(mView != null) {
            ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(mView);
            mView = null;
        }
        if(mView2 != null) {
            ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(mView2);
            mView2 = null;
        }
    }
	
    

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.i("Logger","Service started with intent=" + intent);
		
		if (intent.getAction() == null) {
			//do nothing, and don't crash!
			
		// Do an appropriate action based on the intent.
		} else if (intent.getAction().equals(ACTION_STOP) == true) {
			requestKill();
			stopSelf();

			//Helper.cancelOngoing(this);
			
		} else if (intent.getAction().equals(ACTION_START) == true) {
			mType = 0; // 0 = logcat  1 = dmesg

			Thread thr = new Thread(worker);
			thr.start();
			
			//Helper.showOngoing(this);
		}
	}


 
	Runnable worker = new Runnable() {
		public void run() {
			runLog();
			mStatus = true;
			Log.d("Logger", "status... " + mStatus);
			return;
		}
	};

	private void runLog() {
		Process process = null;
		final String mBuffer = "main";
		//TraceClientHandler c = new TraceClientHandler();
		/*
		try {
			traceServer = new ServerSocket(10777, 5);
			traceServer.setReuseAddress(true);
			
			c.client = traceServer.accept();
			Log.i("Logger", "TraceSubscriber connected from" + c.client.getRemoteSocketAddress().toString());
			clientSubscribed(c.client.getRemoteSocketAddress().toString());
			c.sender = new OutputStreamWriter(c.client.getOutputStream());
			c.sender.write("Register: android logcat2trace");
			
		} catch (IOException e) {
			communicate(MSG_LOG_FAIL);
			e.printStackTrace();
		}*/

		try {

			if (mType == 0) {
				process = Runtime.getRuntime().exec("/system/bin/logcat -b " + mBuffer);
			} else if (mType == 1) {
				process = Runtime.getRuntime().exec("dmesg -s 1000000");
			}

		} catch (IOException e) {
			//communicate(MSG_LOG_FAIL);
		}

		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			String line;

			String marker = String.valueOf(SystemClock.uptimeMillis());
			Log.w("LOGHUDMARKER", marker);
			
			boolean throwAway = true;
			int throwAwayCounter = 0;
			
			while (!killRequested()) {
				line = reader.readLine();
				
				if (throwAway) {
					throwAwayCounter++;
					if (line.startsWith("W/LOGHUDMARKER")) {
						if (line.contains(marker)) {
							throwAway = false;
							Log.i("Logger", "throwAwayCounter = " + throwAwayCounter);
						}
					}
				} else {
					mView.OnLogLine(line);
					//c.sender.write("Trace:dump|Android log|" + line.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r").replace("|", "\\|") + "|\r\n");
					//c.sender.flush();
				}
				//if (mLines == MAX_LINES) {
				//	mScrollback.removeElementAt(0);
				//}

				//mScrollback.add(line);
				//mLines++;
			}

			//traceServer.close();
			//c.client.close();
			Log.i("Logger", "Prepping thread for termination");
			reader.close();
			process.destroy();
			process = null;
			reader = null;
		} catch (IOException e) {
			e.printStackTrace();
			//communicate(MSG_READ_FAIL);
		}

		Log.d("Logger", "Exiting thread...");
		return;
	}

	private synchronized void requestKill() {
		threadKill = true;
		/*try {
			//traceServer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}

	private synchronized boolean killRequested() {
		return threadKill;
	}

}
