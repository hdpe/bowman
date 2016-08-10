package uk.co.blackpepper.sdrclient.maven;

import java.io.File;
import java.io.IOException;

import org.sonatype.plexus.build.incremental.BuildContext;

import uk.co.blackpepper.sdrclient.gen.GeneratedClassWriter;

import static org.apache.commons.io.FileUtils.writeByteArrayToFile;

class MojoClassWriter implements GeneratedClassWriter {

	private final BuildContext context;

	private final File targetDirectory;

	MojoClassWriter(BuildContext context, File targetDirectory) {
		this.context = context;
		this.targetDirectory = targetDirectory;
	}

	@Override
	public void write(String relativePath, String content) throws IOException {
		File file = new File(targetDirectory, relativePath);
		
		long existingFileSize = -1;
		
		if (file.exists()) {
			existingFileSize = file.length();
		}
		
		byte[] newFileContent = content.getBytes("UTF-8");
		
		writeByteArrayToFile(file, newFileContent);
		
		if (newFileContent.length != existingFileSize) {
			context.refresh(file);
		}
	}
}
