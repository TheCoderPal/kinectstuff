package de.artcom.deutschebank.brandscape.infolandschaft.visuals.connectivity.demo;

import java.util.ArrayList;
import java.util.List;

import cc.creativecomputing.CCApp;
import cc.creativecomputing.CCApplicationManager;
import cc.creativecomputing.control.CCControl;
import cc.creativecomputing.graphics.CCAbstractGraphics.CCBlendMode;
import cc.creativecomputing.graphics.CCDrawMode;
import cc.creativecomputing.graphics.CCMesh;
import cc.creativecomputing.graphics.CCVBOMesh;
import cc.creativecomputing.graphics.primitives.CCSphereMesh;
import cc.creativecomputing.graphics.shader.imaging.CCGPUSeperateGaussianBlur;
import cc.creativecomputing.math.CCMath;
import cc.creativecomputing.math.CCVecMath;
import cc.creativecomputing.math.CCVector3f;
import cc.creativecomputing.math.util.CCArcball;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.connectivity.globe.ILFinancialStreams;

public class ILLinesBlurTest extends CCApp {
	
	public final static float MAXIMUM_BLUR_RADIUS = 150;
	
	@CCControl(name = "blur radius", min = 0, max = MAXIMUM_BLUR_RADIUS)
	private float _cBlurRadius = MAXIMUM_BLUR_RADIUS;
	
	private CCGPUSeperateGaussianBlur _myBlur;
	private CCArcball _myArcball;

	private CCSphereMesh _mySphereMesh;
	private ILFinancialStreams _myFinancialCentres;

	public void setup() {
		addControls("blur", "blur", this);

		_myBlur = new CCGPUSeperateGaussianBlur(20, width, height);
		
		_myArcball = new CCArcball(this);
		
		_mySphereMesh = new CCSphereMesh(200, 100);
		
		_myFinancialCentres = new ILFinancialStreams();
		addControls("app", "app", _myFinancialCentres);
	}
	float _myAngle = 0;
	float _myTime = 0;
	public void update(final float theTime){
		_myTime += theTime;
		_myBlur.radius(_cBlurRadius);
	}

	public void draw() {
		g.color(255);
		g.clear();
		
		_myBlur.beginDraw(g);
		_myArcball.draw(g);
		g.clear();
		
		g.blend();
		g.color(0);
//		_mySphereMesh.draw(g);
		
		_myFinancialCentres.drawStraight(g);
		g.color(255);
		_myBlur.endDraw(g);
		
		g.color(0);
		g.text(frameRate,0,0);
	}

	public static void main(String[] args) {
		CCApplicationManager myManager = new CCApplicationManager(ILLinesBlurTest.class);
		myManager.settings().size(1500, 900);
		myManager.settings().antialiasing(8);
		myManager.start();
	}
}
