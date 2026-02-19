1. Pull submodule
git submodule update --init --recursive

2. Setup env
source poky/oe-init-build-env

3. Build image
bitbake jetson-orin-perception
