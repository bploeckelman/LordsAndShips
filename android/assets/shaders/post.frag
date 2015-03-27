#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoord0;

uniform float     u_pulse;
uniform sampler2D u_texture;

const float size = 1.0 / 512.0;

void main() {
	vec4 texColor = vec4(0.0);
	float pulse = u_pulse;

	texColor += texture2D(u_texture, vec2(v_texCoord0.s - 4.0 * size, v_texCoord0.t)) * 0.06;
	texColor += texture2D(u_texture, vec2(v_texCoord0.s - 3.0 * size, v_texCoord0.t)) * 0.09;
	texColor += texture2D(u_texture, vec2(v_texCoord0.s - 2.0 * size, v_texCoord0.t)) * 0.12;
	texColor += texture2D(u_texture, vec2(v_texCoord0.s - 1.0 * size, v_texCoord0.t)) * 0.15;

	texColor += texture2D(u_texture, vec2(v_texCoord0.s, v_texCoord0.t - 4.0 * size)) * 0.06;
	texColor += texture2D(u_texture, vec2(v_texCoord0.s, v_texCoord0.t - 3.0 * size)) * 0.09;
	texColor += texture2D(u_texture, vec2(v_texCoord0.s, v_texCoord0.t - 2.0 * size)) * 0.12;
	texColor += texture2D(u_texture, vec2(v_texCoord0.s, v_texCoord0.t - 1.0 * size)) * 0.15;

	texColor += texture2D(u_texture, v_texCoord0) * 0.16;

	texColor += texture2D(u_texture, vec2(v_texCoord0.s, v_texCoord0.t + 1.0 * size)) * 0.15;
	texColor += texture2D(u_texture, vec2(v_texCoord0.s, v_texCoord0.t + 2.0 * size)) * 0.12;
	texColor += texture2D(u_texture, vec2(v_texCoord0.s, v_texCoord0.t + 3.0 * size)) * 0.09;
	texColor += texture2D(u_texture, vec2(v_texCoord0.s, v_texCoord0.t + 4.0 * size)) * 0.06;

	texColor += texture2D(u_texture, vec2(v_texCoord0.s + 1.0 * size, v_texCoord0.t)) * 0.15;
	texColor += texture2D(u_texture, vec2(v_texCoord0.s + 2.0 * size, v_texCoord0.t)) * 0.12;
	texColor += texture2D(u_texture, vec2(v_texCoord0.s + 3.0 * size, v_texCoord0.t)) * 0.09;
	texColor += texture2D(u_texture, vec2(v_texCoord0.s + 4.0 * size, v_texCoord0.t)) * 0.06;

	gl_FragColor = v_color * texColor;
}

