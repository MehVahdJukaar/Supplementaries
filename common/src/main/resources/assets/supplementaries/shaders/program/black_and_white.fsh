#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

out vec4 fragColor;


//Black & White Matrix
const vec3 c_r = vec3(0.3, 0.6, 0.1);
const vec3 c_g = vec3(0.3, 0.6, 0.1);
const vec3 c_b = vec3(0.3, 0.6, 0.1);

void main() {

    vec2 uv = texCoord;
    vec4 col = texture( DiffuseSampler, uv );


    vec3 rgb = vec3( dot(col.rgb,c_r), dot(col.rgb,c_g), dot(col.rgb,c_b) );

    fragColor = vec4( rgb, 1.0 );
}
