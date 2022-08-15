---
title: Data files
---
Information about `DISK0X.LEC` files (LEC is a known abbreviation for <u>L</u>ucasArts <u>E</u>ntertainment <u>C</u>ompany).

These files contain the actual data of the game. They are containers for *rooms*, *scripts*, *sounds* and *costumes*:

* Rooms are a concept for dividing the games into parts. Every screen in the game has its own room. Examples are the *SCUMM bar*, the *Kitchen*, the *Circus tent* or scenes like the *Lucasfilm logo*, close-up shots or even the copy protection *‘Dial-a-Pirate’*. A room itself is a container for various resources:
  * An image (the room background)
  * Objects (things to interact with) – sometimes with an image attached to it
  * Walking boxes – information about where and how actors can travel within the room
  * Scaling data – the scale factor for three-dimensional appearance
  * Room specific scripts
* Global Scripts – the game logic
* Sounds – music and sound effects
* Costumes are the visual representation of an actor

The scripts, sounds and costumes were defined as part of a room in order to ensure that a room could be loaded in one step without asking for floppy disk changes. Although the global scripts, sounds and costumes could be used in other rooms as well. Explicit heap management allowed to keep often used resources in memory (e.g. Guybrush’s costume).

During the development of the game, every room and its resources has been ‘compiled’ into LFL-files (`001.LFL` to `098.LFL`). In contrast to earlier SCUMM versions those files were consolidated in larger LEC-files during the build process. Depending on the number of disks to distribute the files onto, a different number of files were created (one LEC-file per disk). In some versions of the game a separate LEC-file (`DISK09.LEC`) exists for room 94 only (the room for the Roland MT-32 sounds).

A room never spans over more than one LEC-file.

## Format

In addition to the [file format basics](file-format-basics.md):

* The file consists of multiple consecutive and nested chunks.
* The entire file is encrypted by XOR `0x69`.

The generic structure of data files in [ABNF](https://datatracker.ietf.org/doc/html/rfc5234) notation:

{{< highlight abnf >}}
file                = 1*chunk

chunk               = header body

header              = length name
length              = UINT32                ; the length of the entire chunk (including the 6 byte header itself)
name                = 2ALPHA                ; name of the chunk (e.g. LE, LF, RO)

body                = *OCTET *chunk         ; a chunk is usually either a data chunk or a container for sub-chunks
                                            ; the "LF"-chunk is an exception - it contains 2 byte of data plus
                                            ; multiple sub-chunks

; common data types
UINT32              = 4OCTET                ; unsigned, 32 bit integer; octets in little endian order

; ABNF core rules
ALPHA               = %x41-5A / %x61-7A     ; A-Z
OCTET               = %x00-FF               ; 8 bits of data
{{< /highlight >}}

Parsing the data file structure in a generic way can be challenging, because:

* There is no flag indicating the chunk *value* format (data, container or mixed).
* One cannot trust the *length* field of the `SO` container chunk as it could be incorrect.
  It seems that the first `SO` chunk on disk 1 has a length specified that is larger than the actual child chunks `WA` and `AD`. Some implementations handle the subsequent `SO` chunk as a child of the previous one but that also does not fit as there’s still a gap.
* The chunk *type* itself is not a trustworthy discriminator. The `LF` chunk with `chunk-lf-room-id` 94
  contains one (pseudo) room and several *Roland MT-32* data chunks that also have `RO` in their type field.
  Those sound nodes are *not* wrapped in a `SO` chunk.

Those problems are irrelevant when accessing data files using the [index file](index-file.md) as the index offsets are always pointing to the correct file and position! That’s the reason why the original game engine did not struggle with this.

The typical chunk hierarchy looks like this:

```
LE
├─── FO
├─── LF (repeated several times)
│    ├─── RO (only one room per LF node)
│    │    ├─── HD
│    │    ├─── CC
│    │    ├─── SP
│    │    ├─── BX
│    │    ├─── PA
│    │    ├─── SA
│    │    ├─── BM
│    │    ├─── OI (repeated several times)
│    │    ├─── NL
│    │    ├─── SL
│    │    ├─── OC (repeated several times)
│    │    ├─── EX
│    │    ├─── EN
│    │    ├─── LC
│    │    ╰─── LS (repeated several times)
│    ├─── SC (repeated several times)
│    ├─── SO (repeated several times)
│    │    ├─── WA
│    │    ╰─── AD
│    ├─── AM (repeated several times)
│    ╰─── CO (repeated several times)
├─── LF
│    ├─── RO (only one room per LF node)
│    │    ├─── HD
┊    ┊    ┊
│    ├─── RO (but there could be several RO data chunks for Roland MT-32)
┊    ┊
```

Every `LF` chunk represents one LFL-file (the files used to build the LEC-files during the build process of the game). Those chunks are wrapped in a `LE` chunk together with a `FO` chunk for indexing the `LF` chunks within the LEC-file.

## Chunk details

### Container chunks

In a container chunk the *value* is another chunk. The container chunk does not store any data on its own.
The *length* field spans the entire container including all enclosed chunks.

* The data files are organized as a tree structure. All data files start with a root node `LE`.
  The `LE` chunk spans the entire file thus the *length* field is equal to the file size.
* The leaf node `FO` stores an index that points to all the `LF` branch nodes of this file.
* The `RO` chunk could be either a **RO**om or a **RO**land Midi sound node. Only one *room* can
  exist in an `LF` chunk, but multiple *Roland MT-32 sound nodes* could appear (even in addition
  to the *room* node). The roland chunks are data chunks not containers.
* The `SO` chunk contains the sound nodes `WA` and `AD`.

### Mixed chunks

Mixed chunks contains data **and** other chunks.

* The `LF` chunks start with a room id (see [index file](index-file.md)) and is the
  parent of the nodes `RO`, `SC`, `SO` and `CO`.


### Data chunks

#### Room

| Type | Descriptive name   | Description                                                                                                                                 |
|------|--------------------|---------------------------------------------------------------------------------------------------------------------------------------------|
| HD   | Room header        | The room header contains the width and height of the room background image (`BM`) as well as the number of containing objects (`OC` / `OI`) |
| CC   | Color Cycling      | Color cycling definitions used for "animation" by shifting the color palette. Missing in EGA version.                                       |
| SP   | EGA color palette  | The palette used to run the VGA game in EGA mode on slow hardware. Missing in EGA version.                                                  |
| BX   | Walking Boxes      | Information about areas where Costumes can walk and information about the travel route from one box to another.                             |
| PA   | VGA color palette  | A palette of indexed colors (256 in DOS VGA, 32 in Amiga) used for the entire room. Missing in EGA version.                                                                   |
| SA   | Scale slots        | Information about how to scale Costumes and linked by walking boxes `BX`.                                                                   |
| BM   | Background Image   | The room background image (Width/Height of `HD` is required to decode). For VGA version the palette (`PA`) is also required.                |
| OI   | Object Image       | The object images (Width/Height of `OC` is required to decode). The image codecs are the same as for the background image.                  |
| NL   | List of sounds     | A list of identifiers for sounds.                                                                                                           |
| SL   | List of unknown    | (always empty / not used)                                                                                                                   |
| OC   | Object Code        | Information about an object (incl. width/height to render the object image).                                                                |
| EX   | Room exit script   | The script to run when a room is left.                                                                                                      |
| EN   | Room entry script  | The script to run when a room is entered.                                                                                                   |
| LC   | Local script count | Information about the number of subsequent local scripts.                                                                                   |
| LS   | Local script       | Local scripts for this room.                                                                                                                |

#### Global script

* TODO: SC

#### Sound

* TODO: WA, AD, RO

#### Costume

* TODO: CO

## Comprehensive definition

The specific ABNF notation for the file is this:

{{< highlight abnf >}}
file                = chunk-le                              ; Every files data is store in one large LE chunk


; === CHUNK STRUCTURE ===

chunk-le            = chunk-length "LE" chunk-fo 1*chunk-lf ; Main chunk (LucasArts Entertainment)
chunk-fo            = chunk-length "FO" data-fo             ; Info about room ids and offsets to chunk-lf

chunk-lf            = chunk-length "LF" chunk-lf-data
chunk-lf-data       = chunk-lf-room-id 1*chunk-lf-child
chunk-lf-child      = chunk-ro / chunk-sc /
                      chunk-so / chunk-am / chunk-co
chunk-lf-room-id    = UINT16                                ; room id (as defined per 000.LFL); see also fo-room-id

chunk-ro            = chunk-length "RO"                     ; RO chunk can be either a room or a roland sound chunk
                      ( *chunk-ro-data / chunk-roland-data )
chunk-ro-data       = chunk-hd / chunk-cc / chunk-sp /
                      chunk-bx / chunk-pa / chunk-sa /
                      chunk-bm / chunk-oi / chunk-nl /
                      chunk-sl / chunk-oc / chunk-ex /
                      chunk-en / chunk-lc / chunk-ls

chunk-sc            = chunk-length "SC" script-data         ; global scripts
chunk-so            = chunk-length "SO" chunk-wa chunk-ad   ; Container chunk for sounds
chunk-am            = chunk-length "AM" data-am             ; Amiga sound
chunk-co            = chunk-length "CO" data-co             ; Costumes
chunk-wa            = chunk-length "WA" data-wa             ; PC and PCjr/Tandy speaker sound
chunk-ad            = chunk-length "AD" data-ad             ; AdLib sound
chunk-hd            = chunk-length "HD" data-hd             ; Room header data
chunk-cc            = chunk-length "CC" data-cc             ; Color cycling (missing in EGA version)
chunk-sp            = chunk-length "SP" ( data-sp )         ; EGA color emulation palette (missing in EGA version, empty in Amiga version)
chunk-bx            = chunk-length "BX" data-bx             ; Boxes
chunk-pa            = chunk-length "PA" data-pa             ; VGA color palette (missing in EGA version)
chunk-sa            = chunk-length "SA" data-sa             ; Scale data
chunk-bm            = chunk-length "BM" img-data            ; Background image
chunk-oi            = chunk-length "OI" data-oi             ; Object image
chunk-nl            = chunk-length "NL" data-nl             ; List of sounds
chunk-sl            = chunk-length "SL" data-sl             ; Unknown, unused chunk
chunk-oc            = chunk-length "OC" data-oc             ; object script
chunk-ex            = chunk-length "EX" script-data         ; room exit script
chunk-en            = chunk-length "EN" script-data         ; room entry script
chunk-lc            = chunk-length "LC" data-lc             ; number of local scripts
chunk-ls            = chunk-length "LS" data-ls             ; local script


; === DATA STRUCTURE ===

; FO chunk
data-fo             = fo-lf-count 1*fo-room
fo-lf-count         = UINT8                                 ; number of LF nodes in this LE chunk (file)
fo-room             = fo-room-id fo-lf-offset               ; occurs fo-lf-count times
fo-room-id          = UINT8                                 ; room id (see chunk-lf-room-id)
fo-lf-offset        = UINT32                                ; offset of LF chunk containing the room

; CO chunk
data-co             = co-nr-anim co-format co-palette
                      co-anim-cmds-offset co-limbs-offset
                      co-anim-offset co-anim co-anim-cmds
                      co-limbs co-pics
co-nr-anim          = UINT8                                 ; number of animations (0 is one animation)
co-format           = OCTET                                 ; bit 7 set means that west anims must NOT be mirrored,
                                                            ; bit 0-6 is for the palette size (0x58=16 colors, 0x59=32)
co-palette          = 16UINT8 / 32UINT8                     ; 16 or 32 palette color indexes
co-anim-cmds-offset = UINT16                                ; offset for co-anim-cmds
co-limbs-offset     = 16UINT16                              ; offset of limb picture table (co-limbs)
co-anim-offset      = *UINT16                               ; offset address for each animation (co-anim)

co-anim             = *( co-limb-mask *co-anim-def )
co-limb-mask        = 2OCTET                                ; bitmask for up to 16 limbs to be used
                                                            ; every bit marks if limb is used (1) or not (0)
co-anim-def         = co-anim-start [co-anim-extra]         ; for every limb that is used, one co-anim-def will follow
                                                            ; the first possible limb would be 0x8000, the last 0x1
co-anim-start       = UINT16                                ; the start position in the co-anim-cmds array
                                                            ; 0xFFFF if disabled (no co-anim-extra will follow)
co-anim-extra       = OCTET                                 ; bit 7 indicates if animation should loop (0) or not (1)
                                                            ; rest of the bits are the number of commands to read
                                                            ; for this animation (from co-anim-cmds)

co-anim-cmds        = 1*co-anim-cmd                         ; the animation commands
                                                            ; (length: co-limbs-offset - co-anim-cmds-offset)
co-anim-cmd         = UINT8                                 ; fixed commands:
                                                            ;   0x78=sound, 0x79=stop, 0x7A=start, 0x7B=hide
                                                            ; everything else is a pointer to the actual image

co-limbs            = 1*co-limb-pic-offset                  ; table (item addressed by co-limbs-offset[limb])
co-limb-pic-offset  = 1*co-pic-offset                       ; table (item addressed by co-anim-cmd)
co-pic-offset       = UINT16                                ; offset of picture (co-pics)
co-pics             = 1*( co-pic-header co-pic-data )       ; the actual pictures
co-pic-header       = co-pic-width co-pic-height
                      co-pic-rel-x co-pic-rel-y
                      co-pic-move-x co-pic-move-y
co-pic-width        = UINT16                                ; width of the picture in pixels
co-pic-height       = UINT16                                ; height of the picture in pixels
co-pic-rel-x        = SINT16                                ; relative x position
co-pic-rel-y        = SINT16                                ; relative y position
co-pic-move-x       = SINT16                                ; relative x position move (move this and subsequent pics)
co-pic-move-y       = SINT16                                ; relative y position move (move this and subsequent pics)
co-pic-data         = 1*OCTET                               ; compressed image (4 or 5 bits per pixel)

; WA chunk TODO document
data-wa             = *OCTET

; AD chunk TODO document
data-ad             = *OCTET

; AM chunk TODO document
data-am             = 4OCTET

; HD chunk
data-hd             = hd-width hd-height hd-nr-objects
hd-width            = UINT16                                ; width of room image in pixels
hd-height           = UINT16                                ; height of room image in pixels
hd-nr-objects       = UINT16                                ; number of objects in room

; CC chunk TODO document
data-cc             = 16( cc-freq cc-start cc-end )
cc-freq             = UINT16BE
cc-start            = UINT8
cc-end              = UINT8

; SP chunk TODO document
data-sp             = 256OCTET                              ; EGA color palette

; BX chunk
data-bx             = bx-nr-boxes 1*bx-box
bx-nr-boxes         = UINT8                                 ; number of boxes
bx-box              = bx-ulx bx-uly bx-urx bx-ury           ; one box per nr-of-boxes
                      bx-lrx bx-lry bx-llx bx-lly
                      bx-mask bx-flags bx-scale

bx-ulx              = UINT16                                ; upper left X of the box polygon
bx-uly              = UINT16                                ; upper left Y of the box polygon
bx-urx              = UINT16                                ; upper right X of the box polygon
bx-ury              = UINT16                                ; upper right Y of the box polygon
bx-lrx              = UINT16                                ; lower right X of the box polygon
bx-lry              = UINT16                                ; lower right Y of the box polygon
bx-llx              = UINT16                                ; lower left X of the box polygon
bx-lly              = UINT16                                ; lower left Y of the box polygon
bx-mask             = UINT8                                 ; the Z plane this box masks
bx-flags            = UINT8                                 ; 0x08 : X flip
                                                            ; 0x10 : Y flip
                                                            ; 0x20 : Ignore scale / Player only
                                                            ; 0x40 : Locked
                                                            ; 0x80 : Invisible
bx-scale            = 2OCTET                                ; scaling (or if highest bit is set, the scale slot)

; PA chunk
data-pa             = pa-length 1*( pa-col-r pa-col-g pa-col-b )
pa-length           = UINT16                                ; length of following data (768 for 256 colors)
pa-col-r            = UINT8                                 ; value of red
pa-col-g            = UINT8                                 ; value of green
pa-col-b            = UINT8                                 ; value of blue

; SA chunk TODO document
data-sa             = *4sa-scale-slot
sa-scale-slot       = 2( sa-scale sa-y )
sa-scale            = UINT16
sa-y                = UINT16

; OI chunk
data-oi             = oi-id img-data
oi-id               = UINT16                                ; object id

; NL chunk
data-nl             = nl-cnt *nl-data
nl-cnt              = UINT8                                 ; number of subsequent nl-data
nl-data             = UINT8                                 ; id of sound

; SL chunk
data-sl             = sl-cnt
sl-cnt              = UINT8                                 ; always 0

; OC chunk
data-oc             = oc-id oc-unknown-octet oc-x-strip
                      oc-y-strip-state oc-width oc-parent
                      oc-walk-x oc-walk-y oc-height-actor
                      oc-name-offset oc-verb-table oc-name
                      script-data
oc-id               = UINT16                                ; OC chunk id
oc-unknown-octet    = OCTET                                 ; unknown octet - always 0
oc-x-strip          = UINT8                                 ; x position in strips (multiply by 8 to get pixel position)
oc-y-strip-state    = OCTET                                 ; highest bit is parent state rest is y position in strips
oc-width            = UINT8                                 ; width in strips
oc-parent           = UINT8                                 ; TODO document
oc-walk-x           = UINT16                                ; TODO document
oc-walk-y           = UINT16                                ; TODO document
oc-height-actor     = OCTET                                 ; The higher five bits (mask 0xF8) are the height
                                                            ; The lower three bits (mask 0x07) are the actor direction
oc-name-offset      = UINT8                                 ; Offset of oc-name (relative to chunk start)
oc-verb-table       = *( oc-verb oc-verb-offset ) NUL
oc-verb             = UINT8                                 ; The verb (e.g. TALK TO or USE), 0xFF is the default
oc-verb-offset      = UINT16                                ; The offset of the script within the script-data
oc-name             = *CHAR NUL                             ; NUL terminated string (localized with unknown charset)

; LC chunk
data-lc             = UINT16                                ; number of subsequent LS chunks

; LS chunk
data-ls             = ls-script-id script-data
ls-script-id        = UINT8

; ROLAND sound chunk TODO document
chunk-roland-data   = *OCTET


; === COMMON TYPES ===

; image (used in chunk-bm and chunk-oi)
img-data            = img-zpl-offset 1*img-strip-offset 1*img-strip *img-zplane
img-zpl-offset      = UINT32                                ; offset of img-zplanes (z-plane definitions / image layers)
                                                            ; offset could point to end of chunk (no z-planes defined)
img-strip-offset    = UINT32                                ; pointers to img-strip (relative to img-data)
img-strip           = img-strip-codec img-strip-color img-strip-data
img-strip-codec     = UINT8                                 ; compression codec (>= 1 <= 128)
img-strip-color     = UINT8                                 ; color palette index of the first pixel in the strip
img-strip-data      = 1*OCTET                               ; image data (format depends on codec)
img-zplane          = img-zpl-len [ img-zpl-strip-off img-zpl-strip-data ]
img-zpl-len         = UINT16                                ; number of bytes of this zplane (including this field)
img-zpl-strip-off   = 1*UINT16                              ; pointer to img-zpl-strip-data (relative to img-zplane)
img-zpl-strip-data  = 1*OCTET                               ; compressed mask for every strip

; scumm script
script-data         = 1*( script-opcode *script-opcode-args )
script-opcode       = UINT8                                 ; script instruction code
script-opcode-args  = *OCTET                                ; opcode specific arguments
                                                            ; length is specific to the opcode and the arguments itself

; misc
chunk-length          = UINT32                              ; the length of the entire chunk incl. the header itself and
                                                            ; the chunks child nodes

; common data types
NUL                 = %x00
UINT8               = OCTET                                 ; unsigned, 8 bit integer
SINT16              = 2OCTET                                ; signed, 16 bit integer; octets in little endian order
UINT16              = 2OCTET                                ; unsigned, 16 bit integer; octets in little endian order
UINT16BE            = 2OCTET                                ; unsigned, 16 bit integer; octets in big endian order
UINT32              = 4OCTET                                ; unsigned, 32 bit integer; octets in little endian order

; ABNF core rules
CHAR                = %x01-7F                               ; any 7-bit US-ASCII character, excluding NUL
OCTET               = %x00-FF                               ; 8 bits of data
{{< /highlight >}}

## References

This project wouldn't have been possible without the great work from others:

* [Old SCUMM File Format](http://scummrev.mixnmojo.com/specs/oldfrmt.shtml) ([mirror](http://jsg.id.au/scumm/scummrev/specs/oldfrmt.html))
* Detailed information about Chunks HD, PA, OC, BM, OI (how to decode background images and object images):
  [Thirst is Nothing, Image is Everything](http://scummrev.mixnmojo.com/articles/image.html) ([mirror](http://jsg.id.au/scumm/scummrev/articles/image.html))
* Detailed information about the BX chunk:
  [ScummVM: SCUMM/Technical Reference/Box resources](https://wiki.scummvm.org/index.php?title=SCUMM/Technical_Reference/Box_resources#SCUMM_V4)
* Detailed information about the CO chunk:
  [ScummVM: SCUMM/Technical Reference/Costume resources](https://wiki.scummvm.org/index.php?title=SCUMM/Technical_Reference/Costume_resources)
* Detailed information about the OC chunk:
  [ScummVM: SCUMM/Technical Reference/Object resources](https://wiki.scummvm.org/index.php?title=SCUMM/Technical_Reference/Object_resources)
* Detailed information about Chunks SC, LS, EN, EX, OC:
  [ScummVM: SCUMM/Technical Reference/Script resources](https://wiki.scummvm.org/index.php?title=SCUMM/Technical_Reference/Script_resources)
* Detailed information about Boxes and walk matrix:
  [https://www.limulo.net/website/game/scumm/scumm-boxes-walk-matrix.html](https://www.limulo.net/website/game/scumm/scumm-boxes-walk-matrix.html)
