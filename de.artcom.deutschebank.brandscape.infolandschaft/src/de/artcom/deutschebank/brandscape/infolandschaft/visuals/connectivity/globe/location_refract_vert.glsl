varying vec3 n;
varying vec3 i;


void main(){

    vec4 ecPosition  = gl_ModelViewMatrix * gl_Vertex;
    vec3 ecPosition3 = ecPosition.xyz / ecPosition.w;

    i = normalize(ecPosition3);
    n = normalize(gl_NormalMatrix * gl_Normal);

  	gl_TexCoord[0] = gl_MultiTexCoord0;
    gl_Position = ftransform();
    gl_FrontColor = gl_Color;
}