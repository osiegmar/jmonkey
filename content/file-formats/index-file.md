---
layout: page
title: Index file
---
Information about the index file `000.LFL` (LFL is a known abbreviation for <u>L</u>ucas<u>f</u>ilm <u>L</u>td).

## Content

The index file allows quick access to data structures stored in [data files](data-files.md) by providing:

* List of *room names* (not every room has a name, used for debugging purposes only)
* Directory of *rooms* (information about which room id is in which data file)
* Directory of *scripts*, *sounds* and *costumes* (relative position to the room)
* List of *object properties*

The information about *room names* and *object properties* is not stored elsewhere.
Therefore, this file is not only an index.

## Format

In addition to the [file format basics](file-format-basics.md):

* The file consists of multiple consecutive (non-nested) chunks.
* The room names are encrypted by XOR `0xFF`.

The file format in [ABNF](https://datatracker.ietf.org/doc/html/rfc5234) notation:

{{< highlight abnf >}}
file                = room-names
                      room-directory script-directory sound-directory costume-directory object-directory

room-names          = chunk-length "RN" 1*( room-id room-name ) NUL
room-directory      = chunk-length "0R" no-of-items 1*( file-number file-offset )
script-directory    = chunk-length "0S" no-of-items 1*( room-id room-offset )
sound-directory     = chunk-length "0N" no-of-items 1*( room-id room-offset )
costume-directory   = chunk-length "0C" no-of-items 1*( room-id room-offset )
object-directory    = chunk-length "0O" no-of-items 1*( class-data class-state class-owner )

chunk-length        = UINT32        ; the length of the entire chunk (including this field)
room-id             = UINT8         ; the id of the room
room-name           = 9OCTET        ; all octets are XOR-ed with 0xFF
                                    ; resulting string (ASCII) is NUL terminated
no-of-items         = UINT16        ; number of items that will follow
file-number         = UINT8         ; file number (e.g. 2 for DISK02.LEC). Number 0 means "not used".
file-offset         = UINT32        ; file offsets are not used in this version of SCUMM (always 0)
room-offset         = SINT32        ; the offset address (relative to the room start address)
class-data          = 24BIT         ; class data bitset
class-state         = 4BIT          ; class state
class-owner         = 4BIT          ; class owner

; common data types
NUL                 = %x00
UINT8               = OCTET         ; unsigned, 8 bit integer
UINT16              = 2OCTET        ; unsigned, 16 bit integer; octets in little endian order
SINT32              = 4OCTET        ; signed, 32 bit integer; octets in little endian order
UINT32              = 4OCTET        ; unsigned, 32 bit integer; octets in little endian order

; ABNF core rules
OCTET               = %x00-FF       ; 8 bits of data
BIT                 = "0" / "1"     ; 1 bit of data
{{< /highlight >}}

## Usage

For a better understanding here's an example how to find room id 10 and its parts. All offset addresses are *highly specific* to the version of the game (platform, color depth, size and number of floppy disks, language, etc.).

* The `room-names` tells us that room id 10 is named *logo*. This might be useful for debugging purposes. Those room names are not used in the game.
* The `room-directory` tells us that room 10 is on disk 1 (filename `DISK01.LEC`).
* The `file-offset` in this index is not used (always 0). In order to find the offset of room 10, the `FO` chunk of the referenced [data file](data-files.md) (`DISK01.LEC`) has to be decoded:
    * The `FO` chunks tells us that the `LF` chunk (the container chunk for rooms, scripts, sounds and costumes) for room 10 is at offset 108 (right after the `FO` chunk).
    * The `LF` chunk of room 10 starts with a 6-bytes header (see [file format basics](file-format-basics.md)) and after that with a 2-byte room id (with a value of 10 in this case).
    * Right after these 8 bytes the `RO` chunk begins. So the offset of room 10 is 116 (108 + 6 + 2).
* The `script-directory` and `costume-directory` are pointing to an offset address that is relative to the containing room. The `script-directory` is pointing to an `SC` chunk where the `costume-directory` is pointing to a `CO` chunk. For example, we have costume 59 that points to room 10 with a relative offset address of 102,667. In order to read the `CO` chunk for that costume the address 102,783 (room offset 116 + costume offset 102,667) has to be used.
* The `sound-directory` is a bit special:
  * DOS: The majority of entries are pointing to `SO` chunks in the same way `script-directory` is pointing to `SC` chunks. But there is this special room 94 that contains Roland MT-32 chunks. Those chunks are direct children of the `LF` chunk and are *not* wrapped by a `SO` chunk. In addition to that those Roland MT32-chunks also have `RO` as the type in their header and therefor could be easily mistaken for room chunks.
  * Amiga: The entries are pointing to `AM` chunks in the same way `script-directory` is pointing to `SC` chunks.

## Notes

* In the PC-EGA version `room-id` 94 (`room-name` roland) the `file-number` refers to 9. This file/disk was not shipped with the game. A [patch](https://appsupport.disney.com/hc/en-us/articles/360000760746-Where-can-I-find-updates-and-patches-for-Lucasfilm-games-) can be downloaded that includes the missing file (the Roland MT-32 sound chunks).
* Unused room references use `room-id` 0 and `room-offset` 0. At least the english Amiga-Version also uses `room-id` 255 and `room-offset` -1 for those.
* In some versions unused references weren't removed from the index. Especially the english PC-VGA version references many non-existing scripts, sounds and costumes.
* The fields class-data, class-state and class-owner needs more analysis and documentation

## References

This project wouldn't have been possible without the great work from others:

* [ScummVM: SCUMM/Technical Reference/Index File](https://wiki.scummvm.org/index.php?title=SCUMM/Technical_Reference/Index_File#Scumm_3.2F4)
* [LucasHacks: SCUMM file format specifications and documentation](http://scumm.mixnmojo.com/?page=specs&file=indexfiles.txt) [[archive](https://web.archive.org/web/20070301022307/http://scumm.mixnmojo.com/?page=specs&file=indexfiles.txt)]
