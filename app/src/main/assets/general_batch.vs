precision mediump float;
	
attribute vec4 aVertexColor;		
attribute vec3 aVertexPosition;
attribute vec2 aTextureCoord;
attribute float aTintWeight;
attribute uint aMatrixIndex;

uniform mat4 uMVMatrixArray[24];
uniform mat4 uPVMatrix;
  
varying vec2 vTextureCoord;
varying vec4 vColor;
varying float vTintWeight;

void main(void) {
	gl_Position = uPVMatrix * uMVMatrixArray[aMatrixIndex] * vec4(aVertexPosition, 1.0);
	vTextureCoord = aTextureCoord;
	vColor = aTextureColor;
	vTintWeight = aTintWeight;
}