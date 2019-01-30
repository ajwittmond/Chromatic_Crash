precision mediump float;
	
attribute vec4 aVertexColor;		
attribute vec3 aVertexPosition;
attribute vec2 aTextureCoord;
attribute float aTintWeight;

uniform mat4 uMVMatrix;
uniform mat4 uPVMatrix;
  
varying vec2 vTextureCoord;
varying vec4 vColor;
varying float vTintWeight;

void main(void) {
	gl_Position = uPVMatrix * uMVMatrix * vec4(aVertexPosition, 1.0);
	vTextureCoord = aTextureCoord;
	vColor = aVertexColor;
	vTintWeight = aTintWeight;
}