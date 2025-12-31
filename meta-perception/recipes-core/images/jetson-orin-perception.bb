SUMMARY = "Minimal Jetson Orin Nano image with perception apps"
LICENSE = "MIT"

require recipes-core/images/core-image-minimal.bb

# 1. Cài đặt các gói phần mềm
IMAGE_INSTALL:append = " \
    openssh \
    tegra-tools-tegrastats \
    gdb \
    perception-network \
"