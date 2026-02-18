RDEPENDS:${PN} += "libyaml"
DEPENDS += "libyaml"

PYTHON_PACKAGECONFIG:remove:class-nativesdk = "libyaml"
