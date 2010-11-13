VERSION_NUMBER = "1.0.0"
GROUP = "megrez"
COPYRIGHT = ""

require 'buildr/scala'

repositories.remote << "http://www.ibiblio.org/maven2/"
repositories.remote << "http://repository.jboss.org/nexus/content/groups/public/"

NETTY = 'org.jboss.netty:netty:jar:3.2.2.Final'
MOCKITO = 'org.mockito:mockito-all:jar:1.8.5'
SLF4J_API = 'org.slf4j:slf4j-api:jar:1.6.1'
SLF4J_SIMPLE = 'org.slf4j:slf4j-simple:jar:1.6.1'

class Buildr::ScalaTest
  class << self
    alias dependencies_old dependencies

    def dependencies
      dependencies_old + [MOCKITO]
    end
  end
end

desc "The Megrez continuous integration system"
define "megrez" do
  project.version = VERSION_NUMBER
  project.group = GROUP  

  test.using :scalatest
  test.using :properties => { 'agent.vcs.root' => "#{File.dirname(__FILE__)}/agent/src/test/resources/vcs" }
  test.with MOCKITO

  define "core" do
    compile.with SLF4J_API, SLF4J_SIMPLE
	package(:jar)
  end

  define "agent" do
	compile.with NETTY, SLF4J_API, SLF4J_SIMPLE
	compile.with project("core").package
	test.resources
	package(:jar).with :manifest=>{ 'Main-Class'=>'org.megrez.agent.Main' }
    task :run => :package do
        system "mkdir agent-workspace"
        system "scala -classpath $HOME/.m2/repository/org/slf4j/slf4j-api/1.6.1/slf4j-api-1.6.1.jar:$HOME/.m2/repository/org/slf4j/slf4j-simple/1.6.1/slf4j-simple-1.6.1.jar:$HOME/.m2/repository/org/jboss/netty/netty/3.2.2.Final/netty-3.2.2.Final.jar:core/target/megrez-core-1.0.0.jar:agent/target/megrez-agent-1.0.0.jar org.megrez.agent.Main ws://localhost:8051/agent agent-workspace"
    end	
  end

  define "server" do
    compile.with NETTY, SLF4J_API, SLF4J_SIMPLE
	compile.with project("core").package
    test.resources
    package(:jar).with :manifest=>{ 'Main-Class'=>'org.megrez.server.Main' }

    task :run => :package do
        system "scala -classpath $HOME/.m2/repository/org/slf4j/slf4j-api/1.6.1/slf4j-api-1.6.1.jar:$HOME/.m2/repository/org/slf4j/slf4j-simple/1.6.1/slf4j-simple-1.6.1.jar:$HOME/.m2/repository/org/jboss/netty/netty/3.2.2.Final/netty-3.2.2.Final.jar:core/target/megrez-core-1.0.0.jar:server/target/megrez-server-1.0.0.jar org.megrez.server.Main 8051"
    end
  end
end
