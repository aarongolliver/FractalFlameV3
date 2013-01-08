package processing.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import processing.core.PApplet;

public class TableODS extends Table {

	public TableODS(final File odsFile) {
		this(getContentXML(odsFile), null, false);
	}

	public TableODS(final File odsFile, final boolean actual) {
		this(getContentXML(odsFile), null, actual);
	}

	public TableODS(final PApplet parent, final String filename) {
		this(getContentXML(parent.createInput(filename)), null, false);
	}

	public TableODS(final PApplet parent, final String filename, final boolean actual) {
		this(getContentXML(parent.createInput(filename)), null, actual);
	}

	public TableODS(final PApplet parent, final String filename, final String worksheet, final boolean actual) {
		this(getContentXML(parent.createInput(filename)), worksheet, actual);
	}

	/**
	 * Parse spreadsheet content.
	 * 
	 * @param input
	 *            InputStream of the content.xml file inside the .ods
	 */
	protected TableODS(final InputStream input, final String worksheet, final boolean actual) {
		try {
			// InputStreamReader isr = new InputStreamReader(input, "UTF-8");
			// BufferedReader reader = new BufferedReader(isr);
			// read(reader, worksheet, actual);
			read(input, worksheet, actual);

		} catch (final UnsupportedEncodingException uee) {
			uee.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final ParserConfigurationException e) {
			e.printStackTrace();
		} catch (final SAXException e) {
			e.printStackTrace();
		}
	}

	// protected void read(BufferedReader reader, String worksheet, boolean actual) throws
	// IOException, ParserConfigurationException, SAXException {
	// XML xml = new XML(reader);
	protected void read(final InputStream input, final String worksheet, final boolean actual) throws IOException,
	        ParserConfigurationException, SAXException {
		final XML xml = new XML(input);

		// XML x = new XML(reader);
		// PApplet.saveStrings(new File("/Users/fry/Desktop/namespacefix.xml"), new String[] {
		// xml.toString() });
		// PApplet.saveStrings(new File("/Users/fry/Desktop/newparser.xml"), new String[] {
		// x.toString() });

		// table files will have multiple sheets.. argh
		// <table:table table:name="Sheet1" table:style-name="ta1" table:print="false">
		// <table:table table:name="Sheet2" table:style-name="ta1" table:print="false">
		// <table:table table:name="Sheet3" table:style-name="ta1" table:print="false">

		final XML[] sheets = xml.getChildren("office:body/office:spreadsheet/table:table");
		// xml.getChildren("office:body/office:spreadsheet/table:table/table");
		// System.out.println("found " + sheets.length + " sheets.");

		for (final XML sheet : sheets) {
			// System.out.println(sheet.getAttribute("table:name"));
			if ((worksheet == null) || worksheet.equals(sheet.getString("table:name"))) {
				parseSheet(sheet, actual);
			}
		}
	}

	protected void parseSheet(final XML sheet, final boolean actual) {
		final XML[] rows = sheet.getChildren("table:table-row");
		// xml.getChildren("office:body/office:spreadsheet/table:table/table:table-row");

		int rowIndex = 0;
		for (final XML row : rows) {
			final int rowRepeat = row.getInt("table:number-rows-repeated", 1);
			// if (rowRepeat != 1) {
			// System.out.println(rowRepeat + " " + rowCount + " " + (rowCount + rowRepeat));
			// }
			boolean rowNotNull = false;
			final XML[] cells = row.getChildren();
			int columnIndex = 0;

			for (final XML cell : cells) {
				final int cellRepeat = cell.getInt("table:number-columns-repeated", 1);

				// <table:table-cell table:formula="of:=SUM([.E7:.E8])" office:value-type="float"
				// office:value="4150">
				// <text:p>4150.00</text:p>
				// </table:table-cell>

				String cellData = actual ? cell.getString("office:value") : null;

				// if there's an office:value in the cell, just roll with that
				if (cellData == null) {
					final int cellKids = cell.getChildCount();
					if (cellKids != 0) {
						final XML[] paragraphElements = cell.getChildren("text:p");
						if (paragraphElements.length != 1) {
							for (final XML el : paragraphElements) {
								System.err.println(el.toString());
							}
							throw new RuntimeException("found more than one text:p element");
						}
						final XML textp = paragraphElements[0];
						final String textpContent = textp.getContent();
						// if there are sub-elements, the content shows up as a child element
						// (for which getName() returns null.. which seems wrong)
						if (textpContent != null) {
							cellData = textpContent; // nothing fancy, the text is in the text:p
							                         // element
						} else {
							final XML[] textpKids = textp.getChildren();
							final StringBuffer cellBuffer = new StringBuffer();
							for (final XML kid : textpKids) {
								final String kidName = kid.getName();
								if (kidName == null) {
									appendNotNull(kid, cellBuffer);

								} else if (kidName.equals("text:s")) {
									final int spaceCount = kid.getInt("text:c", 1);
									for (int space = 0; space < spaceCount; space++) {
										cellBuffer.append(' ');
									}
								} else if (kidName.equals("text:span")) {
									appendNotNull(kid, cellBuffer);

								} else if (kidName.equals("text:a")) {
									// <text:a xlink:href="http://blah.com/">blah.com</text:a>
									if (actual) {
										cellBuffer.append(kid.getString("xlink:href"));
									} else {
										appendNotNull(kid, cellBuffer);
									}

								} else {
									appendNotNull(kid, cellBuffer);
									System.err.println(getClass().getName() + ": don't understand: " + kid);
									// throw new RuntimeException("I'm not used to this.");
								}
							}
							cellData = cellBuffer.toString();
						}
						// setString(rowIndex, columnIndex, c); //text[0].getContent());
						// columnIndex++;
					}
				}
				for (int r = 0; r < cellRepeat; r++) {
					if (cellData != null) {
						// System.out.println("setting " + rowIndex + "," + columnIndex + " to " +
						// cellData);
						setString(rowIndex, columnIndex, cellData);
					}
					columnIndex++;
					if (cellData != null) {
						// if (columnIndex > columnMax) {
						// columnMax = columnIndex;
						// }
						rowNotNull = true;
					}
				}
			}
			if (rowNotNull && (rowRepeat > 1)) {
				final String[] rowStrings = getStringRow(rowIndex);
				for (int r = 1; r < rowRepeat; r++) {
					addRow(rowStrings);
				}
			}
			rowIndex += rowRepeat;
			// if (rowNotNull) {
			// rowMax = rowIndex;
			// }
		}
		// if (rowMax != getRowCount()) {
		// System.out.println("removing empty rows: " + rowMax + " instead of " + getRowCount());
		// setRowCount(rowMax);
		// }
		// if (columnMax != getColumnCount()) {
		// System.out.println("removing empty columns: " + columnMax + " instead of " +
		// getColumnCount());
		// setColumnCount(columnMax);
		// }
	}

	protected void appendNotNull(final XML kid, final StringBuffer buffer) {
		final String content = kid.getContent();
		if (content != null) {
			buffer.append(content);
		}
	}

	// static public PNode getContentXML(File file) {
	// return new PNode(getContentReader(file));
	// }

	// static public BufferedReader getContentReader(File file) {
	// return PApplet.createReader(getContentInput(file));
	// }

	/**
	 * Read zip file from a local file, and return the InputStream for content.xml.
	 */
	static protected InputStream getContentXML(final File file) {
		try {
			@SuppressWarnings("resource")
			final ZipFile zip = new ZipFile(file);
			final ZipEntry entry = zip.getEntry("content.xml");
			return zip.getInputStream(entry);

		} catch (final IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Read zip file from an InputStream, and return the InputStream for content.xml.
	 */
	static protected InputStream getContentXML(final InputStream input) {
		final ZipInputStream zis = new ZipInputStream(input);
		ZipEntry entry = null;
		try {
			while ((entry = zis.getNextEntry()) != null) {
				if (entry.getName().equals("content.xml")) { return zis;
				// InputStreamReader isr = new InputStreamReader(zis);
				// BufferedReader reader = new BufferedReader(isr);
				// read(reader, actual);
				// break;
				// return entry.getInputStream();
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}