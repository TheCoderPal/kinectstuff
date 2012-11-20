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

package cc.creativecomputing.maps.shapefile.shapetypes;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cc.creativecomputing.math.CCPath3f;
import cc.creativecomputing.math.CCPath4f;
import cc.creativecomputing.math.CCVector3f;
import cc.creativecomputing.math.CCVector4f;
import cc.creativecomputing.math.spline.CCLinearSpline;
import cc.creativecomputing.math.spline.CCSpline.CCSplineType;

/**
 * Shape: Polygon.<br>
 * 
 * <pre>
 * polygon:   consists of one or more rings (multiple outer rings).
 * ring:      four or more points ... closed, non-self-intersecting loop.
 * interiour: clockwise order of vertices.
 * same content as PolyLine.
 * possible ShapeTypes:
 *   Polygon   (  8 ), 
 *   PolygonZ  ( 18 ), 
 *   PolygonM  ( 28 ),
 * </pre>
 * 
 * @author thomas diewald (2012)
 * 
 */
public class CCShpPolygon extends CCShpShape {

	// SHAPE RECORD CONTENT
	private double[][] SHP_bbox = new double[3][2]; // [x, y, z][min, max]
	private double[] SHP_range_m = new double[2]; // [min, max]

	private int _myNumberOfParts, _myNumberOfPoints;
	private int[] SHP_parts;
	private List<CCVector3f> _myPoints = new ArrayList<CCVector3f>(); 
	private double[] _myMeasureValues; // [number of points][m-value]

	private List<CCLinearSpline> _myParts= null; // [number of polygons][vertices][x, y, z, w]

	public CCShpPolygon(CCShpShapeType shape_type) {
		super(shape_type);
	}

	@Override
	protected void readRecordContent(ByteBuffer bb) {
		SHP_bbox[0][0] = bb.getDouble(); // x-min
		SHP_bbox[1][0] = bb.getDouble(); // y-min
		SHP_bbox[0][1] = bb.getDouble(); // x-max
		SHP_bbox[1][1] = bb.getDouble(); // y-max
		_myNumberOfParts = bb.getInt(); // number of polygon-parts / rings
		_myNumberOfPoints = bb.getInt(); // number of points (total of all parts)

		SHP_parts = new int[_myNumberOfParts];
		for (int i = 0; i < _myNumberOfParts; i++) {
			SHP_parts[i] = bb.getInt(); // index of the point-list (indicates start-point of a polygon)
		}

		for (int i = 0; i < _myNumberOfPoints; i++) {
			_myPoints.add(new CCVector3f(bb.getDouble(), bb.getDouble(),0));
		}

		// if SHAPE-TYPE: 15
		if (_myShapeType.hasZvalues()) {
			SHP_bbox[2][0] = bb.getDouble(); // z-min
			SHP_bbox[2][1] = bb.getDouble(); // z-max
			for (int i = 0; i < _myNumberOfPoints; i++) {
				_myPoints.get(i).z = (float)bb.getDouble(); // z - coordinate
			}
		}

		// if SHAPE-TYPE: 15 | 25
		if (_myShapeType.hasMvalues()) {
			SHP_range_m[0] = bb.getDouble(); // m-min
			SHP_range_m[1] = bb.getDouble(); // m-max
			_myMeasureValues = new double[_myNumberOfPoints];
			for (int i = 0; i < _myNumberOfPoints; i++) {
				_myMeasureValues[i] = bb.getDouble(); // m - value
			}
		}
	}

	@Override
	public void print() {
		System.out.printf(Locale.ENGLISH, "   _ _ _ _ _ \n");
		System.out.printf(Locale.ENGLISH, "  / SHAPE   \\_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _\n");
		System.out.printf(Locale.ENGLISH, "  |                                                    \\\n");
		System.out.printf(Locale.ENGLISH, "  |  <RECORD HEADER>\n");
		System.out.printf(Locale.ENGLISH, "  |    SHP_record_number       = %d\n", _myRecordNumber);
		System.out.printf(Locale.ENGLISH, "  |    SHP_content_length      = %d bytes  (check: start/end/size = %d/%d/%d)\n", _myContentLength * 2, position_start, position_end,
				content_length);
		System.out.printf(Locale.ENGLISH, "  |\n");
		System.out.printf(Locale.ENGLISH, "  |  <RECORD CONTENT>\n");
		System.out.printf(Locale.ENGLISH, "  |    shape_type              = %s (%d)\n", _myShapeType, _myShapeType.ID());
		System.out.printf(Locale.ENGLISH, "  |    SHP_bbox: xmin, xmax    = %+7.3f, %+7.3f\n", SHP_bbox[0][0], SHP_bbox[0][1]);
		System.out.printf(Locale.ENGLISH, "  |    SHP_bbox: ymin, ymax    = %+7.3f, %+7.3f\n", SHP_bbox[1][0], SHP_bbox[1][1]);
		System.out.printf(Locale.ENGLISH, "  |    SHP_bbox: zmin, zmax    = %+7.3f, %+7.3f\n", SHP_bbox[2][0], SHP_bbox[2][1]);
		System.out.printf(Locale.ENGLISH, "  |    SHP_measure: mmin, mmax = %+7.3f, %+7.3f\n", SHP_range_m[0], SHP_range_m[1]);
		System.out.printf(Locale.ENGLISH, "  |    SHP_num_parts           = %d\n", _myNumberOfParts);
		System.out.printf(Locale.ENGLISH, "  |    SHP_num_points          = %d\n", _myNumberOfPoints);
		// for(int i = 0; i < SHP_num_parts; i++){
		// System.out.printf(Locale.ENGLISH, "  |     part_idx[%d] = %d\n", i, SHP_parts[i] );
		// }
		//
		System.out.printf(Locale.ENGLISH, "  \\_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ /\n");
	}

	/**
	 * get the BoundingBox..<br>
	 * data storage: [x, y, z][min, max] <br>
	 * 
	 * @return 2d-array (double), dim-size:[3][2]
	 */
	public double[][] getBoundingBox() {
		return SHP_bbox;
	}

	/**
	 * get range of Measure-Values.<br>
	 * data storage: [min, max] <br>
	 * 
	 * @return 1d-array (double), dim-size:[2]
	 */
	public double[] getMeasureRange() {
		return SHP_range_m;
	}

	/**
	 * get the number of points(vertices).
	 * 
	 * @return the number of points(vertices).
	 */
	public int numberOfPoints() {
		return _myNumberOfPoints;
	}

	/**
	 * get the number of parts(Polygons)
	 * 
	 * @return the number of parts(Polygons).
	 */
	public int numberOfParts() {
		return _myNumberOfParts;
	}

	/**
	 * get an array of all points(vertices).
	 * 
	 * @return an array of all points(vertices).
	 */
	public List<CCVector3f> points() {
		return _myPoints;
	}

	/**
	 * generates a list of polygons, and returns a 3d-double array.<br>
	 * [number of polygons][number of points per polygon][x, y, z, m].
	 * 
	 * @return 3d-double array.
	 */
	public List<CCLinearSpline> parts() {
		// if the method was called before, we already have the array.
		if (_myParts != null) {
			return _myParts;
		}

		int[] indices = new int[_myNumberOfParts + 1]; // generate new indices array
		System.arraycopy(SHP_parts, 0, indices, 0, _myNumberOfParts); // copy start indices
		indices[indices.length - 1] = _myNumberOfPoints; // and add last index

		_myParts = new ArrayList<CCLinearSpline>();
		for (int i = 0; i < indices.length - 1; i++) {
			int from = indices[i]; // start index
			int to = indices[i + 1]; // end-index + 1
			int size = to - from;
			CCLinearSpline myPart = new CCLinearSpline(false);
			for (int j = from, idx = 0; j < to; j++, idx++) {
				CCVector4f myPoint4f = new CCVector4f();
				CCVector3f myPoint3f = _myPoints.get(j);
				myPoint4f.x = myPoint3f.x; // copy of x-value
				myPoint4f.y = myPoint3f.y; // copy of y-value
				myPoint4f.z = myPoint3f.z; // copy of z-value
				if (_myShapeType.hasMvalues()) {
					myPoint4f.w = (float)_myMeasureValues[j]; // copy of m-value
				}
				myPart.addPoint(myPoint4f);
			}
			myPart.endEditSpline();
			_myParts.add(myPart);
		}
		return _myParts;
	}

	/**
	 * get the Measure Values as an Array.
	 * 
	 * @return measure-values. (size=.getNumberOfPoints()).
	 */
	public double[] getMeasureValues() {
		return _myMeasureValues;
	}
}
