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
package de.artcom.deutschebank.brandscape.infolandschaft.visuals;

import cc.creativecomputing.control.CCControl;
import cc.creativecomputing.graphics.CCColor;
import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.graphics.texture.CCTexture2D;
import cc.creativecomputing.graphics.texture.CCTextureIO;

/**
 * @author christianriekoff
 *
 */
public class ILBar{
	
	@CCControl(name = "x", min = -500, max = 500)
	private float _cX = 0;
	@CCControl(name = "y", min = -500, max = 500)
	private float _cY = 0;
	@CCControl(name = "z", min = -5000, max = 1500)
	private float _cZ = 0;
	
	@CCControl(name = "scale x", min = 0.1f, max = 3)
	private float _cScaleX = 0;
	@CCControl(name = "scale y", min = 0.1f, max = 3)
	private float _cScaleY = 0;
	@CCControl(name = "alpha", min = 0f, max = 1f, external = true)
	private float _cAlpha = 0;
	@CCControl(name = "hue", min = 0f, max = 1f, external = true)
	private float _cHue = 0;
	@CCControl(name = "saturation", min = 0f, max = 1f, external = true)
	private float _cSaturation = 0;
	@CCControl(name = "brightness", min = 0f, max = 1f, external = true)
	private float _cBrightness = 0;
	
	@CCControl(name = "rotate", min = -180, max = 180)
	private float _cRotate = 0;
	
	private CCTexture2D _myTexture;
	
	public ILBar() {
		_myTexture = new CCTexture2D(CCTextureIO.newTextureData("Logo_Stripe02.png"));
	}
	
	public void draw(CCGraphics g) {
		g.clearDepthBuffer();
		g.pushMatrix();
		g.scale(ILApp.SCALE);
		g.translate(_cX, _cY, _cZ);
		g.scale(_cScaleX, _cScaleY);
		g.rotate(_cRotate);
		g.color(CCColor.createFromHSB(_cHue, _cSaturation, _cBrightness, _cAlpha));
		g.image(_myTexture, -g.width/2, -g.height/2, g.width, g.height);
		g.popMatrix();
	}
}
