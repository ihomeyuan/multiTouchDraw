package com.model;

import java.util.ArrayList;

import android.util.Log;

public class OneFingerDataFactory {
	private ArrayList<OneFingerData> ofdList;
	private PointFactory pf;
	
	public OneFingerDataFactory(PointFactory pf){
		this.pf = pf;
		ofdList = new ArrayList<OneFingerData>();
	}
	
	public synchronized OneFingerData takeOneFingerData(int newId){
		if(ofdList.size() != 0){
			OneFingerData ofd = ofdList.get(0);
			ofdList.remove(0);
			ofd.setCreateTime(System.currentTimeMillis());
			ofd.setmId(newId);
			return ofd;
		}
		//Log.d("multi","created new fingerdata");
		return new OneFingerData(pf);
	}
	
	public synchronized void returnOneFingerData(OneFingerData ofd){
		ofd.reset();
		ofdList.add(ofd);
	}

}
