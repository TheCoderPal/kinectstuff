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
public class ILGlobeRefractionShader extends CCGLSLShader{
	
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
	
	@CCControl(name = "fresnelPow", min = -1, max = 10)
	private float _cFresnelPow = 0;
	@CCControl(name = "specularPow", min = -1, max = 10)
	private float _cSpecularPow = 0;
	@CCControl(name = "specularBrightPow", min = -1, max = 150)
	private float _cSpecularBrightPow = 0;
	
	@CCControl(name = "light amount", min = 0, max = 1)
	private float _cLightAmount = 0;
	
	@CCControl(name = "alpha", min = 0, max = 1)
	private float _cAlpha = 0;

	
	private CCGraphics _myGraphics;
	private CCTexture2D _myTextureBack;
	private CCTexture2D _myTextureFront;
	private CCTexture2D _myNormalMap;
	private CCTexture2D _myDiffuseMap;
	private CCTexture2D _myMask;

	/**
	 * @param theVertexShaderFile
	 * @param theFragmentShaderFile
	 */
	public ILGlobeRefractionShader(CCGraphics theG, CCTexture2D theTextureBack, CCTexture2D theTextureFront) {
		super(
			CCIOUtil.classPath(ILGlobeRefractionShader.class, "globe_refract_vert.glsl"), 
			CCIOUtil.classPath(ILGlobeRefractionShader.class, "globe_refract_frag.glsl")
		);
		load();
		_myGraphics = theG;
		_myTextureBack = theTextureBack;
		_myTextureFront = theTextureFront;

		_myNormalMap = new CCTexture2D(CCTextureIO.newTextureData("earth/earthNormal.png"));
		_myDiffuseMap = new CCTexture2D(CCTextureIO.newTextureData("earth/earthDiffuse.png"));
		_myMask = new CCTexture2D(CCTextureIO.newTextureData("earth/earthMask.png"));
	}
	private float f() {
		return ((1f - _cEta) * (1f - _cEta)) / ((1f + _cEta) * (1f + _cEta));
	}
	
	private float _myAlpha = 1f;
	
	public void alpha(float theAlpha){
		_myAlpha = theAlpha;
	}
	
	@Override
	public void start() {
		_myGraphics.texture(0, _myTextureBack);
		_myGraphics.texture(1, _myTextureFront);
		_myGraphics.texture(2, _myNormalMap);
		_myGraphics.texture(3, _myDiffuseMap);
		_myGraphics.texture(4, _myMask);
		super.start();
		uniform1i("cubemapBack", 0);
		uniform1i("cubemapFront", 1);
		uniform1i("normalMap", 2);
		uniform1i("diffuseMap", 3);
		uniform1i("earthMask", 4);
		uniform2f("screenDim", _myGraphics.width, _myGraphics.height);
		
		uniform1f("f", f());
		uniform1f("eta", _cEta);
		uniform1f("amount", _cAmount);
		uniform1f("normalMapAmount", _cNormalMapAmount);
		uniform1f("diffuseAmount", _cDiffuseAmount);
		uniform1f("fresnelPower", _cFresnelPower);
		
		uniform3f("lightDir", new CCVector3f(_cLightX, _cLightY, _cLightZ).normalize());
		uniform1f("lightAmount", _cLightAmount);
		uniform1f("fresnelPow", _cFresnelPow);
		uniform1f("specularPow", _cSpecularPow);
		uniform1f("specularBrightPow", _cSpecularBrightPow);
		uniform1f("alpha", _cAlpha * _myAlpha);
	}
	
	@Override
	public void end() {
		super.end();
		_myGraphics.noTexture();
	}
}
