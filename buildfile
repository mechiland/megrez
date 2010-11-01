VERSION_NUMBER = "1.0.0"
GROUP = "megrez"
COPYRIGHT = ""

require 'buildr/scala'

repositories.remote << "http://www.ibiblio.org/maven2/"
repositories.remote << "http://repository.jboss.org/nexus/content/groups/public/"

NETTY = 'org.jboss.netty:netty:jar:3.2.2.Final'
MOCKITO = 'org.mockito:mockito-all:jar:1.8.5'

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
  end

  define "agent" do
	compile.with NETTY 
	test.resources
	package(:jar).with :manifest=>{ 'Main-Class'=>'org.megrez.agent.Main' }
 end

  define "server" do
    compile.with NETTY
    test.resources
    package(:jar).with :manifest=>{ 'Main-Class'=>'org.megrez.server.Main' }
  end
end
