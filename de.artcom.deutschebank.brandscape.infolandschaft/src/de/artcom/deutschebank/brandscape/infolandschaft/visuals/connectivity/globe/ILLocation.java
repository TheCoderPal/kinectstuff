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
package de.artcom.deutschebank.brandscape.infolandschaft.visuals.connectivity.globe;

import cc.creativecomputing.control.CCControl;
import cc.creativecomputing.graphics.font.CCTextureMapFont;
import cc.creativecomputing.graphics.font.text.CCTextAlign;
import cc.creativecomputing.math.CCMath;
import cc.creativecomputing.math.CCVector3f;
import cc.creativecomputing.math.util.CCMovingAverage;
import cc.creativecomputing.xml.CCXMLElement;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.diversity.ILText;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.diversity.ILText.ILTextSettings;

/**
 * @author christianriekoff
 *
 */
public class ILLocation {
	
	@CCControl(name = "shadow settings")
	private static ILTextSettings _cShadowSettings = new ILTextSettings();
	@CCControl(name = "text settings")
	private static ILTextSettings _cTextSettings = new ILTextSettings();
	
	private float _myLongitude;
	private float _myLatitude;
	private float _myValue;
	
	private CCMovingAverage<Float> _myTouchBlend;
	
	private float _mySelectProgress;
	
	private CCVector3f _myPosition;
	private CCVector3f _my2DPosition;
	
	private boolean _myIsSelectable = false;
	
	private String _myName;
	
	private ILText _myText;
	private ILText _myTextShadow;
	
	private ILText _myContent;
	private ILText _myContentShadow;
	
	public ILLocation(CCXMLElement theDataXML, CCTextureMapFont theTextFont, CCTextureMapFont theShadowFont) {
		_myName = theDataXML.child("name").content();
		_myValue = theDataXML.child("value").floatContent();
		_myLatitude = theDataXML.child("latitude").floatContent();
		_myLongitude = theDataXML.child("longitude").floatContent();
		
		_myPosition = sphereCoords(_myLongitude, _myLatitude, 1);
		_my2DPosition = new CCVector3f();
		
		_myTextShadow = new ILText(theShadowFont, _cShadowSettings);
		_myTextShadow.align(CCTextAlign.CENTER);
		_myTextShadow.text(_myName);
		
		_myText = new ILText(theTextFont, _cTextSettings);
		_myText.align(CCTextAlign.CENTER);
		_myText.text(_myName);
		
		_myContentShadow = new ILText(theShadowFont, _cShadowSettings);
		_myContentShadow.text(theDataXML.child("text").child("en").content());
		
		_myContent = new ILText(theTextFont, _cTextSettings);
		_myContent.text(theDataXML.child("text").child("en").content());
		
		_myTouchBlend = CCMovingAverage.floatAverage(0.9f);
		_myTouchBlend.skipRange(10f);
	}
	
	public void update(float theDeltaTime) {
		_mySelectProgress -= theDeltaTime;
		_mySelectProgress = CCMath.saturate(_mySelectProgress);
	}
	
	public ILText content() {
		return _myContent;
	}
	
	public ILText contentShadow() {
		return _myContentShadow;
	}
	
	public ILText text() {
		return _myText;
	}
	
	public ILText textShadow() {
		return _myTextShadow;
	}
	
	public float value() {
		return _myValue;
	}
	
	public float touchBlend() {
		return _myTouchBlend.value();
	}
	
	public void touchBlend(float theBlend, float theSmooth) {
		_myTouchBlend.weight(theSmooth);
		_myTouchBlend.update(theBlend);
	}
	
	public void selectProgress(float theSelectProgress) {
		_mySelectProgress = theSelectProgress;
	}
	
	public float selectProgress() {
		return _mySelectProgress;
	}
	
	public String name() {
		return _myName;
	}
	
	public float longitude() {
		return _myLongitude;
	}
	
	public float latitude() {
		return _myLatitude;
	}
	
	public CCVector3f position() {
		return _myPosition;
	}
	
	public CCVector3f position2D() {
		return _my2DPosition;
	}

	private CCVector3f sphereCoords(float theLat, float theLong, float theRadius) {
		float lo = CCMath.radians(-theLat + 180);
		float la = CCMath.radians(theLong + 90);
				
		return new CCVector3f(
			theRadius * CCMath.sin(la) * CCMath.cos(lo),
			theRadius * CCMath.sin(la) * CCMath.sin(lo),
			theRadius * CCMath.cos(la)
		);
	}
	
	public boolean isSelectable() {
		return _myIsSelectable;
	}
	
	public void isSelectable(boolean theIsSelectable) {
		_myIsSelectable = theIsSelectable;
	}
}
