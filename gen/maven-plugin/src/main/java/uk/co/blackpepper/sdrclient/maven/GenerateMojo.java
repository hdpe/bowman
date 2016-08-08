package uk.co.blackpepper.sdrclient.maven;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.sonatype.plexus.build.incremental.BuildContext;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import uk.co.blackpepper.sdrclient.gen.Generator;
import uk.co.blackpepper.sdrclient.gen.ReflectionClassSourceAdapter;
import uk.co.blackpepper.sdrclient.gen.model.ClassSource;

@Mojo(name = "generate",
	requiresProject = true,
	requiresDependencyResolution = ResolutionScope.COMPILE,
	defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class GenerateMojo extends AbstractMojo {

	@Component
	private BuildContext buildContext;

	@Parameter(defaultValue = "${project}")
	private MavenProject project;

	@Parameter(required = true)
	private String packageName;

	@Parameter(defaultValue = "${project.build.directory}/generated-sources/sdrclient", required = true)
	private File targetDirectory;

	@Override
	public void execute() throws MojoExecutionException {

		project.addCompileSourceRoot(targetDirectory.getAbsolutePath());

		Generator generator = new Generator(new LoggerAdapter(getLog()));
		MojoClassWriter classWriter = new MojoClassWriter(buildContext, targetDirectory);

		Collection<ClassInfo> classes = getClasses();

		for (ClassInfo clazz : classes) {
			try {
				ClassSource classSource = new ReflectionClassSourceAdapter(clazz.load());

				generator.generate(classSource, classWriter);
			}
			catch (IOException exception) {
				throw new MojoExecutionException("Couldn't generate class", exception);
			}
		}
	}

	private Collection<ClassInfo> getClasses() throws MojoExecutionException {
		ClassLoader classLoader = newClassLoader();
		ClassPath classPath;

		try {
			classPath = ClassPath.from(classLoader);
		}
		catch (IOException exception) {
			throw new MojoExecutionException("Couldn't read classpath resources", exception);
		}

		return classPath.getTopLevelClasses(packageName);
	}

	private ClassLoader newClassLoader() throws MojoExecutionException {
		List<String> classpathElements = null;

		try {
			classpathElements = project.getCompileClasspathElements();

			List<URL> projectClasspathList = new ArrayList<URL>();

			for (String element : classpathElements) {
				projectClasspathList.add(toURL(element));
			}

			return new URLClassLoader(projectClasspathList.toArray(new URL[0]));
		}
		catch (DependencyResolutionRequiredException exception) {
			throw new MojoExecutionException("Dependency resolution failed", exception);
		}
	}

	private static URL toURL(String element) throws MojoExecutionException {
		try {
			return new File(element).toURI().toURL();
		}
		catch (MalformedURLException exception) {
			throw new MojoExecutionException(element + " is an invalid classpath element", exception);
		}
	}
}
