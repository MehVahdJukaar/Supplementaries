#version 330

#moj_import <light.glsl>
#moj_import <fog.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler1;
uniform sampler2D Sampler2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;

uniform vec3 Light0_Direction;
uniform vec3 Light1_Direction;

out float vertexDistance;

out vec4 lightMapColor;


out vec3 vertexPos;// position in view space for the fragment
out vec3 spherePos;// position of the sphere in clip
out mat3 sphereRot;// position of the sphere in clip

out vec3 rot;

vec3 decodeExtraVector(vec2 uv, ivec2 uv1) {
    // Combine the two 16-bit shorts into a 32-bit int
    int high = uv1.x & 0xFFFF;
    int low  = uv1.y & 0xFFFF;
    int bits = (high << 16) | low;

    // Convert bit pattern back to float
    float z = intBitsToFloat(bits);

    // Reconstruct original direction
    return vec3(uv, z);
}

mat3 eulerXYZToMatrix(vec3 euler) {
    float cx = cos(euler.x);
    float sx = sin(euler.x);
    float cy = cos(euler.y);
    float sy = sin(euler.y);
    float cz = cos(euler.z);
    float sz = sin(euler.z);

    // Combine the rotations: R = Rz * Ry * Rx
    return mat3(
    cy * cz,                      -cy * sz,                     sy,
    cz * sx * sy + cx * sz,       cx * cz - sx * sy * sz,      -cy * sx,
    -cx * cz * sy + sx * sz,      cz * sx + cx * sy * sz,       cx * cy
    );
}

void main() {

    vec4 posView = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * posView;

    vertexDistance = fog_distance(Position, FogShape);
    lightMapColor = texelFetch(Sampler2, UV2 / 16, 0);

    vertexPos = posView.xyz;

    vec3 extraVec = decodeExtraVector(UV0, UV1);

    spherePos = (ModelViewMat * vec4(Position + Normal, 1)).xyz;

    rot = extraVec;

    //this is the direction of my object
    sphereRot = eulerXYZToMatrix(extraVec);

}
