SUMMARY = "Minimal Jetson Orin Nano image with perception apps"
LICENSE = "MIT"

require recipes-core/images/core-image-minimal.bb

# --- Core system & debugging ---
IMAGE_INSTALL:append = " \
    openssh \
    gdb \
    util-linux \
"

# --- Kernel & hardware support ---
IMAGE_INSTALL:append = " \
    kernel-modules \
    pciutils \
    tegra-tools-tegrastats \
"

# --- Networking ---
IMAGE_INSTALL:append = " \
    networkmanager \
    l4t-usb-device-mode \
    perception-network-config \
"

# --- OTA / device management ---
IMAGE_INSTALL:append = " \
    mender-auth \
    mender-update \
    mender-flash \
"

# --- Streaming ---
IMAGE_INSTALL:append = " \
    v4l-utils \
    gstreamer1.0 \
    gstreamer1.0-plugins-base \
    gstreamer1.0-plugins-good \
    gstreamer1.0-plugins-nvvideo4linux2 \
    gstreamer1.0-plugins-nvarguscamerasrc \
"

# Explicitly choose the 4.x "mender" recipe as the provider
PREFERRED_RPROVIDER_mender-auth = "mender"
PREFERRED_RPROVIDER_mender-update = "mender"
PREFERRED_PROVIDER_mender-native = "mender-native"