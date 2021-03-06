package processing.core;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * OBJ loading implemented using code from Saito's OBJLoader library:
 * http://code.google.com/p/saitoobjloader/ and OBJReader from Ahmet Kizilay
 * http://www.openprocessing.org/visuals/?visualID=191
 */
public class PShapeOBJ extends PShape {

	/**
	 * Initializes a new OBJ Object with the given filename.
	 */
	public PShapeOBJ(final PApplet parent, final String filename) {
		this(parent, parent.createReader(filename));
	}

	public PShapeOBJ(PApplet parent, final BufferedReader reader) {
		final ArrayList<OBJFace> faces = new ArrayList<OBJFace>();
		final ArrayList<OBJMaterial> materials = new ArrayList<OBJMaterial>();
		final ArrayList<PVector> coords = new ArrayList<PVector>();
		final ArrayList<PVector> normals = new ArrayList<PVector>();
		final ArrayList<PVector> texcoords = new ArrayList<PVector>();
		PShapeOBJ.parseOBJ(parent, reader, faces, materials, coords, normals, texcoords);

		// The OBJ geometry is stored with each face in a separate child shape.
		parent = null;
		family = PConstants.GROUP;
		addChildren(faces, materials, coords, normals, texcoords);
	}

	protected PShapeOBJ(final OBJFace face, final OBJMaterial mtl, final ArrayList<PVector> coords,
	        final ArrayList<PVector> normals, final ArrayList<PVector> texcoords) {
		family = PShape.GEOMETRY;
		if (face.vertIdx.size() == 3) {
			kind = PConstants.TRIANGLES;
		} else if (face.vertIdx.size() == 4) {
			kind = PConstants.QUADS;
		} else {
			kind = PConstants.POLYGON;
		}

		stroke = false;
		fill = true;

		// Setting material properties for the new face
		fillColor = PShapeOBJ.rgbaValue(mtl.kd);
		ambientColor = PShapeOBJ.rgbaValue(mtl.ka);
		specularColor = PShapeOBJ.rgbaValue(mtl.ks);
		shininess = mtl.ns;
		if (mtl.kdMap != null) {
			// If current material is textured, then tinting the texture using
			// the
			// diffuse color.
			tintColor = PShapeOBJ.rgbaValue(mtl.kd, mtl.d);
		}

		vertexCount = face.vertIdx.size();
		vertices = new double[vertexCount][12];
		for (int j = 0; j < face.vertIdx.size(); j++) {
			int vertIdx, normIdx;
			PVector vert, norms;

			vert = norms = null;

			vertIdx = face.vertIdx.get(j).intValue() - 1;
			vert = coords.get(vertIdx);

			if (j < face.normIdx.size()) {
				normIdx = face.normIdx.get(j).intValue() - 1;
				if (-1 < normIdx) {
					norms = normals.get(normIdx);
				}
			}

			vertices[j][PConstants.X] = vert.x;
			vertices[j][PConstants.Y] = vert.y;
			vertices[j][PConstants.Z] = vert.z;

			vertices[j][PGraphics.R] = mtl.kd.x;
			vertices[j][PGraphics.G] = mtl.kd.y;
			vertices[j][PGraphics.B] = mtl.kd.z;
			vertices[j][PGraphics.A] = 1;

			if (norms != null) {
				vertices[j][PGraphics.NX] = norms.x;
				vertices[j][PGraphics.NY] = norms.y;
				vertices[j][PGraphics.NZ] = norms.z;
			}

			if ((mtl != null) && (mtl.kdMap != null)) {
				// This face is textured.
				int texIdx;
				PVector tex = null;

				if (j < face.texIdx.size()) {
					texIdx = face.texIdx.get(j).intValue() - 1;
					if (-1 < texIdx) {
						tex = texcoords.get(texIdx);
					}
				}

				image = mtl.kdMap;
				if (tex != null) {
					vertices[j][PGraphics.U] = tex.x;
					vertices[j][PGraphics.V] = tex.y;
				}
			}
		}
	}

	protected void addChildren(final ArrayList<OBJFace> faces, final ArrayList<OBJMaterial> materials,
	        final ArrayList<PVector> coords, final ArrayList<PVector> normals, final ArrayList<PVector> texcoords) {
		int mtlIdxCur = -1;
		OBJMaterial mtl = null;
		for (int i = 0; i < faces.size(); i++) {
			final OBJFace face = faces.get(i);

			// Getting current material.
			if ((mtlIdxCur != face.matIdx) || (face.matIdx == -1)) {
				// To make sure that at least we get the default material
				mtlIdxCur = PApplet.max(0, face.matIdx);
				mtl = materials.get(mtlIdxCur);
			}

			// Creating child shape for current face.
			final PShape child = new PShapeOBJ(face, mtl, coords, normals, texcoords);
			this.addChild(child);
		}
	}

	static protected void parseOBJ(final PApplet parent, final BufferedReader reader, final ArrayList<OBJFace> faces,
	        final ArrayList<OBJMaterial> materials, final ArrayList<PVector> coords, final ArrayList<PVector> normals,
	        final ArrayList<PVector> texcoords) {
		final Hashtable<String, Integer> mtlTable = new Hashtable<String, Integer>();
		int mtlIdxCur = -1;
		boolean readv, readvn, readvt;
		try {

			readv = readvn = readvt = false;
			String line;
			String gname = "object";
			while ((line = reader.readLine()) != null) {
				// Parse the line.
				line = line.trim();
				if (line.equals("") || (line.indexOf('#') == 0)) {
					// Empty line of comment, ignore line
					continue;
				}

				// The below patch/hack comes from Carlos Tomas Marti and is a
				// fix for single backslashes in Rhino obj files

				// BEGINNING OF RHINO OBJ FILES HACK
				// Statements can be broken in multiple lines using '\' at the
				// end of a line.
				// In regular expressions, the backslash is also an escape
				// character.
				// The regular expression \\ matches a single backslash. This
				// regular expression as a Java string, becomes "\\\\".
				// That's right: 4 backslashes to match a single one.
				while (line.contains("\\")) {
					line = line.split("\\\\")[0];
					final String s = reader.readLine();
					if (s != null) {
						line += s;
					}
				}
				// END OF RHINO OBJ FILES HACK

				final String[] parts = line.split("\\s+");
				// if not a blank line, process the line.
				if (parts.length > 0) {
					if (parts[0].equals("v")) {
						// vertex
						final PVector tempv = new PVector(Float.valueOf(parts[1]).floatValue(), Float.valueOf(parts[2])
						        .floatValue(), Float.valueOf(parts[3]).floatValue());
						coords.add(tempv);
						readv = true;
					} else if (parts[0].equals("vn")) {
						// normal
						final PVector tempn = new PVector(Float.valueOf(parts[1]).floatValue(), Float.valueOf(parts[2])
						        .floatValue(), Float.valueOf(parts[3]).floatValue());
						normals.add(tempn);
						readvn = true;
					} else if (parts[0].equals("vt")) {
						// uv, inverting v to take into account Processing's
						// inverted Y axis
						// with respect to OpenGL.
						final PVector tempv = new PVector(Float.valueOf(parts[1]).floatValue(), 1 - Float.valueOf(
						        parts[2]).floatValue());
						texcoords.add(tempv);
						readvt = true;
					} else if (parts[0].equals("o")) {
						// Object name is ignored, for now.
					} else if (parts[0].equals("mtllib")) {
						if (parts[1] != null) {
							final BufferedReader mreader = parent.createReader(parts[1]);
							if (mreader != null) {
								PShapeOBJ.parseMTL(parent, mreader, materials, mtlTable);
							}
						}
					} else if (parts[0].equals("g")) {
						gname = 1 < parts.length ? parts[1] : "";
					} else if (parts[0].equals("usemtl")) {
						// Getting index of current active material (will be
						// applied on
						// all subsequent faces).
						if (parts[1] != null) {
							final String mtlname = parts[1];
							if (mtlTable.containsKey(mtlname)) {
								final Integer tempInt = mtlTable.get(mtlname);
								mtlIdxCur = tempInt.intValue();
							} else {
								mtlIdxCur = -1;
							}
						}
					} else if (parts[0].equals("f")) {
						// Face setting
						final OBJFace face = new OBJFace();
						face.matIdx = mtlIdxCur;
						face.name = gname;

						for (int i = 1; i < parts.length; i++) {
							final String seg = parts[i];

							if (seg.indexOf("/") > 0) {
								final String[] forder = seg.split("/");

								if (forder.length > 2) {
									// Getting vertex and texture and normal
									// indexes.
									if ((forder[0].length() > 0) && readv) {
										face.vertIdx.add(Integer.valueOf(forder[0]));
									}

									if ((forder[1].length() > 0) && readvt) {
										face.texIdx.add(Integer.valueOf(forder[1]));
									}

									if ((forder[2].length() > 0) && readvn) {
										face.normIdx.add(Integer.valueOf(forder[2]));
									}
								} else if (forder.length > 1) {
									// Getting vertex and texture/normal
									// indexes.
									if ((forder[0].length() > 0) && readv) {
										face.vertIdx.add(Integer.valueOf(forder[0]));
									}

									if (forder[1].length() > 0) {
										if (readvt) {
											face.texIdx.add(Integer.valueOf(forder[1]));
										} else if (readvn) {
											face.normIdx.add(Integer.valueOf(forder[1]));
										}

									}

								} else if (forder.length > 0) {
									// Getting vertex index only.
									if ((forder[0].length() > 0) && readv) {
										face.vertIdx.add(Integer.valueOf(forder[0]));
									}
								}
							} else {
								// Getting vertex index only.
								if ((seg.length() > 0) && readv) {
									face.vertIdx.add(Integer.valueOf(seg));
								}
							}
						}

						faces.add(face);
					}
				}
			}

			if (materials.size() == 0) {
				// No materials definition so far. Adding one default material.
				final OBJMaterial defMtl = new OBJMaterial();
				materials.add(defMtl);
			}

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	static protected void parseMTL(final PApplet parent, final BufferedReader reader,
	        final ArrayList<OBJMaterial> materials, final Hashtable<String, Integer> materialsHash) {
		try {
			String line;
			OBJMaterial currentMtl = null;
			while ((line = reader.readLine()) != null) {
				// Parse the line
				line = line.trim();
				final String parts[] = line.split("\\s+");
				if (parts.length > 0) {
					// Extract the material data.
					if (parts[0].equals("newmtl")) {
						// Starting new material.
						final String mtlname = parts[1];
						currentMtl = new OBJMaterial(mtlname);
						materialsHash.put(mtlname, new Integer(materials.size()));
						materials.add(currentMtl);
					} else if (parts[0].equals("map_Kd") && (parts.length > 1)) {
						// Loading texture map.
						final String texname = parts[1];
						currentMtl.kdMap = parent.loadImage(texname);
					} else if (parts[0].equals("Ka") && (parts.length > 3)) {
						// The ambient color of the material
						currentMtl.ka.x = Float.valueOf(parts[1]).floatValue();
						currentMtl.ka.y = Float.valueOf(parts[2]).floatValue();
						currentMtl.ka.z = Float.valueOf(parts[3]).floatValue();
					} else if (parts[0].equals("Kd") && (parts.length > 3)) {
						// The diffuse color of the material
						currentMtl.kd.x = Float.valueOf(parts[1]).floatValue();
						currentMtl.kd.y = Float.valueOf(parts[2]).floatValue();
						currentMtl.kd.z = Float.valueOf(parts[3]).floatValue();
					} else if (parts[0].equals("Ks") && (parts.length > 3)) {
						// The specular color weighted by the specular
						// coefficient
						currentMtl.ks.x = Float.valueOf(parts[1]).floatValue();
						currentMtl.ks.y = Float.valueOf(parts[2]).floatValue();
						currentMtl.ks.z = Float.valueOf(parts[3]).floatValue();
					} else if ((parts[0].equals("d") || parts[0].equals("Tr")) && (parts.length > 1)) {
						// Reading the alpha transparency.
						currentMtl.d = Float.valueOf(parts[1]).floatValue();
					} else if (parts[0].equals("Ns") && (parts.length > 1)) {
						// The specular component of the Phong shading model
						currentMtl.ns = Float.valueOf(parts[1]).floatValue();
					}
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	protected static int rgbaValue(final PVector color) {
		return 0xFF000000 | ((int) (color.x * 255) << 16) | ((int) (color.y * 255) << 8) | (int) (color.z * 255);
	}

	protected static int rgbaValue(final PVector color, final double alpha) {
		return ((int) (alpha * 255) << 24) | ((int) (color.x * 255) << 16) | ((int) (color.y * 255) << 8)
		        | (int) (color.z * 255);
	}

	// Stores a face from an OBJ file
	static protected class OBJFace {
		ArrayList<Integer>	vertIdx;
		ArrayList<Integer>	texIdx;
		ArrayList<Integer>	normIdx;
		int		           matIdx;
		String		       name;

		OBJFace() {
			vertIdx = new ArrayList<Integer>();
			texIdx = new ArrayList<Integer>();
			normIdx = new ArrayList<Integer>();
			matIdx = -1;
			name = "";
		}
	}

	// Stores a material defined in an MTL file.
	static protected class OBJMaterial {
		String	name;
		PVector	ka;
		PVector	kd;
		PVector	ks;
		double	d;
		double	ns;
		PImage	kdMap;

		OBJMaterial() {
			this("default");
		}

		OBJMaterial(final String name) {
			this.name = name;
			ka = new PVector(0.5f, 0.5f, 0.5f);
			kd = new PVector(0.5f, 0.5f, 0.5f);
			ks = new PVector(0.5f, 0.5f, 0.5f);
			d = 1.0f;
			ns = 0.0f;
			kdMap = null;
		}
	}
}
