SUMMARY = "Network for systemd-networkd"
LICENSE = "MIT"

SRC_URI = "file://eth0.network"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

S = "${WORKDIR}"

do_install() {
    install -d ${D}${nonarch_base_libdir}/systemd/network
    install -m 0644 ${WORKDIR}/eth0.network ${D}${nonarch_base_libdir}/systemd/network/
}

FILES:${PN} += "${nonarch_base_libdir}/systemd/network/eth0.network"