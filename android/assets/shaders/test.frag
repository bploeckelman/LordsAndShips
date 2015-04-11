#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

varying LOWP vec4 v_color;
varying      vec2 v_texCoord0;

uniform float     u_time;
uniform sampler2D u_texture;
uniform vec2      u_resolution;

void main() {
	LOWP vec4 vColor = v_color;
	LOWP vec4 texColor = texture2D(u_texture, v_texCoord0);
	float PI = 3.14159265358;
	vec2 uv = v_texCoord0.xy / u_resolution.xy;
	float screenRatio = u_resolution.x / u_resolution.y;

	LOWP vec3 color = vec3(
		cos(uv.x * 10. * PI * screenRatio) +
		cos(uv.y * 10. * PI) * abs(tan(u_time)));

	color.r += uv.x;
	color.g += uv.y;
	color.b += 1. - uv.y;

	gl_FragColor = vec4(color, 1.0);
}
