precision mediump float;

uniform sampler2D uSampler;

varying vec2 vTextureCoord;
varying vec4 vColor;
varying float vTintWeight;

void main(void) {
	vec4 textel = texture2D(uSampler, vTextureCoord);
	gl_FragColor = vec4(mix(textel.rgb,vColor.rgb,vTintWeight),textel.a*vColor.a);
}