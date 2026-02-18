SUMMARY = "ROS SDK environment setup (host)"
LICENSE = "MIT"

inherit allarch nativesdk

do_install() {
    install -d ${D}${SDKPATHNATIVE}/environment-setup.d

    cat << 'EOF' > ${D}${SDKPATHNATIVE}/environment-setup.d/perception-ros-sdk.sh
# ROS SDK environment (cross-compile only)

export ROS_VERSION=2
export ROS_DISTRO=humble

# Target ROS for CMake / ament
export AMENT_PREFIX_PATH="${SDKTARGETSYSROOT}/opt/ros/${ROS_DISTRO}"
export CMAKE_PREFIX_PATH="${SDKTARGETSYSROOT}/opt/ros/${ROS_DISTRO}"

# Native PYTHONPATH ONLY
for d in ${SDKTARGETSYSROOT}/opt/ros/${ROS_DISTRO}/lib/python3*/site-packages; do
    if [ -d "$d" ]; then
        export PYTHONPATH="$d${PYTHONPATH:+:$PYTHONPATH}"
        break
    fi
done

# Native NumPy (for rosidl_generator_py)
for d in ${SDKTARGETSYSROOT}/usr/lib/python3*/site-packages/numpy/core/include; do
    if [ -d "$d" ]; then
        echo "$d"
        export Python3_NumPy_INCLUDE_DIR="$d"
        break
    fi
done
EOF
}

FILES:${PN} += "${SDKPATHNATIVE}/environment-setup.d/perception-ros-sdk.sh"
