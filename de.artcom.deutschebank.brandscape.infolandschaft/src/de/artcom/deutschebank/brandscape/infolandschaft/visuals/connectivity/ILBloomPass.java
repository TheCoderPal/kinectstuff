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

import cc.creativecomputing.CCApp;
import cc.creativecomputing.control.CCControl;
import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.graphics.CCRenderTexture;
import cc.creativecomputing.graphics.CCAbstractGraphics.CCBlendMode;
import cc.creativecomputing.graphics.shader.CCGLSLShader;
import cc.creativecomputing.graphics.shader.imaging.CCGPUSeperateGaussianBlur;
import cc.creativecomputing.graphics.texture.CCFrameBufferObjectAttributes;
import cc.creativecomputing.io.CCIOUtil;

/**
 * @author christianriekoff
 *
 */
public class ILBloomPass {
	private CCRenderTexture _myRenderTexture;
	private CCGLSLShader _myBloomShader;
	
	@CCControl(name = "highlightRange", min = 0, max = 1)
	private float _cHighlightColor;
	
	@CCControl(name = "highlightScale", min = 0, max = 10)
	private float _cHighlightScale;
	
	@CCControl(name = "highlightPow", min = 0, max = 10)
	private float _cHighlightPow;
	
	@CCControl(name = "debug bloom")
	private boolean _cDebugBloom = false;
	
	public final static float MAXIMUM_BLUR_RADIUS = 50;
	
	@CCControl(name = "blur radius", min = 0, max = MAXIMUM_BLUR_RADIUS)
	private float _cBlurRadius = MAXIMUM_BLUR_RADIUS;
	
	@CCControl(name = "blur radius 2", min = 0, max = MAXIMUM_BLUR_RADIUS)
	private float _cBlurRadius2 = MAXIMUM_BLUR_RADIUS;
	
	private CCGPUSeperateGaussianBlur _myBlur;
	
	private int _myWidth;
	private int _myHeight;
	
	public ILBloomPass(CCApp theApp) {
		CCFrameBufferObjectAttributes myAttributes = new CCFrameBufferObjectAttributes();
		myAttributes.samples(8);
		_myRenderTexture = new CCRenderTexture(theApp.g, myAttributes, theApp.width, theApp.height);
		
		_myBlur = new CCGPUSeperateGaussianBlur(10, theApp.width, theApp.height, 2);
		
		theApp.addControls("connectivity", "bloom", 4, this);
		
		_myBloomShader = new CCGLSLShader(
			CCIOUtil.classPath(this, "bloom_vert.glsl"), 
			CCIOUtil.classPath(this, "bloom_frag.glsl")
		);
		_myBloomShader.load();
		
		_myWidth = theApp.width;
		_myHeight = theApp.height;
	}
	
	public void startBloom(CCGraphics g) {
		_myRenderTexture.beginDraw();
	}
	
	public void endBloom(CCGraphics g) {
		_myRenderTexture.endDraw();

		_myBlur.radius(_cBlurRadius);
		g.clear();
		g.color(255);
		_myBlur.beginDraw(g);
		g.clear();
		g.image(_myRenderTexture, -_myWidth/2, -_myHeight/2);
		_myBlur.endDraw(g);
		g.clear();
		
		if(!_cDebugBloom) {
			g.image(_myRenderTexture, -_myWidth/2, -_myHeight/2);
			g.blend(CCBlendMode.ADD);
		}
		
		_myBloomShader.start();
		_myBloomShader.uniform1i("texture", 0);
		_myBloomShader.uniform1f("highlightRange", _cHighlightColor);
		_myBloomShader.uniform1f("highlightScale", _cHighlightScale);
		_myBloomShader.uniform1f("highlightPow", _cHighlightPow);
		g.image(_myBlur.blurredTexture(), -_myWidth/2, -_myHeight/2, _myWidth, _myHeight);
		_myBloomShader.end();
		g.blend();
	}
	
	public void startBlur(CCGraphics g) {
		_myRenderTexture.beginDraw();
	}
	
	public void endBlur(CCGraphics g) {
		_myRenderTexture.endDraw();

		_myBlur.radius(_cBlurRadius2);
		g.color(255);
		_myBlur.beginDraw(g);
		g.clear();
		g.image(_myRenderTexture, -_myWidth/2, -_myHeight/2);
		_myBlur.endDraw(g);
		
	}
}
