#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;


uniform float GameTime;

uniform int NoiseSpeed;
uniform int NoiseScale;
uniform float Intensity;

in float vertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;
in vec4 normal;

out vec4 fragColor;

const float PHI = 1.61803398874989484820459; // Φ = Golden Ratio

float gold_noise(in vec2 xy, in float seed){
    return fract(tan(distance(xy * PHI, xy) * seed) * xy.x);
}

void main() {
    float seed = fract(GameTime*NoiseSpeed); // fractional base seed
    vec4 color = vec4 (
        gold_noise(texCoord0*NoiseScale, seed + 0.1), // r
        gold_noise(texCoord0*NoiseScale, seed + 0.2),  // g
        gold_noise(texCoord0*NoiseScale, seed + 0.3), // b
        gold_noise(texCoord0*NoiseScale, seed + 0.4));// α

    vec4 textColor = texture(Sampler0, texCoord0);

    textColor *= vertexColor * ColorModulator;
    textColor *= lightMapColor;
    textColor = linear_fog(textColor, vertexDistance, FogStart, FogEnd, FogColor);
    fragColor.rgb = mix(textColor.rgb, color.rgb, Intensity);
}



