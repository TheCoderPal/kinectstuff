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
package de.artcom.deutschebank.brandscape.infolandschaft.visuals;

import java.util.ArrayList;
import java.util.List;

import cc.creativecomputing.CCApp;
import cc.creativecomputing.control.CCControl;
import cc.creativecomputing.events.CCMouseAdapter;
import cc.creativecomputing.events.CCMouseEvent;
import cc.creativecomputing.events.CCMouseMotionListener;
import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.math.CCVector2f;
import cc.creativecomputing.xml.CCXMLElement;
import cc.creativecomputing.xml.CCXMLIO;

/**
 * @author christianriekoff
 *
 */
public class ILContentLocations {
	
	@CCControl(name = "edit mode")
	private boolean _cIsInEditMode = false;
	
	public static class ILContentLocation{
		private boolean _myIsTaken;
		private CCVector2f _myLocation;
		
		private ILContentLocation(float theX, float theY) {
			_myIsTaken = false;
			_myLocation = new CCVector2f(theX, theY);
		}
		
		public CCVector2f location() {
			return _myLocation;
		}
		
		public boolean isTaken() {
			return _myIsTaken;
		}
		
		public void isTaken(boolean theIsTaken) {
			_myIsTaken = theIsTaken;
		}
	}

	private List<ILContentLocation> _myContentLocations = new ArrayList<ILContentLocation>();
	
	private CCApp _myApp;
	
	private ILContentLocation _mySelectedLocation;
	
	private boolean _myHasMoved = false;
	
	private String _myFile;
	
	public ILContentLocations(CCApp theApp, String theFile) {
		_myApp = theApp;
		
		_myApp.addMouseListener(new CCMouseAdapter() {
			
			@Override
			public void mousePressed(CCMouseEvent theEvent) {
				if(!theEvent.isShiftDown())return;
				if(!_cIsInEditMode)return;
				_myHasMoved = false;
				if(_mySelectedLocation == null) {
					_mySelectedLocation = new ILContentLocation(theEvent.x(), _myApp.height - theEvent.y());
					_myHasMoved = true;
					_myContentLocations.add(_mySelectedLocation);
				}
			}
				
			@Override
			public void mouseReleased(CCMouseEvent theEvent) {
				if(!theEvent.isShiftDown())return;
				if(!_cIsInEditMode)return;
				if(!_myHasMoved && _mySelectedLocation != null) {
					_myContentLocations.remove(_mySelectedLocation);
					_mySelectedLocation = null;
				}
				CCXMLElement myData = new CCXMLElement("locations");
				for(ILContentLocation myLocation:_myContentLocations) {
					CCXMLElement myLocationXML = myData.createChild("location");
					myLocationXML.createChild("x", myLocation.location().x);
					myLocationXML.createChild("y", myLocation.location().y);
				}
				CCXMLIO.saveXMLElement(myData, _myFile);
			}
		});
		
		_myApp.addMouseMotionListener(new CCMouseMotionListener() {
			
			@Override
			public void mouseMoved(CCMouseEvent theMouseEvent) {
				if(!_cIsInEditMode)return;
				_mySelectedLocation = null;
				for(ILContentLocation myLocation : _myContentLocations) {
					if(myLocation.location().distance(new CCVector2f(theMouseEvent.x(), _myApp.height - theMouseEvent.y())) < 10) {
						_mySelectedLocation = myLocation;
						break;
					}
				}
			}
			
			@Override
			public void mouseDragged(CCMouseEvent theEvent) {
				if(!_cIsInEditMode)return;
				if(!theEvent.isShiftDown())return;
				_myHasMoved = true;
				_mySelectedLocation.location().set(theEvent.x(), _myApp.height - theEvent.y());
			}
		});
		
		_myFile = theFile;
		
		CCXMLElement myData = CCXMLIO.createXMLElement(_myFile);
		if(myData != null) {
			for(CCXMLElement myLocationXML:myData) {
				_myContentLocations.add(new ILContentLocation(
					myLocationXML.child("x").floatContent(),
					myLocationXML.child("y").floatContent()
				));
			}
		}
	}
	
	public void reset() {
		for(ILContentLocation myLocation:_myContentLocations) {
			myLocation.isTaken(false);
		}
		_mySelectedLocation = null;
	}
	
	public ILContentLocation nearestFreeLocation(CCVector2f thePosition) {
		if(_cIsInEditMode)return null;
		float myMinDistance = Float.MAX_VALUE;
		ILContentLocation myResult = null;
		
		for(ILContentLocation myLocation:_myContentLocations) {
			if(myLocation.isTaken())continue;
			
			float myDistance = myLocation.location().distance(thePosition);
			
			if(myDistance < myMinDistance) {
				myMinDistance = myDistance;
				myResult = myLocation;
			}
		}
		
		return myResult;
	}
	
	public void draw(CCGraphics g) {
		if(!_cIsInEditMode)return;
		
		g.color(255);
		for(ILContentLocation myLocation:_myContentLocations) {
			if(myLocation.isTaken()) {
				g.color(0,0,255);
			}else {
				g.color(255);
			}
			g.ellipse(myLocation.location(), 20);
		}
		if(_mySelectedLocation != null) {
			g.color(255,0,0);
			g.ellipse(_mySelectedLocation.location(), 20);
		}
	}
	
}
