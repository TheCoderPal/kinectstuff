package de.artcom.deutschebank.brandscape.infolandschaft.visuals.connectivity.demo;

import java.util.ArrayList;
import java.util.List;

import cc.creativecomputing.CCApp;
import cc.creativecomputing.CCApplicationManager;
import cc.creativecomputing.control.CCControl;
import cc.creativecomputing.graphics.CCAbstractGraphics.CCBlendMode;
import cc.creativecomputing.graphics.CCDrawMode;
import cc.creativecomputing.io.CCIOUtil;
import cc.creativecomputing.math.CCMath;
import cc.creativecomputing.math.CCVector3f;

public class ILFinancialCentresDemo extends CCApp {
	
	
	@CCControl(name = "color", min = 0, max = 1f)
	private float _cColor = 0;
	
	@CCControl(name = "radius", min = 0, max = 1f)
	private float _cRadius = 0;
	
	@CCControl(name = "pow", min = 0, max = 10f)
	private float _cPow = 0;
	
	private List<CCVector3f> _myLocationList = new ArrayList<CCVector3f>();
	private float _myMaxPop = 0;
	
	

	@Override
	public void setup() {
		float myMinLat = Float.MAX_VALUE;
		float myMaxLat = Float.MIN_VALUE;
		float myMinLon = Float.MAX_VALUE;
		float myMaxLon = Float.MIN_VALUE;
		
		float myMinPop = Float.MAX_VALUE;
		
		int myLocations = 0;
		for(String myEntry:CCIOUtil.loadStrings("financial_centres.txt")) {
			String[] myParts = myEntry.split("\t");
			float myPopulation = Float.parseFloat(myParts[1]);
			if(myPopulation == 0)continue;
			float myLatitude = Float.parseFloat(myParts[2]);
			float myLongitude = Float.parseFloat(myParts[3]);
			
			myMinLat = CCMath.min(myMinLat,myLatitude);
			myMaxLat = CCMath.max(myMaxLat,myLatitude);
			myMinLon = CCMath.min(myMinLon,myLongitude);
			myMaxLon = CCMath.max(myMaxLon,myLongitude);
			myMinPop = CCMath.min(myMinPop,myPopulation);
			_myMaxPop = CCMath.max(_myMaxPop,myPopulation);
			
			_myLocationList.add(new CCVector3f(myLongitude, myLatitude, myPopulation));
			myLocations++;
		}
		
		addControls("app", "app", this);
	}

	@Override
	public void update(final float theDeltaTime) {}

	@Override
	public void draw() {
		g.clear();
		g.blendMode(CCBlendMode.ADD);
		g.color(_cColor);
//		g.beginShape(CCDrawMode.POINTS);
//		for(CCVector3f myLoc:_myLocationList) {
//			if(myLoc.z < _cMinPop * _myMaxPop)continue;
//			g.vertex(myLoc.x * 4, myLoc.y * 4);
//		}
//		g.endShape();
		
		for(CCVector3f myLoc:_myLocationList) {
			float myRadius = myLoc.z  * _cRadius;
			g.ellipse(myLoc.x * 4, myLoc.y * 4, myRadius);
		}
		
		for(CCVector3f myLoc1:_myLocationList) {
			for(CCVector3f myLoc2:_myLocationList) {
				float myVal = CCMath.sqrt(myLoc1.z * myLoc2.z) / _myMaxPop;
				myVal = CCMath.pow(myVal,_cPow);
				g.color(myVal * _cColor);
				g.strokeWeight(myVal * 20);
				g.line(
					myLoc1.x * 4, myLoc1.y * 4,
					myLoc2.x * 4, myLoc2.y * 4
				);
			}
		}
	}

	public static void main(String[] args) {
		CCApplicationManager myManager = new CCApplicationManager(ILFinancialCentresDemo.class);
		myManager.settings().size(360 * 4, 180 * 4);
		myManager.settings().antialiasing(8);
		myManager.start();
	}
}
