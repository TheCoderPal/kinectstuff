package de.artcom.deutschebank.brandscape.infolandschaft.visuals.connectivity.demo;


import cc.creativecomputing.CCApp;
import cc.creativecomputing.CCApplicationManager;
import cc.creativecomputing.graphics.primitives.CCSphereMesh;
import cc.creativecomputing.math.util.CCArcball;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.connectivity.globe.ILFinancialStreams;

public class ILFinancialCentresGlobeDemo extends CCApp {
	
	private CCArcball _myArcball;
	private CCSphereMesh _mySphereMesh;
	private ILFinancialStreams _myFinancialCentres;

	@Override
	public void setup() {
		_myArcball = new CCArcball(this);
		
		_mySphereMesh = new CCSphereMesh(200, 100);
		
		_myFinancialCentres = new ILFinancialStreams();
		addControls("app", "app", _myFinancialCentres);
	}

	@Override
	public void update(final float theDeltaTime) {
		
	}

	@Override
	public void draw() {
		_myArcball.draw(g);
		g.clear();
		
		g.blend();
		g.color(0);
		g.ellipse(0,0,400);
		
		_myFinancialCentres.drawStraight(g);
	}

	public static void main(String[] args) {
		CCApplicationManager myManager = new CCApplicationManager(ILFinancialCentresGlobeDemo.class);
		myManager.settings().size(360 * 4, 180 * 4);
		myManager.settings().antialiasing(8);
		myManager.start();
	}
}
