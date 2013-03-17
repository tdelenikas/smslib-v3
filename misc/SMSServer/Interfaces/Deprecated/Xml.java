// SMSLib for Java v3
// A Java API library for sending and receiving SMS via a GSM modem
// or other supported gateways.
// Web Site: http://www.smslib.org
//
// Copyright (C) 2002-2008, Thanasis Delenikas, Athens/GREECE.
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.smslib.InboundMessage;
import org.smslib.OutboundMessage;
import org.smslib.Message.MessageEncodings;
import org.smslib.Message.MessageTypes;
import org.smslib.smsserver.SMSServer;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * This interface uses xml-files to read outgoing messages and write inbound
 * messages.<br />
 * Every file contains neither <u>ONE</u> inbound <b>or</b> <u>ONE</u>
 * outbound message.
 * <hr />
 * The DTDs for the xml files containing a inbound message:<br />
 * 
 * <pre>
 * &lt;!ELEMENT message (originator, text, receive_date)&gt;
 * &lt;!ATTLIST message
 *   id		ID	#REQUIRED
 *   gateway_id	CDATA	#REQUIRED
 *   type		CDATA	#IMPLIED
 *   encoding	CDATA	#IMPLIED &gt;
 * &lt;!ELEMENT originator (#PCDATA)&gt;
 * &lt;!ELEMENT text (#PCDATA)&gt;
 * &lt;!ELEMENT receive_date (#PCDATA)&gt; 
 * </pre>
 * 
 * <hr />
 * The DTDs for the xml files containing a outgoing message:<br />
 * 
 * <pre>
 * &lt;!ELEMENT message (recipient, text, originator, create_date?)&gt;
 * &lt;!ATTLIST message 
 *    id	 	 ID      #REQUIRED
 *    gateway_id	 CDATA	#IMPLIED
 *    status         CDATA  &quot;U&quot; 
 *    encoding       CDATA	&quot;7&quot;
 *    priority       CDATA	&quot;N&quot;
 *    ref_no         CDATA	#IMPLIED
 *    status_report  CDATA	#IMPLIED
 *    flash_sms      CDATA	#IMPLIED
 *    src_port       CDATA	#IMPLIED
 *    dst_port       CDATA	#IMPLIED &gt; 
 * &lt;!ELEMENT recipient (#PCDATA)&gt;
 * &lt;!ELEMENT text (#PCDATA)&gt;
 * &lt;!ELEMENT create_date (#PCDATA)&gt;
 * &lt;!ELEMENT originator (#PCDATA)&gt;
 * </pre>
 * 
 * @author Sebastian Just
 */
public class Xml extends Interface<File>
{
	public static final String sOutSentDirectory = "sent";

	public static final String sOutFailedDirectory = "failed";

	public static final String sOutBrokenDirectory = "broken";

	private static final SimpleDateFormat fileSdf = new SimpleDateFormat("yyyyMMddHHmmss-S");

	private static final SimpleDateFormat iso8601Sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

	/**
	 * Formats a string ISO8601 compliant.
	 * 
	 * @param date
	 *            the date to format
	 * @return a string with a ISO8601 compliant date
	 */
	protected String getDateAsISO8601(Date date)
	{
		String result = iso8601Sdf.format(date);
		StringBuilder sb = new StringBuilder(result.length() + 1);
		sb.append(result.substring(0, result.length() - 2));
		sb.append(":");
		sb.append(result.substring(result.length() - 2));
		return sb.toString();
	}

	/**
	 * Creates a date from a ISO8601 string
	 * 
	 * @param string
	 *            The string to parse
	 * @return A date
	 */
	protected Date getISO8601AsDate(String string)
	{
		StringBuilder sb = new StringBuilder(string);
		sb.replace(string.length() - 3, string.length() - 2, "");
		try
		{
			return iso8601Sdf.parse(sb.toString());
		}
		catch (ParseException e)
		{
			getService().getLogger().logWarn("Can't parse " + string + " as ISO8601 date!", null, null);
			return null;
		}
	}

	/* The used directory which contains all inbound messages */
	private File inDirectory;

	/* The used directory which contains all outbound messages */
	private File outDirectory;

	/* The used directory which contains all failed outbound messages */
	private File outFailedDirectory;

	/* The used directory which contains all sent outbound messages */
	private File outSentDirectory;

	private File outBrokenDirectory;

	public Xml(String myInterfaceId, Properties myProps, SMSServer myServer, InterfaceTypes myType)
	{
		super(myInterfaceId, myProps, myServer, myType);
		setDescription("Interface for xml input/output files");
		/* Read arguments */
		this.inDirectory = new File(getProperty("in") == null ? "." : getProperty("in"));
		this.outDirectory = new File(getProperty("out") == null ? "." : getProperty("out"));
		/* Check given arguments */
		if (isInbound())
		{
			if (!this.inDirectory.isDirectory() || !this.inDirectory.canWrite()) { throw new IllegalArgumentException(myInterfaceId + ".in isn't a directory or isn't write-/readable!"); }
			try
			{
				writeInboundDTD(this.inDirectory);
			}
			catch (IOException e)
			{
				throw new IllegalArgumentException(e);
			}
		}
		if (isOutbound())
		{
			if (!this.outDirectory.isDirectory() || !this.outDirectory.canRead() || !this.outDirectory.canWrite()) { throw new IllegalArgumentException(myInterfaceId + ".out isn't a directory or isn't write-/readable!"); }
			/* Check directory structure */
			this.outSentDirectory = new File(this.outDirectory, sOutSentDirectory);
			if (!this.outSentDirectory.isDirectory())
			{
				if (!this.outSentDirectory.mkdir()) { throw new IllegalArgumentException("Can't create directory '" + this.outSentDirectory); }
			}
			this.outFailedDirectory = new File(this.outDirectory, sOutFailedDirectory);
			if (!this.outFailedDirectory.isDirectory())
			{
				if (!this.outFailedDirectory.mkdir()) { throw new IllegalArgumentException("Can't create directory '" + this.outFailedDirectory); }
			}
			this.outBrokenDirectory = new File(this.outDirectory, sOutBrokenDirectory);
			if (!this.outBrokenDirectory.isDirectory())
			{
				if (!this.outBrokenDirectory.mkdir()) { throw new IllegalArgumentException("Can't create directory '" + this.outBrokenDirectory); }
			}
			try
			{
				writeOutboundDTD(this.outDirectory);
			}
			catch (IOException e)
			{
				throw new IllegalArgumentException(e);
			}
		}
	}

	private void writeInboundDTD(File in) throws IOException
	{
		File dtd = new File(in, "smssvr_in.dtd");
		if (!dtd.exists())
		{
			Writer w = new BufferedWriter(new FileWriter(dtd));
			String CRLF = System.getProperty("line.separator");
			w.write(" <!ELEMENT message (originator, text, receive_date)>");
			w.write(CRLF);
			w.write("   <!ATTLIST message");
			w.write(CRLF);
			w.write("       id		ID	#REQUIRED");
			w.write(CRLF);
			w.write("       gateway_id	CDATA	#REQUIRED");
			w.write(CRLF);
			w.write("       type		CDATA	#IMPLIED");
			w.write(CRLF);
			w.write("       encoding	CDATA	#IMPLIED >");
			w.write(CRLF);
			w.write("     <!ELEMENT originator (#PCDATA)>");
			w.write(CRLF);
			w.write("     <!ELEMENT text (#PCDATA)>");
			w.write(CRLF);
			w.write("     <!ELEMENT receive_date (#PCDATA)>");
			w.write(CRLF);
			w.flush();
			w.close();
		}
	}

	private void writeOutboundDTD(File out) throws IOException
	{
		File dtd = new File(out, "smssvr_out.dtd");
		if (!dtd.exists())
		{
			Writer w = new BufferedWriter(new FileWriter(dtd));
			String CRLF = System.getProperty("line.separator");
			w.write(" <!ELEMENT message (recipient, text, originator, create_date?)>");
			w.write(CRLF);
			w.write("   <!ATTLIST message ");
			w.write(CRLF);
			w.write("      id	 	 ID      #REQUIRED");
			w.write(CRLF);
			w.write("      gateway_id	 CDATA	#IMPLIED");
			w.write(CRLF);
			w.write("      status         CDATA  \"U\" ");
			w.write(CRLF);
			w.write("      encoding       CDATA	\"7\"");
			w.write(CRLF);
			w.write("      priority       CDATA	\"N\"");
			w.write(CRLF);
			w.write("      ref_no         CDATA	#IMPLIED");
			w.write(CRLF);
			w.write("      status_report  CDATA	#IMPLIED");
			w.write(CRLF);
			w.write("      flash_sms      CDATA	#IMPLIED");
			w.write(CRLF);
			w.write("      src_port       CDATA	#IMPLIED");
			w.write(CRLF);
			w.write("      dst_port       CDATA	#IMPLIED> ");
			w.write(CRLF);
			w.write("   <!ELEMENT recipient (#PCDATA)>");
			w.write(CRLF);
			w.write("   <!ELEMENT text (#PCDATA)>");
			w.write(CRLF);
			w.write("   <!ELEMENT create_date (#PCDATA)>");
			w.write(CRLF);
			w.write("   <!ELEMENT originator (#PCDATA)>");
			w.write(CRLF);
			w.flush();
			w.close();
		}
	}

	/**
	 * Adds the given InboundMessage to the given document.
	 * 
	 * @param xmldoc
	 *            The document in which the message is written
	 * @param m
	 *            The message to add to the docment
	 */
	private void addMessageToDocument(Document xmldoc, org.smslib.InboundMessage m)
	{
		Element message = null;
		Element originatorElement = null;
		Node originatorNode = null;
		Element textElement = null;
		Node textNode = null;
		Element timeElement = null;
		Node timeNode = null;
		message = xmldoc.createElement("message");
		message.setAttribute("gateway_id", m.getGatewayId());
		/* Type */
		String msgType = null;
		switch (m.getType())
		{
			case INBOUND:
				msgType = "I";
				break;
			case STATUSREPORT:
				msgType = "S";
				break;
			case OUTBOUND:
				msgType = "O";
				break;
			case UNKNOWN:
				msgType = "U";
				break;
			case WAPSI:
				msgType = "W";
				break;
		}
		if (msgType != null)
		{
			message.setAttributeNS(null, "type", msgType);
		}
		/* Encoding */
		String encoding = null;
		switch (m.getEncoding())
		{
			case ENC7BIT:
				encoding = "7";
				break;
			case ENC8BIT:
				encoding = "8";
				break;
			case ENCUCS2:
				encoding = "U";
				break;
			case ENCCUSTOM:
				encoding = "C";
				break;
		}
		if (encoding != null)
		{
			message.setAttributeNS(null, "encoding", encoding);
		}
		/* Compose message */
		originatorNode = xmldoc.createTextNode(m.getOriginator());
		originatorElement = xmldoc.createElementNS(null, "originator");
		originatorElement.appendChild(originatorNode);
		textNode = xmldoc.createTextNode(m.getText());
		textElement = xmldoc.createElementNS(null, "text");
		textElement.appendChild(textNode);
		timeNode = xmldoc.createTextNode(getDateAsISO8601(m.getDate()));
		timeElement = xmldoc.createElementNS(null, "receive_date");
		timeElement.appendChild(timeNode);
		message.appendChild(originatorElement);
		message.appendChild(textElement);
		message.appendChild(timeElement);
		xmldoc.appendChild(message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.smslib.smsserver.AInterface#getMessagesToSend()
	 */
	@Override
	public Collection<OutboundMessage> getMessagesToSend() throws Exception
	{
		Collection<OutboundMessage> messageList = new ArrayList<OutboundMessage>();
		File[] outFiles = this.outDirectory.listFiles(new FileFilter()
		{
			public boolean accept(File f)
			{
				/* Read only unprocessed files with an .xml suffix */
				return (f.getAbsolutePath().endsWith(".xml") && !getMessageCache().containsValue(f));
			}
		});
		for (int i = 0; i < outFiles.length; i++)
		{
			try
			{
				/* Process each document and add message to the list */
				OutboundMessage msg = readDocument(outFiles[i]);
				if (msg == null) { throw new IllegalArgumentException("Missing required fieldes!"); }
				messageList.add(msg);
				getMessageCache().put(msg.getMessageId(), outFiles[i]);
			}
			catch (IllegalArgumentException e)
			{
				getService().getLogger().logWarn("Skipping outgoing file " + outFiles[i].getAbsolutePath() + ": File is not valid: " + e.getLocalizedMessage(), null, null);
				File brokenFile = new File(this.outBrokenDirectory, outFiles[i].getName());
				if (!outFiles[i].renameTo(brokenFile)) getService().getLogger().logError("Can't move " + outFiles[i] + " to " + brokenFile, null, null);
			}
		}
		return messageList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.smslib.smsserver.AInterface#markMessage(org.smslib.OutboundMessage)
	 */
	@Override
	public void markMessage(org.smslib.OutboundMessage msg) throws Exception
	{
		if (msg == null) { return; }
		File f = getMessageCache().get(msg.getMessageId());
		File newF = null;
		switch (msg.getMessageStatus())
		{
			case SENT:
				newF = new File(this.outSentDirectory, f.getName());
				break;
			case FAILED:
				newF = new File(this.outFailedDirectory, f.getName());
				break;
			default:
				break;
		}
		if (f.renameTo(newF))
		{
			getService().getLogger().logInfo(f + " marked.", null, null);
		}
		else
		{
			getService().getLogger().logWarn("Can't move " + f + " to " + newF, null, null);
		}
		getMessageCache().remove(msg.getMessageId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.smslib.smsserver.AInterface#MessagesReceived(java.util.List)
	 */
	@Override
	public void MessagesReceived(Collection<InboundMessage> msgList) throws Exception
	{
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		for (InboundMessage msg : msgList)
		{
			/* Check type of an message */
			if ((msg.getType() == MessageTypes.INBOUND) || (msg.getType() == MessageTypes.STATUSREPORT))
			{
				Document xmldoc = db.newDocument();
				/* Add inbound message to the xml document */
				addMessageToDocument(xmldoc, msg);
				/* Serialize xml document */
				File outputFile = null;
				do 
				{
					String fileName = fileSdf.format(new java.util.Date()) + ".xml";
					outputFile = new File(this.inDirectory, fileName);
					if (outputFile.exists()) 
					{
						Thread.sleep(100);
					}
				} while (outputFile.exists());
				getService().getLogger().logInfo("Writing inbound files to " + outputFile.getName(), null, null);
				writeDocument(xmldoc, outputFile);
			}
		}
	}

	/**
	 * Tries to read from the given filename the outbound message
	 * 
	 * @param file
	 *            The file to read from
	 * @return Outbound message read form the file
	 * @throws IllegalArgumentExcpetion
	 *             Is thrown if there's something wrong with the content of the
	 *             file
	 * @throws IOExcpetion
	 *             Is throws if there's an I/O problem while reading the file
	 */
	private OutboundMessage readDocument(File file) throws IllegalArgumentException
	{
		Document xmldoc = null;
		try
		{
			/* Check for valid XML file */
			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			dbfac.setValidating(true);
			DocumentBuilder db = dbfac.newDocumentBuilder();
			db.setErrorHandler(new ErrorHandler()
			{
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
				 */
				public void error(SAXParseException arg0)
				{
					throw new IllegalArgumentException(arg0);
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
				 */
				public void fatalError(SAXParseException arg0)
				{
					throw new IllegalArgumentException(arg0);
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
				 */
				public void warning(SAXParseException arg0)
				{
					throw new IllegalArgumentException(arg0);
				}
			});
			xmldoc = db.parse(file);
			DocumentType outDoctype = xmldoc.getDoctype();
			if (!"message".equals(outDoctype.getName())) { throw new IllegalArgumentException("Wrong DOCTYPE - Have to be message!"); }
			if (!"smssvr_out.dtd".equals(outDoctype.getSystemId())) { throw new IllegalArgumentException("Wrong SystemId in DOCTYPE - Have to be smssvr_out.dtd!"); }
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException(e);
		}
		/* Search "message" root element */
		NodeList rnl = xmldoc.getElementsByTagName("message");
		if (rnl == null || rnl.getLength() != 1) { throw new IllegalArgumentException("Wrong root element or root element count!"); }
		/* Process all child elements aka "message" */
		return readNode(rnl.item(0));
	}

	/**
	 * Reads a given node and tries to parse it
	 * 
	 * @param n
	 *            The node to parse
	 * @return An outboundmessage if the node was parsable or null
	 */
	private OutboundMessage readNode(Node n)
	{
		if ("message".equals(n.getNodeName()))
		{
			String recipient = null;
			String text = null;
			String originator = null;
			Element e = (Element) n;
			NodeList cnl = n.getChildNodes();
			/* Read required fields */
			for (int i = 0; i < cnl.getLength(); i++)
			{
				Node en = cnl.item(i);
				if ("recipient".equals(cnl.item(i).getNodeName()))
				{
					recipient = en.getTextContent();
				}
				else if ("text".equals(cnl.item(i).getNodeName()))
				{
					text = en.getTextContent();
				}
				else if ("originator".equals(cnl.item(i).getNodeName()))
				{
					originator = en.getTextContent();
				}
			}
			/* Create outbound message */
			OutboundMessage outMsg = new OutboundMessage(recipient, text);
			/* Set required fields */
			outMsg.setFrom(originator);
			if (!"".equals(e.getAttribute("create_date")))
			{
				outMsg.setDate(getISO8601AsDate(e.getAttribute("create_date")));
			}
			if (!"".equals(e.getAttribute("gateway_id")))
			{
				outMsg.setGatewayId(e.getAttribute("gateway_id"));
			}
			/* Read optional fields - priority */
			String priority = e.getAttribute("priority");
			if ("L".equalsIgnoreCase(priority))
			{
				outMsg.setPriority(-1);
			}
			else if ("N".equalsIgnoreCase(priority))
			{
				outMsg.setPriority(0);
			}
			else if ("H".equalsIgnoreCase(priority))
			{
				outMsg.setPriority(+1);
			}
			/* Read optional fields - encoding */
			String encoding = e.getAttribute("encoding");
			if ("7".equals(encoding))
			{
				outMsg.setEncoding(MessageEncodings.ENC7BIT);
			}
			else if ("8".equals(encoding))
			{
				outMsg.setEncoding(MessageEncodings.ENC8BIT);
			}
			else
			{
				outMsg.setEncoding(MessageEncodings.ENCUCS2);
			}
			/* Read optinal fields - status_report */
			if ("1".equals(e.getAttribute("status_report")))
			{
				outMsg.setStatusReport(true);
			}
			/* Read optinal fields - flash_sms */
			if ("1".equals(e.getAttribute("flash_sms")))
			{
				outMsg.setFlashSms(true);
			}
			/* Read optinal fields - src_port */
			if (!"".equals(e.getAttribute("src_port")))
			{
				outMsg.setSrcPort(Integer.parseInt(e.getAttribute("src_port")));
			}
			/* Read optinal fields - dst_port */
			if (!"".equals(e.getAttribute("dst_port")))
			{
				outMsg.setDstPort(Integer.parseInt(e.getAttribute("dst_port")));
			}
			return outMsg;
		}
		return null;
	}

	/**
	 * Writes the given document to the geiven filename. <br />
	 * The DTD smssvr_in.dtd is added, too.
	 * 
	 * @param doc
	 *            The document to serialize
	 * @param fileName
	 *            The file in which the document should be serialized
	 */
	private void writeDocument(Document doc, File fileName) throws IOException, TransformerFactoryConfigurationError, TransformerException
	{
		FileOutputStream fos = new FileOutputStream(fileName);
		Transformer trans = TransformerFactory.newInstance().newTransformer();
		trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		trans.setOutputProperty(OutputKeys.INDENT, "yes");
		trans.setOutputProperty(OutputKeys.STANDALONE, "yes");
		trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		trans.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "smssvr_in.dtd");
		StreamResult result = new StreamResult(fos);
		DOMSource source = new DOMSource(doc);
		trans.transform(source, result);
		fos.close();
	}
}
