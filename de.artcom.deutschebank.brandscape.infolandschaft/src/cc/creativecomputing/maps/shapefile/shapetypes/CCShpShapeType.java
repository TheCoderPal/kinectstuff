/*  
 * Copyright (c) 2012 Christian Riekoff <info@texone.org>  
 *  
 *  This file is free software: you may copy, redistribute and/or modify it  
 *  under the terms of the GNU General Public License as published by the  
 *  Free Software Foundation, either version 2 of the License, or (at your  
 *  option) any later version.  
 *  
 *  This file is distributed in the hope that it will be useful, but  
 *  WITHOUT ANY WARRANTY; without even the implied warranty of  
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU  
 *  General Public License for more details.  
 *  
 *  You should have received a copy of the GNU General Public License  
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.  
 *  
 * This file incorporates work covered by the following copyright and  
 * permission notice:  
 */
package cc.creativecomputing.maps.shapefile.shapetypes;

// ----------------------------------------------------------------------------
// Shape Type
// ----------------------------------------------------------------------------
public enum CCShpShapeType {

	// Null_Shape ( 0 ),
	//
	// Point ( 1 ),
	// PolyLine ( 3 ),
	// Polygon ( 5 ),
	// MultiPoint ( 8 ),
	//
	// PointZ ( 11 ),
	// PolyLineZ ( 13 ),
	// PolygonZ ( 15 ),
	// MultiPointZ ( 18 ),
	//
	// PointM ( 21 ),
	// PolyLineM ( 23 ),
	// PolygonM ( 25 ),
	// MultiPointM ( 28 ),
	//
	// MultiPatch ( 31 )

	/** ID= 0 */
	NullShape(0, false, false),

	/** ID= 1 */
	Point(1, false, false),
	/** ID=11 */
	PointZ(11, true, true),
	/** ID=21 */
	PointM(21, false, true),

	/** ID= 3 */
	PolyLine(3, false, false),
	/** ID=13 */
	PolyLineZ(13, true, true),
	/** ID=23 */
	PolyLineM(23, false, true),

	/** ID= 5 */
	Polygon(5, false, false),
	/** ID=15 */
	PolygonZ(15, true, true),
	/** ID=25 */
	PolygonM(25, false, true),

	/** ID= 8 */
	MultiPoint(8, false, false),
	/** ID=18 */
	MultiPointZ(18, true, true),
	/** ID=28 */
	MultiPointM(28, false, true),

	/** ID=31 */
	MultiPatch(31, true, true);

	private int ID;
	private boolean has_z_values;
	private boolean has_m_values;

	private CCShpShapeType(int ID, boolean has_z_values, boolean has_m_values) {
		this.has_z_values = has_z_values;
		this.has_m_values = has_m_values;
		this.ID = ID;
	}

	public int ID() {
		return this.ID;
	}

	public static CCShpShapeType byID(int ID) throws Exception {
		for (CCShpShapeType st : CCShpShapeType.values())
			if (st.ID == ID)
				return st;
		throw new Exception("ShapeType: " + ID + " does not exist");
	}

	public boolean hasZvalues() {
		return has_z_values;
	}

	public boolean hasMvalues() {
		return has_m_values;
	}

	public boolean isTypeOfPolygon() {
		return (this == CCShpShapeType.Polygon | this == CCShpShapeType.PolygonM | this == CCShpShapeType.PolygonZ);
	}

	public boolean isTypeOfPolyLine() {
		return (this == CCShpShapeType.PolyLine | this == CCShpShapeType.PolyLineM | this == CCShpShapeType.PolyLineZ);
	}

	public boolean isTypeOfPoint() {
		return (this == CCShpShapeType.Point | this == CCShpShapeType.PointM | this == CCShpShapeType.PointZ);
	}

	public boolean isTypeOfMultiPoint() {
		return (this == CCShpShapeType.MultiPoint | this == CCShpShapeType.MultiPointM | this == CCShpShapeType.MultiPointZ);
	}
}