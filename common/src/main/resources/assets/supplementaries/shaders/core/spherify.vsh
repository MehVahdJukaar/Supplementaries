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
out vec4 normal;
out vec4 normal2;
out vec4 normal3;

out vec3 vertexPos;   // position in view space for the fragment
out vec3 spherePos; // position of the sphere in clip
out vec3 sphereDir; // position of the sphere in clip


vec3 decodeDir(vec2 uv, ivec2 uv1) {
    // Combine the two 16-bit shorts into a 32-bit int
    int high = uv1.x & 0xFFFF;
    int low  = uv1.y & 0xFFFF;
    int bits = (high << 16) | low;

    // Convert bit pattern back to float
    float z = intBitsToFloat(bits);

    // Reconstruct original direction
    return vec3(uv, z);
}


void main() {

    vec4 posView = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * posView;

    vertexDistance = fog_distance(Position, FogShape);
    lightMapColor = texelFetch(Sampler2, UV2 / 16, 0);

    vertexPos = posView.xyz;

    spherePos = (ModelViewMat * vec4(decodeDir(UV0, UV1),1)).xyz;

    //this is the direction of my object
    sphereDir =  (     ModelViewMat * vec4(Normal, 1)).xyz;

    normal = ProjMat * ModelViewMat * vec4(Normal, 1.0);
    normal2 =   ModelViewMat * vec4(Normal, 1.0);
    normal3 =    vec4(Normal, 1.0);
}
