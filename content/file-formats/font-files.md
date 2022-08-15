---
layout: page
title: Font files
---
Information about the font (also known as charset) files `90X.LFL` (LFL is a known abbreviation for <u>L</u>ucas<u>f</u>ilm <u>L</u>td).

## Format

In addition to the [file format basics](file-format-basics.md):

* The file consists of only one chunk. The length information is wrong – also see [Notes](#notes).

The file format in [ABNF](https://datatracker.ietf.org/doc/html/rfc5234) notation:

{{< highlight abnf >}}
file                = header font-header 1*glyph

header              = length name color-map
font-header         = bpp height num-glyphs glyph-offset

length              = UINT32        ; length of data after this field (but strangely 15 byte to small)
name                = %x63.03       ; probably the chunk name
                                    ; always 0x63 ('c'), 0x03 (ASCII control character ETX – end of text)
color-map           = 15UINT8       ; color map (actually indices to colors on the room color map)
bpp                 = UINT8         ; bits per pixel (could be 1 or 2)
height              = UINT8         ; height in pixels
num-glyphs          = UINT16        ; number of glyphs, this font contains
glyph-offset        = 1*UINT32      ; offset of each character glyph (occurrs num-glyphs times)
                                    ; the sequence corresponds to the character (mostly ASCII) -
                                    ; e.g. A is on position 65 (zero based numbering)
                                    ; offsets are relative to the start of font-header
                                    ; offset 0 means "this font has no glyph for this character"

glyph               = width height x-offset y-offset glyph-data
width               = UINT8         ; width in pixels
x-offset            = SINT8         ; x-offset of glyph
y-offset            = SINT8         ; y-offset of glyph
glyph-data          = 1*OCTET       ; bitstream glyph in big endian order
                                    ; (bpp defines how many bits are used for one pixel)

; common data types
SINT8               = OCTET         ; signed, 8 bit integer
UINT8               = OCTET         ; unsigned, 8 bit integer
UINT16              = 2OCTET        ; unsigned, 16 bit integer; octets in little endian order
UINT32              = 4OCTET        ; unsigned, 32 bit integer; octets in little endian order

; ABNF core rules
OCTET               = %x00-FF       ; 8 bits of data
{{< /highlight >}}

## Notes

* The value in the length field is mysteriously 15 byte to small. I assume they forgot to include
  the color-map in the length calculation.
* The purpose of the name field 0x6303 is only a guess

## References

This project wouldn't have been possible without the great work from others:

* [ScummVM: SCUMM/Technical Reference/Charset resources](https://wiki.scummvm.org/index.php?title=SCUMM/Technical_Reference/Charset_resources#V4_charset_format)
