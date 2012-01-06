#ifndef FIELD
#  define FIELD(name)
#  define FIELD_DEFINED
#endif

FIELD(magic)

FIELD(heapSize)
FIELD(codeSize)

FIELD(bootClassCount)
FIELD(appClassCount)
FIELD(stringCount)
FIELD(callCount)

FIELD(bootLoader)
FIELD(appLoader)
FIELD(types)
FIELD(methodTree)
FIELD(methodTreeSentinal)
FIELD(virtualThunks)

#ifdef FIELD_DEFINED
#  undef FIELD
#  undef FIELD_DEFINED
#endif

#ifndef THUNK_FIELD
#  define THUNK_FIELD(name)
#  define THUNK_FIELD_DEFINED
#endif

THUNK_FIELD(default_);
THUNK_FIELD(defaultVirtual);
THUNK_FIELD(native);
THUNK_FIELD(aioob);
THUNK_FIELD(stackOverflow);
THUNK_FIELD(table);

#ifdef THUNK_FIELD_DEFINED
#  undef THUNK_FIELD
#  undef THUNK_FIELD_DEFINED
#endif
