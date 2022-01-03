// Vertex Shader for drawing
//

precision highp float;

attribute vec4 position;
//attribute float thickness;
//attribute float opacity;

uniform vec4 u_color;
uniform mat4 u_MVPMatrix;

varying vec4 destinationColor;

void main(void) {

    destinationColor = vec4(u_color.rgb * position.w, position.w);;//vec4(1.0, 0.0, 0.0, 1.0);// vec4(u_color.rgb * position.w, position.w);
    gl_Position = u_MVPMatrix * vec4(position.x,position.y, 0.0, 1.0);
//    gl_PointSize = 6.0;
    gl_PointSize = position.z;
}