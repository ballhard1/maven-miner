package fr.inria.diverse.maven.resolver.processor;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.zip.ZipException;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;

public class CollectAndResolveArtifactProcessor extends CollectArtifactProcessor {
	
	private int resolved=0;
	
	private int nonResolved=0;
	/**
	 * a class counter
	 */
	private ClassScanCounter counter;
	/**
	 * default constructor
	 */
	public CollectAndResolveArtifactProcessor() {
		super();
	}
	/**
	 * Constructor
	 * @param visitor
	 */
	public CollectAndResolveArtifactProcessor(MultiTaskDependencyVisitor visitor) {
		super(visitor);
	}
	/**
	 * Constructor
	 * @param visitor
	 * @param counter
	 */
	public CollectAndResolveArtifactProcessor (MultiTaskDependencyVisitor visitor, ClassScanCounter counter) {
		super(visitor); 
		this.counter = counter;
	}
	 /**
     * Collects the dependencies of a given artifact, 
     * and resolves the jar in order to count the number of classes 
     * @param artifact {@link Artifact}
     * @throws DependencyCollectionException
     * @throws ArtifactResolutionException
     */
	@Override
	public void process(Artifact artifact) {
		
			super.process(artifact);
			
	        LOGGER.info("Resolving jar file for artifact: {}", artifact);  
	        ArtifactRequest artifactRequest = new ArtifactRequest();
	        artifactRequest.setArtifact( artifact );
	        artifactRequest.addRepository(repo);
	        ArtifactResult artifactResult=null;
	        File jarFile=null;
			try {
				artifactResult = system.resolveArtifact(session, artifactRequest);
				jarFile= artifactResult.getArtifact().getFile();
				 if (jarFile == null) {
					 throw new NullPointerException();
				 }
				 counter.loadJarAndStoreCount(artifactResult.getArtifact());
			     LOGGER.debug("Deleting jar file {}", jarFile.getName());
			     jarFile.delete();
			     resolved++;
			     //TODO use exception wrapper :)
			} catch (ArtifactResolutionException e) {
				LOGGER.error("Unable to collect dependency for artifact {}", artifact);
				e.printStackTrace();
				nonResolved++;
			} catch (MalformedURLException e) {
				LOGGER.error("Unable to collect dependency for artifact {}. The URL {} is malformed", artifact, jarFile.toString());
				e.printStackTrace();
				nonResolved++;

			} catch (ZipException e) {
				LOGGER.error("Unable to collect dependency for artifact {}", artifact);
				e.printStackTrace();
				nonResolved++;

			} catch (IOException e) {
				LOGGER.error("Unable to collect dependency for artifact {}", artifact);
				e.printStackTrace();
				nonResolved++;

			} catch (NullPointerException e) {
				 LOGGER.error("Unable to resolve jar file {}", jarFile.getName());
				 e.printStackTrace();
				 nonResolved++;
			} catch (SecurityException e) {
				LOGGER.error(e.getMessage());
				e.printStackTrace();
				nonResolved++;
			}
	}

	@Override 
	public void report() {
		super.report();
		LOGGER.info("{} artifacts jar have been resolved", resolved);
		LOGGER.info("{} artifacts jar gave failed resolution", nonResolved);
		
	}
}