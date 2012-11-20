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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * base class for Shape-File-Readers. (*.shx, *.shp, *.dbf, ...).
 * 
 * @author thomas diewald (2012).
 * 
 */
public abstract class CCShapeFileReader {

	protected CCShapeFile parent_shapefile;
	protected File file;
	protected ByteBuffer bb;

	public CCShapeFileReader(CCShapeFile parent_shapefile, File file) {
		this.parent_shapefile = parent_shapefile;
		this.file = file;
		this.bb = loadFile(file);
	}

	public abstract void read() throws Exception;

	public abstract void printHeader();

	public abstract void printContent();

	private ByteBuffer loadFile(File theFile)  {
		try {
			FileInputStream is = new FileInputStream(theFile);
			BufferedInputStream bis = new BufferedInputStream(is);
			byte data[] = new byte[bis.available()];
			bis.read(data);
			bis.close();
			is.close();
			return ByteBuffer.wrap(data);
		} catch (IOException e) {
			throw new CCShapeFileException(e);
		}
	}

	public CCShapeFile getShapeFile() {
		return parent_shapefile;
	}

	public File getFile() {
		return file;
	}

}
