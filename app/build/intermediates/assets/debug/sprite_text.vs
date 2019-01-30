precision mediump float;
			
attribute vec3 aVertexPosition;
attribute vec2 aTextureCoord;

uniform mat4 uMVMatrix;
uniform mat4 uPMatrix;

uniform float uXTextureOffset;
uniform float uYTextureOffset;
  
varying vec2 vTextureCoord;

void main(void) {
	gl_Position = uPMatrix * uMVMatrix * vec4(aVertexPosition, 1.0);
	vTextureCoord = vec2(uXTextureOffset,uYTextureOffset)+aTextureCoord;
}