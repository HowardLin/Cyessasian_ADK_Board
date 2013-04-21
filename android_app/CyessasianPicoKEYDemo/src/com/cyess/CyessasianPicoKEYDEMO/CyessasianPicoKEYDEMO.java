/*
 * Copyright 2013 CYESS Technology CO., Ltd
http://www.cyess.com

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.cyess.CyessasianPicoKEYDEMO;

import com.cyess.CyessasianPicoKEYDEMO.USBAccessoryManager;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class CyessasianPicoKEYDEMO extends Activity {

	private final static int USBAccessoryWhat			= 0;
	public static final int UPDATE_LED_SETTING		 	= 1;
	public static final int PUSHBUTTON_STATUS_CHANGE	= 2;
	public static final int POT_STATUS_CHANGE			= 3;
	public static final int GET_TEMPERATURE				= 4;
	public static final int UPDATE_TEMPERATURE			= 5;
	public static final int COMMAND_UPDATE_PWM			= 6;
	public static final int GET_G_DATA					= 7;
	public static final int UPDATE_G_DATA				= 8;
	public static final int APP_CONNECT					= (int)0xFE;
	public static final int APP_DISCONNECT				= (int)0xFF;
	
	public static final int BUTTON_1_PRESSED			= 0x01;
	public static final int BUTTON_2_PRESSED			= 0x02;
	public static final int BUTTON_3_PRESSED			= 0x04;
	public static final int BUTTON_4_PRESSED			= 0x08;
	
	private boolean deviceAttached = false;
	
	private USBAccessoryManager accessoryManager; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cyessasian_pico_keydemo);
		
		accessoryManager = new USBAccessoryManager(handler, USBAccessoryWhat);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater()
				.inflate(R.menu.activity_cyessasian_pico_keydemo, menu);
		return true;
	}
	 @Override
	    public void onResume() {
	    	super.onResume();
	    	USBAccessoryManager.RETURN_CODES result = accessoryManager.enable(this, getIntent());
	    }
	    
	 @Override
	    public void onSaveInstanceState(Bundle savedInstanceState) {
	    	if(deviceAttached == false){
	    		return;
	    	}
			//Call the super function that we are over writing now that we have saved our data.
			super.onSaveInstanceState(savedInstanceState);
	    }
	 
	    @Override
	    public void onPause() {
	    	byte[] commandPacket = new byte[4];
			commandPacket[0] = (byte) APP_DISCONNECT;
			commandPacket[1] = 0;
			accessoryManager.write(commandPacket);	
			try {
				while(accessoryManager.isClosed() == false) {
					Thread.sleep(2000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    	super.onPause();
	    	disconnectAccessory();
	    }
	    
	    /** Resets the demo application when a device detaches 
	     */
	    public void connectAccessory() {
	    	if (!accessoryManager.isConnected())
	    		accessoryManager.enable(this, getIntent());
	    }
	    
	    /** Resets the demo application when a device detaches 
	     */
	    public void disconnectAccessory() {
	    	TextView view;
	    	view = (TextView) findViewById(R.id.deviceStatus);
	    	view.setText("Device not connected.");
			updateButton(R.id.textBtn_1,false);
			updateButton(R.id.textBtn_2,false);
			updateButton(R.id.textBtn_3,false);
			updateButton(R.id.textBtn_4,false);
	    	accessoryManager.disable(this);
	    	finish();
	    }
	    
	    /** 
	     * Handler for receiving messages from the USB Manager thread or
	     *   the LED control modules
	     */
	    private Handler handler = new Handler() {
	    	@Override
	    	public void handleMessage(Message msg) {
	    		TextView view;
	    		byte[] commandPacket = new byte[4];
	    		
				switch(msg.what)
				{				
				case USBAccessoryWhat:
					switch(((USBAccessoryManagerMessage)msg.obj).type) {
					case READ:
						if(accessoryManager.isConnected() == false) {
							return;
						}
						while(true) {

							if(accessoryManager.available() <4) {
								//All of our commands in this example are 2 bytes.  If there are less
								//  than 2 bytes left, it is a partial command
								break;
							}
						
							accessoryManager.read(commandPacket);

							switch(commandPacket[0]) {
							
							case PUSHBUTTON_STATUS_CHANGE:
								
				    			updateButton(R.id.textBtn_1, ((commandPacket[1] & BUTTON_1_PRESSED) == BUTTON_1_PRESSED)?true:false);
				    			updateButton(R.id.textBtn_2, ((commandPacket[1] & BUTTON_2_PRESSED) == BUTTON_2_PRESSED)?true:false);
				    			updateButton(R.id.textBtn_3, ((commandPacket[1] & BUTTON_3_PRESSED) == BUTTON_3_PRESSED)?true:false);
				    			updateButton(R.id.textBtn_4, ((commandPacket[1] & BUTTON_4_PRESSED) == BUTTON_4_PRESSED)?true:false);
				    			break;
							}
							
						}
						break;
					case CONNECTED:
						connectAccessory();
						break;
					case READY:
						
						String model =  ((USBAccessoryManagerMessage)msg.obj).accessory.getModel();
						String version = ((USBAccessoryManagerMessage)msg.obj).accessory.getVersion();
						String designedby =((USBAccessoryManagerMessage)msg.obj).accessory.getManufacturer();
						String serial = ((USBAccessoryManagerMessage)msg.obj).accessory.getSerial();
								
						view = (TextView) findViewById(R.id.deviceStatus);
				    	view.setText("Device connected.");
				    	
						view = (TextView) findViewById(R.id.textModel);
						view.setText(view.getText()+model);

						view = (TextView) findViewById(R.id.textVersion);
						view.setText(view.getText()+"v" + version); 
						
						view = (TextView) findViewById(R.id.textDesignedBy);
						view.setText(view.getText()+ designedby); 
						
						view = (TextView) findViewById(R.id.textSerial);
						view.setText(view.getText() + serial); 
						
						deviceAttached = true ;
						
						commandPacket[0] = (byte) APP_CONNECT;
						commandPacket[1] = 0;
						accessoryManager.write(commandPacket);
						
						break;
					case DISCONNECTED:
						disconnectAccessory();
						break;
					}				
					
	   				break;
	   			
				default:
					break;
				}	//switch
	    	} //handleMessage
	    }; //handler
	    
	    private void updateButton(int id, boolean pressed) {
			TextView textviewToUpdate;
			LinearLayout layoutToUpdate;
			
			textviewToUpdate = (TextView)findViewById(id);
			layoutToUpdate = (LinearLayout)textviewToUpdate.getParent();
			
			if(pressed)
			{
				textviewToUpdate.setText(R.string.Pressed);
				textviewToUpdate.setBackgroundResource(R.color.orange);	
			} else {
				textviewToUpdate.setText(R.string.NotPressed);
				textviewToUpdate.setBackgroundResource(0);
			}
	    }
	    private boolean isButtonPressed(int id)
	    {
			TextView buttonTextView;
			String buttonText;
			
			buttonTextView = ((TextView)findViewById(id));
			buttonText = buttonTextView.getText().toString();
			return buttonText.equals(getString(R.string.Pressed));
	    }
}
