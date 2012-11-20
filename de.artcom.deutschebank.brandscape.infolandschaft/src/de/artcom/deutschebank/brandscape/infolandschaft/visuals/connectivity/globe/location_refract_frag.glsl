#version 110

varying vec3  n;
varying vec3  i;

uniform float fresnelPower;
uniform float eta;
uniform float f;

uniform sampler2D cubemap;

uniform sampler2D normalMap;
uniform float normalMapAmount;

uniform float diffuseAmount;


uniform vec3 lightDir;
uniform float lightAmount;

void main(){
	vec3 texNormal = texture2D(normalMap, gl_TexCoord[0].st).xyz * 2.0 - 1.0;
	vec4 diffuse = gl_Color;//texture2D(diffuseMap, gl_TexCoord[0].st);

	vec3 normal = normalize(mix(n,texNormal,normalMapAmount));

    vec3 refract = refract(i, normal, eta);
    refract = vec3(gl_TextureMatrix[0] * vec4(refract, 1.0));

    vec3 reflect = reflect(i, normal);
    reflect = vec3(gl_TextureMatrix[0] * vec4(reflect, 1.0));
    
    vec3 refractColor = vec3(texture2D(cubemap, refract.xy / 2.0 + vec2(0.5,0)));
    vec3 reflectColor = vec3(texture2D(cubemap, reflect.xy / 2.0 + vec2(0.5,0)));

	float ratio = f + (1.0 - f) * pow((1.0 - dot(-i, normal)), fresnelPower);
    vec3 color = mix(refractColor, reflectColor, ratio);
    color = mix(color, diffuse.rgb, diffuseAmount);
	
	vec3 ppNormal			= normalize( n + texNormal );
	float ppDiffuse			= abs( dot( ppNormal, lightDir ) );
	float ppFresnel			= pow( ( 1.0 - ppDiffuse ), 3.0 );
	float ppSpecular		= pow( ppDiffuse, 10.0 );
	float ppSpecularBright	= pow( ppDiffuse, 120.0 );

	vec3 oceanFinal			= color * ppSpecular + ppSpecularBright + ppFresnel * 15.0;
	
	gl_FragColor.rgb		= mix(color, oceanFinal, lightAmount);// + vec3( r*r, r * 0.25, 0 ) * oceanValue;
	gl_FragColor.a			= gl_Color.a;
}