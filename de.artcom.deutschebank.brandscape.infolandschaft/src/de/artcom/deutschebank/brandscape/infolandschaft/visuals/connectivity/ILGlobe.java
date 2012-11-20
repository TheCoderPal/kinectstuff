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
package de.artcom.deutschebank.brandscape.infolandschaft.visuals.connectivity;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import cc.creativecomputing.control.CCControl;
import cc.creativecomputing.events.CCDrawListener;
import cc.creativecomputing.graphics.CCColor;
import cc.creativecomputing.graphics.CCDrawMode;
import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.graphics.CCGraphics.CCCullFace;
import cc.creativecomputing.graphics.CCRenderTexture;
import cc.creativecomputing.graphics.CCVBOMesh;
import cc.creativecomputing.graphics.primitives.CCSphereMesh;
import cc.creativecomputing.graphics.shader.CCGLSLShader;
import cc.creativecomputing.graphics.texture.CCFrameBufferObjectAttributes;
import cc.creativecomputing.graphics.texture.CCTexture.CCTextureFilter;
import cc.creativecomputing.graphics.util.CCTriangulator;
import cc.creativecomputing.io.CCIOUtil;
import cc.creativecomputing.maps.shapefile.CCShapeFile;
import cc.creativecomputing.maps.shapefile.shapetypes.CCShpPolygon;
import cc.creativecomputing.maps.shapefile.shapetypes.CCShpShape;
import cc.creativecomputing.math.CCMath;
import cc.creativecomputing.math.CCVector3f;
import cc.creativecomputing.math.spline.CCLinearSpline;

public class ILGlobe{
	
	@CCControl(name="trace head color")
	private CCColor _myTraceHeadColor = new CCColor();
	@CCControl(name="trace slow color")
	private CCColor _myTraceSlowColor = new CCColor();
	
	@CCControl(name = "trace duration", min = 0, max = 1)
	private float _cTraceDuration = 1;
	@CCControl(name = "trace fade", min = 0, max = 100)
	private float _cTraceFade = 1;
	
	@CCControl(name="land color hue", min = 0, max = 1)
	private float _cLandHue = 0;
	@CCControl(name="land color saturation", min = 0, max = 1)
	private float _cLandSaturation = 0;
	@CCControl(name="land color brightness", min = 0, max = 1)
	private float _cLandBrightness = 0;
	@CCControl(name="land color alpha", min = 0, max = 1)
	private float _cLandAlpha = 0;
	
	@CCControl(name="water color hue", min = 0, max = 1)
	private float _cWaterHue = 0;
	@CCControl(name="water color saturation", min = 0, max = 1)
	private float _cWaterSaturation = 0;
	@CCControl(name="water color brightness", min = 0, max = 1)
	private float _cWaterBrightness = 0;
	@CCControl(name="water color alpha", min = 0, max = 1)
	private float _cWaterAlpha = 0;
	
	@CCControl(name = "speed", min = 0, max = 1, external = true)
	public float _cSpeed = 0;
	
	@CCControl(name = "stroke weight", min = 0, max = 10, external = true)
	private float _cStrokeWeight = 1;
	
	@CCControl(name="x", min = -1000, max = 1000)
	private float _cX = 0;
	@CCControl(name="y", min = -1000, max = 1000)
	private float _cY = 0;
	

	@CCControl(name="x angle", min = -180, max = 180)
	private float _cXAngle = 0;
	@CCControl(name="z angle", min = -180, max = 180)
	private float _cZAngle = 0;
	
	@CCControl(name="scale", min = 0, max = 10)
	private float _cScale = 0;
	
	private CCShapeFile shapefile;
	private CCVBOMesh _myMesh;
	private CCVBOMesh _my2DMesh;
	private CCSphereMesh _mySphere;

	private CCGLSLShader _myPathShader;
	private CCRenderTexture _myRenderTexture;
	
	private List<CCDrawListener<CCGraphics>> _myLayer = new ArrayList<CCDrawListener<CCGraphics>>();
	

	private CCColor _myLandColor = new CCColor(1f);
	private CCColor _myWaterColor = new CCColor(1f);
	
	public ILGlobe(CCGraphics g) {
		shapefile = new CCShapeFile(CCIOUtil.dataPath("TM_WORLD_BORDERS_SIMPL-0.3"), "TM_WORLD_BORDERS_SIMPL-0.3").read();
		
		int myNumberOfVertices = 0;
		int myNumberOfIndices = 0;
		
		for(CCShpShape myShape:shapefile.shapes()) {
			switch(myShape.type()) {
			case Polygon:
				CCShpPolygon myPolygon = (CCShpPolygon)myShape;
				myNumberOfVertices += myPolygon.numberOfPoints();
				for(CCLinearSpline myPath:myPolygon.parts()) {
					myNumberOfIndices += (myPath.points().size() - 1) * 2;
					
				}
				break;
			}
		}
		
		_myMesh = new CCVBOMesh(CCDrawMode.LINES, myNumberOfVertices);
		FloatBuffer myVertexBuffer = FloatBuffer.allocate(myNumberOfVertices * 4);
		IntBuffer myIndices = IntBuffer.allocate(myNumberOfIndices);
		
		int myVertexCounter = 0;

		CCTriangulator myTriangulator = new CCTriangulator();
		
		for(CCShpShape myShape:shapefile.shapes()) {
			switch(myShape.type()) {
			case Polygon:
				CCShpPolygon myPolygon = (CCShpPolygon)myShape;
				for(CCLinearSpline myPath:myPolygon.parts()) {
					myTriangulator.beginPolygon();
					myTriangulator.beginContour();
					float myStart = CCMath.random();
					float myLenght = 0;
					
					float myX = 0;
					
					for(int i = 0; i < myPath.points().size();i++) {
						CCVector3f myPoint = myPath.points().get(i);
						
						myX += myPoint.x;
					}
					myX /= myPath.points().size();
					myX = CCMath.norm(myX, -180, 180);
					
					myStart = myX;
					
					for(int i = 0; i < myPath.points().size();i++) {
						CCVector3f myPoint = myPath.points().get(i);
						float myTime = myStart + (myLenght / myPath.totalLength() * 0.1f) % 1;
						float lo = CCMath.radians(-myPoint.x + 180);
						float la = CCMath.radians(myPoint.y + 90);
						
						myTriangulator.vertex(myPoint.x, myPoint.y);
						
						myVertexBuffer.put(200 * CCMath.sin(la) * CCMath.cos(lo));
						myVertexBuffer.put(200 * CCMath.sin(la) * CCMath.sin(lo));
						myVertexBuffer.put(200 * CCMath.cos(la)); 
						myVertexBuffer.put(myTime); 
						if(i < myPath.segmentsLengths().size()) {
							myLenght += myPath.segmentsLengths().get(i);
						}
						if(i > 0) {
							myIndices.put(myVertexCounter - 1);
							myIndices.put(myVertexCounter);
						}
						myVertexCounter++;
					}
					myTriangulator.endContour();
					myTriangulator.endPolygon();
				}
				break;
			}
		}
		myVertexBuffer.rewind();
		myIndices.rewind();
		_myMesh.vertices(myVertexBuffer, 4);
		_myMesh.indices(myIndices);
		
		_my2DMesh = new CCVBOMesh(CCDrawMode.TRIANGLES);
		_my2DMesh.vertices(myTriangulator.vertices());
		
		_mySphere = new CCSphereMesh(200, 100);
		
		_myPathShader = new CCGLSLShader(
			CCIOUtil.classPath(this, "globe_vert.glsl"),
			CCIOUtil.classPath(this, "globe_frag.glsl")
		);
		_myPathShader.load();
		
		CCFrameBufferObjectAttributes myAttributes = new CCFrameBufferObjectAttributes();
		myAttributes.samples(8);
		_myRenderTexture = new CCRenderTexture(g, myAttributes, 3600, 1800);
//		_myRenderTexture.generateMipmaps(true);
		_myRenderTexture.textureFilter(CCTextureFilter.LINEAR);
//		_myRenderTexture.textureMipmapFilter(CCTextureMipmapFilter.LINEAR);
		_myRenderTexture.anisotropicFiltering(1.0f);
		
	}
	
	public void addLayer(CCDrawListener<CCGraphics> theLayer) {
		_myLayer.add(theLayer);
	}
	
	private float _myTime = 0;
	
	public void update(float theDeltaTime) {
		_myTime += theDeltaTime * _cSpeed * 0.001;
		
		_myLandColor.setHSB(_cLandHue, _cLandSaturation, _cLandBrightness,_cLandAlpha);
		_myWaterColor.setHSB(_cWaterHue, _cWaterSaturation, _cWaterBrightness, _cWaterAlpha);
	}
	
	private CCColor _myLastLandColor = new CCColor();
	private CCColor _myLastWaterColor = new CCColor();
	
	public void draw(CCGraphics g) {
		g.blend();
		if(!(_myLastLandColor.equals(_myLandColor) && _myLastWaterColor.equals(_myWaterColor))){
			g.pushAttribute();
			_myRenderTexture.beginDraw();
			g.clearColor(_myWaterColor);
			g.clear();
			g.color(_myLandColor);
			g.scale(-10,10);
			_my2DMesh.draw(g);
			_myRenderTexture.endDraw();
			
			_myLastLandColor.set(_myLandColor);
			_myLastWaterColor.set(_myWaterColor);
			g.popAttribute();
		}
		
//		g.image(_myRenderTexture,0,0);
		
		g.pushMatrix();
		g.translate(_cX, _cY);
		g.rotateX(_cXAngle);
		g.rotateZ(_cZAngle);
		g.scale(_cScale);
		g.color(255);
		

		
		g.texture(_myRenderTexture);

		g.cullFace(CCCullFace.FRONT);
		_mySphere.draw(g);
		g.cullFace(CCCullFace.BACK);
		_mySphere.draw(g);
		g.noTexture();
		g.noCullFace();
		
		g.color(255);
		g.strokeWeight(_cStrokeWeight);
		_myPathShader.start();
		_myPathShader.uniform1f("currentTime", _cSpeed);
		_myPathShader.uniform1f("positionPow", _cTraceFade);
		_myPathShader.uniform1f("positionRange", _cTraceDuration);
		
		_myPathShader.uniform4f("positionMaxColor", _myTraceHeadColor);
		_myPathShader.uniform4f("positionMinColor", _myTraceSlowColor);
		_myMesh.draw(g);
		_myPathShader.end();

		for(CCDrawListener<CCGraphics> myLayer:_myLayer) {
			myLayer.draw(g);
		}
		
		g.popMatrix();
	}
}