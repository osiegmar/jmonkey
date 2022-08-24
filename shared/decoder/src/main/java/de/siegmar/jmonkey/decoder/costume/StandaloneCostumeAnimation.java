/*
 * JMonkey - Java based development kit for "The Secret of Monkey Island"
 * Copyright (C) 2022  Oliver Siegmar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.siegmar.jmonkey.decoder.costume;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.siegmar.jmonkey.commons.misc.RasterImage;
import de.siegmar.jmonkey.commons.misc.WritableRasterImage;

public final class StandaloneCostumeAnimation {

    private StandaloneCostumeAnimation() {
    }

    public static CostumeAnimation collectCostumeAnimation(final Costume costume, final int animNo) {
        final List<LimbAnimation> limbs = costume.getAnimation(animNo);

        final int maxFrames = limbs.stream()
            .mapToInt(l -> l.getFrames().size())
            .max()
            .orElse(0);

        if (maxFrames == 0) {
            return new CostumeAnimation(List.of());
        }

        final List<RasterImage> costFrames = new ArrayList<>(maxFrames);

        final boolean flip = costume.header().mirror() && (animNo % 4) == 0;

        final Set<Integer> stoppedLimbs = new HashSet<>();

        for (int frame = 0; frame < maxFrames; frame++) {
            final List<CostumeLimbImage> limbFrames = new ArrayList<>();

            for (final LimbAnimation limb : limbs) {
                final int limbNo = limb.getLimbNo();

                final LimbFrame limbFrame = limb.getFrame(frame);

                if (limbFrame.getCommand().isControl()) {
                    if (limbFrame.getCommand().isStop()) {
                        stoppedLimbs.add(limbNo);
                    } else if (limbFrame.getCommand().isStart()) {
                        stoppedLimbs.remove(limbNo);
                    }
                } else {
                    if (stoppedLimbs.contains(limbNo)) {
                        // FIXME not called – this is obviously not the way limb stopping works
                        throw new IllegalStateException("NOT IMPLEMENTED");
                    } else {
                        limbFrames.add(limbFrame.getLimbImage());
                    }
                }
            }

            if (!limbFrames.isEmpty()) {
                costFrames.add(assembleCostFrames(limbFrames, flip));
            }
        }

        return new CostumeAnimation(costFrames);
    }

    private static RasterImage assembleCostFrames(final List<CostumeLimbImage> limbFrames, final boolean flip) {
        // TODO remove fixed x/y
        final WritableRasterImage img = new WritableRasterImage(230, 220);
        int x = 105;
        int y = 125;

        for (final CostumeLimbImage limbFrame : limbFrames) {
            img.transfer(limbFrame.rasterImage(), x + limbFrame.relX(), y + limbFrame.relY());

            x += limbFrame.moveX();
            y -= limbFrame.moveY();
        }

        if (flip) {
            img.mirror();
        }

        return img.rasterImage();
    }

}
