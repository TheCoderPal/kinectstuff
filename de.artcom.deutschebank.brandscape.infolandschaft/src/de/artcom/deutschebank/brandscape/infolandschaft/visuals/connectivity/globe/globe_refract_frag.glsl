#version 110

varying vec3  n;
varying vec3  i;

uniform float fresnelPower;
uniform float eta;
uniform float f;

uniform sampler2D cubemapBack;
uniform sampler2D cubemapFront;

uniform sampler2D normalMap;
uniform float normalMapAmount;

uniform sampler2D diffuseMap;
uniform float diffuseAmount;

uniform sampler2D earthMask;

uniform vec3 lightDir;
uniform float lightAmount;
uniform float alpha;

uniform vec2 screenDim;
uniform float amount;

uniform float fresnelPow;
uniform float specularPow;
uniform float specularBrightPow;

void main(){

	vec2 texCoord = gl_FragCoord.xy / screenDim;
	texCoord.y = 1.0 - texCoord.y;
	
	vec3 texNormal = texture2D(normalMap, gl_TexCoord[0].st).xyz * 2.0 - 1.0;
	vec4 diffuse = texture2D(diffuseMap, gl_TexCoord[0].st);
	vec3 texSample = texture2D( earthMask, gl_TexCoord[0].st ).rgb;

	vec3 normal = normalize(mix(n,texNormal,normalMapAmount));

    vec3 refract = refract(i, normal, eta);
    refract = vec3(gl_TextureMatrix[0] * vec4(refract, 1.0));

    vec3 reflect = reflect(i, normal);
    reflect = vec3(gl_TextureMatrix[0] * vec4(reflect, 1.0));
    
    vec2 refractCoords = mix(texCoord, refract.xy / 2.0 + vec2(0.5,0),amount);
    vec3 refractColor = vec3(texture2D(cubemapBack, refractCoords));
    vec2 reflectCoords = mix(texCoord, reflect.xy / 2.0 + vec2(0.5,0),amount);
    vec3 reflectColor = vec3(texture2D(cubemapFront, reflectCoords));

	float ratio = f + (1.0 - f) * pow((1.0 - dot(-i, normal)), fresnelPower);
    vec3 color = mix(refractColor, reflectColor, ratio);
    color = mix(color, diffuse.rgb, diffuseAmount);
    
    // use green channel for land elevation data
	float landValue			= texSample.g;

	// use blue channel for ocean elevation data
	float oceanValue		= texSample.b;
	
	vec3 ppNormal			= normalize( n + texNormal );
	float ppDiffuse			= abs( dot( ppNormal, lightDir ) );
	float ppFresnel			= pow( ( 1.0 - ppDiffuse ), fresnelPow );
	float ppSpecular		= pow( ppDiffuse, specularPow );
	float ppSpecularBright	= pow( ppDiffuse, specularBrightPow );

	vec3 landFinal			= (color + ppSpecularBright) * landValue;
	vec3 oceanFinal			= (color * ppSpecular + ppSpecularBright + ppFresnel * 15.0) * oceanValue;
	
	gl_FragColor.rgb		= mix(color, landFinal + oceanFinal, lightAmount);// + vec3( r*r, r * 0.25, 0 ) * oceanValue;
	gl_FragColor.a			= alpha;
}