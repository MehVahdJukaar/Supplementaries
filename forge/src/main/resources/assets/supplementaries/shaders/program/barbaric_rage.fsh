#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

out vec4 fragColor;

const vec3 c_r = vec3(0.9, 0.1, 0.1);
const vec3 c_g = vec3(0, 0.2, 0);
const vec3 c_b = vec3(0, 0, 0.2);

void main() {

    vec2 uv = texCoord;
    fragColor = texture( DiffuseSampler, uv );

    vec2 center = vec2(0.5);
    float dist = distance(uv, center);

    float edgeRadius = 0.; // Adjust the edge radius here
    float edgeRadiusEnd = 0.8;

    // Smoothstep to create a smooth gradient towards the edges
    float edgeGradient = smoothstep(edgeRadius, edgeRadiusEnd, dist);

    vec3 rgb = vec3( dot(fragColor.rgb,c_r), dot(fragColor.rgb,c_g), dot(fragColor.rgb,c_b) );

    // Apply the red filter based on the edge gradient
    vec3 resultColor = mix(rgb, fragColor.rgb, 1.-edgeGradient);
    fragColor = vec4( resultColor, 1.0 );
}
