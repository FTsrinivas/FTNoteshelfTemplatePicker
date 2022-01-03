// Fragment Shader for background drawing
//

precision mediump float;
uniform sampler2D u_texture;
varying vec2 textureCoordinate;

void main()
{
     gl_FragColor = texture2D(u_texture, textureCoordinate);
}