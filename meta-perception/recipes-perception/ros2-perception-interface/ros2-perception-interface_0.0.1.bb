SUMMARY = "ROS 2 perception interface messages"
DESCRIPTION = "Custom ROS 2 interface package for perception messages"
HOMEPAGE = "https://github.com/ltdat1095/ros2_perception_interface"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://package.xml;beginline=10;endline=10;md5=82f0323c08605e5b6f343b05213cf7cc"

SRC_URI = "git://github.com/ltdat1095/ros2_perception_interface;protocol=https;branch=Test-new-structure"
SRCREV = "792c8bd444796730c3607851d878742fab3801e0"

S = "${WORKDIR}/git"

ROS_DISTRO ?= "humble"
ROS_INSTALL_PREFIX = "/opt/ros/${ROS_DISTRO}"

DEPENDS = " \
    python3-colcon-common-extensions-native \
    python3-colcon-ros-native \
    python3-setuptools-native \
    python3-numpy-native \
    python3-numpy \
    cmake-native \
    pkgconfig-native \
    ament-cmake \
    ament-cmake-native \
    ament-package-native \
    rosidl-default-generators-native \
    rosidl-default-generators \
    rosidl-default-runtime \
    rosidl-adapter-native \
    builtin-interfaces \
"

inherit cmake python3native

# This recipe builds with colcon instead of a single top-level CMake configure step.
do_configure() {
    :
}

do_compile() {
    rm -rf ${S}/build ${S}/install ${S}/log

    export COLCON_HOME=${WORKDIR}/colcon
    # Avoid inheriting host/default colcon settings (e.g. symlink-install).
    export COLCON_DEFAULTS_FILE=/dev/null
    export ROS_NATIVE_PREFIX=${STAGING_DIR_NATIVE}${ROS_INSTALL_PREFIX}

    export AMENT_PREFIX_PATH="${STAGING_DIR_TARGET}${prefix}:${STAGING_DIR_NATIVE}${prefix}:${STAGING_DIR_TARGET}${ROS_INSTALL_PREFIX}:${ROS_NATIVE_PREFIX}"
    export CMAKE_PREFIX_PATH="${AMENT_PREFIX_PATH}"
    export PYTHONPATH="${STAGING_DIR_NATIVE}${libdir}/${PYTHON_DIR}/site-packages:${ROS_NATIVE_PREFIX}/lib/${PYTHON_DIR}/site-packages:${PYTHONPATH}"
    export SDKTARGETSYSROOT=${STAGING_DIR_TARGET}
    export PKG_CONFIG="${STAGING_BINDIR_NATIVE}/pkg-config"

    colcon build \
        --build-base ${S}/build \
        --install-base ${S}/install \
        --base-paths ${S} \
        --packages-select perception_interfaces \
        --event-handlers console_direct+ \
        --cmake-args \
            -DCMAKE_BUILD_TYPE=Release \
            -DBUILD_TESTING=OFF \
            -DCMAKE_TOOLCHAIN_FILE=${WORKDIR}/toolchain.cmake \
            -DCMAKE_SYSROOT=${STAGING_DIR_TARGET} \
            -DPYTHON_SOABI=cpython-312-aarch64-linux-gnu \
            -DPython3_SOABI=cpython-312-aarch64-linux-gnu \
            -DCMAKE_FIND_ROOT_PATH="${STAGING_DIR_TARGET};${STAGING_DIR_NATIVE}" \
            -Wno-dev
}

do_install() {
    install -d ${D}${ROS_INSTALL_PREFIX}
    # Copy as real files, not symlinks, so generated modules and type-support libs are present.
    cp -rL --no-preserve=ownership ${S}/install/perception_interfaces/. ${D}${ROS_INSTALL_PREFIX}/
}

FILES:${PN} += "${ROS_INSTALL_PREFIX}"
RDEPENDS:${PN} += "rosidl-default-runtime builtin-interfaces"
INSANE_SKIP:${PN} += "already-stripped"
