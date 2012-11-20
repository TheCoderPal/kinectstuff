package de.artcom.deutschebank.brandscape.infolandschaft.visuals.diversity.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.creativecomputing.CCApp;
import cc.creativecomputing.CCApplicationManager;
import cc.creativecomputing.control.CCControl;
import cc.creativecomputing.cv.openni.CCOpenNI;
import cc.creativecomputing.cv.openni.CCOpenNIUser;
import cc.creativecomputing.cv.openni.CCOpenNIUser.CCUserJointType;
import cc.creativecomputing.cv.openni.CCOpenNIUserGenerator;
import cc.creativecomputing.cv.openni.CCOpenNIUser.CCUserJoint;
import cc.creativecomputing.cv.openni.CCOpenNIUserGenerator.CCUserListener;
import cc.creativecomputing.graphics.CCAbstractGraphics.CCBlendMode;
import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.math.CCVector3f;
import cc.creativecomputing.math.util.CCArcball;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.diversity.demo.ILStepChecker.ILStepListener;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.diversity.demo.ILThrowChecker.ILThrowListener;

/**
 * 
 * @author christianriekoff
 * 
 */
public class ILUserStepDemo extends CCApp {
	private class CCUserParticle{
		private CCVector3f _myVelocity;
		private CCVector3f _myPosition;
		
		private float _myAge = 0;
		
		public CCUserParticle(CCVector3f theVelocity, CCVector3f thePosition) {
			_myVelocity = theVelocity;
			_myPosition = thePosition;
		}
		
		public void update(float theDeltaTime) {
			_myAge += theDeltaTime;
			_myPosition.add(_myVelocity.clone().scale(theDeltaTime));
			_myVelocity.subtract(new CCVector3f(0,_cGravity,0));
		}
		
		public void draw(CCGraphics g) {
			g.pointSize(20);
			g.point(_myPosition);
		}
	}
	
	@CCControl(name = "gravity", min = -100, max = 100)
	private static float _cGravity = 0;
	@CCControl(name = "init velocity scale", min = 0, max = 1000)
	private static float _cInitVelocityScale = 0;

	private CCArcball _myArcball;

	private CCOpenNI _myOpenNI;
	private CCOpenNIUserGenerator _myUserGenerator;
	

	private List<CCUserParticle> _myParticles = new ArrayList<CCUserParticle>();
	
	private Map<Integer, ILStepChecker> _myUserMap = new HashMap<Integer, ILStepChecker>();

	public void setup() {
		addControls("app", "app", this);
		addControls("app", "step", ILStepChecker.class);
		_myArcball = new CCArcball(this);

		_myOpenNI = new CCOpenNI(this);
		_myOpenNI.openFileRecording("kinect/throw.oni");

		// enable skeleton generation for all joints
		_myUserGenerator = _myOpenNI.createUserGenerator();
		_myUserGenerator.events().add(new CCUserListener() {
			
			@Override
			public void onNewUser(CCOpenNIUser theUser) {
				addThrowChecker(theUser);
			}
			
			@Override
			public void onLostUser(CCOpenNIUser theUser) {
				_myUserMap.remove(theUser.id());
			}
			
			@Override
			public void onExitUser(CCOpenNIUser theUser) {
				_myUserMap.remove(theUser.id());
			}
			
			@Override
			public void onEnterUser(CCOpenNIUser theUser) {
				addThrowChecker(theUser);
				
			}
		});
		_myOpenNI.start();
		g.strokeWeight(3);
//		g.perspective(95, width / (float) height, 10, 150000);
	}
	
	private void addThrowChecker(CCOpenNIUser theUser) {
		ILStepChecker myThrowChecker = new ILStepChecker(theUser, CCUserJointType.LEFT_FOOT);
		myThrowChecker.events().add(new ILStepListener() {
			
			@Override
			public void onStep(CCUserJoint theJoint) {
				_myParticles.add(new CCUserParticle(theJoint.velocity().clone().scale(_cInitVelocityScale), theJoint.position().clone()));
			}
		});
		_myUserMap.put(theUser.id(), myThrowChecker);
	}
	
	/* (non-Javadoc)
	 * @see cc.creativecomputing.CCApp#update(float)
	 */
	@Override
	public void update(float theDeltaTime) {
		
		for(int i = 0; i < _myParticles.size();) {
			CCUserParticle myUserParticle = _myParticles.get(i);
			if(myUserParticle._myAge > 1.0) {
				_myParticles.remove(i);
			}else {
				i++;
			}
			myUserParticle.update(theDeltaTime);
		}
		
		for(ILStepChecker myChecker:_myUserMap.values()) {
			myChecker.update(theDeltaTime);
		}
	}

	public void draw() {
		g.clear();

		g.pushMatrix();
		// set the scene pos
		_myArcball.draw(g);
		//
//		g.translate(0, 0, -1000); // set the rotation center of the scene 1000 infront of the camera
		g.pointSize(0.1f);
		g.color(255, 100, 50, 150);
		g.blend(CCBlendMode.ADD);
		g.noDepthTest();
//		_myRenderer.drawDepthMesh(g);
		
		g.color(255);
		g.strokeWeight(1);

		g.noDepthTest();
//		_myDepthGenerator.drawDepthMap(g);

		g.color(255);
		for (CCOpenNIUser myUser : _myUserGenerator.user()) {
			myUser.drawSkeleton(g);
//			myUser.drawOrientations(g, 50);
//			myUser.drawVelocities(g,500);
			myUser.drawAcceleration(g,50);
		}
		for(ILStepChecker myChecker:_myUserMap.values()) {
			myChecker.draw(g);
		}

		for(CCUserParticle myUserParticle:_myParticles) {
			myUserParticle.draw(g);
		}
		
		g.popMatrix();
		
		for(ILStepChecker myChecker:_myUserMap.values()) {
			myChecker.drawValues(g, width);
		}
		
		g.text(frameRate,0,0);
	}

	public static void main(String[] args) {
		CCApplicationManager myManager = new CCApplicationManager(ILUserStepDemo.class);
		myManager.settings().size(1200, 800);
		myManager.settings().antialiasing(8);
//		myManager.settings().frameRate(60);
		myManager.start();
	}
}
