package uk.co.blackpepper.sdrclient.gen;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.JavaType;
import org.jboss.forge.roaster.model.JavaUnit;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import uk.co.blackpepper.sdrclient.annotation.RemoteResource;

public class FileParser {

	public Collection<JavaClassSource> parse(InputStream inputStream) throws IOException {
		JavaUnit source = Roaster.parseUnit(IOUtils.toString(inputStream, "UTF-8"));

		List<JavaClassSource> result = new ArrayList<JavaClassSource>();

		for (JavaType<?> type : source.getTopLevelTypes()) {
			JavaClassSource classSource = (JavaClassSource) type;

			if (classSource.hasAnnotation(RemoteResource.class)) {
				result.add(classSource);
			}
		}

		return result;
	}
}
