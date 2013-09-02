package com.model;

import java.util.ArrayList;

import android.graphics.Point;
import android.util.Log;

public class PointFactory {
	private ArrayList<Point> points;
	
	public PointFactory(){
		points = new ArrayList<Point>();
	}
	
	public synchronized Point takePoint(){
		if(points.size() != 0){
			Point p = points.get(0);
			points.remove(0);
			p.set(0, 0);
			return p;
		}
		//Log.d("multi","created new point");
		return new Point();
	}
	
	public synchronized void returnPoint(Point p){
		points.add(p);
	}

}
