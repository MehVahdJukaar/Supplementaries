#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

out vec4 fragColor;

#define T texture(DiffuseSampler,.5+(p.xy*=.992)).rgb

#define radialLength 0.8     //0.5 - 1.0
#define imageBrightness 10.0   //0 - 10
#define flareBrightness 3.5   // 0 - 10


void main() {

    vec3 p = vec3(texCoord, max(0.0, (imageBrightness/10.0)-0.5)) - 0.5;
    vec3 o = T;

    for (float i=0.0; i<100.0; i++)
    {
        p.z += pow(max(0.0, 0.5-length(T)), 10.0/flareBrightness) * exp(-i * (1.0-(radialLength)) );
    }

    vec3 flare = p.z * vec3(0.7, 0.5, 1.0); //tint

    fragColor = vec4(o+flare, 1.0);
}

