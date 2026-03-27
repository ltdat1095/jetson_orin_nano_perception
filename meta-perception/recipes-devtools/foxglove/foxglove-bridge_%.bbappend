SRC_URI += " \
    https://github.com/foxglove/foxglove-sdk/releases/download/sdk%2Fv0.14.2/foxglove-v0.14.2-cpp-aarch64-unknown-linux-gnu.zip;name=foxglove_sdk;subdir=foxglove-sdk-artifacts \
"
SRC_URI[foxglove_sdk.sha256sum] = "f54e3e08926a8882a9bc8031cfb24a040be2638f1ef4e87236fa3aded1ab2a23"

EXTRA_OECMAKE:append = " -DFETCHCONTENT_SOURCE_DIR_FOXGLOVE_SDK=${WORKDIR}/foxglove-sdk-artifacts/foxglove -DFETCHCONTENT_FULLY_DISCONNECTED=ON -DCMAKE_COMPILE_WARNING_AS_ERROR=OFF"
CXXFLAGS:append = " \
    -I${WORKDIR}/foxglove-sdk-artifacts/foxglove/include \
    -Wno-error=float-equal \
    -Wno-error=deprecated-declarations \
    -Wno-error=array-bounds \
    -Wno-error=stringop-overflow \
    -Wno-error=old-style-cast \
"