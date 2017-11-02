IF(INTERNAL_UPDATE_SERIALIZATION_STRUCTURES)

# Be very careful modifying any of this, otherwise the .bullet format becomes incompatible

	SUBDIRS ( BulletFileLoader BulletWorldImporter)

ELSE(INTERNAL_UPDATE_SERIALIZATION_STRUCTURES)

	SUBDIRS ( BulletFileLoader BulletWorldImporter )

ENDIF (INTERNAL_UPDATE_SERIALIZATION_STRUCTURES)

