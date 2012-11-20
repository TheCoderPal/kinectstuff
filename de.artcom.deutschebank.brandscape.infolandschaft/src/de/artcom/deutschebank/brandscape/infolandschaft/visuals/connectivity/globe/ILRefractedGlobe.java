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
package de.artcom.deutschebank.brandscape.infolandschaft.visuals.connectivity.globe;

import java.util.ArrayList;
import java.util.List;

import cc.creativecomputing.CCApp;
import cc.creativecomputing.control.CCControl;
import cc.creativecomputing.events.CCDrawListener;
import cc.creativecomputing.graphics.CCColor;
import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.graphics.primitives.CCSphereMesh;
import cc.creativecomputing.graphics.texture.CCTexture2D;
import cc.creativecomputing.math.CCMath;
import cc.creativecomputing.math.CCVector3f;

public class ILRefractedGlobe{
	
	@CCControl(name="location color")
	private CCColor _cLocationColor = new CCColor();
	@CCControl(name="location heightlight color")
	private CCColor _cLocationHighlightColor = new CCColor();
	@CCControl(name="location color pow", min = 0.1f, max = 10, external = true)
	private float _cLocationColorPow = 0;
	
	@CCControl(name = "location radius", min = 150, max = 250f, external = true)
	private float _cLocationRadius = 0;
	@CCControl(name="location scale", min = 0, max = 20, external = true)
	private float _cLocationScale = 0;
	@CCControl(name="location highlight scale", min = 0, max = 20, external = true)
	private float _cLocationHighlightScale = 0;
	@CCControl(name="location scale pow", min = 0.1f, max = 10, external = true)
	private float _cLocationScalePow = 0;
	
	@CCControl(name="x", min = -1000, max = 1000, external = true)
	private float _cX = 0;
	@CCControl(name="y", min = -1000, max = 1000, external = true)
	private float _cY = 0;
	
	@CCControl(name="x angle", min = -180, max = 180, external = true)
	private float _cXAngle = 0;
	@CCControl(name="z angle", min = -180, max = 180, external = true)
	private float _cZAngle = 0;
	
	@CCControl(name="scale", min = 0, max = 10, external = true)
	private float _cScale = 0;
	
	@CCControl(name="min depth", min = 0, max = 1)
	private float _cMinDepth = 0;
	@CCControl(name="max depth", min = 0, max = 1)
	private float _cMaxDepth = 0;
	
	private CCSphereMesh _mySphere;
	private CCSphereMesh _mySphere2;
	
	private List<CCDrawListener<CCGraphics>> _myLayer = new ArrayList<CCDrawListener<CCGraphics>>();

	private ILGlobeRefractionShader _myGlobeShader;
	private ILLocationRefractionShader _myLocationShader;
	

	private ILFinancialStreams _myFinancialStreams;
	private CCGraphics _myGraphics;
	
	private float _myAlpha = 1f;
	
	public ILRefractedGlobe(CCApp theApp, CCTexture2D theBackTexture, CCTexture2D theFrontTexture) {

		_mySphere = new CCSphereMesh(200, 100);
		_mySphere2 = new CCSphereMesh(1, 20);
		
		_myGlobeShader = new ILGlobeRefractionShader(theApp.g, theBackTexture, theFrontTexture);
		_myLocationShader = new ILLocationRefractionShader(theApp.g);
		
		theApp.addControls("connectivity", "globe", this);
		theApp.addControls("connectivity", "globe refraction",2, _myGlobeShader);
		theApp.addControls("connectivity", "location refraction",2, _myLocationShader);

		_myFinancialStreams = new ILFinancialStreams();
		theApp.addControls("connectivity", "streams", 1, _myFinancialStreams);
		
		_myGraphics = theApp.g;
	}
	
	public void alpha(float theAlpha){
		_myAlpha = theAlpha;
		_myGlobeShader.alpha(theAlpha);
		_myFinancialStreams.alpha(theAlpha);
	}
	
	public void addLayer(CCDrawListener<CCGraphics> theLayer) {
		_myLayer.add(theLayer);
	}
	
	public List<ILLocation> locations(){
		return _myFinancialStreams.locations();
	}
	
	public void update(float theDeltaTime) {
		_myGraphics.pushMatrix();
		_myGraphics.translate(_cX, _cY);
		_myGraphics.rotateX(_cXAngle);
		_myGraphics.rotateZ(_cZAngle);
		_myGraphics.scale(_cScale);
		
		_myGraphics.pushMatrix();
		_myGraphics.rotateX(180f);
		
		for(ILLocation myLocation:_myFinancialStreams.locations()) {
				
			CCVector3f myCenter = myLocation.position().clone().scale(_cLocationRadius);
				
			_myGraphics.pushMatrix();
			_myGraphics.translate(myCenter);
			_myGraphics.scale(_cLocationScale);
			_myGraphics.camera().updateProjectionInfos();
			CCVector3f my2DCoords = _myGraphics.camera().modelToScreen(new CCVector3f());
			myLocation.isSelectable(my2DCoords.z > _cMinDepth && my2DCoords.z < _cMaxDepth);
			myLocation.position2D().set(my2DCoords);
				
			_myGraphics.popMatrix();
		}
		_myGraphics.popMatrix();
		_myGraphics.popMatrix();
	}
	
	public void draw(CCGraphics g) {
		g.blend();
		
		g.pushMatrix();
		g.translate(_cX, _cY);
		g.rotateX(_cXAngle);
		g.rotateZ(_cZAngle);
		g.scale(_cScale);
		g.color(1f,_myAlpha);
		
		_myGlobeShader.start();
		_mySphere.draw(g);
		_myGlobeShader.end();
		
		g.pushMatrix();
		g.rotateX(180f);
		
		_myFinancialStreams.drawStraight(g);
		
		_myLocationShader.start();
		for(ILLocation myLocation:_myFinancialStreams.locations()) {
				
			CCVector3f myCenter = myLocation.position().clone().scale(_cLocationRadius);
				
			g.pushMatrix();
			g.translate(myCenter);
			float myScale = CCMath.blend(_cLocationScale, _cLocationHighlightScale, CCMath.pow(myLocation.touchBlend(), _cLocationScalePow));
			g.scale(myScale);
			CCColor myColor = CCColor.blend(_cLocationColor, _cLocationHighlightColor, CCMath.pow(myLocation.touchBlend(), _cLocationColorPow));
			myColor.a *= _myAlpha;
			g.color(myColor);
			
			_mySphere2.draw(g);
			g.popMatrix();
		}
		_myLocationShader.end();

		g.color(255);
		g.popMatrix();
		
		g.popMatrix();
	}
	
	public void drawStreams(CCGraphics g) {
		g.color(0);
		g.rect(-2000,-2000, 4000,4000);
		g.pushMatrix();
		g.translate(_cX, _cY);
		g.rotateX(_cXAngle);
		g.rotateZ(_cZAngle);
		g.scale(_cScale);
		g.color(0);
		
//		_mySphere.draw(g);
		
		g.pushMatrix();
		g.rotateX(180f);
		
		_myFinancialStreams.drawBlurred(g);

		g.color(255);
		g.popMatrix();
		
		g.popMatrix();
	}
}