SUMMARY = "Perception App for Jetson"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "gitsm://github.com/ltdat1095/jetson_perception;protocol=https;branch=master"
SRCREV = "${AUTOREV}"

S = "${WORKDIR}/git"

# Define dependency lists according to standard ROS superflore structure
ROS_BUILD_DEPENDS = " \
    rclcpp \
    sensor-msgs \
    std-msgs \
    builtin-interfaces \
    gstreamer1.0 \
    gstreamer1.0-plugins-base \
    cuda-cudart \
    tegra-mmapi \
    deepstream-7.1 \
    fastcdr \
    rcutils \
    rcpputils \
    libyaml-vendor \
    rmw-implementation \
    rmw-implementation-cmake \
    rmw-fastrtps-cpp \
    python3-numpy \
    ament-cmake \
    rosidl-default-generators \
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

ROS_EXPORT_DEPENDS = " \
    rclcpp \
    sensor-msgs \
    std-msgs \
    builtin-interfaces \
"

ROS_BUILDTOOL_EXPORT_DEPENDS = ""

ROS_EXEC_DEPENDS = " \
    rosidl-default-runtime \
    rclcpp \
    sensor-msgs \
    std-msgs \
    builtin-interfaces \
"

ROS_TEST_DEPENDS = ""

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
DEPENDS += "${ROS_EXPORT_DEPENDS} ${ROS_BUILDTOOL_EXPORT_DEPENDS}"
RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

ROS_BUILD_TYPE = "ament_cmake"

inherit ros_distro_humble
inherit ros_superflore_generated
inherit ros_${ROS_BUILD_TYPE}
inherit python3native

# Disable default do_configure as there is no root CMakeLists.txt
do_configure() {
    :
}

# Use manual colcon build since this is a workspace-like structure
do_compile() {
    # Clean previous build to avoid caching issues
    rm -rf ${S}/build ${S}/install ${S}/log
    
    export COLCON_HOME=${WORKDIR}/colcon
    
    # Explicitly define ROS prefix 
    export ROS_PREFIX="/opt/ros/${ROS_DISTRO}"
    
    # Export standard ROS build environment variables
    # We include both standard prefix (/usr) and ROS_PREFIX path
    export AMENT_PREFIX_PATH=${STAGING_DIR_TARGET}${prefix}:${STAGING_DIR_NATIVE}${prefix}:${STAGING_DIR_TARGET}${ROS_PREFIX}:${STAGING_DIR_NATIVE}${ROS_PREFIX}
    export CMAKE_PREFIX_PATH=${STAGING_DIR_TARGET}${prefix}:${STAGING_DIR_NATIVE}${prefix}:${STAGING_DIR_TARGET}${ROS_PREFIX}:${STAGING_DIR_NATIVE}${ROS_PREFIX}
    
    # Python path needs to include ROS Python packages AND target python configurations
    # We prepend STAGING_LIBDIR/${PYTHON_DIR} to find _sysconfigdata for the target
    export PYTHONPATH=${STAGING_LIBDIR}/${PYTHON_DIR}:${STAGING_DIR_NATIVE}${libdir}/${PYTHON_DIR}/site-packages:${STAGING_DIR_NATIVE}${ROS_PREFIX}/lib/${PYTHON_DIR}/site-packages:${PYTHONPATH}
    
    # Force python to use target configuration for extensions
    export _PYTHON_SYSCONFIGDATA_NAME="_sysconfigdata__linux_${TARGET_ARCH}-linux-gnu" 
    export _PYTHON_HOST_PLATFORM="linux-${TARGET_ARCH}"

    # Export SDKTARGETSYSROOT for CMake to find DeepStream/CUDA properly
    export SDKTARGETSYSROOT=${STAGING_DIR_TARGET}
    
    # Explicitly find rmw_implementation if needed
    export RMW_IMPLEMENTATION=rmw_fastrtps_cpp

    colcon build \
        --merge-install \
        --build-base ${S}/build \
        --install-base ${S}/install \
        --paths ${S}/src/live_sensors_node ${S}/src/interfaces \
        --event-handlers console_direct+ \
        --cmake-args \
            -DCMAKE_BUILD_TYPE=Release \
            -DCMAKE_NO_SYSTEM_FROM_IMPORTED=1 \
            -DCMAKE_toolchain_file=${WORKDIR}/toolchain.cmake \
            -DCMAKE_SYSROOT=${STAGING_DIR_TARGET} \
            -DCMAKE_FIND_ROOT_PATH="${STAGING_DIR_TARGET};${STAGING_DIR_NATIVE}" \
            -DRMW_IMPLEMENTATION=rmw_fastrtps_cpp \
            ${EXTRA_OECMAKE}
}

# Override do_install to copy the colcon build output
do_install() {
    install -d ${D}${prefix}/perception-app
    cp -r ${S}/install/* ${D}${prefix}/perception-app/
}

FILES:${PN} = "${prefix}/perception-app"

# Ensure we don't pick up host stuff
INSANE_SKIP:${PN} += "already-stripped"
