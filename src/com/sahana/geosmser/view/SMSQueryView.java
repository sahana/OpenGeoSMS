package com.sahana.geosmser.view;

import java.util.EventListener;

import com.OpenGeoSMS.writer.IWriter;
import com.OpenGeoSMS.writer.OnWriteCode;
import com.OpenGeoSMS.writer.SMSWriter;
import com.OpenGeoSMS.writer.IWriter.OnWriteListener;
import com.sahana.geosmser.R;
import com.sahana.geosmser.GeoSMSPack;
import com.sahana.geosmser.GeoSMSPackFactory;
import com.sahana.geosmser.MainAct;
import com.sahana.geosmser.view.SMSDeliveryView.ISMSDeliveryRenderer;
import com.sahana.geosmser.widget.AutoCompleteSMSTextView;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.AdapterView.OnItemClickListener;

public class SMSQueryView extends RelativeLayout {
    private Context baseContext;
	
	private String contactNumber;
	public SMSWriter smsWriter;
	
	private AutoCompleteSMSTextView autoedtPhoneNumber;
	private Button btnSMSSend;
	
	public int sendingCode;
	private Handler hanMessageSent;
	private GeoSMSPack currPack = null;
	
	public SMSQueryView(Context context) {
		super(context);
		baseContext = context;
	}
	
	// called when inflating
	public SMSQueryView(Context context, AttributeSet attrs) {
		super(context, attrs);
		baseContext = context;
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		init();
		bindInflatedComponent();
	}
	
	@Override
	protected void onAttachedToWindow() {
		initEvents();
		updateFieldInfo();
		updateSendSMSButtonState();
		super.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		super.onRestoreInstanceState(state);
	}
	
	
	private void init() {
		sendingCode = 0;
		contactNumber = "";
		smsWriter =  new SMSWriter(baseContext);
	}
	
	protected void bindInflatedComponent() {
		autoedtPhoneNumber = (AutoCompleteSMSTextView) findViewById(R.id.SMSQueryPhoneNumber);
		btnSMSSend = (Button) findViewById(R.id.SMSQuerySend);
	}
	
	private void initEvents() {
		smsWriter.setOnWriteListener(new EvtSMSWriterOnWrittenListener());
		autoedtPhoneNumber.setOnItemClickListener(new EvtOnItemClickAutoedtPhoneNumber());
		autoedtPhoneNumber.addTextChangedListener(new DelContactTextWatcher());
		btnSMSSend.setOnClickListener(new EvtOnClickSMSQuery());
	}
	

	public String getPhoneFieldText() {
		return autoedtPhoneNumber.getText().toString();
	}
	
	public void clearInputField() {
		autoedtPhoneNumber.setText("");
	}
	
	private void updateFieldInfo() {
		contactNumber = autoedtPhoneNumber.getText().toString();
	}
	
	private void updateSendSMSButtonState() {
		boolean enable = false;
		
		if(contactNumber.matches("\\d{10,}") || contactNumber.matches(".*<\\d{10,}>")) {
			enable = true;
		}
		btnSMSSend.setEnabled(enable);
	}
	
	public void setMessageSentHandler(Handler handler) {
		hanMessageSent = handler;
	}
	
	private void handleDialog(int code) {
		if(hanMessageSent != null) {
			Message msg = hanMessageSent.obtainMessage(code);
			hanMessageSent.sendMessage(msg);
		}
	}
	
	public void setCurrGeoSMSPack(GeoSMSPack pack) {
	    this.currPack = pack;
	}
	
	public class EvtOnClickSMSQuery implements OnClickListener {
		@Override
		public void onClick(View v) {
		    sendingCode = 0;
            btnSMSSend.setEnabled(false);
            handleDialog(MainAct.DIALOG_SMS_QUERY_MESSAGESENDING);
            if (currPack != null) {
                smsWriter.write(currPack, autoedtPhoneNumber.getValue());
            } else {
                smsWriter.write(GeoSMSPackFactory.createQueryPack(), autoedtPhoneNumber.getValue());
            }
			//Toast.makeText(baseContext, "testing", Toast.LENGTH_SHORT).show();
		}
	}
	
	private class EvtOnItemClickAutoedtPhoneNumber implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if(baseContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				autoedtPhoneNumber.isClicked = true;
				autoedtPhoneNumber.dismissDropDown();
			}
		}
	}
	
	
	private class DelContactTextWatcher implements TextWatcher {
		@Override
		public void afterTextChanged(Editable e) {
			updateFieldInfo();
			updateSendSMSButtonState();
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }
	}
	
	private class EvtSMSWriterOnWrittenListener implements OnWriteListener {
		@Override
		public void onSent(IWriter writer, int code, Object target) {
			switch(code) {
				case OnWriteCode.SUCCESS:
					sendingCode = 1;
					break;
				default: 
					sendingCode = -1;
			}
		}
	}
	
}