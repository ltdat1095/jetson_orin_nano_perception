SUMMARY = "Perception App for Jetson"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "gitsm://github.com/ltdat1095/jetson_perception;protocol=https;branch=master"
SRCREV = "${AUTOREV}"

S = "${WORKDIR}/git"

DEPENDS = " \
    python3-colcon-common-extensions-native \
    python3-vcstool-native \
    python3-colcon-ros-native \
    python3-setuptools-native \
    python3-numpy-native \
    python3-numpy \
    git-native \
    cmake-native \
    rclcpp \
    sensor-msgs \
    ament-cmake \
    ament-cmake-native \
    ament-package-native \
    ament-cmake-auto-native \
    rosidl-default-generators-native \
    rosidl-default-generators \
    rosidl-adapter-native \
    rosidl-default-runtime \
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
" 

inherit cmake python3native


# Disable default do_configure as there might not be a root CMakeLists.txt
do_configure() {
    :
}

# We are using colcon, so we override do_compile
do_compile() {
    # Clean previous build to avoid caching issues
    rm -rf ${S}/build ${S}/install ${S}/log
    
    export COLCON_HOME=${WORKDIR}/colcon
    
    # Based on find output, ament_cmakeConfig.cmake is in opt/ros/humble
    export ROS_NATIVE_PREFIX=${STAGING_DIR_NATIVE}/opt/ros/humble
    
    export AMENT_PREFIX_PATH=${STAGING_DIR_TARGET}${prefix}:${STAGING_DIR_NATIVE}${prefix}:${ROS_NATIVE_PREFIX}
    export CMAKE_PREFIX_PATH=${STAGING_DIR_TARGET}${prefix}:${STAGING_DIR_NATIVE}${prefix}:${ROS_NATIVE_PREFIX}
    export PYTHONPATH=${STAGING_DIR_NATIVE}${libdir}/${PYTHON_DIR}/site-packages:${ROS_NATIVE_PREFIX}/lib/${PYTHON_DIR}/site-packages:${PYTHONPATH}

    # Export SDKTARGETSYSROOT for CMake to find DeepStream/CUDA
    export SDKTARGETSYSROOT=${STAGING_DIR_TARGET}
    
    # Debug: Check where ament_package is
    find ${STAGING_DIR_NATIVE} -name "ament_package" -type d
    
    # Robustly find builtin_interfaces and export its directory
    echo "Searching for builtin_interfacesConfig.cmake in ${STAGING_DIR_TARGET}..."
    BUILTIN_INTERFACES_CONFIG=$(find ${STAGING_DIR_TARGET} -name "builtin_interfacesConfig.cmake" | head -n 1)
    if [ -n "$BUILTIN_INTERFACES_CONFIG" ]; then
        BUILTIN_INTERFACES_DIR=$(dirname "$BUILTIN_INTERFACES_CONFIG")
        echo "Found builtin_interfaces at $BUILTIN_INTERFACES_DIR"
        export builtin_interfaces_DIR=$BUILTIN_INTERFACES_DIR
    else
        echo "ERROR: builtin_interfacesConfig.cmake NOT FOUND in STAGING_DIR_TARGET"
    fi

    # Robustly find rosidl_default_runtime and export its directory
    echo "Searching for rosidl_default_runtimeConfig.cmake in ${STAGING_DIR_TARGET}..."
    ROSIDL_DEFAULT_RUNTIME_CONFIG=$(find ${STAGING_DIR_TARGET} -name "rosidl_default_runtimeConfig.cmake" | head -n 1)
    if [ -n "$ROSIDL_DEFAULT_RUNTIME_CONFIG" ]; then
        ROSIDL_DEFAULT_RUNTIME_DIR=$(dirname "$ROSIDL_DEFAULT_RUNTIME_CONFIG")
        echo "Found rosidl_default_runtime at $ROSIDL_DEFAULT_RUNTIME_DIR"
        export rosidl_default_runtime_DIR=$ROSIDL_DEFAULT_RUNTIME_DIR
    else
        echo "ERROR: rosidl_default_runtimeConfig.cmake NOT FOUND in STAGING_DIR_TARGET"
    fi

    # Fix for file format errors (linking native instead of target)
    # Explicitly find and export target paths for these packages
    PKG_FIX_LIST="rosidl_runtime_c rosidl_typesupport_cpp rosidl_typesupport_introspection_cpp rosidl_typesupport_fastrtps_cpp rosidl_runtime_cpp rosidl_typesupport_c rosidl_typesupport_introspection_c rcutils fastcdr rmw rcpputils rosidl_typesupport_fastrtps_c rclcpp sensor_msgs std_msgs ament_index_cpp libstatistics_collector statistics_msgs rosgraph_msgs rcl_interfaces tracetools rcl libyaml_vendor rcl_yaml_param_parser rcl_logging_spdlog spdlog_vendor rcl_logging_interface"
    
    CMAKE_ARGS_FIX=""
    for pkg in $PKG_FIX_LIST; do
        echo "Searching for ${pkg} config in ${STAGING_DIR_TARGET}..."
        
        # Try finding standard Config.cmake
        PKG_CONFIG=$(find ${STAGING_DIR_TARGET} -name "${pkg}Config.cmake" | head -n 1)
        
        # If not found, try lowercase -config.cmake (common for some libs like fastcdr)
        if [ -z "$PKG_CONFIG" ]; then
             PKG_CONFIG=$(find ${STAGING_DIR_TARGET} -name "${pkg}-config.cmake" | head -n 1)
        fi
        
        if [ -n "$PKG_CONFIG" ]; then
            PKG_DIR=$(dirname "$PKG_CONFIG")
            echo "Found $pkg at $PKG_DIR"
            export ${pkg}_DIR=$PKG_DIR
            CMAKE_ARGS_FIX="${CMAKE_ARGS_FIX} -D${pkg}_DIR=${PKG_DIR}"
        else
            echo "WARNING: Config for ${pkg} NOT FOUND in STAGING_DIR_TARGET"
            # Fallback for fastcdr specific check if still needed
            if [ "$pkg" = "fastcdr" ]; then
                 echo "Searching for *fastcdr* in ${STAGING_DIR_TARGET}..."
                 find ${STAGING_DIR_TARGET} -name "*fastcdr*" | head -n 5
            fi
        fi
    done

    # Debug: Print package.xml of interfaces to verify content
    echo "--- Content of ${S}/src/interfaces/package.xml ---"
    if [ -f "${S}/src/interfaces/package.xml" ]; then
        cat "${S}/src/interfaces/package.xml"
    else
        echo "ERROR: ${S}/src/interfaces/package.xml does not exist!"
    fi
    echo "--------------------------------------------------"

    # Debug: Check where ament_cmakeConfig.cmake is
    find ${STAGING_DIR_NATIVE} -name "ament_cmakeConfig.cmake"
    
    # Try using --paths instead of --base-paths to be explicit, but absolute paths
    colcon list --paths ${S}/src/live_sensors_node ${S}/src/interfaces

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
            "-Dbuiltin_interfaces_DIR=${builtin_interfaces_DIR}" \
            "-Drosidl_default_runtime_DIR=${rosidl_default_runtime_DIR}" \
            ${CMAKE_ARGS_FIX}
}

# Override do_install to copy the colcon build output
do_install() {
    install -d ${D}${prefix}/perception-app
    cp -r ${S}/install/* ${D}${prefix}/perception-app/
}

FILES:${PN} = "${prefix}/perception-app"

# Ensure we don't pick up host stuff
INSANE_SKIP:${PN} += "already-stripped"
