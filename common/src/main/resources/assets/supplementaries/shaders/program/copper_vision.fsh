#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

out vec4 fragColor;

// Updated Copper Palette (8 shades)
const vec3 copperPalette[8] = vec3[](
vec3(0.78, 0.42, 0.16), // Base Copper
vec3(0.68, 0.34, 0.22), // Muted Mid Copper (less saturated)
vec3(0.52, 0.26, 0.18), // Dark Burnt Copper
vec3(0.40, 0.24, 0.18), // Bridging Tone (low red)
vec3(0.36, 0.38, 0.28), // Shadow Olive (sand green)
vec3(0.88, 0.72, 0.36), // Brass Highlight
vec3(1.00, 0.82, 0.50), // Warm Light Highlight
vec3(1.00, 0.90, 0.65)  // Brightest Highlight
);

float luminance(vec3 c) {
    return dot(c, vec3(0.299, 0.587, 0.114));
}

float edgeDetect(vec2 uv) {
    vec2 dx = vec2(oneTexel.x, 0.0);
    vec2 dy = vec2(0.0, oneTexel.y);

    float tl = luminance(texture(DiffuseSampler, uv - dx - dy).rgb);
    float  t = luminance(texture(DiffuseSampler, uv - dy).rgb);
    float tr = luminance(texture(DiffuseSampler, uv + dx - dy).rgb);
    float  l = luminance(texture(DiffuseSampler, uv - dx).rgb);
    float  r = luminance(texture(DiffuseSampler, uv + dx).rgb);
    float bl = luminance(texture(DiffuseSampler, uv - dx + dy).rgb);
    float  b = luminance(texture(DiffuseSampler, uv + dy).rgb);
    float br = luminance(texture(DiffuseSampler, uv + dx + dy).rgb);

    float gx = -tl - 2.0 * l - bl + tr + 2.0 * r + br;
    float gy = -tl - 2.0 * t - tr + bl + 2.0 * b + br;

    return length(vec2(gx, gy));
}

vec3 softBlendToPalette(vec3 color) {
    float totalWeight = 0.0;
    vec3 blended = vec3(0.0);

    for (int i = 0; i < 8; ++i) {
        float dist = distance(color, copperPalette[i]);
        float weight = 1.0 / (dist * dist + 0.01);
        blended += copperPalette[i] * weight;
        totalWeight += weight;
    }

    blended /= totalWeight;
    return mix(color, blended, 0.9); // retain some original texture
}

vec3 applyContrast(vec3 color, float amount) {
    return clamp((color - 0.5) * amount + 0.5, 0.0, 1.0);
}

void main() {
    vec2 uv = texCoord;
    vec3 origColor = texture(DiffuseSampler, uv).rgb;

    vec3 copperColor = softBlendToPalette(origColor);

    float edgeStrength = 0.25 * edgeDetect(uv);

    float shadow = smoothstep(0.18, 0.32, edgeStrength);
    float highlight = smoothstep(0.08, 0.22, edgeStrength);

    // Apply only subtle shadow green now
    copperColor = mix(copperColor, copperPalette[4], shadow * 0.3);
    copperColor = mix(copperColor, copperPalette[7], highlight * 0.35);

    copperColor += 0.08; // brighten slightly
    copperColor = applyContrast(copperColor, 1.4);

    fragColor = vec4(copperColor, 1.0);
}
