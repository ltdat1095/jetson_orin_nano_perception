SUMMARY = "DeepStream YOLO custom inference plugin"
DESCRIPTION = "Custom YOLO (v5/v7/v8) bbox parser and TensorRT plugin for NVIDIA DeepStream"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE.md;md5=336ebe1b52bd0d02c5ae40715cb00300"

SRC_URI = "git://github.com/marcoslucianops/DeepStream-Yolo.git;branch=master;protocol=https"
SRCREV = "${AUTOREV}"

SRC_URI += "file://0001-cross-compile-fix.patch"

S = "${WORKDIR}/git"
B = "${S}/nvdsinfer_custom_impl_Yolo"

inherit pkgconfig

# --------------------------------------------------
# Dependencies
# --------------------------------------------------
DEPENDS = "\
    deepstream-7.1 \
    tensorrt-core \
    cuda-cudart \
"

# --------------------------------------------------
# Makefile integration
# --------------------------------------------------
EXTRA_OEMAKE += "\
    CC='${CC}' \
    CXX='${CXX}' \
    LD='${LD}' \
    SYSROOT='${STAGING_DIR_TARGET}' \
    CUDA_VER=12.6 \
    OPENCV=0 \
    GRAPH=0 \
    CFLAGS='${CFLAGS} \
        -I${STAGING_DIR_TARGET}/usr/local/cuda-12.6/include \
        -I${STAGING_DIR_TARGET}/opt/nvidia/deepstream/deepstream-7.1/sources/includes' \
    CXXFLAGS='${CXXFLAGS} \
        -I${STAGING_DIR_TARGET}/usr/local/cuda-12.6/include \
        -I${STAGING_DIR_TARGET}/opt/nvidia/deepstream/deepstream-7.1/sources/includes' \
    CUDA_INC='\
        -I${STAGING_DIR_TARGET}/usr/include \
        -I${STAGING_DIR_TARGET}/usr/local/cuda-12.6/include \
        -I${STAGING_DIR_TARGET}/opt/nvidia/deepstream/deepstream-7.1/sources/includes' \
    LDFLAGS='${LDFLAGS} \
        -L${STAGING_DIR_TARGET}/opt/nvidia/deepstream/deepstream-7.1/lib' \
"

# --------------------------------------------------
# Build
# --------------------------------------------------
do_compile() {
    oe_runmake -C ${B}
}

# --------------------------------------------------
# Install
# --------------------------------------------------
do_install() {
    install -d ${D}${libdir}/deepstream
    install -m 0755 ${B}/libnvdsinfer_custom_impl_Yolo.so \
        ${D}${libdir}/deepstream/
}

FILES:${PN} += "${libdir}/deepstream/libnvdsinfer_custom_impl_Yolo.so"

INSANE_SKIP:${PN} += "ldflags dev-so"
