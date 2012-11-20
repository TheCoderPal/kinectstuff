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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.creativecomputing.CCApp;
import cc.creativecomputing.control.CCControl;
import cc.creativecomputing.cv.openni.util.CCOpenNISkeletonController;
import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.graphics.texture.CCTexture.CCTextureFilter;
import cc.creativecomputing.graphics.texture.CCTexture.CCTextureMipmapFilter;
import cc.creativecomputing.graphics.texture.CCTexture2D;
import cc.creativecomputing.graphics.texture.CCTextureIO;
import cc.creativecomputing.math.CCVecMath;
import cc.creativecomputing.math.CCVector2f;
import cc.creativecomputing.math.CCVector3f;
import cc.creativecomputing.math.CCVector4f;
import cc.creativecomputing.simulation.gpuparticles.CCGPUParticleGroup;
import cc.creativecomputing.simulation.gpuparticles.CCGPUQueueParticles;
import cc.creativecomputing.simulation.gpuparticles.constraints.CCGPUConstraint;
import cc.creativecomputing.simulation.gpuparticles.forces.CCGPUForce;
import cc.creativecomputing.simulation.gpuparticles.forces.CCGPUForceField;
import cc.creativecomputing.simulation.gpuparticles.forces.CCGPUGravity;
import cc.creativecomputing.simulation.gpuparticles.forces.CCGPUViscousDrag;
import cc.creativecomputing.simulation.gpuparticles.forces.blend.CCGPUTimeForceBlend;
import cc.creativecomputing.simulation.gpuparticles.forces.target.CCGPUTargetForce;
import cc.creativecomputing.simulation.gpuparticles.forces.target.CCGPUTargetPointSetSetup;
import cc.creativecomputing.simulation.gpuparticles.forces.target.CCGPUTargetSetup;
import cc.creativecomputing.simulation.gpuparticles.impulses.CCGPUImpulse;
import cc.creativecomputing.simulation.gpuparticles.impulses.CCGPUSphereImpulse;
import cc.creativecomputing.simulation.gpuparticles.render.CCGPUPointSpriteRenderer;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.ILContentLocations;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.ILContentLocations.ILContentLocation;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.diversity.ILBubble.ILBubbleListener;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.diversity.ILContentManager.ILContent;

/**
 * @author christianriekoff
 *
 */
public class ILBubbleManager implements ILBubbleListener{
	
	public static int MAX_BUBBLES = 10;
	
	public static int PARTICLE_GROUPSIZE = 60;
	
	private class ILSphereImpulseControls{
		@CCControl(name = "sphere impulse strength", min = 0, max = 10f)
		private float _cSphereImpulseStrength = 0;
		
		@CCControl(name = "sphere impulse radius", min = 0, max = 500)
		private float _cSphereImpulseRadius = 0;
	}
	
	private ILSphereImpulseControls _mySphereImpulseControls = new ILSphereImpulseControls();
	
	@CCControl(name = "point size", min = 0, max = 150)
	private int _cPointSize = 5;

	@CCControl(name = "noise strength", min = 0, max = 10)
	private float _cFieldStrength = 0;

	@CCControl(name = "noise scale", min = 0, max = 10)
	private float _cFieldScale = 0;

	@CCControl(name = "noise speed", min = 0, max = 10)
	private float _cFieldSpeed = 0;

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

	@CCControl(name = "target near distance", min = 0, max = 200)
	private float _cTargetNearDistance = 0;

	@CCControl(name = "target near max force", min = 0, max = 10)
	private float _cTargetNearMaxForce = 0;
	
	@CCControl(name = "app speed", min = 1, max = 5)
	private float _cAppSpeed = 1;
	
	@CCControl(name = "bubble depth", min = -10000, max = 1000)
	private float _cBubbleDepth = 0;
	
	@CCControl(name = "bubble radius", min = 0, max = 100)
	private float _cBubbleRadius = 0;
	
	@CCControl(name = "bubble scale", min = 0, max = 20)
	private float _cBubbleScale = 0;
	
	@CCControl(name = "open content", external = true)
	private boolean _cOpenContent = false;
	
	
	private CCGPUQueueParticles _myParticles;
	
	private CCGPUForceField _myForceField = new CCGPUForceField(0.005f, 1, new CCVector3f(100, 20, 30));
	private CCGPUGravity _myGravity = new CCGPUGravity(new CCVector3f(0, 10, 0));
	private CCGPUGravity _myGravity1 = new CCGPUGravity(new CCVector3f(0, 0, 0));
	private CCGPUTimeForceBlend _myTimeBlendForce;
	private CCGPUViscousDrag _myDrag;
	
	private CCGPUSphereImpulse _mySphereImpulse;

	private CCGPUTargetForce _myTargetForce = new CCGPUTargetForce(1);
	private CCTexture2D _myPointSpriteTexture;
	private CCGPUPointSpriteRenderer _myRenderer;
	
	private List<ILBubble> _myContentBubbles = new ArrayList<ILBubble>();
	private List<ILBubble> _myFreeBubbles = new ArrayList<ILBubble>();
	private List<ILBubble> _myBubbleList = new ArrayList<ILBubble>();
	private int _myContentBubbleIndex = 0;
	private int _myFreeBubbleIndex = 0;
	
	private Map<ILBubble, ILContentLocation> _myLocationMap = new HashMap<ILBubble, ILContentLocations.ILContentLocation>();
	
	
	private ILContentManager _myContentManager;
	
	private ILContentLocations _myContentLocations;
	
	private CCApp _myApp;
	private CCGraphics _myGraphics;
	
	public ILBubbleManager(CCApp theApp, ILContentManager theContentManager) {
		_myApp = theApp;
		_myGraphics = theApp.g;
		
		_myContentManager = theContentManager;
		_myContentLocations = new ILContentLocations(theApp, "data/diversity_locations.xml");
		_myApp.addControls("diversity ui", "locations", _myContentLocations);
		
		_myPointSpriteTexture = new CCTexture2D(CCTextureIO.newTextureData("spheres.png"));
		_myPointSpriteTexture.generateMipmaps(true);
		_myPointSpriteTexture.textureFilter(CCTextureFilter.LINEAR);
		_myPointSpriteTexture.textureMipmapFilter(CCTextureMipmapFilter.LINEAR);
		
		_myRenderer = new CCGPUPointSpriteRenderer(theApp.g,_myPointSpriteTexture,5, 1);
		_myRenderer.pointSize(3);
		_myRenderer.fadeOut(false);
		
//		_myRenderer = new CCGPUParticlePointRenderer();
		
		final List<CCGPUForce> myForces = new ArrayList<CCGPUForce>();
		myForces.add(_myDrag = new CCGPUViscousDrag(0.3f));
		myForces.add(_myForceField);
		myForces.add(_myGravity);
		
		_myTimeBlendForce = new CCGPUTimeForceBlend(0,40, _myGravity1, _myTargetForce);
		_myTimeBlendForce.blend(0, 1f);
		myForces.add(_myTimeBlendForce);

		final List<CCGPUConstraint> myConstraints = new ArrayList<CCGPUConstraint>();
//		myConstraints.add(_myFloorConstraint = new CCGPUFloorConstraint(-300, 0.3f, 1f, 0.01f));
		
		final List<CCGPUImpulse> myImpulses = new ArrayList<CCGPUImpulse>();
		myImpulses.add(_mySphereImpulse = new CCGPUSphereImpulse(new CCVector3f(), 200, 1));

		_myParticles = new CCGPUQueueParticles(theApp.g, _myRenderer, myForces, myConstraints, myImpulses,  400, 400);
		_myTargetForce.changeTexture(0);
		
		int myStartIndex = 0;
		for(ILContent myContent:_myContentManager.contentList()) {
			ILBubble myBubble = new ILBubble(new CCGPUParticleGroup(_myParticles, myStartIndex,myContent.numberOfParticles()),myContent);
			myBubble.events().add(this);
			myBubble.events().add(theContentManager);
			_myContentBubbles.add(myBubble);
			_myBubbleList.add(myBubble);
			myStartIndex += myContent.numberOfParticles();
		}
		
		for(int i = 0; i < 20;i++) {
			ILBubble myBubble = new ILBubble(new CCGPUParticleGroup(_myParticles, myStartIndex,2000));
			myBubble.events().add(this);
			_myFreeBubbles.add(myBubble);
			_myBubbleList.add(myBubble);
			myStartIndex += 2000;
		}

		theApp.addControls("diversity", "sphere impulse controls",4, _mySphereImpulseControls);
		theApp.addControls("diversity", "bubble manager", 5, this);
		theApp.addControls("diversity", "bubble", 4, ILBubble.class);
	}
	
	@Override
	public void onClose(ILBubble theBubble) {
		if(!theBubble.isContentBubble())return;
		removeTargets(theBubble);
		sendImpulse(theBubble.center(), 100);
	}
	
	@Override
	public void onClosed(ILBubble theBubble) {
		theBubble.reset();
		
		ILContentLocation myLocation = _myLocationMap.remove(theBubble);
		if(myLocation != null) {
			myLocation.isTaken(false);
		}
	}
	
	@Override
	public void onOpen(ILBubble theBubble) {
	}
	
	public void updateTargets(CCGPUTargetSetup theSetup, int theX, int theY, int theWidth, int theHeight) {
		_myTargetForce.updateSetup(0, theSetup, theX, theY, theWidth, theHeight);
	}
	
	public CCGPUQueueParticles particles() {
		return _myParticles;
	}
	
	private float _myTime = 0;
	
	public void removeTargets(ILBubble theBubble){
		CCGPUTargetPointSetSetup myTargetSetup = new CCGPUTargetPointSetSetup();
		myTargetSetup.points().add(new CCVector4f());
		_myTargetForce.updateSetup(0, myTargetSetup, theBubble.particleGroup());
	}
	
	public void sendImpulse(CCVector3f thePosition, float theRadius) {
		_mySphereImpulse.strength(_mySphereImpulseControls._cSphereImpulseStrength);
		_mySphereImpulse.center(thePosition);
		_mySphereImpulse.radius(_mySphereImpulseControls._cSphereImpulseRadius);
		// _mySphereImpulse.center(new CCVector3f(0,_mySphereImpulseControls._cSphereImpulseY,
		// _mySphereImpulseControls._cSphereImpulseZ));//CCMath.random(-700,700)));
		_mySphereImpulse.trigger();
	}
	
	public void update(final float theDeltaTime) {
		_myTime += theDeltaTime * _cFieldSpeed;
			
		_myDrag.strength(_cDragStrength);

		_myGravity.strength(_cGravityStrength);
		_myGravity.direction().set(_cGravityX, _cGravityY, _cGravityZ);
		
		_myTargetForce.strength(_cTargetStrength);
		_myTargetForce.maxForce(_cTargetMax);
		_myTargetForce.lookAhead(_cTargetLookAhead);
		_myTargetForce.nearDistance(_cTargetNearDistance);
		_myTargetForce.nearMaxForce(_cTargetNearMaxForce);
		_myTimeBlendForce.endTime(_cTargetBlendTime);

		_myForceField.strength(_cFieldStrength);
		_myForceField.noiseOffset(new CCVector3f(0, 0, _myTime));
		_myForceField.noiseScale(_cFieldScale * 0.01f);
		
		_myRenderer.pointSize(_cPointSize);
//		_myRenderer.fadeOut(false);
		_myParticles.update(theDeltaTime * _cAppSpeed);
		
		for(ILBubble myBubble:_myBubbleList) {
			myBubble.update(theDeltaTime);
		}
	}
	
	public void emitBubble(CCVector3f thePosition, CCVector3f theVelocity, CCVector3f the2DPosition) {
	
		
		ILContentLocation myLocation = _myContentLocations.nearestFreeLocation(new CCVector2f(the2DPosition.x, the2DPosition.y));
		
		CCGPUTargetPointSetSetup myTargetSetup = new CCGPUTargetPointSetSetup();
		ILBubble myBubble;
		if(myLocation == null || !_cOpenContent) {
			myBubble = _myFreeBubbles.get(_myFreeBubbleIndex);
			_myFreeBubbleIndex++;
			_myFreeBubbleIndex %= _myFreeBubbles.size();

			myTargetSetup.points().add(new CCVector4f());
		}else {
			myBubble = _myContentBubbles.get(_myContentBubbleIndex);
			_myContentBubbleIndex++;
			_myContentBubbleIndex %= _myContentBubbles.size();
			myLocation.isTaken(true);
			
			_myLocationMap.put(myBubble, myLocation);
			
			_myGraphics.pushMatrix();
			_myGraphics.scale(1f / CCOpenNISkeletonController._cSkeletonScale * 2);
			_myGraphics.camera().updateProjectionInfos();
			CCVector3f myScreenOrthogonal = _myGraphics.camera().screenOrthogonal(
				(myLocation.location().x - _myApp.width / 2) * _cBubbleScale + _myApp.width / 2,
				(myLocation.location().y - _myApp.height / 2) * _cBubbleScale + _myApp.height / 2
			).scale(_cBubbleDepth);
			_myGraphics.popMatrix();
	
			myBubble.center2D().set(
				myLocation.location().x - _myApp.width / 2,
				myLocation.location().y - _myApp.height / 2
			);
			myBubble.center().set(
				myLocation.location().x - _myApp.width / 2 + myScreenOrthogonal.x, 
				myLocation.location().y - _myApp.height / 2 + myScreenOrthogonal.y, 
				myScreenOrthogonal.z
			);
			myBubble.center().scale(CCOpenNISkeletonController._cSkeletonScale);
			
			float myRadius = _cBubbleRadius * myBubble.content().radiusScale();
			
			for(int i = 0;i < PARTICLE_GROUPSIZE * PARTICLE_GROUPSIZE;i++) {
				myTargetSetup.points().add(new CCVector4f(CCVecMath.random3f(myRadius).add(myBubble.center()),1f));
			}
			
			
		}
		_myTargetForce.updateSetup(0, myTargetSetup, myBubble.particleGroup());
		
		myBubble.emitParticles(thePosition, theVelocity);
		myBubble.open();
	}

	public void draw(CCGraphics g) {
		_myRenderer.fadeOut(false);
		_myParticles.draw();
//		g.pointSize(_cPointSize);
//		_myParticles.renderer().mesh().draw(g);
	}
	
	public void drawContent(CCGraphics g) {
		g.pushMatrix();
		g.translate( -g.width/2, -g.height/2);
		
//		for(ILInteractionPair myPair:_mySelectedPairs) {
//			
//			g.color(1f, _cRingAlpha);
//			g.beginShape(CCDrawMode.TRIANGLE_STRIP);
//			for(int i = 0; i < CCMath.saturate(myPair.selectProgress()) * 360; i++) {
//				float myAngle = CCMath.radians(i);
//				float x = CCMath.sin(myAngle);
//				float y = CCMath.cos(myAngle);
//				g.vertex(x * _cRingInnerRadius + myPair._myLocation.position2D().x, y * _cRingInnerRadius + myPair._myLocation.position2D().y);
//				g.vertex(x * _cRingOuterRadius + myPair._myLocation.position2D().x, y * _cRingOuterRadius + myPair._myLocation.position2D().y);
//			}
//			g.endShape();
//			
//			
//			g.pushMatrix();
//			g.color(1f);
//			g.translate(myPair._myLocation.position2D().x, myPair._myLocation.position2D().y + _cLocationNameY);
//			myPair._myLocation.textShadow().progress(myPair.selectProgress());
//			myPair._myLocation.textShadow().draw(g);
//			myPair._myLocation.text().progress(myPair.selectProgress());
//			myPair._myLocation.text().draw(g);
//			g.popMatrix();
//			myPair._myLocation.contentShadow().progress(myPair.selectProgress());
//			myPair._myLocation.contentShadow().draw(g);
//			myPair._myLocation.content().progress(myPair.selectProgress());
//			myPair._myLocation.content().draw(g);
//		}
		
		_myContentLocations.draw(g);
		g.popMatrix();
	}
}
