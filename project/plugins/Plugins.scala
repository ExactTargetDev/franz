import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  val etRepo = "et-repo" at "http://etinjavbld1.et.local:8081/nexus/content/groups/public/"
  val standardProject = "com.twitter" % "standard-project" % "0.12.7"
  val sbtThrift = "com.twitter" % "sbt-thrift" % "2.0.1"
  val sbtIdeaRepo = "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"
  val sbtIdea = "com.github.mpeltonen" % "sbt-idea-plugin" % "0.4.0"
  val sbtScrooge = "com.twitter" % "sbt-scrooge" % "1.0.2"
}
