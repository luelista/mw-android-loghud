/**
 * Quelle des Originalcodes:
 * http://stackoverflow.com/questions/4481226/creating-a-system-overlay-always-on-top-button-in-android
 * 
 */

package de.wikilab.loghud;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;


class HUDView2 extends ViewGroup {
    
	HUDView viewRef;
	
	Paint mPaint = new Paint(), mPaint2 = new Paint();
    //public ReentrantLock bufferLock = new ReentrantLock();
    //public Condition  
    
    public HUDView2(Context context) {
        super(context);
        //Toast.makeText(getContext(),"HUDView", Toast.LENGTH_LONG).show();
        
		// SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        
        mPaint.setARGB(144, 0, 0, 188); mPaint2.setARGB(255, 255, 255, 255);
		
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0, 0, 50, 70, mPaint);
        canvas.drawRect(0, 80, 50, 150, mPaint);
        canvas.drawRect(0, 160, 50, 220, mPaint);

        canvas.drawLine(  3,  50,  25,  20, mPaint2);
        canvas.drawLine( 47,  50,  25,  20, mPaint2);
        

        canvas.drawLine(  3, 100,  25, 130, mPaint2);
        canvas.drawLine( 47, 100,  25, 130, mPaint2);

        canvas.drawLine(  3, 170,  25, 200, mPaint2);
        canvas.drawLine( 47, 170,  25, 200, mPaint2);
        canvas.drawLine(  3, 180,  25, 210, mPaint2);
        canvas.drawLine( 47, 180,  25, 210, mPaint2);
        
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    	
    }

    private int scrollDir = 0;
    private boolean scrollCancel = false;
    private Runnable scrollThread = new Runnable() {
		
		@Override
		public void run() {
			try {
				int tempo = 90, cc = 0;
				while(!scrollCancel) {
					int nv = viewRef.scrollOffset + scrollDir;
					if (nv > viewRef.bufferOffset || nv < 0) return;
					
					viewRef.scrollOffset =nv;
					
					viewRef.invalidateThreadSafe();
					Thread.sleep(tempo);
					
					if (tempo > 10) tempo--;
					//if (cc++>2 && tempo > 10) {tempo--; cc=0;}
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	Log.i("onTouchEvent", String.valueOf(event.getX()) + "|" + String.valueOf(event.getY()));
    	if (event.getAction() == MotionEvent.ACTION_DOWN) {

        	if (event.getY() < 70) {
        		scrollCancel = false; scrollDir = -1;
        		Thread t  = new Thread(scrollThread);
        		t.start();
        	} else if (event.getY() < 160) {
        		scrollCancel = false; scrollDir = 1;
        		Thread t  = new Thread(scrollThread);
        		t.start();
        	} else {
        		viewRef.scrollOffset = viewRef.bufferOffset;
        	}
        	
    	} else if (event.getAction() == MotionEvent.ACTION_UP) {
    		scrollCancel = true;
    		if (viewRef.scrollOffset == viewRef.bufferOffset && event.getY() < 70) {
    			Intent intent = new Intent(getContext(), ConfigurationActivity.class);
    			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			getContext().startActivity(intent);
    		}
    	}
    	
        return false;
    }
}

