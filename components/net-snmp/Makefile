#
# CDDL HEADER START
#
# The contents of this file are subject to the terms of the
# Common Development and Distribution License (the "License").
# You may not use this file except in compliance with the License.
#
# You can obtain a copy of the license at usr/src/OPENSOLARIS.LICENSE
# or http://www.opensolaris.org/os/licensing.
# See the License for the specific language governing permissions
# and limitations under the License.
#
# When distributing Covered Code, include this CDDL HEADER in each
# file and include the License file at usr/src/OPENSOLARIS.LICENSE.
# If applicable, add the following below this CDDL HEADER, with the
# fields enclosed by brackets "[]" replaced with your own identifying
# information: Portions Copyright [yyyy] [name of copyright owner]
#
# CDDL HEADER END
#

#
# Copyright (c) 2011, 2022, Oracle and/or its affiliates.
#
BUILD_BITS= 64_and_32

# The Makefile of this component does not support multiple build jobs.
COMPONENT_MAKE_JOBS=1

include ../../make-rules/shared-macros.mk

PATH=$(GNUBIN):$(USRBINDIR)
COMPILER=gcc

COMPONENT_NAME=		net-snmp
COMPONENT_VERSION=	5.8
COMPONENT_PROJECT_URL=	http://www.net-snmp.org/
COMPONENT_ARCHIVE_HASH= \
    sha256:b2fc3500840ebe532734c4786b0da4ef0a5f67e51ef4c86b3345d697e4976adf
COMPONENT_ARCHIVE_URL=	https://sourceforge.net/projects/net-snmp/files/net-snmp/$(COMPONENT_VERSION)/$(COMPONENT_ARCHIVE)
COMPONENT_BUGDB=	service/net-snmp utility/net-snmp library/net-snmp
COMPONENT_ANITYA_ID=	2062

COMPONENT_BAID=		59754

BUILD_32_and_64 += $(BUILD_DIR_32)/.docs
INSTALL_TARGET= build $(INSTALL_32_and_64)
TEST_TARGET= $(TEST_32_and_64)

include $(WS_MAKE_RULES)/common.mk

PYTHON_VERSION = 3.7

studio_C99MODE= $(studio_C99_ENABLE)
CPPFLAGS += "-DFALSE_SHARING_ALIGN=64"

CONFIGURE_ENV +=	PYTHONPROG=$(PYTHON.$(BITS))

CONFIGURE_OPTIONS  +=	--with-default-snmp-version=3
CONFIGURE_OPTIONS  +=	--with-sys-contact="root@localhost"
CONFIGURE_OPTIONS  +=	--with-sys-location=Unknown
CONFIGURE_OPTIONS  +=	--with-logfile=$(VARDIR)/log/snmpd.log
CONFIGURE_OPTIONS  +=	--with-persistent-directory=$(VARDIR)/net-snmp

CONFIGURE_OPTIONS  +=	--with-mibdirs=$(ETCDIR)/net-snmp/snmp/mibs
CONFIGURE_OPTIONS  +=	--datadir=$(ETCDIR)/net-snmp
CONFIGURE_OPTIONS  +=	--enable-agentx-dom-sock-only
CONFIGURE_OPTIONS  +=	--enable-ucd-snmp-compatibility
CONFIGURE_OPTIONS  +=	--enable-ipv6
CONFIGURE_OPTIONS  +=	--enable-mfd-rewrites
CONFIGURE_OPTIONS  +=	--with-openssl=/lib/openssl
CONFIGURE_OPTIONS  +=	--with-transports="UDP TCP UDPIPv6 TCPIPv6"

# Which MIB modules do we want to build
MIB_MODULES = host disman/event-mib ucd-snmp/diskio udp-mib tcp-mib if-mib
CONFIGURE_OPTIONS  +=	--with-mib-modules="$(MIB_MODULES)"

# Python bindings support.
PYTHON_ARGS =		--basedir=/usr/bin
CONFIGURE_OPTIONS  +=	--with-python-modules="$(PYTHON_ARGS)"

# Only build 64 bit Perl support
PERL_ARGS =			DESTDIR=$(PROTO_DIR) INSTALLDIRS=vendor
CONFIGURE_OPTIONS.32  +=	--disable-embedded-perl
CONFIGURE_OPTIONS.32  +=	--without-perl-modules
CONFIGURE_OPTIONS.64  +=	PERLPROG="$(PERL)"
CONFIGURE_OPTIONS.64  +=	--with-perl-modules="$(PERL_ARGS)"
CONFIGURE_OPTIONS.64  +=	--disable-perl-cc-checks

# Install the net-snmp-57 libraries
COMPONENT_PRE_CONFIGURE_ACTION = \
	($(CLONEY) $(SOURCE_DIR) $(@D); $(CLONEY) $(COMPONENT_DIR)/sun $(@D)/sun; \
	cd ../net-snmp-57 && $(GMAKE) install);	

PRE_CLEAN_ACTION += cd ../net-snmp-57 && $(GMAKE) clean;

# Make a copy of the python bits so we can build separate python 3.9 support
COMPONENT_PREP_ACTION += (cd $(@D) ; cp -rp python python3.9);

# for the goofy definition of ARCH in the extra Makefiles
ARCH=32
ARCH=$(BITS:64=$(MACH64))

CCSMAKE_ARGS =	CC="$(CC)" CFGPREFIX=$(USRDIR) CFGLIB64=$(USRLIBDIR64)
CCSMAKE_ARGS +=	CFGLIB=$(USRLIBDIR) ARCH=$(ARCH) ROOT=$(PROTO_DIR)

COMPONENT_TEST_DIR =		$(BUILD_DIR)/$(MACH$(BITS))/testing/
COMPONENT_TEST_CMD =		$(KSH93)
COMPONENT_TEST_TARGETS =	./RUNTESTS
COMPONENT_TEST_TRANSFORMS += \
	'-e "s|\(simple-\).*|\1XXXXXX|g"' \
	'-e "s|\(output (\)\(.*\)|\2|g"'
COMPONENT_TEST_MASTER =	$(COMPONENT_TEST_RESULTS_DIR)/results-$(BITS).master
# Get correct binaries from the proto area.
TEST_BINPATH.32 =       $(PROTOUSRBINDIR)/$(MACH32)
TEST_BINPATH.64 =       $(BUILD_DIR)/prototype/$(MACH64)$(USRBINDIR)
COMPONENT_TEST_ENV +=   PATH=$(TEST_BINPATH.$(BITS)):$(PATH)
# Get correct libraries from the proto area.
TEST_LIBPATH.32 =       $(PROTOUSRLIBDIR)
TEST_LIBPATH.64 =       $(BUILD_DIR)/prototype/$(MACH64)$(USRLIBDIR64)
COMPONENT_TEST_ENV +=   LD_LIBRARY_PATH=$(TEST_LIBPATH.$(BITS))

# Usually component installs both 32 and 64bit compilation into $(PROTO_DIR).
# Here we install 32bit compilation into $(PROTO_DIR) and 64 bit into
# $(BUILD_DIR)/prototype/$(MACH64). So we need to tell pkg to look for binaries
# also here.
PKG_PROTO_DIRS += $(BUILD_DIR)/prototype/$(MACH64)

# Plus add paths to the 5.7 version libraries need to be added from the
# net-snmp-57 proto path.
PKG_PROTO_DIRS += $(WS_TOP)/components/net-snmp-57/build/prototype/$(MACH)
PKG_PROTO_DIRS += $(WS_TOP)/components/net-snmp-57/build/prototype/$(MACH64)

# Now we need to tell where 'gmake manifest-check' looks for binaries.
# By default manifest-check takes binaries from build/prototype/$(uname -p). We
# need to add more sources (see doc/manifest-check.txt for description):
#
# Add documentation
GENERATE_PROTO_DIRS += build/$(MACH32)/docs^build/$$(MACH32)/docs
# Add 64 bit libraries from build/prototype/$(MACH64) into usr/bin/$(MACH64)
GENERATE_PROTO_DIRS += build/prototype/$(MACH64)/usr/lib/$(MACH64)/^usr/lib/$$!(MACH64)
# Add 64 bit binaries from build/prototype/$(MACH64)
GENERATE_PROTO_DIRS += build/prototype/$(MACH64)/usr/bin^usr/bin
# Add net-snmp-57 both 32 and 64 bit locations
GENERATE_PROTO_DIRS += $(WS_TOP)/components/net-snmp-57/build/prototype/$(MACH64)
GENERATE_PROTO_DIRS += $(WS_TOP)/components/net-snmp-57/build/prototype/$(MACH)
# Add demo files
GENERATE_PROTO_DIRS += sun/sdk/demo^sun/sdk/demo

$(BUILD_32): COMPONENT_POST_BUILD_ACTION= \
	(cd $(BUILD_DIR)/$(MACH32)/sun ; ls agent; $(CCSMAKE) -e $(CCSMAKE_ARGS))

$(BUILD_64): COMPONENT_POST_BUILD_ACTION= \
	(cd $(BUILD_DIR)/$(MACH64)/sun ; ls agent; $(CCSMAKE) -e $(CCSMAKE_ARGS))

$(INSTALL_32): COMPONENT_INSTALL_ARGS += PYTHON_VENDOR_PACKAGES=$(PYTHON_VENDOR_PACKAGES)

$(INSTALL_32): COMPONENT_POST_INSTALL_ACTION= \
	($(MKDIR) -p $(BUILD_DIR)/prototype/$(MACH)/etc/net-snmp/snmp/mibs; \
	cd $(BUILD_DIR)/$(MACH32)/sun ; ls agent; $(CCSMAKE) -e $(CCSMAKE_ARGS) install)

$(INSTALL_64): COMPONENT_POST_INSTALL_ACTION= \
	($(MKDIR) -p $(BUILD_DIR)/prototype/$(MACH64)/etc/net-snmp/snmp/mibs; \
	 cd $(BUILD_DIR)/$(MACH64)/sun ; ls agent; $(CCSMAKE) -e $(CCSMAKE_ARGS) install) ; \
	(cd $(@D)/python3.9 ; env PYTHON=$(PYTHON.3.9.$(BITS)) \
	 $(PYTHON.3.9.$(BITS)) ./setup.py install \
		--root $(PROTO_DIR) \
		--install-lib=$(PYTHON.3.9.VENDOR_PACKAGES))

$(INSTALL_64): COMPONENT_INSTALL_ARGS += DESTDIR=$(BUILD_DIR)/prototype/$(MACH64)

# build the docs
$(BUILD_DIR_32)/.docs:
	# We can not use srcdir=$(@D) because that path is platform specific
	# and would produce differently named html files on x86 and sparc.
	(cd $(@D) ; $(ENV) srcdir=../../$(COMPONENT_SRC) $(DOXYGEN) -u $(@D)/doxygen.conf ; \
	    $(ENV) srcdir=../../$(COMPONENT_SRC) $(DOXYGEN) $(@D)/doxygen.conf)
	$(TOUCH) $@

PKG_MACROS += PYVER=$(PYTHON_VERSION)


REQUIRED_PACKAGES += developer/build/make
REQUIRED_PACKAGES += developer/documentation-tool/doxygen
REQUIRED_PACKAGES += library/security/openssl
REQUIRED_PACKAGES += $(PERL_PKG)
REQUIRED_PACKAGES += runtime/python-37
REQUIRED_PACKAGES += runtime/python-39
REQUIRED_PACKAGES += shell/bash
REQUIRED_PACKAGES += shell/ksh93
REQUIRED_PACKAGES += system/core-os
REQUIRED_PACKAGES += system/library/datalink
REQUIRED_PACKAGES += system/library/gcc/gcc-c-runtime
REQUIRED_PACKAGES += system/library/libpcap
REQUIRED_PACKAGES += system/library/math
REQUIRED_PACKAGES += system/linker
REQUIRED_PACKAGES += system/management/snmp/net-snmp
REQUIRED_PACKAGES += system/network
