// Fragment Shader for drawing
//

precision highp float;
precision highp int;

uniform sampler2D u_brushTexture;
varying vec4 destinationColor;

void main ( )
{
    gl_FragColor = texture2D(u_brushTexture, gl_PointCoord) * destinationColor;
}