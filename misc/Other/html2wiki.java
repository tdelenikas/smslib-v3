import java.io.*;
import java.util.*;

public class html2wiki
{
	public static final String IN_DIR = "../../doc/";
	//public static final String OUT_DIR = "./out/";
	public static final String OUT_DIR = "./wiki/";
	public static final String DOC_DIR = "./doc/";

/*
	public static String STYLE_H1 = "<font color='#162e6a'>";
	public static String STYLE_H2 = "<font color='#4e79a2'>";
	public static String STYLE_H3 = "<font color='#4e79a2'><i>";
	public static String STYLE_H4 = "<font color='#4e79a2'>";
	public static String STYLE_H1_END = "</font>";
	public static String STYLE_H2_END = "</font>";
	public static String STYLE_H3_END = "</i></font>";
	public static String STYLE_H4_END = "</font>";
*/

	public static String STYLE_H1 = "";
	public static String STYLE_H2 = "";
	public static String STYLE_H3 = "";
	public static String STYLE_H4 = "";
	public static String STYLE_H1_END = "";
	public static String STYLE_H2_END = "";
	public static String STYLE_H3_END = "";
	public static String STYLE_H4_END = "";

	public static final String FILES[][] =
	{
		{"index.html", "Documentation_Index.wiki", "Documentation Index", ""},
		{"about.html", "About.wiki", "Introduction to SMSLib", "about/"},
		{"bulksms.html", "Bulk_SMS_Operators.wiki", "SMSLib and Bulk SMS Operators", "bulksms/"},
		{"compatibility.html", "Compatibility.wiki", "Compatibility", "compatibility/"},
		{"faq.html", "FAQ.wiki", "Frequently Asked Questions", "faq/"},
		{"gsmerrors.html", "GSM_Errors.wiki", "GSM Errors", "gsmerrors/"},
		{"project_information.html", "Project_Information.wiki", "Project Information", "project-information/"},
		{"smslib_troubleshooting.html", "SMSLib_Troubleshooting.wiki", "Troubleshooting", "smslib/troubleshooting/"},
		{"installation.html", "Installation.wiki", "Installation Instructions", "installation/"},
		{"support.html", "Support.wiki", "Support", "support/"},
		{"smslib_quick_start.html", "SMSLib_QuickStart.wiki", "SMSLib Quick Start Guide", "smslib/quickstart/"},
		{"smslib_parameters.html", "SMSLib_Parameters.wiki", "SMSLib Configuration Parameters", "smslib/parameters/"},
		{"smslib_callbacks.html", "SMSLib_Callbacks.wiki", "SMSLib Callback Methods", "smslib/callback-methods/"},
		{"smslib_contacts.html", "SMSLib_Contacts.wiki", "SMSLib Contact Management", "smslib/contact-management/"},
		{"smslib_groups.html", "SMSLib_Groups.wiki", "SMSLib Group Management", "smslib/group-management/"},
		{"smslib_delivery_reports.html", "SMSLib_Delivery_Reports.wiki", "SMSLib Delivery Reports", "smslib/delivery-reports/"},
		{"smslib_encryption.html", "SMSLib_Encryption.wiki", "SMSLib Encrypted Messages", "smslib/encryption/"},
		{"smsserver.html", "SMSServer.wiki", "Introduction to SMSServer", "smsserver/"},
		{"smsserver_gateways.html", "SMSServer_Gateways.wiki", "SMSServer Gateways", "smsserver/gateways/"},
		{"smsserver_serial_modem_gateway.html", "SMSServer_SerialModem_Gateway.wiki", "SMSServer Serial Modem Gateway", "smsserver/gateways/serialmodem/"},
		{"smsserver_ip_modem_gateway.html", "SMSServer_IPModem_Gateway.wiki", "SMSServer IP Modem Gateway", "smsserver/gateways/ipmodem/"},
		{"smsserver_bulksms_gateway.html", "SMSServer_BulkSms_Gateway.wiki", "SMSServer BULKSMS Gateway", "smsserver/gateways/bulksms/"},
		{"smsserver_clickatell_gateway.html", "SMSServer_Clickatell_Gateway.wiki", "SMSServer Clickatell Gateway", "smsserver/gateways/clickatell/"},
		{"smsserver_kannel_gateway.html", "SMSServer_Kannel_Gateway.wiki", "SMSServer Kannel Gateway", "smsserver/gateways/kannel/"},
		{"smsserver_smpp_gateway.html", "SMSServer_Smpp_Gateway.wiki", "SMSServer SMPP Gateway", "smsserver/gateways/smpp/"},
		{"smsserver_interfaces.html", "SMSServer_Interfaces.wiki", "SMSServer Interfaces", "smsserver/interfaces/"},
		{"smsserver_db_interface.html", "SMSServer_DB_Interface.wiki", "SMSServer Database Interface", "smsserver/interfaces/database/"},
		{"smsserver_http_interface.html", "SMSServer_HTTP_Interface.wiki", "SMSServer HTTP Interface", "smsserver/interfaces/http/"},
		{"smsserver_httpserver_interface.html", "SMSServer_HTTPServer_Interface.wiki", "SMSServer HTTP Server Interface", "smsserver/interfaces/http-server/"},
		{"smslib_dotnet.html", "SMSLib_dotNET.wiki", "SMSLib - .NET Framework port", "smslib/dotnet/"},
		{"contribute.html", "Contribute.wiki", "Contribute", "contribute/"}
	};

	public void convert_wiki(String inFile, String outFile, String introText) throws Exception
	{
		String line;
		BufferedReader inReader = new BufferedReader(new FileReader(inFile));
		PrintWriter outWriter = new PrintWriter(outFile);

		outWriter.println("#summary " + introText);
		if ((inFile.indexOf("index.html") >= 0) || (inFile.indexOf("support.html") >= 0) || (inFile.indexOf("about.html") >= 0))
		{
			outWriter.println("#labels Featured");
		}
		outWriter.println();

		if (inFile.indexOf("index.html") == -1)
		{
			outWriter.println("<wiki:toc />");
			outWriter.println();
		}

		//outWriter.println("<table align='center'><tr><td><wiki:gadget url='http://smslib.googlecode.com/svn/misc/ads/ads728x15.xml' border='0' width='728' height='15' /></td></tr></table>");
		//outWriter.println();

		outWriter.println("<font face='Lucida Sans'>");
		outWriter.println();

		while ((line = inReader.readLine()) != null)
		{
			line = line.replaceAll("<code>", "{{{");
			line = line.replaceAll("</code>", "}}}");
//			line = line.replaceAll("<blockquote>", "<code>");
//			line = line.replaceAll("</blockquote>", "</code>");
			line = line.replaceAll("<blockquote>", "{{{");
			line = line.replaceAll("</blockquote>", "}}}");

			line = line.replaceAll("<h1>", "=" + STYLE_H1);
			line = line.replaceAll("</h1>", STYLE_H1_END + "=");
			line = line.replaceAll("<h2>", "==" + STYLE_H2);
			line = line.replaceAll("</h2>", STYLE_H2_END + "==");
			line = line.replaceAll("<h3>", "===" + STYLE_H3);
			line = line.replaceAll("</h3>", STYLE_H3_END + "===");
			line = line.replaceAll("<h4>", "====" + STYLE_H4);
			line = line.replaceAll("</h4>", STYLE_H4_END + "====");

			line = line.replaceAll("<br />", "");

			if (line.indexOf("<pre>") == 0) continue;
			if (line.indexOf("</pre>") == 0) continue;

			line = line.replaceAll("&gt;", ">");
			line = line.replaceAll("&lt;", "<");

			if (line.indexOf("<table") == 0) continue;
			if (line.indexOf("</table") == 0) continue;
			line = line.replaceAll("<tr><td>", "||");
			line = line.replaceAll("</td></tr>", "||");
			line = line.replaceAll("</td><td>", "||");

			line = line.replaceAll("</li>", "");
			line = line.replaceAll("<li>", "  * ");
			if (line.trim().indexOf("<ul>") == 0) continue;
			if (line.trim().indexOf("</ul>") == 0) continue;

			for (int i = 0; i < FILES.length; i ++)
			{
				if (line.indexOf(FILES[i][0]) >= 0)
				{
					line = line.replaceAll("<a href=\"" + FILES[i][0] + "\">", "[" + FILES[i][1].replaceAll(".wiki", "") + " ");
					line = line.replaceAll("</a>", "]");
				}
			}
			outWriter.println(line);
		}

		inReader.close();
		outWriter.close();
	}

	public void convert_doc(String inFile, String outDir, String wikiPage) throws Exception
	{
		File dir = new File(outDir);
		dir.mkdirs();
		BufferedReader inReader = new BufferedReader(new FileReader(inFile));
		//PrintWriter outWriter = new PrintWriter(outDir + "index.jsp");
		PrintWriter outWriter = new PrintWriter(outDir + "index.php");
		String line, docHtml, toc, tocLine, tocHtml;

		docHtml = "";
		toc = "";

		//outWriter.println("<%@include file=\"/include/header.inc\" %>");
		outWriter.println("<? include $_SERVER['DOCUMENT_ROOT'] . '/include/header.inc' ?>");
		outWriter.println("<div class='PageWidth'>");
		outWriter.println("<div class='MainBlock'>");

		//if (inFile.endsWith("/index.html"))
		//{
		//	outWriter.println("<div style='float: right; margin-left: 30px; margin-bottom: 30px; margin-right: 0px;'>");
		//	outWriter.println("<script type='text/javascript'><!--\ngoogle_ad_client = 'pub-3031093402442295';\n/* Doc-160x600 */\ngoogle_ad_slot = '6485399388';\ngoogle_ad_width = 160;\ngoogle_ad_height = 600;\n//-->\n</script>		<script type='text/javascript'\nsrc='http://pagead2.googlesyndication.com/pagead/show_ads.js'>\n</script>");
		//	outWriter.println("</div>");
		//}

		while ((line = inReader.readLine()) != null)
		{
			if (line.indexOf("<h1>") >= 0)
			{
				tocLine = line;
				tocLine = tocLine.replaceAll("<h1>", "");
				tocLine = tocLine.replaceAll("</h1>", "");
				tocLine = tocLine.replaceAll("\r", "");
				tocLine = tocLine.replaceAll("\n", "");
				toc += "|1" + tocLine;
				docHtml += "<a name='" + toHtml(tocLine) + "'></a>";
			}
			if (line.indexOf("<h2>") >= 0)
			{
				tocLine = line;
				tocLine = tocLine.replaceAll("<h2>", "");
				tocLine = tocLine.replaceAll("</h2>", "");
				tocLine = tocLine.replaceAll("\r", "");
				tocLine = tocLine.replaceAll("\n", "");
				toc += "|2" + tocLine;
				docHtml += "<a name='" + toHtml(tocLine) + "'></a>";
			}
			if (line.indexOf("<h3>") >= 0)
			{
				tocLine = line;
				tocLine = tocLine.replaceAll("<h3>", "");
				tocLine = tocLine.replaceAll("</h3>", "");
				tocLine = tocLine.replaceAll("\r", "");
				tocLine = tocLine.replaceAll("\n", "");
				toc += "|3" + tocLine;
				docHtml += "<a name='" + toHtml(tocLine) + "'></a>";
			}
			if (line.indexOf("<h4>") >= 0)
			{
				tocLine = line;
				tocLine = tocLine.replaceAll("<h4>", "");
				tocLine = tocLine.replaceAll("</h4>", "");
				tocLine = tocLine.replaceAll("\r", "");
				tocLine = tocLine.replaceAll("\n", "");
				toc += "|4" + tocLine;
				docHtml += "<a name='" + toHtml(tocLine) + "'></a>";
			}

			for (int i = 0; i < FILES.length; i ++)
			{
				if (line.indexOf("\"" + FILES[i][0] + "\"") >= 0)
					line = line.replaceAll(FILES[i][0], DOC_DIR.substring(1) + FILES[i][3]);
			}

			docHtml += line + '\n';
		}

		tocHtml = "";
		StringTokenizer tokens = new StringTokenizer(toc, "|");
		while (tokens.hasMoreTokens())
		{
			String t = tokens.nextToken();
			tocHtml = tocHtml + "<ul style='line-height:1.2em; padding-left:" + (Integer.parseInt(t.substring(0,1)) * 14) + "px; margin:6px;'><li><a href='#" + toHtml(t.substring(1)) + "'>" + t.substring(1) + "</a></li></ul>";
		}

		if (!inFile.endsWith("/index.html"))
		{
			tocHtml = "<div style='float:right; background-color:#f0f0f0; border: thin solid #000000; padding:10px 10px 10px 10px; margin-left:10px; width:300px;'><p style='text-align:center; font-weight:bold; font-size:100%;'>Table Of Contents</p>" + tocHtml + "</div>";
			outWriter.println(tocHtml);
		}

		outWriter.println(docHtml);

/*
		if (!inFile.endsWith("/index.html")) outWriter.println("<br /><br /><center>Do you have something to add? Is something wrong?<br />Please leave a comment on the <a href='http://code.google.com/p/smslib/wiki/" + wikiPage + "'>wiki page</a> (note: Google account required)</center>");
*/

		//if (!inFile.endsWith("/index.html")) outWriter.println("<p class='Feedback'>Have a comment to share?</p><div style='width:100%; background-color:#fbfbfb; margin-left:auto; margin-right:auto;' id='disqus_thread'></div><script type='text/javascript'>  /**    * var disqus_identifier; [Optional but recommended: Define a unique identifier (e.g. post id or slug) for this thread]     */  (function() {   var dsq = document.createElement('script'); dsq.type = 'text/javascript'; dsq.async = true;   dsq.src = 'http://smslib.disqus.com/embed.js';   (document.getElementsByTagName('head')[0] || document.getElementsByTagName('body')[0]).appendChild(dsq);  })();</script><noscript>Please enable JavaScript to view the <a href='http://disqus.com/?ref_noscript=smslib'>comments powered by Disqus.</a></noscript><a href='http://disqus.com' class='dsq-brlink'>blog comments powered by <span class='logo-disqus'>Disqus</span></a><br /><br />");
		outWriter.println("</div>");
		outWriter.println("</div>");
		//outWriter.println("<%@include file=\"/include/footer.inc\" %>");
		outWriter.println("<? include $_SERVER['DOCUMENT_ROOT'] . '/include/footer.inc' ?>");

		inReader.close();
		outWriter.close();
	}

	public String toHtml(String s)
	{
		StringBuffer b = new StringBuffer(128);
		for (int i = 0; i < s.length(); i ++)
		{
			if (s.charAt(i) == '?');
			else if (s.charAt(i) == '\'');
			else if (s.charAt(i) == '/');
			else if (s.charAt(i) == '(');
			else if (s.charAt(i) == ')');
			else if (s.charAt(i) == ' ') b.append('_');
			else b.append(s.charAt(i));
		}
		return b.toString().replaceAll("__", "_");
	}

	public static void main(String[] args)
	{
		html2wiki app = new html2wiki();

		try
		{
			for (int i = 0; i < FILES.length; i ++)
			{
				System.out.println("Converting : " + FILES[i][0]);
				app.convert_wiki(IN_DIR + FILES[i][0], OUT_DIR + FILES[i][1], FILES[i][2]);
				app.convert_doc(IN_DIR + FILES[i][0], DOC_DIR + FILES[i][3], FILES[i][1].replaceAll(".wiki", ""));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
	