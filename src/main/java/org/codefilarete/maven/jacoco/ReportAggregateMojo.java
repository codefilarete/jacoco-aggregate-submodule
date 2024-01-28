package org.codefilarete.maven.jacoco;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jacoco.report.IReportGroupVisitor;

/**
 * <p>
 * Creates an aggregated structured code coverage report (HTML, XML, and CSV) from multi-modules
 * Maven project within reactor. The report is created from all sub-modules this project
 * depends on. From those projects class and source files as well as JaCoCo execution data files will be collected.
 * </p>
 *
 * <p>
 * Using the dependency scope allows to distinguish projects which contribute
 * execution data but should not become part of the report:
 * </p>
 *
 * <ul>
 * <li><code>compile</code>, <code>runtime</code>, <code>provided</code>:
 * Project source and execution data is included in the report.</li>
 * <li><code>test</code>: Only execution data is considered for the report.</li>
 * </ul>
 */
@Mojo(name = "report-aggregate", threadSafe = true)
public class ReportAggregateMojo extends AbstractReportMojo {

	/**
	 * A list of execution data files to include in the report from each
	 * project. May use wildcard characters (* and ?). When not specified all
	 * *.exec files from the target folder will be included.
	 */
	@Parameter
	List<String> dataFileIncludes;

	/**
	 * A list of execution data files to exclude from the report. May use
	 * wildcard characters (* and ?). When not specified nothing will be
	 * excluded.
	 */
	@Parameter
	List<String> dataFileExcludes;

	/**
	 * Output directory for the reports. Note that this parameter is only
	 * relevant if the goal is run from the command line or from the default
	 * build lifecycle. If the goal is run indirectly as part of a site
	 * generation, the output directory configured in the Maven Site Plugin is
	 * used instead.
	 */
	@Parameter(defaultValue = "${project.reporting.outputDirectory}/jacoco-aggregate")
	private File outputDirectory;

	/**
	 * The projects in the reactor.
	 */
	@Parameter(property = "reactorProjects", readonly = true)
	private List<MavenProject> reactorProjects;
	
	@Parameter(defaultValue = "${session}", required = true, readonly = true)
	private MavenSession session;
	
	@Override
	public boolean canGenerateReport() {
		boolean canGenerateReport = super.canGenerateReport();

		if (canGenerateReport) {
			if (shouldDelayExecution()) {
				getLog().info("Delaying Report Generation to the end of multi-module project");
				return false;
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Should scanner be delayed?
	 * @return true if goal is attached to phase and not last in a multi-module project
	 */
	private boolean shouldDelayExecution() {
		return !session.getCurrentProject().isExecutionRoot();
	}

	@Override
	void loadExecutionData(ReportSupport support) throws IOException {
		// https://issues.apache.org/jira/browse/MNG-5440
		if (dataFileIncludes == null) {
			dataFileIncludes = Arrays.asList("target/*.exec");
		}

		FileFilter filter = new FileFilter(dataFileIncludes, dataFileExcludes);
		loadExecutionData(support, filter, project.getBasedir());
		for (MavenProject dependency : findProjectToInclude()) {
			loadExecutionData(support, filter, dependency.getBasedir());
		}
	}

	private void loadExecutionData(ReportSupport support, FileFilter filter, File basedir) throws IOException {
		getLog().info("found execution data files in " + basedir + ": " + filter.getFiles(basedir));
		for (File execFile : filter.getFiles(basedir)) {
			support.loadExecutionData(execFile);
		}
	}

	@Override
	File getOutputDirectory() {
		return outputDirectory;
	}

	@Override
	void createReport(IReportGroupVisitor visitor, ReportSupport support) throws IOException {
		IReportGroupVisitor group = visitor.visitGroup(title);
		for (MavenProject dependency : findProjectToInclude()) {
			processProject(support, group, dependency);
		}
	}

	private void processProject(ReportSupport support, IReportGroupVisitor group, MavenProject project)
			throws IOException {
		support.processProject(group, project.getArtifactId(), project, getIncludes(), getExcludes(), sourceEncoding);
	}
	
	@Override
	public File getReportOutputDirectory() {
		return outputDirectory;
	}

	@Override
	public void setReportOutputDirectory(File reportOutputDirectory) {
		if (reportOutputDirectory != null && !reportOutputDirectory.getAbsolutePath().endsWith("jacoco-aggregate")) {
			outputDirectory = new File(reportOutputDirectory, "jacoco-aggregate");
		} else {
			outputDirectory = reportOutputDirectory;
		}
	}
	
	@Override
	public String getOutputName() {
		return "jacoco-aggregate/index";
	}
	
	@Override
	public String getName(Locale locale) {
		return "Codefilarete JaCoCo Aggregate";
	}

	private List<MavenProject> findProjectToInclude() {
		List<MavenProject> result = new ArrayList<>(reactorProjects);
		getLog().info("Projects to be added to report " + buildProjectReferenceId(reactorProjects));
		return result;
	}

	private static String buildProjectReferenceId(List<MavenProject> projects) {
		StringBuilder buffer = new StringBuilder(128);
		for (MavenProject project : projects) {
			buffer.append(buildProjectReferenceId(project)).append(", ");
		}
		return buffer.toString();
	}

	private static String buildProjectReferenceId(MavenProject project) {
        return project.getGroupId() + ':' + project.getArtifactId() + ':' + project.getVersion();
	}
}
