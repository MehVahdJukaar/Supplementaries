#version 150
#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;
in vec3 viewPos;  // View-space position

out vec4 fragColor;

void main() {
    // Convert texture coordinates to normalized device coordinates [-1, 1]
    vec2 pos = texCoord0 * 2.0 - 1.0;

    // Camera position (view space origin)
    vec3 rayOrigin = vec3(0.0);

    // Calculate true world position on billboard plane
    // Depth-scaled radius keeps apparent size consistent
    float depthAdjustedRadius = viewPos.z * 0.2; // Adjust scale factor as needed
    vec3 pointOnPlane = vec3(pos * depthAdjustedRadius, viewPos.z);

    // Sphere center aligned with billboard depth
    vec3 sphereCenter = vec3(0.0, 0.0, viewPos.z);

    // Ray direction from camera through billboard point
    vec3 rayDir = normalize(pointOnPlane - rayOrigin);

    // Calculate intersection parameters
    vec3 oc = rayOrigin - sphereCenter;
    float a = dot(rayDir, rayDir);
    float b = 2.0 * dot(oc, rayDir);
    float c = dot(oc, oc) - depthAdjustedRadius * depthAdjustedRadius; // Use same scaled radius

    float discriminant = b*b - 4.0*a*c;
    if (discriminant < 0.0) discard;

    float t = (-b - sqrt(discriminant)) / (2.0 * a);
    if (t < 0.0) discard;

    // Calculate intersection point and normal
    vec3 intersectPos = rayOrigin + t * rayDir;
    vec3 sphereNormal = normalize(intersectPos - sphereCenter);

    // Convert normal to spherical coordinates
    float theta = atan(sphereNormal.y, sphereNormal.x);
    float phi = acos(sphereNormal.z);

    // Generate UV coordinates
    vec2 sphereUV = vec2(
    (theta + 3.14159265) / (2.0 * 3.14159265),
    phi / 3.14159265
    );

    // Sample texture and apply lighting
    vec4 textColor = texture(Sampler0, sphereUV);
    textColor *= ColorModulator;
    textColor *= lightMapColor;

    // Apply fog
    fragColor = linear_fog(textColor, vertexDistance, FogStart, FogEnd, FogColor);
}