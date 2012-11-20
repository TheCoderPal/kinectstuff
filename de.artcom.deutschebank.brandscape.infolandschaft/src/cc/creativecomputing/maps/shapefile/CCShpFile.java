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
import java.util.Locale;

import cc.creativecomputing.maps.shapefile.shapetypes.CCShpShapeType;
import cc.creativecomputing.maps.shapefile.shapetypes.ShpMultiPoint;
import cc.creativecomputing.maps.shapefile.shapetypes.ShpPoint;
import cc.creativecomputing.maps.shapefile.shapetypes.ShpPolyLine;
import cc.creativecomputing.maps.shapefile.shapetypes.CCShpPolygon;
import cc.creativecomputing.maps.shapefile.shapetypes.CCShpShape;

//http://webhelp.esri.com/arcgisdesktop/9.2/index.cfm?TopicName=Shapefile_file_extensions
/**
 * Shape File Reader (*.shp).<br>
 * <br>
 * contains geometry/shapes of a certain type (ShpPoint, ShpMultiPoint, ShpPolygon, ShpPolyLine).<br>
 * 
 * @author thomas diewald (2012)
 */
public class CCShpFile extends CCShapeFileReader {

	/** enable/disable general info-logging. */
	public static boolean LOG_INFO = true;
	/** enable/disable logging of the header, while loading. */
	public static boolean LOG_ONLOAD_HEADER = true;
	/** enable/disable logging of the content, while loading. */
	public static boolean LOG_ONLOAD_CONTENT = true;

	private CCShapeFileHeader header;
	private ArrayList<CCShpShape> shapes = new ArrayList<CCShpShape>(); // works independent of any *.shx file.

	public CCShpFile(CCShapeFile parent_shapefile, File file) {
		super(parent_shapefile, file);
	}

	@Override
	public void read(){
		// READ HEADER
		header = new CCShapeFileHeader(parent_shapefile, file);
		header.read(bb);

		if (LOG_ONLOAD_HEADER)
			printHeader();

		CCShpShapeType shape_type = header.type();

		// READ CONTENT (depends on the Shape.Type)
		if (shape_type == CCShpShapeType.NullShape) {
			;// TODO: handle NullShapes
		} else if (shape_type.isTypeOfPolygon()) {
			while (bb.position() != bb.capacity())
				shapes.add(new CCShpPolygon(shape_type).read(bb));
		} else if (shape_type.isTypeOfPolyLine()) {
			while (bb.position() != bb.capacity())
				shapes.add(new ShpPolyLine(shape_type).read(bb));
		} else if (shape_type.isTypeOfPoint()) {
			while (bb.position() != bb.capacity())
				shapes.add(new ShpPoint(shape_type).read(bb));
		} else if (shape_type.isTypeOfMultiPoint()) {
			while (bb.position() != bb.capacity())
				shapes.add(new ShpMultiPoint(shape_type).read(bb));
		} else if (shape_type == CCShpShapeType.MultiPatch) {
			System.err.println("(ShapeFile) Shape.Type.MultiPatch not supported at the moment.");
		}

		if (LOG_ONLOAD_CONTENT)
			printContent();

		if (LOG_INFO)
			// System.out.println("(ShapeFile) loaded *.shp-File: \""+file.getName()+"\",  shapes="+shapes.size()+"("+shape_type+")");
			System.out.printf("(ShapeFile) loaded File: \"%s\", records=%d (%s-Shapes)\n", file.getName(), shapes.size(), shape_type);
	}

	public CCShapeFileHeader header() {
		return header;
	}

	/**
	 * get the shapes of the file as an ArrayList.<br>
	 * 
	 * <pre>
	 * elements can be of type (proper casting!):
	 * ShpPoint
	 * ShpMultiPoint
	 * ShpPolygon
	 * ShpPolyLine
	 * </pre>
	 * 
	 * @return ArrayList with elements of type: ShpShape
	 */
	public List<CCShpShape> shapes() {
		return shapes;
	}

	@Override
	public void printHeader() {
		header.print();
	}

	@Override
	public void printContent() {
		System.out.printf(Locale.ENGLISH, "\n");
		System.out.printf(Locale.ENGLISH, "________________________< CONTENT >________________________\n");
		System.out.printf(Locale.ENGLISH, "  FILE: \"%s\"\n", file.getName());
		System.out.printf(Locale.ENGLISH, "\n");
		for (CCShpShape shape : shapes) {
			shape.print();
		}
		System.out.printf(Locale.ENGLISH, "\n");
		System.out.printf(Locale.ENGLISH, "________________________< /CONTENT >________________________\n");
	}
}
