// SMSLib for Java v3
// A Java API library for sending and receiving SMS via a GSM modem
// or other supported gateways.
// Web Site: http://www.smslib.org
//
// Copyright (C) 2002-2012, Thanasis Delenikas, Athens/GREECE.
// SMSLib is distributed under the terms of the Apache License version 2.0
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.smslib.smpp.jsmpp;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.bean.AlertNotification;
import org.jsmpp.bean.Alphabet;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.DataSm;
import org.jsmpp.bean.DeliverSm;
import org.jsmpp.bean.DeliveryReceipt;
import org.jsmpp.bean.ESMClass;
import org.jsmpp.bean.GeneralDataCoding;
import org.jsmpp.bean.MessageClass;
import org.jsmpp.bean.MessageType;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.RegisteredDelivery;
import org.jsmpp.bean.SMSCDeliveryReceipt;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.jsmpp.extra.SessionState;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.DataSmResult;
import org.jsmpp.session.MessageReceiverListener;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.session.Session;
import org.jsmpp.session.SessionStateListener;
import org.jsmpp.util.InvalidDeliveryReceiptException;
import org.smslib.AGateway;
import org.smslib.GatewayException;
import org.smslib.InboundMessage;
import org.smslib.OutboundMessage;
import org.smslib.Service;
import org.smslib.StatusReportMessage;
import org.smslib.TimeoutException;
import org.smslib.Message.MessageEncodings;
import org.smslib.Message.MessageTypes;
import org.smslib.OutboundMessage.FailureCauses;
import org.smslib.OutboundMessage.MessageStatuses;
import org.smslib.StatusReportMessage.DeliveryStatuses;
import org.smslib.helper.Logger;
import org.smslib.notify.InboundMessageNotification;
import org.smslib.smpp.AbstractSMPPGateway;
import org.smslib.smpp.BindAttributes;

/**
 * A gateway that supports SMPP through JSMPP (http://code.google.com/p/jsmpp/).
 * 
 * @author Bassam Al-Sarori
 */
public class JSMPPGateway extends AbstractSMPPGateway {

	private SMPPSession session = null;
	private MessageReceiver messageReceiver=new MessageReceiver();
	private SessionStateListener stateListener=new JSMPPSessionStateListener();
	private BindType bindType;
	private TypeOfNumber bindTypeOfNumber;
	private NumberingPlanIndicator bindNumberingPlanIndicator;
	

	/**
	 * @param id
	 * @param host
	 * @param port
	 * @param bindAttributes
	 */
	public JSMPPGateway(String id, String host, int port,
			BindAttributes bindAttributes) {
		super(id, host, port, bindAttributes);
		
		
		setAttributes(AGateway.GatewayAttributes.SEND | AGateway.GatewayAttributes.CUSTOMFROM | AGateway.GatewayAttributes.BIGMESSAGES | AGateway.GatewayAttributes.FLASHSMS | AGateway.GatewayAttributes.RECEIVE);
		init();
		
	}

	private void init(){
		switch (bindAttributes.getBindType()){
		case RECEIVER:
			bindType=BindType.BIND_RX;
			setInbound(true);
			setOutbound(false);
			break;
		case TRANSMITTER:
			bindType=BindType.BIND_TX;
			setInbound(false);
			setOutbound(true);
			break;
		case TRANSCEIVER:
			bindType=BindType.BIND_TRX;
			setInbound(true);
			setOutbound(true);
			break;
		default:
			IllegalArgumentException illegalArgumentException=new IllegalArgumentException("Unknown BindType "+bindAttributes.getBindType());
			Logger.getInstance().logError(illegalArgumentException.getMessage(), illegalArgumentException, getGatewayId());
			throw illegalArgumentException;
		}
		
		bindTypeOfNumber=TypeOfNumber.valueOf(bindAttributes.getBindAddress().getTypeOfNumber().value());
		bindNumberingPlanIndicator=NumberingPlanIndicator.valueOf(bindAttributes.getBindAddress().getNumberingPlanIndicator().value());
		
		initSession();
	}
	private void initSession(){
		session = new SMPPSession();
		session.addSessionStateListener(stateListener);
		session.setMessageReceiverListener(messageReceiver);
		
	}
	@Override
	public void startGateway() throws TimeoutException, GatewayException,
			IOException, InterruptedException {
		
		if(!session.getSessionState().isBound()){
			if(enquireLink>0){
				session.setEnquireLinkTimer(enquireLink);
			}
			
		session.connectAndBind(host, port, new BindParameter(bindType, bindAttributes.getSystemId(), bindAttributes.getPassword(), bindAttributes.getSystemType(), bindTypeOfNumber, bindNumberingPlanIndicator, null));
		
		  
		}else{
			Logger.getInstance().logWarn("SMPP session already bound.", null, getGatewayId());
		//	throw new GatewayException("Session already bound");
		}
		
	}

	@Override
	public void stopGateway() throws TimeoutException, GatewayException,
			IOException, InterruptedException {
		if(session.getSessionState().isBound()){
			session.removeSessionStateListener(stateListener);
			session.unbindAndClose();
			
			//super.stopGateway();
		}else{
			Logger.getInstance().logWarn("SMPP session not bound.", null, getGatewayId());
			//throw new GatewayException("Session not bound");
		}
		super.stopGateway();
	}

	class MessageReceiver implements MessageReceiverListener{
		 public void onAcceptDeliverSm(DeliverSm deliverSm)
         throws ProcessRequestException {
     if (MessageType.SMSC_DEL_RECEIPT.containedIn(deliverSm.getEsmClass())) {
        
         try {
             DeliveryReceipt delReceipt = deliverSm.getShortMessageAsDeliveryReceipt();
       
             StatusReportMessage statusReportMessage=new StatusReportMessage(delReceipt.getId(),deliverSm.getDestAddress(), deliverSm.getSourceAddr(), delReceipt.getText(),  delReceipt.getSubmitDate(),  delReceipt.getDoneDate());
             
             switch(delReceipt.getFinalStatus()){
             case DELIVRD:
            	 statusReportMessage.setStatus(DeliveryStatuses.DELIVERED);
            	 break;
             case REJECTD:
             case EXPIRED:
             case UNDELIV:
            	 statusReportMessage.setStatus(DeliveryStatuses.ABORTED);
            	 break;
             default:
            	 statusReportMessage.setStatus(DeliveryStatuses.UNKNOWN);
             }
             
             statusReportMessage.setGatewayId(getGatewayId());
             Service.getInstance().getNotifyQueueManager().getNotifyQueue().add(new InboundMessageNotification(getMyself(), MessageTypes.STATUSREPORT, statusReportMessage));
         } catch (InvalidDeliveryReceiptException e) {
        	 Logger.getInstance().logError("Failed getting delivery receipt.", e, getGatewayId());
            
         }
     } else {        
         InboundMessage msg = new InboundMessage(new java.util.Date(), deliverSm.getSourceAddr(), new String(deliverSm.getShortMessage()), 0, null);
 		msg.setGatewayId(JSMPPGateway.this.getGatewayId());
 		if(Alphabet.ALPHA_DEFAULT.value()==deliverSm.getDataCoding()){
       	 msg.setEncoding(MessageEncodings.ENC7BIT);
        }else if(Alphabet.ALPHA_8_BIT.value()==deliverSm.getDataCoding()){
        	msg.setEncoding(MessageEncodings.ENC8BIT);
        }else if(Alphabet.ALPHA_UCS2.value()==deliverSm.getDataCoding()){
        	msg.setEncoding(MessageEncodings.ENCUCS2);
        }else{
        	msg.setEncoding(MessageEncodings.ENCCUSTOM);
        }
 		incInboundMessageCount();
 		Service.getInstance().getNotifyQueueManager().getNotifyQueue().add(new InboundMessageNotification(getMyself(), MessageTypes.INBOUND, msg));
     }
 }
 
 public DataSmResult onAcceptDataSm(DataSm dataSm, Session source)
         throws ProcessRequestException {
	// ignored
     return null;
 }
 
 public void onAcceptAlertNotification(
         AlertNotification alertNotification) {
	 // ignored
 }
	}

	class JSMPPSessionStateListener implements SessionStateListener {
        public void onStateChange(SessionState newState, SessionState oldState,
                Object source) {
        	if(newState.isBound()){
        		if(!getStatus().equals(GatewayStatuses.STARTED)){
        			try {
						JSMPPGateway.super.startGateway();
					} catch (TimeoutException e) {
						Logger.getInstance().logError("Failed starting Gateway.", e, getGatewayId());
						
					} catch (GatewayException e) {
						Logger.getInstance().logError("Failed starting Gateway.", e, getGatewayId());
					} catch (IOException e) {
						Logger.getInstance().logError("Failed starting Gateway.", e, getGatewayId());
					} catch (InterruptedException e) {
						Logger.getInstance().logError("Failed starting Gateway.", e, getGatewayId());
					}
        		}
        	}else if(newState.equals(SessionState.CLOSED)){
        		if(getStatus().equals(GatewayStatuses.STARTED)){
        	
					JSMPPGateway.super.setStatus(GatewayStatuses.RESTART);
				
					initSession();
        		}
        	}
           //System.out.println("State Changed: from "+oldState+" To "+newState);
        }
    }

	@Override
	public boolean sendMessage(OutboundMessage msg) throws TimeoutException,
			GatewayException, IOException, InterruptedException {
		
		Alphabet encoding=Alphabet.ALPHA_DEFAULT;
		
		switch (msg.getEncoding()){
		case ENC8BIT:
			encoding=Alphabet.ALPHA_8_BIT;
			break;
		case ENCUCS2:
			encoding=Alphabet.ALPHA_UCS2;
			break;
		case ENCCUSTOM:
			encoding=Alphabet.ALPHA_RESERVED;
			break;
		}
		GeneralDataCoding dataCoding;
		
		switch(msg.getDCSMessageClass()){
		case MSGCLASS_FLASH:
			dataCoding=new GeneralDataCoding(false, true, MessageClass.CLASS0, encoding);
			break;
		case MSGCLASS_ME:
			dataCoding=new GeneralDataCoding(false, true, MessageClass.CLASS1, encoding);
			break;
		case MSGCLASS_SIM:
			dataCoding=new GeneralDataCoding(false, true, MessageClass.CLASS2, encoding);
			break;
		case MSGCLASS_TE:
			dataCoding=new GeneralDataCoding(false, true, MessageClass.CLASS3, encoding);
			break;
		default:
			dataCoding=new GeneralDataCoding();
			dataCoding.setAlphabet(encoding);
		}
		try {
			final RegisteredDelivery registeredDelivery = new RegisteredDelivery();
	        registeredDelivery.setSMSCDeliveryReceipt((msg.getStatusReport())?SMSCDeliveryReceipt.SUCCESS_FAILURE:SMSCDeliveryReceipt.DEFAULT);
	        
			
			String msgId=session.submitShortMessage(bindAttributes.getSystemType(),
					TypeOfNumber.valueOf(sourceAddress.getTypeOfNumber().value()),
					NumberingPlanIndicator.valueOf(sourceAddress.getNumberingPlanIndicator().value()),
					(msg.getFrom()!=null)?msg.getFrom():getFrom(),
					TypeOfNumber.valueOf(destinationAddress.getTypeOfNumber().value()),
					NumberingPlanIndicator.valueOf(destinationAddress.getNumberingPlanIndicator().value()),
					msg.getRecipient(),
					new ESMClass(),
					(byte)0,
					(byte)msg.getPriority(),
					null,
					formatTimeFromHours(msg.getValidityPeriod()),
					registeredDelivery,
					(byte)0,
					dataCoding,
					(byte)0,
					msg.getText().getBytes());
			msg.setRefNo(msgId);
			msg.setDispatchDate(new Date());
			msg.setGatewayId(getGatewayId());
			msg.setMessageStatus(MessageStatuses.SENT);
			incOutboundMessageCount();
		}catch (PDUException e) {
			msg.setGatewayId(getGatewayId());
			msg.setMessageStatus(MessageStatuses.FAILED);
			msg.setFailureCause(FailureCauses.BAD_FORMAT);
			Logger.getInstance().logError("Message Format not accepted.", e, getGatewayId());
			return false;
		} catch (ResponseTimeoutException e) {
			Logger.getInstance().logError("Message could not be sent.", e, getGatewayId());
			throw new TimeoutException(e.getMessage());
		} catch (InvalidResponseException e) {
			Logger.getInstance().logError("Message could not be sent.", e, getGatewayId());
			throw new IOException("InvalidResponseException: ", e);
		} catch (NegativeResponseException e) {
			Logger.getInstance().logError("Message could not be sent.", e, getGatewayId());
			throw new IOException("NegativeResponseException: ", e);
		}
		
		return true;
	}
	
	private String formatTimeFromHours(int timeInHours){
		if(timeInHours<0){
			return null;
		}
		Calendar cDate=Calendar.getInstance();
		cDate.clear();
		cDate.set(Calendar.YEAR, 0);
		cDate.add(Calendar.HOUR, timeInHours);
		
		int years=cDate.get(Calendar.YEAR)-cDate.getMinimum(Calendar.YEAR);
		int months=cDate.get(Calendar.MONTH);
		int days=cDate.get(Calendar.DAY_OF_MONTH)-1;
		int hours=cDate.get(Calendar.HOUR_OF_DAY);
		
		String yearsString=(years<10)?"0"+years:years+"";
		String monthsString=(months<10)?"0"+months:months+"";
		String daysString=(days<10)?"0"+days:days+"";
		String hoursString=(hours<10)?"0"+hours:hours+"";
		
		return yearsString+monthsString+daysString+hoursString+"0000000R";
	}

	@Override
	public void setEnquireLink(int enquireLink) {
		super.setEnquireLink(enquireLink);
		if(session!=null){
			session.setEnquireLinkTimer(enquireLink);
		}
	}
	
	
}
