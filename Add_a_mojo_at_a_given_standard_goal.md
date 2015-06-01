#Add a mojo at a given phase

# Introduction #

The file META-INF/plexus/components.xml allows to define a new packaging :
here perl-par.

Defining :
  * the extension for the artifact
  * The language in use

# Details #

Add your content here.  Format your content with:
  * Text in **bold** or _italic_
  * Headings, paragraphs, and lists
  * Automatic links to other wiki pages

  * phases
    * initialize
    * process-resources
    * compile
    * process-test-resources
    * test-compile
    * test
    * package
    * install
    * deploy




{{{<?xml version="1.0" encoding="UTF-8"?>


&lt;component-set&gt;


> 

&lt;components&gt;


> > 

&lt;component&gt;


> > > 

&lt;role&gt;

org.apache.maven.artifact.handler.ArtifactHandler

&lt;/role&gt;


> > > <!--
> > > > We want to look this up by dependency-type and POM packaging 'par'

> > > -->
> > > 

&lt;role-hint&gt;

perl-par

&lt;/role-hint&gt;


> > > 

&lt;implementation&gt;

org.apache.maven.artifact.handler.DefaultArtifactHandler
> > > 

&lt;/implementation&gt;


> > > 

&lt;configuration&gt;


> > > > <!--
> > > > > This should always be consistent with the role-hint, to be safe.

> > > > -->
> > > > 

&lt;type&gt;

perl-par

&lt;/type&gt;


> > > > 

&lt;extension&gt;

par

&lt;/extension&gt;


> > > > 

&lt;packaging&gt;

perl-par

&lt;/packaging&gt;


> > > > 

&lt;language&gt;

perl

&lt;/language&gt;



> > > 

&lt;/configuration&gt;



> > 

&lt;/component&gt;




> 

&lt;component&gt;


> > 

&lt;role&gt;

org.apache.maven.lifecycle.mapping.LifecycleMapping

&lt;/role&gt;


> > 

&lt;role-hint&gt;

perl-par

&lt;/role-hint&gt;


> > 

&lt;implementation&gt;

org.apache.maven.lifecycle.mapping.DefaultLifecycleMapping
> > 

&lt;/implementation&gt;


> > 

&lt;configuration&gt;


> > > 

&lt;lifecycles&gt;


> > > > 

&lt;lifecycle&gt;


> > > > > 

&lt;id&gt;

default

&lt;/id&gt;




> 

&lt;phases&gt;


> > <!--
> > > 

&lt;initialize&gt;


> > > fr.biomerieux.bip.plugins:maven-par-plugin:${project.version}:inject-artifact-handler
> > > 

&lt;/initialize&gt;



> > -->
> > 

&lt;process-resources&gt;

org.apache.maven.plugins:maven-resources-plugin:resources
> > 

&lt;/process-resources&gt;


> > <!--
> > > 

&lt;compile&gt;

org.apache.maven.plugins:maven-compiler-plugin:compile
> > > 

&lt;/compile&gt;



> > -->
> > <!--
> > > 

&lt;process-test-resources&gt;

org.apache.maven.plugins:maven-resources-plugin:testResources

&lt;/process-test-resources&gt;


> > > 

&lt;test-compile&gt;

org.apache.maven.plugins:maven-compiler-plugin:testCompile

&lt;/test-compile&gt;


> > > 

&lt;test&gt;

org.apache.maven.plugins:maven-surefire-plugin:test

&lt;/test&gt;



> > -->
> > <!-- perl test running should go here -->
> > 

&lt;test&gt;

fr.biomerieux.bip.plugins:maven-par-plugin:${project.version}:test
> > 

&lt;/test&gt;


> > 

&lt;package&gt;

fr.biomerieux.bip.plugins:maven-par-plugin:${project.version}:par
> > 

&lt;/package&gt;


> > 

&lt;install&gt;

org.apache.maven.plugins:maven-install-plugin:install
> > 

&lt;/install&gt;


> > 

&lt;deploy&gt;

org.apache.maven.plugins:maven-deploy-plugin:deploy
> > 

&lt;/deploy&gt;



> 

&lt;/phases&gt;


> 

&lt;/lifecycle&gt;


> 

&lt;/lifecycles&gt;


> 

&lt;/configuration&gt;


> 

&lt;/component&gt;


> 

&lt;component&gt;


> > 

&lt;role&gt;

org.codehaus.plexus.archiver.Archiver

&lt;/role&gt;


> > 

&lt;role-hint&gt;

par

&lt;/role-hint&gt;


> > 

&lt;implementation&gt;

org.codehaus.plexus.archiver.jar.JarArchiver
> > 

&lt;/implementation&gt;


> > 

&lt;instantiation-strategy&gt;

per-lookup

&lt;/instantiation-strategy&gt;



> 

&lt;/component&gt;



> 

&lt;component&gt;


> > 

&lt;role&gt;

org.codehaus.plexus.archiver.UnArchiver

&lt;/role&gt;


> > 

&lt;role-hint&gt;

par

&lt;/role-hint&gt;


> > 

&lt;implementation&gt;

org.codehaus.plexus.archiver.zip.ZipUnArchiver
> > 

&lt;/implementation&gt;


> > 

&lt;instantiation-strategy&gt;

per-lookup

&lt;/instantiation-strategy&gt;



> 

&lt;/component&gt;


> 

&lt;/components&gt;




&lt;/component-set&gt;


}}}```