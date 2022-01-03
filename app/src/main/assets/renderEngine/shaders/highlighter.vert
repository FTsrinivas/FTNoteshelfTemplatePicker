// Vertex Shader for texture drawing
//

uniform mat4 u_MVPMatrix; //model/view/projection matrix.

attribute vec4 inVertex; //Per-vertex position information we will pass in.
attribute vec2 inTextureCoordinate;// Per-vertex texture coordinate information we will pass in.

varying vec2 textureCoordinate; //This will be passed into the fragment shader.

void main()
{
    // Pass through the texture coordinate.
    textureCoordinate = inTextureCoordinate;

    // multiply the vertex by the matrix to get the normalized screen coordinates.
    gl_Position =  u_MVPMatrix * inVertex;
}