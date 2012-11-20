uniform float currentTime;

uniform float positionRange;
uniform float positionPow;

uniform vec4 positionMaxColor;
uniform vec4 positionMinColor;

void main(){
	float time = gl_TexCoord[0].x;
	
	float myBlend = currentTime - time;
	if(myBlend < 0.0)discard;
	
	float positionIntensity =  1.0 - myBlend / positionRange;
	positionIntensity = pow(positionIntensity, positionPow);
	
	gl_FragColor = mix(positionMinColor, positionMaxColor, positionIntensity) * step(0.0, positionIntensity);
		
	//gl_FragColor = positionColor;
	//gl_FragColor = gl_Color;
}