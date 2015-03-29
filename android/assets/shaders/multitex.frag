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
uniform sampler2D u_texture1;

void main() {
	float time = u_time;
	LOWP vec4 texColor0 = texture2D(u_texture,  v_texCoord0);
	LOWP vec4 texColor1 = texture2D(u_texture1, v_texCoord0);

	gl_FragColor = v_color * texColor0 * texColor1;
}
