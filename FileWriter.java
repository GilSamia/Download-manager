package lab;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.BlockingQueue;

public class FileWriter implements Runnable {

	private final BlockingQueue<Chunk> blockingQueue;
	private MetadataManager metadata;

	public FileWriter(MetadataManager i_metadata, BlockingQueue<Chunk> i_blockingQueue) {
		this.blockingQueue = i_blockingQueue;
		this.metadata = i_metadata;
	}

	private void writeChunks() {
		try {
			File file = new File(metadata.getFileName());
			RandomAccessFile writer = new RandomAccessFile(file, "rw");

			// take chunk from queue, go to offset in the file and write the chunk data.
			while (!this.metadata.isDownloadCompleted()) {

				Chunk chunk = blockingQueue.take();
				writer.seek(chunk.getOffset());
				writer.write(chunk.getData(), 0, chunk.getSize());
				this.metadata.updateDownloadedRanges(chunk);
				writer.close();
			}
		} catch (IOException e) {
			System.err.println(
					"OOPS! Could not write the current chunk, or close the writer.\nPlease try again later." + e);
			System.exit(1);
		} catch (InterruptedException e) {
			System.err.println("OOPS! Something went wrong.\nPlease try again later." + e);
			System.exit(1);
		}
	}

	@Override
	public void run() {
		this.writeChunks();
	}
}
