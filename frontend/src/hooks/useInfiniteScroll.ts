import { RefObject, useEffect, useRef } from 'react';

interface UseInfiniteScrollOptions {
  enabled: boolean;
  onLoadMore: () => void;
  rootMargin?: string;
}

export function useInfiniteScroll({ enabled, onLoadMore, rootMargin = '360px' }: UseInfiniteScrollOptions): RefObject<HTMLDivElement> {
  const markerRef = useRef<HTMLDivElement>(null);
  const onLoadMoreRef = useRef(onLoadMore);

  useEffect(() => {
    onLoadMoreRef.current = onLoadMore;
  }, [onLoadMore]);

  useEffect(() => {
    const marker = markerRef.current;
    if (!enabled || !marker) {
      return;
    }

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          onLoadMoreRef.current();
        }
      },
      { rootMargin }
    );

    observer.observe(marker);
    return () => observer.disconnect();
  }, [enabled, rootMargin]);

  return markerRef;
}
