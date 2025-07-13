#version 150

uniform sampler2D DiffuseSampler;
in vec2 texCoord;
in vec2 oneTexel;
out vec4 fragColor;

// Iron tones with rustier shadows
const vec3 ironPalette[4] = vec3[](
vec3(0.25, 0.15, 0.10), // Rust shadow (warm brown-red)
vec3(0.45, 0.28, 0.24), // Mid rust iron (reddish cast)
vec3(0.65, 0.55, 0.52), // Warm metallic midtone
vec3(0.85, 0.80, 0.78)  // Matte iron highlight
);

// Simple pseudo-random metallic noise
float noise(vec2 uv) {
    return fract(sin(dot(uv, vec2(12.9898, 78.233))) * 43758.5453);
}

float luminance(vec3 c) {
    return dot(c, vec3(0.299, 0.587, 0.114));
}

// Palette blend based on luminance
vec3 blendToIron(float lum) {
    if (lum < 0.3)
    return mix(ironPalette[0], ironPalette[1], smoothstep(0.0, 0.3, lum));
    else if (lum < 0.6)
    return mix(ironPalette[1], ironPalette[2], smoothstep(0.3, 0.6, lum));
    else
    return mix(ironPalette[2], ironPalette[3], smoothstep(0.6, 1.0, lum));
}

void main() {
    vec3 color = texture(DiffuseSampler, texCoord).rgb;
    float lum = luminance(color);

    // Base iron color mapping
    vec3 ironColor = blendToIron(lum);

    // Apply metallic noise
    float grain = noise(texCoord * 400.0); // dense metal speckle
    ironColor += (grain - 0.5) * 0.05; // subtle roughness

    // Slight brightness boost
    ironColor *= 1.1;

    fragColor = vec4(clamp(ironColor, 0.0, 1.0), 1.0);
}
