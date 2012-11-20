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
package de.artcom.deutschebank.brandscape.infolandschaft.visuals.connectivity.avatar;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import cc.creativecomputing.CCApp;
import cc.creativecomputing.control.CCControl;
import cc.creativecomputing.graphics.CCAbstractGraphics.CCBlendMode;
import cc.creativecomputing.graphics.CCColor;
import cc.creativecomputing.graphics.CCDrawMode;
import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.graphics.CCVBOMesh;
import cc.creativecomputing.graphics.shader.CCGLSLShader;
import cc.creativecomputing.graphics.texture.CCTexture2D;
import cc.creativecomputing.io.CCIOUtil;
import cc.creativecomputing.model.collada.CCColladaSkeletonMesh;
import cc.creativecomputing.model.skeleton.CCSkeleton;
import cc.creativecomputing.nio.CCFileChannel;
import cc.creativecomputing.nio.CCFileChannel.CCFileMode;

public class ILConnectivityAvatar {
	
	private class ILAvatarSettings{

		@CCControl(name = "trace duration", min = 0, max = 1)
		private float _cTraceDuration = 1;

		@CCControl(name = "trace fade", min = 0, max = 100)
		private float _cTraceFade = 1;

		@CCControl(name = "stroke weight", min = 0, max = 1)
		private float _cStrokeWeight = 1;
		
		@CCControl(name = "draw mesh")
		private boolean _cDrawMesh = false;
	}


	@CCControl(name = "trace head color")
	private CCColor _cTraceHeadColor = new CCColor();

	@CCControl(name = "trace slow color")
	private CCColor _cTraceSlowColor = new CCColor();

	@CCControl(name = "speed", min = 0, max = 1)
	public float _cSpeed = 0;

	private CCColladaSkeletonMesh _mySkeletonMesh;

	@CCControl(name = "time", min = 0, max = 2)
	private float _cTime = 0;

	private CCVBOMesh _myPathMesh;

	private CCGLSLShader _myPathWeightsShader;

	private ILAvatarRefractionShader _myRefractionShader;
	
	@CCControl(name = "straight settings")
	private ILAvatarSettings _cStraightSettings = new ILAvatarSettings();
	
	@CCControl(name = "blurred settings")
	private ILAvatarSettings _cBlurredSettings = new ILAvatarSettings();
	
	public ILConnectivityAvatar(CCApp theApp, CCTexture2D theBackground, CCColladaSkeletonMesh theSkeletonMesh) {
		_mySkeletonMesh = theSkeletonMesh;
		_myPathWeightsShader = new CCGLSLShader(
			CCIOUtil.classPath(this, "path_weights_vert.glsl"), 
			CCIOUtil.classPath(this, "path_weights_frag.glsl"));
		_myPathWeightsShader.load();

		_myPathMesh = buildMesh(10000, 30);
		
		_myRefractionShader = new ILAvatarRefractionShader(theApp.g, theBackground);

		theApp.addControls("connectivity", "avatar",3, this);
		theApp.addControls("connectivity", "avatar refraction",3, _myRefractionShader);
	}
	
	public ILConnectivityAvatar(CCApp theApp, CCTexture2D theBackground) {
		this(theApp, theBackground, new CCColladaSkeletonMesh("120411_humanoid_01_bakeTR.dae", "humanoid-lib", "bvh_import/Hips"));
	}
	
	public CCColladaSkeletonMesh skeletonMesh() {
		return _mySkeletonMesh;
	}

	public CCVBOMesh buildMesh(int thePaths, int thePathLength) {

		
		CCFileChannel myFileChannel = new CCFileChannel("data/avatar_curves.msh", CCFileMode.RW);
		int myNumberOfIndices = myFileChannel.readInt();

		IntBuffer myIndices = myFileChannel.readInts(myNumberOfIndices);

		int myNumberOfVertices = myFileChannel.readInt();
		FloatBuffer myPositions = myFileChannel.readFloats(myNumberOfVertices * 4);
		FloatBuffer myWeights = myFileChannel.readFloats(myNumberOfVertices * 4);
		FloatBuffer myWeightIndices = myFileChannel.readFloats(myNumberOfVertices * 4);
		
		myPositions.rewind();
		myIndices.rewind();
		myWeights.rewind();
		myWeightIndices.rewind();
		
		CCVBOMesh myMesh = new CCVBOMesh(CCDrawMode.LINES);
		myMesh.vertices(myPositions,4);
		myMesh.indices(myIndices);
		myMesh.textureCoords(1, myWeights, 4);
		myMesh.textureCoords(2, myWeightIndices, 4); 
		return myMesh;
	}

	private float _myTime = 0;

	public void update(float theDeltaTime) {
		_mySkeletonMesh.skeletonController().time(_cTime);
		_myTime += theDeltaTime * _cSpeed * 1f;
	}
	
	private float _myAlpha = 1f;
	
	public void alpha(float theAlpha){
		_myAlpha = theAlpha;
	}
	
	public void draw(CCGraphics g, CCSkeleton theSkeleton, ILAvatarSettings theSettings) {
		g.pushAttribute();
		if(theSettings._cDrawMesh) {
//			g.depthTest();
			g.blend(CCBlendMode.ADD);
			_myRefractionShader.start();
			_myRefractionShader.uniformMatrix4fv("joints", theSkeleton.skinningMatrices());
			_mySkeletonMesh.mesh().draw(g);
			_myRefractionShader.end();
		}
		
		g.noDepthTest();
		g.blend(CCBlendMode.ADD);
		g.strokeWeight(theSettings._cStrokeWeight * 20);
		_myPathWeightsShader.start();
		_myPathWeightsShader.uniformMatrix4fv("joints", theSkeleton.skinningMatrices());

		_myPathWeightsShader.uniform1f("currentTime", _myTime % 1);
		_myPathWeightsShader.uniform1f("positionPow", theSettings._cTraceFade);
		_myPathWeightsShader.uniform1f("positionRange", theSettings._cTraceDuration);

		_myPathWeightsShader.uniform4f(
			"positionMaxColor", 
			_cTraceHeadColor.r, 
			_cTraceHeadColor.g, 
			_cTraceHeadColor.b, 
			_cTraceHeadColor.a * _myAlpha
		);
		_myPathWeightsShader.uniform4f(
			"positionMinColor", 
			_cTraceSlowColor.r, 
			_cTraceSlowColor.g, 
			_cTraceSlowColor.b, 
			_cTraceSlowColor.a * _myAlpha
		);

		_myPathMesh.draw(g);
		_myPathWeightsShader.end();
		g.popAttribute();
	}
	
	public void drawBlurred(CCGraphics g, CCSkeleton theSkeleton) {
		draw(g, theSkeleton, _cBlurredSettings);
	}
	
	public void drawBlurred(CCGraphics g) {
		draw(g, _mySkeletonMesh.skeleton(), _cBlurredSettings);
	}

	public void drawStraight(CCGraphics g, CCSkeleton theSkeleton) {
		draw(g, theSkeleton, _cStraightSettings);
	}

	public void drawStraight(CCGraphics g) {
		draw(g, _mySkeletonMesh.skeleton(), _cStraightSettings);
	}
}