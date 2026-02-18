SUMMARY = "Jetson Stats (jtop)"
DESCRIPTION = "System monitoring tool for NVIDIA Jetson platforms"
LICENSE = "GPL-3.0-only"
LIC_FILES_CHKSUM = "file://LICENSE;md5=8763b57f0092c337eb12c354870a324a"

SRC_URI = " \
        https://github.com/rbonghi/jetson_stats/archive/refs/tags/4.3.1.tar.gz \
        file://disable-install.patch \
"
SRC_URI[sha256sum] = "3400429f9a8e6d9fba148a8051527798945f4db065091a6b0de5220ec4df230b"

S = "${WORKDIR}/jetson_stats-4.3.1"

inherit setuptools3 systemd python3native

SETUPTOOLS_USE_DISTUTILS = "stdlib"
PYTHON_WHEEL_DISABLE = "1"

RDEPENDS:${PN} += " \
    python3-core \
    python3-modules \
    python3-psutil \
    python3-distro \
    python3-pyyaml \
    tegra-tools-tegrastats \
"


SYSTEMD_SERVICE:${PN} = "jtop.service"

FILES:${PN} += " \
    ${systemd_system_unitdir}/jtop.service \
    ${sysconfdir}/profile.d/jtop_env.sh \
"

do_compile() {
    :
}

do_install:append() {
    # Fix shebangs to avoid python3-native leakage (Yocto QA)
    for f in jtop jetson_release jetson_swap jetson_config; do
        if [ -f ${D}${bindir}/$f ]; then
            sed -i -e '1s|^#!.*python.*$|#!/usr/bin/env python3|' \
                ${D}${bindir}/$f
        fi
    done
}

do_install() {
    export JTOP_INSTALL=1
    export JTOP_SKIP_ENV_CHECK=1
    export JTOP_NO_ENV=1

    ${PYTHON} setup.py install \
        --prefix=${prefix} \
        --root=${D} \
        --optimize=1
}

do_install:append() {
    # Move env script from /usr/etc → /etc
    if [ -d ${D}${prefix}/etc ]; then
        install -d ${D}${sysconfdir}
        mv ${D}${prefix}/etc/* ${D}${sysconfdir}/ || true
        rmdir --ignore-fail-on-non-empty ${D}${prefix}/etc || true
    fi
}
