/**
 * Quelle des Originalcodes:
 * http://stackoverflow.com/questions/4481226/creating-a-system-overlay-always-on-top-button-in-android
 * 
 */

package de.wikilab.loghud;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.Toast;


class HUDView extends ViewGroup {
    private Paint mLoadPaint,mLoadPaint2;
    private Paint mBgE,mBgW,mBgI,mBgDef,mBgHead;
    
    public int bufferLength = 60;
    
    public String[] buffer = new String[bufferLength];
    public int bufferOffset = 0;
    public int scrollOffset = 0;
    
    
    public int fontSize = 10;
    public boolean scrollmode = false, transparentmode = false;
    
    public boolean killThread = false;
    
    public Handler handler = new Handler();
    public Runnable invalidateAction = new Runnable() {
		
		@Override
		public void run() {
			HUDView.this.invalidate();
		}
	};
    
	public void invalidateThreadSafe() {
		handler.post(invalidateAction);
	}
	
    //public ReentrantLock bufferLock = new ReentrantLock();
    //public Condition  
    
    public HUDView(Context context) {
        super(context);
        //Toast.makeText(getContext(),"HUDView", Toast.LENGTH_LONG).show();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        fontSize = prefs.getInt("fontsize", 10)+1;
        scrollmode = prefs.getBoolean("scrollmode", false);
        transparentmode = prefs.getBoolean("transparent", false);
        
        if (scrollmode) {
			buffer = new String[100000];
			bufferOffset = 0;
		}
        
        mLoadPaint = new Paint();  iniFont(mLoadPaint);  mLoadPaint.setARGB(255, 255, 255, 255);
        
        mLoadPaint2 = new Paint(); iniFont(mLoadPaint2); mLoadPaint2.setARGB(255, 255, 255, 255);

        mBgE = new Paint();		iniFont(mBgE);    mBgE.setARGB(100, 255, 0, 0);
        mBgW = new Paint();		iniFont(mBgW);    mBgW.setARGB(100, 255, 255, 0);
        mBgI = new Paint();		iniFont(mBgI);    mBgI.setARGB(100, 0, 0, 255);
        mBgDef = new Paint();	iniFont(mBgDef);  mBgDef.setARGB(100, 155, 155, 155);
        mBgHead = new Paint();	iniFont(mBgHead); mBgHead.setARGB(222, 33, 33, 33);
    }
    
    void iniFont(Paint ppp) {
        ppp.setAntiAlias(true);
        ppp.setTextSize(fontSize-1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        synchronized (buffer) {
	    	superDraw2(canvas, 
	    			"*** LOG HUD *** LIVE LOGCAT OUTPUT *** android.wiki-lab.net *** Count: "
	    			+ String.valueOf(bufferOffset) + " *** "
	    			+ (bufferOffset==0?"":String.valueOf((int)(scrollOffset*100/bufferOffset)) + "%"),
	    			5, 10);
	    	
	    	if (!scrollmode) {
		        for(int i = 0; i < bufferOffset; i++) {
	    			if (transparentmode) {
	    				superDraw(canvas, buffer[i], 3, 25+i*fontSize);
	    			} else {
	    				superDraw2(canvas, buffer[i], 3, 25+i*fontSize);
	    			}
		        }
	    	} else {
	    		int rowsToPrint = Math.min(scrollOffset,bufferLength);
	    		int delta = Math.max(0,scrollOffset-bufferLength);
	    		for(int i = 0; i < rowsToPrint; i++) {
	    			if (transparentmode) {
	    				superDraw(canvas, buffer[delta+i], 3, 25+i*fontSize);
	    			} else {
	    				superDraw2(canvas, buffer[delta+i], 3, 25+i*fontSize);
	    			}
		        	
		        }
	    	}
		}
    }

    private void superDraw2(Canvas canvas, String txt, int x, int y) {
    	if (txt.startsWith("E")) {
    		canvas.drawRect(x, y, x+600, y+fontSize-1, mBgE);
    	} else if (txt.startsWith("W")) {
    		canvas.drawRect(x, y, x+600, y+fontSize-1, mBgW);
    	} else if (txt.startsWith("I")) {
    		canvas.drawRect(x, y, x+600, y+fontSize-1, mBgI);
    	} else if (txt.startsWith("***")) {
    		canvas.drawRect(x, y, x+600, y+fontSize-1, mBgHead);
    	} else {
    		canvas.drawRect(x, y, x+600, y+fontSize-1, mBgDef);
    	}
    	canvas.drawText(txt, x+2,y+fontSize-3, mLoadPaint);
    }
    
    
    private void superDraw(Canvas canvas, String txt, int x, int y) {
    	Paint paint2 = mBgDef;
    	if (txt.startsWith("E")) {
    		paint2 = mBgE;
    	} else if (txt.startsWith("W")) {
    		paint2 = mBgW;
    	} else if (txt.startsWith("I")) {
    		paint2 = mBgI;
    	} else if (txt.startsWith("***")) {
    		paint2 = mBgHead;
    	}
    	y+=fontSize;
    	canvas.drawText(txt, x,   y  , paint2);
    	canvas.drawText(txt, x,   y+1, paint2);
    	canvas.drawText(txt, x,   y+2, paint2);
    	canvas.drawText(txt, x+1, y  , paint2);
    	//canvas.drawText(txt, x+1,   y+1, mLoadPaint2);
    	canvas.drawText(txt, x+1, y+2, paint2);
    	canvas.drawText(txt, x+2, y  , paint2);
    	canvas.drawText(txt, x+2, y+1, paint2);
    	canvas.drawText(txt, x+2, y+2, paint2);

    	canvas.drawText(txt, x+1, y+1, mLoadPaint);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    	synchronized (buffer) {
			bufferLength = Math.max(20, (int)((b-t)/fontSize) - 2);
    		if (!scrollmode) {
    			buffer = new String[bufferLength];
    			bufferOffset = 0;
    		}
    	}
    }


	public void OnLogLine(String line) {
		synchronized (buffer) {
			
			buffer[bufferOffset++] = line;
			if (bufferOffset - 1 == scrollOffset) scrollOffset++;
			if (scrollmode) {
				bufferOffset %= 100000;
			} else {
				bufferOffset %= bufferLength;
			}
		}
		invalidateThreadSafe();
	}
	
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //return super.onTouchEvent(event);
        //Toast.makeText(getContext(),"onTouchEvent", Toast.LENGTH_LONG).show();
    	
    	//wird nicht aufgerufen solange nicht 
    	//WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
    	//gesetz wird
    	
    	//if (event.getX() < 50) return false;
    	Log.i("onTouchEvent", String.valueOf(event.getX()) + "|" + String.valueOf(event.getY()));
    	
        return false;
    }
}

