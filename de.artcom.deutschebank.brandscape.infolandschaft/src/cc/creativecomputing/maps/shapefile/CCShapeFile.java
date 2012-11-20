/**
 * diewald_shapeFileReader.
 * 
 * a Java Library for reading ESRI-shapeFiles (*.shp, *.dfb, *.shx).
 * 
 * 
 * Copyright (c) 2012 Thomas Diewald
 *
 *
 * This source is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * A copy of the GNU General Public License is available on the World
 * Wide Web at <http://www.gnu.org/copyleft/gpl.html>. You can also
 * obtain it by writing to the Free Software Foundation,
 * Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package cc.creativecomputing.maps.shapefile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cc.creativecomputing.maps.shapefile.CCShapeFile;
import cc.creativecomputing.maps.shapefile.shapetypes.CCShpShapeType;
import cc.creativecomputing.maps.shapefile.shapetypes.CCShpShape;

/**
 * main class for loading/reading a "ShapeFile".<br>
 * a ShapeFile consists of at least the following files:<br>
 * 
 * <pre>
 * dBaseFile:      *.dbf
 * shapeFile:      *.shp
 * ShapeIndexFile: *.shx
 * </pre>
 * 
 * all this files must be located in the same given folder and start with the same name.<br>
 * example: "my_shapefile.dbf", "my_shapefile.shp", "my_shapefile.shx"<br>
 * In each of the .shp, .shx, and .dbf files, the shapes in each file correspond to each other in sequence. <br>
 * That is, the first record in the .shp file corresponds to the first record in the .shx and .dbf files, and so on.<br>
 * <br>
 * 
 * <a href="http://en.wikipedia.org/wiki/Shapefile" target=blank>http://en.wikipedia.org/wiki/Shapefile"</a><br>
 * 
 * @author thomas diewald (2012)
 * 
 */
public class CCShapeFile {

	// mandatory files
	private CCShxFile _myShxFile; // index-File: contains offsets and content lengths of each record in the main shp-file.
	private CCDbfFile _myDbfFile; // dBASE-file: attribute format; columnar attributes for each shape, in dBase IV format.
	private CCShpFile _myShpFile; // shape-File: contains geometry.

	/**
	 * <pre>
	 * init the ShapeFile, and load the following files:
	 *   "path + filename.shx",
	 *   "path + filename.dbf",
	 *   "path + filename.shp"
	 * </pre>
	 * 
	 * @param thePath
	 * @param theFileName
	 * @throws Exception
	 */
	public CCShapeFile(String thePath, String theFileName){

		// MAIN DIRECTORY
		File dir = new File(thePath);

		// GENERATE NEW READERS
		_myShxFile = new CCShxFile(this, new File(dir, theFileName + ".shx"));
		_myDbfFile = new CCDbfFile(this, new File(dir, theFileName + ".dbf"));
		_myShpFile = new CCShpFile(this, new File(dir, theFileName + ".shp"));
	}

	/**
	 * read shape file.
	 * 
	 * @return current instance
	 * @throws Exception
	 */
	public CCShapeFile read(){
		_myShxFile.read();
		_myDbfFile.read();
		_myShpFile.read();
		return this;
	}

	// ----------------------------------------------------------------------------
	// GET FILE READERS
	// ----------------------------------------------------------------------------
	/**
	 * @return the Shape Index File (*.shx).
	 */
	public CCShxFile shxFile() {
		return _myShxFile;
	}

	/**
	 * @return the dBase File (*.dbf).
	 */
	public CCDbfFile dbfFile() {
		return _myDbfFile;
	}

	/**
	 * @return the Shape File (*.shp).
	 */
	public CCShpFile getFile_SHP() {
		return _myShpFile;
	}

	// ----------------------------------------------------------------------------
	// SIMPLE DATA ACCESS
	// ----------------------------------------------------------------------------

	// ----------------------------------------------------------------------------
	// SHAPE FILE
	// ----------------------------------------------------------------------------

	/**
	 * get the number of shapes, contained in the shp-file.
	 * 
	 * @return number of Shapes
	 */
	public int getSHP_shapeCount() {
		return _myShpFile.shapes().size();
	}

	/**
	 * get a list of all shapes. <br>
	 * e.g. ArrayList<ShpPolygon> shape = shape_file.getShapes();
	 * 
	 * @param <T> Shape: ShpPolygon, ShpPolyLine, ShpPoint, or ShpMultiPoint
	 * @return list of Shapes
	 */
	public List<CCShpShape> shapes() {
		return _myShpFile.shapes();
	}

	/**
	 * get a shapes by the given index.<br>
	 * e.g. ShpPolygon shape = shape_file.getShape(0);
	 * 
	 * @param <T> Shape: ShpPolygon, ShpPolyLine, ShpPoint, or ShpMultiPoint
	 * @param index
	 * @return shape
	 */
	public CCShpShape shape(int index) {
		return shapes().get(index);
	}

	/**
	 * get the shapeType of the shapes contained in the file.
	 * 
	 * @return shapeType
	 */
	public CCShpShapeType shapeType() {
		return _myShpFile.header().type();
	}

	/**
	 * same as: {@link cc.creativecomputing.maps.shapefile.CCShapeFileHeader#getBoundingBox()}
	 * 
	 * @return boundingbox-values
	 */
	public double[][] getSHP_boundingBox() {
		return _myShpFile.header().getBoundingBox();
	}

	// ----------------------------------------------------------------------------
	// DATA BASE FILE
	// ----------------------------------------------------------------------------

	// DATA BASE FIELD
	public int getDBF_fieldCount() {
		return _myDbfFile.getFields().length;
	}

	public DBF_Field[] getDBF_field() {
		return _myDbfFile.getFields();
	}

	public DBF_Field getDBF_field(int index) {
		return _myDbfFile.getFields()[index];
	}

	// DATA BASE RECORD
	/**
	 * get the number of data-base records, contained in the dbf-file (same as number of shapes).
	 * 
	 * @return number of data-base records.
	 */
	public int getDBF_recordCount() {
		return _myDbfFile.getContent().length;
	}

	/**
	 * same as: {@link cc.creativecomputing.maps.shapefile.CCDbfFile#getContent()}
	 * 
	 * @return 2D-String-Table.
	 */
	public String[][] getDBF_record() {
		return _myDbfFile.getContent();
	}

	public String[] getDBF_record(int index) {
		return _myDbfFile.getContent()[index];
	}

	public String getDBF_record(int row, int col) {
		return _myDbfFile.getContent()[row][col];
	}

}