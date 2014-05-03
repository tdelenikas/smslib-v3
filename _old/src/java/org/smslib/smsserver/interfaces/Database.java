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

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import org.smslib.InboundMessage;
import org.smslib.OutboundBinaryMessage;
import org.smslib.OutboundMessage;
import org.smslib.OutboundWapSIMessage;
import org.smslib.StatusReportMessage;
import org.smslib.Message.MessageEncodings;
import org.smslib.Message.MessageTypes;
import org.smslib.OutboundMessage.FailureCauses;
import org.smslib.OutboundMessage.MessageStatuses;
import org.smslib.OutboundWapSIMessage.WapSISignals;
import org.smslib.helper.Logger;
import org.smslib.smsserver.SMSServer;

/**
 * Interface for database communication with SMSServer. <br />
 * Inbound messages and calls are logged in special tables, outbound messages
 * are retrieved from another table.
 */
public class Database extends Interface<Integer>
{
	static final int SQL_DELAY = 1000;

	int sqlDelayMultiplier = 1;

	private Connection dbCon = null;

	public Database(String myInterfaceId, Properties myProps, SMSServer myServer, InterfaceTypes myType)
	{
		super(myInterfaceId, myProps, myServer, myType);
		setDescription("Default database interface.");
	}

	@Override
	public void start() throws Exception
	{
		Connection con = null;
		Statement cmd;
		Class.forName(getProperty("driver"));
		while (true)
		{
			try
			{
				con = getDbConnection();
				cmd = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				cmd.executeUpdate("update " + getProperty("tables.sms_out", "smsserver_out") + " set status = 'U' where status = 'Q'");
				con.commit();
				cmd.close();
				break;
			}
			catch (SQLException e)
			{
				try
				{
					if (con != null) con.close();
					closeDbConnection();
				}
				catch (Exception innerE)
				{
				}
				if (getServer().getShutdown()) break;
				Logger.getInstance().logError(String.format("SQL failure, will retry in %d seconds...", (sqlDelayMultiplier * (SQL_DELAY / 1000))), e, null);
				Thread.sleep(sqlDelayMultiplier * SQL_DELAY);
				sqlDelayMultiplier *= 2;
			}
		}
		super.start();
	}

	@Override
	public void stop() throws Exception
	{
		Connection con = null;
		while (true)
		{
			try
			{
				Statement cmd;
				con = getDbConnection();
				cmd = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				cmd.executeUpdate("update " + getProperty("tables.sms_out", "smsserver_out") + " set status = 'U' where status = 'Q'");
				con.commit();
				cmd.close();
				closeDbConnection();
				break;
			}
			catch (SQLException e)
			{
				try
				{
					if (con != null) con.close();
					closeDbConnection();
				}
				catch (Exception innerE)
				{
				}
				if (getServer().getShutdown()) break;
				Logger.getInstance().logError(String.format("SQL failure, will retry in %d seconds...", (sqlDelayMultiplier * (SQL_DELAY / 1000))), e, null);
				Thread.sleep(sqlDelayMultiplier * SQL_DELAY);
				sqlDelayMultiplier *= 2;
			}
		}
		super.stop();
	}

	@Override
	public void callReceived(String gtwId, String callerId) throws Exception
	{
		Connection con = null;
		while (true)
		{
			try
			{
				PreparedStatement cmd;
				con = getDbConnection();
				cmd = con.prepareStatement("insert into " + getProperty("tables.calls", "smsserver_calls") + " (call_date, gateway_id, caller_id) values (?,?,?) ");
				cmd.setTimestamp(1, new Timestamp(new java.util.Date().getTime()));
				cmd.setString(2, gtwId);
				cmd.setString(3, callerId);
				cmd.executeUpdate();
				con.commit();
				cmd.close();
				break;
			}
			catch (SQLException e)
			{
				try
				{
					if (con != null) con.close();
					closeDbConnection();
				}
				catch (Exception innerE)
				{
				}
				Logger.getInstance().logError(String.format("SQL failure, will retry in %d seconds...", (sqlDelayMultiplier * (SQL_DELAY / 1000))), e, null);
				Thread.sleep(sqlDelayMultiplier * SQL_DELAY);
				sqlDelayMultiplier *= 2;
			}
		}
	}

	@Override
	public void messagesReceived(Collection<InboundMessage> msgList) throws Exception
	{
		Connection con = null;
		while (true)
		{
			try
			{
				PreparedStatement pst;
				con = getDbConnection();
				pst = con.prepareStatement(" insert into " + getProperty("tables.sms_in", "smsserver_in") + " (process, originator, type, encoding, message_date, receive_date, text," + " original_ref_no, original_receive_date, gateway_id) " + " values(?,?,?,?,?,?,?,?,?,?)");
				for (InboundMessage msg : msgList)
				{
					if ((msg.getType() == MessageTypes.INBOUND) || (msg.getType() == MessageTypes.STATUSREPORT))
					{
						pst.setInt(1, 0);
						switch (msg.getEncoding())
						{
							case ENC7BIT:
								pst.setString(4, "7");
								break;
							case ENC8BIT:
								pst.setString(4, "8");
								break;
							case ENCUCS2:
								pst.setString(4, "U");
								break;
							case ENCCUSTOM:
								pst.setString(4, "C");
								break;
						}
						switch (msg.getType())
						{
							case INBOUND:
								pst.setString(3, "I");
								pst.setString(2, msg.getOriginator());
								if (msg.getDate() != null) pst.setTimestamp(5, new Timestamp(msg.getDate().getTime()));
								pst.setString(8, null);
								pst.setTimestamp(9, null);
								break;
							case STATUSREPORT:
								pst.setString(3, "S");
								pst.setString(2, ((StatusReportMessage) msg).getRecipient());
								if (((StatusReportMessage) msg).getSent() != null) pst.setTimestamp(5, new Timestamp(((StatusReportMessage) msg).getSent().getTime()));
								pst.setString(8, ((StatusReportMessage) msg).getRefNo());
								if (((StatusReportMessage) msg).getReceived() != null) pst.setTimestamp(9, new Timestamp(((StatusReportMessage) msg).getReceived().getTime()));
								if (getProperty("update_outbound_on_statusreport", "no").equalsIgnoreCase("yes"))
								{
									PreparedStatement cmd2;
									cmd2 = con.prepareStatement(" update " + getProperty("tables.sms_out", "smsserver_out") + " set status = ? " + " where (recipient = ? or recipient = ?) and ref_no = ? and gateway_id = ?");
									switch (((StatusReportMessage) msg).getStatus())
									{
										case DELIVERED:
											cmd2.setString(1, "D");
											break;
										case KEEPTRYING:
											cmd2.setString(1, "P");
											break;
										case ABORTED:
											cmd2.setString(1, "A");
											break;
										case UNKNOWN:
											break;
									}
									cmd2.setString(2, ((StatusReportMessage) msg).getRecipient());
									if (((StatusReportMessage) msg).getRecipient().startsWith("+")) cmd2.setString(3, ((StatusReportMessage) msg).getRecipient().substring(1));
									else cmd2.setString(3, "+" + ((StatusReportMessage) msg).getRecipient());
									cmd2.setString(4, ((StatusReportMessage) msg).getRefNo());
									cmd2.setString(5, ((StatusReportMessage) msg).getGatewayId());
									cmd2.executeUpdate();
									cmd2.close();
								}
								break;
							default:
								break;
						}
						pst.setTimestamp(6, new Timestamp(new java.util.Date().getTime()));
						if (msg.getEncoding() == MessageEncodings.ENC8BIT) pst.setString(7, msg.getPduUserData());
						else pst.setString(7, (msg.getText().length() == 0 ? "" : msg.getText()));
						pst.setString(10, msg.getGatewayId());
						pst.executeUpdate();
					}
				}
				pst.close();
				con.commit();
				break;
			}
			catch (SQLException e)
			{
				try
				{
					if (con != null) con.close();
					closeDbConnection();
				}
				catch (Exception innerE)
				{
				}
				Logger.getInstance().logError(String.format("SQL failure, will retry in %d seconds...", (sqlDelayMultiplier * (SQL_DELAY / 1000))), e, null);
				Thread.sleep(sqlDelayMultiplier * SQL_DELAY);
				sqlDelayMultiplier *= 2;
			}
		}
	}

	@Override
	public Collection<OutboundMessage> getMessagesToSend() throws Exception
	{
		Connection con = null;
		Collection<OutboundMessage> msgList = new ArrayList<OutboundMessage>();
		while (true)
		{
			try
			{
				OutboundMessage msg;
				Statement cmd;
				PreparedStatement pst;
				ResultSet rs;
				int msgCount;
				msgCount = 1;
				con = getDbConnection();
				cmd = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
				pst = con.prepareStatement("update " + getProperty("tables.sms_out", "smsserver_out") + " set status = 'Q' where id = ? ");
				rs = cmd.executeQuery("select id, type, recipient, text, wap_url, wap_expiry_date, wap_signal, create_date, originator, encoding, status_report, flash_sms, src_port, dst_port, sent_date, ref_no, priority, status, errors, gateway_id from " + getProperty("tables.sms_out", "smsserver_out") + " where status = 'U' order by priority desc, id");
				while (rs.next())
				{
					if (msgCount > Integer.parseInt(getProperty("batch_size"))) break;
					if (getServer().checkPriorityTimeFrame(rs.getInt("priority")))
					{
						switch (rs.getString("type").charAt(0))
						{
							case 'O':
								switch (rs.getString("encoding").charAt(0))
								{
									case '7':
										msg = new OutboundMessage(rs.getString("recipient").trim(), rs.getString("text").trim());
										msg.setEncoding(MessageEncodings.ENC7BIT);
										break;
									case '8':
									{
										String text = rs.getString("text").trim();
										byte bytes[] = new byte[text.length() / 2];
										for (int i = 0; i < text.length(); i += 2)
										{
											int value = (Integer.parseInt("" + text.charAt(i), 16) * 16) + (Integer.parseInt("" + text.charAt(i + 1), 16));
											bytes[i / 2] = (byte) value;
										}
										msg = new OutboundBinaryMessage(rs.getString("recipient").trim(), bytes);
									}
										break;
									case 'U':
										msg = new OutboundMessage(rs.getString("recipient").trim(), rs.getString("text").trim());
										msg.setEncoding(MessageEncodings.ENCUCS2);
										break;
									default:
										msg = new OutboundMessage(rs.getString("recipient").trim(), rs.getString("text").trim());
										msg.setEncoding(MessageEncodings.ENC7BIT);
										break;
								}
								if (rs.getInt("flash_sms") == 1) msg.setFlashSms(true);
								if (rs.getInt("src_port") != -1)
								{
									msg.setSrcPort(rs.getInt("src_port"));
									msg.setDstPort(rs.getInt("dst_port"));
								}
								break;
							case 'W':
								Date wapExpiryDate;
								WapSISignals wapSignal;
								if (rs.getTime("wap_expiry_date") == null)
								{
									Calendar cal = Calendar.getInstance();
									cal.setTime(new Date());
									cal.add(Calendar.DAY_OF_YEAR, 7);
									wapExpiryDate = cal.getTime();
								}
								else wapExpiryDate = rs.getTimestamp("wap_expiry_date");
								if (rs.getString("wap_signal") == null) wapSignal = WapSISignals.NONE;
								else
								{
									switch (rs.getString("wap_signal").charAt(0))
									{
										case 'N':
											wapSignal = WapSISignals.NONE;
											break;
										case 'L':
											wapSignal = WapSISignals.LOW;
											break;
										case 'M':
											wapSignal = WapSISignals.MEDIUM;
											break;
										case 'H':
											wapSignal = WapSISignals.HIGH;
											break;
										case 'D':
											wapSignal = WapSISignals.DELETE;
											break;
										default:
											wapSignal = WapSISignals.NONE;
									}
								}
								msg = new OutboundWapSIMessage(rs.getString("recipient").trim(), new URL(rs.getString("wap_url").trim()), rs.getString("text").trim(), wapExpiryDate, wapSignal);
								break;
							default:
								throw new Exception("Message type '" + rs.getString("type") + "' is unknown!");
						}
						msg.setPriority(rs.getInt("priority"));
						if (rs.getInt("status_report") == 1) msg.setStatusReport(true);
						if ((rs.getString("originator") != null) && (rs.getString("originator").length() > 0)) msg.setFrom(rs.getString("originator").trim());
						msg.setGatewayId(rs.getString("gateway_id").trim());
						msgList.add(msg);
						getMessageCache().put(msg.getMessageId(), rs.getInt("id"));
						pst.setInt(1, rs.getInt("id"));
						pst.executeUpdate();
						con.commit();
						msgCount++;
					}
				}
				con.commit();
				rs.close();
				cmd.close();
				pst.close();
				break;
			}
			catch (SQLException e)
			{
				try
				{
					if (con != null) con.close();
					closeDbConnection();
				}
				catch (Exception innerE)
				{
				}
				Logger.getInstance().logError(String.format("SQL failure, will retry in %d seconds...", (sqlDelayMultiplier * (SQL_DELAY / 1000))), e, null);
				Thread.sleep(sqlDelayMultiplier * SQL_DELAY);
				sqlDelayMultiplier *= 2;
			}
		}
		return msgList;
	}

	@Override
	public int getPendingMessagesToSend() throws Exception
	{
		Connection con = null;
		int count = -1;
		while (true)
		{
			try
			{
				Statement cmd;
				ResultSet rs;
				con = getDbConnection();
				cmd = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				rs = cmd.executeQuery("select count(*) as cnt from " + getProperty("tables.sms_out", "smsserver_out") + " where status in ('U', 'Q')");
				if (rs.next()) count = rs.getInt("cnt");
				rs.close();
				cmd.close();
				break;
			}
			catch (SQLException e)
			{
				try
				{
					if (con != null) con.close();
					closeDbConnection();
				}
				catch (Exception innerE)
				{
				}
				Logger.getInstance().logError(String.format("SQL failure, will retry in %d seconds...", (sqlDelayMultiplier * (SQL_DELAY / 1000))), e, null);
				Thread.sleep(sqlDelayMultiplier * SQL_DELAY);
				sqlDelayMultiplier *= 2;
			}
		}
		return count;
	}

	@Override
	public void markMessage(OutboundMessage msg) throws Exception
	{
		Connection con = null;
		if (getMessageCache().get(msg.getMessageId()) == null) return;
		while (true)
		{
			try
			{
				PreparedStatement selectStatement, updateStatement;
				ResultSet rs;
				int errors;
				con = getDbConnection();
				selectStatement = con.prepareStatement("select errors from " + getProperty("tables.sms_out", "smsserver_out") + " where id = ?");
				selectStatement.setInt(1, getMessageCache().get(msg.getMessageId()));
				rs = selectStatement.executeQuery();
				rs.next();
				errors = rs.getInt("errors");
				rs.close();
				selectStatement.close();
				if (msg.getMessageStatus() == MessageStatuses.SENT)
				{
					updateStatement = con.prepareStatement("update " + getProperty("tables.sms_out", "smsserver_out") + " set status = ?, sent_date = ?, gateway_id = ?, ref_no = ? where id = ?");
					updateStatement.setString(1, "S");
					updateStatement.setTimestamp(2, new Timestamp(msg.getDispatchDate().getTime()));
					updateStatement.setString(3, msg.getGatewayId());
					updateStatement.setString(4, msg.getRefNo());
					updateStatement.setInt(5, getMessageCache().get(msg.getMessageId()));
					updateStatement.executeUpdate();
					con.commit();
					updateStatement.close();
				}
				else if ((msg.getMessageStatus() == MessageStatuses.UNSENT) || ((msg.getMessageStatus() == MessageStatuses.FAILED) && (msg.getFailureCause() == FailureCauses.NO_ROUTE)))
				{
					updateStatement = con.prepareStatement("update " + getProperty("tables.sms_out", "smsserver_out") + " set status = ? where id = ?");
					updateStatement.setString(1, "U");
					updateStatement.setInt(2, getMessageCache().get(msg.getMessageId()));
					updateStatement.executeUpdate();
					con.commit();
					updateStatement.close();
				}
				else
				{
					updateStatement = con.prepareStatement("update " + getProperty("tables.sms_out", "smsserver_out") + " set status = ?, errors = ? where id = ?");
					errors++;
					if (errors > Integer.parseInt(getProperty("retries", "2"))) updateStatement.setString(1, "F");
					else updateStatement.setString(1, "U");
					updateStatement.setInt(2, errors);
					updateStatement.setInt(3, getMessageCache().get(msg.getMessageId()));
					updateStatement.executeUpdate();
					con.commit();
					updateStatement.close();
				}
				break;
			}
			catch (SQLException e)
			{
				try
				{
					if (con != null) con.close();
					closeDbConnection();
				}
				catch (Exception innerE)
				{
				}
				Logger.getInstance().logError(String.format("SQL failure, will retry in %d seconds...", (sqlDelayMultiplier * (SQL_DELAY / 1000))), e, null);
				Thread.sleep(sqlDelayMultiplier * SQL_DELAY);
				sqlDelayMultiplier *= 2;
			}
		}
		getMessageCache().remove(msg.getMessageId());
	}

	private Connection getDbConnection() throws SQLException
	{
		if (dbCon == null)
		{
			dbCon = DriverManager.getConnection(getProperty("url"), getProperty("username", ""), getProperty("password", ""));
			dbCon.setAutoCommit(false);
			sqlDelayMultiplier = 1;
		}
		return dbCon;
	}

	private void closeDbConnection()
	{
		try
		{
			if (dbCon != null) dbCon.close();
		}
		catch (Exception e)
		{
		}
		finally
		{
			dbCon = null;
		}
	}
}
