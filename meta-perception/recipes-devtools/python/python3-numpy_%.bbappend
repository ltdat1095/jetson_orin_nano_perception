# Apply ONLY to nativesdk variant
FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

do_install:append:class-nativesdk() {
    install -d ${D}${datadir}/cmake

    cat << 'EOF' > ${D}${datadir}/cmake/Python3NumPy.cmake
# Yocto SDK helper for CMake FindPython3 NumPy detection

if(NOT DEFINED Python3_NumPy_INCLUDE_DIR)
    set(Python3_NumPy_INCLUDE_DIR
        "${OECORE_NATIVE_SYSROOT}${PYTHON_SITEPACKAGES_DIR}/numpy/core/include"
        CACHE PATH "NumPy include directory"
    )
endif()
EOF
}
