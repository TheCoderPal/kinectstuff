package de.artcom.deutschebank.brandscape.infolandschaft.visuals;

import java.util.ArrayList;
import java.util.List;

import cc.creativecomputing.CCApp;
import cc.creativecomputing.CCApplicationManager;
import cc.creativecomputing.control.CCControl;
import cc.creativecomputing.cv.openni.CCOpenNI;
import cc.creativecomputing.cv.openni.CCOpenNIUser;
import cc.creativecomputing.cv.openni.CCOpenNIUser.CCUserJoint;
import cc.creativecomputing.cv.openni.CCOpenNIUser.CCUserJointType;
import cc.creativecomputing.cv.openni.CCOpenNIUser.CCUserLimb;
import cc.creativecomputing.cv.openni.CCOpenNIUserGenerator;
import cc.creativecomputing.graphics.CCDrawMode;
import cc.creativecomputing.math.CCMath;
import cc.creativecomputing.math.CCVecMath;
import cc.creativecomputing.math.CCVector3f;
import cc.creativecomputing.math.util.CCArcball;
import cc.creativecomputing.math.util.CCQuad3f;
import cc.creativecomputing.simulation.gpuparticles.CCGPUQueueParticles;
import cc.creativecomputing.simulation.gpuparticles.constraints.CCGPUConstraint;
import cc.creativecomputing.simulation.gpuparticles.constraints.CCGPUYConstraint;
import cc.creativecomputing.simulation.gpuparticles.forces.CCGPUForce;
import cc.creativecomputing.simulation.gpuparticles.forces.CCGPUForceField;
import cc.creativecomputing.simulation.gpuparticles.forces.CCGPUGravity;
import cc.creativecomputing.simulation.gpuparticles.forces.CCGPUViscousDrag;
import cc.creativecomputing.simulation.gpuparticles.forces.blend.CCGPUTimeForceBlend;
import cc.creativecomputing.simulation.gpuparticles.forces.target.CCGPUTargetForce;
import cc.creativecomputing.simulation.gpuparticles.forces.target.CCGPUTargetQuadSetup;
import cc.creativecomputing.simulation.gpuparticles.forces.target.CCGPUTargetQuadSetup.CCGPUTargetQuadCreationMode;

public class CCParticleUserEmitTargetDemo extends CCApp {
	
	@CCControl(name = "emit amount", min = 0, max = 1000)
	private float _cEmitAmount = 0;
	
	@CCControl(name = "life time", min = 0, max = 100)
	private float _cLifeTime = 0;
	
	@CCControl(name = "random velocity scale", min = 0, max = 1000)
	private float _cRandomScale = 0;
	
	@CCControl(name = "user velocity scale", min = 0, max = 1)
	private float _cVelocityScale = 0;

	@CCControl(name = "noise strength", min = 0, max = 10)
	private float _cFieldStrength = 0;

	@CCControl(name = "drag strength", min = 0, max = 1)
	private float _cDragStrength = 0;

	@CCControl(name = "gravity strength", min = 0, max = 1)
	private float _cGravityStrength = 0;

	@CCControl(name = "gravity x", min = -10, max = 10)
	private float _cGravityX = 0;

	@CCControl(name = "gravity y", min = -10, max = 10)
	private float _cGravityY = 0;

	@CCControl(name = "gravity z", min = -10, max = 10)
	private float _cGravityZ = 0;

	@CCControl(name = "target strength", min = -0, max = 10)
	private float _cTargetStrength = 0;

	@CCControl(name = "target max", min = 0, max = 100)
	private float _cTargetMax = 0;

	@CCControl(name = "target look Ahead", min = 0, max = 100)
	private float _cTargetLookAhead = 0;

	@CCControl(name = "target blend time", min = 0, max = 10)
	private float _cTargetBlendTime = 0;

	@CCControl(name = "floor Y", min = -300, max = 300)
	private float _cFloorY = 0;

	@CCControl(name = "resilience", min = 0, max = 1f)
	private float _cResilience = 1f;

	@CCControl(name = "user scale", min = 0, max = 2)
	private float _cUserScale = 0;

	@CCControl(name = "user translate", min = -5000, max = 5000)
	private float _cUserTranslate = 0;
	
	@CCControl(name = "app speed", min = 1, max = 5)
	private float _cAppSpeed = 1;
	
	@CCControl(name = "max emit speed", min = 0, max = 10)
	private float _cMaxEmitSpeed = 1;
	
	@CCControl(name = "min emit speed", min = 0, max = 10)
	private float _cMinEmitSpeed = 1;

	private CCGPUQueueParticles _myParticles;
	private CCArcball _myArcball;

	// private CCGPUCurveField _myCurveField = new CCGPUCurveField(1.0f, 1.0f);
	private CCGPUForceField _myForceField = new CCGPUForceField(0.005f, 1, new CCVector3f(100, 20, 30));
	private CCGPUGravity _myGravity = new CCGPUGravity(new CCVector3f(0, 10, 0));
	private CCGPUGravity _myGravity1 = new CCGPUGravity(new CCVector3f(0, 0, 0));
	private CCGPUTimeForceBlend _myTimeBlendForce;
	private CCGPUViscousDrag _myDrag;

	private CCGPUYConstraint _myFloorConstraint;

	private CCOpenNI _myOpenNI;
	private CCOpenNIUserGenerator _myUserGenerator;
	
	private CCGPUTargetForce _myTargetForce = new CCGPUTargetForce();

	public void setup() {
		_myArcball = new CCArcball(this);

		final List<CCGPUForce> myForces = new ArrayList<CCGPUForce>();
		myForces.add(_myDrag = new CCGPUViscousDrag(0.3f));
		myForces.add(_myForceField);
		myForces.add(_myGravity);
		
		_myTimeBlendForce = new CCGPUTimeForceBlend(0,4, _myGravity1, _myTargetForce);
		_myTimeBlendForce.blend(0, 1f);
		myForces.add(_myTimeBlendForce);

		final List<CCGPUConstraint> myConstraints = new ArrayList<CCGPUConstraint>();
		myConstraints.add(_myFloorConstraint = new CCGPUYConstraint(-300, 0.3f, 1f, 0.01f));

		_myParticles = new CCGPUQueueParticles(g, myForces, myConstraints, 700, 700);
		CCQuad3f myQuad = new CCQuad3f(new CCVector3f(-250,-250,0),new CCVector3f( 250,-250,0),new CCVector3f( 250, 250,0),new CCVector3f(-250, 250,0));
		CCGPUTargetQuadSetup myTargetSetup = new CCGPUTargetQuadSetup(myQuad, CCGPUTargetQuadCreationMode.RANDOM);
		_myTargetForce.addTargetSetup(myTargetSetup);
		
		addControls("app", "app", this);

		_myOpenNI = new CCOpenNI(this);
		_myOpenNI.openFileRecording("SkeletonRec.oni");

		// enable skeleton generation for all joints
		_myUserGenerator = _myOpenNI.createUserGenerator();
		_myOpenNI.start();
		g.strokeWeight(3);
	}

	private float _myTime = 0;

	private CCVector3f _myLast1;
	private CCVector3f _myLast2;

	private CCVector3f _myLastVel1;
	private CCVector3f _myLastVel2;
	
	private void emitParticle(CCUserJoint theJoint, CCVector3f theLastPos, CCVector3f theLastVel) {
		float blend = CCMath.random();
		CCVector3f myParticlePos = CCVecMath.blend(blend, theJoint.position(), theLastPos);
		CCVector3f myParticleVel = CCVecMath.blend(blend, theJoint.velocity(), theLastVel);
		
		if(myParticleVel.length() > _cMinEmitSpeed) {
			myParticleVel.truncate(_cMaxEmitSpeed);
			myParticleVel.scale(500 * _cVelocityScale);
			myParticleVel.add(CCVecMath.random3f(CCMath.random(_cRandomScale)));
			
			_myParticles.allocateParticle(myParticlePos, myParticleVel, _cLifeTime, false);
		}
	}

	public void update(final float theDeltaTime) {
		_myTime += 1 / 30f * 0.5f;
		if (_myUserGenerator.user().size() > 0) {
			
			CCOpenNIUser myUser = _myUserGenerator.user().iterator().next();
			CCUserJoint myLeftHandJoint = myUser.joint(CCUserJointType.LEFT_HAND);
			CCUserJoint myRightHandJoint = myUser.joint(CCUserJointType.RIGHT_HAND);
			
			if(_myLast1 != null) {
				for (int i = 0; i < _cEmitAmount; i++) {
					emitParticle(myLeftHandJoint, _myLast1, _myLastVel1);
					emitParticle(myRightHandJoint, _myLast2, _myLastVel2);
				}
			}

			_myLast1 = myLeftHandJoint.position();
			_myLast2 = myRightHandJoint.position();
			
			_myLastVel1 = myLeftHandJoint.velocity();
			_myLastVel2 = myRightHandJoint.velocity();
		}else {
			_myLast1 = null;
			_myLast2 = null;
		}

		_myParticles.update(theDeltaTime * _cAppSpeed);

		_myDrag.strength(_cDragStrength);

		_myGravity.strength(_cGravityStrength);
		_myGravity.direction().set(_cGravityX, _cGravityY, _cGravityZ);
		
		_myTargetForce.strength(_cTargetStrength);
		_myTargetForce.maxForce(_cTargetMax);
		_myTargetForce.lookAhead(_cTargetLookAhead);
		_myTimeBlendForce.endTime(_cTargetBlendTime);

		_myForceField.strength(_cFieldStrength);
		_myForceField.noiseOffset(new CCVector3f(0, 0, _myTime));
		_myForceField.noiseScale(0.0025f);

		_myFloorConstraint.y(_cFloorY);
		_myFloorConstraint.resilience(_cResilience);
	}

	public void draw() {
		g.clear();
		g.noDepthTest();
		g.pushMatrix();
		_myArcball.draw(g);
		g.color(255, 50);
		g.blend();
		// g.pointSprite(_mySpriteTexture);
		// g.smooth();
		g.blend();
		_myParticles.renderer().mesh().draw(g);

		g.color(255, 0, 0);
		for (CCOpenNIUser myUser : _myUserGenerator.user()) {
			g.beginShape(CCDrawMode.LINES);
			for (CCUserLimb myLimb : myUser.limbs()) {
				g.vertex(myLimb.joint1().position().clone().scale(_cUserScale).add(0, 0, _cUserTranslate));
				g.vertex(myLimb.joint2().position().clone().scale(_cUserScale).add(0, 0, _cUserTranslate));
			}
			g.endShape();
		}

		g.popMatrix();
		g.color(255);
		g.text(frameRate + ":" + _myParticles.particlesInUse(), -width / 2 + 20, -height / 2 + 20);
	}

	public static void main(String[] args) {
		CCApplicationManager myManager = new CCApplicationManager(CCParticleUserEmitTargetDemo.class);
		myManager.settings().size(1200, 600);
		myManager.settings().antialiasing(8);
		myManager.start();
	}
}
