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
package de.artcom.deutschebank.brandscape.infolandschaft.visuals.diversity;

import java.util.ArrayList;
import java.util.List;

import cc.creativecomputing.CCApp;
import cc.creativecomputing.control.CCControl;
import cc.creativecomputing.graphics.CCDrawMode;
import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.graphics.CCMesh;
import cc.creativecomputing.graphics.shader.CCShaderTexture;
import cc.creativecomputing.graphics.texture.CCTexture.CCTextureFilter;
import cc.creativecomputing.graphics.texture.CCTexture.CCTextureMipmapFilter;
import cc.creativecomputing.graphics.texture.CCTexture2D;
import cc.creativecomputing.graphics.texture.CCTextureIO;
import cc.creativecomputing.math.CCMath;
import cc.creativecomputing.math.CCVecMath;
import cc.creativecomputing.math.CCVector3f;
import cc.creativecomputing.math.CCVector4f;
import cc.creativecomputing.math.util.CCQuad3f;
import cc.creativecomputing.simulation.gpuparticles.CCGPUQueueParticles;
import cc.creativecomputing.simulation.gpuparticles.constraints.CCGPUConstraint;
import cc.creativecomputing.simulation.gpuparticles.constraints.CCGPUXConstraint;
import cc.creativecomputing.simulation.gpuparticles.constraints.CCGPUYConstraint;
import cc.creativecomputing.simulation.gpuparticles.constraints.CCGPUZConstraint;
import cc.creativecomputing.simulation.gpuparticles.forces.CCGPUForce;
import cc.creativecomputing.simulation.gpuparticles.forces.CCGPUForceField;
import cc.creativecomputing.simulation.gpuparticles.forces.CCGPUGravity;
import cc.creativecomputing.simulation.gpuparticles.forces.CCGPUViscousDrag;
import cc.creativecomputing.simulation.gpuparticles.forces.blend.CCGPUIDTextureBlendForce;
import cc.creativecomputing.simulation.gpuparticles.forces.target.CCGPUTargetForce;
import cc.creativecomputing.simulation.gpuparticles.forces.target.CCGPUTargetPointSetSetup;
import cc.creativecomputing.simulation.gpuparticles.impulses.CCGPUImpulse;
import cc.creativecomputing.simulation.gpuparticles.impulses.CCGPUSphereImpulse;
import cc.creativecomputing.simulation.gpuparticles.render.CCGPUPointSpriteRenderer;

/**
 * @author christianriekoff
 *
 */
public class ILSkeletonParticles {
	
	private class ILSphereImpulseControls{
		@CCControl(name = "impulse strength", min = 0, max = 10f)
		private float _cSphereImpulseStrength = 0;
		
		@CCControl(name = "impulse radius", min = 0, max = 500)
		private float _cSphereImpulseRadius = 0;
		
		@CCControl(name = "impulse timer", min = 0, max = 5)
		private float _cSphereImpulseTimer = 0;
		
		@CCControl(name = "draw debug")
		private boolean _cDrawDebug = true;
	}
	
	private ILSphereImpulseControls _mySphereImpulseControls = new ILSphereImpulseControls();
	
	private class ILTargetControls{
	
		@CCControl(name = "force", min = 0, max = 2)
		private float _cTarget = 0;
		
		@CCControl(name = "lookahead", min = 0, max = 10)
		private float _cTargetLookAhead = 0;
		
		@CCControl(name = "max force", min = 0, max = 10)
		private float _cTargetMaxForce = 0;
		
		@CCControl(name = "near distance", min = 0, max = 100)
		private float _cTargetNearDistance = 0;
		
		@CCControl(name = "near max force", min = 0, max = 10)
		private float _cTargetNearMaxForce = 0;
	}
	
	@CCControl(name = "target controls")
	private ILTargetControls _myTargetControls = new ILTargetControls();
	
	@CCControl(name = "gravity", min = -5, max = 5, external = true)
	private float _cGravity = 0;
	
	@CCControl(name = "noise force", min = 0, max = 2, external = true)
	private float _cNoise = 0;
	
	@CCControl(name = "noise speed", min = 0, max = 1)
	private float _cNoiseSpeed = 0;
	
	@CCControl(name = "noise scale", min = 0, max = 20)
	private float _cNoiseScale = 0;
	
	@CCControl(name = "point size", min = 0, max = 150)
	private int _cPointSize = 5;
	
	private class ILConstraintSettings{
	
		@CCControl(name = "floor y", min = -1000, max = 1000)
		private float _cFloorY = 0;
	
		@CCControl(name = "back z", min = -1000, max = 1000)
		private float _cBackZ = 0;
	
		@CCControl(name = "front z", min = -1000, max = 1000)
		private float _cFrontZ = 0;
	
		@CCControl(name = "left x", min = -1000, max = 1000)
		private float _cLeftX = 0;
	
		@CCControl(name = "right x", min = -1000, max = 1000)
		private float _cRightX = 0;
		
		@CCControl(name = "floor resilience", min = 0, max = 1)
		private float _cFloorResilience = 0;
		
		@CCControl(name = "floor friction", min = 0, max = 1)
		private float _cFloorFriction = 0;
		
		@CCControl(name = "min Velocity", min = 0, max = 1)
		private float _cFloorMinVel = 0;
	}
	
	private class ILQuadSetting{
		@CCControl(name = "left top x", min = -1000, max = 1000)
		private float _cLeftTopX = 0;
		@CCControl(name = "left top y", min = -1000, max = 1000)
		private float _cLeftTopY = 0;

		@CCControl(name = "left botom x", min = -1000, max = 1000)
		private float _cLeftBottomX = 0;
		@CCControl(name = "left bottom y", min = -1000, max = 1000)
		private float _cLeftBottomY = 0;

		@CCControl(name = "right top x", min = -1000, max = 1000)
		private float _cRightTopX = 0;
		@CCControl(name = "right top y", min = -1000, max = 1000)
		private float _cRightTopY = 0;

		@CCControl(name = "right bottom x", min = -1000, max = 1000)
		private float _cRightBottomX = 0;
		@CCControl(name = "right bottom y", min = -1000, max = 1000)
		private float _cRightBottomY = 0;
	}
	
	private ILConstraintSettings _myConstraintSettings = new ILConstraintSettings();
	
	@CCControl(name = "emits per second", min = 0, max = 2000, external = true)
	private float _cEmitPerSecond = 0;
	
	@CCControl(name = "emits height", min = -1000, max = 1000)
	private float _cEmitHeight = 0;

	private CCGPUQueueParticles _myParticles;
	private CCGPUForceField _myForceField = new CCGPUForceField(0.005f,1,new CCVector3f(100,20,30));
	private CCGPUTargetForce _myTargetForce = new CCGPUTargetForce();
	private CCGPUGravity _myGravity;

	private CCTexture2D _myPointSpriteTexture;
	private CCGPUPointSpriteRenderer _myRenderer;
	
	private CCGPUYConstraint _myFloorConstraint;
	private CCGPUZConstraint _myBackConstraint;
	private CCGPUZConstraint _myFrontConstraint;
	private CCGPUXConstraint _myLeftConstraint;
	private CCGPUXConstraint _myRightConstraint;
	
	private CCGPUSphereImpulse _mySphereImpulse;
	
	@CCControl(name = "quad setting")
	private ILQuadSetting _cQuadSettings = new ILQuadSetting();
	
	private CCQuad3f _myQuad3f;
	
	
	public ILSkeletonParticles(CCApp theApp, int theXRes, int theYRes) {
		_myPointSpriteTexture = new CCTexture2D(CCTextureIO.newTextureData("spheres.png"));
		_myPointSpriteTexture.generateMipmaps(true);
		_myPointSpriteTexture.textureFilter(CCTextureFilter.LINEAR);
		_myPointSpriteTexture.textureMipmapFilter(CCTextureMipmapFilter.LINEAR);
		
		_myRenderer = new CCGPUPointSpriteRenderer(theApp.g,_myPointSpriteTexture,5, 1);
		_myRenderer.pointSize(3);
		_myRenderer.fadeOut(false);
//		
//		_myRenderer = new CCGPUParticlePointRenderer();
		
		final List<CCGPUForce> myForces = new ArrayList<CCGPUForce>();
		myForces.add(new CCGPUViscousDrag(0.3f));
		myForces.add(_myGravity = new CCGPUGravity(new CCVector3f(0,-2,0)));
		myForces.add(_myForceField);
		myForces.add(_myTargetForce);
		
		final List<CCGPUConstraint> myConstraints = new ArrayList<CCGPUConstraint>();
		myConstraints.add(_myFloorConstraint = new CCGPUYConstraint(0, 0, 0, 0));
		myConstraints.add(_myBackConstraint = new CCGPUZConstraint(0, 0, 0, 0));
		myConstraints.add(_myFrontConstraint = new CCGPUZConstraint(0, 0, 0, 0));
		_myFrontConstraint.negate();
		myConstraints.add(_myLeftConstraint = new CCGPUXConstraint(0, 0, 0, 0));
		myConstraints.add(_myRightConstraint = new CCGPUXConstraint(0, 0, 0, 0));
		_myRightConstraint.negate();
		
		final List<CCGPUImpulse> myImpulses = new ArrayList<CCGPUImpulse>();
		myImpulses.add(_mySphereImpulse = new CCGPUSphereImpulse(new CCVector3f(), 200, 1));
		
		_myParticles = new CCGPUQueueParticles(theApp.g, _myRenderer, myForces, myConstraints, myImpulses, theXRes, theYRes);
		
		for(int i = 0; i < theXRes * theYRes * 0.9f; i++){
			_myParticles.allocateParticle(
				new CCVector3f(CCMath.random(-400,400), 0, CCMath.random(-400,-300)),
				CCVecMath.random3f(10),
				10, true
			);
		}
		
		theApp.addControls("diversity", "constraint controls",2, _myConstraintSettings);
		theApp.addControls("diversity", "sphere impulse controls",2, _mySphereImpulseControls);
		
		_myQuad3f = new CCQuad3f(
			new CCVector3f(_cQuadSettings._cLeftTopX, _cQuadSettings._cLeftTopY), 
			new CCVector3f(_cQuadSettings._cLeftBottomX, _cQuadSettings._cLeftBottomY),
			new CCVector3f(_cQuadSettings._cRightBottomX, _cQuadSettings._cRightBottomY),
			new CCVector3f(_cQuadSettings._cRightTopX, _cQuadSettings._cRightTopY)
		);
	}
	
	public void triggerImpulse(CCVector3f thePosition) {
		_mySphereImpulse.center(thePosition);
		_mySphereImpulse.strength(_mySphereImpulseControls._cSphereImpulseStrength);
		_mySphereImpulse.radius(_mySphereImpulseControls._cSphereImpulseRadius );
		_mySphereImpulse.trigger();
	}
	
	public void targets(CCShaderTexture theTargets) {
		_myTargetForce.addTargetSetup(theTargets);
	}
	
	public float floorY() {
		return _myConstraintSettings._cFloorY;
	}
	
	public float backZ() {
		return _myConstraintSettings._cBackZ;
	}
	
	public float pointSize() {
		return _cPointSize;
	}
	
	float _myTime = 0;
	
	public CCMesh mesh() {
		return _myParticles.renderer().mesh();
	}
	
	private float _myTriggerTimer = 0;
	private float _myLastEmits = 0;
	
	public void update(float theDeltaTime) {
		_myLastEmits += _cEmitPerSecond * theDeltaTime;
		int myEmits = (int)_myLastEmits;
		_myLastEmits -= myEmits;
		
		for(int i = 0; i < myEmits && _myParticles.freeParticles() > 0;i++) {
			_myParticles.allocateParticle(
				new CCVector3f(
					CCMath.random(_myConstraintSettings._cLeftX + 10,_myConstraintSettings._cRightX - 10), 
					_cEmitHeight, 
					(_myConstraintSettings._cFrontZ+_myConstraintSettings._cBackZ) / 2),//CCMath.random(_myConstraintSettings._cFrontZ,_myConstraintSettings._cBackZ)),
				CCVecMath.random3f(10),
				10, true
			);
		}
		
		_myForceField.strength(_cNoise);
		_myTargetForce.strength(_myTargetControls._cTarget);
		_myTargetForce.maxForce(_myTargetControls._cTargetMaxForce);
		_myTargetForce.lookAhead(_myTargetControls._cTargetLookAhead);
		_myTargetForce.nearDistance(_myTargetControls._cTargetNearDistance);
		_myTargetForce.nearMaxForce(_myTargetControls._cTargetNearMaxForce);
		
		_myFloorConstraint.resilience(_myConstraintSettings._cFloorResilience);
		_myFloorConstraint.friction(_myConstraintSettings._cFloorFriction);
		_myFloorConstraint.minimalVelocity(_myConstraintSettings._cFloorMinVel);
		_myFloorConstraint.y(_myConstraintSettings._cFloorY);
		
		_myBackConstraint.resilience(_myConstraintSettings._cFloorResilience);
		_myBackConstraint.friction(_myConstraintSettings._cFloorFriction);
		_myBackConstraint.minimalVelocity(_myConstraintSettings._cFloorMinVel);
		_myBackConstraint.z(_myConstraintSettings._cBackZ);
		
		_myFrontConstraint.resilience(_myConstraintSettings._cFloorResilience);
		_myFrontConstraint.friction(_myConstraintSettings._cFloorFriction);
		_myFrontConstraint.minimalVelocity(_myConstraintSettings._cFloorMinVel);
		_myFrontConstraint.z(_myConstraintSettings._cFrontZ);
		
		_myLeftConstraint.resilience(_myConstraintSettings._cFloorResilience);
		_myLeftConstraint.friction(_myConstraintSettings._cFloorFriction);
		_myLeftConstraint.minimalVelocity(_myConstraintSettings._cFloorMinVel);
		_myLeftConstraint.x(_myConstraintSettings._cLeftX);
		
		_myRightConstraint.resilience(_myConstraintSettings._cFloorResilience);
		_myRightConstraint.friction(_myConstraintSettings._cFloorFriction);
		_myRightConstraint.minimalVelocity(_myConstraintSettings._cFloorMinVel);
		_myRightConstraint.x(_myConstraintSettings._cRightX);
		
		_myTime +=theDeltaTime * _cNoiseSpeed;
		
		_myParticles.update(theDeltaTime);
		
		_myForceField.noiseOffset(new CCVector3f(0,0,_myTime));
		_myForceField.noiseScale(_cNoiseScale / 100f);
		_myGravity.strength(_cGravity);
		
		_myTriggerTimer += theDeltaTime;
		
		if(_myTriggerTimer > _mySphereImpulseControls._cSphereImpulseTimer) {
			_myTriggerTimer = 0;
//			_mySphereImpulse.center(
//				new CCVector3f(
//					CCMath.random(-_mySphereImpulseControls._cImpulseX, _mySphereImpulseControls._cImpulseX),
//					_mySphereImpulseControls._cSphereImpulseY, 
//					CCMath.random(_mySphereImpulseControls._cImpulseZ1, _mySphereImpulseControls._cImpulseZ2)
//				)
//			);
//			
//			_mySphereImpulse.strength(_mySphereImpulseControls._cSphereImpulseStrength);
//			_mySphereImpulse.radius(_mySphereImpulseControls._cSphereImpulseRadius);
//			_mySphereImpulse.trigger();
			
//			triggerImpulse(
//				new CCVector3f(
//					CCMath.random(-_mySphereImpulseControls._cImpulseX, _mySphereImpulseControls._cImpulseX),
//					_mySphereImpulseControls._cSphereImpulseY, 
//					CCMath.random(_mySphereImpulseControls._cImpulseZ1, _mySphereImpulseControls._cImpulseZ2)
//				)
//			);
		}
		

		_myRenderer.pointSize(_cPointSize);
		_myRenderer.fadeOut(false);
		
		float myZ = (_myConstraintSettings._cBackZ + _myConstraintSettings._cFrontZ) / 2;
//		_myQuad3f.leftBottom().set(_cQuadSettings._cLeftBottomX, _cQuadSettings._cLeftBottomY, myZ);
//		_myQuad3f.leftTop().set(_cQuadSettings._cLeftTopX, _cQuadSettings._cLeftTopY, myZ);
//			new CCVector3f(), 
//			new CCVector3f(),
//			new CCVector3f(_cQuadSettings._cRightBottomX, _cQuadSettings._cRightBottomY),
//			new CCVector3f(_cQuadSettings._cRightTopX, _cQuadSettings._cRightTopY)
//		);
	}
	
	public void draw(CCGraphics g) {
		_myParticles.draw();
//		g.pointSize(_cPointSize);
//		_myParticles.renderer().mesh().draw(g);
//		_myQu
		if(!_mySphereImpulseControls._cDrawDebug)return;
		g.ellipse(_mySphereImpulse.center(),_mySphereImpulseControls._cSphereImpulseRadius);
	}
	
	public void reset() {
		_myParticles.reset();
		for(int i = 0; i < _myParticles.size(); i++){
			_myParticles.allocateParticle(
				new CCVector3f(CCMath.random(-400,400), 0, CCMath.random(-400,-300)),
				CCVecMath.random3f(10),
				10, true
			);
		}
	}
}
