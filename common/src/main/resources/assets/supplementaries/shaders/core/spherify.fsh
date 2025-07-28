#version 150
#moj_import <fog.glsl>
#moj_import <light.glsl>

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

uniform vec3 Light0_Direction;
uniform vec3 Light1_Direction;

in float vertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;
in vec3 viewPos;
in mat3 frag_IViewRotMat; // inverse of view rotation matrix

out vec4 fragColor;

void main() {
    vec2 pos = texCoord0 * 2.0 - 1.0;
    vec3 rayOrigin = vec3(0.0);

    float depthAdjustedRadius = viewPos.z * 0.15;
    vec3 pointOnPlane = vec3(pos * depthAdjustedRadius, viewPos.z);
    vec3 sphereCenter = vec3(0.0, 0.0, viewPos.z);
    vec3 rayDir = normalize(pointOnPlane - rayOrigin);

    vec3 oc = rayOrigin - sphereCenter;
    float a = dot(rayDir, rayDir);
    float b = 2.0 * dot(oc, rayDir);
    float c = dot(oc, oc) - depthAdjustedRadius * depthAdjustedRadius;

    float discriminant = b * b - 4.0 * a * c;
    if (discriminant < 0.0) discard;

    float t = (-b - sqrt(discriminant)) / (2.0 * a);
    if (t < 0.0) discard;

    vec3 intersectPos = rayOrigin + t * rayDir;
    vec3 sphereNormal = normalize(intersectPos - sphereCenter);

    // Convert normal to world space
    vec3 worldNormal = normalize(frag_IViewRotMat * sphereNormal);

    // Texture lookup (spherical UV mapping)
    float theta = atan(sphereNormal.y, sphereNormal.x);
    float phi = acos(sphereNormal.z);
    vec2 sphereUV = vec2(
    (theta + 3.14159265) / (2.0 * 3.14159265),
    phi / 3.14159265
    );

    // Sample base texture
    vec4 baseColor = texture(Sampler0, sphereUV);
    baseColor *= ColorModulator;

    // Apply lighting with correct world-space normal
    vec4 litColor = minecraft_mix_light(Light0_Direction, Light1_Direction, worldNormal, baseColor);
    litColor *= lightMapColor;

    // Fog
    fragColor = linear_fog(litColor, vertexDistance, FogStart, FogEnd, FogColor);
}
