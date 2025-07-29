#version 150
#moj_import <fog.glsl>
#moj_import <light.glsl>

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec2 ScreenSize;

uniform vec4 ColorModulator;
uniform vec3 Light0_Direction;
uniform vec3 Light1_Direction;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in vec4 spherePos; // Sphere center in view space
out vec4 fragColor;

void main() {
    float Radius = 0.25;

    vec3 sphereCenter = spherePos.xyz;

    // Reconstruct ray from camera through this fragment
    vec2 ndc = vec2(
    (gl_FragCoord.x / ScreenSize.x) * 2.0 - 1.0,
    (gl_FragCoord.y / ScreenSize.y) * 2.0 - 1.0
    );

    vec4 clipNear = vec4(ndc, -1.0, 1.0);
    vec4 clipFar  = vec4(ndc,  1.0, 1.0);

    mat4 invProj = inverse(ProjMat);
    vec4 viewNear = invProj * clipNear;
    vec4 viewFar  = invProj * clipFar;

    viewNear /= viewNear.w;
    viewFar  /= viewFar.w;

    vec3 rayOrigin = vec3(0.0);
    vec3 rayDir = normalize(viewFar.xyz - viewNear.xyz);

    vec3 oc = rayOrigin - sphereCenter;

    float b = dot(oc, rayDir);
    float c = dot(oc, oc) - Radius * Radius;
    float discriminant = b * b - c;

    if (discriminant < 0.0) discard;

    float sqrtDisc = sqrt(discriminant);
    float t = -b - sqrtDisc;
    if (t < 0.0) {
        t = -b + sqrtDisc;
        if (t < 0.0) discard;
    }

    vec3 intersect = rayOrigin + t * rayDir;
    vec3 normal_view = normalize(intersect - sphereCenter);

    mat3 viewRotMat = mat3(ModelViewMat);
    mat3 IViewRotMat = inverse(viewRotMat);

    vec3 normal_world = normalize(IViewRotMat * normal_view);

    vec4 baseColor = vec4(1.0);
    baseColor *= ColorModulator;

    vec4 litColor = minecraft_mix_light(Light0_Direction, Light1_Direction, normal_world, baseColor);

    // Optionally apply light map color or fog if you have those inputs:
    // litColor *= lightMapColor;
    // fragColor = linear_fog(litColor, vertexDistance, FogStart, FogEnd, FogColor);

    fragColor = litColor;
}
