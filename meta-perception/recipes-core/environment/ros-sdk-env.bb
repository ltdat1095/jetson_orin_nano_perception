SUMMARY = "ROS SDK environment setup (target only)"
LICENSE = "MIT"

inherit allarch

do_install() {
    install -d ${D}${sysconfdir}/profile.d

    cat << 'EOF' > ${D}${sysconfdir}/profile.d/perception-ros.sh
# ROS 2 target environment

export ROS_VERSION=2
export ROS_DISTRO=humble

ROS_ROOT="/opt/ros/${ROS_DISTRO}"

if [ -f "${ROS_ROOT}/setup.sh" ]; then
    . "${ROS_ROOT}/setup.sh"
fi
EOF
}

FILES:${PN} += "${sysconfdir}/profile.d/perception-ros.sh"
