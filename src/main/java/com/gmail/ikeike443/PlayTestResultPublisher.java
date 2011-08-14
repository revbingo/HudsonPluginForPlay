package com.gmail.ikeike443;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * @author ikeike443
 */
public class PlayTestResultPublisher extends Publisher {
	
	private String app_path;
	
	@DataBoundConstructor
	public PlayTestResultPublisher(String app_path) {
		this.app_path = app_path;
	}


	@Override
	public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
		try {
			System.out.println("App path is " + app_path);
			FilePath root = new FilePath(build.getRootDir());
			FilePath[] files = build.getProject().getWorkspace().list(app_path + "/test-result/*");
			for (FilePath filePath : files) {
				filePath.copyTo(new FilePath(root, "test-result/"+filePath.getName()));
			}
			Properties conf = new Properties();
			InputStream inputStream = new FileInputStream(new File(
					build.getWorkspace() + "/" + app_path + "/conf/application.conf"));
			conf.load(inputStream);
			
			PlayTestResultAction act = new PlayTestResultAction(root);
			act.setPassed(new FilePath(root, "test-result/result.passed").exists());
			act.setAppName(conf.getProperty("application.name"));//TODO set default name
			build.addAction(act);

			inputStream.close();//TODO move down to correct space
			
			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} 

	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl)super.getDescriptor();
	}

	/**
	 * Descriptor for {@link PlayTestResultPublisher}. Used as a singleton.
	 * The class is marked as public so that it can be accessed from views.
	 *
	 * <p>
	 * See <tt>views/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
	 * for the actual HTML fragment for the configuration screen.
	 */
	@Extension // this marker indicates Hudson that this is an implementation of an extension point.
	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}



		@Override
		public String getDisplayName() {
			return "Play! auto-test reports";
		}

	}

}

