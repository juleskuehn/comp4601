package edu.carleton.comp4601.utility;

import java.util.List;

import edu.carleton.comp4601.dao.Document;
import edu.carleton.comp4601.dao.DocumentCollection;

public class HTMLTableFormatter {
	
	private static final String style = "<style>table,th,td{padding:10px; border:1px; solid black;}tr:nth-child(even) "
			+ "{background-color: #f2f2f2}"
			+ "th {background-color: #4CAF50;color: white;} "
			+ "td{vertical-align:middle}</style>";
	
	StringBuffer buff;
	
	String headFormat;
	String bodyFormat;
	String footFormat;
	String coll2Title;
	boolean singleColumn;
	
	public HTMLTableFormatter(String headFormat, String bodyFormat, String footFormat) {
		this.headFormat = headFormat;
		this.bodyFormat = bodyFormat;
		this.footFormat = footFormat;
		this.buff = new StringBuffer();
		this.singleColumn = false;
		this.coll2Title = "SCORE";
	}
	
	public HTMLTableFormatter() {
		this("<html><head>%s</head>", "<body><table>%s", "%s</body></html>");
	}
	
	public String head(String head) {
		return String.format(headFormat,head);
	}
	
	public String body(String body) {
		return String.format(bodyFormat,body);
	}
	
	public String foot(String head) {
		return String.format(footFormat,head);
	}
	
	public String html(List<Document> docs) {
		buff.append(head(style));
		buff.append(body("<th>#</th><th>DOCUMENT</th>"));
		if (!singleColumn) buff.append("<th>" + coll2Title + "</th>");
		for (Document d : docs) {
			document(d);
		}
		buff.append(foot("</table>"));
		return buff.toString();
	}
	
	public String html(DocumentCollection dc) {
		return html(dc.getDocuments());
	}
	
	private void document(Document d) {
		buff.append("<tr>");
		buff.append("<td>");
		buff.append(d.getId());
		buff.append("</td>");
		buff.append("<td>");
		buff.append("<a href=\"");
		buff.append(d.getUrl());
		buff.append("\">");
		buff.append(d.getName());
		buff.append("</a>");
		buff.append("</td>");
		if (!singleColumn) {
			buff.append("<td>");
			buff.append(d.getScore());
			buff.append("</td>");
		}
		buff.append("</tr>");
	}

	public void singleColumn() {
		this.singleColumn = true;
	}

	public void setColl2Title(String title) {
		this.coll2Title = title;
	}

	public String getHeadFormat() {
		return headFormat;
	}

	public void setHeadFormat(String headFormat) {
		this.headFormat = headFormat;
	}

	public String getBodyFormat() {
		return bodyFormat;
	}

	public void setBodyFormat(String bodyFormat) {
		this.bodyFormat = bodyFormat;
	}

	public String getFootFormat() {
		return footFormat;
	}

	public void setFootFormat(String footFormat) {
		this.footFormat = footFormat;
	}

}
