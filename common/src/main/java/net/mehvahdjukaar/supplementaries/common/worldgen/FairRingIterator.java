package net.mehvahdjukaar.supplementaries.common.worldgen;

import java.util.*;

public class FairRingIterator implements Iterator<FairRingIterator.Ring>, Iterable<FairRingIterator.Ring> {

    public record Ring(int gridSize, int radius, int commonIterationsIndex) {
    }

    private static class GridRing {
        final int gridIndex;
        final int gridSize;
        final int kMax;
        int k;
        int iterations = 0;

        GridRing(int gridIndex, int gridSize, int k, int kMax) {
            this.gridIndex = gridIndex;
            this.gridSize = gridSize;
            this.k = k;
            this.kMax = kMax;
        }

        int radius() {
            return k * gridSize;
        }
    }

    private final PriorityQueue<GridRing> pq;

    /**
     * @param gridSizes list of grid sizes
     * @param maxRadius maximum radius requested
     * @param inclusive if true, the last ring that overshoots maxRadius is included (ceil), otherwise floor
     */
    public FairRingIterator(List<Integer> gridSizes, long maxRadius, boolean inclusive) {
        if (gridSizes == null) throw new IllegalArgumentException("gridSizes must not be null");

        pq = new PriorityQueue<>(
                Comparator.comparingLong(GridRing::radius)
                        .thenComparingInt(r -> r.gridIndex)
        );

        for (int i = 0; i < gridSizes.size(); i++) {
            int s = gridSizes.get(i);
            if (s <= 0) continue;

            int kMax;
            if (inclusive) {
                kMax = (int) Math.ceil((double) (maxRadius) / s); // include rings that cover maxRadius
            } else {
                kMax = (int) ((maxRadius) / s); // floor division
            }

            pq.add(new GridRing(i, s, 0, kMax));
        }
    }

    @Override
    public boolean hasNext() {
        return !pq.isEmpty();
    }

    private int commonIterations() {
        return pq.stream().mapToInt(r -> r.iterations).min().orElse(0);
    }

    @Override
    public Ring next() {
        if (pq.isEmpty()) throw new NoSuchElementException();

        GridRing gr = pq.poll();
        int snapped = gr.radius();

        if (gr.k < gr.kMax) {
            gr.k += 1;
            pq.add(gr);
        }
        int commonIterations = commonIterations();
        if(gr.iterations <= commonIterations) {
            gr.iterations++;
            commonIterations = commonIterations();
        }

        return new Ring(gr.gridSize, snapped , commonIterations);
    }

    @Override
    public Iterator<Ring> iterator() {
        return this;
    }
}
