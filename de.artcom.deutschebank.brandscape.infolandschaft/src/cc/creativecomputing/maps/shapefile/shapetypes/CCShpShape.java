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
import java.nio.ByteOrder;

import cc.creativecomputing.maps.shapefile.CCShapeFileException;

/**
 * Base class for Shapes.
 * 
 * @author thomas diewald (2012)
 * 
 */
public abstract class CCShpShape {
	protected CCShpShapeType _myShapeType;

	// RECORD HEADER
	protected int _myRecordNumber, _myContentLength, SHP_shape_type;
	protected int position_start, position_end, content_length; // for checking

	protected CCShpShape(CCShpShapeType theShapeType) {
		_myShapeType = theShapeType;
	}

	/**
	 * read the shape-data from the bytebuffer (buffer-position has to be defined before).<br>
	 * 
	 * @param bb byte-buffer
	 * @return current Shape-instance
	 * @throws Exception
	 */
	public CCShpShape read(ByteBuffer bb) {
		// 1) READ RECORD HEADER
		readRecordHeader(bb);

		// 2) READ RECORD CONTENT
		position_start = bb.position();

		// 2.1) check Shape Type
		bb.order(ByteOrder.LITTLE_ENDIAN);
		SHP_shape_type = bb.getInt();
		try {
			CCShpShapeType shape_type_tmp = CCShpShapeType.byID(SHP_shape_type);
			if (shape_type_tmp == _myShapeType) {
				readRecordContent(bb);
			} else if (shape_type_tmp == CCShpShapeType.NullShape) {
				;
			} else if (shape_type_tmp != _myShapeType) {
				throw new Exception("(Shape) shape_type = " + shape_type_tmp + ", but expected " + _myShapeType);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		position_end = bb.position();
		content_length = position_end - position_start;
		if (content_length != _myContentLength * 2)
			throw new CCShapeFileException("(Shape) content_length = " + content_length + ", but expected " + _myContentLength * 2);

		// if( SHP_File.__PRINT_ON_LOAD)
		// print();

		return this;
	}

	protected void readRecordHeader(ByteBuffer bb) {
		bb.order(ByteOrder.BIG_ENDIAN);
		_myRecordNumber = bb.getInt();
		_myContentLength = bb.getInt();
	}

	protected abstract void readRecordContent(ByteBuffer bb);

	public abstract void print();

	/**
	 * get the record number of the shape.
	 * 
	 * @return record number
	 */
	public int recordNumber() {
		return _myRecordNumber;
	}

	/**
	 * get the Type of the Shape.
	 * 
	 * @return ShpShape.Type
	 */
	public CCShpShapeType type() {
		return _myShapeType;
	}

}
