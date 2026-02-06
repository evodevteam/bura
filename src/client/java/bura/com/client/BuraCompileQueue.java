package bura.com.client;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Comparator;
import java.util.PriorityQueue;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;

public final class BuraCompileQueue {
	private static final int MAX_QUEUE_SIZE = ClientChunkThrottle.MAX_COMPILE_QUEUE * 6;
	private static final PriorityQueue<CompileEntry> QUEUE = new PriorityQueue<>(
		Comparator.comparingDouble(CompileEntry::distanceSq)
			.thenComparingLong(CompileEntry::sequence)
	);
	private static final Long2ObjectOpenHashMap<CompileEntry> BY_SECTION = new Long2ObjectOpenHashMap<>();
	private static long sequenceCounter = 0L;
	private static long priorityBoostNode = 0L;
	private static int stalledTicks = 0;
	private static boolean forceSchedule = false;

	private BuraCompileQueue() {
	}

	public static void boost(long sectionNode) {
		priorityBoostNode = sectionNode;
	}

	public static void enqueue(SectionRenderDispatcher dispatcher,
		SectionRenderDispatcher.RenderSection.CompileTask task,
		double distanceSq,
		long sectionNode) {
		CompileEntry existing = BY_SECTION.get(sectionNode);
		if (existing != null) {
			existing.cancelled = true;
			BY_SECTION.remove(sectionNode);
		} else if (QUEUE.size() >= MAX_QUEUE_SIZE) {
			return;
		}
		CompileEntry entry = new CompileEntry(dispatcher, task, distanceSq, sequenceCounter++, sectionNode);
		QUEUE.add(entry);
		BY_SECTION.put(sectionNode, entry);
	}

	public static void drain(boolean singleplayer) {
		if (!singleplayer || QUEUE.isEmpty()) {
			return;
		}
		ClientChunkThrottle.onOverloadSample(QUEUE.size());
		int attempts = Math.min(QUEUE.size(), 64);
		boolean scheduledAny = false;
		forceSchedule = stalledTicks >= 20;
		while (!QUEUE.isEmpty() && attempts-- > 0) {
			if (!ClientChunkThrottle.canScheduleMore()) {
				break;
			}
			CompileEntry next = QUEUE.peek();
			if (next == null) {
				break;
			}
			if (next.cancelled) {
				QUEUE.poll();
				continue;
			}
			int queueSize = next.dispatcher.getCompileQueueSize();
			if (queueSize >= ClientChunkThrottle.getDynamicCompileCap()) {
				break;
			}
			double distSq = next.distanceSq;
			if (next.sectionNode == priorityBoostNode) {
				distSq = 0.0;
				priorityBoostNode = 0L;
			}
			if (!forceSchedule && !ClientChunkThrottle.tryConsumeRebuild(distSq, queueSize, true)) {
				QUEUE.poll();
				next.bumpSequence(sequenceCounter++);
				QUEUE.add(next);
				continue;
			}
			QUEUE.poll();
			BY_SECTION.remove(next.sectionNode);
			next.dispatcher.schedule(next.task);
			scheduledAny = true;
		}
		if (scheduledAny) {
			stalledTicks = 0;
			forceSchedule = false;
		} else {
			stalledTicks++;
		}
	}

	public static int size() {
		return QUEUE.size();
	}

	public static boolean isStalled() {
		return stalledTicks >= 20;
	}

	public static boolean isOverloaded() {
		return QUEUE.size() > ClientChunkThrottle.MAX_COMPILE_QUEUE * 3;
	}

	private static final class CompileEntry {
		private final SectionRenderDispatcher dispatcher;
		private final SectionRenderDispatcher.RenderSection.CompileTask task;
		private final double distanceSq;
		private long sequence;
		private final long sectionNode;
		private boolean cancelled;

		private CompileEntry(SectionRenderDispatcher dispatcher,
			SectionRenderDispatcher.RenderSection.CompileTask task,
			double distanceSq,
			long sequence,
			long sectionNode) {
			this.dispatcher = dispatcher;
			this.task = task;
			this.distanceSq = distanceSq;
			this.sequence = sequence;
			this.sectionNode = sectionNode;
			this.cancelled = false;
		}

		private double distanceSq() {
			return distanceSq;
		}

		private long sequence() {
			return sequence;
		}

		private void bumpSequence(long newSequence) {
			this.sequence = newSequence;
		}
	}
}
