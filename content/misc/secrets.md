---
title: Secrets
---
Below is a list of unknown ("secret") technical details:

* How to decode the `SP` chunk (that seems to be used to emulate EGA graphics in the VGA-version of the game)?
* How did the original interpreter emulate CGA from EGA version?
* The `SL` chunk is always empty (and seems to be unused) – what was it meant for?
* The frequency in the `CC` (Color Cycling) chunk is a mystery. It is big endian encoded. A value of 0xAAA is ignored. To calculate the delay, 16,384 has to be divided by the frequency. What is the delay unit - jiffies?

**... Let me know when you know!** 👀
