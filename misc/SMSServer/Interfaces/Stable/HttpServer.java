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

package org.smslib.smsserver.interfaces;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.smslib.AGateway;
import org.smslib.InboundMessage;
import org.smslib.Library;
import org.smslib.OutboundMessage;
import org.smslib.InboundMessage.MessageClasses;
import org.smslib.Message.MessageEncodings;
import org.smslib.Message.MessageTypes;
import org.smslib.OutboundMessage.MessageStatuses;
import org.smslib.Service;
import org.smslib.smsserver.SMSServer;

/**
 * Interface for embedded web server.<br />
 */
public class HttpServer extends Interface<Integer>
{
	public static final int ERR_INVALID_PARMS = -9000;

	public static final int ERR_MISSING_PARMS = -9001;

	public static final int ERR_SEND_ERROR = -9002;

	public static final int ERR_WRONG_PASSWORD = -9003;

	public static final int ERR_INTERNAL_ERROR = -9999;

	WebServer webServer;

	public HttpServer(String myInterfaceId, Properties myProps, SMSServer myServer, InterfaceTypes myType)
	{
		super(myInterfaceId, myProps, myServer, myType);
		setDescription("Default HTTP Server interface.");
	}

	@Override
	public void start() throws Exception
	{
		setWebServer(new WebServer(this.getServer(), Integer.parseInt(getProperty("port", "8080"))));
		Thread.sleep(2000);
		getWebServer().start();
		Thread.sleep(2000);
		super.start();
	}

	@Override
	public void stop() throws Exception
	{
		getWebServer().interrupt();
		super.stop();
	}

	WebServer getWebServer()
	{
		return this.webServer;
	}

	void setWebServer(WebServer myWebServer)
	{
		this.webServer = myWebServer;
	}

	class WebServer extends Thread
	{
		SMSServer server;

		int port;

		public WebServer(SMSServer myServer, int myPort)
		{
			this.server = myServer;
			this.port = myPort;
		}

		@Override
		public void run()
		{
			Server httpServer = new Server(this.port);
			httpServer.setHandler(new HttpHandler(this.server));
			try
			{
				httpServer.start();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	class HttpHandler extends AbstractHandler
	{
		protected SMSServer server;

		public HttpHandler(SMSServer myServer)
		{
			this.server = myServer;
		}

		protected SMSServer getSMSServer()
		{
			return this.server;
		}

		@Override
		public void handle(String target, Request req, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
		{
			if (request.getPathInfo().toString().equals("/")) new Status(getSMSServer(), target, request, response).process();
			else if (request.getPathInfo().toString().equals("/status")) new Status(getSMSServer(), target, request, response).process();
			else if (request.getPathInfo().toString().equals("/send")) new Send(getSMSServer(), target, request, response).process();
			else if (request.getPathInfo().toString().equals("/read")) new Read(getSMSServer(), target, request, response).process();
		}
	}

	abstract class AHttpCommand
	{
		private SMSServer smsServer;

		private String target;

		private HttpServletRequest request;

		private HttpServletResponse response;

		public AHttpCommand(SMSServer myServer, String myTarget, HttpServletRequest myRequest, HttpServletResponse myResponse)
		{
			setSMSServer(myServer);
			setTarget(myTarget);
			setRequest(myRequest);
			setResponse(myResponse);
		}

		protected SMSServer getSMSServer()
		{
			return this.smsServer;
		}

		protected void setSMSServer(SMSServer mySmsServer)
		{
			this.smsServer = mySmsServer;
		}

		protected String getTarget()
		{
			return this.target;
		}

		protected void setTarget(String myTarget)
		{
			this.target = myTarget;
		}

		protected HttpServletRequest getRequest()
		{
			return this.request;
		}

		protected void setRequest(HttpServletRequest myRequest)
		{
			this.request = myRequest;
		}

		protected HttpServletResponse getResponse()
		{
			return this.response;
		}

		protected void setResponse(HttpServletResponse myResponse)
		{
			this.response = myResponse;
		}

		public abstract void process() throws IOException, ServletException;
	}

	class Status extends AHttpCommand
	{
		public Status(SMSServer myServer, String myTarget, HttpServletRequest myRequest, HttpServletResponse myResponse)
		{
			super(myServer, myTarget, myRequest, myResponse);
		}

		@Override
		public void process() throws IOException
		{
			PrintWriter body = getResponse().getWriter();
			getResponse().setContentType("text/html");
			getResponse().setStatus(HttpServletResponse.SC_OK);
			body.println("<html>");
			body.println("<head>");
			body.println("<title>SMSServer - Status</title>");
			body.println("<style type='text/css'> ");
			body.println("<!--");
			body.println("#box-table-a");
			body.println("{");
			body.println("font-family: 'Lucida Sans Unicode', 'Lucida Grande', Sans-Serif;");
			body.println("font-size: 12px;");
			body.println("width: 100px;");
			body.println("text-align: left;");
			body.println("border-collapse: collapse;");
			body.println("}");
			body.println("#box-table-a th");
			body.println("{");
			body.println("font-size: 13px;");
			body.println("font-weight: normal;");
			body.println("padding: 8px;");
			body.println("background: #b9c9fe;");
			body.println("border-top: 4px solid #aabcfe;");
			body.println("border-bottom: 1px solid #fff;");
			body.println("color: #039;");
			body.println("}");
			body.println("#box-table-a td");
			body.println("{");
			body.println("padding: 8px;");
			body.println("background: #e8edff; ");
			body.println("border-bottom: 1px solid #fff;");
			body.println("color: #669;");
			body.println("border-top: 1px solid transparent;");
			body.println("}");
			body.println("#box-table-a tr:hover td");
			body.println("{");
			body.println("background: #d0dafd;");
			body.println("color: #339;");
			body.println("}");
			body.println("-->");
			body.println("</style>");
			body.println("</head>");
			body.println("<body style='font-family:Arial, Helvetica, sans-serif; font-size: 0.8em;'>");
			body.println("<p style='font-size: 2em; font-weight: bold; text-align: center;'>SMSServer - Status Page</p>");
			{
				String tpl, line;
				int days, hours, minutes;
				long msMillisDiff;
				tpl = "<p style='text-align: center;'>Version: _VERSION<br />Uptime: since _START, _D days, _H hours, _M minutes</p>";
				line = tpl.replaceAll("_VERSION", Library.getLibraryVersion());
				line = line.replaceAll("_START", new Date(Service.getInstance().getStartMillis()).toString());
				msMillisDiff = System.currentTimeMillis() - Service.getInstance().getStartMillis();
				days = (int) (msMillisDiff / (24 * 60 * 60 * 1000));
				msMillisDiff = msMillisDiff - (days * (24 * 60 * 60 * 1000));
				hours = (int) (msMillisDiff / (60 * 60 * 1000));
				msMillisDiff = msMillisDiff - (hours * (60 * 60 * 1000));
				minutes = (int) (msMillisDiff / (60 * 1000));
				line = line.replaceAll("_D", "" + days);
				line = line.replaceAll("_H", "" + hours);
				line = line.replaceAll("_M", "" + minutes);
				body.println(line);
			}
			body.println("<p style='font-size: 1.3em; font-weight: bold; text-align: center;'>Gateway Status</p>");
			body.println("<center>");
			body.println("<table id='box-table-a' style='width: 750px;'>");
			body.println("<tr><th></th><th></th><th></th><th></th><th scope='col' colspan='2' style='text-align: center;'>Total Traffic</th><th></th><th></th><th></th></tr>");
			body.println("<tr><th>Name</th><th>Type</th><th style='text-align: center;'>IN?</th><th style='text-align: center;'>OUT?</th><th style='text-align: right;'>Inbound</th><th style='text-align: right;'>Outbound</th><th style='text-align: right;'>Queued</th><th style='text-align: right;'>Restarts</th><th style='text-align: center;'>Status</th></tr>");
			{
				String line;
				for (AGateway gateway : Service.getInstance().getGateways())
				{
					line = "<tr><td>_NAME</td><td>_CLASS</td><td style='text-align: center;'>_ISIN</td><td style='text-align: center;'>_ISOUT</td><td style='text-align: right;'>_INBOUND</td><td style='text-align: right;'>_OUTBOUND</td><td style='text-align: right;'>_QUEUED</td><td style='text-align: right;'>_RESTART</td><td style='text-align: center;'>_STATUS</td></tr>";
					line = line.replaceAll("_NAME", gateway.getGatewayId());
					line = line.replaceAll("_CLASS", gateway.getClass().getName());
					line = line.replaceAll("_ISIN", (gateway.isInbound() ? "Yes" : "No"));
					line = line.replaceAll("_ISOUT", (gateway.isOutbound() ? "Yes" : "No"));
					line = line.replaceAll("_INBOUND", "" + gateway.getInboundMessageCount());
					line = line.replaceAll("_OUTBOUND", "" + gateway.getOutboundMessageCount());
					line = line.replaceAll("_QUEUED", "" + Service.getInstance().getQueueManager().getPendingMessages(gateway.getGatewayId()));
					line = line.replaceAll("_RESTART", "" + gateway.getRestartCount());
					switch (gateway.getStatus())
					{
						case STOPPING:
							line = line.replaceAll("_STATUS", "" + "Stopping...");
							break;
						case STOPPED:
							line = line.replaceAll("_STATUS", "" + "Stopped");
							break;
						case FAILURE:
							line = line.replaceAll("_STATUS", "" + "Failure");
							break;
						case RESTART:
							line = line.replaceAll("_STATUS", "" + "Restarting");
							break;
						case STARTING:
							line = line.replaceAll("_STATUS", "" + "Starting...");
							break;
						case STARTED:
							line = line.replaceAll("_STATUS", "" + "Started");
							break;
					}
					body.println(line);
				}
			}
			body.println("</table>");
			body.println("</center>");
			body.println("<br />");
			body.println("<p style='font-size: 1.3em; font-weight: bold; text-align: center;'>Interface Status</p>");
			body.println("<center>");
			body.println("<table id='box-table-a' style='width: 500px;'>");
			//body.println("<tr><th></th><th></td><th></th><th></th><th scope='col' colspan='2' style='text-align: center;'>Total Traffic</th></tr>");
			body.println("<tr><th>Name</th><th>Type</th><th style='text-align: center;'>IN?</th><th style='text-align: center;'>OUT?</th><th style='text-align: center;'>Queue</th></tr>");
			{
				String line;
				for (Interface<? extends Object> inf : getSMSServer().getInfList())
				{
					line = "<tr><td>_NAME</td><td>_CLASS</td><td style='text-align: center;'>_ISIN</td><td style='text-align: center;'>_ISOUT</td><td style='text-align: center;'>_QUEUE</td></tr>";
					line = line.replaceAll("_NAME", inf.getId());
					line = line.replaceAll("_CLASS", inf.getClass().getName());
					line = line.replaceAll("_ISIN", (inf.isInbound() ? "Yes" : "No"));
					line = line.replaceAll("_ISOUT", (inf.isOutbound() ? "Yes" : "No"));
					try
					{
						line = line.replaceAll("_QUEUE", (inf.getPendingMessagesToSend() == -1 ? "N/A" : "" + inf.getPendingMessagesToSend()));
					}
					catch (Exception e)
					{
						line = line.replaceAll("_QUEUE", "N/A");
					}
					body.println(line);
				}
			}
			body.println("</table>");
			body.println("</center>");
			body.println("</body>");
			body.println("</html>");
			((Request) getRequest()).setHandled(true);
		}
	}

	public class Read extends AHttpCommand
	{
		public Read(SMSServer myServer, String myTarget, HttpServletRequest myRequest, HttpServletResponse myResponse)
		{
			super(myServer, myTarget, myRequest, myResponse);
		}

		@Override
		public void process() throws IOException
		{
			String gateway;
			String password;
			
			PrintWriter body = getResponse().getWriter();
			password = getRequest().getParameter("password");
			if (password != null && password.equalsIgnoreCase(getProperty("password.read", "")))
			{
				List<InboundMessage> msgList = new ArrayList<InboundMessage>();
				getResponse().setContentType("text/xml");
				getResponse().setStatus(HttpServletResponse.SC_OK);
				gateway = getRequest().getParameter("gateway");
				try
				{
					if ((gateway != null) && (gateway.length() != 0)) Service.getInstance().readMessages(msgList, MessageClasses.ALL, gateway);
					else Service.getInstance().readMessages(msgList, MessageClasses.ALL);
					body.println("<?xml version='1.0' encoding='UTF-8'?>");
					body.println("<messages>");
					body.printf("<error>0</error>", 0);
					for (InboundMessage msg : msgList)
					{
						body.println("<message>");
						if (msg.getType() == MessageTypes.INBOUND)
						{
							body.printf("<message_type>%s</message_type>", "Inbound");
							body.printf("<message_date>%s</message_date>\n", getDateAsISO8601(msg.getDate()));
							body.printf("<originator>%s</originator>\n", msg.getOriginator());
							body.printf("<text>\n<![CDATA[\n%s\n]]>\n</text>\n", msg.getText());
							body.printf("<encoding>%s</encoding>\n", msg.getEncoding());
							body.printf("<gateway>%s</gateway>\n", msg.getGatewayId());
						}
						body.println("</message>");
					}
					body.println("</messages>");
				}
				catch (Exception e)
				{
					body.println("<messages>");
					body.printf("<error>%d</error>", ERR_INTERNAL_ERROR);
					body.println("</messages>");
				}
			}
			else
			{
				getResponse().setContentType("text/xml");
				getResponse().setStatus(HttpServletResponse.SC_OK);
				body.println("<?xml version='1.0' encoding='UTF-8'?>");
				body.println("<messages>");
				body.printf("<error>%d</error>", ERR_WRONG_PASSWORD);
				body.printf("<error_description>%s</error_description>", "Invalid password.");
				body.println("</messages>");
			}
			((Request) getRequest()).setHandled(true);
		}
	}

	public class Send extends AHttpCommand
	{
		public Send(SMSServer myServer, String myTarget, HttpServletRequest myRequest, HttpServletResponse myResponse)
		{
			super(myServer, myTarget, myRequest, myResponse);
		}

		@Override
		public void process() throws IOException
		{
			boolean foundErrors = false;
			String recipient, text, parm;
			String password;
			
			PrintWriter body = getResponse().getWriter();
			password = getRequest().getParameter("password");
			if (password != null && password.equalsIgnoreCase(getProperty("password.send", "")))
			{
				getResponse().setContentType("text/xml");
				getResponse().setStatus(HttpServletResponse.SC_OK);
				recipient = getRequest().getParameter("recipient");
				text = getRequest().getParameter("text");
				if (((recipient == null) || (recipient.length() == 0)) || ((text == null) || (text.length() == 0)))
				{
					foundErrors = true;
					pushResponse(body, ERR_MISSING_PARMS, "Missing Parameters.");
				}
				else
				{
					OutboundMessage msg = new OutboundMessage(recipient, text);
					parm = getRequest().getParameter("encoding");
					if ((parm != null) && parm.length() != 0)
					{
						if (parm.equalsIgnoreCase("7")) msg.setEncoding(MessageEncodings.ENC7BIT);
						else if (parm.equalsIgnoreCase("8")) msg.setEncoding(MessageEncodings.ENC8BIT);
						else if (parm.equalsIgnoreCase("U")) msg.setEncoding(MessageEncodings.ENCUCS2);
						else
						{
							foundErrors = true;
							pushResponse(body, ERR_INVALID_PARMS, "Invalid encoding requested.");
						}
					}
					parm = getRequest().getParameter("priority");
					if ((parm != null) && (parm.length() != 0))
					{
						try
						{
							msg.setPriority(Integer.parseInt(parm));
						}
						catch (Exception e)
						{
							foundErrors = true;
							pushResponse(body, ERR_INVALID_PARMS, "Invalid priority requested.");
						}
					}
					parm = getRequest().getParameter("from");
					if ((parm != null) && (parm.length() != 0)) msg.setFrom(parm);
					parm = getRequest().getParameter("gateway");
					if ((parm != null) && (parm.length() != 0)) msg.setGatewayId(parm);
					if (!foundErrors)
					{
						try
						{
							Service.getInstance().sendMessage(msg);
							pushResponse(body, msg);
						}
						catch (Exception e)
						{
							pushResponse(body, ERR_INTERNAL_ERROR, e.getMessage());
						}
					}
				}
			}
			else
			{
				pushResponse(body, ERR_WRONG_PASSWORD, "Invalid password.");
			}
			((Request) getRequest()).setHandled(true);
		}

		private void pushResponse(PrintWriter body, OutboundMessage msg)
		{
			body.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			body.println("<send>");
			if (msg.getMessageStatus() == MessageStatuses.SENT)
			{
				body.printf("<error>%d</error>", 0);
			}
			else
			{
				body.printf("<error>%d</error>", ERR_SEND_ERROR);
				body.printf("<error_description>%s</error_description>", "Message not sent.");
			}
			body.printf("<message_status>%s</message_status>", msg.getMessageStatus());
			if (msg.getMessageStatus() == MessageStatuses.FAILED) body.printf("<failure_cause>%s</failure_cause>", msg.getFailureCause());
			body.printf("<ref_no>%s</ref_no>", msg.getRefNo());
			body.printf("<gateway>%s</gateway>", msg.getGatewayId());
			body.println("</send>");
		}

		private void pushResponse(PrintWriter body, int errNo, String errMessage)
		{
			body.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			body.println("<send>");
			body.printf("<error>%d</error>", errNo);
			body.printf("<error_description>%s</error_description>", errMessage);
			body.println("</send>");
		}
	}

	protected String getDateAsISO8601(Date date)
	{
		Date myDate;

		myDate = (date == null ? new Date() : date);
		String result = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(myDate);
		StringBuilder sb = new StringBuilder(result.length() + 1);
		sb.append(result.substring(0, result.length() - 2));
		sb.append(":");
		sb.append(result.substring(result.length() - 2));
		return sb.toString();
	}
}
