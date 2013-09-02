package com.model;

import java.util.ArrayList;

import android.graphics.Point;

public class OneFingerData {
	private PointFactory pf;
	private ArrayList<Point> pointData;
	private int mId;
	private int color;
	private long createTime;
	
	public OneFingerData(PointFactory pf){
		this.pf = pf;
		pointData = new ArrayList<Point>();
		setmId(-1);
		color = 0;
		createTime = System.currentTimeMillis();
	}

	public int getmId() {
		return mId;
	}

	public void setmId(int mId) {
		this.mId = mId;
	}

	public void addPoint(int x, int y){
		Point p = pf.takePoint();
		p.set(x, y);
		pointData.add(p);
	}
	
	public ArrayList<Point> getPointData() {
		return pointData;
	}
	
	public void reset(){
		returnAllPoints();
		setmId(-1);
		color = 0;
	}
	
	public int getColor(){
		return color;
	}
	
	public void setColor(int c){
		color = c;
	}

	private void returnAllPoints(){
		for(int i = 0; i < pointData.size(); i++){
			pf.returnPoint(pointData.get(i));
		}
		pointData.clear();
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

}
