// Fragment Shader for background drawing
//

precision mediump float;
uniform sampler2D u_texture;
varying vec2 textureCoordinate;

void main()
{
     vec4 outputColor = texture2D(u_texture, textureCoordinate);
     outputColor.a *= 0.5;
     outputColor.rgb *= 0.5;
     gl_FragColor = outputColor;
}