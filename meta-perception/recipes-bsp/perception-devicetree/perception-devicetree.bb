DESCRIPTION = "Custom Jetson Orin Nano devicetree"
HOMEPAGE = "https://github.com/OE4T"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit devicetree

COMPATIBLE_MACHINE = "(tegra)"

DEPENDS += "nvidia-kernel-oot dtc-native"

SRC_URI = "\
    file://tegra234-p3768-0000-cam0-imx219.dts \
"

DT_INCLUDE = " \
    ${RECIPE_SYSROOT}/usr/src/device-tree/nvidia/tegra/nv-public \
    ${RECIPE_SYSROOT}/usr/src/device-tree/nvidia/t23x/nv-public/include/kernel \
    ${RECIPE_SYSROOT}/usr/src/device-tree/nvidia/t23x/nv-public/include/nvidia-oot \
    ${RECIPE_SYSROOT}/usr/src/device-tree/nvidia/t23x/nv-public/include/platforms \
    ${RECIPE_SYSROOT}/usr/src/device-tree/nvidia/t23x/nv-public/nv-platform \
    ${RECIPE_SYSROOT}/usr/src/device-tree/nvidia/t23x/nv-public \
    ${S} \
    ${KERNEL_INCLUDE} \
"

# From kernel-devicetree/generic-dts/Makefile
DTC_PPFLAGS:append = " -DLINUX_VERSION=600 -DTEGRA_HOST1X_DT_VERSION=2"

# re-implement function from devicetree.bbclass to preserve order of KERNEL_INCLUDE
def expand_includes(varname, d):
    import glob
    includes = list()
    # expand all includes with glob
    for i in (d.getVar(varname) or "").split():
        for g in glob.glob(i):
            if os.path.isdir(g): # only add directories to include path
                includes.append(g)
    return includes


devicetree_do_compile:append() {
    import subprocess
    b = d.getVar("B")
    s = d.getVar("S")
    rootpath = d.getVar("RECIPE_SYSROOT")

    #base = os.path.join(rootpath, "boot/devicetree/tegra234-p3768-0000+p3767-0005-nv.dtb")
    # overlay = os.path.join(rootpath, "boot/devicetree/tegra234-p3767-camera-p3768-imx219-A.dtbo")
    #overlay_dts = os.path.join(s,"tegra234-p3768-0000-cam0-imx219.dts")
    #overlay_dtbo = os.path.join(b,"tegra234-p3768-0000-cam0-imx219.dtbo")
    #result = os.path.join(b, "tegra234-p3768-0000+p3767-0005-perception.dtb")


    includes = expand_includes("DT_INCLUDE", d)
    base = os.path.join(rootpath,
        "boot/devicetree/tegra234-p3768-0000+p3767-0005-nv.dtb")

    overlay_dts = os.path.join(
        s, "tegra234-p3768-0000-cam0-imx219.dts")

    preprocessed = os.path.join(
        b, "overlay.pp.dts")

    overlay_dtbo = os.path.join(
        b, "tegra234-p3768-0000-cam0-imx219.dtbo")

    result = os.path.join(
        b, "tegra234-p3768-0000+p3767-0005-perception.dtb")

    bb.note("Preprocessing overlay with cpp")

    bb.note("Preprocessing overlay with cpp")
    subprocess.check_call([
        "cpp",
        "-nostdinc",
        "-undef",
        "-x", "assembler-with-cpp",
        *sum([["-I", i] for i in includes], []),
        overlay_dts,
        preprocessed
    ])

    bb.note("Compiling overlay with dtc")
    subprocess.check_call([
        "dtc",
        "-@",
        "-I", "dts",
        "-O", "dtb",
        "-o", overlay_dtbo,
        preprocessed
    ])

    bb.note("Applying DT overlay via fdtoverlay")
    subprocess.check_call([
        "fdtoverlay",
        "-i", base,
        "-o", result,
        overlay_dtbo
    ])
}
