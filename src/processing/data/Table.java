/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
 * Part of the Processing project - http://processing.org Copyright (c) 2011-12 Ben Fry and Casey
 * Reas Copyright (c) 2006-11 Ben Fry This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License version 2.1 as published by
 * the Free Software Foundation. This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this library; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package processing.data;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import processing.core.PApplet;
import processing.core.PConstants;

// function that will convert awful CSV to TSV.. or something else?
// maybe to write binary instead? then read the binary file once it's ok?

// if loading from a File object (or PApplet is passed in and we can check online)
// then check the (probable) size of the file before loading

// implement binary tables

// no column max/min functions since it needs to be per-datatype
// better to use float mx = max(float(getColumn(3)));
// *** but what to do with null entries?

// todo: need a method to reset the row/column indices after add/remove
// or just make sure that it's covered for all cases

// no longer the case, ja?
// <p>By default, empty rows are skipped and so are lines that start with the
// # character. Using # at the beginning of a line indicates a comment.</p>

// attempt at a CSV spec: http://tools.ietf.org/html/rfc4180

/**
 * <p>
 * Generic class for handling tabular data, typically from a CSV, TSV, or other sort of spreadsheet
 * file.
 * </p>
 * <p>
 * CSV files are <a href="http://en.wikipedia.org/wiki/Comma-separated_values">comma separated
 * values</a>, often with the data in quotes. TSV files use tabs as separators, and usually don't
 * bother with the quotes.
 * </p>
 * <p>
 * File names should end with .csv if they're comma separated.
 * </p>
 * 
 * @webref data:composite
 */
public class Table {
	protected int	         rowCount;

	// protected boolean skipEmptyRows = true;
	// protected boolean skipCommentLines = true;
	// protected String extension = null;
	// protected boolean commaSeparatedValues = false;
	// protected boolean awfulCSV = false;

	protected String	     missingString	 = null;
	protected int	         missingInt	     = 0;
	protected long	         missingLong	 = 0;
	protected float	         missingFloat	 = Float.NaN;
	protected double	     missingDouble	 = Double.NaN;
	protected int	         missingCategory	= -1;

	String[]	             columnTitles;
	HashMapBlows[]	         columnCategories;
	HashMap<String, Integer>	columnIndices;

	protected Object[]	     columns;	                   // [column]

	static final int	     STRING	         = 0;
	static final int	     INT	         = 1;
	static final int	     LONG	         = 2;
	static final int	     FLOAT	         = 3;
	static final int	     DOUBLE	         = 4;
	static final int	     CATEGORICAL	 = 5;
	int[]	                 columnTypes;

	protected RowIterator	 rowIterator;

	/**
	 * Creates a new, empty table. Use addRow() to add additional rows.
	 */
	public Table() {
		init();
	}

	public Table(final File file) throws IOException {
		this(file, null);
	}

	// version that uses a File object; future releases (or data types)
	// may include additional optimizations here
	public Table(final File file, final String options) throws IOException {
		parse(new FileInputStream(file), checkOptions(file, options));
	}

	public Table(final InputStream input) throws IOException {
		this(input, null);
	}

	/**
	 * Read the table from a stream. Possible options include:
	 * <ul>
	 * <li>csv - parse the table as comma-separated values
	 * <li>tsv - parse the table as tab-separated values
	 * <li>newlines - this CSV file contains newlines inside individual cells
	 * <li>header - this table has a header (title) row
	 * </ul>
	 * 
	 * @param input
	 * @param options
	 * @throws IOException
	 */
	public Table(final InputStream input, final String options) throws IOException {
		parse(input, options);
	}

	public Table(final ResultSet rs) {
		init();
		try {
			final ResultSetMetaData rsmd = rs.getMetaData();

			final int columnCount = rsmd.getColumnCount();
			setColumnCount(columnCount);

			for (int col = 0; col < columnCount; col++) {
				setColumnTitle(col, rsmd.getColumnName(col + 1));

				final int type = rsmd.getColumnType(col + 1);
				switch (type) { // TODO these aren't tested. nor are they complete.
				case Types.INTEGER:
				case Types.TINYINT:
				case Types.SMALLINT:
					setColumnType(col, INT);
					break;
				case Types.BIGINT:
					setColumnType(col, LONG);
					break;
				case Types.FLOAT:
					setColumnType(col, FLOAT);
					break;
				case Types.DECIMAL:
				case Types.DOUBLE:
				case Types.REAL:
					setColumnType(col, DOUBLE);
					break;
				}
			}

			int row = 0;
			while (rs.next()) {
				for (int col = 0; col < columnCount; col++) {
					switch (columnTypes[col]) {
					case STRING:
						setString(row, col, rs.getString(col + 1));
						break;
					case INT:
						setInt(row, col, rs.getInt(col + 1));
						break;
					case LONG:
						setLong(row, col, rs.getLong(col + 1));
						break;
					case FLOAT:
						setFloat(row, col, rs.getFloat(col + 1));
						break;
					case DOUBLE:
						setDouble(row, col, rs.getDouble(col + 1));
						break;
					default:
						throw new IllegalArgumentException("column type " + columnTypes[col] + " not supported.");
					}
				}
				row++;
				// String[] row = new String[columnCount];
				// for (int col = 0; col < columnCount; col++) {
				// row[col] = rs.get(col + 1);
				// }
				// addRow(row);
			}

		} catch (final SQLException s) {
			throw new RuntimeException(s);
		}
	}

	protected void init() {
		columns = new Object[0];
		columnTypes = new int[0];
		columnCategories = new HashMapBlows[0];
	}

	protected String checkOptions(final File file, String options) throws IOException {
		String extension = null;
		final String filename = file.getName();
		final int dotIndex = filename.lastIndexOf('.');
		if (dotIndex != -1) {
			extension = filename.substring(dotIndex + 1).toLowerCase();
			if (!extension.equals("csv") && !extension.equals("tsv")) {
				// ignore extension
				extension = null;
			}
		}
		if (extension == null) {
			if (options == null) { throw new IOException(
			        "This table filename has no extension, and no options are set."); }
		} else { // extension is not null
			if (options == null) {
				options = extension;
			} else {
				// prepend the extension, it will be overridden if there's an option for it.
				options = extension + "," + options;
			}
		}
		return options;
	}

	protected void parse(final InputStream input, final String options) throws IOException {
		init();

		boolean awfulCSV = false;
		boolean header = false;
		String extension = null;
		if (options != null) {
			final String[] opts = PApplet.splitTokens(options, " ,");
			for (final String opt : opts) {
				if (opt.equals("tsv")) {
					extension = "tsv";
				} else if (opt.equals("csv")) {
					extension = "csv";
				} else if (opt.equals("newlines")) {
					awfulCSV = true;
				} else if (opt.equals("header")) {
					header = true;
				} else {
					throw new IllegalArgumentException("'" + opt + "' is not a valid option for loading a Table");
				}
			}
		}

		final BufferedReader reader = PApplet.createReader(input);
		if (awfulCSV) {
			parseAwfulCSV(reader, header);
		} else if ("tsv".equals(extension)) {
			parseBasic(reader, header, true);
		} else if ("csv".equals(extension)) {
			parseBasic(reader, header, false);
		}
	}

	protected void parseBasic(final BufferedReader reader, boolean header, final boolean tsv) throws IOException {
		String line = null;
		int row = 0;
		if (rowCount == 0) {
			setRowCount(10);
		}
		// int prev = 0; //-1;
		while ((line = reader.readLine()) != null) {
			if (row == getRowCount()) {
				setRowCount(row << 1);
			}
			if ((row == 0) && header) {
				setColumnTitles(tsv ? PApplet.split(line, '\t') : splitLineCSV(line));
				header = false;
			} else {
				setRow(row, tsv ? PApplet.split(line, '\t') : splitLineCSV(line));
				row++;
			}

			/*
			 * // this is problematic unless we're going to calculate rowCount first if (row % 10000
			 * == 0) { if (row < rowCount) { int pct = (100 * row) / rowCount; if (pct != prev) { //
			 * also prevents "0%" from showing up System.out.println(pct + "%"); prev = pct; } } try
			 * { Thread.sleep(5); } catch (InterruptedException e) { e.printStackTrace(); } }
			 */
		}
		// shorten or lengthen based on what's left
		if (row != getRowCount()) {
			setRowCount(row);
		}
	}

	// public void convertTSV(BufferedReader reader, File outputFile) throws IOException {
	// convertBasic(reader, true, outputFile);
	// }

	protected void parseAwfulCSV(final BufferedReader reader, boolean header) throws IOException {
		char[] c = new char[100];
		int count = 0;
		boolean insideQuote = false;
		int row = 0;
		int col = 0;
		int ch;
		while ((ch = reader.read()) != -1) {
			if (insideQuote) {
				if (ch == '\"') {
					// this is either the end of a quoted entry, or a quote character
					reader.mark(1);
					if (reader.read() == '\"') {
						// it's "", which means a quote character
						if (count == c.length) {
							c = PApplet.expand(c);
						}
						c[count++] = '\"';
					} else {
						// nope, just the end of a quoted csv entry
						reader.reset();
						insideQuote = false;
						// TODO nothing here that prevents bad csv data from showing up
						// after the quote and before the comma...
						// set(row, col, new String(c, 0, count));
						// count = 0;
						// col++;
						// insideQuote = false;
					}
				} else { // inside a quote, but the character isn't a quote
					if (count == c.length) {
						c = PApplet.expand(c);
					}
					c[count++] = (char) ch;
				}
			} else { // not inside a quote
				if (ch == '\"') {
					insideQuote = true;

				} else if ((ch == '\r') || (ch == '\n')) {
					if (ch == '\r') {
						// check to see if next is a '\n'
						reader.mark(1);
						if (reader.read() != '\n') {
							reader.reset();
						}
					}
					setString(row, col, new String(c, 0, count));
					count = 0;
					if ((row == 0) && header) {
						// Use internal row removal (efficient because only one row).
						removeTitleRow();
						// Un-set the header variable so that next time around, we don't
						// just get stuck into a loop, removing the 0th row repeatedly.
						header = false;
					}
					row++;
					col = 0;

				} else if (ch == ',') {
					setString(row, col, new String(c, 0, count));
					count = 0;
					// starting a new column, make sure we have room
					col++;
					checkColumn(col);

				} else { // just a regular character, add it
					if (count == c.length) {
						c = PApplet.expand(c);
					}
					c[count++] = (char) ch;
				}
			}
		}
		// catch any leftovers
		if (count > 0) {
			setString(row, col, new String(c, 0, count));
		}
	}

	/**
	 * Parse a line of text as comma-separated values, returning each value as one entry in an array
	 * of String objects. Remove quotes from entries that begin and end with them, and convert
	 * 'escaped' quotes to actual quotes.
	 * 
	 * @param line
	 *            line of text to be parsed
	 * @return an array of the individual values formerly separated by commas
	 */
	static protected String[] splitLineCSV(final String line) {
		final char[] c = line.toCharArray();
		int rough = 1; // at least one
		boolean quote = false;
		for (final char element : c) {
			if (!quote && (element == ',')) {
				rough++;
			} else if (element == '\"') {
				quote = !quote;
			}
		}
		final String[] pieces = new String[rough];
		int pieceCount = 0;
		int offset = 0;
		while (offset < c.length) {
			int start = offset;
			int stop = nextComma(c, offset);
			offset = stop + 1; // next time around, need to step over the comment
			if ((c[start] == '\"') && (c[stop - 1] == '\"')) {
				start++;
				stop--;
			}
			int i = start;
			int ii = start;
			while (i < stop) {
				if (c[i] == '\"') {
					i++; // skip over pairs of double quotes become one
				}
				if (i != ii) {
					c[ii] = c[i];
				}
				i++;
				ii++;
			}
			final String s = new String(c, start, ii - start);
			pieces[pieceCount++] = s;
		}
		// make any remaining entries blanks instead of nulls
		for (int i = pieceCount; i < pieces.length; i++) {
			pieces[i] = "";

		}
		return pieces;
	}

	static protected int nextComma(final char[] c, final int index) {
		boolean quote = false;
		for (int i = index; i < c.length; i++) {
			if (!quote && (c[i] == ',')) {
				return i;
			} else if (c[i] == '\"') {
				quote = !quote;
			}
		}
		return c.length;
	}

	// A 'Class' object is used here, so the syntax for this function is:
	// Table t = loadTable("cars3.tsv", "header");
	// Record[] records = (Record[]) t.parse(Record.class);
	// While t.parse("Record") might be nicer, the class is likely to be an
	// inner class (another tab in a PDE sketch) or even inside a package,
	// so additional information would be needed to locate it. The name of the
	// inner class would be "SketchName$Record" which isn't acceptable syntax
	// to make people use. Better to just introduce the '.class' syntax.

	// Unlike the Table class itself, this accepts char and boolean fields in
	// the target class, since they're much more prevalent, and don't require
	// a zillion extra methods and special cases in the rest of the class here.

	// since this is likely an inner class, needs a reference to its parent,
	// because that's passed to the constructor parameter (inserted by the
	// compiler) of an inner class by the runtime.

	/** incomplete, do not use */
	public void parseInto(final Object enclosingObject, final String fieldName) {
		Class<?> target = null;
		Object outgoing = null;
		Field targetField = null;
		try {
			// Object targetObject,
			// Class target -> get this from the type of fieldName
			// Class sketchClass = sketch.getClass();
			final Class<?> sketchClass = enclosingObject.getClass();
			targetField = sketchClass.getDeclaredField(fieldName);
			// PApplet.println("found " + targetField);
			final Class<?> targetArray = targetField.getType();
			if (!targetArray.isArray()) {
				// fieldName is not an array
			} else {
				target = targetArray.getComponentType();
				outgoing = Array.newInstance(target, getRowCount());
			}
		} catch (final NoSuchFieldException e) {
			e.printStackTrace();
		} catch (final SecurityException e) {
			e.printStackTrace();
		}

		// Object enclosingObject = sketch;
		// PApplet.println("enclosing obj is " + enclosingObject);
		final Class<?> enclosingClass = target.getEnclosingClass();
		Constructor<?> con = null;

		try {
			if (enclosingClass == null) {
				con = target.getDeclaredConstructor(); // new Class[] { });
				// PApplet.println("no enclosing class");
			} else {
				con = target.getDeclaredConstructor(new Class[] { enclosingClass });
				// PApplet.println("enclosed by " + enclosingClass.getName());
			}
			if (!con.isAccessible()) {
				// System.out.println("setting constructor to public");
				con.setAccessible(true);
			}
		} catch (final SecurityException e) {
			e.printStackTrace();
		} catch (final NoSuchMethodException e) {
			e.printStackTrace();
		}

		final Field[] fields = target.getDeclaredFields();
		final ArrayList<Field> inuse = new ArrayList<Field>();
		for (final Field field : fields) {
			final String name = field.getName();
			if (getColumnIndex(name, false) != -1) {
				// System.out.println("found field " + name);
				if (!field.isAccessible()) {
					// PApplet.println("  changing field access");
					field.setAccessible(true);
				}
				inuse.add(field);
			} else {
				// System.out.println("skipping field " + name);
			}
		}

		int index = 0;
		try {
			for (final TableRow row : rows()) {
				Object item = null;
				if (enclosingClass == null) {
					// item = target.newInstance();
					item = con.newInstance();
				} else {
					item = con.newInstance(new Object[] { enclosingObject });
				}
				// Object item = defaultCons.newInstance(new Object[] { });
				for (final Field field : inuse) {
					final String name = field.getName();
					// PApplet.println("gonna set field " + name);

					if (field.getType() == String.class) {
						field.set(item, row.getString(name));

					} else if (field.getType() == Integer.TYPE) {
						field.setInt(item, row.getInt(name));

					} else if (field.getType() == Long.TYPE) {
						field.setLong(item, row.getLong(name));

					} else if (field.getType() == Float.TYPE) {
						field.setFloat(item, row.getFloat(name));

					} else if (field.getType() == Double.TYPE) {
						field.setDouble(item, row.getDouble(name));

					} else if (field.getType() == Boolean.TYPE) {
						final String content = row.getString(name);
						if (content != null) {
							// Only bother setting if it's true,
							// otherwise false by default anyway.
							if (content.toLowerCase().equals("true") || content.equals("1")) {
								field.setBoolean(item, true);
							}
						}
						// if (content == null) {
						// field.setBoolean(item, false); // necessary?
						// } else if (content.toLowerCase().equals("true")) {
						// field.setBoolean(item, true);
						// } else if (content.equals("1")) {
						// field.setBoolean(item, true);
						// } else {
						// field.setBoolean(item, false); // necessary?
						// }
					} else if (field.getType() == Character.TYPE) {
						final String content = row.getString(name);
						if ((content != null) && (content.length() > 0)) {
							// Otherwise set to \0 anyway
							field.setChar(item, content.charAt(0));
						}
					}
				}
				// list.add(item);
				Array.set(outgoing, index++, item);
			}
			if (!targetField.isAccessible()) {
				// PApplet.println("setting target field to public");
				targetField.setAccessible(true);
			}
			// Set the array in the sketch
			// targetField.set(sketch, outgoing);
			targetField.set(enclosingObject, outgoing);

		} catch (final InstantiationException e) {
			e.printStackTrace();
		} catch (final IllegalAccessException e) {
			e.printStackTrace();
		} catch (final IllegalArgumentException e) {
			e.printStackTrace();
		} catch (final InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public void save(final File file, final String options) throws IOException {
		save(new FileOutputStream(file), checkOptions(file, options));
	}

	public void save(final OutputStream output, final String options) {
		final PrintWriter writer = PApplet.createWriter(output);
		if (options != null) {
			final String[] opts = PApplet.splitTokens(options, ", ");
			for (final String opt : opts) {
				if (opt.equals("csv")) {
					writeCSV(writer);
				} else if (opt.equals("tsv")) {
					writeTSV(writer);
				} else if (opt.equals("html")) {
					writeHTML(writer);
				} else {
					throw new IllegalArgumentException("'" + opt + "' not understood. "
					        + "Only csv, tsv, and html are " + "accepted as save parameters");
				}
			}
		}
		writer.close();
	}

	protected void writeTSV(final PrintWriter writer) {
		if (columnTitles != null) {
			for (int col = 0; col < columns.length; col++) {
				if (col != 0) {
					writer.print('\t');
				}
				if (columnTitles[col] != null) {
					writer.print(columnTitles[col]);
				}
			}
			writer.println();
		}
		for (int row = 0; row < rowCount; row++) {
			for (int col = 0; col < getColumnCount(); col++) {
				if (col != 0) {
					writer.print('\t');
				}
				final String entry = getString(row, col);
				// just write null entries as blanks, rather than spewing 'null'
				// all over the spreadsheet file.
				if (entry != null) {
					writer.print(entry);
				}
			}
			writer.println();
		}
		writer.flush();
	}

	protected void writeCSV(final PrintWriter writer) {
		if (columnTitles != null) {
			for (int col = 0; col < columns.length; col++) {
				if (col != 0) {
					writer.print(',');
				}
				if (columnTitles[col] != null) {
					writeEntryCSV(writer, columnTitles[col]);
				}
			}
			writer.println();
		}
		for (int row = 0; row < rowCount; row++) {
			for (int col = 0; col < getColumnCount(); col++) {
				if (col != 0) {
					writer.print(',');
				}
				final String entry = getString(row, col);
				// just write null entries as blanks, rather than spewing 'null'
				// all over the spreadsheet file.
				if (entry != null) {
					writeEntryCSV(writer, entry);
				}
			}
			// Prints the newline for the row, even if it's missing
			writer.println();
		}
		writer.flush();
	}

	protected void writeEntryCSV(final PrintWriter writer, final String entry) {
		if (entry != null) {
			if (entry.indexOf('\"') != -1) { // convert quotes to double quotes
				final char[] c = entry.toCharArray();
				writer.print('\"');
				for (final char element : c) {
					if (element == '\"') {
						writer.print("\"\"");
					} else {
						writer.print(element);
					}
				}
				writer.print('\"');

				// add quotes if commas or CR/LF are in the entry
			} else if ((entry.indexOf(',') != -1) || (entry.indexOf('\n') != -1) || (entry.indexOf('\r') != -1)) {
				writer.print('\"');
				writer.print(entry);
				writer.print('\"');

				// add quotes if leading or trailing space
			} else if ((entry.length() > 0) && ((entry.charAt(0) == ' ') || (entry.charAt(entry.length() - 1) == ' '))) {
				writer.print('\"');
				writer.print(entry);
				writer.print('\"');

			} else {
				writer.print(entry);
			}
		}
	}

	protected void writeHTML(final PrintWriter writer) {
		writer.println("<html>");

		writer.println("<head>");
		writer.println("  <meta http-equiv=\"content-type\" content=\"text/html;charset=utf-8\" />");
		writer.println("</head>");

		writer.println("<body>");
		writer.println("  <table>");
		for (int row = 0; row < getRowCount(); row++) {
			writer.println("    <tr>");
			for (int col = 0; col < getColumnCount(); col++) {
				final String entry = getString(row, col);
				writer.print("      <td>");
				writeEntryHTML(writer, entry);
				writer.println("      </td>");
			}
			writer.println("    </tr>");
		}
		writer.println("  </table>");
		writer.println("</body>");

		writer.println("</hmtl>");
		writer.flush();
	}

	protected void writeEntryHTML(final PrintWriter writer, final String entry) {
		// char[] chars = entry.toCharArray();
		for (final char c : entry.toCharArray()) { // chars) {
			if (c == '<') {
				writer.print("&lt;");
			} else if (c == '>') {
				writer.print("&gt;");
			} else if (c == '&') {
				writer.print("&amp;");
			} else if (c == '\'') {
				writer.print("&apos;");
			} else if (c == '"') {
				writer.print("&quot;");

				// not necessary with UTF-8?
				// } else if (c < 32 || c > 127) {
				// writer.print("&#");
				// writer.print((int) c);
				// writer.print(';');

			} else {
				writer.print(c);
			}
		}
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	public void addColumn() {
		addColumn(null, STRING);
	}

	public void addColumn(final String title) {
		addColumn(title, STRING);
	}

	public void addColumn(final String title, final int type) {
		insertColumn(columns.length, title, type);
	}

	public void insertColumn(final int index) {
		insertColumn(index, null, STRING);
	}

	public void insertColumn(final int index, final String title) {
		insertColumn(index, title, STRING);
	}

	public void insertColumn(final int index, final String title, final int type) {
		if ((title != null) && (columnTitles == null)) {
			columnTitles = new String[columns.length];
		}
		if (columnTitles != null) {
			columnTitles = PApplet.splice(columnTitles, title, index);
			columnIndices = null;
		}
		columnTypes = PApplet.splice(columnTypes, type, index);

		// columnCategories = (HashMapBlows[])
		// PApplet.splice(columnCategories, new HashMapBlows(), index);
		final HashMapBlows[] catTemp = new HashMapBlows[columns.length + 1];
		// Faster than arrayCopy for a dozen or so entries
		for (int i = 0; i < index; i++) {
			catTemp[i] = columnCategories[i];
		}
		catTemp[index] = new HashMapBlows();
		for (int i = index; i < columns.length; i++) {
			catTemp[i + 1] = columnCategories[i];
		}
		columnCategories = catTemp;

		final Object[] temp = new Object[columns.length + 1];
		System.arraycopy(columns, 0, temp, 0, index);
		System.arraycopy(columns, index, temp, index + 1, columns.length - index);
		columns = temp;

		switch (type) {
		case INT:
			columns[index] = new int[rowCount];
			break;
		case LONG:
			columns[index] = new long[rowCount];
			break;
		case FLOAT:
			columns[index] = new float[rowCount];
			break;
		case DOUBLE:
			columns[index] = new double[rowCount];
			break;
		case STRING:
			columns[index] = new String[rowCount];
			break;
		case CATEGORICAL:
			columns[index] = new int[rowCount];
			break;
		}
	}

	public void removeColumn(final String columnName) {
		removeColumn(getColumnIndex(columnName));
	}

	public void removeColumn(final int column) {
		final Object[] temp = new Object[columns.length + 1];
		System.arraycopy(columns, 0, temp, 0, column);
		System.arraycopy(columns, column + 1, temp, column, (columns.length - column) + 1);
		columns = temp;
	}

	public int getColumnCount() {
		return columns.length;
	}

	/**
	 * Change the number of columns in this table. Resizes all rows to ensure the same number of
	 * columns in each row. Entries in the additional (empty) columns will be set to null.
	 * 
	 * @param newCount
	 */
	public void setColumnCount(final int newCount) {
		final int oldCount = columns.length;
		if (oldCount != newCount) {
			columns = (Object[]) PApplet.expand(columns, newCount);
			// create new columns, default to String as the data type
			for (int c = oldCount; c < newCount; c++) {
				columns[c] = new String[rowCount];
			}

			if (columnTitles != null) {
				columnTitles = PApplet.expand(columnTitles, newCount);
			}
			columnTypes = PApplet.expand(columnTypes, newCount);
			columnCategories = (HashMapBlows[]) PApplet.expand(columnCategories, newCount);
		}
	}

	public void setColumnType(final String columnName, final String columnType) {
		setColumnType(checkColumnIndex(columnName), columnType);
	}

	/**
	 * Set the data type for a column so that using it is more efficient.
	 * 
	 * @param column
	 *            the column to change
	 * @param columnType
	 *            One of int, long, float, double, or String.
	 */
	public void setColumnType(final int column, final String columnType) {
		int type = -1;
		if (columnType.equals("String")) {
			type = STRING;
		} else if (columnType.equals("int")) {
			type = INT;
		} else if (columnType.equals("long")) {
			type = LONG;
		} else if (columnType.equals("float")) {
			type = FLOAT;
		} else if (columnType.equals("double")) {
			type = DOUBLE;
		} else if (columnType.equals("categorical")) {
			type = CATEGORICAL;
		} else {
			throw new IllegalArgumentException("'" + columnType + "' is not a valid column type.");
		}
		setColumnType(column, type);
	}

	protected void setColumnType(final String columnName, final int newType) {
		setColumnType(checkColumnIndex(columnName), newType);
	}

	/**
	 * Sets the column type. If data already exists, then it'll be converted to the new type.
	 * 
	 * @param column
	 *            the column whose type should be changed
	 * @param newType
	 *            something fresh, maybe try an int or a float for size?
	 */
	protected void setColumnType(final int column, final int newType) {
		switch (newType) {
		case INT: {
			final int[] intData = new int[rowCount];
			for (int row = 0; row < rowCount; row++) {
				final String s = getString(row, column);
				intData[row] = PApplet.parseInt(s, missingInt);
			}
			columns[column] = intData;
			break;
		}
		case LONG: {
			final long[] longData = new long[rowCount];
			for (int row = 0; row < rowCount; row++) {
				final String s = getString(row, column);
				try {
					longData[row] = Long.parseLong(s);
				} catch (final NumberFormatException nfe) {
					longData[row] = missingLong;
				}
			}
			columns[column] = longData;
			break;
		}
		case FLOAT: {
			final float[] floatData = new float[rowCount];
			for (int row = 0; row < rowCount; row++) {
				final String s = getString(row, column);
				floatData[row] = PApplet.parseFloat(s, missingFloat);
			}
			columns[column] = floatData;
			break;
		}
		case DOUBLE: {
			final double[] doubleData = new double[rowCount];
			for (int row = 0; row < rowCount; row++) {
				final String s = getString(row, column);
				try {
					doubleData[row] = Double.parseDouble(s);
				} catch (final NumberFormatException nfe) {
					doubleData[row] = missingDouble;
				}
			}
			columns[column] = doubleData;
			break;
		}
		case STRING: {
			if (columnTypes[column] != STRING) {
				final String[] stringData = new String[rowCount];
				for (int row = 0; row < rowCount; row++) {
					stringData[row] = getString(row, column);
				}
				columns[column] = stringData;
			}
			break;
		}
		case CATEGORICAL: {
			final int[] indexData = new int[rowCount];
			final HashMapBlows categories = new HashMapBlows();
			for (int row = 0; row < rowCount; row++) {
				final String s = getString(row, column);
				indexData[row] = categories.index(s);
			}
			columnCategories[column] = categories;
			columns[column] = indexData;
			break;
		}
		default: {
			throw new IllegalArgumentException("That's not a valid column type.");
		}
		}
		// System.out.println("new type is " + newType);
		columnTypes[column] = newType;
	}

	/**
	 * Set the entire table to a specific data type.
	 */
	public void setTableType(final String type) {
		for (int col = 0; col < getColumnCount(); col++) {
			setColumnType(col, type);
		}
	}

	/**
	 * Set the titles (and if a second column is present) the data types for this table based on a
	 * file loaded separately. This will look for the title in column 0, and the type in column 1.
	 * Better yet, specify a column named "title" and another named "type" in the dictionary table
	 * to future-proof the code.
	 * 
	 * @param dictionary
	 */
	public void setColumnTypes(final Table dictionary) {
		int titleCol = 0;
		int typeCol = 1;
		if (dictionary.hasColumnTitles()) {
			titleCol = dictionary.getColumnIndex("title", true);
			typeCol = dictionary.getColumnIndex("type", true);
		}
		setColumnTitles(dictionary.getStringColumn(titleCol));
		if (dictionary.getColumnCount() > 1) {
			for (int i = 0; i < dictionary.getRowCount(); i++) {
				setColumnType(i, dictionary.getString(i, typeCol));
			}
		}
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	/**
	 * Remove the first row from the data set, and use it as the column titles. Use
	 * loadTable("table.csv", "header") instead.
	 */
	@Deprecated
	public String[] removeTitleRow() {
		final String[] titles = getStringRow(0);
		removeRow(0);
		setColumnTitles(titles);
		return titles;
	}

	public void setColumnTitles(final String[] titles) {
		if (titles != null) {
			checkColumn(titles.length - 1);
		}
		columnTitles = titles;
		columnIndices = null; // remove the cache
	}

	public void setColumnTitle(final int column, final String title) {
		checkColumn(column);
		if (columnTitles == null) {
			columnTitles = new String[getColumnCount()];
		}
		columnTitles[column] = title;
		columnIndices = null; // reset these fellas
	}

	public boolean hasColumnTitles() {
		return columnTitles != null;
	}

	public String[] getColumnTitles() {
		return columnTitles;
	}

	public String getColumnTitle(final int col) {
		return (columnTitles == null) ? null : columnTitles[col];
	}

	public int getColumnIndex(final String columnName) {
		return getColumnIndex(columnName, true);
	}

	/**
	 * Get the index of a column.
	 * 
	 * @param name
	 *            Name of the column.
	 * @param report
	 *            Whether to throw an exception if the column wasn't found.
	 * @return index of the found column, or -1 if not found.
	 */
	protected int getColumnIndex(final String name, final boolean report) {
		if (columnTitles == null) {
			if (report) { throw new IllegalArgumentException("This table has no header, so no column titles are set."); }
			return -1;
		}
		// only create this on first get(). subsequent calls to set the title will
		// also update this array, but only if it exists.
		if (columnIndices == null) {
			columnIndices = new HashMap<String, Integer>();
			for (int col = 0; col < columns.length; col++) {
				columnIndices.put(columnTitles[col], col);
			}
		}
		final Integer index = columnIndices.get(name);
		if (index == null) {
			if (report) {
				// Throws an exception here because the name is known and therefore most useful.
				// (Rather than waiting for it to fail inside, say, getInt())
				throw new IllegalArgumentException("This table has no column named '" + name + "'");
			}
			return -1;
		}
		return index.intValue();
	}

	/**
	 * Same as getColumnIndex(), but creates the column if it doesn't exist. Named this way to not
	 * conflict with checkColumn(), an internal function used to ensure that a columns exists, and
	 * also to denote that it returns an int for the column index.
	 * 
	 * @param title
	 *            column title
	 * @return index of a new or previously existing column
	 */
	public int checkColumnIndex(final String title) {
		final int index = getColumnIndex(title, false);
		if (index != -1) { return index; }
		addColumn(title);
		return getColumnCount() - 1;
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	public int getRowCount() {
		return rowCount;
	}

	public int lastRowIndex() {
		return getRowCount() - 1;
	}

	public void setRowCount(final int newCount) {
		if (newCount != rowCount) {
			if (newCount > 1000000) {
				System.out.println("setting row count to " + PApplet.nfc(newCount));
			}
			final long t = System.currentTimeMillis();
			for (int col = 0; col < columns.length; col++) {
				switch (columnTypes[col]) {
				case INT:
					columns[col] = PApplet.expand((int[]) columns[col], newCount);
					break;
				case LONG:
					columns[col] = PApplet.expand((long[]) columns[col], newCount);
					break;
				case FLOAT:
					columns[col] = PApplet.expand(columns[col], newCount);
					break;
				case DOUBLE:
					columns[col] = PApplet.expand((double[]) columns[col], newCount);
					break;
				case STRING:
					columns[col] = PApplet.expand((String[]) columns[col], newCount);
					break;
				case CATEGORICAL:
					columns[col] = PApplet.expand((int[]) columns[col], newCount);
					break;
				}
				if (newCount > 1000000) {
					try {
						Thread.sleep(10); // gc time!
					} catch (final InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			if (newCount > 1000000) {
				final int ms = (int) (System.currentTimeMillis() - t);
				System.out.println("  resize took " + PApplet.nfc(ms) + " ms");
			}
		}
		rowCount = newCount;
	}

	public TableRow addRow() {
		setRowCount(rowCount + 1);
		return new RowPointer(this, rowCount - 1);
	}

	public TableRow addRow(final Object[] columnData) {
		setRow(getRowCount(), columnData);
		return new RowPointer(this, rowCount - 1);
	}

	public void insertRow(final int insert, final Object[] columnData) {
		for (int col = 0; col < columns.length; col++) {
			switch (columnTypes[col]) {
			case CATEGORICAL:
			case INT: {
				final int[] intTemp = new int[rowCount + 1];
				System.arraycopy(columns[col], 0, intTemp, 0, insert);
				System.arraycopy(columns[col], insert, intTemp, insert + 1, (rowCount - insert) + 1);
				columns[col] = intTemp;
				break;
			}
			case LONG: {
				final long[] longTemp = new long[rowCount + 1];
				System.arraycopy(columns[col], 0, longTemp, 0, insert);
				System.arraycopy(columns[col], insert, longTemp, insert + 1, (rowCount - insert) + 1);
				columns[col] = longTemp;
				break;
			}
			case FLOAT: {
				final float[] floatTemp = new float[rowCount + 1];
				System.arraycopy(columns[col], 0, floatTemp, 0, insert);
				System.arraycopy(columns[col], insert, floatTemp, insert + 1, (rowCount - insert) + 1);
				columns[col] = floatTemp;
				break;
			}
			case DOUBLE: {
				final double[] doubleTemp = new double[rowCount + 1];
				System.arraycopy(columns[col], 0, doubleTemp, 0, insert);
				System.arraycopy(columns[col], insert, doubleTemp, insert + 1, (rowCount - insert) + 1);
				columns[col] = doubleTemp;
				break;
			}
			case STRING: {
				final String[] stringTemp = new String[rowCount + 1];
				System.arraycopy(columns[col], 0, stringTemp, 0, insert);
				System.arraycopy(columns[col], insert, stringTemp, insert + 1, (rowCount - insert) + 1);
				columns[col] = stringTemp;
				break;
			}
			}
		}
		setRow(insert, columnData);
		rowCount++;
	}

	public void removeRow(final int row) {
		for (int col = 0; col < columns.length; col++) {
			switch (columnTypes[col]) {
			case CATEGORICAL:
			case INT: {
				final int[] intTemp = new int[rowCount - 1];
				// int[] intData = (int[]) columns[col];
				// System.arraycopy(intData, 0, intTemp, 0, dead);
				// System.arraycopy(intData, dead+1, intTemp, dead, (rowCount - dead) + 1);
				System.arraycopy(columns[col], 0, intTemp, 0, row);
				System.arraycopy(columns[col], row + 1, intTemp, row, (rowCount - row) - 1);
				columns[col] = intTemp;
				break;
			}
			case LONG: {
				final long[] longTemp = new long[rowCount - 1];
				// long[] longData = (long[]) columns[col];
				// System.arraycopy(longData, 0, longTemp, 0, dead);
				// System.arraycopy(longData, dead+1, longTemp, dead, (rowCount - dead) + 1);
				System.arraycopy(columns[col], 0, longTemp, 0, row);
				System.arraycopy(columns[col], row + 1, longTemp, row, (rowCount - row) - 1);
				columns[col] = longTemp;
				break;
			}
			case FLOAT: {
				final float[] floatTemp = new float[rowCount - 1];
				// float[] floatData = (float[]) columns[col];
				// System.arraycopy(floatData, 0, floatTemp, 0, dead);
				// System.arraycopy(floatData, dead+1, floatTemp, dead, (rowCount - dead) + 1);
				System.arraycopy(columns[col], 0, floatTemp, 0, row);
				System.arraycopy(columns[col], row + 1, floatTemp, row, (rowCount - row) - 1);
				columns[col] = floatTemp;
				break;
			}
			case DOUBLE: {
				final double[] doubleTemp = new double[rowCount - 1];
				// double[] doubleData = (double[]) columns[col];
				// System.arraycopy(doubleData, 0, doubleTemp, 0, dead);
				// System.arraycopy(doubleData, dead+1, doubleTemp, dead, (rowCount - dead) + 1);
				System.arraycopy(columns[col], 0, doubleTemp, 0, row);
				System.arraycopy(columns[col], row + 1, doubleTemp, row, (rowCount - row) - 1);
				columns[col] = doubleTemp;
				break;
			}
			case STRING: {
				final String[] stringTemp = new String[rowCount - 1];
				System.arraycopy(columns[col], 0, stringTemp, 0, row);
				System.arraycopy(columns[col], row + 1, stringTemp, row, (rowCount - row) - 1);
				columns[col] = stringTemp;
			}
			}
		}
		rowCount--;
	}

	/*
	 * public void setRow(int row, String[] pieces) { checkSize(row, pieces.length - 1); //
	 * pieces.length may be less than columns.length, so loop over pieces for (int col = 0; col <
	 * pieces.length; col++) { setRowCol(row, col, pieces[col]); } } protected void setRowCol(int
	 * row, int col, String piece) { switch (columnTypes[col]) { case STRING: String[] stringData =
	 * (String[]) columns[col]; stringData[row] = piece; break; case INT: int[] intData = (int[])
	 * columns[col]; intData[row] = PApplet.parseInt(piece, missingInt); break; case LONG: long[]
	 * longData = (long[]) columns[col]; try { longData[row] = Long.parseLong(piece); } catch
	 * (NumberFormatException nfe) { longData[row] = missingLong; } break; case FLOAT: float[]
	 * floatData = (float[]) columns[col]; floatData[row] = PApplet.parseFloat(piece, missingFloat);
	 * break; case DOUBLE: double[] doubleData = (double[]) columns[col]; try { doubleData[row] =
	 * Double.parseDouble(piece); } catch (NumberFormatException nfe) { doubleData[row] =
	 * missingDouble; } break; case CATEGORICAL: int[] indexData = (int[]) columns[col];
	 * indexData[row] = columnCategories[col].index(piece); break; default: throw new
	 * IllegalArgumentException("That's not a valid column type."); } }
	 */

	public void setRow(final int row, final Object[] pieces) {
		checkSize(row, pieces.length - 1);
		// pieces.length may be less than columns.length, so loop over pieces
		for (int col = 0; col < pieces.length; col++) {
			setRowCol(row, col, pieces[col]);
		}
	}

	protected void setRowCol(final int row, final int col, final Object piece) {
		switch (columnTypes[col]) {
		case STRING:
			final String[] stringData = (String[]) columns[col];
			if (piece == null) {
				stringData[row] = null;
				// } else if (piece instanceof String) {
				// stringData[row] = (String) piece;
			} else {
				// Calls toString() on the object, which is 'return this' for String
				stringData[row] = String.valueOf(piece);
			}
			break;
		case INT:
			final int[] intData = (int[]) columns[col];
			// intData[row] = PApplet.parseInt(piece, missingInt);
			if (piece == null) {
				intData[row] = missingInt;
			} else if (piece instanceof Integer) {
				intData[row] = (Integer) piece;
			} else {
				intData[row] = PApplet.parseInt(String.valueOf(piece), missingInt);
			}
			break;
		case LONG:
			final long[] longData = (long[]) columns[col];
			if (piece == null) {
				longData[row] = missingLong;
			} else if (piece instanceof Long) {
				longData[row] = (Long) piece;
			} else {
				try {
					longData[row] = Long.parseLong(String.valueOf(piece));
				} catch (final NumberFormatException nfe) {
					longData[row] = missingLong;
				}
			}
			break;
		case FLOAT:
			final float[] floatData = (float[]) columns[col];
			if (piece == null) {
				floatData[row] = missingFloat;
			} else if (piece instanceof Float) {
				floatData[row] = (Float) piece;
			} else {
				floatData[row] = PApplet.parseFloat(String.valueOf(piece), missingFloat);
			}
			break;
		case DOUBLE:
			final double[] doubleData = (double[]) columns[col];
			if (piece == null) {
				doubleData[row] = missingDouble;
			} else if (piece instanceof Double) {
				doubleData[row] = (Double) piece;
			} else {
				try {
					doubleData[row] = Double.parseDouble(String.valueOf(piece));
				} catch (final NumberFormatException nfe) {
					doubleData[row] = missingDouble;
				}
			}
			break;
		case CATEGORICAL:
			final int[] indexData = (int[]) columns[col];
			if (piece == null) {
				indexData[row] = missingCategory;
			} else {
				indexData[row] = columnCategories[col].index(String.valueOf(piece));
			}
			break;
		default:
			throw new IllegalArgumentException("That's not a valid column type.");
		}
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	public TableRow getRow(final int row) {
		return new RowPointer(this, row);
	}

	/**
	 * Note that this one iterator instance is shared by any calls to iterate the rows of this
	 * table. This is very efficient, but not thread-safe. If you want to iterate in a
	 * multi-threaded manner, don't use the iterator.
	 */
	public Iterable<TableRow> rows() {
		return new Iterable<TableRow>() {
			public Iterator<TableRow> iterator() {
				if (rowIterator == null) {
					rowIterator = new RowIterator(Table.this);
				} else {
					rowIterator.reset();
				}
				return rowIterator;
			}
		};
	}

	public Iterator<TableRow> rows(final int[] indices) {
		return new RowIndexIterator(this, indices);
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	static class RowPointer implements TableRow {
		Table	table;
		int		row;

		public RowPointer(final Table table, final int row) {
			this.table = table;
			this.row = row;
		}

		public void setRow(final int row) {
			this.row = row;
		}

		public String getString(final int column) {
			return table.getString(row, column);
		}

		public String getString(final String columnName) {
			return table.getString(row, columnName);
		}

		public int getInt(final int column) {
			return table.getInt(row, column);
		}

		public int getInt(final String columnName) {
			return table.getInt(row, columnName);
		}

		public long getLong(final int column) {
			return table.getLong(row, column);
		}

		public long getLong(final String columnName) {
			return table.getLong(row, columnName);
		}

		public float getFloat(final int column) {
			return table.getFloat(row, column);
		}

		public float getFloat(final String columnName) {
			return table.getFloat(row, columnName);
		}

		public double getDouble(final int column) {
			return table.getDouble(row, column);
		}

		public double getDouble(final String columnName) {
			return table.getDouble(row, columnName);
		}

		public void setString(final int column, final String value) {
			table.setString(row, column, value);
		}

		public void setString(final String columnName, final String value) {
			table.setString(row, columnName, value);
		}

		public void setInt(final int column, final int value) {
			table.setInt(row, column, value);
		}

		public void setInt(final String columnName, final int value) {
			table.setInt(row, columnName, value);
		}

		public void setLong(final int column, final long value) {
			table.setLong(row, column, value);
		}

		public void setLong(final String columnName, final long value) {
			table.setLong(row, columnName, value);
		}

		public void setFloat(final int column, final float value) {
			table.setFloat(row, column, value);
		}

		public void setFloat(final String columnName, final float value) {
			table.setFloat(row, columnName, value);
		}

		public void setDouble(final int column, final double value) {
			table.setDouble(row, column, value);
		}

		public void setDouble(final String columnName, final double value) {
			table.setDouble(row, columnName, value);
		}
	}

	static class RowIterator implements Iterator<TableRow> {
		Table		table;
		RowPointer	rp;
		int		   row;

		public RowIterator(final Table table) {
			this.table = table;
			row = -1;
			rp = new RowPointer(table, row);
		}

		public void remove() {
			table.removeRow(row);
		}

		public TableRow next() {
			rp.setRow(++row);
			return rp;
		}

		public boolean hasNext() {
			return (row + 1) < table.getRowCount();
		}

		public void reset() {
			row = -1;
		}
	}

	static class RowIndexIterator implements Iterator<TableRow> {
		Table		table;
		RowPointer	rp;
		int[]		indices;
		int		   index;

		public RowIndexIterator(final Table table, final int[] indices) {
			this.table = table;
			this.indices = indices;
			index = -1;
			// just set to something arbitrary
			rp = new RowPointer(table, -1);
		}

		public void remove() {
			table.removeRow(indices[index]);
		}

		public TableRow next() {
			rp.setRow(indices[++index]);
			return rp;
		}

		public boolean hasNext() {
			// return row+1 < table.getRowCount();
			return (index + 1) < indices.length;
		}

		public void reset() {
			index = -1;
		}
	}

	static public Iterator<TableRow> createIterator(final ResultSet rs) {
		return new Iterator<TableRow>() {
			boolean	already;

			public boolean hasNext() {
				already = true;
				try {
					return rs.next();
				} catch (final SQLException e) {
					throw new RuntimeException(e);
				}
			}

			public TableRow next() {
				if (!already) {
					try {
						rs.next();
					} catch (final SQLException e) {
						throw new RuntimeException(e);
					}
				} else {
					already = false;
				}

				return new TableRow() {
					public double getDouble(final int column) {
						try {
							return rs.getDouble(column);
						} catch (final SQLException e) {
							throw new RuntimeException(e);
						}
					}

					public double getDouble(final String columnName) {
						try {
							return rs.getDouble(columnName);
						} catch (final SQLException e) {
							throw new RuntimeException(e);
						}
					}

					public float getFloat(final int column) {
						try {
							return rs.getFloat(column);
						} catch (final SQLException e) {
							throw new RuntimeException(e);
						}
					}

					public float getFloat(final String columnName) {
						try {
							return rs.getFloat(columnName);
						} catch (final SQLException e) {
							throw new RuntimeException(e);
						}
					}

					public int getInt(final int column) {
						try {
							return rs.getInt(column);
						} catch (final SQLException e) {
							throw new RuntimeException(e);
						}
					}

					public int getInt(final String columnName) {
						try {
							return rs.getInt(columnName);
						} catch (final SQLException e) {
							throw new RuntimeException(e);
						}
					}

					public long getLong(final int column) {
						try {
							return rs.getLong(column);
						} catch (final SQLException e) {
							throw new RuntimeException(e);
						}
					}

					public long getLong(final String columnName) {
						try {
							return rs.getLong(columnName);
						} catch (final SQLException e) {
							throw new RuntimeException(e);
						}
					}

					public String getString(final int column) {
						try {
							return rs.getString(column);
						} catch (final SQLException e) {
							throw new RuntimeException(e);
						}
					}

					public String getString(final String columnName) {
						try {
							return rs.getString(columnName);
						} catch (final SQLException e) {
							throw new RuntimeException(e);
						}
					}

					public void setString(final int column, final String value) {
						immutable();
					}

					public void setString(final String columnName, final String value) {
						immutable();
					}

					public void setInt(final int column, final int value) {
						immutable();
					}

					public void setInt(final String columnName, final int value) {
						immutable();
					}

					public void setLong(final int column, final long value) {
						immutable();
					}

					public void setLong(final String columnName, final long value) {
						immutable();
					}

					public void setFloat(final int column, final float value) {
						immutable();
					}

					public void setFloat(final String columnName, final float value) {
						immutable();
					}

					public void setDouble(final int column, final double value) {
						immutable();
					}

					public void setDouble(final String columnName, final double value) {
						immutable();
					}

					private void immutable() {
						throw new IllegalArgumentException("This TableRow cannot be modified.");
					}

				};
			}

			public void remove() {
				throw new IllegalArgumentException("remove() not supported");
			}
		};
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	public int getInt(final int row, final int column) {
		checkBounds(row, column);
		if ((columnTypes[column] == INT) || (columnTypes[column] == CATEGORICAL)) {
			final int[] intData = (int[]) columns[column];
			return intData[row];
		}
		final String str = getString(row, column);
		return ((str == null) || str.equals(missingString)) ? missingInt : PApplet.parseInt(str, missingInt);
	}

	public int getInt(final int row, final String columnName) {
		return getInt(row, getColumnIndex(columnName));
	}

	public void setMissingInt(final int value) {
		missingInt = value;
	}

	public void setInt(final int row, final int column, final int value) {
		if (columnTypes[column] == STRING) {
			setString(row, column, String.valueOf(value));

		} else {
			checkSize(row, column);
			if (columnTypes[column] != INT) { throw new IllegalArgumentException("Column " + column
			        + " is not an int column."); }
			final int[] intData = (int[]) columns[column];
			intData[row] = value;
		}
	}

	public void setInt(final int row, final String columnName, final int value) {
		setInt(row, getColumnIndex(columnName), value);
	}

	public int[] getIntColumn(final String name) {
		final int col = getColumnIndex(name);
		return (col == -1) ? null : getIntColumn(col);
	}

	public int[] getIntColumn(final int col) {
		final int[] outgoing = new int[rowCount];
		for (int row = 0; row < rowCount; row++) {
			outgoing[row] = getInt(row, col);
		}
		return outgoing;
	}

	public int[] getIntRow(final int row) {
		final int[] outgoing = new int[columns.length];
		for (int col = 0; col < columns.length; col++) {
			outgoing[col] = getInt(row, col);
		}
		return outgoing;
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	public long getLong(final int row, final int column) {
		checkBounds(row, column);
		if (columnTypes[column] == LONG) {
			final long[] longData = (long[]) columns[column];
			return longData[row];
		}
		final String str = getString(row, column);
		if ((str == null) || str.equals(missingString)) { return missingLong; }
		try {
			return Long.parseLong(str);
		} catch (final NumberFormatException nfe) {
			return missingLong;
		}
	}

	public long getLong(final int row, final String columnName) {
		return getLong(row, getColumnIndex(columnName));
	}

	public void setMissingLong(final long value) {
		missingLong = value;
	}

	public void setLong(final int row, final int column, final long value) {
		if (columnTypes[column] == STRING) {
			setString(row, column, String.valueOf(value));

		} else {
			checkSize(row, column);
			if (columnTypes[column] != LONG) { throw new IllegalArgumentException("Column " + column
			        + " is not a 'long' column."); }
			final long[] longData = (long[]) columns[column];
			longData[row] = value;
		}
	}

	public void setLong(final int row, final String columnName, final long value) {
		setLong(row, getColumnIndex(columnName), value);
	}

	public long[] getLongColumn(final String name) {
		final int col = getColumnIndex(name);
		return (col == -1) ? null : getLongColumn(col);
	}

	public long[] getLongColumn(final int col) {
		final long[] outgoing = new long[rowCount];
		for (int row = 0; row < rowCount; row++) {
			outgoing[row] = getLong(row, col);
		}
		return outgoing;
	}

	public long[] getLongRow(final int row) {
		final long[] outgoing = new long[columns.length];
		for (int col = 0; col < columns.length; col++) {
			outgoing[col] = getLong(row, col);
		}
		return outgoing;
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	/**
	 * Get a float value from the specified row and column. If the value is null or not parseable as
	 * a float, the "missing" value is returned. By default, this is Float.NaN, but can be
	 * controlled with setMissingFloat().
	 */
	public float getFloat(final int row, final int column) {
		checkBounds(row, column);
		if (columnTypes[column] == FLOAT) {
			final float[] floatData = (float[]) columns[column];
			return floatData[row];
		}
		final String str = getString(row, column);
		if ((str == null) || str.equals(missingString)) { return missingFloat; }
		return PApplet.parseFloat(str, missingFloat);
	}

	public float getFloat(final int row, final String columnName) {
		return getFloat(row, getColumnIndex(columnName));
	}

	public void setMissingFloat(final float value) {
		missingFloat = value;
	}

	public void setFloat(final int row, final int column, final float value) {
		if (columnTypes[column] == STRING) {
			setString(row, column, String.valueOf(value));

		} else {
			checkSize(row, column);
			if (columnTypes[column] != FLOAT) { throw new IllegalArgumentException("Column " + column
			        + " is not a float column."); }
			final float[] longData = (float[]) columns[column];
			longData[row] = value;
		}
	}

	public void setFloat(final int row, final String columnName, final float value) {
		setFloat(row, getColumnIndex(columnName), value);
	}

	public float[] getFloatColumn(final String name) {
		final int col = getColumnIndex(name);
		return (col == -1) ? null : getFloatColumn(col);
	}

	public float[] getFloatColumn(final int col) {
		final float[] outgoing = new float[rowCount];
		for (int row = 0; row < rowCount; row++) {
			outgoing[row] = getFloat(row, col);
		}
		return outgoing;
	}

	public float[] getFloatRow(final int row) {
		final float[] outgoing = new float[columns.length];
		for (int col = 0; col < columns.length; col++) {
			outgoing[col] = getFloat(row, col);
		}
		return outgoing;
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	public double getDouble(final int row, final int column) {
		checkBounds(row, column);
		if (columnTypes[column] == DOUBLE) {
			final double[] doubleData = (double[]) columns[column];
			return doubleData[row];
		}
		final String str = getString(row, column);
		if ((str == null) || str.equals(missingString)) { return missingDouble; }
		try {
			return Double.parseDouble(str);
		} catch (final NumberFormatException nfe) {
			return missingDouble;
		}
	}

	public double getDouble(final int row, final String columnName) {
		return getDouble(row, getColumnIndex(columnName));
	}

	public void setMissingDouble(final double value) {
		missingDouble = value;
	}

	public void setDouble(final int row, final int column, final double value) {
		if (columnTypes[column] == STRING) {
			setString(row, column, String.valueOf(value));

		} else {
			checkSize(row, column);
			if (columnTypes[column] != DOUBLE) { throw new IllegalArgumentException("Column " + column
			        + " is not a 'double' column."); }
			final double[] doubleData = (double[]) columns[column];
			doubleData[row] = value;
		}
	}

	public void setDouble(final int row, final String columnName, final double value) {
		setDouble(row, getColumnIndex(columnName), value);
	}

	public double[] getDoubleColumn(final String name) {
		final int col = getColumnIndex(name);
		return (col == -1) ? null : getDoubleColumn(col);
	}

	public double[] getDoubleColumn(final int col) {
		final double[] outgoing = new double[rowCount];
		for (int row = 0; row < rowCount; row++) {
			outgoing[row] = getDouble(row, col);
		}
		return outgoing;
	}

	public double[] getDoubleRow(final int row) {
		final double[] outgoing = new double[columns.length];
		for (int col = 0; col < columns.length; col++) {
			outgoing[col] = getDouble(row, col);
		}
		return outgoing;
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	// public long getTimestamp(String rowName, int column) {
	// return getTimestamp(getRowIndex(rowName), column);
	// }

	/**
	 * Returns the time in milliseconds by parsing a SQL Timestamp at this cell.
	 */
	// public long getTimestamp(int row, int column) {
	// String str = get(row, column);
	// java.sql.Timestamp timestamp = java.sql.Timestamp.valueOf(str);
	// return timestamp.getTime();
	// }

	// public long getExcelTimestamp(int row, int column) {
	// return parseExcelTimestamp(get(row, column));
	// }

	// static protected DateFormat excelDateFormat;

	// static public long parseExcelTimestamp(String timestamp) {
	// if (excelDateFormat == null) {
	// excelDateFormat = new SimpleDateFormat("MM/dd/yy HH:mm");
	// }
	// try {
	// return excelDateFormat.parse(timestamp).getTime();
	// } catch (ParseException e) {
	// e.printStackTrace();
	// return -1;
	// }
	// }

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	// public void setObject(int row, int column, Object value) {
	// if (value == null) {
	// data[row][column] = null;
	// } else if (value instanceof String) {
	// set(row, column, (String) value);
	// } else if (value instanceof Float) {
	// setFloat(row, column, ((Float) value).floatValue());
	// } else if (value instanceof Integer) {
	// setInt(row, column, ((Integer) value).intValue());
	// } else {
	// set(row, column, value.toString());
	// }
	// }

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	/**
	 * Get a String value from the table. If the row is longer than the table
	 * 
	 * @param row
	 * @param col
	 * @return the String defined by the row and col variables
	 */
	public String getString(final int row, final int col) {
		checkBounds(row, col);
		if (columnTypes[col] == STRING) {
			final String[] stringData = (String[]) columns[col];
			return stringData[row];
		} else if (columnTypes[col] == CATEGORICAL) {
			final int index = getInt(row, col);
			return columnCategories[col].key(index);
		} else {
			return String.valueOf(Array.get(columns[col], row));
		}
	}

	public String getString(final int row, final String columnName) {
		return getString(row, getColumnIndex(columnName));
	}

	public void setMissingString(final String value) {
		missingString = value;
	}

	public void setString(final int row, final int column, final String value) {
		checkSize(row, column);
		if (columnTypes[column] != STRING) { throw new IllegalArgumentException("Column " + column
		        + " is not a String column."); }
		final String[] stringData = (String[]) columns[column];
		stringData[row] = value;
	}

	public void setString(final int row, final String columnName, final String value) {
		final int column = checkColumnIndex(columnName);
		setString(row, column, value);
	}

	public String[] getStringColumn(final String name) {
		final int col = getColumnIndex(name);
		return (col == -1) ? null : getStringColumn(col);
	}

	public String[] getStringColumn(final int col) {
		final String[] outgoing = new String[rowCount];
		for (int i = 0; i < rowCount; i++) {
			outgoing[i] = getString(i, col);
		}
		return outgoing;
	}

	public String[] getStringRow(final int row) {
		final String[] outgoing = new String[columns.length];
		for (int col = 0; col < columns.length; col++) {
			outgoing[col] = getString(row, col);
		}
		return outgoing;
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	/**
	 * Return the row that contains the first String that matches.
	 * 
	 * @param value
	 *            the String to match
	 * @param column
	 *            the column to search
	 */
	public int findRowIndex(final String value, final int column) {
		checkBounds(-1, column);
		if (columnTypes[column] == STRING) {
			final String[] stringData = (String[]) columns[column];
			if (value == null) {
				for (int row = 0; row < rowCount; row++) {
					if (stringData[row] == null) { return row; }
				}
			} else {
				for (int row = 0; row < rowCount; row++) {
					if ((stringData[row] != null) && stringData[row].equals(value)) { return row; }
				}
			}
		} else { // less efficient, includes conversion as necessary
			for (int row = 0; row < rowCount; row++) {
				final String str = getString(row, column);
				if (str == null) {
					if (value == null) { return row; }
				} else if (str.equals(value)) { return row; }
			}
		}
		return -1;
	}

	/**
	 * Return the row that contains the first String that matches.
	 * 
	 * @param value
	 *            the String to match
	 * @param columnName
	 *            the column to search
	 */
	public int findRowIndex(final String value, final String columnName) {
		return findRowIndex(value, getColumnIndex(columnName));
	}

	/**
	 * Return a list of rows that contain the String passed in. If there are no matches, a zero
	 * length array will be returned (not a null array).
	 * 
	 * @param value
	 *            the String to match
	 * @param column
	 *            the column to search
	 */
	public int[] findRowIndices(final String value, final int column) {
		final int[] outgoing = new int[rowCount];
		int count = 0;

		checkBounds(-1, column);
		if (columnTypes[column] == STRING) {
			final String[] stringData = (String[]) columns[column];
			if (value == null) {
				for (int row = 0; row < rowCount; row++) {
					if (stringData[row] == null) {
						outgoing[count++] = row;
					}
				}
			} else {
				for (int row = 0; row < rowCount; row++) {
					if ((stringData[row] != null) && stringData[row].equals(value)) {
						outgoing[count++] = row;
					}
				}
			}
		} else { // less efficient, includes conversion as necessary
			for (int row = 0; row < rowCount; row++) {
				final String str = getString(row, column);
				if (str == null) {
					if (value == null) {
						outgoing[count++] = row;
					}
				} else if (str.equals(value)) {
					outgoing[count++] = row;
				}
			}
		}
		return PApplet.subset(outgoing, 0, count);
	}

	/**
	 * Return a list of rows that contain the String passed in. If there are no matches, a zero
	 * length array will be returned (not a null array).
	 * 
	 * @param value
	 *            the String to match
	 * @param columnName
	 *            the column to search
	 */
	public int[] findRowIndices(final String value, final String columnName) {
		return findRowIndices(value, getColumnIndex(columnName));
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	public TableRow findRow(final String value, final int column) {
		final int row = findRowIndex(value, column);
		return (row == -1) ? null : new RowPointer(this, row);
	}

	public TableRow findRow(final String value, final String columnName) {
		return findRow(value, getColumnIndex(columnName));
	}

	public Iterator<TableRow> findRows(final String value, final int column) {
		return new RowIndexIterator(this, findRowIndices(value, column));
	}

	public Iterator<TableRow> findRows(final String value, final String columnName) {
		return findRows(value, getColumnIndex(columnName));
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	/**
	 * Return the row that contains the first String that matches.
	 * 
	 * @param regexp
	 *            the String to match
	 * @param column
	 *            the column to search
	 */
	public int matchRowIndex(final String regexp, final int column) {
		checkBounds(-1, column);
		if (columnTypes[column] == STRING) {
			final String[] stringData = (String[]) columns[column];
			for (int row = 0; row < rowCount; row++) {
				if ((stringData[row] != null) && (PApplet.match(stringData[row], regexp) != null)) { return row; }
			}
		} else { // less efficient, includes conversion as necessary
			for (int row = 0; row < rowCount; row++) {
				final String str = getString(row, column);
				if ((str != null) && (PApplet.match(str, regexp) != null)) { return row; }
			}
		}
		return -1;
	}

	/**
	 * Return the row that contains the first String that matches.
	 * 
	 * @param what
	 *            the String to match
	 * @param columnName
	 *            the column to search
	 */
	public int matchRowIndex(final String what, final String columnName) {
		return matchRowIndex(what, getColumnIndex(columnName));
	}

	/**
	 * Return a list of rows that contain the String passed in. If there are no matches, a zero
	 * length array will be returned (not a null array).
	 * 
	 * @param what
	 *            the String to match
	 * @param column
	 *            the column to search
	 */
	public int[] matchRowIndices(final String regexp, final int column) {
		final int[] outgoing = new int[rowCount];
		int count = 0;

		checkBounds(-1, column);
		if (columnTypes[column] == STRING) {
			final String[] stringData = (String[]) columns[column];
			for (int row = 0; row < rowCount; row++) {
				if ((stringData[row] != null) && (PApplet.match(stringData[row], regexp) != null)) {
					outgoing[count++] = row;
				}
			}
		} else { // less efficient, includes conversion as necessary
			for (int row = 0; row < rowCount; row++) {
				final String str = getString(row, column);
				if ((str != null) && (PApplet.match(str, regexp) != null)) {
					outgoing[count++] = row;
				}
			}
		}
		return PApplet.subset(outgoing, 0, count);
	}

	/**
	 * Return a list of rows that match the regex passed in. If there are no matches, a zero length
	 * array will be returned (not a null array).
	 * 
	 * @param what
	 *            the String to match
	 * @param columnName
	 *            the column to search
	 */
	public int[] matchRowIndices(final String what, final String columnName) {
		return matchRowIndices(what, getColumnIndex(columnName));
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	public TableRow matchRow(final String regexp, final int column) {
		final int row = matchRowIndex(regexp, column);
		return (row == -1) ? null : new RowPointer(this, row);
	}

	public TableRow matchRow(final String regexp, final String columnName) {
		return matchRow(regexp, getColumnIndex(columnName));
	}

	public Iterator<TableRow> matchRows(final String value, final int column) {
		return new RowIndexIterator(this, matchRowIndices(value, column));
	}

	public Iterator<TableRow> matchRows(final String value, final String columnName) {
		return matchRows(value, getColumnIndex(columnName));
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	/**
	 * Replace a String with another. Set empty entries null by using replace("", null) or use
	 * replace(null, "") to go the other direction. If this is a typed table, only String columns
	 * will be modified.
	 * 
	 * @param orig
	 * @param replacement
	 */
	public void replace(final String orig, final String replacement) {
		for (int col = 0; col < columns.length; col++) {
			replace(orig, replacement, col);
		}
	}

	public void replace(final String orig, final String replacement, final int col) {
		if (columnTypes[col] == STRING) {
			final String[] stringData = (String[]) columns[col];
			for (int row = 0; row < rowCount; row++) {
				if (stringData[row].equals(orig)) {
					stringData[row] = replacement;
				}
			}
		}
	}

	public void replace(final String orig, final String replacement, final String colName) {
		replace(orig, replacement, getColumnIndex(colName));
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	public void replaceAll(final String orig, final String replacement) {
		for (int col = 0; col < columns.length; col++) {
			replaceAll(orig, replacement, col);
		}
	}

	public void replaceAll(final String regex, final String replacement, final int column) {
		checkBounds(-1, column);
		if (columnTypes[column] == STRING) {
			final String[] stringData = (String[]) columns[column];
			for (int row = 0; row < rowCount; row++) {
				if (stringData[row] != null) {
					stringData[row] = stringData[row].replaceAll(regex, replacement);
				}
			}
		} else {
			throw new IllegalArgumentException("replaceAll() can only be used on String columns");
		}
	}

	/**
	 * Run String.replaceAll() on all entries in a column. Only works with columns that are already
	 * String values.
	 * 
	 * @param what
	 *            the String to match
	 * @param columnName
	 *            the column to search
	 */
	public void replaceAll(final String regex, final String replacement, final String columnName) {
		replaceAll(regex, replacement, getColumnIndex(columnName));
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	/**
	 * Remove any of the specified characters from the entire table.
	 */
	public void removeTokens(final String tokens) {
		for (int col = 0; col < getColumnCount(); col++) {
			removeTokens(tokens, col);
		}
	}

	/**
	 * Removed any of the specified characters from a column. For instance, the following code
	 * removes dollar signs and commas from column 2:
	 * 
	 * <pre>
	 * table.removeTokens(&quot;,$&quot;, 2);
	 * </pre>
	 */
	public void removeTokens(final String tokens, final int column) {
		for (int row = 0; row < rowCount; row++) {
			final String s = getString(row, column);
			if (s != null) {
				final char[] c = s.toCharArray();
				int index = 0;
				for (int j = 0; j < c.length; j++) {
					if (tokens.indexOf(c[j]) == -1) {
						if (index != j) {
							c[index] = c[j];
						}
						index++;
					}
				}
				if (index != c.length) {
					setString(row, column, new String(c, 0, index));
				}
			}
		}
	}

	public void removeTokens(final String tokens, final String columnName) {
		removeTokens(tokens, getColumnIndex(columnName));
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	public void trim() {
		for (int col = 0; col < getColumnCount(); col++) {
			trim(col);
		}
	}

	public void trim(final int column) {
		if (columnTypes[column] == STRING) {
			final String[] stringData = (String[]) columns[column];
			for (int row = 0; row < rowCount; row++) {
				if (stringData[row] != null) {
					stringData[row] = PApplet.trim(stringData[row]);
				}
			}
		}
	}

	public void trim(final String columnName) {
		trim(getColumnIndex(columnName));
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	protected void checkColumn(final int col) {
		if (col >= columns.length) {
			setColumnCount(col + 1);
		}
	}

	protected void checkRow(final int row) {
		if (row >= rowCount) {
			setRowCount(row + 1);
		}
	}

	protected void checkSize(final int row, final int col) {
		checkRow(row);
		checkColumn(col);
	}

	protected void checkBounds(final int row, final int column) {
		if ((row < 0) || (row >= rowCount)) { throw new ArrayIndexOutOfBoundsException("Row " + row
		        + " does not exist."); }
		if ((column < 0) || (column >= columns.length)) { throw new ArrayIndexOutOfBoundsException("Column " + column
		        + " does not exist."); }
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	class HashMapBlows {
		HashMap<String, Integer>	dataToIndex	= new HashMap<String, Integer>();
		ArrayList<String>		 indexToData	= new ArrayList<String>();

		int index(final String key) {
			final Integer value = dataToIndex.get(key);
			if (value != null) { return value; }

			final int v = dataToIndex.size();
			dataToIndex.put(key, v);
			indexToData.add(key);
			return v;
		}

		String key(final int index) {
			return indexToData.get(index);
		}

		int size() {
			return dataToIndex.size();
		}

		void write(final DataOutputStream output) throws IOException {
			output.writeInt(size());
			for (final String str : indexToData) {
				output.writeUTF(str);
			}
		}

		void writeln(final PrintWriter writer) throws IOException {
			for (final String str : indexToData) {
				writer.println(str);
			}
			writer.flush();
			writer.close();
		}

		void read(final DataInputStream input) throws IOException {
			final int count = input.readInt();
			dataToIndex = new HashMap<String, Integer>(count);
			for (int i = 0; i < count; i++) {
				final String str = input.readUTF();
				dataToIndex.put(str, i);
				indexToData.add(str);
			}
		}
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	class HashMapSucks extends HashMap<String, Integer> {

		void increment(final String what) {
			final Integer value = get(what);
			if (value == null) {
				put(what, 1);
			} else {
				put(what, value + 1);
			}
		}

		void check(final String what) {
			if (get(what) == null) {
				put(what, 0);
			}
		}
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	protected String[] getUnique(final String column) {
		return getUnique(getColumnIndex(column));
	}

	protected String[] getUnique(final int column) {
		final HashMapSucks found = new HashMapSucks();
		for (int row = 0; row < getRowCount(); row++) {
			found.check(getString(row, column));
		}
		final String[] outgoing = new String[found.size()];
		found.keySet().toArray(outgoing);
		return outgoing;
	}

	protected HashMap<String, Integer> getUniqueCount(final String columnName) {
		return getUniqueCount(getColumnIndex(columnName));
	}

	protected HashMap<String, Integer> getUniqueCount(final int column) {
		final HashMapSucks outgoing = new HashMapSucks();
		for (int row = 0; row < rowCount; row++) {
			final String entry = getString(row, column);
			if (entry != null) {
				outgoing.increment(entry);
			}
		}
		return outgoing;
	}

	/**
	 * Return an object that maps the String values in one column back to the row from which they
	 * came. For instance, if the "name" of each row is found in the first column,
	 * getColumnRowLookup(0) would return an object that would map each name back to its row.
	 */
	protected HashMap<String, Integer> getRowLookup(final int col) {
		final HashMap<String, Integer> outgoing = new HashMap<String, Integer>();
		for (int row = 0; row < getRowCount(); row++) {
			outgoing.put(getString(row, col), row);
		}
		return outgoing;
	}

	// incomplete, basically this is silly to write all this repetitive code when
	// it can be implemented in ~3 lines of code...
	// /**
	// * Return an object that maps the data from one column to the data of found
	// * in another column.
	// */
	// public HashMap<?,?> getLookup(int col1, int col2) {
	// HashMap outgoing = null;
	//
	// switch (columnTypes[col1]) {
	// case INT: {
	// if (columnTypes[col2] == INT) {
	// outgoing = new HashMap<Integer, Integer>();
	// for (int row = 0; row < getRowCount(); row++) {
	// outgoing.put(getInt(row, col1), getInt(row, col2));
	// }
	// } else if (columnTypes[col2] == LONG) {
	// outgoing = new HashMap<Integer, Long>();
	// for (int row = 0; row < getRowCount(); row++) {
	// outgoing.put(getInt(row, col1), getLong(row, col2));
	// }
	// } else if (columnTypes[col2] == FLOAT) {
	// outgoing = new HashMap<Integer, Float>();
	// for (int row = 0; row < getRowCount(); row++) {
	// outgoing.put(getInt(row, col1), getFloat(row, col2));
	// }
	// } else if (columnTypes[col2] == DOUBLE) {
	// outgoing = new HashMap<Integer, Double>();
	// for (int row = 0; row < getRowCount(); row++) {
	// outgoing.put(getInt(row, col1), getDouble(row, col2));
	// }
	// } else if (columnTypes[col2] == STRING) {
	// outgoing = new HashMap<Integer, String>();
	// for (int row = 0; row < getRowCount(); row++) {
	// outgoing.put(getInt(row, col1), get(row, col2));
	// }
	// }
	// break;
	// }
	// }
	// return outgoing;
	// }

	// public StringIntPairs getColumnRowLookup(int col) {
	// StringIntPairs sc = new StringIntPairs();
	// String[] column = getStringColumn(col);
	// for (int i = 0; i < column.length; i++) {
	// sc.set(column[i], i);
	// }
	// return sc;
	// }

	// public String[] getUniqueEntries(int column) {
	// // HashMap indices = new HashMap();
	// // for (int row = 0; row < rowCount; row++) {
	// // indices.put(data[row][column], this); // 'this' is a dummy
	// // }
	// StringIntPairs sc = getStringCount(column);
	// return sc.keys();
	// }
	//
	//
	// public StringIntPairs getStringCount(String columnName) {
	// return getStringCount(getColumnIndex(columnName));
	// }
	//
	//
	// public StringIntPairs getStringCount(int column) {
	// StringIntPairs outgoing = new StringIntPairs();
	// for (int row = 0; row < rowCount; row++) {
	// String entry = data[row][column];
	// if (entry != null) {
	// outgoing.increment(entry);
	// }
	// }
	// return outgoing;
	// }
	//
	//
	// /**
	// * Return an object that maps the String values in one column back to the
	// * row from which they came. For instance, if the "name" of each row is
	// * found in the first column, getColumnRowLookup(0) would return an object
	// * that would map each name back to its row.
	// */
	// public StringIntPairs getColumnRowLookup(int col) {
	// StringIntPairs sc = new StringIntPairs();
	// String[] column = getStringColumn(col);
	// for (int i = 0; i < column.length; i++) {
	// sc.set(column[i], i);
	// }
	// return sc;
	// }

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	// TODO naming/whether to include
	protected Table createSubset(final int[] rowSubset) {
		final Table newbie = new Table();
		newbie.setColumnTitles(columnTitles); // also sets columns.length
		newbie.columnTypes = columnTypes;
		newbie.setRowCount(rowSubset.length);

		for (int i = 0; i < rowSubset.length; i++) {
			final int row = rowSubset[i];
			for (int col = 0; col < columns.length; col++) {
				switch (columnTypes[col]) {
				case STRING:
					newbie.setString(i, col, getString(row, col));
					break;
				case INT:
					newbie.setInt(i, col, getInt(row, col));
					break;
				case LONG:
					newbie.setLong(i, col, getLong(row, col));
					break;
				case FLOAT:
					newbie.setFloat(i, col, getFloat(row, col));
					break;
				case DOUBLE:
					newbie.setDouble(i, col, getDouble(row, col));
					break;
				}
			}
		}
		return newbie;
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	/**
	 * Searches the entire table for float values. Returns missing float (Float.NaN by default) if
	 * no valid numbers found.
	 */
	protected float getMaxFloat() {
		boolean found = false;
		float max = PConstants.MIN_FLOAT;
		for (int row = 0; row < getRowCount(); row++) {
			for (int col = 0; col < getColumnCount(); col++) {
				final float value = getFloat(row, col);
				if (!Float.isNaN(value)) { // TODO no, this should be comparing to the missing value
					if (!found) {
						max = value;
						found = true;
					} else if (value > max) {
						max = value;
					}
				}
			}
		}
		return found ? max : missingFloat;
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	// converts a TSV or CSV file to binary.. do not use
	protected void convertBasic(final BufferedReader reader, final boolean tsv, final File outputFile)
	        throws IOException {
		final FileOutputStream fos = new FileOutputStream(outputFile);
		final BufferedOutputStream bos = new BufferedOutputStream(fos, 16384);
		final DataOutputStream output = new DataOutputStream(bos);
		output.writeInt(0); // come back for row count
		output.writeInt(getColumnCount());
		if (columnTitles != null) {
			output.writeBoolean(true);
			for (final String title : columnTitles) {
				output.writeUTF(title);
			}
		} else {
			output.writeBoolean(false);
		}
		for (final int type : columnTypes) {
			output.writeInt(type);
		}

		String line = null;
		// setRowCount(1);
		int prev = -1;
		int row = 0;
		while ((line = reader.readLine()) != null) {
			convertRow(output, tsv ? PApplet.split(line, '\t') : splitLineCSV(line));
			row++;

			if ((row % 10000) == 0) {
				if (row < rowCount) {
					final int pct = (100 * row) / rowCount;
					if (pct != prev) {
						System.out.println(pct + "%");
						prev = pct;
					}
				}
				// try {
				// Thread.sleep(5);
				// } catch (InterruptedException e) {
				// e.printStackTrace();
				// }
			}
		}
		// shorten or lengthen based on what's left
		// if (row != getRowCount()) {
		// setRowCount(row);
		// }

		// has to come afterwards, since these tables get built out during the conversion
		int col = 0;
		for (final HashMapBlows hmb : columnCategories) {
			if (hmb == null) {
				output.writeInt(0);
			} else {
				hmb.write(output);
				hmb.writeln(PApplet.createWriter(new File(columnTitles[col] + ".categories")));
				// output.writeInt(hmb.size());
				// for (Map.Entry<String,Integer> e : hmb.entrySet()) {
				// output.writeUTF(e.getKey());
				// output.writeInt(e.getValue());
				// }
			}
			col++;
		}

		output.flush();
		output.close();

		// come back and write the row count
		final RandomAccessFile raf = new RandomAccessFile(outputFile, "rw");
		raf.writeInt(rowCount);
		raf.close();
	}

	protected void convertRow(final DataOutputStream output, final String[] pieces) throws IOException {
		if (pieces.length > getColumnCount()) { throw new IllegalArgumentException("Row with too many columns: "
		        + PApplet.join(pieces, ",")); }
		// pieces.length may be less than columns.length, so loop over pieces
		for (int col = 0; col < pieces.length; col++) {
			switch (columnTypes[col]) {
			case STRING:
				output.writeUTF(pieces[col]);
				break;
			case INT:
				output.writeInt(PApplet.parseInt(pieces[col], missingInt));
				break;
			case LONG:
				try {
					output.writeLong(Long.parseLong(pieces[col]));
				} catch (final NumberFormatException nfe) {
					output.writeLong(missingLong);
				}
				break;
			case FLOAT:
				output.writeFloat(PApplet.parseFloat(pieces[col], missingFloat));
				break;
			case DOUBLE:
				try {
					output.writeDouble(Double.parseDouble(pieces[col]));
				} catch (final NumberFormatException nfe) {
					output.writeDouble(missingDouble);
				}
				break;
			case CATEGORICAL:
				output.writeInt(columnCategories[col].index(pieces[col]));
				break;
			}
		}
		for (int col = pieces.length; col < getColumnCount(); col++) {
			switch (columnTypes[col]) {
			case STRING:
				output.writeUTF("");
				break;
			case INT:
				output.writeInt(missingInt);
				break;
			case LONG:
				output.writeLong(missingLong);
				break;
			case FLOAT:
				output.writeFloat(missingFloat);
				break;
			case DOUBLE:
				output.writeDouble(missingDouble);
				break;
			case CATEGORICAL:
				output.writeInt(missingCategory);
				break;

			}
		}
	}

	/*
	 * private void convertRowCol(DataOutputStream output, int row, int col, String piece) { switch
	 * (columnTypes[col]) { case STRING: String[] stringData = (String[]) columns[col];
	 * stringData[row] = piece; break; case INT: int[] intData = (int[]) columns[col]; intData[row]
	 * = PApplet.parseInt(piece, missingInt); break; case LONG: long[] longData = (long[])
	 * columns[col]; try { longData[row] = Long.parseLong(piece); } catch (NumberFormatException
	 * nfe) { longData[row] = missingLong; } break; case FLOAT: float[] floatData = (float[])
	 * columns[col]; floatData[row] = PApplet.parseFloat(piece, missingFloat); break; case DOUBLE:
	 * double[] doubleData = (double[]) columns[col]; try { doubleData[row] =
	 * Double.parseDouble(piece); } catch (NumberFormatException nfe) { doubleData[row] =
	 * missingDouble; } break; default: throw new
	 * IllegalArgumentException("That's not a valid column type."); } }
	 */
}
