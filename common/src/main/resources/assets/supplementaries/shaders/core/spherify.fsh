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
uniform sampler2D Sampler0;

in vec4 lightMapColor;

in vec3 spherePos;
in mat3 sphereRot;
in float vertexDistance;
in vec3 rot;

out vec4 fragColor;

const float Radius = 0.25;

// Returns intersection distance 't' along ray or -1 if no hit
float intersectSphere(vec3 rayOrigin, vec3 rayDir, vec3 center, float radius) {
    vec3 oc = rayOrigin - center;
    float b = dot(oc, rayDir);
    float c = dot(oc, oc) - radius * radius;
    float disc = b * b - c;
    if (disc < 0.0) return -1.0;
    float t = -b - sqrt(disc);
    return (t >= 0.0) ? t : (-b + sqrt(disc) >= 0.0 ? -b + sqrt(disc) : -1.0);
}

// maps a direction vector to UV coordinates on a cubemap atlas
vec2 cubeMapUV(vec3 direction) {
    float x = direction.x;
    float y = direction.y;
    float z = direction.z;

    float abs_x = abs(x);
    float abs_y = abs(y);
    float abs_zf = abs(-z); // north = -z

    float max_axis;
    float uc, vc;
    float u, v;
    vec2 base_uv;

    if (abs_x >= abs_y && abs_x >= abs_zf) {
        max_axis = abs_x;
        if (x < 0.0) {
            // West → leftmost cell in bottom row
            uc = z;
            vc = -y;
            u = 0.5 * (uc / max_axis + 1.0);
            v = 0.5 * (vc / max_axis + 1.0);
            base_uv = vec2(2.0 / 4.0, 1.0 / 2.0);
        } else {
            // East → 3rd cell in bottom row
            uc = -z;
            vc = -y;
            u = 0.5 * (uc / max_axis + 1.0);
            v = 0.5 * (vc / max_axis + 1.0);
            base_uv = vec2(0.0 / 4.0, 1.0 / 2.0);
        }
    } else if (abs_y >= abs_x && abs_y >= abs_zf) {
        max_axis = abs_y;
        if (y > 0.0) {
            // Up → 2nd cell in top row
            uc = -x;
            vc = -z;
            u = 0.5 * (uc / max_axis + 1.0);
            v = 0.5 * (vc / max_axis + 1.0);
            base_uv = vec2(1.0 / 4.0, 0.0 / 2.0);
        } else {
            // Down → 3rd cell in top row
            uc = -x;
            vc = -z;
            u = 0.5 * (uc / max_axis + 1.0);
            v = 0.5 * (vc / max_axis + 1.0);
            base_uv = vec2(2.0 / 4.0, 0.0 / 2.0);
        }
    } else {
        max_axis = abs_zf;
        if (-z > 0.0) {
            // North → 2nd cell in bottom row
            uc = -x;
            vc = -y;
            u = 0.5 * (uc / max_axis + 1.0);
            v = 0.5 * (vc / max_axis + 1.0);
            base_uv = vec2(1.0 / 4.0, 1.0 / 2.0);
        } else {
            // South → 4th cell in bottom row
            uc = x;
            vc = -y;
            u = 0.5 * (uc / max_axis + 1.0);
            v = 0.5 * (vc / max_axis + 1.0);
            base_uv = vec2(3.0 / 4.0, 1.0 / 2.0);
        }
    }

    // Scale u,v from local face space (0–1) to global atlas space
    vec2 final_uv = base_uv + vec2(u, v) * vec2(1.0 / 4.0, 1.0 / 2.0);
    return final_uv;
}

void main() {
    // Screen -> NDC
    vec2 ndc = vec2((gl_FragCoord.x / ScreenSize.x) * 2.0 - 1.0,
    (gl_FragCoord.y / ScreenSize.y) * 2.0 - 1.0);

    // Clip space points at near and far planes
    vec4 clipNear = vec4(ndc, -1.0, 1.0);
    vec4 clipFar  = vec4(ndc,  1.0, 1.0);

    // Unproject to view space
    //problem must be somewhere here
    mat4 invProj = inverse(ProjMat);
    vec4 viewNear = invProj * clipNear;
    viewNear /= viewNear.w;
    vec4 viewFar  = invProj * clipFar;
    viewFar /= viewFar.w;

    vec3 rayOrigin = vec3(0.0).xyz;
    vec3 rayDir = normalize(viewFar.xyz - viewNear.xyz);

    float t = intersectSphere(rayOrigin, rayDir, spherePos, Radius);
    if (t < 0.0) discard;

    vec3 intersect = rayOrigin + t * rayDir;
    vec3 normal_view = normalize(intersect - spherePos);

    // Convert normal to world space by inverse rotation of ModelView
    vec3 normal_world = normalize(inverse(mat3(ModelViewMat)) * normal_view);

    vec3 rotatedNormal = sphereRot *  normal_world;

    vec2 uv = cubeMapUV(rotatedNormal);
    vec4 baseColor = texture(Sampler0, uv);

    baseColor *= minecraft_mix_light(Light0_Direction, Light1_Direction, normal_world, vec4(1.0,1.0,1.0,1.0));
    baseColor *= ColorModulator;
    baseColor *= lightMapColor;
    baseColor = linear_fog(baseColor, vertexDistance, FogStart, FogEnd, FogColor);

    fragColor = baseColor;
    // Optional fog, comment in if you want


    //distance from sphere = color
    // fragColor = vec4(1.0 - length(intersect - spherePos) / Radius, 0.0, 0.0, 1.0);
}
