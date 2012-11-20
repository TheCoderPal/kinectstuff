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

import cc.creativecomputing.control.CCControl;
import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.graphics.shader.CCGLSLShader;
import cc.creativecomputing.graphics.texture.CCTexture2D;
import cc.creativecomputing.io.CCIOUtil;
import cc.creativecomputing.math.CCVector3f;

/**
 * @author christianriekoff
 *
 */
public class ILAvatarRefractionShader extends CCGLSLShader{
	
	@CCControl(name = "amount", min = 0, max = 1)
	private float _cAmount;
	
	@CCControl(name = "alpha", min = 0, max = 1)
	private float _cAlpha;
	
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
	
	@CCControl(name = "fresnelPow", min = -1, max = 10)
	private float _cFresnelPow = 0;
	@CCControl(name = "specularPow", min = -1, max = 10)
	private float _cSpecularPow = 0;
	@CCControl(name = "specularBrightPow", min = -1, max = 150)
	private float _cSpecularBrightPow = 0;
	
	private CCGraphics _myGraphics;
	private CCTexture2D _myTexture;

	/**
	 * @param theVertexShaderFile
	 * @param theFragmentShaderFile
	 */
	public ILAvatarRefractionShader(CCGraphics theG, CCTexture2D theTexture) {
		super(
			CCIOUtil.classPath(ILAvatarRefractionShader.class, "refract_weights_vert.glsl"), 
			CCIOUtil.classPath(ILAvatarRefractionShader.class, "refract_weights_frag.glsl")
		);
		load();
		_myGraphics = theG;
		_myTexture = theTexture;
	}

	public void Texture(CCTexture2D theTexture) {
		_myTexture = theTexture;
	}
	
	private float f() {
		return ((1f - _cEta) * (1f - _cEta)) / ((1f + _cEta) * (1f + _cEta));
	}
	
	@Override
	public void start() {
		_myGraphics.texture(0, _myTexture);
		super.start();
		uniform1i("Texture", 0);
		
		uniform1f("f", f());
		uniform1f("eta", _cEta);
		uniform1f("amount", _cAmount);
//		uniform1f("diffuseAmount", _cDiffuseAmount);
		uniform1f("fresnelPower", _cFresnelPower);
		uniform1f("alpha", _cAlpha);
		
		uniform3f("lightDir", new CCVector3f(_cLightX, _cLightY, _cLightZ).normalize());
		uniform1f("lightAmount", _cLightAmount);
		uniform1f("fresnelPow", _cFresnelPow);
		uniform1f("specularPow", _cSpecularPow);
		uniform1f("specularBrightPow", _cSpecularBrightPow);
	}
	
	@Override
	public void end() {
		super.end();
		_myGraphics.noTexture();
	}
}
