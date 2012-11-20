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

import cc.creativecomputing.control.CCControl;
import cc.creativecomputing.graphics.CCAbstractGraphics.CCBlendMode;
import cc.creativecomputing.graphics.CCColor;
import cc.creativecomputing.graphics.CCDrawMode;
import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.graphics.CCVBOMesh;
import cc.creativecomputing.graphics.font.CCFontIO;
import cc.creativecomputing.graphics.font.CCFontSettings;
import cc.creativecomputing.graphics.font.CCTextureMapFont;
import cc.creativecomputing.graphics.shader.CCGLSLShader;
import cc.creativecomputing.io.CCIOUtil;
import cc.creativecomputing.math.CCMath;
import cc.creativecomputing.math.CCVecMath;
import cc.creativecomputing.math.CCVector2f;
import cc.creativecomputing.xml.CCXMLElement;
import cc.creativecomputing.xml.CCXMLIO;

public class ILFinancialStreams {
		
		@CCControl(name = "radius", min = 0, max = 1000f)
		private float _cRadius = 0;
		
		@CCControl(name = "radius inc", min = 0, max = 100f)
		private float _cRadiusInc = 0;
		
		@CCControl(name="trace head color")
		private CCColor _myTraceHeadColor = new CCColor();
		@CCControl(name="trace slow color")
		private CCColor _myTraceSlowColor = new CCColor();
		
		@CCControl(name = "trace duration", min = 0, max = 1)
		private float _cTraceDuration = 1;
		@CCControl(name = "trace fade", min = 0, max = 100)
		private float _cTraceFade = 1;
		
		@CCControl(name = "speed", min = 0, max = 1, external = true)
		public float _cSpeed = 0;
		
		@CCControl(name = "stroke Weight", min = 0, max = 10)
		public float _cStrokeWeight = 0;
		
		@CCControl(name = "blurred strokeWeight", min = 0, max = 30)
		public float _cBlurredStrokeWeight = 0;
		
		private List<ILLocation> _myLocationList = new ArrayList<ILLocation>();
		private float _myMaxPop = 0;
		
		private CCVBOMesh _myStreamMesh;
		
		private CCGLSLShader _myStreamShader;
		
		public ILFinancialStreams() {
			CCFontSettings myFontSettings = new CCFontSettings("DeuBaUnivers-Regular", 30);
			CCTextureMapFont myTextFont = CCFontIO.createTextureMapFont(myFontSettings);
			
			CCFontSettings myShadowFontSettings = new CCFontSettings("DeuBaUnivers-Regular", 30);
			myShadowFontSettings.blurRadius(10);
			CCTextureMapFont myShadowFont = CCFontIO.createTextureMapFont(myShadowFontSettings);
			
			for(CCXMLElement myLocationXML:CCXMLIO.createXMLElement("connectivity_content.xml")) {
				ILLocation myLocation = new ILLocation(myLocationXML, myTextFont, myShadowFont);
				_myMaxPop = CCMath.max(_myMaxPop,myLocation.value());
				_myLocationList.add(myLocation);
			}
			
			_myStreamMesh = buildMesh();
			
			_myStreamShader = new CCGLSLShader(
				CCIOUtil.classPath(this, "stream_vert.glsl"),
				CCIOUtil.classPath(this, "stream_frag.glsl")
			);
			_myStreamShader.load();
		}
		
		private CCVBOMesh buildMesh() {
			int myNumberOfVertices = (_myLocationList.size()) / 2 *  (_myLocationList.size());
			myNumberOfVertices *= 100;
			CCVBOMesh myLinesMesh = new CCVBOMesh(CCDrawMode.LINES, myNumberOfVertices);
			
			List<Integer> myIndices = new ArrayList<Integer>();
			int counter = 0;
			for(int i = 0; i < _myLocationList.size() - 1;i++) {
				ILLocation myLoc1 = _myLocationList.get(i);
				for(int j = i + 1; j < _myLocationList.size();j++) {
					ILLocation myLoc2 = _myLocationList.get(j);
					
					CCVector2f myLoc12f = new CCVector2f(myLoc1.longitude(), myLoc1.latitude());
					CCVector2f myLoc22f = new CCVector2f(myLoc2.longitude(), myLoc2.latitude());
					if(CCMath.abs(myLoc22f.x - myLoc12f.x) > 180) {
						if(myLoc12f.x < 0)myLoc12f.x += 360;
						else myLoc22f.x += 360;
					}
					
					float myStartTime = CCMath.random();
					
					for(int k = 0; k < 100;k++) {
						float myBlend = k / 99f;
						CCVector2f myLoc = CCVecMath.blend(myBlend, myLoc12f, myLoc22f);
						myLinesMesh.addVertex(myLoc.x, myLoc.y, myBlend, myStartTime + myBlend * 0.01f);
						if(k > 0) {
							myIndices.add(counter - 1);
							myIndices.add(counter);
						}
						counter++;
					}
				}
			}
			myLinesMesh.indices(myIndices);
			
			return myLinesMesh;
		}
		
		public void drawStraight(CCGraphics g) {
			draw(g, _cStrokeWeight);
		}
		
		public void drawBlurred(CCGraphics g) {
			draw(g, _cBlurredStrokeWeight);
		}
		
		private float _myAlpha = 1f;
		
		public void alpha(float theAlpha){
			_myAlpha = theAlpha;
		}
		
		private void draw(CCGraphics g, float theStrokeWeight) {
			g.pushAttribute();
			g.noDepthMask();
			
			g.blend(CCBlendMode.ADD);
			g.strokeWeight(theStrokeWeight);
			g.color(1f);
			_myStreamShader.start();
			_myStreamShader.uniform1f("radius", _cRadius);
			_myStreamShader.uniform1f("radiusInc", _cRadiusInc);
			
			_myStreamShader.uniform1f("currentTime", _cSpeed);
			_myStreamShader.uniform1f("positionPow", _cTraceFade);
			_myStreamShader.uniform1f("positionRange", _cTraceDuration);
			
			
			_myStreamShader.uniform4f(
				"positionMaxColor", 
				_myTraceHeadColor.r, 
				_myTraceHeadColor.g,
				_myTraceHeadColor.b, 
				_myTraceHeadColor.a * _myAlpha
			);
			_myStreamShader.uniform4f(
				"positionMinColor", 
				_myTraceSlowColor.r, 
				_myTraceSlowColor.g,
				_myTraceSlowColor.b, 
				_myTraceSlowColor.a * _myAlpha
			);
			_myStreamMesh.draw(g);
			_myStreamShader.end();
			
			g.popAttribute();
		}
		
		public List<ILLocation> locations(){
			return _myLocationList;
		}
	}