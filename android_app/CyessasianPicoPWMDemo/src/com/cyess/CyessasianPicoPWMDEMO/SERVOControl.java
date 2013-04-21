package com.cyess.CyessasianPicoPWMDEMO;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.SeekBar;

public class SERVOControl extends SeekBar implements SeekBar.OnSeekBarChangeListener {
	private Handler uiHandler;
	private int mPort;
	
	public SERVOControl(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init();
	}
	
	public SERVOControl(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();
	}

	private void init() {

		setOnSeekBarChangeListener(this);
		uiHandler = null;
	}
	
	public void setHandler(Handler handler) {
		uiHandler = handler;
	}
	
	public void setPort(int port) {
		mPort = port;
		setMax(125);
		setProgress(getMax() / 2);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		int what;
		int value;
		what = CyessasianPicoPWMDEMO.COMMAND_UPDATE_PWM;
		value =getMax()+progress*4;
    
	Message ledUpdate = Message.obtain(uiHandler, what, mPort, value);
		
		if(uiHandler != null)
			uiHandler.sendMessage(ledUpdate);

	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}

	public int getState() {
		return getProgress();
	}
	
	public void setState(int progress) {
		setProgress(progress);
	}
}
