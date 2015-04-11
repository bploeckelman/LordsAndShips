#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

varying LOWP vec4 v_color;
varying      vec2 v_texCoord0;

uniform      sampler2D u_texture;
uniform LOWP vec4      u_ambient;

void main() {
	vec4 diffuseColor = texture2D(u_texture, v_texCoord0);
	vec3 ambientColor = u_ambient.rgb * u_ambient.a;
	vec3 finalColor = v_color.rgb * diffuseColor.rgb * ambientColor;
	gl_FragColor = vec4(finalColor, diffuseColor.a);
}
