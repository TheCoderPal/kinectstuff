#version 110

varying vec3  n;
varying vec3  i;

uniform float fresnelPower;
uniform float eta;
uniform float f;

uniform sampler2D background;
uniform sampler2D foreground;

uniform sampler2D normalMap;
uniform float normalMapAmount;

uniform sampler2D diffuseMap;
uniform float diffuseAmount;

uniform sampler2D earthMask;

uniform vec3 lightDir;
uniform float lightAmount;
uniform float landNormalMapAmount;
uniform float alpha;

uniform float amount;

uniform vec4 landColor;
uniform float landPow;
uniform vec4 waterColor;
uniform float waterPow;

/*
float ratio   = f + (1.0 - f) * pow((1.0 - dot(-i, normal)), fresnelPower);

    vec3 refract = refract(i, normal, eta);
    refract = vec3(gl_TextureMatrix[0] * vec4(refract, 1.0));

    vec3 reflect = reflect(i, normal);
    reflect = vec3(gl_TextureMatrix[0] * vec4(reflect, 1.0));
    
    vec2 fragCoord = vec2(0.0,1.0) + gl_FragCoord.xy / vec2(1500.0,-900.0);
	vec2 texCoordRefract = mix(fragCoord,refract.xy / 2.0 + vec2(0.5,0), amount);
	vec2 texCoordReflect = mix(fragCoord,reflect.xy / 2.0 + vec2(0.5,0), amount);
    vec3 refractColor = vec3(texture2D(background, texCoordRefract));//refract.xy / 2.0 + vec2(0.5,0)));
    vec3 reflectColor = vec3(texture2D(foreground, texCoordReflect));//reflect.xy / 2.0 + vec2(0.5,0)));

    vec3 color   = mix(refractColor, reflectColor, ratio);
    color = mix(color, diffuse.rgb, diffuseAmount);
    
    
    */
    
vec3 lightColor(){

}

void main(){
	vec3 texNormal = texture2D(normalMap, gl_TexCoord[0].st).xyz * 2.0 - 1.0;
	vec4 diffuse = texture2D(diffuseMap, gl_TexCoord[0].st);
	
	vec3 normal = normalize(mix(n,texNormal,normalMapAmount));
	
	vec3 texSample = texture2D(earthMask, gl_TexCoord[0].st ).rgb;

	
    
    // use green channel for land elevation data
	float landValue = pow(texSample.g,landPow);

	// use blue channel for ocean elevation data
	float oceanValue = pow(texSample.b, waterPow);
	
	float landBrightness = pow(texSample.r,landPow) * landValue;
	
	vec3 ppNormal			= normalize( n + texNormal );
	float ppDiffuse			= abs( dot( ppNormal, lightDir ) );
	float ppFresnel			= pow( ( 1.0 - ppDiffuse ), 3.0 );
	float ppSpecular		= pow( ppDiffuse, 10.0 );
	float ppSpecularBright	= pow( ppDiffuse, 120.0 );


	vec3 landFinal			= landColor.rgb * landValue * ppDiffuse;// + ppSpecularBright * landValue;
	vec3 oceanFinal			= waterColor.rgb * oceanValue;//ppSpecular * oceanValue + oceanValue * ppSpecularBright + oceanValue * ppFresnel * 15.0;
	
	gl_FragColor.rgb		= landFinal + oceanFinal;//mix(color, landFinal + oceanFinal, lightAmount);// + vec3( r*r, r * 0.25, 0 ) * oceanValue;
	gl_FragColor.a			= alpha;
}