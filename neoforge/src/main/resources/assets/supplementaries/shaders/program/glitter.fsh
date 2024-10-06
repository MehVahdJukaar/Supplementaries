#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

out vec4 fragColor;

#define intensity 4.5// Default is 1.0
#define yellowTarget vec3(1.0, 0.95, 0.0)// The yellow color target

float colorDistance(vec3 color, vec3 target) {
    return length(color - target);
}

vec3 RGBtoHCV(in vec3 rgb)
{
    // RGB [0..1] to Hue-Chroma-Value [0..1]
    // Based on work by Sam Hocevar and Emil Persson
    vec4 p = (rgb.g < rgb.b) ? vec4(rgb.bg, -1., 2. / 3.) : vec4(rgb.gb, 0., -1. / 3.);
    vec4 q = (rgb.r < p.x) ? vec4(p.xyw, rgb.r) : vec4(rgb.r, p.yzx);
    float c = q.x - min(q.w, q.y);
    float h = abs((q.w - q.y) / (6. * c + 0.00001) + q.z);
    return vec3(h, c, q.x);
}

float hueDistance(vec3 color, vec3 target) {
    vec3 hsvColor = RGBtoHCV(color);
    vec3 hsvTarget = RGBtoHCV(target);

    // Calculate hue distance with circular wrapping
    float hueDist = abs(hsvColor.x - hsvTarget.x);
    hueDist = min(hueDist, 1.0 - hueDist);// Wrap around

    // Calculate saturation and value differences
    float saturationDistance = abs(hsvColor.y - hsvTarget.y);
    float valueDistance = abs(hsvColor.z - hsvTarget.z);

    return (hueDist + (valueDistance*0.35) + (saturationDistance*0.35))/ (1.+0.35+0.35);// Ensure result is between 0 and 1
}

float yellowFactor(vec3 color) {
    // Invert the distance so that pixels closer to yellow have higher values
    float dist = hueDistance(color, yellowTarget);
    return smoothstep(0.77, 0.90, 1.0 - dist);// A smooth transition for colors close to yellow
}

vec4 blend(in vec2 Coord, in sampler2D Tex, in float MipBias){
    vec2 TexelSize = MipBias * oneTexel;
    vec4 Color = texture(Tex, Coord);
    float totalFactor = yellowFactor(Color.rgb);
    vec4 totalColor = Color * totalFactor;

    // Take 6 samples from the texture and accumulate yellowFactor
    float maxSamples =12.;
    for (float i = 1.; i <= maxSamples; i += 1.)
    {
        float inv = 1. / i;
        vec4 sample1 = texture(Tex, Coord + vec2(TexelSize.x, TexelSize.y) * inv);
        vec4 sample2 = texture(Tex, Coord + vec2(-TexelSize.x, TexelSize.y) * inv);
        vec4 sample3 = texture(Tex, Coord + vec2(TexelSize.x, -TexelSize.y) * inv);
        vec4 sample4 = texture(Tex, Coord + vec2(-TexelSize.x, -TexelSize.y) * inv);

        float factor1 = yellowFactor(sample1.rgb);
        float factor2 = yellowFactor(sample2.rgb);
        float factor3 = yellowFactor(sample3.rgb);
        float factor4 = yellowFactor(sample4.rgb);

        totalColor += sample1 * factor1 + sample2 * factor2 + sample3 * factor3 + sample4 * factor4;
    }

    return totalColor / (4.*maxSamples);
}

void main() {
    vec2 uv = texCoord;
    vec4 Color = texture(DiffuseSampler, uv);

    // Get the bloom effect from the blend function
    vec4 blended = blend(uv, DiffuseSampler, 65.0);

    // Clamp the result and apply bloom intensity
    vec4 highlight = clamp(blended * intensity, 0.0, 1.0);

    fragColor = 1.0 - (1.0 - Color) * (1.0 - highlight);
    fragColor.a = 1.0;
}
