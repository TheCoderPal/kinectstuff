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

import cc.creativecomputing.control.CCControl;
import cc.creativecomputing.graphics.CCColor;
import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.graphics.shader.CCGLSLShader;
import cc.creativecomputing.graphics.texture.CCTexture2D;
import cc.creativecomputing.graphics.texture.CCTextureIO;
import cc.creativecomputing.io.CCIOUtil;
import cc.creativecomputing.math.CCVector3f;

/**
 * @author christianriekoff
 *
 */
public class CopyOfILGlobeRefractionShader extends CCGLSLShader{
	
	@CCControl(name = "amount", min = 0, max = 1)
	private float _cAmount;
	
	@CCControl(name = "normal map amount", min = 0, max = 1)
	private float _cNormalMapAmount;
	
	@CCControl(name = "diffuse amount", min = 0, max = 1)
	private float _cDiffuseAmount;
	
	@CCControl(name = "eta", min = 0, max = 2)
	private float _cEta;
	
	@CCControl(name = "fresnel power", min = 0, max = 10)
	private float _cFresnelPower;
	
	@CCControl(name = "light x", min = -1, max = 1)
	private float _cLightX = 0;
	
	@CCControl(name = "light y", min = -1, max = 1)
	private float _cLightY = 0;
	
	@CCControl(name = "light z", min = -1, max = 1)
	private float _cLightZ = 0;
	
	@CCControl(name = "light amount", min = 0, max = 1)
	private float _cLightAmount = 0;
	
	@CCControl(name = "alpha", min = 0, max = 1)
	private float _cAlpha = 0;
	
	@CCControl(name = "land hue", min = 0, max = 1)
	private float _cLandHue = 0;
	
	@CCControl(name = "land saturation", min = 0, max = 1)
	private float _cLandSaturation = 0;
	
	@CCControl(name = "land brightness", min = 0, max = 1)
	private float _cLandBrightness = 0;
	
	@CCControl(name = "land alpha", min = 0, max = 1)
	private float _cLandAlpha = 0;
	
	@CCControl(name = "land pow", min = 0, max = 10)
	private float _cLandPow = 0;

	
	@CCControl(name = "water hue", min = 0, max = 1)
	private float _cWaterHue = 0;
	
	@CCControl(name = "water saturation", min = 0, max = 1)
	private float _cWaterSaturation = 0;
	
	@CCControl(name = "water brightness", min = 0, max = 1)
	private float _cWaterBrightness = 0;
	
	@CCControl(name = "water alpha", min = 0, max = 1)
	private float _cWaterAlpha = 0;
	
	@CCControl(name = "water pow", min = 0, max = 10)
	private float _cWaterPow = 0;

	
	private CCGraphics _myGraphics;
	private CCTexture2D _myBackground;
	private CCTexture2D _myForeground;
	private CCTexture2D _myNormalMap;
	private CCTexture2D _myDiffuseMap;
	private CCTexture2D _myMask;

	/**
	 * @param theVertexShaderFile
	 * @param theFragmentShaderFile
	 */
	public CopyOfILGlobeRefractionShader(CCGraphics theG, CCTexture2D theBackground, CCTexture2D theForegroundTexture) {
		super(
			CCIOUtil.classPath(CopyOfILGlobeRefractionShader.class, "globe_refract_vert.glsl"), 
			CCIOUtil.classPath(CopyOfILGlobeRefractionShader.class, "globe_refract_frag.glsl")
		);
		load();
		_myGraphics = theG;
		_myBackground = theBackground;
		_myForeground = theForegroundTexture;

		_myNormalMap = new CCTexture2D(CCTextureIO.newTextureData("earth/earthNormal.png"));
		_myDiffuseMap = new CCTexture2D(CCTextureIO.newTextureData("earth/earthDiffuse.png"));
		_myMask = new CCTexture2D(CCTextureIO.newTextureData("earth/earthMask2.png"));
	}

	public void Texture(CCTexture2D theTexture) {
		_myBackground = theTexture;
	}
	
	private float f() {
		return ((1f - _cEta) * (1f - _cEta)) / ((1f + _cEta) * (1f + _cEta));
	}
	
	@Override
	public void start() {
		_myGraphics.texture(0, _myBackground);
		_myGraphics.texture(1, _myForeground);
		_myGraphics.texture(2, _myNormalMap);
		_myGraphics.texture(3, _myDiffuseMap);
		_myGraphics.texture(4, _myMask);
		super.start();
		uniform1i("background", 0);
		uniform1i("foreground", 1);
		uniform1i("normalMap", 2);
		uniform1i("diffuseMap", 3);
		uniform1i("earthMask", 4);
		
		uniform1f("f", f());
		uniform1f("eta", _cEta);
		uniform1f("amount", _cAmount);
		uniform1f("normalMapAmount", _cNormalMapAmount);
		uniform1f("diffuseAmount", _cDiffuseAmount);
		uniform1f("fresnelPower", _cFresnelPower);
		
		uniform3f("lightDir", new CCVector3f(_cLightX, _cLightY, _cLightZ).normalize());
		uniform1f("lightAmount", _cLightAmount);
		uniform1f("alpha", _cAlpha);
		
		uniform4f("landColor", new CCColor().setHSB(_cLandHue, _cLandSaturation, _cLandBrightness, _cLandAlpha));
		uniform1f("landPow", _cLandPow);
		uniform4f("waterColor", new CCColor().setHSB(_cWaterHue, _cWaterSaturation, _cWaterBrightness, _cWaterAlpha));
		uniform1f("waterPow", _cWaterPow);
	}
	
	@Override
	public void end() {
		super.end();
		_myGraphics.noTexture();
	}
}
