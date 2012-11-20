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
import java.util.Collections;
import java.util.List;

import cc.creativecomputing.CCApp;
import cc.creativecomputing.control.CCControl;
import cc.creativecomputing.events.CCListenerManager;
import cc.creativecomputing.events.CCMouseAdapter;
import cc.creativecomputing.events.CCMouseEvent;
import cc.creativecomputing.graphics.CCDrawMode;
import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.math.CCMath;
import cc.creativecomputing.math.CCVector2f;
import cc.creativecomputing.util.logging.CCLog;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.ILContentLocations;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.ILContentLocations.ILContentLocation;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.connectivity.globe.ILLocation;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.connectivity.globe.ILRefractedGlobe;

/**
 * @author christianriekoff
 *
 */
public class ILInteractionManager {

	public static class ILInteractionSource{
		
		protected CCVector2f _myPosition = null;
		
		public ILInteractionSource(CCVector2f thePosition){
			_myPosition = thePosition;
		}
		
		public CCVector2f position(){
			return _myPosition;
		}
		
	}
	
	public static class ILInteractionMouseSource extends ILInteractionSource{

		
		private CCApp _myApp;
		
		public ILInteractionMouseSource(CCApp theApp) {
			super(new CCVector2f());
			_myApp = theApp;
			theApp.addMouseListener(new CCMouseAdapter() {
				@Override
				public void mousePressed(CCMouseEvent theEvent) {
					_myPosition = new CCVector2f(theEvent.position().x, _myApp.height - theEvent.position().y);
				}
			});
			theApp.addMouseMotionListener(new CCMouseAdapter() {
				@Override
				public void mouseDragged(CCMouseEvent theEvent) {
					_myPosition.set(theEvent.position().x, _myApp.height - theEvent.position().y);
				}
			});
		}
		
	}
	
	@CCControl(name = "highlight radius", min = 0, max = 200)
	private float _cHighlightRadius = 0;
	
	@CCControl(name = "select radius", min = 0, max = 200)
	private float _cSelectRadius = 0;

	@CCControl(name = "location name y", min = -200, max = 200)
	private float _cLocationNameY = 0;
	
	@CCControl(name = "select time", min = 0, max = 5)
	private float _cSelectTime = 0;
	
	@CCControl(name = "open time", min = 0, max = 5)
	private float _cOpenTime = 0;
	
	@CCControl(name = "close time", min = 0, max = 60)
	private float _cCloseTime = 0;
	
	@CCControl(name = "touch blend weight", min = 0f, max = 1f)
	private float _cTouchBlendSmooth = 0;
	
	@CCControl(name = "ring alpha", min = 0f, max = 1f)
	private float _cRingAlpha = 0;
	
	@CCControl(name = "ring inner radius", min = 0f, max = 50f)
	private float _cRingInnerRadius = 0;
	
	@CCControl(name = "ring outer radius", min = 0f, max = 50f)
	private float _cRingOuterRadius = 0;
	
	@CCControl(name = "draw debug")
	private boolean _cDrawDebug = false;
	
	private CCApp _myApp;
	private ILRefractedGlobe _myGlobe;
	
	private List<ILInteractionSource> _mySources = new ArrayList<ILInteractionSource>();
	
	private ILContentLocations _myContentLocations;
	
	public ILInteractionManager(CCApp theApp, ILRefractedGlobe theGlobe) {
		_myApp = theApp;
		_myGlobe = theGlobe;
		_myContentLocations = new ILContentLocations(theApp, "connectivity_locations.xml");
		_myApp.addControls("connectivity ui", "locations", _myContentLocations);
		
		theApp.addControls("connectivity ui", "interaction", this);
		theApp.addControls("connectivity ui", "text", ILLocation.class);
	}
	
	public void addSource(ILInteractionSource theSource) {
		_mySources.add(theSource);
	}
	
	public void removeSource(ILInteractionSource theSource) {
		_mySources.remove(theSource);
		for(ILInteractionPair myPair:_mySelectedPairs) {
			if(myPair._mySource == theSource) {
				myPair._mySource = null;
			}
		}
	}
	
	public static interface ILInteractionListener{
		public void onSelect(ILInteractionPair thePair);
	}
	
	public class ILInteractionPair implements Comparable<ILInteractionPair>{
		private ILInteractionSource _mySource;
		private ILLocation _myLocation;
		private ILContentLocation _myContentLocation;
		
		private float _mySelectTime = 0;
		private float _myOpenTime = 0;
		
		private boolean _myIsSelected = false;
		
		private CCListenerManager<ILInteractionListener> _myEvents = CCListenerManager.create(ILInteractionListener.class);
		
		private boolean _myIsOver = false;
		
		private float _myLabelProgress = 0;
		
		private float _myTextProgress = 0;
		
		private float _myRingProgress = 0;
		
		public ILInteractionPair(ILInteractionSource theSource, ILLocation theLocation) {
			_mySource = theSource;
			_myLocation = theLocation;
		}
		
		public CCListenerManager<ILInteractionListener> events(){
			return _myEvents;
		}

		public float distance() {
			if(_mySource == null)return Float.MAX_VALUE;
			return _mySource.position().distance(_myLocation.position2D());
		}
		
		public void update(final float theDeltaTime) {
			_myIsOver = _mySource != null && distance() < _cSelectRadius;
			
			if(_myIsOver) {
				_mySelectTime += theDeltaTime;
			}else {
				_mySelectTime -= theDeltaTime;
			}
			
			_myLabelProgress = CCMath.saturate(_mySelectTime / _cSelectTime);
			_myRingProgress = _myLabelProgress;
			_myTextProgress = 0;
			_mySelectTime = CCMath.constrain(_mySelectTime,0,_cSelectTime);
			
			if(_mySelectTime >= _cSelectTime && !_myIsSelected) {
//				_myEvents.proxy().onSelect(this);
				
				_myContentLocation = _myContentLocations.nearestFreeLocation(new CCVector2f(_myLocation.position2D().x, _myLocation.position2D().y));
				if(_myContentLocation != null) {
					_myIsSelected = true;
					_myOpenTime = 0;
					_myContentLocation.isTaken(true);
					_myLocation.content().position(_myContentLocation.location());
					_myLocation.contentShadow().position(_myContentLocation.location());
				}
			}
			_myLocation.selectProgress(_mySelectTime / _cSelectTime);
			
			if(!_myIsSelected)return;
			
			_myOpenTime += theDeltaTime;
			
			_myLabelProgress = CCMath.saturate((_cCloseTime - _myOpenTime) / _cOpenTime);
			_myTextProgress = CCMath.min(_myLabelProgress, _myOpenTime / _cOpenTime);
			_myRingProgress = CCMath.saturate(1 - _myOpenTime / _cCloseTime);
			System.out.println();
			System.out.println(_myOpenTime +":" +(_cCloseTime - _myOpenTime));
			System.out.println(((_cCloseTime - _myOpenTime) / _cOpenTime)+":"+(_myOpenTime / _cOpenTime));
			
			if(_myOpenTime > _cCloseTime) {
				_myIsSelected = false;
				_myContentLocation.isTaken(false);
			}
		}
		
		public boolean isInSelection() {
			return _mySelectTime > 0 || _myIsSelected;
		}
		
		public float ringProgress() {
			return _myRingProgress;
		}
		
		public float labelProgress() {
			return _myLabelProgress;
		}
		
		public float textProgress() {
			return _myTextProgress;
		}
		
		public float selectProgress() {
			return _mySelectTime / _cSelectTime;
		}
		
		@Override
		public int compareTo(ILInteractionPair theArg0) {
			float difference =  theArg0.distance() - distance();
			return difference < 0 ? 1 : -1;
		}
	}
	
	private List<ILInteractionPair> _mySelectedPairs = new ArrayList<ILInteractionManager.ILInteractionPair>();
	
	public void update(final float theDeltaTime) {
		
		List<ILInteractionPair> myPairs = new ArrayList<ILInteractionManager.ILInteractionPair>();
		List<ILInteractionSource> mySources = new ArrayList<ILInteractionManager.ILInteractionSource>(_mySources);
		List<ILLocation> myLocations = new ArrayList<ILLocation>(_myGlobe.locations());
		
		for(int i = 0; i < _mySelectedPairs.size(); ) {
			ILInteractionPair myPair = _mySelectedPairs.get(i);
			myPair.update(theDeltaTime);
			
			if(!myPair.isInSelection()) {
				_mySelectedPairs.remove(i);
			}else {
				i++;
			}
		}
		
		for(ILInteractionPair mySelectedPair:_mySelectedPairs) {
			myPairs.add(mySelectedPair);
//			mySources.remove(mySelectedPair._mySource);
			myLocations.remove(mySelectedPair._myLocation);
		}
		
		for(ILInteractionSource mySource:mySources) {
			if(mySource.position() == null) {
				continue;
			}
			
			for(ILLocation myLocation:myLocations) {
				if(!myLocation.isSelectable()) {
					continue;
				}
				ILInteractionPair myPair = new ILInteractionPair(mySource, myLocation);
				if(myPair.distance() > _cHighlightRadius) {
					continue;
				}

				myPairs.add(myPair);
				
				if(myPair.isInSelection())continue;
				
				if(myPair.distance() <= _cSelectRadius) {
					_mySelectedPairs.add(myPair);
				}
			}
		}
		
		Collections.sort(myPairs);
		for(int i = 0; i < myPairs.size() - 1; i++) {
			ILInteractionPair myPair1 = myPairs.get(i);
			
			for(int j = i + 1; j < myPairs.size();) {
				ILInteractionPair myPair2 = myPairs.get(j);
				if(myPair1._myLocation == myPair2._myLocation) {
					myPairs.remove(j);
				}else {
					j++;
				}
			}
		}
		
		for(ILLocation myLocation:_myGlobe.locations()) {
			
			float myMinDistance = _cHighlightRadius;
			ILInteractionPair myNearestPair = null;
			for(ILInteractionPair myPair:myPairs) {
				if(myLocation != myPair._myLocation ) continue;
					
				float myDistance = myPair.distance();
				if(myDistance < myMinDistance) {
					myMinDistance = myDistance;
					myNearestPair = myPair;
				}
			}
			if(myNearestPair != null) {
				myLocation.touchBlend(1 - myNearestPair.distance() / _cHighlightRadius, _cTouchBlendSmooth);
			}else {
				myLocation.touchBlend(0, _cTouchBlendSmooth);
			}
			
			myLocation.update(theDeltaTime);
		}
	}
	
	private void drawDebug(CCGraphics g) {
		g.pushMatrix();
		g.translate( -_myApp.width/2, -_myApp.height/2);
		for(ILLocation myLocation:_myGlobe.locations()) {
			if(!myLocation.isSelectable())continue;

			g.color(1 - myLocation.touchBlend(), myLocation.touchBlend(),0);
			g.ellipse(myLocation.position2D().x, myLocation.position2D().y, 20);
		}

		g.color(0,255,0);
		for(ILInteractionSource mySource:_mySources) {
			if(mySource.position() == null)continue;
			g.ellipse(mySource.position().x, mySource.position().y, 20);
		}
		for(ILInteractionPair myPair:_mySelectedPairs) {

			g.color(0,0,1f);
			g.ellipse(myPair._myLocation.position2D().x, myPair._myLocation.position2D().y, 20);
			
			g.color(1f);
			g.beginShape(CCDrawMode.TRIANGLE_STRIP);
			for(int i = 0; i < myPair.selectProgress() * 360; i++) {
				float myAngle = CCMath.radians(i);
				float x = CCMath.sin(myAngle);
				float y = CCMath.cos(myAngle);
				g.vertex(x * 22 + myPair._myLocation.position2D().x, y * 22 + myPair._myLocation.position2D().y);
				g.vertex(x * 28 + myPair._myLocation.position2D().x, y * 28 + myPair._myLocation.position2D().y);
			}
			g.endShape();
			
			
			g.text(myPair._myLocation.name(), myPair._myLocation.position2D().x, myPair._myLocation.position2D().y + 20);
		}
		g.popMatrix();
	}
	
	public void drawBlurred(CCGraphics g) {
		if(_cDrawDebug)drawDebug(g);
		else {
			g.pushMatrix();
			g.translate( -_myApp.width/2, -_myApp.height/2);
			
			for(ILInteractionPair myPair:_mySelectedPairs) {
				
				g.color(1f, _cRingAlpha);
				g.beginShape(CCDrawMode.TRIANGLE_STRIP);
				for(int i = 0; i < CCMath.saturate(myPair.ringProgress()) * 360; i++) {
					float myAngle = CCMath.radians(i);
					float x = CCMath.sin(myAngle);
					float y = CCMath.cos(myAngle);
					g.vertex(x * _cRingInnerRadius + myPair._myLocation.position2D().x, y * _cRingInnerRadius + myPair._myLocation.position2D().y);
					g.vertex(x * _cRingOuterRadius + myPair._myLocation.position2D().x, y * _cRingOuterRadius + myPair._myLocation.position2D().y);
				}
				g.endShape();
			}
			g.popMatrix();
		}
	}
	
	public void draw(CCGraphics g) {
		g.pushMatrix();
		g.translate( -_myApp.width/2, -_myApp.height/2);
		
		for(ILInteractionPair myPair:_mySelectedPairs) {
			
			g.color(1f, _cRingAlpha);
			g.beginShape(CCDrawMode.TRIANGLE_STRIP);
			for(int i = 0; i < CCMath.saturate(myPair.ringProgress()) * 360; i++) {
				float myAngle = CCMath.radians(i);
				float x = CCMath.sin(myAngle);
				float y = CCMath.cos(myAngle);
				g.vertex(x * _cRingInnerRadius + myPair._myLocation.position2D().x, y * _cRingInnerRadius + myPair._myLocation.position2D().y);
				g.vertex(x * _cRingOuterRadius + myPair._myLocation.position2D().x, y * _cRingOuterRadius + myPair._myLocation.position2D().y);
			}
			g.endShape();
			
			
			g.pushMatrix();
			g.color(1f);
			g.translate(myPair._myLocation.position2D().x, myPair._myLocation.position2D().y + _cLocationNameY);
			myPair._myLocation.textShadow().progress(myPair.labelProgress());
			myPair._myLocation.textShadow().draw(g);
			myPair._myLocation.text().progress(myPair.labelProgress());
			myPair._myLocation.text().draw(g);
			g.popMatrix();
			myPair._myLocation.contentShadow().progress(myPair.textProgress());
			myPair._myLocation.contentShadow().draw(g);
			myPair._myLocation.content().progress(myPair.textProgress());
			myPair._myLocation.content().draw(g);
		}
		
		_myContentLocations.draw(g);
		
		for(ILInteractionSource mySource:_mySources){
			g.ellipse(mySource.position(), 20);
		}
		g.popMatrix();
	}
}
