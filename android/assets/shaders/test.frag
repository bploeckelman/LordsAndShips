#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoord0;

uniform float     u_time;
uniform sampler2D u_texture;
uniform sampler2D u_texture1;

void main() {
	vec4 texColor0 = texture2D(u_texture, v_texCoord0);
	vec4 texColor1 = texture2D(u_texture1, v_texCoord0);

	gl_FragColor = vec4(v_color.r, abs(sin(u_time)), v_color.b, v_color.a) * texColor0 * texColor1;
}
