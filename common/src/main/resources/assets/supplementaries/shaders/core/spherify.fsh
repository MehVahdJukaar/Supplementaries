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
in vec3 sphereDir;
in float vertexDistance;

in vec4 normal;
in vec4 normal2;
in vec4 normal3;

out vec4 fragColor;

const float Radius = 0.25;

const vec3 originalNorth = vec3(0.0, 0.0, 1.0); // where 'north' currently points

mat3 rotationBetweenVectors(vec3 from, vec3 to) {
    vec3 v = normalize(cross(from, to));
    float c = dot(from, to);
    float k = 1.0 / (1.0 + c);

    mat3 vx = mat3(
    0.0, -v.z, v.y,
    v.z, 0.0, -v.x,
    -v.y, v.x, 0.0
    );

    return mat3(1.0) + vx + vx * vx * k;
}

vec3 rebaseRelativeToBase(vec3 baseVec, vec3 vecToRebase) {
    // Step 1: Normalize the base vector to define new X axis
    vec3 xAxis = normalize(baseVec);

    // Step 2: Create an arbitrary vector to build a perpendicular Y axis
    // Pick a vector not parallel to xAxis
    vec3 arbitrary = abs(xAxis.x) < 0.99 ? vec3(1, 0, 0) : vec3(0, 1, 0);

    // Step 3: Build the Y axis (perpendicular to xAxis)
    vec3 yAxis = normalize(cross(xAxis, arbitrary));

    // Step 4: Build the Z axis (perpendicular to both X and Y)
    vec3 zAxis = cross(xAxis, yAxis);

    // Step 5: Express vecToRebase in the new basis
    return vec3(
    dot(vecToRebase, xAxis),
    dot(vecToRebase, yAxis),
    dot(vecToRebase, zAxis)
    );
}

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


// Map 3D normalized direction to UV in your 32x16 texture layout
vec2 cubeMapUV(vec3 dir) {
    // Absolute values of components
    vec3 absDir = abs(dir);

    // Determine major axis face
    float maxAxis = max(max(absDir.x, absDir.y), absDir.z);

    vec2 uv;
    vec2 faceUV; // local face UV in [0,1]
    ivec2 facePos; // position of the face block in the texture grid

    if (maxAxis == absDir.x) {
        // X major axis
        if (dir.x > 0.0) {
            // Positive X face = east
            facePos = ivec2(2,1); // east
            // Map yz to uv
            faceUV = vec2( ( -dir.z / absDir.x + 1.0 ) * 0.5, ( dir.y / absDir.x + 1.0 ) * 0.5 );
        } else {
            // Negative X face = west
            facePos = ivec2(0,1); // west
            // Map zy to uv (reverse z)
            faceUV = vec2( ( dir.z / absDir.x + 1.0 ) * 0.5, ( dir.y / absDir.x + 1.0 ) * 0.5 );
        }
    } else if (maxAxis == absDir.y) {
        // Y major axis
        if (dir.y > 0.0) {
            // Positive Y face = up
            facePos = ivec2(1,0); // up
            // Map xz to uv
            faceUV = vec2( ( dir.x / absDir.y + 1.0 ) * 0.5, ( -dir.z / absDir.y + 1.0 ) * 0.5 );
        } else {
            // Negative Y face = down
            facePos = ivec2(2,0); // down
            // Map xz to uv
            faceUV = vec2( ( dir.x / absDir.y + 1.0 ) * 0.5, ( dir.z / absDir.y + 1.0 ) * 0.5 );
        }
    } else {
        // Z major axis
        if (dir.z > 0.0) {
            // Positive Z face = south
            facePos = ivec2(3,1); // south
            // Map xy to uv
            faceUV = vec2( ( -dir.x / absDir.z + 1.0 ) * 0.5, ( dir.y / absDir.z + 1.0 ) * 0.5 );
        } else {
            // Negative Z face = north
            facePos = ivec2(1,1); // north
            // Map xy to uv
            faceUV = vec2( ( dir.x / absDir.z + 1.0 ) * 0.5, ( dir.y / absDir.z + 1.0 ) * 0.5 );
        }
    }

    // Convert block coords (in 8x8 blocks) to normalized UV on the whole texture
    // Each block is 8x8 pixels; total texture is 32x16 pixels (4 blocks wide x 2 blocks tall)
    float blockWidth = 8.0 / 32.0;
    float blockHeight = 8.0 / 16.0;

    uv = vec2(
    facePos.x * blockWidth + faceUV.x * blockWidth,
    facePos.y * blockHeight + faceUV.y * blockHeight
    );

    return uv;
}


void main() {
    // Screen -> NDC
    vec2 ndc = vec2((gl_FragCoord.x / ScreenSize.x) * 2.0 - 1.0,
    (gl_FragCoord.y / ScreenSize.y) * 2.0 - 1.0);

    // Clip space points at near and far planes
    vec4 clipNear = vec4(ndc, -1.0, 1.0);
    vec4 clipFar  = vec4(ndc,  1.0, 1.0);

    // Unproject to view space
    mat4 invProj = inverse(ProjMat);
    vec4 viewNear = invProj * clipNear;
    viewNear /= viewNear.w;
    vec4 viewFar  = invProj * clipFar;
    viewFar /= viewFar.w;

    vec3 rayOrigin = vec3(0.0);
    vec3 rayDir = normalize(viewFar.xyz - viewNear.xyz);

    float t = intersectSphere(rayOrigin, rayDir, spherePos, Radius);
    if (t < 0.0) discard;

    vec3 intersect = rayOrigin + t * rayDir;
    vec3 normal_view = normalize(intersect - spherePos);

    // Convert normal to world space by inverse rotation of ModelView
    vec3 normal_world = normalize(inverse(mat3(ModelViewMat)) * normal_view);

    vec3 rotatedNormal = rebaseRelativeToBase(normal3.xyz, normal_world);

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
