
IF (BUILD_BLEND_DEMO OR INTERNAL_UPDATE_SERIALIZATION_STRUCTURES)
	SUBDIRS(BlenderSerialize )
ENDIF()


IF(INTERNAL_UPDATE_SERIALIZATION_STRUCTURES)

# makesdna and HeaderGenerator are for advanced use only
# makesdna can re-generate the binary DNA representing the Bullet serialization structures
# Be very careful modifying any of this, otherwise the .bullet format becomes incompatible

	SUBDIRS ( BulletFileLoader BulletXmlWorldImporter BulletWorldImporter HeaderGenerator makesdna)

ELSE(INTERNAL_UPDATE_SERIALIZATION_STRUCTURES)

	SUBDIRS ( BulletFileLoader BulletXmlWorldImporter BulletWorldImporter )

ENDIF (INTERNAL_UPDATE_SERIALIZATION_STRUCTURES)

