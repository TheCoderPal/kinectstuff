package de.artcom.deutschebank.brandscape.infolandschaft.visuals;

import java.util.ArrayList;
import java.util.List;

import cc.creativecomputing.CCApp;
import cc.creativecomputing.CCApplicationManager;
import cc.creativecomputing.control.CCControl;
import cc.creativecomputing.cv.openni.CCOpenNI;
import cc.creativecomputing.cv.openni.CCOpenNIDepthGenerator;
import cc.creativecomputing.cv.openni.CCOpenNIImageGenerator;
import cc.creativecomputing.cv.openni.CCOpenNIUser;
import cc.creativecomputing.cv.openni.CCOpenNIUserGenerator;
import cc.creativecomputing.cv.openni.util.CCOpenNIFloorPlaneDetector;
import cc.creativecomputing.cv.openni.util.CCOpenNIInteractionArea;
import cc.creativecomputing.cv.openni.util.CCOpenNISkeletonController;
import cc.creativecomputing.cv.openni.util.CCOpenNIInteractionArea.CCOpenNIInteractionAreaListener;
import cc.creativecomputing.events.CCKeyEvent;
import cc.creativecomputing.graphics.CCDrawMode;
import cc.creativecomputing.graphics.export.CCScreenCapture;
import cc.creativecomputing.graphics.texture.CCTexture2D;
import cc.creativecomputing.graphics.texture.CCTextureIO;
import cc.creativecomputing.math.CCMath;
import cc.creativecomputing.math.CCVector3f;
import cc.creativecomputing.math.util.CCArcball;
import cc.creativecomputing.timeline.CCUITimelineConnector;
import cc.creativecomputing.timeline.view.swing.SwingTimelineContainer;
import cc.creativecomputing.util.logging.CCLog;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.connectivity.ILConnectivityTheme;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.diversity.ILDiversityTheme;

public class ILApp extends CCApp {
	
	public static final float SCALE = 0.5f;
	
	public static final int WIDTH = 1920;
	
	public static final int HEIGHT = 1080;
	
	@CCControl(name = "bar x", min = -500, max = 500)
	private float _cBarX = 0;
	@CCControl(name = "bar y", min = -500, max = 500)
	private float _cBarY = 0;
	@CCControl(name = "bar z", min = -5000, max = 1500)
	private float _cBarZ = 0;
	
	@CCControl(name = "bar scale x", min = 0.1f, max = 3)
	private float _cBarScaleX = 0;
	@CCControl(name = "bar scale y", min = 0.1f, max = 3)
	private float _cBarScaleY = 0;
	@CCControl(name = "bar alpha", min = 0f, max = 1f, external = true)
	private float _cBarAlpha = 0;
	
	@CCControl(name = "bar rotate", min = -180, max = 180)
	private float _cBarRotate = 0;
	
	@CCControl(name = "mask", min = 0, max = 100)
	private float _cMask = 0;
	
	private class ILOpenNIControls{
	
		@CCControl(name = "openNI rotate X", min = -180, max = 180)
		private float _cRotateX = 0;
	
		@CCControl(name = "openNI translate X", min = -5000f, max = 5000f)
		private float _cTranslateX = 0;
	
		@CCControl(name = "openNI translate Y", min = -5000f, max = 5000f)
		private float _cTranslateY = 0;
	
		@CCControl(name = "openNI translate Z", min = -5000f, max = 5000f)
		private float _cTranslateZ = 0;
	
		@CCControl(name = "openNI scale", min = 0.1f, max = 1f)
		private float _cScale = 0;
		
		@CCControl(name = "draw openni")
		private boolean _cDrawOpenNI = false;
		
		@CCControl(name = "draw textures")
		private boolean _cDrawTextures = false;
	}
	
	private ILOpenNIControls _myOpenNIControls;
	
	private CCArcball _myArcball;
	
	private CCTexture2D _myBarTexture;
	
	private List<ILTheme> _myThemes = new ArrayList<ILTheme>();
	
	private CCUITimelineConnector _myTimelineConnection;
	private SwingTimelineContainer _myTimeline;
	
	private CCOpenNI _myOpenNI;
	private CCOpenNIDepthGenerator _myDepthGenerator;
	private CCOpenNIImageGenerator _myImageGenerator;
	private CCOpenNIUserGenerator _myUserGenerator;
	private CCOpenNIFloorPlaneDetector _myFloorPlaneDetector;
	private CCOpenNIInteractionArea _myInteractionArea;
 
	@Override
	public void setup() {
//		_myTimeline = new SwingTimelineContainer();
//		_myTimelineConnection = new CCUITimelineConnector(this, _myTimeline);
//		_myTimeline.setSize(1400, 300);
		
		_myBarTexture = new CCTexture2D(CCTextureIO.newTextureData("120522_diversity_balken.png"));
		
		g.reportError(false);
		
		_myOpenNI = new CCOpenNI(this);
//		_myOpenNI.mirror(true);
//		_myOpenNI.openFileRecording("brandspace_record1.oni");
//		_myOpenNI.openFileRecording("kinect/kinect_00.oni");
		_myOpenNI.openFileRecording("SkeletonRec.oni");
		_myDepthGenerator = _myOpenNI.depthGenerator();
		_myOpenNI.imageGenerator();
		_myUserGenerator = _myOpenNI.createUserGenerator();
		_myOpenNI.start();

		addControls("openni","transform", 0,  _myOpenNIControls = new ILOpenNIControls());
		addControls("openni", "filter",2, _myUserGenerator.filter());
		addControls("openni", "skeleton", CCOpenNISkeletonController.class);
		
		_myInteractionArea = new CCOpenNIInteractionArea(_myUserGenerator);
		_myInteractionArea.events().add(new CCOpenNIInteractionAreaListener() {
			
			@Override
			public void onLeave(CCOpenNIUser theUser) {
				CCLog.info("LEAVE");
			}
			
			@Override
			public void onEnter(CCOpenNIUser theUser) {
				CCLog.info("ENTER");
			}
		});
		addControls("openni", "area",1, _myInteractionArea);
		
		_myFloorPlaneDetector = new CCOpenNIFloorPlaneDetector(_myOpenNI);
		addControls("openni", "floorplane", 3,_myFloorPlaneDetector);

		addTheme(new ILDiversityTheme(this,_myInteractionArea));
		addTheme(new ILConnectivityTheme(this,_myInteractionArea));
		
		
		_myArcball = new CCArcball(this);
		fixUpdateTime(1/30f);
	}
	
	public void addTheme(ILTheme theTheme) {
		_myThemes.add(theTheme);
	}

	@Override
	public void update(final float theDeltaTime) {
		_myOpenNI.transformationMatrix().reset();
		_myOpenNI.transformationMatrix().translate(
			_myOpenNIControls._cTranslateX,
			_myOpenNIControls._cTranslateY, 
			_myOpenNIControls._cTranslateZ
		);
		_myOpenNI.transformationMatrix().rotateX(CCMath.radians(_myOpenNIControls._cRotateX));
		_myOpenNI.transformationMatrix().scale(_myOpenNIControls._cScale);
		
		_myInteractionArea.update(theDeltaTime);

//		_myTimeline.update(theDeltaTime);
		for(ILTheme myTheme:_myThemes) {
			myTheme.update(theDeltaTime);
		}
	}
	
	private void drawOpenNI() {
		g.clear();
		g.pushMatrix();
		_myArcball.draw(g);
				
		g.scale(0.1f);
		
		CCVector3f[] myPoints = _myDepthGenerator.depthMapRealWorld(4);
		g.color(255);
		g.beginShape(CCDrawMode.POINTS);
		for(CCVector3f myPoint:myPoints) {
			g.vertex(myPoint);
		}
		g.endShape();
			
		g.color(255,0,0);
		_myInteractionArea.draw(g);
		_myFloorPlaneDetector.draw(g);
			
		for (CCOpenNIUser myUser : _myUserGenerator.user()) {
			g.color(255,0,0);
			myUser.drawDirectSkeleton(g);
			g.color(1f, 1f);
			myUser.drawSkeleton(g);
			g.color(0f,1f,0,1f);
			myUser.boundingBox().draw(g);
		}
			
		_myOpenNI.drawCamFrustum(g);
		
		g.popMatrix();
		
		if(_myOpenNIControls._cDrawTextures) {
			g.color(255);
			g.image(_myDepthGenerator.texture(), -_myDepthGenerator.width(), -_myDepthGenerator.height() / 2);
			g.image(_myImageGenerator.texture(), 0, -_myImageGenerator.height() / 2);
		}
	}

	@Override
	public void draw() {
		if(_myOpenNIControls._cDrawOpenNI) {
			drawOpenNI();
			return;
		}
		g.clearColor(0);
		g.clear();
	
		for(ILTheme myTheme:_myThemes) {
			myTheme.draw(g);
		}
		
		g.clearDepthBuffer();
		g.depthTest();
		g.pushMatrix();
		g.translate(_cBarX, _cBarY, _cBarZ);
		g.scale(_cBarScaleX, _cBarScaleY);
		g.rotate(_cBarRotate);
		g.color(1f, _cBarAlpha);
		g.image(_myBarTexture, -width/2, -height/2, width, height);
		g.popMatrix();
//		_myMouseSource.drawHandSpace(g);
		for(ILTheme myTheme:_myThemes) {
			myTheme.drawContent(g);
		}
		
		g.clearDepthBuffer();
		g.color(0);
		g.rect(-width/2, height/2 - _cMask, width,_cMask);
	}
	
	@Override
	public void keyPressed(CCKeyEvent theKeyEvent) {
		switch(theKeyEvent.keyCode()) {
		case CCKeyEvent.VK_A:
			_myArcball.reset();
			break;
		case CCKeyEvent.VK_S:
			CCScreenCapture.capture("export/diversity/"+frameCount+".png", width, height);
			break;
		}
	}

	public static void main(String[] args) {
		
		CCApplicationManager myManager = new CCApplicationManager(ILApp.class);
		myManager.settings().size(1600, 1000);
		myManager.settings().location(0, 0);
		// myManager.settings().undecorated(true);
		// myManager.settings().display(1);
		// myManager.settings().uiTranslation(120, 130);
		// myManager.settings().displayMode(CCDisplayMode.FULLSCREEN);
		myManager.settings().antialiasing(8);
		myManager.start();
		
//		CCApplicationManager myManager = new CCApplicationManager(ILApp.class);
//		myManager.settings().size(1920, 1080);
//		myManager.settings().undecorated(true);
//		myManager.settings().location(0,0);
//		myManager.settings().antialiasing(8);
//		myManager.settings().uiTranslation(0, 40);
////		myManager.settings().display(1);
//		myManager.start();
	}
}
