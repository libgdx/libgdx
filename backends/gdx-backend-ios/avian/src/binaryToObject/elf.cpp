/* Copyright (c) 2009-2011, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

#include "stdint.h"
#include "stdio.h"
#include "string.h"

#include "endianness.h"

#define EI_NIDENT 16

#define EI_MAG0 0
#define EI_MAG1 1
#define EI_MAG2 2
#define EI_MAG3 3
#define EI_CLASS 4
#define EI_DATA 5
#define EI_VERSION 6
#define EI_OSABI 7
#define EI_ABIVERSION 8

#define ELFMAG0 0x7f
#define ELFMAG1 'E'
#define ELFMAG2 'L'
#define ELFMAG3 'F'

#define ELFCLASS64 2
#define ELFCLASS32 1

#define EV_CURRENT 1

#define ELFDATA2LSB 1
#define ELFDATA2MSB 2

#define ELFOSABI_SYSV 0

#define ET_REL 1

#define EM_386 3
#define EM_X86_64 62
#define EM_ARM 40
#define EM_PPC 20

#define SHT_PROGBITS 1
#define SHT_SYMTAB 2
#define SHT_STRTAB 3

#define SHF_WRITE (1 << 0)
#define SHF_ALLOC (1 << 1)
#define SHF_EXECINSTR (1 << 2)

#define STB_GLOBAL 1

#define STT_NOTYPE 0

#define STV_DEFAULT 0

#define ELF64_ST_INFO(bind, type) (((bind) << 4) + ((type) & 0xf))
#define ELF32_ST_INFO(bind, type) ELF64_ST_INFO((bind), (type))

#if (BITS_PER_WORD == 64)
#  define FileHeader Elf64_Ehdr
#  define SectionHeader Elf64_Shdr
#  define Symbol Elf64_Sym
#  define Class ELFCLASS64
#  define SYMBOL_INFO ELF64_ST_INFO
#elif (BITS_PER_WORD == 32)
#  define FileHeader Elf32_Ehdr
#  define SectionHeader Elf32_Shdr
#  define Symbol Elf32_Sym
#  define Class ELFCLASS32
#  define SYMBOL_INFO ELF32_ST_INFO
#else
#  error
#endif

#define OSABI ELFOSABI_SYSV

namespace {

typedef uint16_t Elf64_Half;
typedef uint32_t Elf64_Word;
typedef uint64_t Elf64_Addr;
typedef uint64_t Elf64_Xword;
typedef uint16_t Elf64_Section;
typedef uint64_t Elf64_Off;

struct Elf64_Ehdr {
  unsigned char e_ident[EI_NIDENT];
  Elf64_Half e_type;
  Elf64_Half e_machine;
  Elf64_Word e_version;
  Elf64_Addr e_entry;
  Elf64_Off e_phoff;
  Elf64_Off e_shoff;
  Elf64_Word e_flags;
  Elf64_Half e_ehsize;
  Elf64_Half e_phentsize;
  Elf64_Half e_phnum;
  Elf64_Half e_shentsize;
  Elf64_Half e_shnum;
  Elf64_Half e_shstrndx;
};

struct Elf64_Shdr {
  Elf64_Word sh_name;
  Elf64_Word sh_type;
  Elf64_Xword sh_flags;
  Elf64_Addr sh_addr;
  Elf64_Off sh_offset;
  Elf64_Xword sh_size;
  Elf64_Word sh_link;
  Elf64_Word sh_info;
  Elf64_Xword sh_addralign;
  Elf64_Xword sh_entsize;
};

struct Elf64_Sym {
  Elf64_Word st_name;
  unsigned char st_info;
  unsigned char st_other;
  Elf64_Section st_shndx;
  Elf64_Addr st_value;
  Elf64_Xword st_size;
};

typedef uint16_t Elf32_Half;
typedef uint32_t Elf32_Word;
typedef uint32_t Elf32_Addr;
typedef uint64_t Elf32_Xword;
typedef uint16_t Elf32_Section;
typedef uint32_t Elf32_Off;

struct Elf32_Ehdr {
  unsigned char	e_ident[EI_NIDENT];
  Elf32_Half e_type;
  Elf32_Half e_machine;
  Elf32_Word e_version;
  Elf32_Addr e_entry;
  Elf32_Off e_phoff;
  Elf32_Off e_shoff;
  Elf32_Word e_flags;
  Elf32_Half e_ehsize;
  Elf32_Half e_phentsize;
  Elf32_Half e_phnum;
  Elf32_Half e_shentsize;
  Elf32_Half e_shnum;
  Elf32_Half e_shstrndx;
};

struct Elf32_Shdr {
  Elf32_Word sh_name;
  Elf32_Word sh_type;
  Elf32_Word sh_flags;
  Elf32_Addr sh_addr;
  Elf32_Off sh_offset;
  Elf32_Word sh_size;
  Elf32_Word sh_link;
  Elf32_Word sh_info;
  Elf32_Word sh_addralign;
  Elf32_Word sh_entsize;
};

struct Elf32_Sym {
  Elf32_Word st_name;
  Elf32_Addr st_value;
  Elf32_Word st_size;
  unsigned char st_info;
  unsigned char st_other;
  Elf32_Section st_shndx;
};

void
writeObject(const uint8_t* data, unsigned size, FILE* out,
            const char* startName, const char* endName,
            const char* sectionName, unsigned sectionFlags,
            unsigned alignment, int machine, int encoding)
{
  const unsigned sectionCount = 5;
  const unsigned symbolCount = 2;

  const unsigned sectionNameLength = strlen(sectionName) + 1;
  const unsigned startNameLength = strlen(startName) + 1;
  const unsigned endNameLength = strlen(endName) + 1;

  const char* const sectionStringTableName = ".shstrtab";
  const char* const stringTableName = ".strtab";
  const char* const symbolTableName = ".symtab";

  const unsigned sectionStringTableNameLength
    = strlen(sectionStringTableName) + 1;
  const unsigned stringTableNameLength = strlen(stringTableName) + 1;
  const unsigned symbolTableNameLength = strlen(symbolTableName) + 1;

  const unsigned nullStringOffset = 0;

  const unsigned sectionStringTableNameOffset = nullStringOffset + 1;
  const unsigned stringTableNameOffset
    = sectionStringTableNameOffset + sectionStringTableNameLength;
  const unsigned symbolTableNameOffset
    = stringTableNameOffset + stringTableNameLength;
  const unsigned sectionNameOffset
    = symbolTableNameOffset + symbolTableNameLength;
  const unsigned sectionStringTableLength
    = sectionNameOffset + sectionNameLength;

  const unsigned startNameOffset = nullStringOffset + 1;
  const unsigned endNameOffset = startNameOffset + startNameLength;
  const unsigned stringTableLength = endNameOffset + endNameLength;

  const unsigned bodySectionNumber = 1;
  const unsigned sectionStringTableSectionNumber = 2;
  const unsigned stringTableSectionNumber = 3;

  FileHeader fileHeader;
  memset(&fileHeader, 0, sizeof(FileHeader));
  fileHeader.e_ident[EI_MAG0] = V1(ELFMAG0);
  fileHeader.e_ident[EI_MAG1] = V1(ELFMAG1);
  fileHeader.e_ident[EI_MAG2] = V1(ELFMAG2);
  fileHeader.e_ident[EI_MAG3] = V1(ELFMAG3);
  fileHeader.e_ident[EI_CLASS] = V1(Class);
  fileHeader.e_ident[EI_DATA] = V1(encoding);
  fileHeader.e_ident[EI_VERSION] = V1(EV_CURRENT);
  fileHeader.e_ident[EI_OSABI] = V1(OSABI);
  fileHeader.e_ident[EI_ABIVERSION] = V1(0);
  fileHeader.e_type = V2(ET_REL);
  fileHeader.e_machine = V2(machine);
  fileHeader.e_version = V4(EV_CURRENT);
  fileHeader.e_entry = VW(0);
  fileHeader.e_phoff = VW(0);
  fileHeader.e_shoff = VW(sizeof(FileHeader));
  fileHeader.e_flags = V4(machine == EM_ARM ? 0x04000000 : 0);
  fileHeader.e_ehsize = V2(sizeof(FileHeader));
  fileHeader.e_phentsize = V2(0);
  fileHeader.e_phnum = V2(0);
  fileHeader.e_shentsize = V2(sizeof(SectionHeader));
  fileHeader.e_shnum = V2(sectionCount);
  fileHeader.e_shstrndx = V2(sectionStringTableSectionNumber);

  SectionHeader nullSection;
  memset(&nullSection, 0, sizeof(SectionHeader));

  SectionHeader bodySection;
  bodySection.sh_name = V4(sectionNameOffset);
  bodySection.sh_type = V4(SHT_PROGBITS);
  bodySection.sh_flags = VW(sectionFlags);
  bodySection.sh_addr = VW(0);
  unsigned bodySectionOffset
    = sizeof(FileHeader) + (sizeof(SectionHeader) * sectionCount);
  bodySection.sh_offset = VW(bodySectionOffset);
  unsigned bodySectionSize = size;
  bodySection.sh_size = VW(bodySectionSize);
  bodySection.sh_link = V4(0);
  bodySection.sh_info = V4(0);
  bodySection.sh_addralign = VW(alignment);
  bodySection.sh_entsize = VW(0);

  SectionHeader sectionStringTableSection;
  sectionStringTableSection.sh_name = V4(sectionStringTableNameOffset);
  sectionStringTableSection.sh_type = V4(SHT_STRTAB);
  sectionStringTableSection.sh_flags = VW(0);
  sectionStringTableSection.sh_addr = VW(0);
  unsigned sectionStringTableSectionOffset
    = bodySectionOffset + bodySectionSize;
  sectionStringTableSection.sh_offset = VW(sectionStringTableSectionOffset);
  unsigned sectionStringTableSectionSize = sectionStringTableLength;
  sectionStringTableSection.sh_size = VW(sectionStringTableSectionSize);
  sectionStringTableSection.sh_link = V4(0);
  sectionStringTableSection.sh_info = V4(0);
  sectionStringTableSection.sh_addralign = VW(1);
  sectionStringTableSection.sh_entsize = VW(0);

  SectionHeader stringTableSection;
  stringTableSection.sh_name = V4(stringTableNameOffset);
  stringTableSection.sh_type = V4(SHT_STRTAB);
  stringTableSection.sh_flags = VW(0);
  stringTableSection.sh_addr = VW(0);
  unsigned stringTableSectionOffset
    = sectionStringTableSectionOffset + sectionStringTableSectionSize;
  stringTableSection.sh_offset  = VW(stringTableSectionOffset);
  unsigned stringTableSectionSize = stringTableLength;
  stringTableSection.sh_size = VW(stringTableSectionSize);
  stringTableSection.sh_link = V4(0);
  stringTableSection.sh_info = V4(0);
  stringTableSection.sh_addralign = VW(1);
  stringTableSection.sh_entsize = VW(0);

  SectionHeader symbolTableSection;
  symbolTableSection.sh_name = V4(symbolTableNameOffset);
  symbolTableSection.sh_type = V4(SHT_SYMTAB);
  symbolTableSection.sh_flags = VW(0);
  symbolTableSection.sh_addr = VW(0);
  unsigned symbolTableSectionOffset
    = stringTableSectionOffset + stringTableSectionSize;
  symbolTableSection.sh_offset = VW(symbolTableSectionOffset);
  unsigned symbolTableSectionSize = sizeof(Symbol) * symbolCount;
  symbolTableSection.sh_size = VW(symbolTableSectionSize);
  symbolTableSection.sh_link = V4(stringTableSectionNumber);
  symbolTableSection.sh_info = V4(0);
  symbolTableSection.sh_addralign = VW(BITS_PER_WORD / 8);
  symbolTableSection.sh_entsize = VW(sizeof(Symbol));

  Symbol startSymbol;
  startSymbol.st_name = V4(startNameOffset);
  startSymbol.st_value = VW(0);
  startSymbol.st_size = VW(0);
  startSymbol.st_info = V1(SYMBOL_INFO(STB_GLOBAL, STT_NOTYPE));
  startSymbol.st_other = V1(STV_DEFAULT);
  startSymbol.st_shndx = V2(bodySectionNumber);

  Symbol endSymbol;
  endSymbol.st_name = V4(endNameOffset);
  endSymbol.st_value = VW(size);
  endSymbol.st_size = VW(0);
  endSymbol.st_info = V1(SYMBOL_INFO(STB_GLOBAL, STT_NOTYPE));
  endSymbol.st_other = V1(STV_DEFAULT);
  endSymbol.st_shndx = V2(bodySectionNumber);

  fwrite(&fileHeader, 1, sizeof(fileHeader), out);
  fwrite(&nullSection, 1, sizeof(nullSection), out);
  fwrite(&bodySection, 1, sizeof(bodySection), out);
  fwrite(&sectionStringTableSection, 1, sizeof(sectionStringTableSection),
         out);
  fwrite(&stringTableSection, 1, sizeof(stringTableSection), out);
  fwrite(&symbolTableSection, 1, sizeof(symbolTableSection), out);

  fwrite(data, 1, size, out);

  fputc(0, out);
  fwrite(sectionStringTableName, 1, sectionStringTableNameLength, out);
  fwrite(stringTableName, 1, stringTableNameLength, out);
  fwrite(symbolTableName, 1, symbolTableNameLength, out);
  fwrite(sectionName, 1, sectionNameLength, out);

  fputc(0, out);
  fwrite(startName, 1, startNameLength, out);
  fwrite(endName, 1, endNameLength, out);

  fwrite(&startSymbol, 1, sizeof(startSymbol), out);
  fwrite(&endSymbol, 1, sizeof(endSymbol), out);
}

} // namespace

#define MACRO_MAKE_NAME(a, b, c) a##b##c
#define MAKE_NAME(a, b, c) MACRO_MAKE_NAME(a, b, c)

namespace binaryToObject {

bool
MAKE_NAME(writeElf, BITS_PER_WORD, Object)
  (uint8_t* data, unsigned size, FILE* out, const char* startName,
   const char* endName, const char* architecture, unsigned alignment,
   bool writable, bool executable)
{
  int machine;
  int encoding;
  if (strcmp(architecture, "x86_64") == 0) {
    machine = EM_X86_64;
    encoding = ELFDATA2LSB;
  } else if (strcmp(architecture, "i386") == 0) {
    machine = EM_386;
    encoding = ELFDATA2LSB;
  } else if (strcmp(architecture, "arm") == 0) {
    machine = EM_ARM;
    encoding = ELFDATA2LSB;
  } else if (strcmp(architecture, "powerpc") == 0) {
    machine = EM_PPC;
    encoding = ELFDATA2MSB;
  } else {
    fprintf(stderr, "unsupported architecture: %s\n", architecture);
    return false;
  }

  const char* sectionName;
  unsigned sectionFlags = SHF_ALLOC;
  if (writable) {
    if (executable) {
      sectionName = ".rwx";
      sectionFlags |= SHF_WRITE | SHF_EXECINSTR;
    } else {
      sectionName = ".data";
      sectionFlags |= SHF_WRITE;
    }
  } else if (executable) {
    sectionName = ".text";
    sectionFlags |= SHF_EXECINSTR;
  } else {
    sectionName = ".rodata";
  }

  writeObject(data, size, out, startName, endName, sectionName, sectionFlags,
              alignment, machine, encoding);

  return true;
}

} // namespace binaryToObject
