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
public class ILLocationRefractionShader extends CCGLSLShader{
	
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
	

	
	private CCGraphics _myGraphics;
	private CCTexture2D _myNormalMap;

	/**
	 * @param theVertexShaderFile
	 * @param theFragmentShaderFile
	 */
	public ILLocationRefractionShader(CCGraphics theG) {
		super(
			CCIOUtil.classPath(ILLocationRefractionShader.class, "location_refract_vert.glsl"), 
			CCIOUtil.classPath(ILLocationRefractionShader.class, "location_refract_frag.glsl")
		);
		load();
		_myGraphics = theG;

		_myNormalMap = new CCTexture2D(CCTextureIO.newTextureData("earth_small/earthNormal.png"));
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
		_myGraphics.texture(0, _myNormalMap);
		super.start();
		uniform1i("normalMap", 0);
		
		uniform1f("f", f());
		uniform1f("eta", _cEta);
		uniform1f("amount", _cAmount);
		uniform1f("normalMapAmount", _cNormalMapAmount);
		uniform1f("diffuseAmount", _cDiffuseAmount);
		uniform1f("fresnelPower", _cFresnelPower);
		
		uniform3f("lightDir", new CCVector3f(_cLightX, _cLightY, _cLightZ).normalize());
		uniform1f("lightAmount", _cLightAmount);
	}
	
	@Override
	public void end() {
		super.end();
		_myGraphics.noTexture();
	}
}
