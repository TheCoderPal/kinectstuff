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
package de.artcom.deutschebank.brandscape.infolandschaft.visuals.connectivity;

import java.util.ArrayList;
import java.util.List;

import cc.creativecomputing.control.CCControl;
import cc.creativecomputing.graphics.CCDrawMode;
import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.graphics.font.CCFontIO;
import cc.creativecomputing.graphics.font.CCOutlineFont;
import cc.creativecomputing.graphics.font.text.CCTextAlign;
import cc.creativecomputing.graphics.font.text.CCTextContours;
import cc.creativecomputing.math.CCMath;
import cc.creativecomputing.math.CCVecMath;
import cc.creativecomputing.math.CCVector2f;

/**
 * @author christianriekoff
 *
 */
public class ILContourText {
	private class Letter{
		private Path _myPath;
		
		private float _myMax;
		
		
		private float _myDelay;
		
		public Letter(final Path thePath, float theDelay) {
			_myPath = thePath;
			
			_myMax = 0;
			_myDelay = -theDelay;
		}
		
		public void update(final float theDeltaTime) {
			_myMax = _cProgress * (1 + _cDelay) + _myDelay * _cDelay;
		}
		
		public void draw(CCGraphics g) {
			_myMax = _cProgress * (1 + _cDelay) + _myDelay * _cDelay;
			_myPath.draw(g, 0, CCMath.constrain(_myMax,0,1));
		}
		
		public void reset() {
			_myMax = -_myDelay * _cDelay;
		}
	}
	
	private static class Path{
		
		private float _myLength = 0;
		private CCVector2f _myLastPoint;
		
		private List<Float> _myLengths = new ArrayList<Float>();
		private List<Float> _myDistances = new ArrayList<Float>();
		private List<CCVector2f> _myPoints = new ArrayList<CCVector2f>();
		
		public void addPoint(CCVector2f thePoint) {
			if(_myLastPoint == null) {
				_myLengths.add(0f);
			}else {
				float myDistance = _myLastPoint.distance(thePoint);
				_myLength += myDistance;
				_myLengths.add(_myLength);
				_myDistances.add(myDistance);
			}
			_myLastPoint = thePoint;
			_myPoints.add(thePoint);
		}
		
		public int pointIndex(final float theBlend) {
			float myPointLength = theBlend * _myLength;
			
			int myIndex = 0;
			for(float myLength:_myLengths) {
				if(myPointLength < myLength) {
					break;
				}
				myIndex++;
			}
			
			return myIndex;
		}
		
		public CCVector2f point(final float theBlend) {
			float myPointLength = theBlend * _myLength;
			
			if(theBlend == 1f)return _myPoints.get(_myPoints.size()-1);
			if(theBlend == 0f)return _myPoints.get(0);
			
			int myIndex = 0;
			float myPosition = 0;
			for(float myLength:_myLengths) {
				if(myPointLength < myLength) {
					myPosition = myLength - myPointLength;
					break;
				}
				myIndex++;
			}
			float myBlend = myPosition / _myDistances.get(myIndex - 1);

			CCVector2f myV1 = _myPoints.get(myIndex - 1);
			CCVector2f myV2 = _myPoints.get(myIndex);
			
			return CCVecMath.blend(1 - myBlend, myV1, myV2);
		}
		
		public void draw(CCGraphics g, float theMin, float theMax) {
			CCVector2f myStartPoint = point(theMin);
			CCVector2f myEndPoint = point(theMax);
			
			int myStartIndex = pointIndex(theMin);
			int myEndIndex = pointIndex(theMax);
			
			g.beginShape(CCDrawMode.LINE_STRIP);
			g.vertex(myStartPoint);
			for(int i = myStartIndex; i < myEndIndex; i++) {
				CCVector2f myVertex = _myPoints.get(i);
				g.vertex(myVertex);
			}
			g.vertex(myEndPoint);
			g.endShape();
		}
	}

	int nNumPoints = 4;

	private float _myTime = 0.001f;

	private List<Letter> _myLetters;
	
	private CCTextContours _myTextContour;
	
	@CCControl(name = "delay", min = 0, max = 10)
	private float _cDelay = 0;
	@CCControl(name = "progress", min = 0, max = 1)
	private float _cProgress = 0;
	@CCControl(name = "stroke weight", min = 0.1f, max = 20)
	private float _cStrokeWeight = 1;
	
	public ILContourText(String theText) {
		CCOutlineFont font = CCFontIO.createOutlineFont("Arial",24, 30);
		
		_myTextContour = new CCTextContours(font);
		_myTextContour.align(CCTextAlign.CENTER);
		_myTextContour.text(theText);// _myTextPath = font.getPath(myChar, CCTextAlign.CENTER, 50,myX, 0, 0);
		
		_myLetters = new ArrayList<Letter>();
		float myCounter = 0;
		for(List<CCVector2f> myContour:_myTextContour.contours()) {
			Path _myPath = new Path();
			
			for(int i = 0; i < myContour.size();i++) {
				CCVector2f myPoint = myContour.get(i);
				_myPath.addPoint(myPoint);
			}

			
			_myPath.addPoint(myContour.get(0));
			
			_myLetters.add(new Letter(_myPath, myCounter / _myTextContour.contours().size()));
			myCounter++;
		}
	}
	
	public void progress(float theProgress) {
		_cProgress = theProgress;
	}
	
	public void delay(float theDelay) {
		_cDelay = theDelay;
	}
	
	public void update(final float theDeltaTime) {
		_myTime += theDeltaTime * 0.1f;
		if(_myTime > 1)_myTime-=1;
		for(Letter myLetter:_myLetters) {
			myLetter.update(theDeltaTime);
		}
	}
	
	public void draw(CCGraphics g){
		g.strokeWeight(_cStrokeWeight);
		for(Letter myLetter:_myLetters) {
			myLetter.draw(g);
		}
	}
	
	public void reset() {
		for(Letter myLetter:_myLetters) {
			myLetter.reset();
		}
	}
}
