package uk.co.blackpepper.sdrclient.maven;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.sonatype.plexus.build.incremental.BuildContext;

import uk.co.blackpepper.sdrclient.gen.GeneratedClassWriter;

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
		FileUtils.write(file, content, "UTF-8");
		context.refresh(file);
	}
}
