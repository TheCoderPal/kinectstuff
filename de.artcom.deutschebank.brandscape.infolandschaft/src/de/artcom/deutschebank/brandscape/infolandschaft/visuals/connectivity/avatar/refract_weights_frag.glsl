#version 110

varying vec3  n;
varying vec3  i;

uniform float fresnelPower;
uniform float eta;
uniform float f;

uniform sampler2D Texture;

uniform vec3 lightDir;
uniform float lightAmount;
uniform float alpha;

uniform float fresnelPow;
uniform float specularPow;
uniform float specularBrightPow;

void main(){

	vec3 normal = n;

	float ratio   = f + (1.0 - f) * pow((1.0 - dot(-i, normal)), fresnelPower);

    vec3 refract = refract(i, normal, eta);
    refract = vec3(gl_TextureMatrix[0] * vec4(refract, 1.0));

    vec3 reflect = reflect(i, normal);
    reflect = vec3(gl_TextureMatrix[0] * vec4(reflect, 1.0));
    
    vec3 refractColor = vec3(texture2D(Texture, refract.xy / 2.0 + vec2(0.5,0)));
    vec3 reflectColor = vec3(texture2D(Texture, reflect.xy / 2.0 + vec2(0.5,0)));

    vec3 color   = mix(refractColor, reflectColor, ratio);
   // color = mix(color, diffuse.rgb, diffuseAmount);
    gl_FragColor = vec4(color, alpha);
    
    vec3 ppNormal			= normalize(normal );
	float ppDiffuse			= abs( dot( ppNormal, lightDir ) );
	float ppFresnel			= pow( ( 1.0 - ppDiffuse ), fresnelPow );
	float ppSpecular		= pow( ppDiffuse, specularPow );
	float ppSpecularBright	= pow( ppDiffuse, specularBrightPow );
	
	

	//vec3 landFinal			= color * landValue + ppSpecularBright * landValue;
	vec3 oceanFinal			= vec3(1,1,1) * ppSpecular + ppSpecularBright;// + ppFresnel * 15.0;
	
	gl_FragColor.rgb		= mix(color, oceanFinal, lightAmount);// + vec3( r*r, r * 0.25, 0 ) * oceanValue;
	gl_FragColor.a			= alpha;
}