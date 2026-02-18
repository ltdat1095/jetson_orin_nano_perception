SUMMARY = "Minimal Jetson Orin Nano image with perception apps"
LICENSE = "MIT"

require recipes-core/images/core-image-minimal.bb

# --- Core system & debugging ---
IMAGE_INSTALL:append = " \
    openssh \
    gdb \
    procps \
    ca-certificates \
    iproute2 \
    ethtool \
    rsync \
    util-linux \
    dtc \
"

# --- Kernel & hardware support ---
IMAGE_INSTALL:append = " \
    nvidia-kernel-oot-cameras \
    nvidia-kernel-oot-display \
    pciutils \
    kernel-module-r8169 \
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

# --- Protocol ---
IMAGE_INSTALL:append = " \
    packagegroup-ros-debs \
"

# --- Streaming pipe line---
IMAGE_INSTALL:append = " \
    gstreamer1.0-plugins-ugly \
    gstreamer1.0-plugins-bad \
    gstreamer1.0-plugins-good \
    gstreamer1.0-plugins-nvarguscamerasrc \
    gstreamer1.0-plugins-nvvidconv \
    tegra-libraries-multimedia \
    tegra-libraries-multimedia-utils \
    tegra-argus-daemon \
    deepstream-7.1 \
    ffmpeg \
    gstreamer1.0-libav \
"

# --- Streaming debug---
IMAGE_INSTALL:append = " \
    x264 \
    tensorrt-trtexec \
    v4l-utils \
    deepstream-tests \
    gstreamer1.0-plugins-nvvideosinks \
    gstreamer1.0-plugins-nvvideo4linux2 \
    gstreamer1.0-plugins-nvv4l2camerasrc \
    tegra-libraries-multimedia-v4l \
"

# --- Debugging ---
IMAGE_INSTALL:append = " \
    kmod \
    tegra-tools-tegrastats \
"

# --- SDK Generating ---
TOOLCHAIN_TARGET_TASK:append = " \
    gstreamer1.0-dev \
    gstreamer1.0-plugins-base-dev \
    gstreamer1.0-plugins-good-dev \
    tensorrt-core-dev \
    tensorrt-plugins-prebuilt-dev \
    packagegroup-ros-sdk \
"

TOOLCHAIN_HOST_TASK:append = " \
    nativesdk-packagegroup-ros \
"