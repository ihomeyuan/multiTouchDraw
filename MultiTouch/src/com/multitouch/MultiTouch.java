package com.multitouch;

import java.util.ArrayList;

import com.model.*;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams; 

public class MultiTouch extends Activity {
	private DrawPnl    drawPnl; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  
				WindowManager.LayoutParams.FLAG_FULLSCREEN);  

		drawPnl = new DrawPnl(this); 
		addContentView(drawPnl, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)); 
	}

	private class DrawPnl extends SurfaceView implements SurfaceHolder.Callback{
		private static final String TAG = "Multi";
		private DrawThread drawThread;

		private OneFingerDataFactory ofdFactory;
		private OneFingerData[] cDrawing;
		private ArrayList<OneFingerData> drawList;

		private static final int MAX_POINTERS = 10;
		private int cPointers = 0;
		private Paint color[];
		private static final long DRAW_TIME = 3000;

		public DrawPnl(Context context) { 
			super(context); 
			getHolder().addCallback(this); 
			color = new Paint[10];
			int r, g, b = 0;
			for(int i = 0; i < color.length; i++){
				r = (int)(Math.random() * 200);
				g = (int)(Math.random() * 200);
				b = (int)(Math.random() * 200);
				color[i] = new Paint();
				color[i].setColor(Color.rgb(r, g, b));
			}

			PointFactory pf = new PointFactory();
			ofdFactory = new OneFingerDataFactory(pf);
			drawList = new ArrayList<OneFingerData>();
			cDrawing = new OneFingerData[MAX_POINTERS];
			drawThread = new DrawThread(getHolder());
			drawThread.setRunning(true); 
		}

		@Override
		public boolean onTouchEvent(MotionEvent e) { 
			synchronized (drawThread.getSurfaceHolder()) { 
				try{
					if(cPointers != e.getPointerCount())
						emptyCurrentList(e);
					final int action = e.getAction(); 
					switch (action & MotionEvent.ACTION_MASK) { 
					case MotionEvent.ACTION_DOWN: { 
						emptyCurrentList(e);
						break; 
					} 
					case MotionEvent.ACTION_MOVE: { 
						for(int i = 0; i < cPointers; i++)
							cDrawing[e.getPointerId(i)].addPoint((int)e.getX(e.getPointerId(i)), (int)e.getY(e.getPointerId(i)));
						break; 
					} 
					case MotionEvent.ACTION_UP: { 
						emptyCurrentList(e);
						break; 
					} 
					case MotionEvent.ACTION_CANCEL: { 
						emptyCurrentList(e);
						break; 
					} 
					} 
				}
				catch(NullPointerException ignore){
					Log.e(TAG, "nullpointer");
				}
				catch(IllegalArgumentException ignore2){
					Log.e(TAG, ignore2.getMessage());

				}
				return true; 
			} 
		}

		private void emptyCurrentList(MotionEvent e){
			for(int i = 0; i < cDrawing.length; i++){
				if(cDrawing[i] != null){
					//Log.d(TAG, "added with id: " + cDrawing[i].getmId() + " to drawList");
					drawList.add(cDrawing[i]);
					cDrawing[i] = null;
				}
			}
			cPointers = e.getPointerCount();
			if(cPointers > MAX_POINTERS)
				cPointers = MAX_POINTERS;
			for(int i = 0; i < cPointers; i++){
				cDrawing[e.getPointerId(i)] = ofdFactory.takeOneFingerData(e.getPointerId(i));
				cDrawing[e.getPointerId(i)].setColor(cPointers);
			}
		}

		//@Override
		public void doDraw(Canvas canvas) { 
			canvas.drawColor(Color.WHITE);
			try{
				for(int i = drawList.size() - 1; i >= 0; i--){
					if(drawList.get(i).getCreateTime() + DRAW_TIME < System.currentTimeMillis()){
						//Log.d(TAG, "removed with id:" + drawList.get(i).getmId() + " from drawList");
						ofdFactory.returnOneFingerData(drawList.get(i));
						drawList.remove(i);
					}
				}
				for(int i = 0; i < drawList.size(); i++){
					float a1 = (float)(System.currentTimeMillis() - drawList.get(i).getCreateTime() + DRAW_TIME);
					a1 = a1 / (float)DRAW_TIME;
					a1 *= 255;
					a1 = 255 - a1;
					for(int j = 0; j < drawList.get(i).getPointData().size() - 1; j++){
						Point start = drawList.get(i).getPointData().get(j);
						Point end = drawList.get(i).getPointData().get(j + 1);
						color[drawList.get(i).getColor()].setAlpha((int)a1);
						canvas.drawLine(start.x, start.y, end.x, end.y, color[drawList.get(i).getColor()]);
					}
				}
			}
			catch(IndexOutOfBoundsException e){
				e.printStackTrace();
			}

		}

		@Override
		public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
			// TODO Auto-generated method stub
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			if(drawThread.getState() == Thread.State.NEW) 
				drawThread.start(); 
			else if(drawThread.getState() == Thread.State.TERMINATED){ 
				drawThread = new DrawThread(getHolder()); 
				drawThread.setRunning(true); 
				drawThread.start(); 
			} 

		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			boolean retry = true; 
			drawThread.setRunning(false); 
			while (retry) { 
				try { 
					drawThread.join(); 
					retry = false; 
				} catch (InterruptedException e) { 
					// we will try it again and again... 
				} 
			} 

		}

		/************************************************************************************** 
		 * The thread that draws the screen 
		 **************************************************************************************/
		private class DrawThread extends Thread{ 
			private SurfaceHolder    surfaceHolder; 
			private boolean    run = false; 

			public DrawThread(SurfaceHolder surfaceHolder) { 
				this.surfaceHolder = surfaceHolder; 
			} 

			public void setRunning(boolean run) { 
				this.run = run; 
			} 

			public SurfaceHolder getSurfaceHolder() { 
				return surfaceHolder; 
			} 

			@Override
			public void run() { 
				Canvas c; 
				while (run) { 
					c = null; 
					try { 
						c = surfaceHolder.lockCanvas(null); 
						synchronized (surfaceHolder) { 
							//drawPnl.onDraw(c); 
							drawPnl.doDraw(c); 
						} 
					} finally { 
						if (c != null) { 
							surfaceHolder.unlockCanvasAndPost(c); 
						} 
					} 
				} 
			} 
		}
	}
}
