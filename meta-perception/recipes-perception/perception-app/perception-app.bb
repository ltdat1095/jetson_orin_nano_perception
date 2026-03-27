SUMMARY = "Perception App for Jetson"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "gitsm://github.com/ltdat1095/jetson_perception;protocol=https;branch=master"
SRCREV = "296d0fd6c701711b7cd54cfff02afbafde212354"

S = "${WORKDIR}/git"

DEPENDS = " \
    python3-colcon-common-extensions-native \
    python3-colcon-ros-native \
    python3-vcstool-native \
    python3-setuptools-native \
    python3-numpy-native \
    python3-numpy \
    git-native \
    cmake-native \
    pkgconfig-native \
    ament-cmake \
    ament-cmake-native \
    ament-package-native \
    rosidl-default-generators-native \
    rosidl-default-generators \
    rosidl-default-runtime \
    rosidl-adapter-native \
    rclcpp \
    rmw-implementation \
    sensor-msgs \
    std-msgs \
    builtin-interfaces \
    gstreamer1.0 \
    gstreamer1.0-plugins-base \
    cuda-cudart \
    tegra-mmapi \
    deepstream-7.1 \
"

ROS_BUILDTOOL_DEPENDS = " \
    ament-cmake-native \
    ament-package-native \
    ament-cmake-auto-native \
    rosidl-default-generators-native \
    rosidl-adapter-native \
    python3-colcon-common-extensions-native \
    python3-vcstool-native \
    python3-colcon-ros-native \
    python3-setuptools-native \
    python3-numpy-native \
    git-native \
    cmake-native \
"

# This repo has multiple ROS 2 packages and no single top-level CMake project.
do_configure() {
    :
}

do_compile() {
    rm -rf ${S}/build ${S}/install ${S}/log

    export COLCON_HOME=${WORKDIR}/colcon
    # Avoid inheriting host/default colcon settings (e.g. symlink-install)
    # that produce non-relocatable install trees in Yocto packages.
    export COLCON_DEFAULTS_FILE=/dev/null
    export ROS_NATIVE_PREFIX=${STAGING_DIR_NATIVE}/opt/ros/humble

    export AMENT_PREFIX_PATH="${STAGING_DIR_TARGET}${prefix}:${STAGING_DIR_NATIVE}${prefix}:${STAGING_DIR_TARGET}/opt/ros/humble:${ROS_NATIVE_PREFIX}"
    export CMAKE_PREFIX_PATH="${AMENT_PREFIX_PATH}"
    export PYTHONPATH="${STAGING_DIR_NATIVE}${libdir}/${PYTHON_DIR}/site-packages:${ROS_NATIVE_PREFIX}/lib/${PYTHON_DIR}/site-packages:${PYTHONPATH}"
    export SDKTARGETSYSROOT=${STAGING_DIR_TARGET}
    export PKG_CONFIG="${STAGING_BINDIR_NATIVE}/pkg-config"

    colcon build \
        --build-base ${S}/build \
        --install-base ${S}/install \
        --base-paths ${S}/src \
        --packages-select perception_interfaces live_sensors_node inference_node \
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
    install -d ${D}${prefix}/perception-app
    # Copy as real files, not symlinks, so rosidl-generated Python modules
    # and type-support libs are present in the target rootfs.
    cp -rL --no-preserve=ownership ${S}/install/. ${D}${prefix}/perception-app/
}

FILES:${PN} = "${prefix}/perception-app"
INSANE_SKIP:${PN} += "already-stripped"
