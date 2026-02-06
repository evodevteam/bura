package bura.com.client;

import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

public final class BuraVisibilityCache {
	private static final int CACHE_MAX_ENTRIES = 20000;
	private static final Long2ByteOpenHashMap VISIBILITY_CACHE = new Long2ByteOpenHashMap();
	private static final Long2IntOpenHashMap VISIBILITY_FRAME = new Long2IntOpenHashMap();
	private static final Long2IntOpenHashMap FORCE_VISIBLE_UNTIL = new Long2IntOpenHashMap();
	private static int frameId = 0;

	private BuraVisibilityCache() {
	}

	public static int nextFrame() {
		return ++frameId;
	}

	public static void clearIfTooLarge() {
		if (VISIBILITY_CACHE.size() > CACHE_MAX_ENTRIES) {
			VISIBILITY_CACHE.clear();
			VISIBILITY_FRAME.clear();
			FORCE_VISIBLE_UNTIL.clear();
		}
	}

	public static int getLastFrame(long node) {
		return VISIBILITY_FRAME.getOrDefault(node, -1);
	}

	public static boolean getCachedVisible(long node) {
		return VISIBILITY_CACHE.getOrDefault(node, (byte) 0) == 1;
	}

	public static void put(long node, int frame, boolean visible) {
		VISIBILITY_FRAME.put(node, frame);
		VISIBILITY_CACHE.put(node, visible ? (byte) 1 : (byte) 0);
	}

	public static void invalidate(long node) {
		VISIBILITY_CACHE.remove(node);
		VISIBILITY_FRAME.remove(node);
		FORCE_VISIBLE_UNTIL.remove(node);
	}

	public static void forceVisible(long node, int frames) {
		FORCE_VISIBLE_UNTIL.put(node, frameId + frames);
	}

	public static boolean isForceVisible(long node) {
		return FORCE_VISIBLE_UNTIL.getOrDefault(node, -1) >= frameId;
	}
}
