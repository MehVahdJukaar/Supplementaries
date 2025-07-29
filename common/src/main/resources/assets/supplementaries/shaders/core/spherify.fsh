#version 150
#moj_import <fog.glsl>
#moj_import <light.glsl>

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

uniform vec3 Light0_Direction;
uniform vec3 Light1_Direction;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;

in float vertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;
in vec3 viewPos;   // sphere center in view space
in vec4 normal;

out vec4 fragColor;

void main() {
    // Compute fragment's view-space position
    vec2 quadUV = texCoord0 * 2.0 - 1.0;

    // You must know your quad size in world units (e.g. 2x2 = radius 1)
    float quadRadius = 1.0; // size of quad in world units (half extent)
    float sphereRadius = 0.5; // radius of the sphere in world units

    // Rebuild view-space fragment position from UV and quad size
    vec3 fragPosView = viewPos + vec3(quadUV * quadRadius, 0.0);

    // Ray origin = camera at (0,0,0), direction toward fragment
    vec3 rayOrigin = vec3(0.0);
    vec3 rayDir = normalize(fragPosView - rayOrigin);

    // Sphere center is viewPos (center of quad)
    vec3 sphereCenter = viewPos;
    vec3 oc = rayOrigin - sphereCenter;

    float a = dot(rayDir, rayDir);
    float b = 2.0 * dot(oc, rayDir);
    float c = dot(oc, oc) - sphereRadius * sphereRadius;
    float discriminant = b * b - 4.0 * a * c;

    if (discriminant < 0.0) discard;

    float t = (-b - sqrt(discriminant)) / (2.0 * a);
    if (t < 0.0) discard;

    // Calculate intersection point and normal at that point
    vec3 intersect = rayOrigin + t * rayDir;
    vec3 normal_view = normalize(intersect - sphereCenter);

    // Convert to world space using inverse rotation of view matrix
    mat3 viewRotMat = mat3(ModelViewMat);      // Extract rotation
    mat3 IViewRotMat = inverse(viewRotMat);    // Invert rotation

    vec3 normal_world = normalize(IViewRotMat * normal_view);

    // Texture color using spherical mapping or solid color
    vec4 baseColor = vec4(1.0); // solid white
    baseColor *= ColorModulator;

    vec4 litColor = minecraft_mix_light(Light0_Direction, Light1_Direction, normal_world, baseColor);
    litColor *= lightMapColor;

    fragColor = linear_fog(litColor, vertexDistance, FogStart, FogEnd, FogColor);
}
